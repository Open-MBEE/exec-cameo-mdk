/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.lib;

import org.junit.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {
    /**
     * epoch is a timestamp corresponding to TimePoint = 0 as the date/time that
     * the simulation starts. It is an offset of the time since Jan 1, 1970. For
     * example, if epoch == 1341614935000 milliseconds, then a TimePoint or int
     * value of 0 corresponds to Fri, Jul 06, 2012 3:48:55 PM. This number comes
     * from using the 'date' unix command:
     * $ date; date '+%s'
     * Fri, Jul 06, 2012 3:48:55 PM
     * 1341614935
     * The units of time and the epoch are specified by
     * Units units below.
     */
    protected static Date epoch = new Date();

    // private final static Timepoint epochTimepoint = new Timepoint( "", 0,
    // null );

    public static final String timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String fileTimestampFormat = "yyyy-MM-dd'T'HH.mm.ss.SSSZ";

    public enum Units {
        days(24 * 3600 * 1e9), hours(3600 * 1e9), minutes(60 * 1e9), seconds(1e9), milliseconds(1e6),
        microseconds(1e3), nanoseconds(1);

        private double factor;

        Units(double f) {
            factor = f;
        }

        public static double conversionFactor(Units fromUnits, Units toUnits) {
            double f = fromUnits.factor / toUnits.factor;
            // if ( Debug.isOn() ) Debug.outln( "conversionFactor(" + fromUnits
            // + ", " + toUnits
            // + ") = " + fromUnits.factor + " / " + toUnits.factor
            // + " = " + f );
            return f;
        }

        public String toShortString() {
            switch (this) {
                case days:
                    return "d";
                case hours:
                    return "h";
                case minutes:
                    return "m";
                case seconds:
                    return "s";
                case milliseconds:
                    return "ms";
                case microseconds:
                    return "\u00B5s";
                case nanoseconds:
                    return "ns";
                default:
                    return null;
            }
        }

        public static Units fromString(String unitsString) {
            Units unit = null;
            try {
                if (unitsString == null || unitsString.length() == 0) {
                    Assert.fail("Parse of units from \"" + unitsString + "\" failed!");
                }
                if (unitsString.equals(microseconds.toShortString())) {
                    unit = microseconds;
                }
                else {
                    switch (unitsString.charAt(0)) {
                        case 'd':
                            unit = days;
                            break;
                        case 'h':
                            unit = hours;
                            break;
                        case 's':
                            unit = seconds;
                            break;
                        case 'n':
                            unit = nanoseconds;
                            break;
                        case 'm':
                            if (unitsString.length() == 1) {
                                unit = minutes;
                                break;
                            }
                            else {
                                switch (unitsString.charAt(1)) {
                                    case 'i':
                                        if (unitsString.length() <= 2) {
                                            Assert.fail("Parse of units from \"" + unitsString + "\" failed!");
                                        }
                                        else {
                                            switch (unitsString.charAt(2)) {
                                                case 'n':
                                                    unit = minutes;
                                                    break;
                                                case 'l':
                                                    unit = milliseconds;
                                                    break;
                                                case 'c':
                                                    unit = microseconds;
                                                    break;
                                                default:
                                                    Assert.fail("Parse of units from \"" + unitsString
                                                            + "\" failed!");
                                            }
                                        }
                                        break;
                                    case 's':
                                        unit = milliseconds;
                                        break;
                                    default:
                                        Assert.fail("Parse of units from \"" + unitsString + "\" failed!");
                                }
                            }
                            break;
                        default:
                            Assert.fail("Parse of units from \"" + unitsString + "\" failed!");
                    }
                }
                if (unit != null && !unitsString.equals(unit.toString())
                        && !unitsString.equals(unit.toShortString())) {
                    Assert.fail("Parse of units from \"" + unitsString + "\" failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return unit;
        }
    }

    public static int timeSinceMidnight(Date start, Units units) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(start);
        c2.setTime(start);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);
        long diffMillis = (int) (c1.getTimeInMillis() - c2.getTimeInMillis());
        double f = Units.conversionFactor(units, Units.milliseconds);
        return (int) (diffMillis / f);
    }

    public double convertTo(double duration, Units fromUnit, Units toUnit) {
        Double f = new Double(Units.conversionFactor(fromUnit, toUnit));
        f *= duration;
        return f;
    }

    /**
     * Get a Date corresponding to the input time.
     *
     * @param time  as a duration since the Unix epoch (Jan 1, 1970)
     * @param units of the time duration
     * @return a conversion to Date
     */
    public Date getDate(double time, Units units) {
        Date d = new Date((long) (Units.conversionFactor(units, Units.milliseconds) * time));
        return d;
    }

    /**
     * Returns true if the start (s) and end (e) dates are contained within the
     * start (sb) and end (eb) bounds. A null value of the start or end bounds
     * indicates the container is boundless. sb and eb are considered to be
     * contained.
     *
     * @param sb
     * @param eb
     * @param s
     * @param e
     * @return whether [sb,eb] contains [s,e];
     */
    public static boolean contains(Date sb, Date eb, Date s, Date e) {
        return contains(sb, eb, s, e, true, true);
    }

    /**
     * Returns true if the start (s) and end (e) dates are contained within the
     * start (sb) and end (eb) bounds. A null value of the start or end bounds
     * indicates the container is boundless.
     *
     * @param sb
     * @param eb
     * @param s
     * @param e
     * @param includeStart whether sb should be considered contained
     * @param includeEnd   whether eb should be considered contained
     * @return whether [sb,eb] contains [s,e].
     */
    public static boolean contains(Date sb, Date eb, Date s, Date e, boolean includeStart, boolean includeEnd) {
        // LogUtil.logger().setLevel( Level.ALL );
        if (sb == null && eb == null) {
            return true;
        }
        if (sb == null) {
            if (e == null) {
                return false;
            }
            int compareEnds = e.compareTo(eb);
            return (compareEnds < 0 || (includeEnd && compareEnds == 0));
        }
        if (eb == null) {
            if (s == null) {
                return false;
            }
            int compareStarts = s.compareTo(sb);
            return (compareStarts > 0 || (includeStart && compareStarts == 0));
        }
        if (s == null || e == null) {
            return false;
        }
        int compareEnds = e.compareTo(eb);
        int compareStarts = s.compareTo(sb);
        boolean c = // s.after(sb) && e.before(eb);
                (compareEnds < 0 || (includeEnd && compareEnds == 0))
                        && (compareStarts > 0 || (includeStart && compareStarts == 0));
        // LogUtil.debug( "contains(" + sb + ", " + eb + ", " + s + ", " + e +
        // ") = " + c );
        // if ( sb != null && eb != null && s != null && e != null ) {
        // LogUtil.debug( "contains(" + ( sb.getTime() / 1000 ) + "s, "
        // + ( eb.getTime() / 1000 ) + "s, " + ( s.getTime() / 1000 )
        // + "s, " + ( e.getTime() / 1000 ) + "s) = " + c );
        // }
        return c;
    }

    // public static long toSeconds(Amount<Duration> duration) {
    // if (duration == null) {
    // LogUtil.error("Cannot convert null duration!");
    // return 0;
    // }
    // return duration.longValue(SI.SECOND);
    // }

    public static String toTimeString(Date d, String format) {
        if (d != null) {
            return toTimeString(d.getTime(), format);
        }
        else {
            Debug.error("Cannot convert null Date");
            return null;
        }
    }

    public static String toTimeString(long millis, String format) {
        if (format == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millis);
        String timeString = new SimpleDateFormat(format).format(cal.getTime());
        return timeString;
    }

    public static Date dateFromTimestamp(String timestamp) {
        String formatsToTry[] = {TimeUtils.timestampFormat, TimeUtils.timestampFormat.replace(".SSS", ""),
                TimeUtils.timestampFormat.replace("Z", ""), TimeUtils.timestampFormat.replace(".SSSZ", ""),
                "EEE MMM dd HH:mm:ss zzz yyyy"};
        // ArrayList formatsToTry = new ArrayList();
        // format
        int pos = timestamp.lastIndexOf(':');
        if (pos == timestamp.length() - 3 && timestamp.replaceAll("[^:]", "").length() == 3) {
            timestamp = timestamp.replaceFirst(":([0-9][0-9])$", "$1");
        }
        // for ( String format : formatsToTry ) {
        for (int i = 0; i < formatsToTry.length; ++i) {
            String format = formatsToTry[i];
            DateFormat df = new SimpleDateFormat(format);
            try {
                Date d = df.parse(timestamp);
                return d;
            } catch (IllegalArgumentException e1) {
                if (i == formatsToTry.length - 1) {
                    e1.printStackTrace();
                }
            } catch (ParseException e) {
                if (i == formatsToTry.length - 1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static long fromTimestampToMillis(String timestamp) {
        long t = 0;
        DateFormat df = new SimpleDateFormat(timestampFormat);
        try {
            Date d = df.parse(timestamp);
            assert (d != null);
            // THIS IS WRONG!
            // t = (long)( Units.conversionFactor( Units.milliseconds,
            // Timepoint.units )
            // * d.getTime() );
            t = d.getTime();
        } catch (java.text.ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return t;
    }

    // public static Integer fromMillisToInteger( long millis ) {
    // int t = (int)( Units.conversionFactor( Units.milliseconds,
    // Timepoint.units )
    // * ( millis - epoch.getTime() ) );
    // return t;
    // }

    // public static Integer fromDateToInteger( Date date ) {
    // return fromMillisToInteger( date.getTime() );
    // }

    // public static Integer fromTimestampToInteger( String timestamp ) {
    // Integer t = null;
    // DateFormat df = new SimpleDateFormat( timestampFormat );
    // try {
    // Date d = df.parse( timestamp );
    // assert ( d != null );
    // t = fromDateToInteger(d);
    // } catch ( java.text.ParseException e1 ) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // }
    // return t;
    // }

    // Converts time offset to a date-time String in Timepoint.timestamp format.
    // Assumes t is an offset from Timepoint.epoch in Timepoint.units.
    public static String toTimestamp(long t, Units units) {
        Calendar cal = Calendar.getInstance();
        double cf = Units.conversionFactor(units, Units.milliseconds);
        cal.setTimeInMillis((long) (getEpoch().getTime() + t * cf));
        String timeString = new SimpleDateFormat(timestampFormat).format(cal.getTime());
        return timeString;
    }

    // Converts time offset to a date-time String in Timepoint.timestamp format.
    // Assumes t is an offset from Timepoint.epoch in Timepoint.units.
    public static String timestampForFile() {
        String timeString = new SimpleDateFormat(fileTimestampFormat).format(System.currentTimeMillis());
        return timeString;
    }

    public static final String aspenTeeFormat = "yyyy-MM-dd'T'HH:mm:ss";

    public static String toAspenTimeString(long millis) {
        return toAspenTimeString(millis, aspenTeeFormat);
    }

    public static String toAspenTimeString(Date d) {
        return toAspenTimeString(d, aspenTeeFormat);
    }

    public static String toAspenTimeString(Date d, String format) {
        if (d != null) {
            return toAspenTimeString(d.getTime(), format);
        }
        else {
            Debug.errln("Cannot convert null Date");
            return null;
        }
    }

    public static String toAspenTimeString(long millis, String format) {
        if (format == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millis);
        String timeString = new SimpleDateFormat(format).format(cal.getTime());
        return timeString;
    }

    // public String toTimestamp() {
    // return toTimestamp( getValue(false) );
    // // Double cf = Units.conversionFactor( Units.milliseconds );
    // // return millisToTimestamp( (long)( getValue() * cf ) );
    // //// Calendar cal = Calendar.getInstance();
    // //// cal.setTimeInMillis( (long)( ( getEpoch().getTime() + getValue() ) *
    // cf ) );
    // //// String timeString =
    // //// new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" ).format(
    // cal.getTime() );
    // //// return timeString;
    // }

    // @Override
    // public String toString() {
    // //return toTimestamp();
    // return super.toString();
    // }

    /**
     * @return the epoch
     */
    public synchronized static Date getEpoch() {
        return epoch;
    }

    // public synchronized static Timepoint getEpochTimepoint() {
    // return epochTimepoint;
    // }

    /**
     * @param epoch the epoch to set
     */
    public synchronized static void setEpoch(Date epoch) {
        TimeUtils.epoch = epoch;
        System.out.println("Epoch set to " + epoch);
    }

    public static void setEpoch(String epochString) {
        setEpoch(dateFromTimestamp(epochString));
    }

}
