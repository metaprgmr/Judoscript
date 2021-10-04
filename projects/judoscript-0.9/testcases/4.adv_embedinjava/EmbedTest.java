import java.awt.Frame;
import com.judoscript.JudoEngine;

public class EmbedTest
{
  public static void main (String[] args) throws Exception
  {
    // 1. Create a JudoScript engine
    JudoEngine je = new JudoEngine();

    try {

      // 2. Create an object to pass on to the script
      Frame frame = new Frame();
      je.putBean("frame", frame);

      // 3. Invoke judoscript
      je.runScript("randgen.judo");

      // 4. Get back an object from judoscript
      String title = (String)je.getBean("title");

      // 5. Show the frame
      frame.setTitle(title);
      frame.show();

    } catch(Exception e) { e.printStackTrace(); }
  }
}
