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


/** Started with org.apache.tomcat.util.MessageBytes */

package com.judoscript.util;

import java.io.OutputStream;
import java.io.IOException;

/**
 * This class is used to represent a subarray of bytes in an HTTP message.
 *
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 */

public class MsgBytes extends Ascii
{
    protected byte[] bytes;
    protected int offset;
    protected int length;

    public MsgBytes() {}
    public MsgBytes(byte[] b, int off, int len) { setBytes(b, off, len); }

    public void reset() { bytes = null; }

    public void setBytes(byte[] b, int off, int len) {
        bytes = b;
        offset = off;
        length = len;
    }

    /**
     * Puts the message bytes in buf starting at buf_offset.
     * @return the number of bytes added to buf.
     */
    public int getBytes(byte buf[], int buf_offset) 
    {
        if (bytes != null) 
            System.arraycopy(bytes, offset, buf, buf_offset, length);
        return length;
    }

    public byte[]  getBytes()  { return bytes; }
    public int     getOffset() { return offset; }
    public int     getLength() { return length; }
    public boolean isSet()     { return bytes != null; }
    public String  toString()  { return bytes != null ? new String(bytes,offset,length) : null; }
    public int     length()    { return bytes != null ? length : 0; }

    /**
     * Returns the message bytes parsed as an unsigned integer.
     * @exception NumberFormatException if the integer format was invalid
     */
    public int toInteger() throws NumberFormatException {
        return parseInt(bytes,offset,length);
    }

    /**
     * Returns the message bytes parsed as a date.
     * @param d the HttpDate object to use for parsing
     * @exception IllegalArgumentException if the date format was invalid
     */
    public long toDate(HttpDate d) throws IllegalArgumentException {
        if (bytes == null)
            throw new IllegalArgumentException("invalid date format");
        d.parse(bytes, offset, length);
        return d.getTime();
    }

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equals(String s) {
        byte[] b = bytes;
        int len = length;
        if (b == null || len != s.length())
            return false;
        int off = offset;
        for (int i = 0; i < len; i++)
            if (b[off++] != s.charAt(i))
                return false;
        return true;
    }

    /**
     * Compares the message bytes to the specified String object. Case is
     * ignored in the comparison.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equalsIgnoreCase(String s) {
        byte[] b = bytes;
        int len = length;
        if (b == null || len != s.length())
            return false;
        int off = offset;
        for (int i = 0; i < len; i++)
            if (toLower(b[off++]) != toLower((byte)s.charAt(i)))
                return false;
        return true;
    }

    /**
     * Compares the message bytes to the specified subarray of bytes.
     * @param b the bytes to compare
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equals(byte[] b, int off, int len) {
        byte[] b1 = bytes;
        if (b1 == null || len != length)
            return false;
        int off1 = offset;
        while (len-- > 0)
            if (b[off++] != b1[off1++])
                return false;
        return true;
    }

    /**
     * Compares the message bytes to the specified subarray of bytes.
     * Case is ignored in the comparison.
     * @param b the bytes to compare
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equalsIgnoreCase(byte[] b, int off, int len) {
        byte[] b1 = bytes;
        if (b1 == null || len != length)
            return false;
        int off1 = offset;
        while (len-- > 0)
            if (toLower(b[off++]) != toLower(b1[off1++]))
                return false;
        return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     */
    public boolean startsWith(String s) {
        byte[] b = bytes;
        int len = s.length();
        if (b == null || len > length)
            return false;
        int off = offset;
        for (int i = 0; i < len; i++)
            if (b[off++] != s.charAt(i))
                return false;
        return true;
    }

    /**
     * Writes the message bytes to the specified output stream.
     * @param out the output stream
     * @exception IOException if an I/O error has occurred
     */
    public void write(OutputStream out) throws IOException {
        if (bytes != null)
            out.write(bytes, offset, length);
    }


    ////////////////// my addition ////////////////////


    /**
     * Sets the new offset.
     */
    public void setOffset(int ofst) {
        if ((bytes != null) && (ofst >= 0) && (ofst <= offset+length)) {
            length += offset - ofst;
            offset = ofst;
        }
    }

    public void skip(int distance) {
        if ((bytes != null) && (distance >= offset) && (distance <= length)) {
            length -= distance;
            offset += distance;
        }
    }

    public boolean startsWithIgnoreCase(String s) {
        try {
            int i = offset;
            int len = s.length();
            for (int j = 0; j < len; j++) {
                if (i >= length) return false;
                if (toLower((int)bytes[i++]) != toLower((int)s.charAt(j)))
                    return false;
            }
            return true;
        } catch (NullPointerException npe) {}
        return false;
    }

    public boolean startsWith(int index, String s) {
        if (index < offset) return startsWith(s);
        return new MsgBytes(bytes,index,length-(index-offset)).startsWith(s);
    }

    public boolean startsWithIgnoreCase(int index, String s) {
        if (index < offset) return startsWithIgnoreCase(s);
        return new MsgBytes(bytes,index,length-(index-offset)).startsWithIgnoreCase(s);
    }

    public int indexOf(int index, String s) {
        if (index < offset) index = offset;
        for (int i=index; i<offset+length; i++)
            if (startsWith(i,s)) return i;
        return -1;
    }

    public int indexOf(String s) { return indexOf(-1,s); }

    public int indexOfIgnoreCase(int index, String s) {
        if (index < offset) index = offset;
        for (int i=index; i<offset+length; i++)
            if (startsWithIgnoreCase(i,s)) return i;
        return -1;
    }

    public int indexOfIgnoreCase(String s) { return indexOfIgnoreCase(-1,s); }

    public int indexOf(int index, int c) {
        if (index < offset) index = offset;
        try {
            for (int i=index; i<offset+length; i++)
                if ((int)bytes[i] == c) return i;
        } catch (NullPointerException npe) {}
        return -1;
    }

    public int indexOf(int c) { return indexOf(-1,c); }

    public String substring(int idx, int len) {
        try {
            if (idx < offset)
                idx = offset;
            if ((idx + len) >= (offset + length))
                len = length - idx;
            if (len <= 0) return null;
            return new String(bytes, idx, len);
        } catch(Exception e) {}
        return null;
    }

    public byte[] getEffectiveBytes () {
        if (bytes == null) return null;
        byte[] ret = new byte[length];
        System.arraycopy(bytes,offset,ret,0,length);
        return ret;
    }

    public MsgBytes getEffectiveMsgBytes () {
        byte[] effbytes = getEffectiveBytes();
        if (effbytes == null) return null;
        return new MsgBytes(effbytes,0,effbytes.length);
    }

} // end of class MsgBytes.
