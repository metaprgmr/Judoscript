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

//
// Note! Compile with jdk1.4 only!!
//

package com.judoscript.jdk14;

import java.util.ArrayList;
import java.util.regex.*;
import java.util.Hashtable;

import com.judoscript.*;
import com.judoscript.bio.*;
//import com.judoscript.util.Lib;
import org.apache.commons.lang.StringUtils;


public final class RegexCompiler implements RegexEngine
{
  public RegexCompiler() {}

  // caches compiled patterns.
  Hashtable patterns = new Hashtable();

  // Only args[0] is used. The rest could be for other uses, e.g. in replaceFirst/All().
  public Variable compile(Expr[] args) throws Throwable {
    int len = (args==null) ? 0 : args.length;
    if (len <= 0) return ValueSpecial.NIL;

    String pat = null;
    String flags = null;
    Variable v = args[0].eval();
    if (v instanceof _Array) {
      _Array arr = (_Array)v;
      if (arr.size() >= 2)
        flags = arr.resolve(1).getStringValue();
      if (arr.size() >= 1)
        pat = arr.resolve(0).getStringValue();
    } else if (v instanceof JavaObject) {
      if (v instanceof JavaArray) {
        JavaArray arr = (JavaArray)v;
        if (arr.size() >= 2)
          flags = arr.resolve(1).getStringValue();
        if (arr.size() >= 1)
          pat = arr.resolve(0).getStringValue();
      } else {
        Object o = v.getObjectValue();
        if (o instanceof Pattern)
          return v;
        pat = o.toString();
      }
    } else {
      pat = v.getStringValue();
    }
    return compile(pat, flags);
  }

  public Variable compile(String pat, String flags) throws Exception {
    if (StringUtils.isBlank(pat))
      return ValueSpecial.NIL;

    int f = 0;
    if (StringUtils.isNotBlank(flags)) {
      if (flags.indexOf('c')>=0) f |= Pattern.CANON_EQ;         // canonical equivalence
      if (flags.indexOf('i')>=0) f |= Pattern.CASE_INSENSITIVE; // case-insensitive matching
      if (flags.indexOf('x')>=0) f |= Pattern.COMMENTS;         // Permits whitespace and comments
      if (flags.indexOf('d')>=0) f |= Pattern.DOTALL;           // dotall mode
      if (flags.indexOf('m')>=0) f |= Pattern.MULTILINE;        // multiline mode
      if (flags.indexOf('u')>=0) f |= Pattern.UNICODE_CASE;     // Unicode-aware case folding
      if (flags.indexOf('l')>=0) f |= Pattern.UNIX_LINES;       // Unix lines mode
    }
    return compile(pat, f);
  }

  public Variable compile(String pat, int flags) throws Exception {
    String key = pat;
    if (flags != 0)
      key += "$$" + flags;
    Variable x = (Variable)patterns.get(key);
    if (x == null) {
      x = JudoUtil.toVariable(flags==0 ? Pattern.compile(pat) : Pattern.compile(pat, flags));
      patterns.put(key, x);
    }
    return x;
  }

  Pattern getPattern(Expr[] params) throws Throwable {
    return (Pattern)compile(params).getObjectValue();
  }

  public Variable matcher(String s, Expr[] params) throws Throwable {
    return JudoUtil.toVariable(getPattern(params).matcher(s));
  }

  public Variable matches(String s, Expr[] params) throws Throwable {
    return ConstInt.getBool(getPattern(params).matcher(s).matches());
  }

  public Variable matchesStart(String s, Expr[] params) throws Throwable {
    return ConstInt.getBool(getPattern(params).matcher(s).lookingAt());
  }

  public Variable replaceAll(String s, Expr[] params) throws Throwable {
    if ((s != null) && (params.length >= 1)) {
      Matcher m = getPattern(params).matcher(s);
      String replace = (params.length < 2) ? "" : params[1].getStringValue();
      s = m.replaceAll(replace);
    }
    return JudoUtil.toVariable(s);
  }

  public Variable replaceFirst(String s, Expr[] params) throws Throwable {
    if ((s != null) && (params.length >= 1)) {
      Matcher m = getPattern(params).matcher(s);
      String replace = (params.length < 2) ? "" : params[1].getStringValue();
      s = m.replaceFirst(replace);
    }
    return JudoUtil.toVariable(s);
  }

  public Variable split(String s, Expr[] params) throws Throwable {
    _Array ar = new _Array();
    Pattern pattern = getPattern(params);
    if (pattern == null) {
      ar.append(JudoUtil.toVariable(s));
    } else {
      int limit = (params.length<2) ? 0 : (int)params[1].getLongValue();
      String[] sa = (limit==0) ? pattern.split(s) : pattern.split(s, limit);
      limit = (sa==null) ? 0 : sa.length;
      for (int i=0; i<limit; ar.append(JudoUtil.toVariable(sa[i++])));
    }
    return ar;
  }

  public Variable splitWithMatches(String s, Expr[] params, boolean needNonMatches) throws Throwable {
    Matcher matcher = getPattern(params).matcher(s);
    ArrayList ret = new ArrayList();
    int end = 0;
    while (matcher.find(end)) {
      int start = matcher.start();
      if (needNonMatches && (start > end))
        ret.add(s.substring(end, start));
      end = matcher.end();
      ret.add(s.substring(start, matcher.end()));
    }
    if (needNonMatches && (end < s.length()))
      ret.add(s.substring(end));

    return JudoUtil.toVariable(ret);
  }

} // end of class RegexCompiler.
