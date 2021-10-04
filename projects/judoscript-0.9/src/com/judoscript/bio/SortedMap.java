/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 10-7-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.util.*;
import com.judoscript.util.AssociateList;
import com.judoscript.util.Doublet;


public class SortedMap extends UserDefined
{
  public SortedMap() throws Throwable { this(null); }

  public SortedMap(Type type) throws Throwable {
    super(type);
    Comparator c = UserDefinedComparator.getComparator(type==null ? null : type.comparator);
    storage = c==null ? new TreeMap() : new TreeMap(c);
  }

  public final void init(Object inits) throws Throwable {
    AssociateList es = null;
    Comparator cptr = null;
    if (inits == null) {
      if (inits instanceof AssociateList) {
        es = (AssociateList)inits;
      } else if (inits instanceof Doublet) {
        Doublet dblt = (Doublet)inits;
        es = (AssociateList)dblt.o2;
        cptr = UserDefinedComparator.getComparator(dblt.o1);
        if (cptr != null)
          storage = new TreeMap(cptr);
      }
    }
    super.init(es);
  }

  public final String getTypeName() { return "SortedMap"; }

} // end of class SortedMap.
