/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 06-10-2004  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.parser.helper;

import java.io.*;
import com.judoscript.parser.*;

public final class MixtureParser
{
  CharStream instrm;
  StringBuffer sb;
  int state;
  int mark;
  boolean multi;
  int ch;

  public MixtureParser() {}

  public String parse(CharStream jcs, boolean multi) throws IOException {
    instrm = jcs;
    this.multi = multi;
    if (sb==null)
      sb = new StringBuffer();
    else
      sb.setLength(0);
    state = 0;
    mark = -1;

    fsa();

    return sb.toString();
  }

  private int readChar() throws IOException {
    return instrm.readChar();
  }

  // push back and exit if single && ';' || multi && '}'
  private boolean action0() {
    if (multi && ch=='}' || !multi && ch==';') {
      instrm.backup(1);
      return true;
    } else {
      sb.append((char)ch);
      return false;
    }
  }

  // action1() -- add
  private void action1() {
    sb.append((char)ch);
  }

  // action2() -- mark; add
  private void action2() {
    mark = sb.length();
    sb.append((char)ch);
  }

  // action3() -- add; clear mark
  private void action3() {
    sb.append((char)ch);
    mark = -1;
  }

  // action4() -- remove from mark; clear mark
  private void action4() {
    sb.setLength(mark);
    mark = -1;
  }

  // action5() -- remove from mark; clear mark; add
  private void action5() {
    sb.setLength(mark);
    mark = -1;
    sb.append((char)ch);
  }


  /**
   * ==================
   *  Generated method
   * ==================
   *
   * Need these methods:
   *
   *  readChar() -- should handle EOF
   *  action0() -- push back and exit if single&&';' || multi&&'}'
   *  action1() -- add
   *  action2() -- mark; add
   *  action3() -- add; clear mark
   *  action4() -- remove from mark; clear mark
   *  action5() -- remove from mark; clear mark; add
   */
  private void fsa() throws IOException {
    state = 0;
loop:
    while(true) {
      ch = readChar();
      //int old_state = state;
      switch(state) {
      case 0:
        switch(ch) {
        case '(' : action1(); state=1; break;
        case ';' : if (action0()) break loop; break;
        case '}' : if (action0()) break loop; break;
        case '/' : action2(); state=9; break;
        case '-' : action2(); state=15; break;
        case '#' : action2(); state=17; break;
        default  : action1(); break;
        }
        break;
      case 1:
        switch(ch) {
        case '*' : action1(); state=2; break;
        default  : action1(); state=0; break;
        }
        break;
      case 2:
        switch(ch) {
        case ')' : action1(); state=0; break;
        case ' ' : action1(); break;
        case '\t': action1(); break;
        case '\r': action1(); break;
        case '\n': action1(); break;
        default  : action1(); state=3; break;
        }
        break;
      case 3:
        switch(ch) {
        case '*' : action1(); state=4; break;
        case '\'': action1(); state=5; break;
        case '"' : action1(); state=7; break;
        default  : action1(); break;
        }
        break;
      case 4:
        switch(ch) {
        case ')' : action1(); state=0; break;
        default  : action1(); state=3; break;
        }
        break;
      case 5:
        switch(ch) {
        case '\\': action1(); state=6; break;
        case '\'': action1(); state=3; break;
        default  : action1(); break;
        }
        break;
      case 6:
        switch(ch) {
        default  : action1(); state=5; break;
        }
        break;
      case 7:
        switch(ch) {
        case '\\': action1(); state=8; break;
        case '"' : action1(); state=3; break;
        default  : action1(); break;
        }
        break;
      case 8:
        switch(ch) {
        default  : action1(); state=7; break;
        }
        break;
      case 9:
        switch(ch) {
        case '*' : action1(); state=10; break;
        case '?' : action1(); state=12; break;
        case '/' : action1(); state=14; break;
        default  : action3(); state=0; break;
        }
        break;
      case 10:
        switch(ch) {
        case '*' : action1(); state=11; break;
        default  : action1(); break;
        }
        break;
      case 11:
        switch(ch) {
        case '/' : action4(); state=0; break;
        default  : action1(); state=10; break;
        }
        break;
      case 12:
        switch(ch) {
        case '?' : action1(); state=13; break;
        default  : action1(); break;
        }
        break;
      case 13:
        switch(ch) {
        case '/' : action4(); state=0; break;
        default  : action1(); state=12; break;
        }
        break;
      case 14:
        switch(ch) {
        case '\r': action5(); state=0; break;
        case '\n': action5(); state=0; break;
        default  : action1(); break;
        }
        break;
      case 15:
        switch(ch) {
        case '-' : action1(); state=16; break;
        default  : action1(); state=0; break;
        }
        break;
      case 16:
        switch(ch) {
        case '\r': action5(); state=0; break;
        case '\n': action5(); state=0; break;
        default  : action1(); break;
        }
        break;
      case 17:
        switch(ch) {
        case '!' : action1(); state=18; break;
        default  : action1(); state=0; break;
        }
        break;
      case 18:
        switch(ch) {
        case '\r': action5(); state=0; break;
        case '\n': action5(); state=0; break;
        default  : action1(); break;
        }
        break;
      }
      //System.out.println("["+old_state+"]--" + ((char)ch) + "-->["+state+"] (mark: " + mark + "): " + sb);
    }
  } // end of fsa().

} // end of class.
