import org.apache.tools.ant.*;

public class AntTest
{
  public static void main(String args[]) {
    try {
      Project proj = new Project();
      proj.init();
      proj.addBuildListener(new DefaultLogger());

      Task task = proj.createTask("taskdef");
      IntrospectionHelper helper = IntrospectionHelper.getHelper(task.getClass());
  
      helper.setAttribute(proj, task, "name", "judo");
      helper.setAttribute(proj, task, "classname", "com.judoscript.AntJudoScriptTask");

      task.execute();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
