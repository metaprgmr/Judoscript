/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-14-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jusp;

import java.util.Iterator;
import javax.servlet.http.HttpSession;
import com.judoscript.*;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.EnumerationIterator;


public final class JuspSession extends JavaObject
{
	JuspSession(HttpSession s) { super(HttpSession.class, s); }
	public HttpSession getHttpSession() { return (HttpSession)object; }
	
	public Variable resolveVariable(String name) {
		return JudoUtil.toVariable(getHttpSession().getAttribute(name));
	}
	
	public Variable setVariable(String name, Variable val, int type) throws Throwable {
		if (val.isNil())
			getHttpSession().removeAttribute(name);
		else
			getHttpSession().setAttribute(name, val.getObjectValue());
		return val;
	}
	
	public int size() {
		try { return getHttpSession().getValueNames().length; }
		catch(Exception e) { return 0; }
	}
	
  public Iterator getIterator(int start, int to, int step, boolean upto, boolean backward) throws Throwable
	{
    return new EnumerationIterator(getHttpSession().getAttributeNames());
	}
	
} // end of class JuspSession.
