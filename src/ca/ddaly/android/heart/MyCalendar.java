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

import android.util.Log;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MyCalendar extends GregorianCalendar{

  private static final String TAG = "MyCalendar";

    /**
     * Constructs a new {@code MyCalendar} initialized to the current date and
     * time with the default {@code Locale} and {@code TimeZone}.
     */
    public MyCalendar() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Constructs a new {@code MyCalendar} initialized to midnight in the default
     * {@code TimeZone} and {@code Locale} on the specified date.
     *
     * @param year
     *            the year.
     * @param month
     *            the month.
     * @param day
     *            the day of the month.
     */
    public MyCalendar(int year, int month, int day) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(year, month, day);
    }

    /**
     * Constructs a new {@code MyCalendar} initialized to the specified date and
     * time in the default {@code TimeZone} and {@code Locale}.
     *
     * @param year
     *            the year.
     * @param month
     *            the month.
     * @param day
     *            the day of the month.
     * @param hour
     *            the hour.
     * @param minute
     *            the minute.
     */
    public MyCalendar(int year, int month, int day, int hour, int minute) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(year, month, day, hour, minute);
    }

    /**
     * Constructs a new {@code MyCalendar} initialized to the specified date and
     * time in the default {@code TimeZone} and {@code Locale}.
     *
     * @param year
     *            the year.
     * @param month
     *            the month.
     * @param day
     *            the day of the month.
     * @param hour
     *            the hour.
     * @param minute
     *            the minute.
     * @param second
     *            the second.
     */
    public MyCalendar(int year, int month, int day, int hour,
            int minute, int second) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(year, month, day, hour, minute, second);
    }

    MyCalendar(long milliseconds) {
        this(false);
        setTimeInMillis(milliseconds);
    }

    /**
     * Constructs a new {@code MyCalendar} initialized to the current date and
     * time and using the specified {@code Locale} and the default {@code TimeZone}.
     *
     * @param locale
     *            the {@code Locale}.
     */
    public MyCalendar(Locale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a new {@code MyCalendar} initialized to the current date and
     * time and using the specified {@code TimeZone} and the default {@code Locale}.
     *
     * @param timezone
     *            the {@code TimeZone}.
     */
    public MyCalendar(TimeZone timezone) {
        this(timezone, Locale.getDefault());
    }

    /**
     * Constructs a new {@code MyCalendar} initialized to the current date and
     * time and using the specified {@code TimeZone} and {@code Locale}.
     *
     * @param timezone
     *            the {@code TimeZone}.
     * @param locale
     *            the {@code Locale}.
     */
    public MyCalendar(TimeZone timezone, Locale locale) {
        super(timezone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    MyCalendar(boolean ignored) {
        super(TimeZone.getDefault());
        setFirstDayOfWeek(SUNDAY);
        setMinimalDaysInFirstWeek(1);
    }


    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * default {@code Locale} and default {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance() {
        return new MyCalendar();
    }

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * given {@code Locale} and default {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance(Locale locale) {
        return new MyCalendar(locale);
    }

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * default {@code Locale} and given {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance(TimeZone timezone) {
        return new MyCalendar(timezone);
    }

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * given {@code Locale} and given {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance(TimeZone timezone, Locale locale) {
        return new MyCalendar(timezone, locale);
    }

  /**
   * Returns the time as local time represented by this {@code MyCalendar}, recomputing the time from its
   * fields if necessary.
   *
   * @throws IllegalArgumentException
   *                if the time is not set and the time cannot be computed
   *                from the current field values.
   */
  @Override
  public long getTimeInMillis() {
    long utc_time;
    utc_time = super.getTimeInMillis();

/*********************
  DOES NOT ACCOMPLISH WHAT i WANT

    if (BuildConfig.DEBUG) {
      Log.v(TAG,"utc_time =" + utc_time);
      Log.v(TAG,"returning " + (utc_time + get(ZONE_OFFSET) + get(DST_OFFSET)));
    }

    return utc_time + get(ZONE_OFFSET) + get(DST_OFFSET);
************************/

    return utc_time;
  }
}
