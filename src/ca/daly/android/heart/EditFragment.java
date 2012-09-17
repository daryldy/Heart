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
import android.widget.NumberPicker;
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
  private static final int SYSTOLIC_INIT_VAL = 120;
  private static final int DIASTOLIC_INIT_VAL = 80;
  private static final int PULSE_INIT_VAL = 70;
  private TextView date_field;
  private TextView time_field;
  private TextView notes_field;
  private NumberPicker systolic_field;
  private NumberPicker diastolic_field;
  private NumberPicker rate_field;
  private RadioGroup location;
  private RadioGroup side;
  private Long id = 0L; // current record's id  (0 = not set)
  private Calendar date_time = Calendar.getInstance();

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
    initData();

    date_field = (Button)result.findViewById(R.id.date);
    date_field.setOnClickListener(this);
    time_field = (TextView)result.findViewById(R.id.time);
    time_field.setOnClickListener(this);
    notes_field = (TextView)result.findViewById(R.id.notes);
    systolic_field = (NumberPicker)result.findViewById(R.id.systolic);
    systolic_field.setMaxValue(200);   // TODO -- what is a reasonable max?
    systolic_field.setMinValue(80);    // TODO -- what is a reasonable min?
    diastolic_field = (NumberPicker)result.findViewById(R.id.diastolic);
    diastolic_field.setMaxValue(200);   // TODO -- what is a reasonable max?
    diastolic_field.setMinValue(80);    // TODO -- what is a reasonable min?
    rate_field = (NumberPicker)result.findViewById(R.id.rate);
    rate_field.setMaxValue(150);   // TODO -- what is a reasonable max?
    rate_field.setMinValue(40);    // TODO -- what is a reasonable min?
    location = (RadioGroup)result.findViewById(R.id.location);
    side = (RadioGroup)result.findViewById(R.id.side);
    initScreenValues();

    if (savedInstanceState != null) {
      // restore saved state (data & screen)
      id = savedInstanceState.getLong(DatabaseHelper.ID);
      date_time.setTimeInMillis(savedInstanceState.getLong(DatabaseHelper.DATE));
      setDateTimeText();
      systolic_field.setValue(savedInstanceState.getInt(DatabaseHelper.SYSTOLIC));
      diastolic_field.setValue(savedInstanceState.getInt(DatabaseHelper.DIASTOLIC));
      rate_field.setValue(savedInstanceState.getInt(DatabaseHelper.RATE));
    }

    return(result);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.edit_actions, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  public void setRecord(ContentValues rec) {
    Log.d("debug","setRecord");
    date_time.setTimeInMillis(rec.getAsLong(DatabaseHelper.DATE));
    setDateTimeText();
    systolic_field.setValue(rec.getAsInteger(DatabaseHelper.SYSTOLIC).intValue());
    diastolic_field.setValue(rec.getAsInteger(DatabaseHelper.DIASTOLIC).intValue());
    rate_field.setValue(rec.getAsInteger(DatabaseHelper.RATE).intValue());
    notes_field.setText(rec.getAsString(DatabaseHelper.NOTES));
    location.check(rec.getAsBoolean(DatabaseHelper.LOCATION) ? R.id.upperarm : R.id.forearm);
    side.check(rec.getAsBoolean(DatabaseHelper.SIDE) ? R.id.left : R.id.right);
  }

  public void setId(Long id) {
    Log.d("debug","setId: id = " + id);
    this.id = id;
  }

  @Override
  public void onStop() {
    Log.d("debug","onStop: id = " + id);

    doSave();
    super.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);

    Log.d("debug","onSaveInstanceState");
    state.putLong(DatabaseHelper.ID,id);
    state.putInt(DatabaseHelper.SYSTOLIC,systolic_field.getValue());
    state.putInt(DatabaseHelper.DIASTOLIC,diastolic_field.getValue());
    state.putInt(DatabaseHelper.RATE,rate_field.getValue());
    state.putLong(DatabaseHelper.DATE,date_time.getTimeInMillis());
  }

  public void changeRec(Long id) {
    Log.d("debug","changeRec: id = " + id);
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
    switch (v.getId()) {
      case R.id.date:
        chooseDate(v);
	break;
      case R.id.time:
        chooseTime(v);
	break;
    }
  }

  private void initData() {
    Log.d("debug","initData");
    date_time.setTime(new Date());   // today now
    id = 0L;
  }
  private void initScreenValues() {
    Log.d("debug","initScreenValues");
    notes_field.setText("");
    systolic_field.setValue(SYSTOLIC_INIT_VAL);
    diastolic_field.setValue(DIASTOLIC_INIT_VAL);
    rate_field.setValue(PULSE_INIT_VAL);
    location.check(R.id.upperarm);
    side.check(R.id.left);
    setDateTimeText();
  }
  
  private void doDelete() {

    if (id != 0) {
      DatabaseHelper.getInstance(getActivity()).deleteRecordAsync(id);
    }
    initData();
    initScreenValues();
  }
  
  private void doSave() {
    if (isDirty()) {
      Log.d ("debug","saving id: " + id);
      ContentValues rec = new ContentValues();
      rec.put(DatabaseHelper.ID,id);
      rec.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
      rec.put(DatabaseHelper.SYSTOLIC,new Integer(systolic_field.getValue()));
      rec.put(DatabaseHelper.DIASTOLIC,new Integer(diastolic_field.getValue()));
      rec.put(DatabaseHelper.RATE,new Integer(rate_field.getValue()));
      rec.put(DatabaseHelper.NOTES,notes_field.getText().toString());
      rec.put(DatabaseHelper.LOCATION,location.getCheckedRadioButtonId() == R.id.upperarm);
      rec.put(DatabaseHelper.SIDE,side.getCheckedRadioButtonId() == R.id.left);
      DatabaseHelper.getInstance(getActivity()).saveRecordAsync(this, rec);
      Toast.makeText(getActivity().getApplicationContext(), "Saved Entry", Toast.LENGTH_SHORT).show();
                         //TODO -- change above UI text to resource
    }
  }

  private boolean isDirty() {
    // if main values are different then their "new" record values then assume record is dirty
    return (systolic_field.getValue() != SYSTOLIC_INIT_VAL
              || diastolic_field.getValue() != DIASTOLIC_INIT_VAL
	      || rate_field.getValue() != PULSE_INIT_VAL);
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
       setDateTimeText();
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
       setDateTimeText();
    }
  };
  
  private void setDateTimeText() {
    date_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
    time_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
  }
}
