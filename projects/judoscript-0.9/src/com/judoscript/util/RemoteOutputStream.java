/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-02-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;


public class RemoteOutputStream extends FilterOutputStream implements Remote
{
  public RemoteOutputStream(OutputStream os) { super(os); }
}
