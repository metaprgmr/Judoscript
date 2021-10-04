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


package com.judoscript.ext;

import com.ice.jni.registry.*;

public class WindowsRegistry extends FactoryUtil
{
  public static Object getWindowsRegistry() throws Exception {
    checkClass("com.ice.jni.registry.Registry",hint);
    // TODO: automatically create JavaPackages alias for com.ice.jni.registry package.
    return private_getWindowsRegistry();
  }

  private static Object private_getWindowsRegistry() throws Exception {
    return Registry.class;
  }

  static final String hint =
  "\nJudoScript's Windows Registry feature is optional and requires third-party software --\n\n" +
  "     Software: The JNI Registry Library\n" +
  "        Class: com.ice.jni.registry.Registry\n" +
  "        Where: http://www.trustice.com/java/jnireg/index.shtml \n" +
  "   Search For: 'windows registry java ice jgt' \n" +
  "     Download: jniregistry.zip or current.";
}
