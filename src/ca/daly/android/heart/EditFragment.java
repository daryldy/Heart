package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.RadioGroup;
import android.text.format.DateUtils;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.ContentValues;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.util.Log;
import java.util.Date;
import java.util.Calendar;

public class EditFragment extends SherlockFragment implements DatabaseHelper.RecordListener,
                                                              Heart.EditListener,
							      DialogInterface.OnClickListener,
							      View.OnClickListener {
  TextView date_field;
  TextView time_field;
  TextView notes_field;
  TextView systolic_field;
  TextView diastolic_field;
  TextView rate_field;
  RadioGroup location;
  RadioGroup side;
  ContentValues rec_orig;
  Long id = null;  // current record's id
  Calendar date_time = Calendar.getInstance();

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setHasOptionsMenu(true);
  }


  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
  			   Bundle savedInstanceState) {
    View result = inflater.inflate(R.layout.editfrag, container);

    date_field = (Button)result.findViewById(R.id.date);
    date_field.setOnClickListener(this);
    time_field = (TextView)result.findViewById(R.id.time);
    time_field.setOnClickListener(this);
    notes_field = (TextView)result.findViewById(R.id.notes);
    systolic_field = (TextView)result.findViewById(R.id.systolic);
    diastolic_field = (TextView)result.findViewById(R.id.diastolic);
    rate_field = (TextView)result.findViewById(R.id.rate);
    location = (RadioGroup)result.findViewById(R.id.location);
    side = (RadioGroup)result.findViewById(R.id.side);
    doReset();
    return(result);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.edit_actions, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  public void setRecord(ContentValues rec) {
    rec_orig = rec;
    date_time.setTimeInMillis(rec_orig.getAsLong(DatabaseHelper.DATE));
    showDateTime();
    systolic_field.setText(rec_orig.getAsString(DatabaseHelper.SYSTOLIC));
    diastolic_field.setText(rec_orig.getAsString(DatabaseHelper.DIASTOLIC));
    rate_field.setText(rec_orig.getAsString(DatabaseHelper.RATE));
    notes_field.setText(rec_orig.getAsString(DatabaseHelper.NOTES));
    location.check(rec_orig.getAsBoolean(DatabaseHelper.LOCATION) ? R.id.upperarm : R.id.forearm);
    side.check(rec_orig.getAsBoolean(DatabaseHelper.SIDE) ? R.id.left : R.id.right);
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public void onPause() {
    doSave();

    Log.d("debug","onPause: id = " + id);

    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d("debug","onResume: id = " + id);
  }

  public void changeRec(Long id) {
    this.id = id;
    DatabaseHelper.getInstance(getActivity()).getRecordAsync(id, this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.delete) {
      Log.d("debug","selected delete");
      new DeleteDialog().show(getActivity().getSupportFragmentManager(),"delete");
      return(true);
    }
    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    // assuming that this is for the delete dialog -- TODO -- how to ensure it is??
    doDelete();
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.date) {
      chooseDate(v);
    } else {
      chooseTime(v);
    }
  }

  private void doReset() {
    showDateTime();

    rec_orig = new ContentValues();
    rec_orig.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
    rec_orig.put(DatabaseHelper.SYSTOLIC,"0");
    rec_orig.put(DatabaseHelper.DIASTOLIC,"0");
    rec_orig.put(DatabaseHelper.RATE,"0");
    rec_orig.put(DatabaseHelper.NOTES,"");
    rec_orig.put(DatabaseHelper.LOCATION,true);   // TRUE = upperarm, FALSE = forearm
    rec_orig.put(DatabaseHelper.SIDE,true);       // TRUE = left, FALSE = right
    notes_field.setText("");   // TODO - s/b getting value from rec_orig
    systolic_field.setText("0");   // TODO - s/b getting value from rec_orig
    diastolic_field.setText("0");   // TODO - s/b getting value from rec_orig
    rate_field.setText("0");   // TODO - s/b getting value from rec_orig
    location.check(rec_orig.getAsBoolean(DatabaseHelper.LOCATION) ? R.id.upperarm : R.id.forearm);
    side.check(rec_orig.getAsBoolean(DatabaseHelper.SIDE) ? R.id.left : R.id.right);
    id = null;
  }

  private void doDelete() {

    if (id != null) {
      DatabaseHelper.getInstance(getActivity()).deleteRecordAsync(id);
    }
    doReset();
  }
  
  private void doSave() {
    if (isDirty()) {
      Log.d ("debug","saving id: " + id);
      ContentValues rec = new ContentValues();
      rec.put(DatabaseHelper.ID,id);
      rec.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
      rec.put(DatabaseHelper.SYSTOLIC,Integer.parseInt(systolic_field.getText().toString()));
      rec.put(DatabaseHelper.DIASTOLIC,Integer.parseInt(diastolic_field.getText().toString()));
      rec.put(DatabaseHelper.RATE,Integer.parseInt(rate_field.getText().toString()));
      rec.put(DatabaseHelper.NOTES,notes_field.getText().toString());
      rec.put(DatabaseHelper.LOCATION,location.getCheckedRadioButtonId() == R.id.upperarm);
      rec.put(DatabaseHelper.SIDE,side.getCheckedRadioButtonId() == R.id.left);
      DatabaseHelper.getInstance(getActivity()).saveRecordAsync(this, rec);
    }
  }

  private boolean isDirty() {
    // TODO -- need to enhance this -- what about other fields???
    return (! (notes_field.getText().toString().equals(rec_orig.getAsString(DatabaseHelper.NOTES))
               && systolic_field.getText().toString().equals(rec_orig.getAsString(DatabaseHelper.SYSTOLIC))
               && diastolic_field.getText().toString().equals(rec_orig.getAsString(DatabaseHelper.DIASTOLIC))
               && rate_field.getText().toString().equals(rec_orig.getAsString(DatabaseHelper.RATE))
	       && rec_orig.getAsLong(DatabaseHelper.DATE) == date_time.getTimeInMillis()
               && rec_orig.getAsBoolean(DatabaseHelper.LOCATION) == (location.getCheckedRadioButtonId() == R.id.upperarm)
               && rec_orig.getAsBoolean(DatabaseHelper.SIDE) == (side.getCheckedRadioButtonId() == R.id.left)
	       ));
    //return (notes_field.isDirty());  // TODO this requires API Level 11 -- probably need to make more generic
  }

  public void chooseDate(View v) {
    new DatePickerDialog(getActivity(),dateListener,date_time.get(Calendar.YEAR),
                                                    date_time.get(Calendar.MONTH),
						    date_time.get(Calendar.DAY_OF_MONTH))
			  .show();
  }

  DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
    public void onDateSet(DatePicker view, 
                          int year, 
			  int monthOfYear,
                          int dayOfMonth) {
       date_time.set(Calendar.YEAR, year);
       date_time.set(Calendar.MONTH, monthOfYear);
       date_time.set(Calendar.DAY_OF_MONTH, dayOfMonth);
       showDateTime();
    }
  };
  
  public void chooseTime(View v) {
    new TimePickerDialog(getActivity(),timeListener,date_time.get(Calendar.HOUR_OF_DAY),
                                                    date_time.get(Calendar.MINUTE),
						    true)
			  .show();
  }

  TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
    public void onTimeSet(TimePicker view, 
                          int hourOfDay, 
                          int minute) {
       date_time.set(Calendar.HOUR_OF_DAY, hourOfDay);
       date_time.set(Calendar.MINUTE, minute);
       showDateTime();
    }
  };
  
  private void showDateTime() {
    date_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
    time_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
  }
}
