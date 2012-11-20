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
