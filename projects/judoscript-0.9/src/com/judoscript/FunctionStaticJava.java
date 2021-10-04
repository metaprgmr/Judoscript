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


package com.judoscript;

import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.XMLWriter;


public final class FunctionStaticJava extends FunctionBase
{
  Object clazz; // String or JavaObject (for Class)
  String methodName;
  boolean isConst;
  HashMap annotation;

  public FunctionStaticJava(int line, String name, String clsName, boolean isConst, String method,
             HashMap annotation)
  {
    setLineNumber(line);
    setName(name);
    clazz = clsName;
    this.isConst = isConst;
    this.annotation = annotation;
    methodName = method;
  }

  public FunctionStaticJava(int line, String name, Class clz, String method, HashMap annotation)
  {
		setLineNumber(line);
		setName(name);
		clazz = new JavaObject(clz);
		this.isConst = false;
		this.annotation = annotation;
		methodName = method;
	}

  public Map getAnnotation() { return annotation; }

  public Variable invoke(Expr[] args, int[] javaTypes) throws Throwable {
  	if (clazz instanceof JavaObject) {
  		return ((JavaObject)clazz).invoke(methodName, args, javaTypes);
  	} else {
	    String cls = (String)clazz;
	    if (isConst)
	      cls = RT.getScript().resolveConst(cls).getStringValue();
	    if (StringUtils.isBlank(cls))
	      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "The Java class is not found.");
	    return RT.getScript().getStaticJavaClass(cls).invoke(methodName, args, javaTypes);
  	}
  }

  public void dump(XMLWriter out) {
    out.openTag("FunctionForStaticJava");
    out.tagAttr("name", name);
    out.tagAttr("class", (clazz instanceof JavaObject) ? ((JavaObject)clazz).getClassName() : clazz.toString());
    out.tagAttr("method", methodName);
    out.tagAttr("isConst", "" + isConst);
    out.endTagLn();
  }

} // end of class FunctionStaticJava.
