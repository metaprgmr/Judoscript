import java.awt.Frame;
import com.judoscript.JudoEngine;

public class CallTest
{
  public static void main (String[] args) throws Exception
  {
    // 1. Create a JudoScript engine
    JudoEngine je = new JudoEngine();

    try {

      // 2. Create a JudoScript function
      je.runCode("function foo a,b { return a+b; }");

      // 3. Call the function
      Object[] params = new Object[] { new Integer(1), new Double(2) };
      System.out.println( je.call("foo", params) );
      params[1] = new Integer(2);
      System.out.println( je.call("foo", params) );

    } catch(Exception e) { e.printStackTrace(); }
  }
}
