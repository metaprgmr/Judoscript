/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


/** started with org.apache.tomcat.util.HttpDate */

package com.judoscript.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.text.*;

public class HttpDate extends Ascii {

    // ONLY FOR COMPAT -- KILL ASAP -- just make sure that dependant
    // classes know what's up. ref. MimeHeaderField
    private static final String DATESTR = "Sun, 06 Nov 1994 08:49:37 GMT";
    public static final int DATELEN = DATESTR.length();
    // END COMPAT -- DON'T FORGET TO KILL
    
    // we force our locale here as all http dates are in english
    private final static Locale loc = Locale.US;

    // all http dates are expressed as time at GMT
    private final static TimeZone zone = TimeZone.getTimeZone("GMT");

    // format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
    private final static String rfc1123Pattern = "EEE, dd MMM yyyyy HH:mm:ss z";

    // format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT"
    private final static String rfc1036Pattern = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    // format for C asctime() date string -- "Sun Nov  6 08:49:37 1994"
    private final static String asctimePattern = "EEE MMM d HH:mm:ss yyyyy";

    private final static SimpleDateFormat rfc1123Format =
	new SimpleDateFormat(rfc1123Pattern, loc);
    
    private final static SimpleDateFormat rfc1036Format =
	new SimpleDateFormat(rfc1036Pattern, loc);
    
    private final static SimpleDateFormat asctimeFormat =
	new SimpleDateFormat(asctimePattern, loc);
    
    static {
	rfc1123Format.setTimeZone(zone);
	rfc1036Format.setTimeZone(zone);
	asctimeFormat.setTimeZone(zone);
    }

    // protected so that oldcookieexpiry in cookieutils can use
    // yes, this is sloppy as crap and could stand to be done better.
    protected Calendar calendar = new GregorianCalendar(zone, loc);

    public HttpDate() {
        calendar.setTime(new Date(System.currentTimeMillis()));
    }
    
    public HttpDate(long ms) {
        calendar.setTime(new Date(ms));
    }

    public void setTime() {
        calendar.setTime(new Date(System.currentTimeMillis()));
    }

    public void setTime(long ms) {
        calendar.setTime(new Date(ms));
    }
    
    public void parse(String dateString) {
      calendar.setTime(parseHttpDate(dateString));
    }

    public static Date parseHttpDate(String dateString) {
        try { return rfc1123Format.parse(dateString); } catch (ParseException e) { }
        try { return rfc1036Format.parse(dateString); } catch (ParseException e) { } 
        try { return asctimeFormat.parse(dateString); }
        catch (ParseException pe) {
            throw new IllegalArgumentException("invalid date format");
        }
    }

    public void parse(byte[] b, int off, int len) {
        // ok -- so this is pretty stoopid, but the old version of this
        // source took this arg set, so we will too for now (backwards compat)
        String dateString = new String(b, off, len);
        parse(dateString);
    }

    public void write(OutputStream out) throws IOException {
        String dateString = rfc1123Format.format(calendar.getTime());
        byte[] b = dateString.getBytes();
        out.write(b);
    }

    public String toString() {
        return rfc1123Format.format(calendar.getTime());
    }

    public static TimeZone getTimeZone() { return zone; }

    public long getTime() {
        return calendar.getTime().getTime();
    }

    // KILL, THIS IS ONLY HERE FOR TEMP COMPAT as MimeHeaderField uses it.
    public int getBytes(byte[] buf, int off, int len) {
	if (len < DATELEN) {
	    throw new IllegalArgumentException("array too small");
	}

	String dateString = rfc1123Format.format(calendar.getTime());
	byte[] b = dateString.getBytes();
	System.arraycopy(b, 0, buf, off, DATELEN);
	return DATELEN;
    }    
}
