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

public class ListFragment extends SherlockListFragment implements DatabaseHelper.ListAdapterListener {

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    SimpleCursorAdapter adapter;

    super.onActivityCreated(savedInstanceState);

    DatabaseHelper.getInstance(getActivity()).loadListAsync(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d("debug","starting destroy");
    ((SimpleCursorAdapter)getListAdapter()).getCursor().close();
    Log.d("debug","finished destroy");
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Log.d("debug","clicked position: " + position + " id: " + id);
    ((ListActivity)getActivity()).RecordSelect(id);
  }

  interface ListSelectListener {
    void RecordSelect(long id);
  }
}
