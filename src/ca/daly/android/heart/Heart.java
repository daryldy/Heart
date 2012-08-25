package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.widget.TextView;
import android.text.format.DateUtils;
import android.content.Intent;
import android.util.Log;
import java.util.Date;

public class Heart extends SherlockFragmentActivity {
  static final int REC_REQUEST=5001;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
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
	startActivityForResult(i,REC_REQUEST);
	return (true);
      case R.id.about:
        i=new Intent(this, SimpleContentActivity.class);
	startActivity(i);
	return (true);
      case R.id.help:
        i=new Intent(this, SimpleContentActivity.class);
	startActivity(i);
	return (true);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data){
    if (requestCode == REC_REQUEST && resultCode == SherlockFragmentActivity.RESULT_OK) {
      Log.d ("debug", "got activity result: " + data.getExtras().get("ca.daly.android.heart.REC_ID"));
      EditFragment editfrag = (EditFragment)getSupportFragmentManager().findFragmentById(R.id.editfrag);
      editfrag.adjustRec(data.getLongExtra("ca.daly.android.heart.REC_ID",0));
    }
  }

  interface EditListener {
    void adjustRec(Long id);
  }
}
