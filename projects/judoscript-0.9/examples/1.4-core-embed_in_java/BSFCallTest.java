import org.apache.bsf.*;
import org.apache.bsf.util.*;

public class BSFCallTest
{
  public static void main (String[] args) throws Exception
  {
    try {

      // 1. Register judoscript to BSF
      BSFManager.registerScriptingEngine(
        "judoscript", "com.judoscript.BSFJudoEngine", new String[]{"judo","jud"} );

      // 2. Create a BSF manager and obtain the engine
      BSFManager manager = new BSFManager();
      BSFEngine engine = manager.loadScriptingEngine("judoscript");

      // 3. Create a JudoScript function
      engine.exec(null, 0, 0, "function foo a,b { return a+b; }");

      // 4. Call the function
      Object[] params = new Object[] { new Integer(1), new Double(2) };
      System.out.println( engine.call(null, "foo", params) );
      params[1] = new Integer(2);
      System.out.println( engine.call(null, "foo", params) );

    } catch(Exception e) { e.printStackTrace(); }
  }
}
