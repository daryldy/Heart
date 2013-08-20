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

import android.preference.DialogPreference;
import android.preference.Preference.BaseSavedState;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.os.Parcel;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import java.lang.String;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TimePickerPreference extends DialogPreference {
  private static final String TAG = "TimePickerPreference";
  public static final DateFormat STORAGE_FORMAT_PARSER = new SimpleDateFormat("HH:mm",Locale.US);
  private TimePicker myPicker = null;
  private String lastTime = null;    // in storage format HH:mm
  private Context ctxt = null;

  public TimePickerPreference(Context context, AttributeSet attrs) {
    super(context,attrs);

    ctxt = context;
    setDialogLayoutResource(R.layout.timepicker_dialog);
    setPositiveButtonText(android.R.string.ok);
    setNegativeButtonText(android.R.string.cancel);

    setDialogIcon(null);
  }

  @Override
  protected void onBindDialogView(View view){
    super.onBindDialogView(view);

    if (BuildConfig.DEBUG) {
      Log.v (TAG,"onBindDialogView");
    }
    myPicker = (TimePicker) view.findViewById(R.id.timePicker);
    myPicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(ctxt));
    if (lastTime != null) {
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onBindDialogView: setting picker values");
      }
      updatePicker();
    }
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      if (BuildConfig.DEBUG) {
	Log.v (TAG,"onDialogClosed: persisting time");
      }
      lastTime = formatForStorage();
      persistString(lastTime);
    }
  }

  @Override
  protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    if (BuildConfig.DEBUG) {
      Log.v (TAG,"onSetInitialValue");
    }
    if (restorePersistedValue) {
      lastTime = this.getPersistedString("00:00");
    } else {
      lastTime = (String) defaultValue;
      persistString(lastTime);
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return new String(a.getString(index));
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    final Parcelable superState = super.onSaveInstanceState();
    // Check whether this Preference is persistent (continually saved)
    if (isPersistent()) {
        // No need to save instance state since it's persistent, use superclass state
        return superState;
    }

    // Create instance of custom BaseSavedState
    final SavedState myState = new SavedState(superState);
    // Set the state's value with the class member that holds current setting value
    myState.value = formatForStorage();
    return myState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (BuildConfig.DEBUG) {
      Log.v (TAG,"onRestoreInstanceState");
    }

    // Check whether we saved the state in onSaveInstanceState
    if (state == null || !state.getClass().equals(SavedState.class)) {
        // Didn't save the state, so call superclass
        super.onRestoreInstanceState(state);
        return;
    }

    // Cast state to custom BaseSavedState and pass to superclass
    SavedState myState = (SavedState) state;
    super.onRestoreInstanceState(myState.getSuperState());
    
    // Set this Preference's widget to reflect the restored state
    lastTime = myState.value;
    updatePicker();
  }

  /**
   * returns myPicker's value in this preference's storage format
   *      storage format = HH:mm (US Locale)
   * @return formatted String
   */
  private String formatForStorage() {
    if (myPicker != null) {
      return String.format(Locale.US,"%02d:%02d",myPicker.getCurrentHour(),myPicker.getCurrentMinute());
    } else {
      return "";
    }
  }

  /**
   * update picker with current time data
   */
  private void updatePicker() {
    if (myPicker != null) {
      Calendar cal = Calendar.getInstance();
      try {
	cal.setTime(STORAGE_FORMAT_PARSER.parse(lastTime));
      } catch (ParseException e) {
	e.printStackTrace();
      }

      myPicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
      myPicker.setCurrentMinute(cal.get(Calendar.MINUTE));
    }
  }


  private static class SavedState extends BaseSavedState {
    String value;

    public SavedState(Parcelable superState) {
        super(superState);
    }

    public SavedState(Parcel source) {
        super(source);
        value = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(value);
    }

    // Standard creator object using an instance of this class
    public static final Parcelable.Creator<SavedState> CREATOR =
            new Parcelable.Creator<SavedState>() {

        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };
  }
}
