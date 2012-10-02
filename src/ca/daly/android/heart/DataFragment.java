package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragment;
import android.content.ContentValues;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;

public class DataFragment extends SherlockFragment 
                          implements DatabaseHelper.RecordListener,
			             Heart.idChangeListener {

  private Long id = 0L;  // current record's id (0 = not set)
  public Calendar date_time = Calendar.getInstance();
  public Integer systolic;
  public Integer diastolic;
  public Integer pulse;
  public String notes;
  public Boolean location;
  public Boolean side;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initialize();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
			   Bundle savedInstanceState) {
    setRetainInstance(true);
    if (savedInstanceState != null) {
      // restore saved state (data & screen)
      Log.d("debug","DataFragment: restoring instance state");
      id = savedInstanceState.getLong(DatabaseHelper.ID);
      systolic = savedInstanceState.getInt(DatabaseHelper.SYSTOLIC);
      diastolic = savedInstanceState.getInt(DatabaseHelper.DIASTOLIC);
      pulse = savedInstanceState.getInt(DatabaseHelper.PULSE);
      date_time.setTimeInMillis(savedInstanceState.getLong(DatabaseHelper.DATE));
      notes = savedInstanceState.getString(DatabaseHelper.NOTES);
      location = savedInstanceState.getBoolean(DatabaseHelper.LOCATION);
      side = savedInstanceState.getBoolean(DatabaseHelper.SIDE);
    }

    Log.d("debug","DataFragment: onCreateView: date_time: " + date_time.getTimeInMillis());
    return (null);   // this is fragment is for data only -- no UI
  }

  @Override
  public void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);

    Log.d("debug","DataFragment: onSaveInstanceState");
    state.putLong(DatabaseHelper.ID,id);
    state.putInt(DatabaseHelper.SYSTOLIC,systolic);
    state.putInt(DatabaseHelper.DIASTOLIC,diastolic);
    state.putInt(DatabaseHelper.PULSE,pulse);
    state.putLong(DatabaseHelper.DATE,date_time.getTimeInMillis());
    state.putString(DatabaseHelper.NOTES,notes);
    state.putBoolean(DatabaseHelper.LOCATION,location);
    state.putBoolean(DatabaseHelper.SIDE,side);
  }

  public void setRecord(ContentValues new_rec) {
    id = new_rec.getAsLong(DatabaseHelper.ID);
    Log.d("debug","DataFragment: setRecord: id =" + id);
    updateData(new_rec);
    updateViewer();
  }

  private void updateData(ContentValues new_data) {
    date_time.setTimeInMillis(new_data.getAsLong(DatabaseHelper.DATE));
    systolic = new_data.getAsInteger(DatabaseHelper.SYSTOLIC);
    diastolic = new_data.getAsInteger(DatabaseHelper.DIASTOLIC);
    pulse = new_data.getAsInteger(DatabaseHelper.PULSE);
    notes = new_data.getAsString(DatabaseHelper.NOTES);
    location = new_data.getAsBoolean(DatabaseHelper.LOCATION);
    side = new_data.getAsBoolean(DatabaseHelper.SIDE);
  }


  public void setId(Long id) {
    this.id = id;
  }

  public void newID(Long id) {
    DatabaseHelper.getInstance(getActivity()).getRecordAsync(id, this);
  }

  public void doSave() {
    ContentValues rec = new ContentValues();
    rec.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
    rec.put(DatabaseHelper.SYSTOLIC,systolic);
    rec.put(DatabaseHelper.DIASTOLIC,diastolic);
    rec.put(DatabaseHelper.PULSE,pulse);
    rec.put(DatabaseHelper.NOTES,notes);
    rec.put(DatabaseHelper.LOCATION,location);
    rec.put(DatabaseHelper.SIDE,side);

    doSave(rec,true);
  }

  public void doSave(ContentValues rec) {
    doSave(rec,true);
  }

  public void doSave(ContentValues rec,boolean idNotify) {
    if (isDirty(rec)) {
      rec.put(DatabaseHelper.ID,id);
      DatabaseHelper.getInstance(getActivity()).saveRecordAsync((idNotify ? this : null), rec);
      Toast.makeText(getActivity().getApplicationContext(), getActivity().getApplicationContext().getString(R.string.saved_entry), Toast.LENGTH_LONG).show();
      updateData(rec);
                         //TODO -- change above UI text to resource
    }
    if (! idNotify) {
      // don't want notification so must be finished with this record
      initialize();
      updateViewer();
    }
  }

  public void doDelete() {
    if (id != 0) {
      DatabaseHelper.getInstance(getActivity()).deleteRecordAsync(id);
    }
    initialize();
    updateViewer();
  }

  private boolean isDirty(ContentValues rec) {
    boolean dirty;

    Log.d("debug","DataFragment: isDirty curr data: date_time:" + date_time.getTimeInMillis()
                                        + " systolic:" + systolic
					+ " diastolic: " + diastolic
					+ " pulse: " + pulse
					+ " notes: " + notes
					+ " location: " + location
					+ " side: " + side);
    Log.d("debug","DataFragment: isDirty data to save: date_time:" + rec.getAsLong(DatabaseHelper.DATE)
                                        + " systolic:" + rec.getAsInteger(DatabaseHelper.SYSTOLIC)
					+ " diastolic: " + rec.getAsInteger(DatabaseHelper.DIASTOLIC)
					+ " pulse: " + rec.getAsInteger(DatabaseHelper.PULSE)
					+ " notes: " + rec.getAsString(DatabaseHelper.NOTES)
					+ " location: " + rec.getAsBoolean(DatabaseHelper.LOCATION)
					+ " side: " + rec.getAsBoolean(DatabaseHelper.SIDE));
    dirty = (date_time.getTimeInMillis() != rec.getAsLong(DatabaseHelper.DATE)
             || !systolic.equals(rec.getAsInteger(DatabaseHelper.SYSTOLIC))
	     || !diastolic.equals(rec.getAsInteger(DatabaseHelper.DIASTOLIC))
	     || !pulse.equals(rec.getAsInteger(DatabaseHelper.PULSE))
	     || !notes.equals(rec.getAsString(DatabaseHelper.NOTES))
	     || !location.equals(rec.getAsBoolean(DatabaseHelper.LOCATION))
	     || !side.equals(rec.getAsBoolean(DatabaseHelper.SIDE)));
    return dirty;
  }

  private void initialize() {
    Log.d("debug","DataFragment: initialize");
    id = 0L;

    // TODO -- better way to set initial values??
    date_time.setTime(new Date());  // today now
    systolic = 121;
    diastolic = 81;
    pulse = 71;
    notes = "";
    location = true;
    side = true;
  }

  private void updateViewer() {
    // TODO -- probably a better way to do this
    if (((Heart)getActivity()).myViewer != null) {
      ((Heart)getActivity()).myViewer.updateView();
    }
  }
}
