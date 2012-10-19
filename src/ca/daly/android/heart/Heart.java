package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.util.Log;
import android.net.Uri;
import android.content.ContentResolver;
import java.util.ArrayList;
import android.support.v4.app.FragmentTransaction;

/**
 * Main application activity
 */
public class Heart extends SherlockFragmentActivity 
                   implements ListFragment.ListSelectListener,
		              DatabaseHelper.RecordChangedListener {
  private static final String TAG = "Heart";
  static final int REC_REQUEST=5001;
  private static final String DATA_FRAG="Data Fragment";
  private EditFragment myViewer;
  private ListFragment myList;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    /*
    if (BuildConfig.DEBUG
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      StrictMode.setThreadPolicy(buildPolicy());
    }
    */

    myViewer = (EditFragment)getSupportFragmentManager().findFragmentById(R.id.editfrag_container);
    if (myViewer == null) {
      Log.v (TAG,"onCreate: setting up new edit fragment");
      myViewer = new EditFragment();
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.editfrag_container, myViewer)
				 .commit();
    } else {
      Log.v (TAG,"onCreate: edit fragment still exists");
    }

    myList = (ListFragment)getSupportFragmentManager().findFragmentById(R.id.reclist);
    if (myList == null && findViewById(R.id.reclist) != null) {
      Log.v (TAG,"onCreate: setting up new list fragment");
      myList = new ListFragment();
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.reclist, myList)
				 .commit();
    } else {
      Log.v (TAG,"onCreate: list fragment still exists");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    new MenuInflater(this).inflate(R.menu.options, menu);

    return (super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i = null;

    switch (item.getItemId()) {
      case android.R.id.home:
	return (true);
      case R.id.edit:
        i=new Intent(this, ListActivity.class);
	myViewer.doSave();   // ensure current record is updated to db so list can show it
	startActivityForResult(i,REC_REQUEST);
	return (true);
      case R.id.about:
        i=new Intent(this, SimpleContentActivity.class);
	i.putExtra(SimpleContentActivity.EXTRA_FILE,
	                   "file:///android_asset/misc/about.html");
	startActivity(i);
	return (true);
      case R.id.help:
        i=new Intent(this, SimpleContentActivity.class);
	i.putExtra(SimpleContentActivity.EXTRA_FILE,
	                   "file:///android_asset/misc/help.html");
	startActivity(i);
	return (true);
      case R.id.graph:
	myViewer.doSave();   // ensure current record is updated to db so it can be graphed
        startGraph();
      case R.id.add:
	myList.unselectCurr();
	RecordSelect(0L);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data){
    if (requestCode == REC_REQUEST && resultCode == SherlockFragmentActivity.RESULT_OK) {
      Log.v (TAG, "got activity result: " + data.getExtras().get("ca.daly.android.heart.REC_ID"));
      switchNewEditFrag(data.getLongExtra("ca.daly.android.heart.REC_ID",0));
    }
  }

  @Override 
  protected void onStart() {
    super.onStart();
    DatabaseHelper.getInstance(this).addRecordChangedListener(this);  // need to keep list updated with record changes
  }

  @Override 
  protected void onStop() {
    super.onStop();
    DatabaseHelper.getInstance(this).removeRecordChangedListener(this);
  }

  private void startGraph() {
    Uri uri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                               .authority("ca.daly.android.heart")
			       .build();
    Intent i = new Intent(Intent.ACTION_VIEW,uri);
    i.putExtra(Intent.EXTRA_TITLE, this.getApplicationContext().getString(R.string.graph_title));

    // TODO -- continue to try to get pulse to display correctly as a secondary Y axis
    //i.putExtra("com.googlecode.chartdroid.intent.extra.SERIES_LABELS",new String[] {"Systolic","Diastolic","Pulse"});
    //i.putExtra("com.googlecode.chartdroid.intent.extra.SERIES_AXIS_SELECTION",new int[] {1,1,2});

    i.putExtra("com.googlecode.chartdroid.intent.extra.SERIES_LABELS",new String[] {"Systolic","Diastolic"});

    ArrayList<String> axisTitles = new ArrayList<String>();
    axisTitles.add(""); // date
    axisTitles.add("mmHg");
    //axisTitles.add("Pulse");
    i.putExtra("com.googlecode.chartdroid.intent.extra.AXIS_TITLES",axisTitles);
    i.putExtra("com.googlecode.chartdroid.intent.extra.FORMAT_STRING_Y","%.0f");
    //i.putExtra("com.googlecode.chartdroid.intent.extra.FORMAT_STRING_Y_SECONDARY","%.0f");
    startActivity(i);
  }

  /**
   * receiver for a record selected event from ListFragment
   *   - also handles an Add user request (with id of 0) 
   */
  public void RecordSelect(long id) {
    Log.v (TAG,"RecordSelect: new EditFragment id = " + id);
    switchNewEditFrag(id);
  }
 
  /**
   * switch to new edit fragment for display/edit of a new record
   */
  private void switchNewEditFrag(long id) {
    Bundle args = new Bundle();
    args.putLong("id",id);
    myViewer = new EditFragment();
    myViewer.setArguments(args);
			       /*
			         .setCustomAnimations(android.R.anim.fade_in,
			             android.R.anim.fade_out,
				     android.R.anim.fade_in,
				     android.R.anim.fade_out)
				 .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
			       */
    getSupportFragmentManager().beginTransaction()
				 .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                 .replace(R.id.editfrag_container, myViewer)
				 .commit();
  }

  /**
   * receiver of record changed event from DatabaseHelper
   */
  public void recordChanged(Long id) {
    // a record (id) has changed (could be value(s) changed or could be deleted)
    // for now just refresh the list (if there is one)  
    //   TODO -- might be better if could just update the given record
    Log.v (TAG,"recordChanged: id = " + id);
    if (myList != null) {
      myList.updateList();
    }
  }

  private StrictMode.ThreadPolicy buildPolicy() {
    Log.v (TAG,"Setting strict mode");
    return(new StrictMode.ThreadPolicy.Builder().detectAll()
						.penaltyLog().build());
 }
}
