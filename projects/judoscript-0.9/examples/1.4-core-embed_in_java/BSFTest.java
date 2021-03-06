import java.awt.Frame;
import java.io.FileReader;
import org.apache.bsf.*;
import org.apache.bsf.util.*;

public class BSFTest
{
  public static void main (String[] args) throws Exception
  {
    // 1. Register judoscript to BSF
    BSFManager.registerScriptingEngine(
      "judoscript", "com.judoscript.BSFJudoEngine", new String[]{"judo","jud"} );

    String fileName = "randgen.judo";

    // 2. Create a BSF manager
    BSFManager manager = new BSFManager();

    // 3. Create an object to pass on to the script
    Frame frame = new Frame();
    manager.declareBean("frame", frame, Frame.class);

    try {

      // 4. Invoke judoscript
      manager.exec(manager.getLangFromFilename(fileName), fileName, 0, 0, 
                   IOUtils.getStringFromReader(new FileReader(fileName)));

      // 5. Get back an object from judoscript
      String title = (String)manager.lookupBean("title");

      // 6. Show the frame
      frame.setTitle(title);
      frame.show();

    } catch(BSFException be) {
      System.out.println("exception: " + be.getMessage());
      Throwable oe= be.getTargetException();
      if(null != oe)
        System.out.println("\nOriginal Exception:"+ oe.getMessage());
      be.printStackTrace();
    } catch(Exception e) { 
      e.printStackTrace();
    }
  }
}
