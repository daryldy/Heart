package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.os.Bundle;
import android.content.Intent;

public class ListActivity extends SherlockFragmentActivity 
      implements ListFragment.ListSelectListener {

  @Override 
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list);
  }

  public void RecordSelect(long id) {
    //Intent edit = new Intent(this, Heart.class);
    ////edit.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //startActivity (edit);
    Intent result=new Intent();

    result.putExtra("ca.daly.android.heart.REC_ID",id);
    setResult(RESULT_OK,result);
    finish();
  }
}
