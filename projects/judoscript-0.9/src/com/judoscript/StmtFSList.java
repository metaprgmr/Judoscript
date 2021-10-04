/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 04-07-2002  JH   Fixed a bug that "remove" did not work.
 * 08-28-2004  JH   Enhanced listFiles to handle operations.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.*;
import com.judoscript.util.*;

/**
 * Supported operations are:
 *
 *  FS_LIST
 *  FS_LIST_COUNT
 *  ( FS_LIST_SIZE
 *  | FS_LIST_COMPRESSED_SIZE
 *  | FS_LIST_COUNTLINES
 *  | FS_LIST_COUNTWORDS
 *  )+
 *  FS_REMOVE
 *  FS_SETFILETIME
 *  FS_SETREADONLY
 *  FS_ADD_TO_CLASSPATH
 *  FS_DO
 *
 */
public class StmtFSList extends StmtBase
{
  public int action = FS_LIST;
  public String name = "listFiles";
  int moreCountActions[] = { FS_NONE, FS_NONE, FS_NONE };
      // Can only be FS_LIST_SIZE/COMPRESSED_SIZE/COUNTLINES/COUNTWORDS.

  public Expr mergeResult = null;
  public int  option = FileFinder.LIST_FILE_ONLY;
  public int  infoOption = FS_LIST_PER_NONE;
  public int  listOption = FS_LIST_BY_NONE;
  public boolean recursive = false;
  public boolean doEcho    = false;
  public boolean noHidden  = false;
  public boolean asTree    = false;
  public Expr base  = null;
  public Expr incls = null;
  public Expr excls = null;
  public Expr limit = null;
  public Object value = null;

  public StmtFSList() { super(-1); }

  public void setCustomAction(BlockSimple blk) {
    action = FS_DO;
    value = new CustomAction();
    ((CustomAction)value).setStmts(blk);
  }

  public void setAction(String actionName, int _action) throws IllegalArgumentException {
    if (action == FS_LIST || action == FS_ADD_TO_CLASSPATH) {
      action = _action;
      name = actionName;
      return;
    } else if (doEcho) {
      ; // fail it.
    } else {
      switch(action) {
      case FS_LIST_SIZE:
      case FS_LIST_COMPRESSED_SIZE:
      case FS_LIST_COUNTLINES:
      case FS_LIST_COUNTWORDS:
        switch(_action) {
        case FS_LIST_SIZE:
        case FS_LIST_COMPRESSED_SIZE:
        case FS_LIST_COUNTLINES:
        case FS_LIST_COUNTWORDS:
          for (int i=0; i<moreCountActions.length; ++i) {
            int x = moreCountActions[i];
            if (x == _action)
              return;
            if (x == FS_NONE) {
              moreCountActions[i] = _action;
              return;
            }
          }
          return;
        }
      }
    }
    throw new IllegalArgumentException(
      "Action '" + actionName + "' can not be specified with '" + name + "' command.");
  }

  FileFinder getFileFinder(String s_base, String s_incls, String s_excls) throws Exception {
    if (s_incls.indexOf("://") > 0)
      return new FileFinder(new UrlFS(), s_incls);

    String path = Lib.getFileExt(s_base).toLowerCase();
    FileFinder ff;
    if (path.equals("zip") ||
        path.equals("jar") ||
        path.equals("war") ||
        path.equals("ear") ||
        path.equals("rar"))
    {
      try {
        if (action==FS_REMOVE || action==FS_SETREADONLY || action==FS_SETFILETIME)
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                               "Cannot " + name + " files within zip archives.");
        return new FileFinder(new ZipFile(s_base),s_incls,s_excls,recursive,noHidden);
      } catch(IOException ioe) {
        ExceptionRuntime.rte(RTERR_IO_FILE_OPEN_FAILURE,
                             "Failed to open zip archive '" + s_base + "'", ioe);
      }
      return null;
    } else if (path.equals("tar")||path.equals("taz")||s_base.toLowerCase().endsWith(".tar.gz")) {
        if (action==FS_REMOVE || action==FS_SETREADONLY || action==FS_SETFILETIME)
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                               "Cannot " + name + " files within zip archives.");
        try {
          return new FileFinder(new TarFS(s_base), s_incls, s_excls, recursive, noHidden);
        } catch(IOException ioe) {
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                               Lib.getExceptionMsg("Failed to open tar archive", ioe));
          return null;
        }
    } else {
      s_base = s_base.replace('\\', '/');
      if (!s_base.endsWith("/")) s_base += "/";
      return new FileFinder(s_base, JudoUtil.fixFilePaths(s_incls), JudoUtil.fixFilePaths(s_excls),
                            recursive, noHidden);
    }
  }

  public void exec() throws Throwable {
    if (asTree)
      option = FileFinder.LIST_EVERYTHING;

    String s_incls = incls!=null ? incls.getStringValue() : null;
    if (StringUtils.isBlank(s_incls)) {
      if (action == FS_REMOVE)
        return; // don't delete anything if no files specified.
      s_incls = "*";
    }

    int max = limit!=null ? (int)limit.getLongValue() : -1;
    String s_base = (base != null) ? RT.getFilePath(base.getStringValue()) : RT.getCurrentDir().toString();
    String s_excls = (excls != null) ? excls.getStringValue() : null;
    FileFinder ff = getFileFinder(s_base,s_incls,s_excls);

    if (doEcho) {
      if (listOption == FS_LIST_BY_NONE) {
        setResult(ff.list(new ListPrinter(RT.getOut()), option, max));
        return;
      } // if sorted, fall thru...
    }
     
    JudoListReceiver lr = null;
    switch (action) {
    case FS_LIST:
    case FS_ADD_TO_CLASSPATH:
      if (mergeResult == null) {
        lr = asTree ? (JudoListReceiver)new ListTree(ff.getBaseDir()) : new ListArray();
      } else {
        Variable v = mergeResult.eval();
        lr = (v instanceof _Array) ? new ListArray((_Array)v) : new ListArray();
      }
      break;

    case FS_LIST_COUNT:
      lr = new ListCount();
      break;

    case FS_LIST_COUNTLINES:
    case FS_LIST_COUNTWORDS:
    case FS_LIST_SIZE:
    case FS_LIST_COMPRESSED_SIZE:
      lr = new ListSizeAndCount(infoOption==FS_LIST_PER_FILE, action, moreCountActions);
      break;

    case FS_SETFILETIME:
    case FS_SETREADONLY:
    case FS_REMOVE:
    case FS_DO:
      lr = new ActionReceiver(RT.getOut());
      break;

    default: // shouldn't happen.
      System.out.println("??? " + action);
      break;
    }

    ff.list(lr, option, max);
    if (listOption != FS_LIST_BY_NONE) {
      ((ListArray)lr).sort(listOption);
      if (doEcho) {
        List lst = ((ListArray)lr).array.getStorage();
        PrintWriter pw = RT.getOut();
        for (int i=0; i<lst.size(); ++i)
          pw.println(lst.get(i));
        pw.flush();
        return;
      }
    }

    if (action == FS_ADD_TO_CLASSPATH) {
      Object[] oa = (((ListArray)lr).array).getObjectArrayValue();
      UserClasspath ucp = RT.getClasspath();
      for (int i=0; i<oa.length; ++i)
        ucp.add(oa[i].toString());
    }

    setResult(lr.getResult());
  }

  public void setResult(int res) throws Throwable {
    setResult(ConstInt.getInt(res));
  }

  public void setResult(Variable res) throws Throwable {
    RT.setVariable(THIS_NAME, res, 0);
    RT.setVariable(FS_RESULT_NAME, res, 0);
  }

  public void pushNewFrame() {}

  public void dump(XMLWriter out) {
    out.openTag("StmtFSList");
    // TODO: dump().
    out.endTag();
  }

  //////////////////////////////////////////////////
  // inner interface JudoListReceiver
  //
  public static interface JudoListReceiver extends ListReceiver
  {
    public Variable getResult();
  }

  //////////////////////////////////////////////////
  // inner class ListTree
  //
  static class ListTree implements JudoListReceiver
  {
    HashMap path2node = new HashMap();
    UserDefined tree = null;

    ListTree(String base) throws Throwable {
      base = base.replace('\\','/');
      if (base.endsWith("/"))
        base = base.substring(0,base.length()-1);
      tree = TreeNodeType.createTreeNode("path",base,"isDir",ConstInt.TRUE);
      path2node.put(base, tree);
    }

    public void finish() {}
    public Variable getResult() {
      Comparator cptr = new Comparator() {
        public int compare(Object o1, Object o2) {
          UserDefined ud1 = (UserDefined)o1;
          UserDefined ud2 = (UserDefined)o2;
          if (ud1.exists("isDir")) {
            if (!ud2.exists("isDir"))
              return -1;
          } else if (ud2.exists("isDir"))
            return 1;
          try {
            return ((UserDefined)o1).resolve("path").toString().compareTo(
                     ((UserDefined)o2).resolve("path").toString());
          } catch(Throwable e) { return 0; }
        }
      };
      try { tree.invoke("sortChildrenWholeTree", new Expr[]{ JudoUtil.toVariable(cptr) }, null); }
      catch(Throwable e) {}
      return tree;
    }
    public void receive(Object file) throws Throwable {
      File f = (File)file;
      UserDefined node = TreeNodeType.createTreeNode("path", JudoUtil.getPathName(f));
      if (f.isDirectory())
        node.setVariable("isDir", ConstInt.TRUE, 0);
      path2node.put(JudoUtil.getPathName(f), node);
      String ppath;
      File pfile = f.getParentFile();
      if (pfile != null) {
        ppath = JudoUtil.getPathName(pfile);
      } else { // ZippedFile or TarredFile
        ppath = JudoUtil.getPathName(f);
        int idx = ppath.lastIndexOf('/');
        ppath = (idx <= 0) ? "" : ppath.substring(0, idx);
      }
      UserDefined pnode = (UserDefined)path2node.get(ppath);
      if (pnode == null)
        pnode = tree;
      pnode.invoke("addChild", new Expr[]{ node }, null);
    }

  } // end of inner class ListTree.

  //////////////////////////////////////////////////
  // inner class ListArray
  //
  static class ListArray implements JudoListReceiver
  {
    int count = 0;
    public _Array array;

    ListArray() { array = new _Array(); }
    ListArray(_Array ar) { array = ar; }

    public void receive(Object file) {
      array.append(JudoUtil.toVariable(JudoUtil.getPathName((File)file)));
    }

    public void finish() { count = 0; }
    public Variable getResult() { return array; }

    public void sort(int orderBy) {
      if (orderBy == FS_LIST_BY_NAME) {
        try { array.sort(null, null); } catch(Throwable e) {}
        return;
      } 
      List lst = array.getStorage();
      Object[] fa = new Object[lst.size()];
      int i;
      if (orderBy == FS_LIST_BY_EXT) {
        for (i=0; i<lst.size(); ++i)
          fa[i] = lst.get(i).toString();
      } else {
        for (i=0; i<lst.size(); ++i)
          fa[i] = new File(lst.get(i).toString());
      }
      Comparator c;
      if (orderBy == FS_LIST_BY_SIZE) {
        c = new Comparator() {
          public int compare(Object o1, Object o2) {
            long s1 = ((File)o1).length();
            long s2 = ((File)o2).length();
            return (s1==s2) ? 0 : (s1>s2) ? 1 : -1;
          }
        };
      } else if (orderBy == FS_LIST_BY_DATE) {
        c = new Comparator() {
          public int compare(Object o1, Object o2) {
            long t1 = ((File)o1).lastModified();
            long t2 = ((File)o2).lastModified();
            return (t1==t2) ? 0 : (t1>t2) ? 1 : -1;
          }
        };
      } else { // FS_LIST_BY_EXT
        c = new Comparator() {
          public int compare(Object o1, Object o2) {
            String s1 = Lib.getFileExt(o1.toString());
            String s2 = Lib.getFileExt(o2.toString());
            return s1.compareToIgnoreCase(s2);
          }
        };
      }
      Arrays.sort(fa, c);
      for (i=0; i<lst.size(); ++i)
        lst.set(i, JudoUtil.toVariable(JudoUtil.getPathName((File)fa[i])));
    }

  } // end of inner class ListArray.

  //////////////////////////////////////////////////
  // inner class ListCount
  //
  static class ListCount implements JudoListReceiver
  {
    int dirs = 0;
    int files = 0;
    long total = 0;

    public ListCount() {}

    public void receive(Object file) {
      File f = (File)file;
      ++total;
      if (f.isDirectory())
        ++dirs;
      else if (f.isFile())
        ++files;
    }

    public void finish() {}

    public Variable getResult() {
      _Array array = new _Array();
      array.append(ConstInt.getInt(files));
      array.append(ConstInt.getInt(dirs));
      array.append(ConstInt.getInt(total));
      return array;
    }
  }

  //////////////////////////////////////////////////
  // inner class ListSizeAndCount
  //
  static class ListSizeAndCount implements JudoListReceiver
  {
    int actions[]; // FS_LIST_COUNTLINES/_COUNTWORDS/_SIZE/_COMPRESSED_SIZE
    long results[];
    boolean perFile;
    com.judoscript.bio.SortedMap sm;

    ListSizeAndCount(boolean perFile, int action, int moreCountActions[]) throws Throwable {
      this.perFile = perFile;

      int len = 0;
      for (; moreCountActions[len] != FS_NONE; ++len);
      ++len;

      results = new long[len];
      actions = new int[len];
      actions[0] = action;
      for (int i=1; i<len; ++i)
        actions[i] = moreCountActions[i-1];

      if (perFile)
        sm = new com.judoscript.bio.SortedMap();
    }

    public void receive(Object file) {
      File f = (File)file;
      long temp[] = new long[results.length];
      for (int i=0; i<actions.length; ++i) {
        temp[i] = 0;
        int type = actions[i];
        if (type==FS_LIST_COUNTLINES || type==FS_LIST_COUNTWORDS) {
          if (f.isFile()) {
            if (f instanceof TarFS.TarredFile)
              ; // Unable to count in tar files so far.
            else {
              try {
                if (type==FS_LIST_COUNTLINES)
                  temp[i] = Lib.countFileLines(f);
                else
                  temp[i] = Lib.countFileWords(f);
              } catch(Throwable e) {}
            }
          }
        } else if ((type==FS_LIST_COMPRESSED_SIZE) && (file instanceof ZipFS.ZippedFile)) {
          temp[i] = ((ZipFS.ZippedFile)file).getCompressedSize();
        } else {
          temp[i] = f.length();
        }
        results[i] += temp[i];
      }
      if (perFile) {
        try {
          sm.setVariable(f.getPath(),
            results.length==1 ? (Variable)ConstInt.getInt(temp[0]) : JudoUtil.toVariable(temp),
            0);
        } catch(Throwable e) {}
      }
    }

    public void finish() {
      if (perFile) {
        try {
          sm.setVariable("TOTAL",
            results.length==1 ? (Variable)ConstInt.getInt(results[0]) : JudoUtil.toVariable(results),
            0);
        } catch(Throwable e) {}
      }
    }

    public Variable getResult() {
      if (perFile)
        return sm;
      return results.length==1 ? (Variable)ConstInt.getInt(results[0]) : JudoUtil.toVariable(results);
    }
  }

  //////////////////////////////////////////////////
  // inner class ActionReceiver
  //
  class ActionReceiver implements JudoListReceiver
  {
    ArrayList result = new ArrayList();
    PrintWriter out;
    long lvalue;

    ActionReceiver(PrintWriter out) {
      this.out = out;
      if (action == FS_SETFILETIME) {
        try {
          lvalue = ((Expr)value).getLongValue();
        } catch(Throwable e) {
          lvalue = System.currentTimeMillis();
        }
      }
    }

    public void receive(Object file) throws Throwable {
      boolean ret = false;
      String cmd = null;
      if (option == FileFinder.LIST_FILE_ONLY && ((File)file).isDirectory())
        return;
      if (option == FileFinder.LIST_DIR_ONLY && !((File)file).isDirectory())
        return;

      switch(action) {
      case FS_SETFILETIME:
        ret = ((File)file).setLastModified(lvalue);
        break;
      case FS_SETREADONLY:
        ret = ((File)file).setReadOnly();
        break;
      case FS_REMOVE:
        ret = ((File)file).delete();
        break;
      case FS_DO:
/*
        if (value instanceof AccessFunction) {
          AccessFunction af = (AccessFunction)value;
          File f = (File)file;
          Expr p1 = JudoUtil.toVariable(JudoUtil.getPathName(f));
          Expr p2;
          if (f instanceof ZipFS.ZippedFile)
            p2 = JudoUtil.toVariable(((ZipFS.ZippedFile)f).getZipFile());
          else 
            p2 = ValueSpecial.NIL;
          RT.call(af.getName(), new Expr[]{p1, p2}, null, true).getBoolValue();
*/
        if (value instanceof CustomAction) {
          CustomAction ca = (CustomAction)value;
          File f = (File)file;
          Variable p1 = JudoUtil.toVariable(JudoUtil.getPathName(f));
          Variable p2;
          if (f instanceof ZipFS.ZippedFile) // tarred file: can do nothing.
            p2 = JudoUtil.toVariable(((ZipFS.ZippedFile)f).getZipFile());
          else 
            p2 = ValueSpecial.NIL;
          ca.exec(p1, p2);
        } else {
          cmd = Lib.toOSPath(((Expr)value).getStringValue());
          Runtime.getRuntime().exec(cmd).waitFor();
        }
        ret = true;
        break;
      default:
        ;
      }
      if (ret)
        result.add(JudoUtil.getPathName(((File)file)));
      if (doEcho && out != null) {
        switch(action) {
        case FS_SETFILETIME: out.println("settime " + file);  break;
        case FS_SETREADONLY: out.println("readonly " + file); break;
        case FS_REMOVE:      out.println("remove " + file);   break;
        case FS_DO:          out.println(cmd); break;
        default:             out.println("Unknown action: " + action + ": " + file); return;
        }
        out.flush();
      }
    }
    public void finish() {}
    public Variable getResult() { return JudoUtil.toVariable(result); }

  } // end of inner class ActionReceiver.

  static class CustomAction extends Block
  {
    public void exec(Variable curfile, Variable archive) throws Throwable {
      this.pushNewFrame();
      try {
        RT.setLocalVariable("$_", curfile, 0);
        RT.setLocalVariable("$$archive", archive, 0);
        RuntimeContext rtc = RT.curCtxt();

loop:   while (true) {
          int curStmt = beginBlock();
          if (curStmt < 0)
            break;
inloop:   for (int i=curStmt; i<codeLength; i++) {
            try {
              rtc.execStmt(stmts[i]);
            } catch(Exception e) {
              if (e instanceof ExceptionControl) {
                ExceptionControl ce = (ExceptionControl)e;
                if (ce.isBreak())
                  FileFinder.breakSearch(); // exception.
                if (ce.isContinue())
                  return;
                throw ce; // return, exit.
              }
              handleException(e);
              break loop;
            }
          }
          if (!endBlock())
            break;
        }
      } finally {
        try { handleFinally(); } finally { popFrame(); }
      }
    }

  } // end of class CustomAction.

} // end of class StmtFSList.

