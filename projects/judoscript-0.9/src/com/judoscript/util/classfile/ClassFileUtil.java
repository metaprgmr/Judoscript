/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-9-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util.classfile;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.*;
import java.util.Hashtable;
import org.apache.commons.lang.StringUtils;

public class ClassFileUtil implements ClassFileConsts
{
  public static final Hashtable opcodeMap = new Hashtable();
  static {
    for (int i=0; i<instructions.length; ++i)
      opcodeMap.put(instructions[i].mnemonic, instructions[i]);
  }

  public static int getOpcode(String mnemonic) {
    try { return ((InstInfo)opcodeMap.get(mnemonic)).opcode; }
    catch(ArrayIndexOutOfBoundsException aioobe) { return UNKNOWN; }
  }

  public static String getAccessFlagNames(int flags) {
    StringBuffer sb = new StringBuffer();
    if ((flags & Modifier.PUBLIC) == Modifier.PUBLIC) sb.append("public ");
    if ((flags & Modifier.PRIVATE) == Modifier.PRIVATE) sb.append("private ");
    if ((flags & Modifier.PROTECTED) == Modifier.PROTECTED) sb.append("protected ");
    if ((flags & Modifier.STATIC) == Modifier.STATIC) sb.append("static ");
    if ((flags & Modifier.FINAL) == Modifier.FINAL) sb.append("final ");
    if ((flags & Modifier.SYNCHRONIZED) == Modifier.STRICT) sb.append("synchronized ");
    if ((flags & Modifier.VOLATILE) == Modifier.VOLATILE) sb.append("volatile ");
    if ((flags & Modifier.TRANSIENT) == Modifier.TRANSIENT) sb.append("transient ");
    if ((flags & Modifier.NATIVE) == Modifier.NATIVE) sb.append("native ");
    if ((flags & Modifier.ABSTRACT) == Modifier.ABSTRACT) sb.append("abstract ");
    if ((flags & Modifier.STRICT) == Modifier.STRICT) sb.append("strict ");
    if ((flags & Modifier.INTERFACE) == Modifier.INTERFACE) sb.append("interface ");
    return sb.toString().trim();
  }

  public static int getAccessFlags(String accessNames) {
    if (StringUtils.isBlank(accessNames)) return 0;
    int ret = 0;
    if (accessNames.indexOf("public") >= 0) ret |= Modifier.PUBLIC;
    else if (accessNames.indexOf("protected") >= 0) ret |= Modifier.PROTECTED;
    else if (accessNames.indexOf("private") >= 0) ret |= Modifier.PRIVATE;
    if (accessNames.indexOf("static") >= 0) ret |= Modifier.STATIC;
    if (accessNames.indexOf("final") >= 0) ret |= Modifier.FINAL;
    if (accessNames.indexOf("synchronized") >= 0) ret |= Modifier.SYNCHRONIZED;
    if (accessNames.indexOf("native") >= 0) ret |= Modifier.NATIVE;
    if (accessNames.indexOf("abstract") >= 0) ret |= Modifier.ABSTRACT;
    if (accessNames.indexOf("interface") >= 0) ret |= Modifier.INTERFACE;
    return ret;
  }

  public static int[] checkCode(byte[] code) throws BadClassFormatException {
    int pc = 0;
    int[] addrs = new int[code.length];
    try {
      for (int i=0; i<code.length; ) {
        int opcode = getU1(code,i);
        addrs[pc++] = i++;
        InstInfo inst = instructions[opcode];
        int operands = inst.operand_bytes;
        if (operands >= 0) {
          i += operands;
        } else if (operands == UNKNOWN) {
          switch(opcode) {
          case OPCODE_wide:
            opcode = getU1(code,i);
            inst = instructions[opcode];
            i += 1 + 2 * inst.operand_bytes;
            continue;

          case OPCODE_tableswitch:
            i += ((4-i) % 4) + 4; // padding + default address
            int low  = getS4(code,i);
            i += 4;
            int high = getS4(code,i);
            i += 4 + ((high - low + 1) << 2); // * 4.
            continue;

          case OPCODE_lookupswitch:
            i += ((4-i) % 4) + 4; // padding + default address
            int npairs = getS4(code,i);
            i += 4 + (npairs << 3); // * 8.
            continue;
          }
        }
      }
    } catch(ArrayIndexOutOfBoundsException ae) {
      throw new BadClassFormatException("Code checking out of bounds.");
    }
    if (pc < addrs.length) {
      int[] ia = new int[pc];
      System.arraycopy(addrs, 0, ia, 0, pc);
      addrs = ia;
    }
    return addrs;
  }

  public static String getInst(byte[] code, int addr) throws BadClassFormatException {
    int opcode = getU1(code,addr++);
    InstInfo inst = instructions[opcode];
    int operands = inst.operand_bytes;
    if (operands == 0) return inst.mnemonic;
    StringBuffer sb = new StringBuffer(inst.mnemonic);
    if (operands > 0) {
      short[] params = inst.operand_types;
      for (int i=0; i<params.length; ++i) {
        sb.append(' ');
        switch(params[i]) {
        case T_U4:
        case T_S4: sb.append(getS4(code,addr)); addr+=4; break;
        case T_U2: sb.append(getU2(code,addr)); addr+=2; break;
        case T_S2: sb.append(getS2(code,addr)); addr+=2; break;
        case T_U1: sb.append(getU1(code,addr++)); break;
        case T_S1: sb.append(code[addr++]); break;
        }
      }
    } else if (operands == UNKNOWN) {
      switch(opcode) {
      case OPCODE_wide:
        sb.append(' ');
        opcode = getU1(code,addr++);
        inst = instructions[opcode];
        sb.append(inst.mnemonic);
        operands = inst.operand_bytes;
        short[] params = inst.operand_types;
        for (int i=0; i<params.length; ++i) {
          sb.append(' ');
          switch(2*params[i]) {
          case T_U4:
          case T_S4: sb.append(getS4(code,addr)); addr+=4; break;
          case T_U2: sb.append(getU2(code,addr)); addr+=2; break;
          case T_S2: sb.append(getS2(code,addr)); addr+=2; break;
          }
        }
        break;

      case OPCODE_tableswitch:
        addr += ((4-addr) % 4); // padding
        sb.append(" *:" + getS4(code,addr)); addr += 4;
        int low  = getS4(code,addr); addr += 4;
        int high = getS4(code,addr); addr += 4;
        for (; low<=high; low++) {
          sb.append("|");
          sb.append(low);
          sb.append(":");
          sb.append(getS4(code,addr));
          addr += 4;
        }
        break;

      case OPCODE_lookupswitch:
        addr += ((4-addr) % 4); // padding
        sb.append(" *:" + getS4(code,addr));
        addr += 4;
        int npairs = getS4(code,addr);
        addr += 4;
        for (int i=0; i<npairs; i++) {
          sb.append("|");
          sb.append(getS4(code,addr));
          addr += 4;
          sb.append(":");
          sb.append(getS4(code,addr));
          addr += 4;
        }
        break;
      }
    }
    return sb.toString();
  }

  public static void putS2(byte[] data, int idx, int val) {
    data[idx++] = (byte)(val>>8);
    data[idx]   = (byte)val;
  }
  public static void putS4(byte[] data, int idx, int val) {
    data[idx++] = (byte)(val>>24);
    data[idx++] = (byte)(val>>16);
    data[idx++] = (byte)(val>>8);
    data[idx]   = (byte)val;
  }
  public static void putS8(byte[] data, int idx, long val) {
    data[idx++] = (byte)(val>>56);
    data[idx++] = (byte)(val>>48);
    data[idx++] = (byte)(val>>40);
    data[idx++] = (byte)(val>>32);
    data[idx++] = (byte)(val>>24);
    data[idx++] = (byte)(val>>24);
    data[idx++] = (byte)(val>>16);
    data[idx++] = (byte)(val>>8);
    data[idx]   = (byte)val;
  }

  public static int getU1(byte[] data, int idx) { return data[idx]&0x0FF; }
  public static int getU2(byte[] data, int idx) { return (data[idx++]<<8)&0x0FF00 | data[idx]&0x0FF; }
  public static int getS4(byte[] data, int idx) {
    return (data[idx++]<<24) | (data[idx++]<<16)&0x0FF0000 | (data[idx++]<<8)&0x0FF00 | data[idx]&0x0FF;
  }
  public static long getS8(byte[] data, int idx) { return ((long)getS4(data,idx)<<32) | getS4(data,idx+4); }
  public static short getS2(byte[] data, int idx) { return (short)getU2(data,idx); }

  public static int getUtf8Length(String s) {
    try { return s.getBytes("UTF-8").length; } catch(Exception e) { return 0; }  
  }

  public static String getHtmlRefCP(int i) { return "<a href=#cp_"+i+">cp["+i+"]</a>"; }

  public static void printAttrsHtml(PrintWriter out, Attr[] attrs) throws IOException {
    if (attrs == null) return;
    out.println("<table border=1>");
    for (int i=0; i<attrs.length; ++i) {
      out.println("<tr><td valign=top>"+i+"</td><td>");
      attrs[i].printHtml(out);
      out.println("</td></tr>");
    }
    out.println("</table>");
  }

  // returns int[0] input stack size, int[1] output stack size.
  public static int[] getInvokeStackSizes(String desc) throws BadClassFormatException {
    int[] ret = {0,0};
    if (!desc.startsWith("(")) return ret;
    int ptr = desc.indexOf(")");
    if (ptr <= 0) return ret;
    String x = desc.substring(ptr+1);
    switch(x.charAt(0)) {
    case 'D':
    case 'J': ret[1] = 2; break;
    case 'V': ret[1] = 0; break;
    default:  ret[1] = 1; break;
    }
    try {
      desc = desc.substring(1,ptr);
loop: for (ptr=0; ; ++ptr) {
        switch(desc.charAt(ptr)) {
        case 'D':
        case 'J': ret[0]+=2; break;
        case 'L': ++ret[0];
                  ptr = desc.indexOf(';',ptr);
                  if (ptr < 0) // ??? something wrong.
                    break loop;
                  break;
        case '[': ++ret[0];
                  while (desc.charAt(ptr) == '[') ++ptr;
                  if (desc.charAt(ptr) == 'L') {
                    ptr = desc.indexOf(';',ptr);
                    if (ptr < 0)
                      throw new BadClassFormatException("Invalid signature: " + desc);
                  }
                  break;
        default:  ++ret[0]; break;
        }
      }
    } catch(Exception e) {}
    return ret;
  }

  // returns a byte[] of 'V', 'Z', 'B', 'C', 'S', 'I', 'J', 'F', 'D',
  // 'T' for String, 'O' for Object and 'A' (for objects and arrays);
  // the last is the return type.
  public static byte[] parseMethodSig(String desc) throws BadClassFormatException {
    int sublen = "Ljava/lang/String;".length();
    byte[] ba = new byte[50]; // if more than 49 parameters, explode!
    int idx = 0;
    if (!desc.startsWith("(")) return null;
    int ptr = desc.indexOf(")");
    if (ptr <= 0) return null;
    byte retType;
    char ch = desc.charAt(ptr+1);
    switch(ch) {
    case 'Z':
    case 'B':
    case 'C':
    case 'S':
    case 'I':
    case 'J':
    case 'F':
    case 'D':
    case 'V': retType = (byte)ch; break;
    default:  retType = (byte)(desc.regionMatches(ptr+1,"Ljava/lang/String;",0,sublen) ? 'T' : 'A');
              if ( (retType == 'A') && (desc.regionMatches(ptr+1,"Ljava/lang/Object;",0,sublen)) )
                retType = (byte)'O';
              break;
    }
    for (int i=1; i<ptr; ++i) {
      ch = desc.charAt(i);
      switch(ch) {
      case 'Z':
      case 'B':
      case 'C':
      case 'S':
      case 'I':
      case 'J':
      case 'F':
      case 'D':
      case 'V': ba[idx++] = (byte)ch; break;
      default:  ba[idx] = (byte)(desc.regionMatches(i,"Ljava/lang/String;",0,sublen) ? 'T' : 'A');
                if ( (ba[idx] == 'A') && (desc.regionMatches(i,"Ljava/lang/Object;",0,sublen)) )
                  ba[idx] = (byte)'O';
                ++idx;
                while (desc.charAt(i) == '[') ++i;
                if (desc.charAt(i) == 'L') {
                  while ((i<ptr) && (desc.charAt(++i) != ';'));
                  if (i>=ptr)
                    throw new BadClassFormatException("Invalid signature: " + desc);
                }
                break;
      }
    }
    ba[idx] = retType;
    byte[] ret = new byte[++idx];
    System.arraycopy(ba,0,ret,0,idx);
    return ret;
  }

  // returns int[0] input stack size, int[1] output stack size.
  public static int[] getInvokeStackSizes(Class retType, Class[] params) {
    int in = 0;
    for (int i=0; i<params.length; in+=getVMTypeSize(params[i++]));
    return new int[]{ in, getVMTypeSize(retType) };
  }

  public static int getVMTypeSize(Class c) {
    return c.equals(Void.TYPE) ? 0 : ((c.equals(Double.TYPE) || c.equals(Long.TYPE)) ? 2 : 1);
  }

  public static String getVMTypeName(Class c) {
    if (c.equals(Void.TYPE)) return "V";
    if (c.isArray()) return '[' + getVMTypeName(c.getComponentType());
    if (c.isPrimitive()) {
      if (c.equals(Integer.TYPE))   return "I";
      if (c.equals(Boolean.TYPE))   return "Z";
      if (c.equals(Short.TYPE))     return "S";
      if (c.equals(Byte.TYPE))      return "B";
      if (c.equals(Character.TYPE)) return "C";
      if (c.equals(Long.TYPE))      return "J";
      if (c.equals(Float.TYPE))     return "F";
      if (c.equals(Double.TYPE))    return "D";
    }
    return 'L' + c.getName().replace('.','/') + ';';
  }

  public static String getVMMethodSig(Method m) {
    return getVMMethodSig(m.getReturnType(), m.getParameterTypes());
  }

  public static String getVMMethodSig(Class retType, Class[] params) {
    StringBuffer sb = new StringBuffer("(");
    int len = (params==null) ? 0 : params.length;
    for (int i=0; i<len; ++i)
      sb.append(getVMTypeName(params[i]));
    sb.append(')');
    sb.append(getVMTypeName(retType));
    return sb.toString();
  }

} // end of class ClassFileUtil.

/*[judo]

x = '(Ljava/lang/String;II)V';

for a in com.judoscript.util.classfile.ClassFileUtil::getInvokeStackSizes(x) {
  . a;
}
 
[judo]*/
