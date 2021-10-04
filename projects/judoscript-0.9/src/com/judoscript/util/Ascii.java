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


/** Started with org.apache.tomcat.util.Ascii */

package com.judoscript.util;

public class Ascii {
    /*
     * Character translation tables.
     */

    private static final byte[] toUpper = new byte[256];
    private static final byte[] toLower = new byte[256];

    /*
     * Character type tables.
     */

    private static final boolean[] isAlpha = new boolean[256];
    private static final boolean[] isUpper = new boolean[256];
    private static final boolean[] isLower = new boolean[256];
    private static final boolean[] isWhite = new boolean[256];
    private static final boolean[] isDigit = new boolean[256];

    /*
     * Initialize character translation and type tables.
     */

    static {
	for (int i = 0; i < 256; i++) {
	    toUpper[i] = (byte)i;
	    toLower[i] = (byte)i;
	}

	for (int lc = 'a'; lc <= 'z'; lc++) {
	    int uc = lc + 'A' - 'a';
	    toUpper[lc] = (byte)uc;
	    toLower[uc] = (byte)lc;
	    isAlpha[lc] = true;
	    isAlpha[uc] = true;
	    isLower[lc] = true;
	    isUpper[uc] = true;
	}

	isWhite[ ' '] = true;
	isWhite['\t'] = true;
	isWhite['\r'] = true;
	isWhite['\n'] = true;
	isWhite['\f'] = true;
	isWhite['\b'] = true;

	for (int d = '0'; d <= '9'; d++)
	    isDigit[d] = true;
    }

    public static int toUpper(int c) { return toUpper[c & 0xff] & 0xff; }
    public static int toLower(int c) { return toLower[c & 0xff] & 0xff; }
    public static boolean isAlpha(int c) { return isAlpha[c & 0xff]; }
    public static boolean isUpper(int c) { return isUpper[c & 0xff]; }
    public static boolean isLower(int c) { return isLower[c & 0xff]; }
    public static boolean isWhite(int c) { return isWhite[c & 0xff]; }
    public static boolean isDigit(int c) { return isDigit[c & 0xff]; }

    /**
     * Parses an unsigned integer from the specified subarray of bytes.
     * @exception NumberFormatException if the integer format was invalid
     */
    public static int parseInt(byte[] b, int off, int len) throws NumberFormatException {
        int c;
	if (b == null || len <= 0 || !isDigit(c = b[off++]))
	    throw new NumberFormatException("invalid number format");
	int n = c - '0';
	while (--len > 0) {
	    if (!isDigit(c = b[off++]))
	        throw new NumberFormatException("invalid number format");
	    n = n * 10 + c - '0';
	}
	return n;
    }
}
