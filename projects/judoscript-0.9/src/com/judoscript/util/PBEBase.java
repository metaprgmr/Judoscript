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


package com.judoscript.util;

import java.io.*;

public abstract class PBEBase
{
  String password;

  public PBEBase(String password) { this.password = password; }

  public abstract byte[] encrypt(byte[] in) throws Exception;
  public abstract byte[] decrypt(byte[] in) throws Exception;
  public abstract void encrypt(InputStream is, OutputStream os) throws Exception;
  public abstract void decrypt(InputStream is, OutputStream os) throws Exception;

  public String getEncryptedPassword() throws Exception {
    return new String(Lib.base64Encode(encrypt(password.getBytes())));
  }

  public String decryptedPassword(String pass) throws Exception {
    return new String(decrypt(Lib.base64Decode(pass)));
  }

} // end of class PBEBase.
