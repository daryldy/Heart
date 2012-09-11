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
    Log.d("debug","running query");
    if ("axes".equals(url.getQueryParameter("aspect") )) {
      // serve the axes metadata
      Log.d("debug","query:axes");
      return (null);
    } else if ("series".equals(url.getQueryParameter("aspect") )) {
      // serve the series metadata
      Log.d("debug","query:series");
      return (null);
    } else {
      Log.d("debug","query:data");
      //SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
      //qBuilder.setTables(DatabaseHelper.TABLE);

      // ignoring sort 

      //Cursor c = qBuilder.query(DatabaseHelper.getInstance(getContext()).getReadableDatabase(),projection,selection,selectionArgs,null,null,null);

      // c.setNotificationUri(getContext().getContentResolver(),url);  TODO -- not needed ???

      //MatrixCursor c = new MatrixCursor(new String[] {"_id","COLUMN_SERIES_INDEX","AXIS_A","AXIS_B"});
      //c.newRow().add(1).add(1).add(System.currentTimeMillis()).add(6);
      //c.newRow().add(1).add(1).add(System.currentTimeMillis()+ 5000).add(50);
      //c.newRow().add(1).add(1).add(System.currentTimeMillis() - 1000).add(95);

      Cursor c = DatabaseHelper.getInstance(getContext()).getReadableDatabase().rawQuery("Select _id,1 as COLUMN_SERIES_INDEX,date as AXIS_A, systolic as AXIS_B from heart",null);
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
