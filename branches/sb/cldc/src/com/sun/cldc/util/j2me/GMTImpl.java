/*
 * Copyright 2011 Oracle. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

package com.sun.cldc.util.j2me;

import java.util.TimeZone;
import java.util.Calendar;
import com.sun.cldc.util.TimeZoneImplementation;

/**
 * This class provides the minimal time zone implementations
 * for J2ME CLDC/MIDP.  The only supported
 * time zone is UTC/GMT.
 *
 * @see java.util.TimeZone
 */
public class GMTImpl extends TimeZoneImplementation {

    public GMTImpl() {}

    final static byte staticMonthLength[] = {31,29,31,30,31,30,31,31,30,31,30,31};

    static final int millisPerHour = 60*60*1000;
    static final int millisPerDay  = 24*millisPerHour;

    static void checkParams(int era, int year, int month, int day,
            int dayOfWeek, int millis) {
        if (true) {
            // Use this parameter checking code for normal operation.  Only one
            // of these two blocks should actually get compiled into the class
            // file.
            if ((era != 0 && era != 1)
                    || month < Calendar.JANUARY
                    || month > Calendar.DECEMBER
                    || day < 1
                    || day > staticMonthLength[month]
                    || dayOfWeek < Calendar.SUNDAY
                    || dayOfWeek > Calendar.SATURDAY
                    || millis < 0
                    || millis >= millisPerDay) {
                throw new IllegalArgumentException("Illegal date");
            }
        } else {
            // This parameter checking code is better for debugging, but
            // overkill for normal operation.  Only one of these two blocks
            // should actually get compiled into the class file.
            if (era != 0 && era != 1) {
                throw new IllegalArgumentException("Illegal era " + era);
            }
            if (month < Calendar.JANUARY
                    || month > Calendar.DECEMBER) {
                throw new IllegalArgumentException("Illegal month " + month);
            }
            int monthLength = staticMonthLength[month];
            if (day < 1
                    || day > monthLength) {
                throw new IllegalArgumentException("Illegal day " + day);
            }
            if (dayOfWeek < Calendar.SUNDAY
                    || dayOfWeek > Calendar.SATURDAY) {
                throw new IllegalArgumentException("Illegal day of week " + dayOfWeek);
            }
            if (millis < 0
                    || millis >= millisPerDay) {
                throw new IllegalArgumentException("Illegal millis " + millis);
            }
            if (monthLength < 28
                    || monthLength > 31) {
                throw new IllegalArgumentException("Illegal month length " + monthLength);
            }
        }
    }

    /**
     * Gets the time zone offset, for the specified date, modified in case of daylight
     * savings. This is the offset to add *to* GMT to get local time.
     * This method may return incorrect results for rules that start at the end
     * of February (e.g., last Sunday in February) or the beginning of March (e.g., March 1).
     *
     * @param era           The era of the given date (0 = BC, 1 = AD).
     * @param year          The year in the given date.
     * @param month         The month in the given date. Month is 0-based. e.g.,
     *                      0 for January.
     * @param day           The day-in-month of the given date.
     * @param dayOfWeek     The day-of-week of the given date.
     * @param millis        The milliseconds in day in <em>standard</em> local time.
     * @return              The offset to add *to* GMT to get local time.
     * @exception IllegalArgumentException the era, month, day,
     * dayOfWeek, or millis parameters are out of range
     */
    public int getOffset(int era, int year, int month, int day,
                         int dayOfWeek, int millis) {
        checkParams(era, year, month, day,dayOfWeek, millis);
        return 0;
    }

    /**
     * Gets the GMT offset for this time zone.
     */
    public int getRawOffset() {
        return 0;
    }

    /**
     * Queries if this time zone uses Daylight Savings Time.
     */
    public boolean useDaylightTime() {
        return false;
    }

    /**
     * Gets the ID of this time zone.
     * @return the ID of this time zone.
     */
    public String getID() {
       return GMT_ID;
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     * @param ID the ID for a <code>TimeZone</code>, either an abbreviation such as
     * "GMT", or a full name such as "America/Los_Angeles".
     * <p> The only time zone ID that is required to be supported is "GMT",
     * though typically, the timezones for the regions where the device is
     * sold should be supported.
     * @return the specified <code>TimeZone</code>, or null if the given ID
     * cannot be understood.
     */
    public TimeZone getInstance(String ID) {
        if (ID == null || ID.equals(GMT_ID)) {
            return new GMTImpl();
        }
        return null;
    }

    static final String GMT_ID        = "GMT";

    private static final String[] IDS = {"GMT"};
    /** Gets all the available IDs supported.
     * @return  an array of IDs.
     */
    public String[] getIDs() {
        return IDS;
    }

    // =======================privates===============================

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The <code>equals</code> method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     <code>x</code>, <code>x.equals(x)</code> should return
     *     <code>true</code>.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     <code>x</code> and <code>y</code>, <code>x.equals(y)</code>
     *     should return <code>true</code> if and only if
     *     <code>y.equals(x)</code> returns <code>true</code>.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     <code>x</code>, <code>y</code>, and <code>z</code>, if
     *     <code>x.equals(y)</code> returns <code>true</code> and
     *     <code>y.equals(z)</code> returns <code>true</code>, then
     *     <code>x.equals(z)</code> should return <code>true</code>.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     <code>x</code> and <code>y</code>, multiple invocations of
     *     <tt>x.equals(y)</tt> consistently return <code>true</code>
     *     or consistently return <code>false</code>, provided no
     *     information used in <code>equals</code> comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value <code>x</code>,
     *     <code>x.equals(null)</code> should return <code>false</code>.
     * </ul>
     * <p>
     * The <tt>equals</tt> method for class <code>Object</code> implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values <code>x</code> and
     * <code>y</code>, this method returns <code>true</code> if and only
     * if <code>x</code> and <code>y</code> refer to the same object
     * (<code>x == y</code> has the value <code>true</code>).
     * <p>
     * Note that it is generally necessary to override the <tt>hashCode</tt>
     * method whenever this method is overridden, so as to maintain the
     * general contract for the <tt>hashCode</tt> method, which states
     * that equal objects must have equal hash codes.
     *
     * @param object   the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    public boolean equals(Object object) {
        return object instanceof GMTImpl;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * <p>
     * The general contract of <code>hashCode</code> is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the <tt>hashCode</tt> method
     *     must consistently return the same integer, provided no information
     *     used in <tt>equals</tt> comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the <tt>equals(Object)</tt>
     *     method, then calling the <code>hashCode</code> method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link java.lang.Object#equals(java.lang.Object)}
     *     method, then calling the <tt>hashCode</tt> method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hashtables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class <tt>Object</tt> does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java<font size="-2"><sup>TM</sup></font> programming language.)
     *
     * @return a hash code value for this object.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.util.Hashtable
     */
    public int hashCode() {
        return this.getID().hashCode();
    }

}
