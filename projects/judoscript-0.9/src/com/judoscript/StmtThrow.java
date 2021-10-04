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

import com.judoscript.bio.*;
import com.judoscript.util.XMLWriter;


public class StmtThrow extends StmtBase
{
  Expr excpt;

  public StmtThrow(int line, Expr excpt) {
    super(line);
    this.excpt = excpt;
  }

  public void exec() throws Throwable {
    Variable ex = (excpt==null) ? ValueSpecial.NIL : excpt.eval();

    String msg = null;
    ExceptionRuntime rte = null;

    if (ex instanceof ExcptError) {
      throw ((ExcptError)ex).getException();
    } else if (ex instanceof JavaObject) {
      try {
        Object o = ((JavaObject)ex).getObjectValue();
        if (o instanceof Exception)
          rte = new ExceptionRuntime(RTERR_JAVA_EXCEPTION, null, (Exception)o);
        msg = o.toString();
      } catch(ClassCastException cce) {}
    } else {
      msg = ex.toString();
    }

    if (rte == null)
      rte = new ExceptionRuntime(RTERR_USER_EXCEPTION, msg, null);

    throw rte;
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtThrow");
    if (excpt != null) excpt.dump(out);
    out.endTagLn();
  }

} // end of class StmtThrow.
