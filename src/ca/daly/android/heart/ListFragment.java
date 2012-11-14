package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockListFragment;
import android.os.Bundle;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.view.View;

public class ListFragment extends SherlockListFragment 
                          implements DatabaseHelper.ListAdapterListener {

  private final static String TAG = "ListFragment";
  private int currPos = -1;
  private long currID = -1;

  interface ListSelectListener {
    void RecordSelect(long id);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (BuildConfig.DEBUG) {
      Log.v(TAG,"onActivityCreated");
    }
    updateList();
    getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    if (BuildConfig.DEBUG) {
      Log.v(TAG,"onViewCreated");
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (BuildConfig.DEBUG) {
      Log.v(TAG,"starting destroy");
    }
    ((SimpleCursorAdapter)getListAdapter()).getCursor().close();
    if (BuildConfig.DEBUG) {
      Log.v(TAG,"finished destroy");
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (BuildConfig.DEBUG) {
      Log.v(TAG,"clicked position: " + position + " id: " + id);
    }
    ((ListSelectListener)getActivity()).RecordSelect(id);
    currID = id;
    currPos = position;
  }

  @Override
  public void setListAdapter(ListAdapter adapter) {
    if (BuildConfig.DEBUG) {
      Log.v(TAG,"setListAdapter");
    }
    super.setListAdapter(adapter);
    // cannot use currPos here because sometimes the currPos does not
    // match with the currID on the new adapter (this occurs when adding
    // a new record for example)
    if (currID > -1) {
      for (int position = 0; position < adapter.getCount(); position++) {
	if (adapter.getItemId(position) == currID) {
	  getListView().setItemChecked(position,true);
	  currPos = position;
	  break;
	}
      }
    }
  }

  public void updateList() {
    if (BuildConfig.DEBUG) {
      Log.v(TAG,"updateList");
    }
    DatabaseHelper.getInstance(getActivity()).loadListAsync(this);
  }
 
  /**
   * remove selection highlight from current position
   */
  public void unselectCurr() {
    if (currPos > -1) {
      getListView().setItemChecked(currPos,false);
      currPos = -1;  // reset to nothing selected
      currID = -1;
    }
  }
}
