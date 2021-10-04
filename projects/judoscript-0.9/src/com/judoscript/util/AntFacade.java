/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 07-29-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.*;

/**
 * The study is to programmatically construct a Task and execute it.
 * 
 * <p>
 * The topics includes:
 * <ul>
 * <li> constructing the task, and 
 * <li> establish the runtime Project that runs the task in.
 * </ul>
 * 
 * The runtime Project includes these issues:
 * <ol>
 * <li> System.out and System.err redirected to another agent's.
 * <li> System properties shared with another agent.
 * <li> Exception and return values.
 * </ol> 
 */
public class AntFacade extends Project
{
  Task task = null;
  IntrospectionHelper helper = null;
  
  public AntFacade() { this(null,null); }

  public AntFacade(PrintWriter out, PrintWriter err) {
    super();
    super.init();

    BuildListener logger;
    if (out == null && err == null) {
      logger = new DefaultLogger();
      ((DefaultLogger)logger).setMessageOutputLevel(Project.MSG_INFO);
    } else {
      logger = new AntWriterLogger(out, err);
      ((AntWriterLogger)logger).setMessageOutputLevel(Project.MSG_INFO);
    }

    super.addBuildListener(logger);
  }
  
  private void checkTask() throws IllegalArgumentException { checkTask(null); }

  private void checkTask(String msg) throws IllegalArgumentException {
    if (task == null)
      throw new IllegalArgumentException(StringUtils.defaultString(msg, "Ant task is not set."));
  }

  /**
   * Creates a new task for this project.
   * The project never have more than one task.
   * @param name
   */
  public void newTask(String name) throws IllegalArgumentException {
    task = createTask(name);
    checkTask("Task " + name + " is not found.");
    helper = IntrospectionHelper.getHelper(task.getClass());
  }

  /**
   * Adds text to the task, if task is not null. 
   * @param text
   */
  public void taskAddText(String text) throws IllegalArgumentException {
    checkTask();
    helper.addText(this, task, text); 
  }

  /**
   * Sets the attribute for the task, if task is not null. 
   */
  public void taskSetAttribute(String name, String value) throws IllegalArgumentException {
    checkTask();
    helper.setAttribute(this, task, name, value);
  }

  /**
   * Creates an element for the named element in task,
   * if the task is not null.
   * @param name
   * @return the new element created.
   */
  public Object taskCreateElement(String name) throws IllegalArgumentException {
    checkTask();
    return helper.createElement(this, task, name);
  }

  /**
   * A helper method to set text for an embedded object.
   * @param o the object, typically returned by <code>taskCreateElement()</code>,
   *          to be add text to.
   * @param text
   * @see taskCreateElement(String)
   */
  public void objectAddText(Object o, String text) {
    IntrospectionHelper h = IntrospectionHelper.getHelper(o.getClass());
    h.addText(this, o, text);
  }

  /**
   * A helper method to set attribute for an embedded object.
   * @param o the object, typically returned by <code>taskCreateElement()</code>,
   *          to be set attribute to.
   * @param name
   * @param value
   */
  public void objectSetAttribute(Object o, String name, String value) {
    IntrospectionHelper h = IntrospectionHelper.getHelper(o.getClass());
    h.setAttribute(this, o, name, value);
  }

  /**
   * A helper method to create a child element for an embedded object.
   * @param o the object, typically returned by <code>taskCreateElement()</code>,
   *          to be created a child for.
   * @param name
   * @return
   */
  public Object objectCreateElement(Object o, String name) {
    IntrospectionHelper h = IntrospectionHelper.getHelper(o.getClass());
    return h.createElement(this, o, name);
  }

  /**
   * Executes the task, if the task is not null.
   */
  public void executeTask() throws Throwable {
    checkTask();
    setSystemProperties();

    try {
      task.execute();
    
      // write back to system properties
      Map props = getProperties();
      Map sysprops = System.getProperties();
      Iterator iter = props.keySet().iterator();
      while (iter.hasNext()) {
        Object k = iter.next();
        sysprops.put(k, props.get(k));
      }
    } catch(BuildException be) {
      throw be.getCause();
    }
  }

  /*
  public static void main(String args[]) {
    new AntFacade().study();
  }

  public void study() {
    newTask("echo");
    //taskAddText("Hello, ants!");
    taskSetAttribute("message", "Hello, more ants!");
    executeTask();
  }
  */

} // end of class AntFacade.
