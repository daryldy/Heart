package ca.daly.android.heart;

import android.content.ContentValues;

public interface DataStore {
  void SwitchRecord(Long id);
  ContentValues Get();
  void Put(ContentValues rec);
  void SetViewer(EditFragment viewer);
}
