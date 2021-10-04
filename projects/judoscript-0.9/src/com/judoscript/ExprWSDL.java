/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-02-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.IOException;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.wsdl.Binding;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.apache.axis.Constants;
import org.apache.axis.encoding.ser.*;
import org.apache.axis.encoding.SerializerFactory;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.Target;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.wsdl.symbolTable.BaseType;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymTabEntry;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.SchemaUtils;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.fromJava.Types;
import org.apache.axis.message.SOAPHandler;

import com.judoscript.bio.UserDefined;
import com.judoscript.bio._Array;
import com.judoscript.util.Lib;
import com.judoscript.util.XMLWriter;


public class ExprWSDL extends ExprSingleBase
{
  Expr serviceName = null;

  public ExprWSDL(Expr wsdlurl) { super(wsdlurl); }

  public void setServiceName(Expr expr) {
    serviceName = expr;
  }

  public int getType() { return TYPE_WS; }

  public Variable eval() throws Throwable {
    try {
      RT.getClass("org.apache.axis.wsdl.symbolTable.SymbolTable");
    } catch(/*ClassNotFound*/Exception e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS,
        "WSDL invocation depends on Apache Axis 1.x package.\n" +
        "Make sure the Axis libraries are in the classpath.");
    }
    return _eval();
  }

  private Variable _eval() throws Throwable {
    String wsdl_url = expr.getStringValue();
    QName sqn = (serviceName != null) ? toQName(serviceName.getStringValue()) : null;
    return new WSDLService(expr.getStringValue(), sqn);
  }

  /**
   * @param s can take the form of "{uri}name", where uri part is optional.
   *          This form is what QName.toString() returns.
   */
  public static QName toQName(String s) {
    String uri = null;
    s = s.trim();
    if (s.charAt(0) == '{') {
      int idx = s.indexOf('}');
      if (idx > 0) {
        uri = s.substring(1, idx);
        s = s.substring(idx+1);
      }
    }
    return new QName(uri, s);
  }

  public void dump(XMLWriter out) {
    // TODO: dump().
  }

} // end of class ExprWSDL.


//
// The WSDL service value object.
//
class WSDLService extends ObjectInstance
{
  WSDLDynamicInvoker wsdldi;
  QName serviceQName;

  public WSDLService(String wsdlurl, QName serviceQName) throws Exception {
    wsdldi = getWSDL(wsdlurl);
    this.serviceQName = serviceQName;
  }

  public int getType() { return TYPE_WS; }
  public String getTypeName() { return "WSDL"; }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    return invoke(null, fxn, params, javaTypes);
  }

  public Variable invoke(String portName, String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    return JudoUtil.toVariable(wsdldi.invokeMethod(serviceQName, portName, fxn, params));
  }

  public Variable resolveVariable(String portName) {
    return new WSDLPort(this, portName);
  }

  ////////////////////////////////////////////////////////////
  // WSDL Cache
  // 
  private static final HashMap wsdlCache = new HashMap(); // URL => WSDLService

  public static WSDLDynamicInvoker getWSDL(String wsdlurl) throws Exception {
    WSDLDynamicInvoker wsdldi = (WSDLDynamicInvoker)wsdlCache.get(wsdlurl);
    if (wsdldi == null) {
      wsdldi = new WSDLDynamicInvoker(wsdlurl);
      wsdlCache.put(wsdlurl, wsdldi);
    }
    return wsdldi;
  }

} // end of class WSDLService.


//
// The WSDL port value object.
//
class WSDLPort extends ObjectInstance
{
  WSDLService wsdlsvc;
  String      portName;

  WSDLPort(WSDLService s, String n) { wsdlsvc = s; portName = n; }

  public int getType() { return TYPE_WS; }
  public String getTypeName() { return "WSDLPort"; }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    return wsdlsvc.invoke(portName, fxn, params, javaTypes);
  }
}


//
// Use Axis for completely dynamic, stubless invocations.
// Supports both doc/lit and rpc/encoded services.
//
class WSDLDynamicInvoker
{
  Parser wsdlParser = null;
  TypeMapping typeMap = null;

  WSDLDynamicInvoker(String wsdlURL) throws Exception {
    wsdlParser = (Parser)RT.getClass("org.apache.axis.wsdl.gen.Parser").newInstance();
    wsdlParser.run(wsdlURL);
  }

  public Object invokeMethod(QName serviceQName, String portName, String opName, Expr[] args)
    throws Throwable
  {
    //String operationQName = null;

    Service service = selectService(serviceQName);
    Operation operation = null;
    org.apache.axis.client.Service dpf = new org.apache.axis.client.Service(wsdlParser, service.getQName());

    ArrayList inputs = new ArrayList();
    Port port = selectPort(service.getPorts(), portName);
    if (portName == null)
      portName = port.getName();
    Binding binding = port.getBinding();
    Call call = dpf.createCall(QName.valueOf(portName), QName.valueOf(opName));
    ((org.apache.axis.client.Call)call).setTimeout(new Integer(15*1000));
    typeMap = ((org.apache.axis.client.Call)call).getTypeMapping();

    // Output types and names
    ArrayList outNames = new ArrayList();

    // Input types and names
    ArrayList inNames = new ArrayList();
    ArrayList inTypes = new ArrayList();
    SymbolTable symbolTable = wsdlParser.getSymbolTable();
    BindingEntry bEntry = symbolTable.getBindingEntry(binding.getQName());
    Parameters parameters = null;
    Iterator i = bEntry.getParameters().keySet().iterator();

    while (i.hasNext()) {
      Operation o = (Operation) i.next();
      if (o.getName().equals(opName)) {
        operation = o;
        parameters = (Parameters)bEntry.getParameters().get(o);
        break;
      }
    }
    if ((operation == null) || (parameters == null)) {
      ExceptionRuntime.rte(Consts.RTERR_ILLEGAL_ARGUMENTS, opName + " was not found.");
    }

    // loop over paramters and set up in/out params
    for (int j = 0; j < parameters.list.size(); ++j) {
      Parameter p = (Parameter) parameters.list.get(j);

      if (p.getMode() == 1) {         // IN
        inNames.add(p.getQName().getLocalPart());
        inTypes.add(p);
      } else if (p.getMode() == 2) {  // OUT
        outNames.add(p.getQName());

        checkSerializationFactories(p.getType()); // De/serializerFactories
      } else if (p.getMode() == 3) {  // INOUT
        inNames.add(p.getQName().getLocalPart());
        inTypes.add(p);
        outNames.add(p.getQName());
      }
    }

    // set output type
    if (parameters.returnParam != null) {
      checkSerializationFactories(parameters.returnParam.getType()); // De/serializerFactories

      QName returnQName = parameters.returnParam.getQName();
      outNames.add(returnQName);
    }

    if (inNames.size() != args.length)
      ExceptionRuntime.rte(Consts.RTERR_ILLEGAL_ARGUMENTS, "Need "+inNames.size()+" arguments!!!");

    for (int pos = 0; pos < inNames.size(); ++pos)
      inputs.add(getValue(((Parameter)inTypes.get(pos)).getType(), args[pos]));

    Object ret = call.invoke(inputs.toArray());
    Map outputs = call.getOutputParams();

    if ((outputs == null) || (outputs.size() <= 0))
      return ret;

    HashMap map = new HashMap();
    for (int pos=0; pos<outNames.size(); ++pos) {
      QName name = (QName)outNames.get(pos);
      Object value = outputs.get(name);
      if (value != null)
        map.put(name.getLocalPart(), value);
      else if (pos == 0)
        map.put(name.getLocalPart(), ret);
    }
    return map;
  }

  public Object getValue(TypeEntry type, Expr arg) throws Throwable {
    Object o;
    Variable v;
    QName paramType = org.apache.axis.wsdl.toJava.Utils.getXSIType(type);

    String dims = type.getDimensions();

    if ( (dims != null) && dims.endsWith("[]") ) { // is array
      registerArrayFactories(type); // This is not necessary for, say, ArrayOfstring;
                                    // but it shouldn't hurt, and may be(?) necessary
                                    // for arrays of custom types.
      v = arg.eval();

      if (v instanceof _Array) {
        _Array arr = (_Array)v;
        TypeEntry refType = type.getRefType();
        if (refType == null)
          return arr.toObjectArray();

        Object[] oa = new Object[arr.size()];
        for (int i=0; i<oa.length; ++i)
          oa[i] = getValue(refType, arr.resolve(i));

        return oa;
      }

      o = v.getObjectValue();
      if (o instanceof List || o.getClass().isArray())
        return o;

      return new Object[]{ o }; // make an array?
    }
    else if (type instanceof BaseType && ((BaseType) type).isBaseType()) {
      DeserializerFactory factory = (DeserializerFactory)typeMap.getDeserializer(paramType);
      Deserializer deserializer = (Deserializer)factory.getDeserializerAs(Constants.AXIS_SAX);

      if (deserializer instanceof DateDeserializer)
        return arg.getDateValue();

      else if (deserializer instanceof CalendarDeserializer) {
        o = arg.getObjectValue();
        if (o instanceof Calendar)
          return o;
        if (o instanceof Date) {
          Calendar cal = new GregorianCalendar();
          cal.setTime((Date)o);
          return cal;
        }
      }

      else if (deserializer instanceof QNameDeserializer) {
        o = arg.getObjectValue();
        return (o instanceof QName) ? o : ExprWSDL.toQName(o.toString());
      }

      else if (deserializer instanceof SimpleDeserializer)
        return ((SimpleDeserializer)deserializer).makeValue(arg.getStringValue());
      
      else if (deserializer instanceof Base64Deserializer ||
               deserializer instanceof HexDeserializer)
      {
        v = arg.eval();
        if (v instanceof _Array)
          return ((_Array)v).toByteArray();
        o = v.getObjectValue();
        if (o instanceof byte[])
          return o;
        if (o instanceof Byte[]) {
          Byte[] Ba = (Byte[])o;
          byte[] ba = new byte[Ba.length];
          for (int i=0; i<Ba.length; ++i)
            ba[i] = Ba[i].byteValue();
          return ba;
        }
      }

      else if (deserializer instanceof ArrayDeserializer ||
               deserializer instanceof VectorDeserializer)
      {
        v = arg.eval();
        if (v instanceof _Array)
          return ((_Array)v).toObjectArray();

        o = v.getObjectValue();
        if (o instanceof List ||
            o.getClass().isArray() ||
            o instanceof Iterator ||
            o instanceof Enumeration)
          return o;
      }

      else if (deserializer instanceof MapDeserializer) {
        v = arg.eval();
        if (v instanceof UserDefined)
          return ((UserDefined)v).toMap();

        o = v.getObjectValue();
        if (o instanceof Map)
          return o;
      }

    }
    else { // not array nor know types
      registerObjectFactories(type); // make sure Serializer/Deserializer are set.

      SymbolTable symTab = wsdlParser.getSymbolTable();

      if (type instanceof DefinedType) {
        TypeEntry extBase = ((DefinedType)type).getComplexTypeExtensionBase(symTab);
        if (extBase != null)
          return getValue(extBase, arg);
      }

      return arg.eval(); // ObjectInstance is expected by our Serializer/Deserializer.
    }

    ExceptionRuntime.rte(Consts.RTERR_ILLEGAL_ARGUMENTS,
                         "Don't know how to convert '" + arg + "' to " + type.getQName() + ".");
    return null;
  }

  public void checkSerializationFactories(TypeEntry type) throws Throwable {
    if (type instanceof BaseType) {
      if (((BaseType)type).isBaseType())
        return;
      if (type.getQName().equals(new QName("http://xml.apache.org/xml-soap", "Map"))) {
        registerObjectFactories(type);
        return;
      }
    }

    SymbolTable symTab = wsdlParser.getSymbolTable();
    String dims = type.getDimensions();

    if ( (dims != null) && dims.endsWith("[]") ) { // is array
      registerArrayFactories(type);
      return;
    }

    // not array nor know types
    if (type instanceof DefinedType) {
      TypeEntry extBase = ((DefinedType)type).getComplexTypeExtensionBase(symTab);
      if (extBase != null) {
        checkSerializationFactories(extBase);
        return;
      }
    }

    // Make sure Serializer/Deserializer are set for the type.
    registerObjectFactories(type);
  }

  public void registerArrayFactories(TypeEntry type) throws Throwable {
    if (typeMap.isRegistered(Object[].class, type.getQName()))
      return;

    typeMap.register(Object[].class, type.getQName(),
                     new ArraySerializerFactory(), new ArrayDeserializerFactory());
    type = type.getRefType();
    if (type != null)
      checkSerializationFactories(type);
  }

  public void registerObjectFactories(TypeEntry type) throws Throwable {
    if (typeMap.isRegistered(ObjectInstance.class, type.getQName()))
      return;

    SymbolTable symTab = wsdlParser.getSymbolTable();
    List elemDecls = SchemaUtils.getContainedElementDeclarations(type.getNode(), symTab);
    boolean isMap = type.getQName().equals(new QName("http://xml.apache.org/xml-soap", "Map"));
    ObjectSerializationFactories factories = new ObjectSerializationFactories(this, elemDecls, isMap);
    typeMap.register(ObjectInstance.class, type.getQName(), factories, factories);

    for (int i=0; i<elemDecls.size(); ++i) {
      ElementDecl ed = (ElementDecl)elemDecls.get(i);
      checkSerializationFactories(ed.getType());
    }
  }

  // Helper for WSDL to pick service
  private Service selectService(QName serviceQName) throws Exception {
    ServiceEntry serviceEntry = (ServiceEntry)getSymTabEntry(serviceQName, ServiceEntry.class);
    return serviceEntry.getService();
  }

  // Helper for WSDL to pick service
  private Service selectService(String serviceNS, String serviceName) throws Exception {
    QName serviceQName = (serviceNS != null) && (serviceName != null)
                         ? new QName(serviceNS, serviceName) : null;
    return selectService(serviceQName);
  }

  // Helper for WSDL to get symbol table entry
  private SymTabEntry getSymTabEntry(QName qname, Class cls) {
    HashMap map = wsdlParser.getSymbolTable().getHashMap();
    Iterator iterator = map.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      QName key = (QName) entry.getKey();
      List v = (List)entry.getValue();

      if ((qname == null) || qname.equals(key)) {
        for (int i=0; i<v.size(); ++i) {
          SymTabEntry symTabEntry = (SymTabEntry)v.get(i);
          if (cls.isInstance(symTabEntry))
            return symTabEntry;
        }
      }
    }
    return null;
  }

  // Helper for WSDL to pick port
  private Port selectPort(Map ports, String portName) throws Exception {
    Iterator valueIterator = ports.keySet().iterator();
    while (valueIterator.hasNext()) {
      String name = (String) valueIterator.next();

      if ((portName == null) || (portName.length() == 0)) {
        Port port = (Port) ports.get(name);
        List list = port.getExtensibilityElements();

        for (int i = 0; (list != null) && (i < list.size()); i++) {
          Object obj = list.get(i);
          if (obj instanceof SOAPAddress)
            return port;
        }
      } else if ((name != null) && name.equals(portName)) {
        return (Port) ports.get(name);
      }
    }
    return null;
  }

} // end of class WSDLDynamicInvoker.


//
// SerializerFactory and DeserializerFactory for
// ObjectDe/serializer and MapDe/serializer.
//
class ObjectSerializationFactories implements DeserializerFactory, SerializerFactory
{
  private HashMap fields; // of localName => ElementDecl
  private WSDLDynamicInvoker dynaInvoker;
  private boolean forMap;

  ObjectSerializationFactories(WSDLDynamicInvoker invoker, List elements, boolean forMap) {
    this.dynaInvoker = invoker;
    this.forMap = forMap;

    fields = new HashMap();
    for (int i=elements.size()-1; i>=0; --i) {
      ElementDecl ed = (ElementDecl)elements.get(i);
      fields.put(ed.getName().getLocalPart(), ed);
    }
  }

  public javax.xml.rpc.encoding.Serializer getSerializerAs(String mechanismType) {
    return new ObjectSerializer(dynaInvoker, fields, forMap);
  }

  public javax.xml.rpc.encoding.Deserializer getDeserializerAs(String mechanismType) {
    return new ObjectDeserializer(fields, dynaInvoker.wsdlParser.getSymbolTable(), forMap);
  }

  public Iterator getSupportedMechanismTypes() {
    ArrayList mechanisms = new ArrayList();
    mechanisms.add(Constants.AXIS_SAX);
    return mechanisms.iterator();
  }

} // end of class ObjectSerializationFactories.


//
// ObjectDeserializer
//
class ObjectDeserializer extends DeserializerImpl
{
  private HashMap fields; // of localName => ElementDecl
  private SymbolTable symTab;
  private boolean forMap;

  public ObjectDeserializer(HashMap fields, SymbolTable symTab, boolean forMap) {
    this.fields = fields;
    this.symTab = symTab;
    this.forMap = forMap;
  }

  public void startElement(String namespace,
                           String localName,
                           String qName,
                           Attributes attributes,
                           DeserializationContext context)
    throws SAXException
  {
    value = new UserDefined();
    super.startElement(namespace, localName, qName, attributes, context);
  }

  // This method is invoked when an element start tag is encountered.
  // @param namespace is the namespace of the element
  // @param localName is the name of the element
  // @param prefix is the element's prefix
  // @param attributes are the attributes on the element...used to get the type
  // @param context is the DeserializationContext
  public SOAPHandler onStartChild(String namespace,
                                  String localName,
                                  String prefix,
                                  Attributes attributes,
                                  DeserializationContext context)
    throws SAXException
  {
    ElementDecl ed = (ElementDecl)fields.get(localName);
    if (ed == null)
      throw new SAXException("Invalid element in Data struct - " + localName);

    TypeEntry type = ed.getType();
    QName typeQName = type.getQName();
    Deserializer dSer = context.getDeserializerForType(typeQName);
    if (dSer == null) {
      if (type instanceof DefinedType) {
        TypeEntry extBase = ((DefinedType)type).getComplexTypeExtensionBase(symTab);
        if (extBase != null)
          dSer = context.getDeserializerForType(extBase.getQName());
      }
    }
    if (dSer == null)
      throw new SAXException("No deserializer for a " + typeQName + "???");

    dSer.registerValueTarget(new ObjectTarget((ObjectInstance)value, localName));

    return (SOAPHandler)dSer;
  }

} // end of class ObjectDeserializer.


//
// ObjectSerializer
//
class ObjectSerializer implements Serializer
{
  private HashMap fields; // of localName => ElementDecl
  private WSDLDynamicInvoker dynaInvoker;
  private boolean forMap;

  ObjectSerializer(WSDLDynamicInvoker invoker, HashMap fields, boolean forMap) {
    this.fields = fields;
    this.dynaInvoker = invoker;
    this.forMap = forMap;
  }

  // Serialize an element named name, with the indicated attributes
  // and value.
  // @param name is the element name
  // @param attributes are the attributes...serialize is free to add more.
  // @param value is the value
  // @param context is the SerializationContext
  public void serialize(QName name, Attributes attributes,
                        Object value, SerializationContext context)
    throws IOException
  {
    ObjectInstance var = (ObjectInstance)value;
    context.startElement(name, attributes);

    try {
      Iterator iter = fields.values().iterator();
      while (iter.hasNext()) {
        ElementDecl ed = (ElementDecl)iter.next();
        QName fldName = ed.getName();
        Variable val = ((ObjectInstance)value).resolveVariable(fldName.getLocalPart());
        context.serialize(fldName, null, dynaInvoker.getValue(ed.getType(), val));
      }
    } catch(Throwable t) {
      throw new IOException(Lib.getExceptionMsg(null, t));
    }

    context.endElement();
  }

  public String getMechanismType() { return Constants.AXIS_SAX; }

  public Element writeSchema(Class javaType, Types types) throws Exception {
    return null;
  }

} // end of class ObjectSerializer.


//
// ObjectTarget
//
class ObjectTarget implements Target
{
  private ObjectInstance var;
  private String localName;

  ObjectTarget(ObjectInstance var, String localName) {
    this.var = var;
    this.localName = localName;
  }

  public void set(Object value) throws SAXException {
    try {
      var.setVariable(localName, JudoUtil.toVariable(value), 0);
    } catch(Throwable t) {
      RT.logger.error("Failed to set a value for a SOAP call.", t);
      throw new SAXException(Lib.getExceptionMsg(null, t));
    }
  }
}
