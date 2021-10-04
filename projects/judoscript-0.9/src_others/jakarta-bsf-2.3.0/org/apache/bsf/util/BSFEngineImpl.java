/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf.util;

import java.rmi.RemoteException;
import java.util.*;
import java.io.*;
import java.beans.*;
import org.apache.bsf.*;

/**
 * This is a base implementation of the BSFEngine interface which
 * engine implementations may choose to extend to get the basic 
 * methods of the interface implemented. 
 * <p>
 *
 * @author   Sanjiva Weerawarana
 * @author   Olivier Gruber (adding debugging support)
 */

public abstract class BSFEngineImpl implements BSFEngine {

    protected BSFManager mgr; // my manager
    protected BSFDebugManagerImpl dbgmgr; // the global debug manager.
    protected String lang; // my language string
    protected Vector declaredBeans; // BSFDeclaredBeans
    protected String classPath;
    protected String tempDir;
    protected ClassLoader classLoader;

    /**
     * By default, an engine does not support debugging.
     * Subclasses will need to redefine this method in order
     * to provide debuggers with their language-specific
     * interface for debugging.
     */
    public Object getSpecificDebuggingInterface() {
        return null;
    }

    /**
     * Basic engines are not supporting breakpoints for the meaning 
     * and support is something that is language-specific.
     */
    public void disconnectedDebuggerNotify() {
    }

    public void placeBreakpointAtLine(int brkptid, String docname,
                                      int lineno) throws BSFException {
        throw new BSFException (BSFException.REASON_UNSUPPORTED_FEATURE,
                                "BSF:" + lang + 
                                "engine does not yet support debugging.");
    }

    public void placeBreakpointAtOffset(int brkptid, String docname,
                                        int offset) throws BSFException {
        throw new BSFException (BSFException.REASON_UNSUPPORTED_FEATURE,
                                "BSF:" + lang +
                                "engine does not yet support debugging.");
    }

    public void removeBreakpoint(String docname, int brkptid)
        throws BSFException {
        throw new BSFException (BSFException.REASON_UNSUPPORTED_FEATURE,
                                "BSF:" + lang +
                                "engine does not yet support debugging.");
    }

    public void setEntryExit(String docname, boolean on)
        throws BSFException {
        throw new BSFException (BSFException.REASON_UNSUPPORTED_FEATURE,
                                "BSF:" + lang +
                                "engine does not yet support debugging.");
    }
    
    /**
     * Default impl of apply - calls eval ignorning parameters and returns
     * the result.
     */
    public Object apply(String source, int lineNo, int columnNo, 
                        Object funcBody, Vector paramNames, Vector arguments)
        throws BSFException {
        return eval(source, lineNo, columnNo, funcBody);
    }

    /**
     * Default impl of compileApply - calls compileExpr ignorning parameters.
     */
    public void compileApply(String source, int lineNo, int columnNo,
                             Object funcBody, Vector paramNames, 
                             Vector arguments, CodeBuffer cb)
        throws BSFException {
        compileExpr(source, lineNo, columnNo, funcBody, cb);
    }

    /**
     * Default impl of compileExpr - generates code that'll create a new
     * manager, evaluate the expression, and return the value.
     */
    public void compileExpr(String source, int lineNo, int columnNo,
                            Object expr, CodeBuffer cb) throws BSFException {
        ObjInfo bsfInfo = cb.getSymbol("bsf");
        
        if (bsfInfo == null) {
            bsfInfo = new ObjInfo(BSFManager.class, "bsf");
            cb.addFieldDeclaration("org.apache.bsf.BSFManager bsf = " + 
                                   "new org.apache.bsf.BSFManager();");
            cb.putSymbol("bsf", bsfInfo);
        }

        String evalString = bsfInfo.objName + ".eval(\"" + lang + "\", ";
        evalString += "request.getRequestURI(), " + lineNo + ", " + columnNo;
        evalString += "," + StringUtils.lineSeparator;
        evalString += StringUtils.getSafeString(expr.toString()) + ")";

        ObjInfo oldRet = cb.getFinalServiceMethodStatement();

        if (oldRet != null && oldRet.isExecutable()) {
            cb.addServiceMethodStatement(oldRet.objName + ";");
        }

        cb.setFinalServiceMethodStatement(new ObjInfo(Object.class, 
                                                      evalString));
        
        cb.addServiceMethodException("org.apache.bsf.BSFException");
    }

    /**
     * Default impl of compileScript - generates code that'll create a new
     * manager, and execute the script.
     */
    public void compileScript(String source, int lineNo, int columnNo,
                              Object script, CodeBuffer cb) 
        throws BSFException {
        ObjInfo bsfInfo = cb.getSymbol("bsf");
        
        if (bsfInfo == null) {
            bsfInfo = new ObjInfo(BSFManager.class, "bsf");
            cb.addFieldDeclaration("org.apache.bsf.BSFManager bsf = " + 
                                   "new org.apache.bsf.BSFManager();");
            cb.putSymbol("bsf", bsfInfo);
        }

        String execString = bsfInfo.objName + ".exec(\"" + lang + "\", ";
        execString += "request.getRequestURI(), " + lineNo + ", " + columnNo;
        execString += "," + StringUtils.lineSeparator;
        execString += StringUtils.getSafeString(script.toString()) + ")";

        ObjInfo oldRet = cb.getFinalServiceMethodStatement();

        if (oldRet != null && oldRet.isExecutable()) {
            cb.addServiceMethodStatement(oldRet.objName + ";");
        }

        cb.setFinalServiceMethodStatement(new ObjInfo(void.class, execString));

        cb.addServiceMethodException("org.apache.bsf.BSFException");
    }

    public void declareBean(BSFDeclaredBean bean) throws BSFException {
        throw new BSFException(BSFException.REASON_UNSUPPORTED_FEATURE,
                               "language " + lang + 
                               " does not support declareBean(...).");
    }

    /**
     * Default impl of execute - calls eval and ignores the result.
     */
    public void exec(String source, int lineNo, int columnNo, Object script)
        throws BSFException {
        eval(source, lineNo, columnNo, script);
    }

    /**
     * Get the debug manager in the constructor, not in initialize.
     * First, this is ok since the debug manager is not BSFManager
     * dependent. Second, the debug manager needs to be known
     * sooner than "initialize" for the JavaScript engine.
     */
    public BSFEngineImpl() {
        this.dbgmgr = (BSFDebugManagerImpl) BSFManager.getDebugManager();
    }		
	
    /**
     * initialize the engine; called right after construction by 
     * the manager. Declared beans are simply kept in a vector and
     * that's it. Subclasses must do whatever they want with it.
     */
    public void initialize(BSFManager mgr, String lang, Vector declaredBeans)
        throws BSFException {
        
        this.mgr = mgr;
        this.lang = lang;
        this.declaredBeans = declaredBeans;

        // initialize my properties from those of the manager. It'll send
        // propagate change events to me
        this.classPath = mgr.getClassPath();
        this.tempDir = mgr.getTempDir();
        this.classLoader = mgr.getClassLoader();
        
        // All BSFManager instances and engines are registering themselves
        // to the debug manager. This enables the debug manager to make
        // the connection between the existing engines and the registered
        // debuggers.
        // Also see terminate() for the notification of termination
        // to the debug manager.
        //
        // NOTE: will happen only when BSF is in debug mode
        // This will avoid the potentially severe bottleneck 
        // at the BSFDebugManager level in WebSphere or Tomcat...
        if (dbgmgr != null) {
            dbgmgr.registerEngine(mgr, this, lang);
        }
    }

    /**
     * Receive property change events from the manager and update my fields
     * as needed.
     *
     * @param e PropertyChange event with the change data
     */
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        Object value = e.getNewValue();
        
        if (name.equals("classPath")) {
            classPath = (String) value;
        } 
        else if (name.equals("tempDir")) {
            tempDir = (String) value;
        } 
        else if (name.equals("classLoader")) {
            classLoader = (ClassLoader) value;
        }
    }
    
    public void terminate() {
        mgr = null;
        declaredBeans = null;
        classLoader = null;
        
        // See initialize method.
        // Notify the debug manager so that it knows about 
        // the existing BSFManager and BSFEngine instances.
        if (dbgmgr != null) {
            dbgmgr.terminateEngineNotify(mgr, this, lang);
        }
    }
    
    public void undeclareBean(BSFDeclaredBean bean) throws BSFException {
        throw new BSFException(BSFException.REASON_UNSUPPORTED_FEATURE,
                               "language " + lang + 
                               " does not support undeclareBean(...).");
    }
}
