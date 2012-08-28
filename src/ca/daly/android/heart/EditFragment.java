package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateUtils;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.util.Log;
import java.util.Date;

public class EditFragment extends SherlockFragment implements DatabaseHelper.RecordListener,
                                                              Heart.EditListener,
							      DialogInterface.OnClickListener {
  TextView date_field;
  TextView time_field;
  TextView notes_field;
  String notes_orig;
  Long id = null;  // current record's id

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

    date_field = (TextView)result.findViewById(R.id.date);
    time_field = (TextView)result.findViewById(R.id.time);
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

  private void doReset() {
    long now = new Date().getTime();
    date_field.setText(DateUtils.formatDateTime(getActivity(), now, DateUtils.FORMAT_SHOW_DATE));
    time_field.setText(DateUtils.formatDateTime(getActivity(), now, DateUtils.FORMAT_SHOW_TIME));

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
}
