package ca.daly.android.heart;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import java.lang.System;

public class Provider extends ContentProvider {
 
  private final static String TAG = "Provider";

  @Override
  public boolean onCreate() {
    return (true);
  }
  
  @Override
  public String getType(Uri url) {
    return ("vnd.android.cursor.dir/vnd.com.googlecode.chartdroid.timeline");
  }

  @Override
  public Cursor query(Uri url, String[] projection, String selection,
                        String[] selectionArgs, String sort) {
    Log.v(TAG,"running query");
    if ("axes".equals(url.getQueryParameter("aspect") )) {
      // serve the axes metadata
      Log.v(TAG,"query:axes");

      // TODO -- continue to try to get pulse to display correctly as a secondary Y axis
      //MatrixCursor c = new MatrixCursor(new String[] {"_id","COLUMN_AXIS_LABEL","COLUMN_AXIS_ROLE","COLUMN_AXIS_MIN","COLUMN_AXIS_MAX"});
      //c.newRow().add(1).add("").add(0).add(null).add(null);
      //c.newRow().add(2).add("mmHg").add(1).add(null).add(null);
      //c.newRow().add(3).add("Pulse").add(1).add(50).add(90);

      //MatrixCursor c = new MatrixCursor(new String[] {"_id","COLUMN_AXIS_LABEL"});
      //c.newRow().add(1).add("");
      //c.newRow().add(2).add("mmHg");

      //return (c);

      return (null);
    } else if ("series".equals(url.getQueryParameter("aspect") )) {
      // serve the series metadata
      Log.v(TAG,"query:series");

      // TODO -- continue to try to get pulse to display correctly as a secondary Y axis

      //MatrixCursor c = new MatrixCursor(new String[] {"_id","COLUMN_SERIES_LABEL","COLUMN_SERIES_AXIS_SELECT"});
      //c.newRow().add(1).add("Systolic").add(1);
      //c.newRow().add(2).add("Diastolic").add(1);
      //c.newRow().add(3).add("Pulse").add(1);

      //MatrixCursor c = new MatrixCursor(new String[] {"_id","COLUMN_SERIES_LABEL"});
      //c.newRow().add(1).add("Systolic");
      //c.newRow().add(2).add("Diastolic");

      //return (c);

      return (null);
    } else {
      Log.v(TAG,"query:data");

      // TODO -- continue to try to get pulse to display correctly as a secondary Y axis
      //Cursor c = DatabaseHelper.getInstance(getContext()).getReadableDatabase().rawQuery("Select _id,1 as COLUMN_SERIES_INDEX,date as AXIS_A, systolic as AXIS_B from heart union all select _id,2 as COLUMN_SERIES_INDEX,date as AXIS_A, diastolic as AXIS_B from heart union all select _id,3 as COLUMN_SERIES_INDEX,date as AXIS_A, pulse as AXIS_C from heart",null);

      Cursor c = DatabaseHelper.getInstance(getContext()).getReadableDatabase().rawQuery("Select _id,1 as COLUMN_SERIES_INDEX,date as AXIS_A, systolic as AXIS_B from heart union all select _id,2 as COLUMN_SERIES_INDEX,date as AXIS_A, diastolic as AXIS_B from heart order by date",null);
      return (c);
    }
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) {
    throw new RuntimeException("Operation not supported");
  }

  @Override
  public int update(Uri url, ContentValues values, String where,
                      String[] whereArgs) {
    throw new RuntimeException("Operation not supported");
  }

  @Override
  public int delete(Uri url, String where, String[] whereArgs) {
    throw new RuntimeException("Operation not supported");
  }
}
