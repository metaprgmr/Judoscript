/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-05-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.parser.helper;

import java.util.*;
import com.judoscript.*;
import com.judoscript.bio.JavaExtension;
import com.judoscript.util.Lib;
import com.judoscript.util.DoubleKey;
import com.judoscript.util.classfile.*;
import com.judoscript.parser.ParseException;
import java.lang.reflect.*;

public class JavaExtensionHelper implements Consts
{
  private int method_index = 1;
  private HashSet userMethods = new HashSet();

  String thisClassName;
  Script script;
  Class  superClass = null;
  Class  interfaces[] = { com.judoscript.bio.JavaExtensionUser.class };
  HashMap fields = new HashMap();  // String(name) -> String(VM-desc)
  Function constructor = null;

  public JavaExtensionHelper(String name) {
    thisClassName = name;
  }

  public void addParent(String clsName) throws ParseException {
    try { 
      Class c = RT.getSysClass(clsName);
      if (!c.isInterface()) {
        if (superClass != null)
          throw new ParseException("Attempt to extend both "+superClass.getName()+" and "+clsName);
        superClass = c;
      } else {
        for (int i=0; i<interfaces.length; ++i)
          if (interfaces[i].equals(c)) // exists already
            return;
        Class[] ca = new Class[interfaces.length+1];
        System.arraycopy(interfaces,0,ca,0,interfaces.length);
        ca[interfaces.length] = c;
        interfaces = ca;
      }
    } catch(Exception cnfe) {
      throw new ParseException("Class " + clsName + " is not found.");
    }
  }

  public boolean hasConstructor() { return (constructor != null); }

  public void setConstructor(Function ctor) { constructor = ctor; }

  public void defineMethod(Stack stack, int start, int end) { // top 4
    BlockSimple blk = (BlockSimple)stack.pop();
    List params = (List)stack.pop();
    String name = (String)stack.pop();
    String retType = (String)stack.pop();

    String[] ps = new String[params.size()/2];
    StringBuffer sig = new StringBuffer("(");
    for (int i=0; i<params.size(); i+=2) {
      sig.append(params.get(i));
      ps[i/2] = (String)params.get(i+1);
    }
    sig.append(")");
    sig.append(retType);

    userMethods.add( new DoubleKey( name, sig.toString().replace('.','/'),
                                    new FunctionUser(start,end,"_><_"+(method_index++),true,ps,null,blk) )
                   );
  }

  public void addField(String name, String desc) { fields.put(name,desc); }

  public void addField(String name, String desc, int dim) {
    String type = "";
    for (int i=0; i<dim; i++) type += '[';
    if (desc.length() == 1) type += desc;  // primitive type.
    else type += 'L' + desc + ';';
    fields.put(name,type);
  }

  // Create the Java class and do everything.
  public JavaExtension.Type getUserType(HashSet superCalls, HashSet javaxCalls) throws Exception {
    Object[] dual = createClass(superCalls, javaxCalls);
    JavaExtension.Type jt = new JavaExtension.Type(thisClassName,(Class)dual[0],(Class[])dual[1]);
    Iterator iter = userMethods.iterator();
    while (iter.hasNext()) {
      jt.addFunction( (Function)((DoubleKey)iter.next()).value );
    }
    if (constructor != null) jt.setConstructor(constructor);
    return jt;
  }

  private void getAccessibleParentMethods(Class[] itfs, HashSet regMethods, HashSet absMethods)
  {
    int i, acc;
    Method m;
    String s;

    // Collect all regular and abstract methods based on this algorithm in 2 steps:
    //
    // 1. Get all public and protected methods in the parent class into regMethods and absMethods.

    // All public methods
    if (superClass == null) superClass = Object.class;
    Method[] mthds = superClass.getMethods();
    for (i=0; i<mthds.length; ++i) {
      m = mthds[i];
      acc = m.getModifiers();
      if ( !Modifier.isStatic(acc) && !Modifier.isFinal(acc) ) {
        (Modifier.isAbstract(acc) ? absMethods : regMethods)
        .add( new DoubleKey(m.getName(), ClassFileUtil.getVMMethodSig(m), m) );
      }
    }
    // Declared protected methods
    mthds = superClass.getDeclaredMethods();
    for (i=0; i<mthds.length; ++i) {
      m = mthds[i];
      acc = m.getModifiers();
      if ( !Modifier.isStatic(acc) && !Modifier.isFinal(acc) && Modifier.isProtected(acc) ) {
        (Modifier.isAbstract(acc) ? absMethods : regMethods)
        .add( new DoubleKey(m.getName(), ClassFileUtil.getVMMethodSig(m), m) );
      }
    }
    // Ancestor's protected methods
    if (!superClass.equals(Object.class)) {
      Class pc = superClass;
      do {
        pc = pc.getSuperclass();
        mthds = pc.getDeclaredMethods();
        for (i=0; i<mthds.length; ++i) {
          m = mthds[i];
          acc = m.getModifiers();
          if ( !Modifier.isStatic(acc) && !Modifier.isFinal(acc) && Modifier.isProtected(acc) ) {
            (Modifier.isAbstract(acc) ? absMethods : regMethods)
            .add( new DoubleKey(m.getName(), ClassFileUtil.getVMMethodSig(m), m) );
          }
        }
      } while (!pc.equals(Object.class));
    }

    // 2. For all instance methods:
    //    a) if one exists in regMethods, ignore.
    //    b) if one exists in userMethods, ignore.
    //    c) otherwise, add to absMethods.

    for (i=1; i<itfs.length; ++i) {
      mthds = itfs[i].getMethods();
      for (int j=0; j<mthds.length; ++j) {
        m = mthds[j];
        DoubleKey dk = new DoubleKey(m.getName(), ClassFileUtil.getVMMethodSig(m), m);
        if (!regMethods.contains(dk) && !userMethods.contains(dk))
          absMethods.add(dk);
      }
    }
  }

  private void dupMethod(ClassFileWriter cfw, String className, String name, String newName,
                         Class retType, Class[] params, Class[] excpts)
               throws BadClassFormatException
  {
    int[] szs = ClassFileUtil.getInvokeStackSizes(retType,params);
    MethodBody mb = new MethodBody(newName, cfw, Math.max(szs[0],szs[1])+1, szs[0]+1);
    mb._aload_0();
    int ptr = 1;
    for (int j=0; j<params.length; ++j) {
      if (!params[j].isPrimitive())           mb._aload(ptr++);
      else if (params[j].equals(Float.TYPE))  mb._fload(ptr++);
      else if (params[j].equals(Long.TYPE))   { mb._lload(ptr); ptr += 2; }
      else if (params[j].equals(Double.TYPE)) { mb._dload(ptr); ptr += 2; }
      else                                    mb._iload(ptr++);
    }
    mb._invokespecial(className,name,ClassFileUtil.getVMMethodSig(retType,params));
    if (retType.equals(Void.TYPE))        mb._return();
    else if (!retType.isPrimitive())      mb._areturn();
    else if (retType.equals(Float.TYPE))  mb._freturn();
    else if (retType.equals(Long.TYPE))   mb._lreturn();
    else if (retType.equals(Double.TYPE)) mb._dreturn();
    else                                  mb._ireturn();
    cfw.addMethod(Modifier.PUBLIC,retType,params,newName,excpts,mb.getCode());
  }

  private void createMethodProxy(ClassFileWriter cfw, String name, String desc, String target)
               throws BadClassFormatException
  {
    int typ;
    int[] szs = ClassFileUtil.getInvokeStackSizes(desc);
    MethodBody mb = new MethodBody(name, cfw, 8, szs[0]+1);
    mb._aload_0(); 
    mb._getfield(thisClassName.replace('.','/'), "_javax", "Lcom/judoscript/bio/JavaExtension;"); 
    mb._ldc(target); 
    byte[] ba = ClassFileUtil.parseMethodSig(desc);
    if (ba.length == 1) { 
      mb._aconst_null(); 
    } else { 
      mb._bipush(ba.length-1); 
      mb._anewarray("com/judoscript/Variable"); 
      int lvptr = 1;
      for (int i=0; i<ba.length-1; ++i) { 
        mb._dup(); 
        mb._bipush(i); 
        typ = ba[i];
        switch(typ) { // wrap this parameter
        case 'Z':
          mb._iload(lvptr++);
          mb._invokestatic("com/judoscript/ConstInt", "getBool", "(Z)Lcom/judoscript/ConstInt;");
          break;
        case 'B':
          mb._iload(lvptr++);
          mb._invokestatic("com/judoscript/ConstInt", "getInt", "(B)Lcom/judoscript/ConstInt;");
          break;
        case 'C':
          mb._iload(lvptr++);
          mb._invokestatic("com/judoscript/ConstInt", "getInt", "(C)Lcom/judoscript/ConstInt;");
          break;
        case 'S':
          mb._iload(lvptr++);
          mb._invokestatic("com/judoscript/ConstInt", "getInt", "(S)Lcom/judoscript/ConstInt;");
          break;
        case 'I':
          mb._iload(lvptr++);
          mb._invokestatic("com/judoscript/ConstInt", "getInt", "(I)Lcom/judoscript/ConstInt;");
          break;
        case 'J':
          mb._lload(lvptr);
          lvptr += 2;
          mb._invokestatic("com/judoscript/ConstInt", "getInt", "(J)Lcom/judoscript/ConstInt;");
          break;
        case 'F':
        case 'D':
          mb._new("com/judoscript/ConstDouble");
          mb._dup();
          if (typ=='F') { mb._fload(lvptr++); mb._f2d(); }
          else { mb._dload(lvptr); lvptr += 2; }
          mb._invokespecial("com/judoscript/ConstDouble", "<init>", "(D)V");
          break;
        default:
//      case 'T':
          mb._aload(lvptr++);
          mb._invokestatic("com/judoscript/JudoUtil", "toVariable",
                           "(Ljava/lang/Object;)Lcom/judoscript/Variable;");
          break;
        }
        mb._aastore(); // ready to store into the array.
      } 
    } 

    String jxcls = "com/judoscript/bio/JavaExtension";
    String sig = "(Ljava/lang.String;[Lcom/judoscript/Variable;)";

    typ = ba[ba.length-1];
    switch(typ) { // Variable -> return type. 
    case 'V':
      mb._invokevirtual(jxcls, "call", sig + "Lcom/judoscript/Variable;");
      mb._pop();
      mb._return();
      break;
    case 'Z': mb._invokevirtual(jxcls, "callForBool",   sig + "Z"); mb._ireturn(); break;
    case 'B': mb._invokevirtual(jxcls, "callForByte",   sig + "B"); mb._ireturn(); break;
    case 'C': mb._invokevirtual(jxcls, "callForChar",   sig + "C"); mb._ireturn(); break;
    case 'S': mb._invokevirtual(jxcls, "callForShort",  sig + "S"); mb._ireturn(); break;
    case 'I': mb._invokevirtual(jxcls, "callForInt",    sig + "I"); mb._ireturn(); break;
    case 'J': mb._invokevirtual(jxcls, "callForLong",   sig + "J"); mb._lreturn(); break;
    case 'F': mb._invokevirtual(jxcls, "callForFloat",  sig + "F"); mb._freturn(); break;
    case 'D': mb._invokevirtual(jxcls, "callForDouble", sig + "D"); mb._dreturn(); break;
    case 'T': mb._invokevirtual(jxcls, "callForString", sig + "Ljava/lang/String;"); mb._areturn(); break;
    default:  mb._invokevirtual(jxcls, "callForObject", sig + "Ljava/lang/Object;");
              if (typ != 'O') {
                String retcls = desc.substring(desc.indexOf(")")+1).replace('.','/');
                if (retcls.startsWith("L")) retcls = retcls.substring(1,retcls.length()-1);
                mb._checkcast(retcls);
              }
              mb._areturn();
              break;
    }

    cfw.addMethod(Modifier.PUBLIC,desc,name,null,mb.getCode());
  }

  private void createFieldAccess(ClassFileWriter cfw, Field field, HashSet regMethods) {
    MethodBody mb;
    try {
      Class cls = field.getDeclaringClass();
      int sz = (cls.equals(Long.TYPE) || cls.equals(Double.TYPE)) ? 2 : 1;
      String type = ClassFileUtil.getVMTypeName(cls);
      String capName = Lib.capitalizeFirstLetter(field.getName());
      DoubleKey dk = new DoubleKey("get"+capName, "()"+type, null);
      if (!regMethods.contains(dk)) { // create getXXX() method
        mb = new MethodBody((String)dk.o1, cfw, sz, 1);
        mb._aload_0();
        mb._getfield(thisClassName, field.getName(), type);
        switch(type.charAt(0)) {
        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I': mb._ireturn(); break;
        case 'J': mb._lreturn(); break;
        case 'F': mb._freturn(); break;
        case 'D': mb._dreturn(); break;
        default:  mb._areturn(); break;
        }
        cfw.addMethod(Modifier.PUBLIC, (String)dk.o2, (String)dk.o1, null, mb.getCode());
      }
      dk.o1 = "set"+capName;
      dk.o2 = "(" + type + ")V";
      if (!regMethods.contains(dk)) { // create setXXX() method
        mb = new MethodBody((String)dk.o1, cfw, sz+1, sz);
        mb._aload_0();
        switch(type.charAt(0)) {
        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I': mb._iload(1); break;
        case 'J': mb._lload(1); break;
        case 'F': mb._fload(1); break;
        case 'D': mb._dload(1); break;
        default:  mb._aload(1); break;
        }
        mb._putfield(thisClassName, field.getName(), type);
        mb._return();
        cfw.addMethod(Modifier.PUBLIC, (String)dk.o2, (String)dk.o1, null, mb.getCode());
      }
    } catch(BadClassFormatException e) {} // not happening.
  }

  // returns Object[]{ Class, Class[] }: ret[0] = new class, ret[1] = parent class/interfaces.
  private Object[] createClass(HashSet superCalls, HashSet javaxCalls) throws Exception
  {
    Object[] ret = { null, interfaces };
    Class[] params;
    DoubleKey dk;
    Method m;
    Class c;
    int i, j;

    // Collect all methods
    HashSet regMethods = new HashSet(); // regular methods.
    HashSet absMethods = new HashSet(); // abstract methods.
    getAccessibleParentMethods(interfaces, regMethods, absMethods);

    // Create the class file object and do the work!
    ClassFileWriter cfw = new ClassFileWriter(Modifier.PUBLIC,thisClassName,superClass.getName(),interfaces);

    //----------------------------------------------------------------------------------------
    // Design of the produced class file:
    //
    // 0. Create a _setJavax(JavaExtension jx) method to store the reference to JavaExtension.
    // 1. Add user fields.
    // 2. For all constructors, duplicate them with public access.
    // 3. For each protected field, create a get/set() if not no such methods exist.
    // 4. For any method XXX() in regMethods,
    //    a) Create a public super_XXX() stub for called ones.
    //    b) If protected and does not exist in userMethods, create a public stub.
    // 5. For any in absmethods, if not exist in userMethods, create an empty body.
    // 6. For any in userMethods, create the proxy.
    //----------------------------------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////
    // 0. Create a _setJavax(JavaExtension) method to store the reference to its
    //    extension class.

    cfw.addField(Modifier.PRIVATE, "Lcom/judoscript/bio/JavaExtension;", "_javax", null);
    MethodBody mb = new MethodBody("_setJavax",cfw,2,2);
    mb._aload_0();
    mb._aload_1();
    mb._putfield(thisClassName,"_javax","Lcom/judoscript/bio/JavaExtension;");
    mb._return();
    cfw.addMethod(Modifier.PUBLIC,"(Lcom/judoscript/bio/JavaExtension;)V","_setJavax",null,mb.getCode());

    ////////////////////////////////////////////////////////////////////////////
    // 1. Add user fields.

    Iterator keys = fields.keySet().iterator();
    while (keys.hasNext()) {
      String k = (String)keys.next();
      cfw.addField((String)fields.get(k), k);
    }

    ////////////////////////////////////////////////////////////////////////////
    // 2. For all constructors, duplicate them with public access.

    String superClassName = superClass.getName(); 
    Constructor[] ctors = superClass.getDeclaredConstructors();
    for (i=ctors.length-1; i>=0; --i) {
      Constructor ctor = ctors[i];
      dupMethod(cfw, superClassName, "<init>", "<init>", Void.TYPE, 
                ctor.getParameterTypes(), ctor.getExceptionTypes());
    }

    ////////////////////////////////////////////////////////////////////////////
    // 3. For each protected field, create a get/set() if not no such methods exist.

    if (superClass != null) {
      Field[] fields = superClass.getFields();
      for (i=0; i<fields.length; ++i) {
        if (Modifier.isProtected(fields[i].getModifiers()) && javaxCalls.contains(fields[i].getName()))
          createFieldAccess(cfw, fields[i], regMethods);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    // 4. For any method XXX() in regMethods,
    //    a) Create a public super_XXX() stub for called ones.
    //    b) If protected and does not exist in userMethods, create a public stub.

    Iterator iter = regMethods.iterator();
    while (iter.hasNext()) {
      dk = (DoubleKey)iter.next();
      m = (Method)dk.value;
      String mn = m.getName();
      if (superCalls.contains(mn))
        dupMethod(cfw, superClassName, mn, "super_"+mn, m.getReturnType(),
                  m.getParameterTypes(), m.getExceptionTypes());
      if (Modifier.isProtected(m.getModifiers()) && !userMethods.contains(dk) && javaxCalls.contains(mn))
        dupMethod(cfw, superClassName, mn, mn, m.getReturnType(),
                  m.getParameterTypes(), m.getExceptionTypes());
    }

    ////////////////////////////////////////////////////////////////////////////
    // 5. For any in absmethods, if not exist in userMethods, create an empty body.

    iter = absMethods.iterator();
    while (iter.hasNext()) {
      dk = (DoubleKey)iter.next();
      if (userMethods.contains(dk)) continue;
      m = (Method)dk.value;
      c = m.getReturnType();
      params = m.getParameterTypes();
      int[] szs = ClassFileUtil.getInvokeStackSizes(c, params);
      int sz = Math.max(szs[0],szs[1])+1;
      mb = new MethodBody(m.getName(), cfw, Math.max(szs[0],szs[1])+1, szs[0]+1);
      if (c.equals(Void.TYPE))        { mb._return(); }
      else if (!c.isPrimitive())      { mb._aconst_null(); mb._areturn(); }
      else if (c.equals(Float.TYPE))  { mb._fconst_0(); mb._freturn(); }
      else if (c.equals(Double.TYPE)) { mb._dconst_0(); mb._dreturn(); }
      else if (c.equals(Long.TYPE))   { mb._lconst_0(); mb._lreturn(); }
      else                            { mb._iconst_0(); mb._ireturn(); }
      cfw.addMethod(Modifier.PUBLIC, c, params, m.getName(), m.getExceptionTypes(), mb.getCode());
    }

    ////////////////////////////////////////////////////////////////////////////
    // 6. For any in userMethods, create the proxy.

    iter = userMethods.iterator();
    while (iter.hasNext()) {
      dk = (DoubleKey)iter.next();
      createMethodProxy( cfw, (String)dk.o1, (String)dk.o2, ((Function)dk.value).getName() );
    }

    //cfw.write(thisClassName + ".class");
    try {
      ret[0] = cfw.toClass();
    } catch(LinkageError le) {
      System.err.println("Warning: Java extension class "+cfw.className+" not loaded: "+le.getMessage());
    }
    return ret;
  }

} // end of class JavaExtensionHelper.
