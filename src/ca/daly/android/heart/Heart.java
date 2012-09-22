package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.util.Log;
import android.net.Uri;
import android.content.ContentResolver;
import java.util.ArrayList;

public class Heart extends SherlockFragmentActivity {
  static final int REC_REQUEST=5001;
  private static final String DATA_FRAG="Data Fragment";
  public DataFragment myData;  // TODO -- s/b private
  public EditFragment myViewer;  // TODO -- s/b private

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    myData = (DataFragment)getSupportFragmentManager().findFragmentByTag(DATA_FRAG);
    if (myData == null) {
      Log.d ("debug","Heart Activity: onCreate: setting up new data fragment");
      myData = new DataFragment();
      getSupportFragmentManager().beginTransaction()
                                 .add(myData,DATA_FRAG)
				 .commit();
    } else {
      Log.d ("debug","Heart Activity: onCreate: data fragment still exists");
    }

    myViewer = (EditFragment)getSupportFragmentManager().findFragmentById(R.id.editfrag_container);
    if (myViewer == null) {
      Log.d ("debug","Heart Activity: onCreate: setting up new edit fragment");
      myViewer = new EditFragment();
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.editfrag_container, myViewer)
				 .commit();
    } else {
      Log.d ("debug","Heart Activity: onCreate: edit fragment still exists");
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

    EditFragment editfrag = (EditFragment)getSupportFragmentManager().findFragmentById(R.id.editfrag_container);
    switch (item.getItemId()) {
      case android.R.id.home:
	return (true);
      case R.id.edit:
        i=new Intent(this, ListActivity.class);
	editfrag.doSave(true);   // ensure current record is updated to db so list can show it
	startActivityForResult(i,REC_REQUEST);
	return (true);
      case R.id.about:
        i=new Intent(this, SimpleContentActivity.class);
	startActivity(i);
	return (true);
      case R.id.help:
        i=new Intent(this, SimpleContentActivity.class);
	startActivity(i);
	return (true);
      case R.id.graph:
	editfrag.doSave(true);   // ensure current record is updated to db so it can be graphed
        startGraph();
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data){
    if (requestCode == REC_REQUEST && resultCode == SherlockFragmentActivity.RESULT_OK) {
      Log.d ("debug", "got activity result: " + data.getExtras().get("ca.daly.android.heart.REC_ID"));
      myData.newID(data.getLongExtra("ca.daly.android.heart.REC_ID",0));
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d ("debug","Heart Activity: onStop");
  }

  interface idChangeListener {
    void newID(Long id);
  }

  private void startGraph() {
    Uri uri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                               .authority("ca.daly.android.heart")
			       .build();
    Intent i = new Intent(Intent.ACTION_VIEW,uri);
    i.putExtra(Intent.EXTRA_TITLE, "Blood Pressure");

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
}
