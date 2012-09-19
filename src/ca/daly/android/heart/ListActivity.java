package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.os.Bundle;
import android.content.Intent;

public class ListActivity extends SherlockFragmentActivity 
      implements ListFragment.ListSelectListener {

  @Override 
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
      getSupportFragmentManager().beginTransaction()
                                 .add(android.R.id.content, new ListFragment()).commit();
    }
  }

  public void RecordSelect(long id) {
    Intent result=new Intent();

    result.putExtra("ca.daly.android.heart.REC_ID",id);
    setResult(RESULT_OK,result);
    finish();
  }
}
