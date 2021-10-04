/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-08-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jusp;

import javax.servlet.ServletException;

public class JuspException extends ServletException
{
	public JuspException() { super(); }
	public JuspException(Throwable t) { super(t); }
	public JuspException(String msg) { super(msg); }
	public JuspException(String msg, Throwable t) { super(msg, t); }
}
