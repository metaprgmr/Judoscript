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


package com.judoscript.studio;

import java.io.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;


public final class SourceCodeDocument extends DefaultStyledDocument
{
  public static final int maxBufLen = 4096;

//  String filePath = null;
  File file = null;
  boolean dirty = false;

  Segment segment = new Segment(); // expect to read often.
  int aNlPos = 0;
  int aLineNum = 1;

  UndoAction undoAction = new UndoAction();
  RedoAction redoAction = new RedoAction();
  UndoManager undo = new UndoManager();


  public SourceCodeDocument(String fileName) throws IOException {
    this();
    load(new File(fileName));
  }

  public SourceCodeDocument(File file) throws IOException {
    this();
    load(file);
  }

  public SourceCodeDocument() {
    addUndoableEditListener(new UndoableEditListener() {
        public void undoableEditHappened(UndoableEditEvent e) {
          undo.addEdit(e.getEdit());
          undoAction.updateUndoState();
          redoAction.updateRedoState();
        }
      }
    );
    addDocumentListener(new DocumentListener() {
        public final void changedUpdate(DocumentEvent e) { setDirty(); }
        public final void insertUpdate(DocumentEvent e)  { setDirty(); }
        public final void removeUpdate(DocumentEvent e)  { setDirty(); }
      }
    );
  }

  void setDirty() {
    dirty = true;
    aNlPos = 0;
    aLineNum = 1;
  }

  public Action getUndoAction() { return undoAction; }
  public Action getRedoAction() { return redoAction; }
  public File   getFile() { return file; }
  public void   setFile(File f) { file = f; }
  public boolean hasFile() { return (file != null); }
  public boolean isDirty() { return dirty; }

  public void clear() {
    try { remove(0, getLength()); } catch(BadLocationException ble) {}
    dirty = false;
    file = null;
  }

  public void load(File f) throws IOException {
    file = f;
    FileReader fr = new FileReader(f);
    char[] buf = new char[maxBufLen];
    while (true) {
      int len = fr.read(buf,0,buf.length);
      try { insertString(getLength(), new String(buf,0,len), null); }
      catch(BadLocationException ble) {}
      if (len < buf.length)
        break;
    }
    fr.close();
    dirty = false;
  }

  public void save(File file) throws IOException {
    setFile(file);
    save();
  }

  public void save(String path) throws IOException {
    setFile(new File(path));
    save();
  }

  public void save() throws IOException {
    FileWriter fw = new FileWriter(file);
    Reader r = getReader(true);
    char[] buf = new char[maxBufLen];
    while (true) {
      int len = r.read(buf,0,buf.length);
      if (len <= 0)
        break;
      fw.write(buf,0,len);
    }
    fw.close();
    dirty = false;
  }

  public int getLineNumber(int pos) throws BadLocationException {
    if (pos < 0) pos = 0;
    else if (pos > getLength()) pos = getLength();

    if (pos == aNlPos)
      return aLineNum;

    int delta;
    if (pos > aNlPos) {
      delta = 1;
      getText(aNlPos+1, pos-aNlPos-1, segment);
    } else {
      delta = -1;
      getText(pos, aNlPos-pos+1, segment);
    }
    int i = segment.offset; 
    int j = i + segment.count;
    for (; i<j; i++) {
      if (segment.array[i] == '\n') {
        if (delta == 1)
          aNlPos += i - segment.offset + 1;
        else
          aNlPos -= i - segment.offset;
        aLineNum += delta;
      }
    }
    return aLineNum;
  }

  public int gotoLine(int lineNum) {
    if (lineNum == aLineNum)
      return aNlPos + 1;

    return 0; // TODO
  }

  public Reader getReader(boolean forSave) { return new MyReader(forSave); }

  /////////////////////////////////////////////////////////
  // inner classes
  //

  class UndoAction extends AbstractAction
  {
    public UndoAction() { super("Undo"); setEnabled(false); }
          
    public void actionPerformed(ActionEvent e) {
      try {
        undo.undo();
      } catch (CannotUndoException ex) {
        System.out.println("Unable to undo: " + ex);
        ex.printStackTrace();
      }
      updateUndoState();
      redoAction.updateRedoState();
    }
          
    protected void updateUndoState() {
      if (undo.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, undo.getUndoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }      
  }    

  class RedoAction extends AbstractAction
  {
    public RedoAction() { super("Redo"); setEnabled(false); }

    public void actionPerformed(ActionEvent e) {
      try {
        undo.redo();
      } catch (CannotRedoException ex) {
        System.out.println("Unable to redo: " + ex);
        ex.printStackTrace();
      }
      updateRedoState();
      undoAction.updateUndoState();
    }

    protected void updateRedoState() {
      if (undo.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, undo.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }    

  class MyReader extends Reader
  {
    int ptr = 0;
    boolean forSave;

    MyReader(boolean forSave) { this.forSave = forSave; }

    public void close() { ptr = getLength(); }

    public int read(char[] cbuf, int off, int len) throws IOException {
      try {
        if (ptr >= getLength()) {
          if (forSave)
            return 0;
          else
            throw new IOException("EOF encountered.");
        }
        segment.count = 0;
        len = Math.min(len, getLength()-ptr);
        getText(ptr, len, segment);
        len = segment.count;
        ptr += len;
      } catch(BadLocationException ble) {}
      for (int i=0; i<len; i++)
        cbuf[i+off] = segment.array[i+segment.offset];
      return len;
    }
  }

} // end of class SourceCodeDocument.
