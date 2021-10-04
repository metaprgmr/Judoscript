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

import jp.ne.so_net.ga2.no_ji.jcom.*;
import com.judoscript.*;
import com.judoscript.bio.JavaObject;


public class ComIUnknown extends JavaObject
{
  public ComIUnknown(IUnknown unk) { super(unk); }

  public int getType() { return TYPE_COM; }
  public String getTypeName() { return "ComIUnknown"; }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    if (fxn.equals("queryInterface")) {
      if (params.length < 2) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                             "queryInterface() takes a Java class name and a guid");
      }
      Object id = params[1].getObjectValue();
      GUID guid = (id instanceof GUID) ? (GUID)id : GUID.parse(id.toString());
      return JComUtil.wrap(((IUnknown)object).queryInterface(params[0].getStringValue(), guid));
    } else if (fxn.equals("getIDispatch")) {
      return JComUtil.getIDispatch((IUnknown)object);
    } else if (fxn.equals("getIEnumVARIANT")) {
      return JComUtil.getIEnumVARIANTWrapped((IUnknown)object);
    }
    return super.invoke(fxn,params,javaTypes);
  }

} // end of class ComIUnknown.
