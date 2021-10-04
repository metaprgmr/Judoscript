/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-10-2002  JH   Fixed a Tar-related bug caused by a directory
 *                  name in tar.
 * 03-27-2005  JH   Added "as" support.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.List;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.*;
import com.ice.tar.*; 

public class StmtFSCopy extends StmtFSList
{
  static final String DEFAULT_PAGE = "default.htm";

  public Expr under    = null;
  public Expr as       = null;
  public Expr strip    = null;
  public Expr manifest = null;

  public boolean keepDirs = false;
  public boolean compress = true;
  public boolean archive  = false;
  public boolean force    = false;
  public boolean verbose  = false;
  public boolean dupOk    = false;

  public void exec() throws Throwable {
    StreamCopier sc = null;

    if (recursive)
      keepDirs = true;

    int cnt = 0;
    String s_incls = (incls != null) ? incls.getStringValue() : "*";
    String s_excls = (excls != null) ? excls.getStringValue() : null;
    String s_base = (base != null) ? RT.getFilePath(base.getStringValue()) : RT.getCurrentDir().toString();
    s_base = RT.getFile(s_base).getAbsolutePath().replace('\\','/');
    String s_under = null;
    if (under != null) {
      s_under = under.getStringValue();
      if (s_under.startsWith("/") || s_under.startsWith("\\"))
        s_under = s_under.substring(1);
      if (!s_under.endsWith("/") && !s_under.endsWith("\\"))
        s_under += '/';
    }
    String s_strip = null;
    int strip_len = 0;
    if (strip != null) {
      s_strip = strip.getStringValue();
      strip_len = s_strip.length();
    }
    String s_as = null;
    if (as != null)
      s_as = as.getStringValue();

    FileFinder ff = getFileFinder(s_base, s_incls, s_excls);
    try {
      if (ff.isLocal() && !s_base.endsWith("/"))
        s_base += '/';

      Variable var = value!=null ? ((Expr)value).eval() : JudoUtil.toVariable(RT.getCurrentDir().toString());
      // if it is a zip file destination, handle differently.
      if (var instanceof JavaObject) {
        Object o = ((JavaObject)var).getObjectValue();
        if (o instanceof ZipWriter) {
          ListArray lr = new ListArray();
          ff.list(lr, 0, -1);
          cnt = zip((ZipWriter)o, lr.array.getStorage(), ff.isLocal()?s_base:"", ff, s_under, s_as, s_strip);
          setResult(cnt);
          return;
        } else if (o instanceof TarWriter) {
          ListArray lr = new ListArray();
          ff.list(lr, 0, -1);
          cnt = tar((TarWriter)o, lr.array.getStorage(), ff.isLocal()?s_base:"", ff, s_under, s_as, s_strip, sc);
          setResult(cnt);
          return;
        }
      }

      File f;
      String path;
      ListArray lr = new ListArray();
      ff.list(lr, 0, -1);
      List files = lr.array.getStorage();
      if (files.size() == 0) {
        setResult(0);
        return;
      }

      String s_dest = JudoUtil.fixFilePaths(var.getStringValue());
      if (StringUtils.isBlank(s_dest))
        s_dest = RT.getCurrentDir().toString();
      else if (archive) {
        // if it is to archive, do zipping
        path = Lib.getFileExt(s_dest).toLowerCase();
        if (path.equals("zip") ||
            path.equals("jar") ||
            path.equals("war") ||
            path.equals("ear") ||
            path.equals("rar"))
          cnt = zip(s_dest, files, ff.isLocal() ? s_base : "", ff, s_under, s_as, s_strip, sc);
        else
          cnt = tar(s_dest, files, ff.isLocal() ? s_base : "", ff, s_under, s_as, s_strip, sc);
        setResult(cnt);
        return;
      }
      File dest = RT.getFile(s_dest); // either current dir or as specified.

      boolean b = dest.exists();
      if (!b || b && !dest.isDirectory()) { // the destination is an existing file.
        if (files.size() > 1)
          ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED,
            "Cannot copy multiple files to a single destination '"+s_dest+"'.");
        if (ff.isTarFS()) {
          cnt = copyFromTar(s_base, files, dest, null, null, null);
          setResult(cnt);
          return;
        }
        path = files.get(0).toString(); // the only one.

        try {
          if (ff.isLocal())
            f = RT.getFile(path);
          else
            f = ff.getFile(path);
          if (f.isFile()) {
            boolean copied = Lib.copyFile(f, dest, !force, sc);
            if (doEcho && (copied || verbose)) {
              LinePrintWriter lpw = RT.getOut();
              if (copied) {
                lpw.println("copy " + f.getCanonicalPath() + "\n  to " + dest.getCanonicalPath());
              } else {
                lpw.println("pass " + f.getCanonicalPath());
              }
              lpw.flush();
            }
            setResult(1);
          } else { // for non-files, just ignore.
            setResult(0);
          }
        } catch(IOException ioe) {
          ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED,"Failed to copy '"+path+"'",ioe);
        }
        return;
      }

      if (StringUtils.isNotBlank(s_under))
        dest = new File(dest, s_under);

      if (!dest.exists() && files.size() == 1) {
        if (ff.isTarFS()) {
          setResult(copyFromTar(s_base, files, dest, null, null, null));
          return;
        }
        path = files.get(0).toString(); // so this is the only one source.
        if (ff.isZipFS()) f = ff.getFile(path);
        else f = RT.getFile(path);

        // Do the copying --
        try {
          if (f.isFile()) { // for copying, only for a file.
            boolean copied = Lib.copyFile(f, dest, !force, sc);
            if (doEcho && (copied || verbose)) {
              LinePrintWriter lpw = RT.getOut();
              if (copied) {
                lpw.println("copy " + f.getCanonicalPath() + "\n  to " + dest.getCanonicalPath());
              } else {
                lpw.println("pass " + f.getCanonicalPath());
              }
              lpw.flush();
            }
            setResult(1);
          } else { // for non-files, ignore.
            setResult(0);
          }
        } catch(IOException ioe) {
          ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Failed to "+name+" '"+path+"'", ioe);
        }
        return;
      }

      // Here, the base destination directory exists; but s_under subdir may not.
      dest.mkdirs();

      if (ff.isTarFS()) {
        cnt = copyFromTar(s_base, files, dest, null, null, s_strip);
        setResult(cnt);
        return;
      }

      // If recursive, maintain the directory structure; otherwise, just copy
      // the files. Same-name files will be overwritten by later ones.

      int baselen = s_base.length();
      for (int i=0; i<files.size(); ++i) {
        path = files.get(i).toString().replace('\\','/');
        File f1 = ff.getFile(path);
        if (f1.isDirectory())
          continue;
        if (ff.isLocal()) {
          f = new File(dest,
                keepDirs && path.startsWith(s_base) ? path.substring(baselen) : Lib.getFileName(path));
        } else if (ff.isUrlFS()) {
          f = new File(dest, getUrlPath((UrlFS.UrlFile)f1));
        } else { // zip file
          String tgt = path;
          if (strip_len > 0 && tgt.startsWith(s_strip))
            tgt = tgt.substring(strip_len);
          f = new File(dest, keepDirs ? tgt : Lib.getFileName(tgt));
        }
        boolean copied = false;
        try { copied = Lib.copyFile(f1, f, !force, sc); } catch(FileNotFoundException fnfe) {}
        if (doEcho && (copied || verbose)) {
          LinePrintWriter lpw = RT.getOut();
          if (copied) {
            lpw.println("copy " + f1.getCanonicalPath() +
                        "\n  to " + f.getCanonicalPath());
          } else {
            lpw.println("pass " + f1.getCanonicalPath());
          }
          lpw.flush();
        }
        ++cnt;
      }
      setResult(cnt);
    } finally {
      ff.close();
    }

  } // end of exec().

  String getUrlPath(UrlFS.UrlFile uf) {
    String x = uf.getName();
    if (StringUtils.isBlank(x))
      x = DEFAULT_PAGE;
    x = keepDirs ? uf.getPath() + x : x;
    if (x.startsWith("/"))
      x = x.substring(1);
    return x;
  }
  
  // For linear storage like tar, we need to reopen the file
  // and go through all the entries again.
  //
  // dest is File, ZipWriter or TarWriter.
  int copyFromTar(String tarFile, List files, Object dest, String s_under, String s_as, String s_strip)
      throws ExceptionRuntime
  {
    // dump all the file names into a set to speed up matching.
    HashSet set = new HashSet();
    for (int i=files.size()-1; i>=0; --i)
      set.add(files.get(i).toString());

    int strip_len = (s_strip!=null) ? s_strip.length() : 0;
    int cnt = 0;
    try {
      TarInputStream tis;
      FileInputStream fis = new FileInputStream(tarFile);
      String x = tarFile.toLowerCase();
      if (x.endsWith(".tar.gz") || x.endsWith(".taz"))
        tis = new TarInputStream(new GZIPInputStream(fis));
      else
        tis = new TarInputStream(fis);
      TarEntry te = tis.getNextEntry();
      while (te != null) {
        String tgt = te.getName();
        if (strip_len > 0 && tgt.startsWith(s_strip))
          tgt = tgt.substring(strip_len);

        if (set.contains(te.getName())) {
          if (dest instanceof File) {
            if (tgt != null && tgt.endsWith("/")) {
              new File((File)dest,tgt).mkdirs();
            } else {
              File to = new File((File)dest, tgt); // s_under is handled already.
              Lib.ensureDirectory(to);
              FileOutputStream fos = new FileOutputStream(to);
              tis.copyEntryContents(fos);
              fos.close();
              if (doEcho) {
                RT.getOut().println("-tar " + to.getCanonicalPath());
                RT.getOut().flush();
              }
            }
          } else {
            if (StringUtils.isNotBlank(s_under)) {
              if (tgt.startsWith("/"))
                tgt = tgt.substring(1);
              tgt = s_under + tgt;
            }
            if (dest instanceof ZipWriter)
              ((ZipWriter)dest).writeFileFromTar(tis, te, tgt);
            else if (dest instanceof TarWriter)
              ((TarWriter)dest).writeFileFromTar(tis, te, tgt);
          }
        }
        te = tis.getNextEntry();
      }
      tis.close();
    } catch(IOException ioe) {
      ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED,
                           Lib.getExceptionMsg("Failed to read files from tar", ioe));
    }
    return cnt;
  }

  public int zip(String zipFile, List files, String base, FileFinder ff,
                 String s_under, String s_as, String s_strip, StreamCopier sc) throws Throwable
  {
    int cnt = 0;
    try {
      JarOutputStream jos = null;
      if (manifest != null) {
        String manif = manifest.getStringValue();
        if (StringUtils.isNotBlank(manif)) {
          StringBufferInputStream sbis = new StringBufferInputStream(manif);
          jos = new JarOutputStream(new FileOutputStream(zipFile), new Manifest(sbis));
        }
      }
      ZipWriter zw = jos==null ? new ZipWriter(zipFile, sc) : new ZipWriter(jos, sc);
      cnt = zip(zw, files, base, ff, s_under, s_as, s_strip);
      zw.close();
    } catch(IOException ioe) {
      ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Failed to archive to '"+zipFile+"'", ioe);
    }
    return cnt;
  }

  public int zip(ZipWriter zw, List files, String base, FileFinder ff,
                 String s_under, String s_as, String s_strip) throws Throwable
  {
    int cnt = 0;
    zw.setCompress(compress);
    if (ff.isTarFS())
      return copyFromTar(ff.getTarFileName(), files, zw, s_under, s_as, s_strip);

    int strip_len = (s_strip!=null) ? s_strip.length() : 0;
    int baselen = base.length();
    for (int i=0; i<files.size(); ++i) {
      String path = files.get(i).toString().replace('\\', '/');
      String tgt = path;
      if (strip_len > 0 && tgt.startsWith(s_strip))
        tgt = tgt.substring(strip_len);
      String zename;
      if (StringUtils.isNotBlank(s_as)) {
        if (files.size() > 1)
          ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Unable to zip multiple files into \""+s_as+"\".");
        zename = s_as;
      } else {
        if (ff.isUrlFS())
          zename = getUrlPath((UrlFS.UrlFile)ff.getFile(path));
        else
          zename = tgt.startsWith(base) ? tgt.substring(baselen) : Lib.getFileName(tgt);
        if (StringUtils.isNotBlank(s_under)) {
          if (zename.startsWith("/"))
            zename = zename.substring(1);
          zename = s_under + zename;
        } 
      } 
    
      File f1 = null;
      try {
        if (ff.isLocal())
          f1 = RT.getFile(path);
        else
          f1 = ff.getFile(path); // should not throw that exception.
        if (f1.isDirectory())
          continue;
        zw.writeFile(f1, zename, dupOk);
        if (doEcho) {
          RT.getOut().println("+zip " + f1.getCanonicalPath());
          RT.getOut().flush();
        }
        ++cnt;
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Failed to zip '" + f1.getAbsolutePath()+"'", e);
      }
    }
    return cnt;
  }

  public int tar(String tarFile, List files, String base, FileFinder ff,
                 String s_under, String s_as, String s_strip, StreamCopier sc) throws Throwable
  {
    int cnt = 0;
    try {
      TarWriter tw = new TarWriter(tarFile, sc); // TarWriter handles ".tar.gz" and ".taz", etc.
      cnt = tar(tw, files, base, ff, s_under, s_as, s_strip, sc);
      tw.close();
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Failed to archive to '"+tarFile+"'", e);
    }
    return cnt;
  }

  public int tar(TarWriter tw, List files, String base, FileFinder ff,
                 String s_under, String s_as, String s_strip, StreamCopier sc) throws Throwable
  {
    int cnt = 0;
    if (ff.isTarFS())
      return copyFromTar(ff.getTarFileName(), files, tw, s_under, s_as, s_strip);

    int strip_len = (s_strip!=null) ? s_strip.length() : 0;
    int baselen = base.length();
    for (int i=0; i<files.size(); ++i) {
      String path = files.get(i).toString().replace('\\', '/');
      String tgt = path;
      if (strip_len > 0 && tgt.startsWith(s_strip))
        tgt = tgt.substring(strip_len);
      String tename;
      if (StringUtils.isNotBlank(s_as)) {
        if (files.size() > 1)
          ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Unable to tar multiple files into \""+s_as+"\".");
        tename = s_as;
      } else {
        if (ff.isUrlFS())
          tename = getUrlPath((UrlFS.UrlFile)ff.getFile(path));
        else
        tename = tgt.startsWith(base) ? tgt.substring(baselen) : Lib.getFileName(tgt);
        if (StringUtils.isNotBlank(s_under)) {
          if (tename.startsWith("/"))
            tename = tename.substring(1);
          tename = s_under + tename;
        }
      }

      File f1 = null;
      try {
        if (ff.isLocal())
          f1 = RT.getFile(path);
        else
          f1 = ff.getFile(path); // should not throw that exception.
        if (f1.isDirectory())
          continue;
        tw.writeFile(f1, tename, dupOk);
        if (doEcho) {
          RT.getOut().println("+tar " + f1.getCanonicalPath());
          RT.getOut().flush();
        }
        ++cnt;
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED, "Failed to tar '" + f1.getAbsolutePath() + "'", e);
      }
    }
    return cnt;
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtFSCopy");
    // TODO: dump().
    out.endTag();
  }

} // end of class StmtFSCopy.
