package ca.daly.android.heart;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME="heart.db";
  private static final int SCHEMA_VERSION=1;
  private static DatabaseHelper singleton=null;
  private Context ctxt=null;

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
    try {
      db.beginTransaction();
      db.execSQL("create table heart (_id integer primary key autoincrement, date datetime, systolic integer, notes varchar(50));");
      db.setTransactionSuccessful();
    }
    finally {
      db.endTransaction();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    throw new RuntimeException(ctxt.getString(R.string.on_upgrade_error));
  }

  interface RecordListener{
    void setRecord(String notes); // TODO -- other fields
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

    LoadListTask (ListAdapterListener listener) {
      this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
      heartCursor = getReadableDatabase().rawQuery("select * from heart",null);
      heartCursor.getCount();  // force query to execute

      return(null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPostExecute(Void nothing) {
      SimpleCursorAdapter adapter;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	adapter=new SimpleCursorAdapter(ctxt, R.layout.row,
					heartCursor, new String[] {
					       "_id",
					       "notes" },
					new int[] { R.id.key, R.id.notes },
					0);
      } else {
	adapter=new SimpleCursorAdapter(ctxt, R.layout.row,
					heartCursor, new String[] {
					       "_id",
					       "notes" },
					new int[] { R.id.key, R.id.notes });
      }
      listener.setListAdapter(adapter);
    }
  }

  void getRecordAsync(Long id, RecordListener listener) {
    new GetRecordTask(listener).execute(id);
  }

  void saveRecordAsync(Long id, String notes) {
    String id_str;
    if (id == null) {
      id_str = "new";
    } else {
      id_str = String.valueOf(id);
    }
    new SaveRecordTask().execute(id_str,notes);
  }

  private class GetRecordTask extends AsyncTask<Long, Void, String> {
    private RecordListener listener = null;
    
    GetRecordTask(RecordListener listener) {
      this.listener=listener;
    }

    @Override
    protected String doInBackground (Long... params) {
      String[] args={params[0].toString()};

      Log.d ("debug", "GetRecordTask: doInBackground args[0]: " + args[0]);
      Cursor c = getReadableDatabase().rawQuery("select notes from heart where _id = ?", args);
      c.moveToFirst();
      if (c.isAfterLast()) {
	return(null);
      }

      String result=c.getString(0);

      c.close();

      Log.d ("debug","doInBackground returning: " + result);
      return (result);
    }

    @Override
    public void onPostExecute(String notes) {
      listener.setRecord(notes);
    }
  }

  private class SaveRecordTask extends AsyncTask<String, Void, Void> {
    //SaveRecordTask(int id, String notes) {
      //this.id = id;
      //this.notes = notes;
    //}

    @Override
    protected Void doInBackground(String... params) {
      String[] args;

      Log.d ("debug", "SaveRecordTask: doInBackground params: " + params[0] + " " + params[1]);
      if (params[0].equals("new")) {
        Log.d ("debug", "SaveRecordTask: new record");
        args = new String[] {params[1]};
        getWritableDatabase().execSQL("INSERT into heart (notes) values (?)", args);
      } else {
        Log.d ("debug", "SaveRecordTask: update record");
	args = new String[] {params[0],params[1]};
        getWritableDatabase().execSQL("INSERT OR REPLACE into heart (_id,notes) values (?,?)", args);
      }
      return(null);
    }
  }
}
