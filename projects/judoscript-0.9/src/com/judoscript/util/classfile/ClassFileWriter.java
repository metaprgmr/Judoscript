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

import java.lang.reflect.Modifier;
import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;


public class ClassFileWriter implements ClassFileConsts
{
  Vector cp = new Vector();         // constant_pool; cp[0] is not used by spec.

  static class MyClassLoader extends ClassLoader {
    MyClassLoader(ClassLoader parent) { super(parent); }
    public Class defineClass(String name, byte[] d) { return defineClass(name,d,0,d.length); }
  }
  static MyClassLoader myClassLoader = new MyClassLoader(Thread.currentThread().getContextClassLoader());

  public String className;
  int access_flags; // bit-wise ORs of Modifier flags.
  int this_class;   // u2 index to cp of CONSTANT_Class.
  int super_class;  // u2 index to cp of CONSTANT_Class.
  int[] interfaces; // u2[] indices to cp of CONSTANT_Class's.

  Hashtable fields = new Hashtable(); // String -> Member
  Vector methods = new Vector();      // Member's
  Vector attributes = new Vector();   // Attr's


  public ClassFileWriter(String accesses, String thisclass, String parentclass, Class[] itfs) {
    this(ClassFileUtil.getAccessFlags(accesses), thisclass, parentclass, itfs);
  }

  public ClassFileWriter(int accesses, String thisclass, String parentclass, Class[] itfs) {
    init(thisclass,accesses);
    super_class = cpClass(StringUtils.defaultString(parentclass,"java/lang/Object").replace('.','/'));
    if (itfs == null)
      interfaces = null;
    else {
      interfaces = new int[itfs.length];
      for (int i=0; i<itfs.length; interfaces[i]=cpClass(itfs[i++].getName().replace('.','/')));
    }
  }

  // parents: [0] may be a class; rest are interfaces.
  public ClassFileWriter(int accesses, String thisclass, Class[] parents) {
    init(thisclass,accesses);
    interfaces = null;
    int idx;
    int len = (parents==null) ? 0 : parents.length;
    if (len > 0) {
      if (parents[0].isInterface()) {
        idx = 0;
        interfaces = new int[len];
      } else {
        super_class = cpClass(parents[0].getName().replace('.','/'));
        idx = 1;
        if (len > 1) interfaces = new int[len-1];
      }
      for (int ptr=0; idx<len; ++idx)
        interfaces[ptr++] = cpClass(parents[idx].getName().replace('.','/'));
    }
    if (super_class <= 0) super_class = cpClass("java/lang/Object");
  }

  private void init(String thisclass,int accesses) {
    className = thisclass;
    cp.addElement("placeholder");
    this_class = cpClass(thisclass.replace('.','/'));
    access_flags = accesses | Modifier.SYNCHRONIZED; // for SUPER.
  }

  public Member addField(String desc, String name) { return addField(Modifier.PUBLIC,desc,name,null); }
  public Member addField(int access, String desc, String name, Attr[] attrs) {
    desc = desc.replace('.','/');
    Member m = new Member(cpUtf8(name.replace('.','/')), cpUtf8(desc), access, attrs,
                          cpFieldRef(className,name,desc));
    fields.put(name,m);
    return m;
  }
  public Member addField(String access, String desc, String name, Attr[] attrs) {
    return addField(ClassFileUtil.getAccessFlags(access),desc,name,attrs);
  }
  public int getFieldRef(String name) throws BadClassFormatException {
    Member m = (Member)fields.get(name);
    if (m==null) throw new BadClassFormatException("Field '" + name + "' does not exist.");
    return m.cp_index;
  }

  public Member addMethod(String access, String desc, String name, String[] exceptions, Attr.Code code) {
    return addMethod(ClassFileUtil.getAccessFlags(access),desc,name,exceptions,code);
  }

  public Member addMethod(int access, String desc, String name, String[] exceptions, Attr.Code code) {
    Attr[] attrs;
    if ((exceptions!=null) && (exceptions.length>0)) {
      if (code == null)
        attrs = new Attr[]{ new Attr.Exceptions(this,exceptions) };
      else
        attrs = new Attr[]{ code, new Attr.Exceptions(this,exceptions) };
    } else if (code != null) {
      attrs = new Attr[]{ code };
    } else {
      attrs = null;
    }
    Member m = new Member(cpUtf8(name.replace('.','/')), cpUtf8(desc.replace('.','/')),
                          access, attrs, cpMethodRef(className,name,desc));
    methods.addElement(m);
    return m;
  }

  public Member addMethod(int access, Class retType, Class[] paramTypes, String name,
                          Class[] exceptions, Attr.Code code)
  {
    String[] excpts = null;
    int len = (exceptions==null) ? 0 : exceptions.length;
    if (len > 0) {
      excpts = new String[len];
      for (int i=0; i<len; excpts[i]=exceptions[i++].getName().replace('.','/'));
    }
    return addMethod(access,ClassFileUtil.getVMMethodSig(retType,paramTypes),name,excpts,code);
  }

  public Member addConstructor(int access, Class[] params, Class[] exceptions, Attr.Code code) {
    return addMethod(access,Void.TYPE,params,"<init>",exceptions,code);
  }

  public Member addConstructor(String access, String desc, String[] exceptions, Attr.Code code) {
    return addMethod(access,desc,"<init>",exceptions,code);
  }

  public Member addConstructor(int access, String desc, String[] exceptions, Attr.Code code) {
    return addMethod(access,desc,"<init>",exceptions,code);
  }

  public Member addStaticInit(Attr.Code code) {
    return addMethod(Modifier.PUBLIC|Modifier.STATIC,"<clinit>","()V",null,code);
  }

  public void addAttr(Attr attr) { attributes.addElement(attr); }

  public Class toClass() {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      write(baos);
      return myClassLoader.defineClass(className,baos.toByteArray());
    } catch(IOException ioe) { return null; }
  }

  public void write(String fname) throws IOException { write(new FileOutputStream(fname)); }

  public void write(OutputStream os) throws IOException {
    int i;

    if ((access_flags & Modifier.INTERFACE) == 0) {
      // check on constructors -- if none, add a default one.
      boolean hasCtors = false;
      for (i=0; i<methods.size(); ++i) {
        String name = getCPString(((Member)methods.elementAt(i)).name_index);
        if ("<init>".equals(name)) { hasCtors = true; break; }
      }
      if (!hasCtors) {
        MethodBody mb = new MethodBody("<init>",this,1,1);
        mb._aload_0();
        mb._invokespecial(getCPClassName(super_class),"<init>","()V");
        mb._return();
        try { addConstructor(Modifier.PUBLIC, "()V", null, mb.getCode()); }
        catch(BadClassFormatException e) {}
      }
    }

    DataOutputStream dos = (os instanceof DataOutputStream)
                           ? (DataOutputStream)os : new DataOutputStream(os);

    dos.writeInt(0x0CAFEBABE);
    dos.writeShort(MINOR);
    dos.writeShort(MAJOR);

    dos.writeShort(cp.size());
    for (i=1; i<cp.size(); ((CPInfo)cp.elementAt(i++)).write(dos));

    dos.writeShort(access_flags);
    dos.writeShort(this_class);
    dos.writeShort(super_class);
    if (interfaces == null) {
      dos.writeShort(0);
    } else {
      dos.writeShort(interfaces.length);
      for (i=0; i<interfaces.length; dos.writeShort(interfaces[i++]));
    }

    dos.writeShort(fields.size());
    Iterator values = fields.values().iterator();
    while (values.hasNext()) ((Member)values.next()).write(dos);

    dos.writeShort(methods.size());
    for (i=0; i<methods.size(); ((Member)methods.elementAt(i++)).write(dos));

    dos.writeShort(attributes.size());
    for (i=0; i<attributes.size(); ((Attr)attributes.elementAt(i++)).write(dos));

    dos.close();
  }

/////////////////////////////////////////////////////////////
//
  private Hashtable bookkeeper = new Hashtable(); // String -> Integer
  private int u4_0 = -1;
  private int u4_1 = -1;
  private int u8_0 = -1;
  private int u8_1 = -1;

  public String getCPString(int idx) { return ((CPInfo.Utf8)cp.elementAt(idx)).text; }
  public String getCPClassName(int idx) { return getCPString(((CPInfo.U2)cp.elementAt(idx)).index); }

  public int cpUtf8(String text) {
    String k = "utf:" + text;
    Integer I = (Integer)bookkeeper.get(k);
    if (I != null) return I.intValue();
    cp.addElement( new CPInfo.Utf8(text) );
    int i = cp.size() - 1;
    bookkeeper.put(k, new Integer(i));
    return i;
  }
  public int cpInt(int i) {
    switch(i) {
    case 0:  if (u4_0 < 0) {
               cp.addElement( new CPInfo.U4(CONSTANT_Integer,0) );
               u4_0 = cp.size() - 1;
             }
             return u4_0;
    case 1:  if (u4_1 < 0) {
               cp.addElement( new CPInfo.U4(CONSTANT_Integer,1) );
               u4_1 = cp.size() - 1;
             }
             return u4_1;
    default: cp.addElement( new CPInfo.U4(CONSTANT_Integer,i) );
             return cp.size() - 1;
    }
  }
  public int cpLong(long l) {
    if (l==0) {
      if (u8_0 < 0) {
        cp.addElement( new CPInfo.U8(CONSTANT_Long,0,0) );
        u8_0 = cp.size() - 1;
      }
      return u8_0;
    } else if (l==1) {
      if (u8_1 < 0) {
        cp.addElement( new CPInfo.U8(CONSTANT_Long,0,1) );
        u8_1 = cp.size() - 1;
      }
      return u8_1;
    } else {
      cp.addElement( new CPInfo.U8(CONSTANT_Long,l) );
      return cp.size() - 1;
    }
  }
  public int cpFloat(float f) {
    cp.addElement( new CPInfo.U4(CONSTANT_Float,Float.floatToIntBits(f)) );
    return cp.size() - 1;
  }
  public int cpDouble(double d) {
    cp.addElement( new CPInfo.U8(CONSTANT_Double,Double.doubleToLongBits(d)) );
    return cp.size() - 1;
  }
  public int cpString(String text) { return _cpText(text,CONSTANT_String,"s:"); }
  public int cpClass(String name)  { return _cpText(name,CONSTANT_Class, "c:"); }
  public int cpNameAndType(String name, String type) {
    String prefix = "nt:" + name + ":" + type;
    Integer I = (Integer)bookkeeper.get(prefix);
    if (I != null) return I.intValue();
    cp.addElement( new CPInfo.NameAndType(cpUtf8(name), cpUtf8(type)) );
    int i = cp.size() - 1;
    bookkeeper.put(prefix, new Integer(i));
    return i;
  }
  public int cpFieldRef (String className, String name, String type) {
    return _cpRef(className,name,type,CONSTANT_Fieldref,"f:");
  }
  public int cpMethodRef(String className, String name, String type) {
    return _cpRef(className,name,type,CONSTANT_Methodref,"m:");
  }
  public int cpInterfaceMethodRef(String className, String name, String type) {
    return _cpRef(className,name,type,CONSTANT_InterfaceMethodref,"im:");
  }

  private int _cpText(String text, int type, String prefix) {
    prefix += text;
    Integer I = (Integer)bookkeeper.get(prefix);
    if (I != null) return I.intValue();
    cp.addElement( new CPInfo.U2(type, cpUtf8(text)) );
    int i = cp.size() - 1;
    bookkeeper.put(prefix, new Integer(i));
    return i;
  }

  private int _cpRef(String className, String name, String type, int _type, String prefix) {
    className = className.replace('.','/');
    name = name.replace('.','/');
    type = type.replace('.','/');
    prefix += className + ":" + name + ":" + type;
    Integer I = (Integer)bookkeeper.get(prefix);
    if (I != null) return I.intValue();
    cp.addElement( new CPInfo.Ref(_type, cpClass(className), cpNameAndType(name,type)) );
    int i = cp.size() - 1;
    bookkeeper.put(prefix, new Integer(i));
    return i;
  }

  public void addDefaultCtor() {
    MethodBody mb = new MethodBody("<init>", this, 1, 1);
    mb._aload_0();
    mb._invokespecial(getCPClassName(super_class),"<init>","()V");
    mb._return();
    try { addMethod(Modifier.PUBLIC,"()V","<init>",null,mb.getCode()); }
    catch(Exception e) { e.printStackTrace(); }
  }

  public void helloWorld() {
    MethodBody mb = new MethodBody("main",this);
    mb.setMaxStack(2);
    mb.sysPrintln("out","Hello World!");
    mb._return();
    try {
      addMethod(Modifier.PUBLIC|Modifier.STATIC, "([Ljava.lang.String;)V", "main", null, mb.getCode());
    } catch(BadClassFormatException e) { /* not happening */ }
  }

} // end of class ClassFileWriter.


/*[judo]

const #cfc = javaclass com.judoscript.util.classfile.ClassFileConsts;
const #cfw = javaclass com.judoscript.util.classfile.ClassFileWriter;

itfs = javanew java.lang.String[] { 'java.lang.Cloneable', 'java.lang.Comparable' };
itfs = null;

newclass = javanew #cfw('public abstract class', 'Foo', 'java.lang.Object', itfs);
newclass.addMethod('public abstract', '(Ljava.lang.Object;)I', 'compareTo', null, null);
newclass.write('Foo.class');

catch: $_.printInternalStackTrace();

[judo]*/

