/* JudoScript, The Scripting Solution for the Java Platform
 * Copyright (C) 2001-2002 James Huang, http://www.judoscript.com
 *
 * This is free software; you can embed, modify and redistribute
 * it under the terms of the GNU Lesser General Public License
 * version 2.1 or up as published by the Free Software Foundation,
 * which you should have received a copy along with this software.
 * In case you did not, please download it from the internet at
 * http://www.gnu.org/copyleft/lesser.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 12-14-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.ext.win32;

import java.util.ArrayList;
import java.util.List;
import jp.ne.so_net.ga2.no_ji.jcom.*;
import com.judoscript.*;

public class JComUtil implements Consts
{
  public static final ReleaseManager rm = new ReleaseManager();

  public static IDispatch getIDispatch(String progId) throws Exception {
    if (progId.startsWith("{")) progId = Com.getProgIDFromCLSID(GUID.parse(progId));
    return new IDispatch(rm, progId);
  }

  // Only for *.jcom.* classes. -- see JudoUtil.toVariable().
  public static Variable wrap(Object o) throws Exception {
    if (o instanceof IDispatch)
      return new ComIDispatch((IDispatch)o);
    else if (o instanceof IUnknown) 
      return new ComIUnknown((IUnknown)o);
    else if (o instanceof VariantCurrency)
      return new ConstDouble(((VariantCurrency)o).get(), JAVA_CURRENCY);
    else if (o instanceof IEnumVARIANT)
      return JudoUtil.toVariable(IEnumVARIANT2List((IEnumVARIANT)o));
    // Others are:
    //   GUID, ITypeInfo, ITypeLib and ReleaseManager.
    return JudoUtil.toVariable(o);
  }

  public static Variable getIDispatch(IUnknown unk) throws Exception {
    return wrap(unk.queryInterface("jp.ne.so_net.ga2.no_ji.jcom.IDispatch", GUID.IID_IDispatch));
  }

  public static List getIEnumVARIANT(IUnknown unk) throws Exception {
    IEnumVARIANT ev = (IEnumVARIANT)unk.queryInterface("jp.ne.so_net.ga2.no_ji.jcom.IEnumVARIANT",
                                                       GUID.IID_IEnumVARIANT);
    return IEnumVARIANT2List(ev);
  }

  public static Variable getIEnumVARIANTWrapped(IUnknown unk) throws Exception {
    return JudoUtil.toVariable(getIEnumVARIANT(unk));
  }

  public static List IEnumVARIANT2List(IEnumVARIANT ev) throws Exception {
    ArrayList list = new ArrayList();
    while (true) {
      Object x = ev.next();
      if (x == null) break;
      list.add(x);
    }
    return list;
  }

} // end of class JComUtil.
