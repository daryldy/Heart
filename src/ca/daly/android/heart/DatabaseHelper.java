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


package ca.daly.android.heart;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.util.Log;
import android.text.format.DateUtils;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static final String TAG = "DatabaseHelper";
  private static final String DATABASE_NAME="heart.db";
  private static final int SCHEMA_VERSION=4;
  private static DatabaseHelper singleton=null;
  private Context ctxt=null;
  static final String TABLE="heart";
  static final String ID="_id";
  static final String DATE="date";
  static final String SYSTOLIC="systolic";
  static final String DIASTOLIC="diastolic";
  static final String PULSE="pulse";
  static final String NOTES="notes";
  static final String LOCATION="location";
  static final String SIDE="side";
  private ArrayList<RecordChangedListener> recordChangedListeners = new ArrayList<RecordChangedListener>();
  private Long requestSerialNo = 1L;

  synchronized static DatabaseHelper getInstance(Context ctxt) {
    if (singleton == null) {
      singleton=new DatabaseHelper(ctxt.getApplicationContext());
    }
    return(singleton);
  }

  public DatabaseHelper(Context ctxt) {
    super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
    this.ctxt=ctxt;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
          // location : True = upper_arm, False = forearm
	  // side     : True = left, False = right
    try {
      db.beginTransaction();
      db.execSQL("create table heart (_id integer primary key autoincrement, date datetime, systolic integer, notes varchar(50), diastolic integer, pulse integer, location boolean, side boolean);");
      db.setTransactionSuccessful();
    }
    finally {
      db.endTransaction();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    if (newVersion == 2) {
      try {
	db.beginTransaction();
	db.execSQL("alter table heart add column diastolic integer;");
	db.execSQL("alter table heart add column heart_rate integer;");
	db.execSQL("alter table heart add column upper_arm boolean;");
	db.execSQL("alter table heart add column left boolean;");
	db.setTransactionSuccessful();
      }
      finally {
	db.endTransaction();
      }
    }
    if (newVersion == 3) {
      // not a good conversion -- okay for now because versions prior to 3 were never deployed
      try {
	db.beginTransaction();
	db.execSQL("alter table heart rename to heart_v2;");
        db.execSQL("create table heart (_id integer primary key autoincrement, date datetime, systolic integer, notes varchar(50), diastolic integer, heart_rate integer, location boolean, side boolean);");
	db.setTransactionSuccessful();
      }
      finally {
	db.endTransaction();
      }
    }
    if (newVersion == 4) {
      // not a good conversion -- okay for now because versions prior to 4 were never deployed
      try {
	db.beginTransaction();
	db.execSQL("drop table heart_v2;");
	db.execSQL("alter table heart rename to heart_v2;");
        db.execSQL("create table heart (_id integer primary key autoincrement, date datetime, systolic integer, notes varchar(50), diastolic integer, pulse integer, location boolean, side boolean);");
	db.setTransactionSuccessful();
      }
      finally {
	db.endTransaction();
      }
    }
  }

  interface RecordChangedListener {
    void recordChanged(Long id);
  }

  interface RecordListener{
    void setRecord(ContentValues rec);
    void setId(Long id);
  }

  interface ListAdapterListener {
    void setListAdapter(ListAdapter adapter);
  }

  public void addRecordChangedListener(RecordChangedListener listener) {
    recordChangedListeners.add(listener);
    if (BuildConfig.DEBUG) {
      Log.v (TAG, "addRecordChangedListener: size = " + recordChangedListeners.size());
    }
  }

  public void removeRecordChangedListener(RecordChangedListener listener) {
    recordChangedListeners.remove(listener);
    if (BuildConfig.DEBUG) {
      Log.v (TAG, "removeRecordChangedListener: size = " + recordChangedListeners.size());
    }
  }

  void loadListAsync(ListAdapterListener listener) {
    new LoadListTask(listener).execute();
  }

  private class LoadListTask extends AsyncTask<Void, Void, Void> {
    private Cursor heartCursor = null;
    private ListAdapterListener listener = null;
    private String[] columns = {SYSTOLIC,DIASTOLIC,PULSE,DATE};

    LoadListTask (ListAdapterListener listener) {
      this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
      heartCursor = getReadableDatabase().query(TABLE,new String[] {ID,SYSTOLIC,DIASTOLIC,PULSE,DATE},null,null,null,null,"date desc");
                               // TODO -- should really be using columns value plus ID
			       //         -- java seems make this harder then it should be!!!!
      heartCursor.getCount();  // force query to execute

      return(null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPostExecute(Void nothing) {
      SimpleCursorAdapter adapter;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	adapter=new SimpleCursorAdapter(ctxt, 
	                                R.layout.row,
					heartCursor, 
					columns,
					new int[] {R.id.systolic,R.id.diastolic,R.id.pulse,R.id.date}
					, 0) {
			    @Override
			    public void setViewText(TextView v, String text) {
			      super.setViewText(v, convText(v, text));
			    }    
			  };
      } else {
	adapter=new SimpleCursorAdapter(ctxt, 
				        R.layout.row,
					heartCursor, 
					columns,
					new int[] {R.id.systolic,R.id.diastolic,R.id.pulse,R.id.date}
					) {
			    @Override
			    public void setViewText(TextView v, String text) {
			      super.setViewText(v, convText(v, text));
			    }    
			  };
      }
      listener.setListAdapter(adapter);
    }

    private String convText(TextView v, String text) {

      switch (v.getId()) {
        case R.id.date:
		String formatedText = text;
                formatedText = DateUtils.formatDateTime(ctxt, Long.parseLong(text), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
		return formatedText;
      }
      return text;
    }
  }

  void getRecordAsync(Long id, RecordListener listener) {
    new GetRecordTask(listener).execute(id);
  }

  private class GetRecordTask extends AsyncTask<Long, Void, ContentValues> {
    private RecordListener listener = null;
    
    GetRecordTask(RecordListener listener) {
      this.listener=listener;
    }

    @Override
    protected ContentValues doInBackground (Long... params) {
      ContentValues rec = null;
      String[] args={params[0].toString()};

      if (BuildConfig.DEBUG) {
	Log.v (TAG, "GetRecordTask: doInBackground args[0]: " + args[0]);
      }
      Cursor c = getReadableDatabase().query(TABLE,new String[] {ID,DATE,SYSTOLIC,NOTES,DIASTOLIC,PULSE,LOCATION,SIDE},"_id = ?",args,null,null,null,"1");
      c.moveToFirst();
      if (! c.isAfterLast()) {
	rec = new ContentValues();
	rec.put(ID,c.getLong(0));
	rec.put(DATE,c.getLong(1));
	rec.put(SYSTOLIC,c.getInt(2));
	rec.put(NOTES,c.getString(3));
	rec.put(DIASTOLIC,c.getInt(4));
	rec.put(PULSE,c.getInt(5));
	rec.put(LOCATION,c.getInt(6));
	rec.put(SIDE,c.getInt(7));

	if (BuildConfig.DEBUG) {
	  Log.v (TAG,"doInBackground returning: " + rec.getAsString(SYSTOLIC) + " " + rec.getAsString(NOTES));
	}
      }

      c.close();
      return (rec);
    }

    @Override
    public void onPostExecute(ContentValues rec) {
      listener.setRecord(rec);
    }
  }

  public Long SaveRecordAsync(RecordListener listener,ContentValues rec) {
    Long serialNo = getNewSerialNo();
    new SaveRecordTask(listener,serialNo).execute(rec);
    return serialNo;
  }

  private class SaveRecordTask extends AsyncTask<ContentValues, Void, Long> {
    private RecordListener listener = null;
    private Long serialNo;
    
    SaveRecordTask(RecordListener listener,Long serialNo) {
      this.listener=listener;
      this.serialNo = serialNo;
    }

    @Override
    protected Long doInBackground(ContentValues... params) {
      ContentValues vals;
      Long result;

      vals = params[0];

      if (vals.getAsLong(ID) == 0) {
	if (BuildConfig.DEBUG) {
	  Log.v (TAG, "SaveRecordTask: new record");
	}
	vals.remove(ID);
	result = getWritableDatabase().insert(TABLE,null,vals);
      } else {
	if (BuildConfig.DEBUG) {
	  Log.v (TAG, "SaveRecordTask: update record");
	}
	result = getWritableDatabase().replace(TABLE,null,vals);
      }

      if (BuildConfig.DEBUG) {
	Log.v(TAG,"Finished insert/replace: result = " + result.toString());
      }
      return(result);
    }

    @Override
    public void onPostExecute(Long id) {
      if (listener != null) {
        listener.setId(id);
      }

      for (RecordChangedListener lnr: recordChangedListeners) {
        lnr.recordChanged(id);
      }
    }
  }

  void deleteRecordAsync(Long id) {
    new DeleteRecordTask().execute(id);
  }

  private class DeleteRecordTask extends AsyncTask<Long, Void, Long> {
    @Override
    protected Long doInBackground(Long... params) {
      Long id = params[0];
      String[] arg={id.toString()};

      getWritableDatabase().delete(TABLE,"_id = ?",arg);
      return(id);
    }
    
    @Override
    public void onPostExecute(Long id) {
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"DeleteRecordTask: onPostExecute");
      }
      for (RecordChangedListener lnr: recordChangedListeners) {
	if (BuildConfig.DEBUG) {
	  Log.v (TAG,"DeleteRecordTask: onPostExecute: sending notification");
	}
        lnr.recordChanged(id);
      }
    }
  }

  private Long getNewSerialNo() {
    requestSerialNo++;
    if (BuildConfig.DEBUG) {
      Log.v(TAG,"getNewSerialNo: issued new serial number: " + requestSerialNo);
    }
    return requestSerialNo;
  }
}
