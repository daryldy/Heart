/*
 *  Copyright (C) 2012 Daryl Daly
 *
 *  This file is part of Heart Observe
 *
 *  Heart Observe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Heart Observe is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.ddaly.android.heart;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.util.Log;
import android.net.Uri;
import android.content.ActivityNotFoundException;
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
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onCreate: setting up new edit fragment");
      }
      myViewer = new EditFragment();
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.editfrag_container, myViewer)
				 .commit();
    } else {
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onCreate: edit fragment still exists");
      }
    }

    myList = (ListFragment)getSupportFragmentManager().findFragmentById(R.id.reclist);
    if (myList == null && findViewById(R.id.reclist) != null) {
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onCreate: setting up new list fragment");
      }
      myList = new ListFragment();
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.reclist, myList)
				 .commit();
    } else {
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onCreate: list fragment still exists");
      }
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
        doGraph();
      case R.id.add:
	myList.unselectCurr();
	RecordSelect(0L);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data){
    if (requestCode == REC_REQUEST && resultCode == SherlockFragmentActivity.RESULT_OK) {
      if (BuildConfig.DEBUG) {
	Log.v (TAG, "got activity result: " + data.getExtras().get("ca.ddaly.android.heart.REC_ID"));
      }
      switchNewEditFrag(data.getLongExtra("ca.ddaly.android.heart.REC_ID",0));
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

  private void doGraph() {
    TimePressureGraph graph = TimePressureGraph.getInstance(this);
    Intent i = graph.getIntent();
    if ( i != null ) {
      startActivity(graph.getIntent());
    } else {
      Toast.makeText(this.getApplicationContext(), this.getApplicationContext().getString(R.string.no_graph_data), Toast.LENGTH_LONG).show();
    }
  }

  /**
   * receiver for a record selected event from ListFragment
   *   - also handles an Add user request (with id of 0) 
   */
  public void RecordSelect(long id) {
    if (BuildConfig.DEBUG) {
      Log.v (TAG,"RecordSelect: new EditFragment id = " + id);
    }
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
    if (BuildConfig.DEBUG) {
      Log.v (TAG,"recordChanged: id = " + id);
    }
    if (myList != null) {
      myList.updateList();
    }
  }

  private StrictMode.ThreadPolicy buildPolicy() {
    if (BuildConfig.DEBUG) {
      Log.v (TAG,"Setting strict mode");
    }
    return(new StrictMode.ThreadPolicy.Builder().detectAll()
						.penaltyLog().build());
 }
}
