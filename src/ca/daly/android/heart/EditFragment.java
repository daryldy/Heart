package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.widget.TextView;
import android.text.format.DateUtils;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.util.Log;
import java.util.Date;

public class EditFragment extends SherlockFragment implements DatabaseHelper.RecordListener,
                                                              Heart.EditListener {
  TextView date_field;
  TextView time_field;
  TextView notes_field;
  Long id;  // current record's id

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
  			   Bundle savedInstanceState) {
    View result = inflater.inflate(R.layout.editfrag, container);

    id = null;    // new record
    date_field = (TextView)result.findViewById(R.id.date);
    time_field = (TextView)result.findViewById(R.id.time);
    long now = new Date().getTime();
    date_field.setText(DateUtils.formatDateTime(getActivity(), now, DateUtils.FORMAT_SHOW_DATE));
    time_field.setText(DateUtils.formatDateTime(getActivity(), now, DateUtils.FORMAT_SHOW_TIME));
   
    notes_field = (TextView)result.findViewById(R.id.notes);

    return(result);
  }

  @Override
  public void setRecord(String notes) {
    notes_field.setText(notes);
  }

  @Override
  public void onPause() {
    // TODO -- probably want some conditions on when to save -- i.e. don't save if there is no valid data entered
    Log.d ("debug","saving id: " + id);
    DatabaseHelper.getInstance(getActivity()).saveRecordAsync(id, notes_field.getText().toString());

    super.onPause();
  }

  public void changeRec(Long id) {
    this.id = id;
    DatabaseHelper.getInstance(getActivity()).getRecordAsync(id, this);
  }
}
