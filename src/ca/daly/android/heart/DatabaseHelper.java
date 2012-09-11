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

public class DatabaseHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME="heart.db";
  private static final int SCHEMA_VERSION=3;
  private static DatabaseHelper singleton=null;
  private Context ctxt=null;
  static final String TABLE="heart";
  static final String ID="_id";
  static final String DATE="date";
  static final String SYSTOLIC="systolic";
  static final String DIASTOLIC="diastolic";
  static final String RATE="heart_rate";
  static final String NOTES="notes";
  static final String LOCATION="location";
  static final String SIDE="side";

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
      db.execSQL("create table heart (_id integer primary key autoincrement, date datetime, systolic integer, notes varchar(50), diastolic integer, heart_rate integer, location boolean, side boolean);");
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
  }

  interface RecordListener{
    void setRecord(ContentValues rec);
    void setId(Long id);
  }

  interface ListAdapterListener {
    void setListAdapter(ListAdapter adapter);
  }

  void loadListAsync(ListAdapterListener listener) {
    new LoadListTask(listener).execute();
  }

  private class LoadListTask extends AsyncTask<Void, Void, Void> {
    private Cursor heartCursor = null;
    private ListAdapterListener listener = null;
    private String[] columns = {SYSTOLIC,DIASTOLIC,RATE,DATE};

    LoadListTask (ListAdapterListener listener) {
      this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
      heartCursor = getReadableDatabase().query(TABLE,new String[] {ID,SYSTOLIC,DIASTOLIC,RATE,DATE},null,null,null,null,"date desc");
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
					new int[] {R.id.systolic,R.id.diastolic,R.id.heart_rate,R.id.date}
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
					new int[] {R.id.systolic,R.id.diastolic,R.id.heart_rate,R.id.date}
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
      String[] args={params[0].toString()};

      Log.d ("debug", "GetRecordTask: doInBackground args[0]: " + args[0]);
      Cursor c = getReadableDatabase().query(TABLE,new String[] {DATE,SYSTOLIC,NOTES,DIASTOLIC,RATE,LOCATION,SIDE},"_id = ?",args,null,null,null,"1");
      c.moveToFirst();
      if (c.isAfterLast()) {
	return(null);
      }
      ContentValues rec = new ContentValues();
      rec.put(DATE,c.getLong(0));
      rec.put(SYSTOLIC,c.getInt(1));
      rec.put(NOTES,c.getString(2));
      rec.put(DIASTOLIC,c.getInt(3));
      rec.put(RATE,c.getInt(4));
      rec.put(LOCATION,c.getInt(5));
      rec.put(SIDE,c.getInt(6));
      c.close();

      Log.d ("debug","doInBackground returning: " + rec.getAsString(SYSTOLIC) + " " + rec.getAsString(NOTES));
      return (rec);
    }

    @Override
    public void onPostExecute(ContentValues rec) {
      listener.setRecord(rec);
    }
  }

  void saveRecordAsync(RecordListener listener,ContentValues rec) {
    new SaveRecordTask(listener).execute(rec);
  }

  private class SaveRecordTask extends AsyncTask<ContentValues, Void, Long> {
    private RecordListener listener = null;
    
    SaveRecordTask(RecordListener listener) {
      this.listener=listener;
    }

    @Override
    protected Long doInBackground(ContentValues... params) {
      ContentValues vals;
      Long result;

      vals = params[0];

      if (vals.getAsLong(ID) == null) {
        Log.d ("debug", "SaveRecordTask: new record");
	result = getWritableDatabase().insert(TABLE,null,vals);
      } else {
        Log.d ("debug", "SaveRecordTask: update record");
	result = getWritableDatabase().replace(TABLE,null,vals);
      }

      Log.d("debug","Finished insert/replace: result = " + result.toString());
      return(result);
    }

    @Override
    public void onPostExecute(Long id) {
      listener.setId(id);
    }
  }

  void deleteRecordAsync(Long id) {
    new DeleteRecordTask().execute(id);
  }

  private class DeleteRecordTask extends AsyncTask<Long, Void, Void> {
    @Override
    protected Void doInBackground(Long... params) {
      String[] arg={params[0].toString()};

      getWritableDatabase().delete(TABLE,"_id = ?",arg);
      return(null);
    }
  }
}
