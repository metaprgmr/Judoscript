/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 07-22-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript;

import java.util.Map;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.*;

/**
 * Can be aliased as
 * <code>org.apache.tools.ant.taskdefs.optional.JudoScript</code>.
 */
public class AntJudoScriptTask extends Task
{
  private String  code   = null;
  private String  script = null;
  private String  params = null;

  public AntJudoScriptTask() {
  }

  public void addText(String s) { code = s; }
  public void setSrc(String s) { script = s; }
  public void setParams(String s) { params = s; }

  public void execute() throws BuildException {
    try {
      JudoEngine engine = new JudoEngine();
      engine.putBean("$$anttask", JudoUtil.toVariable(this));

      // Write system properties into the project
      getProject().setSystemProperties();

      // Write the project properties into system properties,
      // as the embedded JudoScript code uses system properties.
      getProjectProperties();

      String[] args = JudoUtil.parseParams(params);
      if (StringUtils.isNotBlank(code))
        engine.runCode(code, args, null);
      else
        engine.runScript(script, args, null);

      // Write system properties into the project. The embedded
      // code may, likely, have modified system properties.
      getProject().setSystemProperties();

    } catch(Throwable e) {
      throw new BuildException(e);
    }
  }

  // Write the project properties into system properties
  void getProjectProperties() {
    Map props = getProject().getProperties();
    Map sysprops = System.getProperties();
    Iterator iter = props.keySet().iterator();
    while (iter.hasNext()) {
      Object k = iter.next();
      sysprops.put(k, props.get(k));
    }
  }

  public static void antcall(Expr[] params) throws Throwable {
    Task task = (Task)RT.resolveGlobalVariable("$$anttask").getObjectValue();
    Project proj = (Project)task.getProject();

    if (proj == null)
      ExceptionRuntime.rte(Consts.RTERR_ILLEGAL_ACCESS,
                           "The anttask() function can only be used within Ant build script.");
    try {
      int len = params.length;
      for (int i=0; i<len; ++i)
        proj.executeTarget(params[i].getStringValue());
    } catch(BuildException be) {
      throw be.getCause();
    }
  }

} // end of class AntJudoScriptTask.
