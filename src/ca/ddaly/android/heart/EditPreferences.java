/*
 *  Copyright (C) 2013 Daryl Daly
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

package ca.ddaly.android.heart;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.util.Log;
import android.text.format.DateFormat;
import java.text.ParseException;
import java.util.Locale;

public class EditPreferences extends SherlockPreferenceActivity
                             implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = "EditPreferences";
  public static final String TIME_FILTER_KEY = "time_filter";
  public static final String START_TIME_KEY = "start_time";
  public static final String END_TIME_KEY = "end_time";
  public static final String DATE_FILTER_KEY = "date_filter";
  public static final String START_DATE_KEY = "start_date";
  public static final String END_DATE_KEY = "end_date";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    // set initial preference information
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    onSharedPreferenceChanged(prefs,TIME_FILTER_KEY);
    onSharedPreferenceChanged(prefs,START_TIME_KEY);
    onSharedPreferenceChanged(prefs,END_TIME_KEY);
    onSharedPreferenceChanged(prefs,DATE_FILTER_KEY);
    onSharedPreferenceChanged(prefs,START_DATE_KEY);
    onSharedPreferenceChanged(prefs,END_DATE_KEY);
  }

  @Override
  protected void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (key.equals(TIME_FILTER_KEY)) {
      // changed master flag -- enable/disable child prefs
      Preference startTime = findPreference(START_TIME_KEY);
      startTime.setEnabled(sharedPreferences.getBoolean(key,false)); 
      Preference endTime = findPreference(END_TIME_KEY);
      endTime.setEnabled(sharedPreferences.getBoolean(key,false)); 
    }
    if (key.equals(START_TIME_KEY) || key.equals(END_TIME_KEY)) {
      Preference pref = findPreference(key);
      String time = sharedPreferences.getString(key,"");
      java.text.DateFormat displayDF = DateFormat.getTimeFormat(this);   // for display of Time (using Locale default)
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onSharedPreferenceChanged: time = " + time);
      }
      try {
        pref.setSummary(displayDF.format(TimePickerPreference.STORAGE_FORMAT_PARSER.parse(time)));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    if (key.equals(DATE_FILTER_KEY)) {
      // changed master flag -- enable/disable child prefs
      findPreference(START_DATE_KEY).setEnabled(sharedPreferences.getBoolean(key,false)); 
      findPreference(END_DATE_KEY).setEnabled(sharedPreferences.getBoolean(key,false)); 
    }
    if (key.equals(START_DATE_KEY) || key.equals(END_DATE_KEY)) {
      Preference pref = findPreference(key);
      String date = sharedPreferences.getString(key,"");
      java.text.DateFormat displayDF = DateFormat.getDateFormat(this);     // for display of Date (using Locale default)

      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onSharedPreferenceChanged: date = " + date);
      }
      try {
        pref.setSummary(displayDF.format(DatePickerPreference.STORAGE_FORMAT_PARSER.parse(date)));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
  }
}
