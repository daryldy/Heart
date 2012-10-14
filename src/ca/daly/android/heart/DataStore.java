package ca.daly.android.heart;

import android.content.ContentValues;

public interface DataStore {
  ContentValues Get();
  void Put(ContentValues rec);
}
