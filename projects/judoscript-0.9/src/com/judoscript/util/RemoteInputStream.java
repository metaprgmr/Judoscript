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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.rmi.Remote;


public class RemoteInputStream extends FilterInputStream implements Remote
{
  public RemoteInputStream(InputStream is) { super(is); }
}
