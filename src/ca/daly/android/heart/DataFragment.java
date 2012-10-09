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
				     DataStore {

  private Long id = 0L;  // current record's id (0 = not set)
  public Calendar date_time = Calendar.getInstance();
  public Integer systolic;
  public Integer diastolic;
  public Integer pulse;
  public String notes;
  public Boolean location;
  public Boolean side;
  private Long currentReqTrackNo;
  private EditFragment viewer = null;

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

  public ContentValues Get() {
    ContentValues rtrnVal = new ContentValues();
    Log.d("debug","DataFragment: Get");
    rtrnVal.put(DatabaseHelper.ID,id);
    rtrnVal.put(DatabaseHelper.SYSTOLIC,systolic);
    rtrnVal.put(DatabaseHelper.DIASTOLIC,diastolic);
    rtrnVal.put(DatabaseHelper.PULSE,pulse);
    rtrnVal.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
    rtrnVal.put(DatabaseHelper.NOTES,notes);
    rtrnVal.put(DatabaseHelper.LOCATION,location);
    rtrnVal.put(DatabaseHelper.SIDE,side);
    return rtrnVal;
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


  public void setId(Long id, Long requestTrackNo) {
    if (currentReqTrackNo.equals(requestTrackNo)) {
      // still on the sames record
      this.id = id;
    }
  }

  public void SwitchRecord(Long id) {
    Log.d("debug","SwitchRecord: id = " + id);
    if (id.equals(0L)) {
      // new record
      Log.d("debug","SwitchRecord: initializing");
      initialize();
    } else {
      Log.d("debug","SwitchRecord: loading record");
      DatabaseHelper.getInstance(getActivity()).getRecordAsync(id, this);
    }
  }

  public void Put(ContentValues rec) { 
    if (isDirty(rec)) {
      Log.d ("debug","doSave: saving record");
      rec.put(DatabaseHelper.ID,id);
      currentReqTrackNo = DatabaseHelper.getInstance(getActivity()).SaveRecordAsync(this,rec);
      Toast.makeText(getActivity().getApplicationContext(), getActivity().getApplicationContext().getString(R.string.saved_entry), Toast.LENGTH_LONG).show();
      updateData(rec);
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
    if (viewer != null) {
      viewer.updateView();
    }
  }

  public void SetViewer(EditFragment viewer) {
    this.viewer = viewer;
  }
}
