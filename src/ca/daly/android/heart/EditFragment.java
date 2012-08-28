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
import android.text.format.DateUtils;
import android.content.Intent;
import android.content.DialogInterface;
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
  String notes_orig;
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
    doReset();
    return(result);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.edit_actions, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  public void setRecord(String notes) {
    notes_orig = notes;
    notes_field.setText(notes_orig);
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
    //long now = new Date().getTime();
    date_time.setTime(new Date());
    date_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
    time_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));

    notes_orig = "";
    notes_field.setText(notes_orig);
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
      DatabaseHelper.getInstance(getActivity()).saveRecordAsync(this, id, notes_field.getText().toString());
    }
  }

  private boolean isDirty() {
    // TODO -- need to enhance this -- what about other fields???
    return (! notes_field.getText().toString().equals(notes_orig));
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
       date_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
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
       time_field.setText(DateUtils.formatDateTime(getActivity(), date_time.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
    }
  };

}
