/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.Markup;
import com.judoscript.util.AntFacade;
import com.judoscript.util.XMLWriter;


public class StmtAntTask extends StmtBase
{
  Markup markup;

  public StmtAntTask(int line, Markup markup) {
    super(line);
    this.markup = markup;
  }

  public void exec() throws Throwable {
    AntFacade ant = (AntFacade)RT.getAntFacade();

    ant.newTask(markup.getName());

    int i;
    int len = markup.numAttrs();
    for (i=0; i<len; ++i)
      ant.taskSetAttribute(markup.getAttrName(i), markup.getAttrValue(i));

    len = markup.numChildren();
    for (i=0; i<len; ++i) {
      Markup child = markup.getChild(i);
      if (child.isText())
        ant.taskAddText(child.getText());
      else {
        Object elem = ant.taskCreateElement(child.getName());
        for (int j=0; j<child.numAttrs(); ++j)
          ant.objectSetAttribute(elem, child.getAttrName(j), child.getAttrValue(j));   
        processChildElement(ant, elem, child);
      }
    }

    ant.executeTask();
  }

  protected static void processChildElement(AntFacade ant, Object elem, Markup markup) {
    for (int i=0; i<markup.numChildren(); ++i) {
      Markup mu = markup.getChild(i);
      if (mu.isText())
        ant.objectAddText(elem, mu.getText());
      else {
        Object el = ant.objectCreateElement(elem, mu.getName());
        for (int j=0; j<mu.numAttrs(); ++j)
          ant.objectSetAttribute(elem, mu.getAttrName(j), mu.getAttrValue(j));
        processChildElement(ant, el, mu);
      }
    }
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtAntTask");
  }

} // end of class StmtAntTask
