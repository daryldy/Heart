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

public class EditFragment extends SherlockFragment implements // DatabaseHelper.RecordListener,
                                                              // Heart.EditListener,
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
  private NumberPicker pulse_field;
  private RadioGroup location;
  private RadioGroup side;
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

    setRetainInstance(false);
    View result = inflater.inflate(R.layout.editfrag, null, false);

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
    diastolic_field.setMinValue(50);    // TODO -- what is a reasonable min?
    pulse_field = (NumberPicker)result.findViewById(R.id.pulse);
    pulse_field.setMaxValue(150);   // TODO -- what is a reasonable max?
    pulse_field.setMinValue(40);    // TODO -- what is a reasonable min?
    location = (RadioGroup)result.findViewById(R.id.location);
    side = (RadioGroup)result.findViewById(R.id.side);

    if (savedInstanceState != null) {
      // restore saved state (data & screen)
      date_time.setTimeInMillis(savedInstanceState.getLong(DatabaseHelper.DATE));
      setDateTimeText();
      systolic_field.setValue(savedInstanceState.getInt(DatabaseHelper.SYSTOLIC));
      diastolic_field.setValue(savedInstanceState.getInt(DatabaseHelper.DIASTOLIC));
      pulse_field.setValue(savedInstanceState.getInt(DatabaseHelper.PULSE));
    } else {
      // get data from myData object
      updateView();
    }

    return(result);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.edit_actions, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  public void updateView() {
    Log.d("debug","updateView");
    DataFragment myData;
    myData = ((Heart)getActivity()).myData;
    date_time.setTimeInMillis(myData.date_time.getTimeInMillis());
    setDateTimeText();
    systolic_field.setValue(myData.systolic);
    diastolic_field.setValue(myData.diastolic);
    pulse_field.setValue(myData.pulse);
    notes_field.setText(myData.notes);
    location.check(myData.location ? R.id.upperarm : R.id.forearm);
    side.check(myData.side ? R.id.left : R.id.right);
  }

  @Override
  public void onStop() {
    doSave(true);
    super.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);

    Log.d("debug","onSaveInstanceState");
    state.putInt(DatabaseHelper.SYSTOLIC,systolic_field.getValue());
    state.putInt(DatabaseHelper.DIASTOLIC,diastolic_field.getValue());
    state.putInt(DatabaseHelper.PULSE,pulse_field.getValue());
    state.putLong(DatabaseHelper.DATE,date_time.getTimeInMillis());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.delete) {
      Log.d("debug","selected delete");
      new DeleteDialog().show(getActivity().getSupportFragmentManager(),"delete");
      return(true);
    }
    if (item.getItemId() == R.id.add) {
      Log.d("debug","selected add");
      doSave(false); // this will do everything we need
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

  private void doDelete() {
    ((Heart)getActivity()).myData.doDelete();
  }
  
  public void doSave(boolean notify) {
    ContentValues screenValues = new ContentValues();
    screenValues.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
    screenValues.put(DatabaseHelper.SYSTOLIC,new Integer(systolic_field.getValue()));
    screenValues.put(DatabaseHelper.DIASTOLIC,new Integer(diastolic_field.getValue()));
    screenValues.put(DatabaseHelper.PULSE,new Integer(pulse_field.getValue()));
    screenValues.put(DatabaseHelper.NOTES,notes_field.getText().toString());
    screenValues.put(DatabaseHelper.LOCATION,location.getCheckedRadioButtonId() == R.id.upperarm);
    screenValues.put(DatabaseHelper.SIDE,side.getCheckedRadioButtonId() == R.id.left);
    ((Heart)getActivity()).myData.doSave(screenValues,notify);
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
