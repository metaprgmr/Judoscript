/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-10-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.user;

import java.util.*;

public class JuspUtil
{
  public static String uriToFunctionName(String uri) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<uri.length(); ++i) {
      char ch = uri.charAt(i);
      if (Character.isJavaIdentifierPart(ch))
        sb.append(ch);
      else {
        switch(ch) {
        case '.': sb.append('$'); break;
        default:  sb.append('_'); break;
        }
      }
    }
    return sb.toString();
  }

  public static final int PI_URI          = 0;
  public static final int PI_FILEPATH     = 1;
  public static final int PI_FUNCTIONNAME = 2;
  public static final int PI_CONTEXTPATH  = 3;
  public static final int PI_JUSPWORKDIR  = 4;
  
  /**
   * @param uri
   * @param refUri
   * @param refFilePath
   * @param ctxVPath
   */
  public static String[] uriToFilePath(String uri, String refUri, String refFilePath, String ctxVPath, String juspWorkDir) {
    String[] ret = new String[5];
    if (uri.startsWith("/")) {
      if (ctxVPath.endsWith("/"))
        ctxVPath = ctxVPath.substring(0, ctxVPath.length()-1);
      ret[PI_URI] = uri;
      ret[PI_FILEPATH] = ctxVPath + uri;
    } else {
      // uri is relative
      int idx = refUri.lastIndexOf('/');
      if (idx > 0)
        ret[PI_URI] = refUri.substring(0, idx+1) + uri;
      else
        ret[PI_URI] = uri;
      
      try {
        ret[PI_FILEPATH] = uri;
        refFilePath = refFilePath.replace('\\', '/');
        idx = refFilePath.lastIndexOf('/');
        if (idx > 0)
          ret[PI_FILEPATH] = refFilePath.substring(0, idx+1) + uri;
      } catch(Exception e) {}
    }
    ret[PI_FUNCTIONNAME] = uriToFunctionName(ret[PI_URI]);
    ret[PI_CONTEXTPATH] = ctxVPath;
    ret[PI_JUSPWORKDIR] = juspWorkDir;
    return ret;
  }


  private static final String monthNames[] = {
    "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"
  };

  private static String formDateCtrName(String prefix, String fld, String postfix) {
    return (postfix==null) ? prefix + fld : prefix + fld + postfix;
  }

  public static Date readDateControl(Map map, String prefix) {
    return readDateControl(map, prefix, null);
  }

  public static Date readDateControl(Map map, String prefix, String postfix) {
    try {
      Calendar cal = Calendar.getInstance();
      String ctr = (String)map.get(formDateCtrName(prefix, "_y_", postfix));
      cal.set(Calendar.YEAR, Integer.parseInt(ctr));
      ctr = (String)map.get(formDateCtrName(prefix, "_m_", postfix));
      cal.set(Calendar.MONTH, Integer.parseInt(ctr));
      ctr = (String)map.get(formDateCtrName(prefix, "_d_", postfix));
      cal.set(Calendar.DATE, Integer.parseInt(ctr));
      return cal.getTime();
    } catch(Exception e) {
      return null;
    }
  }

  public static String dateControl(String prefix, int yearsBefore, int yearsAfter) {
    return dateControl(prefix, null, null, yearsBefore, yearsAfter, null);
  }

  public static String dateControl(String prefix, String postfix, int yearsBefore, int yearsAfter) {
    return dateControl(prefix, postfix, null, yearsBefore, yearsAfter, null);
  }

  public static String dateControl(String prefix, Date date, int yearsBefore, int yearsAfter) {
    return dateControl(prefix, null, date, yearsBefore, yearsAfter, null);
  }

  /**
   *
   * @param prefix
   * @param postfix
   * @param date
   * @param yearsBefore
   * @param yearsAfter
   * @param extra  -- extra for the <select> control (e.g. style, javascript, ...)
   * @return 3 SELECT controls with names as prefix+'_'+y/m/d+'_'+postfix
   */
  public static String dateControl(String prefix, String postfix, Date date,
                                   int yearsBefore, int yearsAfter, String extra)
  {
    if (yearsBefore <= 0)
      yearsBefore = 5;
    if (yearsAfter <= 0)
      yearsAfter = 20;
    int numOfYears = yearsAfter + yearsBefore + 1;

    int i;
    int y = -1;
    int m = -1;
    int d = -1;
    Calendar cal = Calendar.getInstance();
    int startYear = cal.get(Calendar.YEAR) - yearsBefore;
    if (date != null) {
      cal.setTime(date);
      y = cal.get(Calendar.YEAR);
      m = cal.get(Calendar.MONTH);
      d = cal.get(Calendar.DATE);
    }

    StringBuffer sb = new StringBuffer();

    sb.append("<select name='");
    sb.append(formDateCtrName(prefix, "_d_", postfix));
    if (extra != null) sb.append(" " + extra);
    sb.append("'>");
    for (i=1; i<=31; ++i) {
      sb.append("<option value='"+i+"'");
      if (d==i) sb.append(" selected");
      sb.append(">");
      sb.append(i);
      sb.append("</option>");
    }
    sb.append("</select>");

    sb.append("<select name='");
    sb.append(formDateCtrName(prefix, "_m_", postfix));
    if (extra != null) sb.append(" " + extra);
    sb.append("'>");
    for (i=0; i<monthNames.length; ++i) {
      sb.append("<option value='"+i+"'");
      if (m==i) sb.append(" selected");
      sb.append(">");
      sb.append(monthNames[i]);
      sb.append("</option>");
    }
    sb.append("</select>");

    sb.append("<select name='");
    sb.append(formDateCtrName(prefix, "_y_", postfix));
    if (extra != null) sb.append(" " + extra);
    sb.append("'>");
    for (i=0; i<numOfYears; ++i) {
      int year = startYear + i;
      sb.append("<option value='"+year+'\'');
      if (y==year) sb.append(" selected");
      sb.append(">");
      sb.append(year);
      sb.append("</option>");
    }
    sb.append("</select>");

    return sb.toString();
  }

} // end of class JuspUtil.
