/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 07-20-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.rmi.Remote;


public class RemoteReader extends java.io.BufferedReader implements Remote
{
  public RemoteReader(java.io.Reader r) { super(r); }
}
