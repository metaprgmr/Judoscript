import java.lang.reflect.Modifier;
import com.judoscript.jamaica.*;

public class JCCTest extends ClassLoader
{
  public static final JCCTest classLoader = new JCCTest();

  private JCCTest() {}

  /**
   * This is the interface that any dynamica class will implement.
   */
  public static interface EventHandler {
    public void event(String e);
    public void event(int    e);
    public void event(int[]  e);
  }

  public static void main(String[] args) {
    try {
      // Create the dyncamic class
      Class cls = generateHandlerClass();

      // Create an instance of the dyncamic class and use it.
      EventHandler eh = (EventHandler)cls.newInstance();
      eh.event("Cool!");
      eh.event(5);
      eh.event(new int[] { 3, 6, 9 });
      eh.event((int[])null);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  static Class generateHandlerClass() throws JavaClassCreatorException {
    String[] paramNames = new String[] { "e" }; // because they are all the same.

    JavaClassCreator jcc = JavaClassCreator.getJavaClassCreator();
    jcc.startClass(Modifier.PUBLIC, "DynaHandler", null, new String[]{ "JCCTest$EventHandler" });

    jcc.addDefaultConstructor(Modifier.PUBLIC);

    // Create method:
    //   public void event(String e) {
    //     %println "String event: ", e
    //   }
    jcc.startMethod(Modifier.PUBLIC, "event", new String[]{ "java.lang.String" }, paramNames, "void", null);
    jcc. macroPrint("println", null, new Object[]{ "String event: ", jcc.createVarAccess("e") });
    jcc.endMethod();

    // Create method:
    //   public void event(int e) {
    //     %println "int event: ", e
    //   }
    jcc.startMethod(Modifier.PUBLIC, "event", new String[]{ "int" }, paramNames, "void", null);
    jcc. macroPrint("println", null, new Object[]{ "int event: ", jcc.createVarAccess("e") });
    jcc.endMethod();

    // Create method:
    //   public void event(int[] e) {
    //     int i = 0;
    //     %if e != null
    //        aload e
    //        arraylength
    //        istore i
    //     %end_if
    // 
    //     %println "Array event: length=", i
    // 
    //     %if i > 0
    //       %array_iterate e i
    //         %println "e[", i, "]=", e[i]
    //       %end_array_iterate
    //     %end_if
    //  }
    jcc.startMethod(Modifier.PUBLIC, "event", new String[]{ "int[]" }, paramNames, "void", null);
    jcc. addLocalVariable("i", "int", new Integer(0));
    jcc. macroIf("!=", jcc.createVarAccess("e"), null, "?if_1", false);
    jcc.   inst_aload("e");
    jcc.   inst_arraylength();
    jcc.   inst_istore("i");
    jcc. macroEndIf("?if_1");
    jcc. macroPrint("println", null, new Object[]{ "Array event: length=", jcc.createVarAccess("i") });
    jcc. macroIf(">", new JavaClassCreator.VarAccess("i"), new Integer(0), "?if_2", false);
    jcc.  macroArrayIterate(jcc.createVarAccess("e"), "i", "?iter_1");
    jcc.   macroPrint("println", null, new Object[]{
             " e[", jcc.createVarAccess("i"), "]=", jcc.createArrayAccess("e", jcc.createVarAccess("i"))
           });
    jcc.  macroEndArrayIterate("i", "?iter_1");
    jcc. macroEndIf("?if_2");
    jcc.endMethod();

    byte[] bytes = jcc.endClass(); 

    return classLoader.defineClass("DynaHandler", bytes, 0, bytes.length);
  }
}
