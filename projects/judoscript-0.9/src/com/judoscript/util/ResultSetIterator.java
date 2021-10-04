/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 04-18-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.util.Iterator;
import java.sql.ResultSet;

public class ResultSetIterator implements Iterator
{
  ResultSet rs;

  public ResultSetIterator(ResultSet rs) { this.rs = rs; }
  public boolean hasNext() { try { return rs.next(); } catch(Exception e) { return false; } }
  public Object next() { return rs; }
  public void remove() {}
}
