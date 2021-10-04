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
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package test;

/**
 * This object is simply to test the native java interface
 */
public class TestObject
{
  public boolean pub_bool = true;
  public byte    pub_byte = (byte)5;
  public char    pub_char = ':';
  public short   pub_short = (short)5;
  public int     pub_int  = 50;
  public long    pub_long = 500;
  public float   pub_float = (float)0.5;
  public double  pub_double = 5.5;
  public String  pub_str = "A String";
  public java.util.Date pub_date = new java.util.Date();
  public Object  pub_obj = null;

  public TestObject() {}
  public TestObject(boolean _bool)  { pub_bool = _bool; }
  public TestObject(byte _byte)     { pub_byte = _byte; }
  public TestObject(char _char)     { pub_char = _char; }
  public TestObject(short _short)   { pub_short = _short; }
  public TestObject(int _int)       { pub_int = _int; }
  public TestObject(long _long)     { pub_long = _long; }
  public TestObject(float _float)   { pub_float = _float; }
  public TestObject(double _double) { pub_double = _double; }
  public TestObject(String _String) { pub_str = _String; }

  public int sum(int a, int b) { return a + b; }
  public String quote(String s) { return "\"" + s + "\""; }
  public void happy() { System.out.println(); System.out.println("Happy Happy!"); }

  public void dump() {
    System.out.println(pub_bool);
    System.out.println(pub_byte);
    System.out.println(pub_char);
    System.out.println(pub_short);
    System.out.println(pub_int);
    System.out.println(pub_long);
    System.out.println(pub_float);
    System.out.println(pub_double);
    System.out.println(pub_str);
    System.out.println(pub_date);
  }
}
