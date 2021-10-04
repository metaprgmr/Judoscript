/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 12-14-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.ext;

import jp.ne.so_net.ga2.no_ji.jcom.*;
import com.judoscript.ext.win32.*;


public class COMFactory extends FactoryUtil
{
  public static Object getCOM(String progId) throws Exception {
    checkClass(mainClass,hint);
    return private_getCOM(progId);
  }

  public static Object loadTypeLib(String fileName) throws Exception {
    checkClass(mainClass,hint);
    return private_loadTypeLib(fileName);
  }

  /**
   * @return a GUID
   */
  public static Object progID2CLSID(String progid) throws Exception {
    return Com.getCLSIDFromProgID(progid);
  }

  public static String CLSID2ProgID(String clsid) throws Exception {
    return Com.getProgIDFromCLSID(GUID.parse(clsid));
  }

  private static Object private_getCOM(String progId) throws Exception {
    return JComUtil.getIDispatch(progId);
  }

  private static Object private_loadTypeLib(String fileName) throws Exception {
    return ITypeLib.loadTypeLib(JComUtil.rm,fileName);
  }

  static final String mainClass = "jp.ne.so_net.ga2.no_ji.jcom.IDispatch";
  static final String hint =
  "\nJudoScript's COM/ActiveX feature is optional and only available\n" +
  "on Wintel platforms. On Windows, make sure jcom.dll is in the\n" +
  "path to use any COM features.\n\n" +
  "     Software: The JCOM Package \n" +
  "        Class: " + mainClass + '\n' +
  "        Where: http://www.hcn.zaq.ne.jp/no-ji/jcom/ \n" +
  "   Search For: 'jcom java com bridge' \n" +
  "     Download: JCom223.zip (already included)\n";

} // end of class COMFactory.
