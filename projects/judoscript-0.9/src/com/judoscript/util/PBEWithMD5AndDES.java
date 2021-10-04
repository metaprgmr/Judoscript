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
import javax.crypto.*;
import javax.crypto.spec.*;

public class PBEWithMD5AndDES extends PBEBase
{
  public static final String ENCRYPTION_NAME = "PBEWithMD5AndDES";

  PBEParameterSpec pbeParamSpec;
  SecretKey        pbeKey;

  public PBEWithMD5AndDES(String password) throws Exception {
    super(password);

    byte[] salt = {
      (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
      (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };
    int count = 20;

    SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ENCRYPTION_NAME);
    pbeParamSpec = new PBEParameterSpec(salt, count);
    char[] ca = new char[password.length()];
    password.getChars(0, ca.length, ca, 0);
    pbeKey = keyFac.generateSecret(new PBEKeySpec(ca));
  }

  public byte[] encrypt(byte[] data) throws Exception {
    Cipher c = Cipher.getInstance(ENCRYPTION_NAME);
    c.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
    return c.doFinal(data);
  }

  public byte[] decrypt(byte[] data) throws Exception {
    Cipher c = Cipher.getInstance(ENCRYPTION_NAME);
    c.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
    return c.doFinal(data);
  }

  public void encrypt(InputStream is, OutputStream os) throws Exception {
    Cipher c = Cipher.getInstance(ENCRYPTION_NAME);
    c.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
    CipherInputStream cis = new CipherInputStream( is, c );

    int i;
    while ( (i=cis.read()) >= 0 )
      os.write(i);
  }

  public void decrypt(InputStream is, OutputStream os) throws Exception {
    Cipher c = Cipher.getInstance(ENCRYPTION_NAME);
    c.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
    CipherInputStream cis = new CipherInputStream(is, c);

    int i;
    while ( (i=cis.read()) >= 0 )
      os.write(i);
  }

} // end of class PBEWithMD5AndDES.
