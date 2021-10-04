/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-28-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.user;

public class JuspPageInfo
{
  public boolean isErrorPage = false;
  public boolean isBinary = false;
  public String contentType = null;
  public String pageEncoding = null;
  public int status = 200;
  public long isStatic_refresh = -1;

  public boolean isIncludeOnly = false; // used by jusp2html: if true, not generated.
  public String  any = null;            // used by jusp2html: the generated code.

  public String getGeneratedCode() { return any; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[JuspPageInfo] -----------------------------------------\n")
      .append("     isErrorPage: ").append(isErrorPage).append('\n')
      .append("        isBinary: ").append(isBinary).append('\n')
      .append("     contentType: ").append(contentType).append('\n')
      .append("    pageEncoding: ").append(pageEncoding).append('\n')
      .append("          status: ").append(status).append('\n')
      .append("isStatic_refresh: ").append(isStatic_refresh).append('\n')
      .append("   isIncludeOnly: ").append(isIncludeOnly).append('\n')
      .append("             any: ").append('\n').append(any).append('\n')
      .append("[/JuspPageInfo] ----------------------------------------");
    return sb.toString();
  }
}
