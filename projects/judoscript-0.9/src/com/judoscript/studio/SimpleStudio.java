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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.parser.JudoParser;
import com.judoscript.util.*;


//--------------------------------------------------------------------------------
//
// Architecture:
//
// One the left is a StudioOutputPane, which implements SutioOutputDevice,
// that allows to get output streams and writers for system's out, err and log.
//
// On the righthandside is a JTabbedPane, where each resident is a
// JScrollPane/JTextPane enclosing a SourceCodeDocument, a DefaultStyledDocument.
//
//--------------------------------------------------------------------------------

// TODO list:
//
// 1. short cut keys
// 2. search/replace/goto/matching-delimiters
// 3. insert date/string/color
// 4. help/reference
// 5. command line parameters
// 6. multi-file view

public final class SimpleStudio extends JFrame //implements Debugger
{
  ///////////////////////////////////////////////////////////////////
  // GUI elements

  static final String windowTitleBase = "JudoScript Studio";
  int newFileSeq = 1;
  JLabel status;
  StudioOutputPane outputPane;
  JTabbedPane codePane;

  JMenuItem miFileNew;
  JMenuItem miFileOpen;
  JMenuItem miFileSave;
  JMenuItem miFileSaveas;
  JMenuItem miFileSaveall;
  JMenuItem miFileClose;
  JMenuItem miFileRun;
  JMenuItem miFileClear;
  JMenuItem miFileExit;
  JMenuItem miEditUndo = null;
  JMenuItem miEditRedo = null;
  JMenu     mnuEdit;

  public SimpleStudio(String[] args) {
    //some initial setup
    super(windowTitleBase);

    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) { shutdown(); }
        public void windowActivated(WindowEvent e) {
          Component comp = codePane.getSelectedComponent();
          if (comp != null)
            comp.requestFocus();
        }
      }
    );

    //Create the output pane and configure it.
    outputPane = new StudioOutputPane();
    JScrollPane outputScrollPane = new JScrollPane(outputPane);
    outputScrollPane.setPreferredSize(new Dimension(200, 200));

    //Create a tabbed pane for the code editor/viewer and help window.
    codePane = new JTabbedPane();

    //Create a split pane for the output and text areas.
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,outputScrollPane,codePane);
    splitPane.setOneTouchExpandable(true);

    //Create the status area.
    JPanel statusPane = new JPanel(new GridLayout(1, 1));
    status = new JLabel("Welcome!");
    statusPane.add(status);

    //Add the components to the frame.
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(splitPane, BorderLayout.CENTER);
    contentPane.add(statusPane, BorderLayout.SOUTH);
    setContentPane(contentPane);

    //Set up the menu bar.
    JMenuBar mb = new JMenuBar();

    //Add the file menu:
    JMenu menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    AbstractAction fileAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if      (src == miFileNew)    newFile();
        else if (src == miFileOpen)   openFile(null);
        else if (src == miFileSave)   saveFile(false,null);
        else if (src == miFileSaveas) saveFile(true,null);
        else if (src == miFileSaveall)saveAllFiles();
        else if (src == miFileClose)  closeFile();
        else if (src == miFileClear)  outputPane.clear();
        else if (src == miFileRun)    run();
        else if (src == miFileExit)   shutdown();
      }
    };
    menu.add(miFileNew     = createMenuItem(fileAction, "New",          KeyEvent.VK_N));
    menu.add(miFileOpen    = createMenuItem(fileAction, "Open",         KeyEvent.VK_O));
    menu.add(miFileSave    = createMenuItem(fileAction, "Save",         KeyEvent.VK_S));
    menu.add(miFileSaveas  = createMenuItem(fileAction, "Save As",      0));
    menu.add(miFileSaveall = createMenuItem(fileAction, "Save All",     KeyEvent.VK_A));
    menu.add(miFileClose   = createMenuItem(fileAction, "Close",        0));
    menu.addSeparator();
    menu.add(miFileRun     = createMenuItem(fileAction, "Run",          KeyEvent.VK_R));
    menu.add(miFileClear   = createMenuItem(fileAction, "Clear Output", KeyEvent.VK_L));
    menu.addSeparator();
    menu.add(miFileExit    = createMenuItem(fileAction, "Exit",         0));
    mb.add(menu);

    //Add the edit menu:
    Hashtable actions = new Hashtable();
    Action[] actionsArray = outputPane.getActions();
    for (int i = 0; i < actionsArray.length; i++) {
      Action a = actionsArray[i];
      actions.put(a.getValue(Action.NAME), a);
    }
    mnuEdit = new JMenu("Edit");
    menu = mnuEdit;
    menu.setMnemonic(KeyEvent.VK_E);
    menu.add(createMenuItem((Action)actions.get(DefaultEditorKit.cutAction),
                            "Cut to Clipboard", KeyEvent.VK_X));
    menu.add(createMenuItem((Action)actions.get(DefaultEditorKit.copyAction),
                            "Copy to Clipboard", KeyEvent.VK_C));
    menu.add(createMenuItem((Action)actions.get(DefaultEditorKit.pasteAction),
                            "Paste from Clipboard", KeyEvent.VK_V));
    menu.addSeparator();
    menu.add(createMenuItem((Action)actions.get(DefaultEditorKit.selectAllAction),"Select All",0));
    mb.add(menu);

    //Add the insert menu:
    menu = new JMenu("Insert");
    menu.setMnemonic(KeyEvent.VK_I);
      // TODO
    mb.add(menu);

    setJMenuBar(mb);

    //Put the initial text into the text pane.
    outputPane.printlnTitle("Legends:");
    outputPane.printlnOut("\nThis is an output message.");
    outputPane.printlnErr("This is an error message.");
    outputPane.printlnLog("This is a log message.");
    outputPane.printlnInfo("This is a Studio message.");
    outputPane.println();

    int len = (args!=null) ? args.length : 0;
    if (len == 0) {
      newFile();
    } else {
      ListReceiver lr = new ListReceiver() {
        public void receive(Object file) { openFile(((File)file).getAbsolutePath()); }
        public void finish() {}
      };

      for (int i=0; i<len; i++) {
        if (args[i].indexOf('?') >= 0 || args[i].indexOf('*') >= 0) {
          FileFinder ff = new FileFinder((String)null, args[i], null, false, true);
          try { ff.list(lr, FileFinder.LIST_FILE_ONLY, -1); }
          catch(Throwable e) { e.printStackTrace(); }
        } else {
          openFile(args[i]);
        }
      }
    }
  }

  class MyPane extends JScrollPane
  {
    SourceCodeDocument source;
    JTextPane textPane;

    MyPane(SourceCodeDocument doc, JTextPane pane) {
      super(pane);
      textPane = pane;
      source = doc;

      textPane.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) { updateUndoRedoMenuItems(source); }
          public void focusLost(FocusEvent e) {}
        }
      );
    }
  }

  JMenuItem createMenuItem(Action action, String name, int accel) {
    if ((name != null) && (action != null))
      action.putValue(Action.NAME, name);
    JMenuItem item;
    if (action != null)
      item = new JMenuItem(action);
    else if (name == null)
      item = new JMenuItem();
    else
      item = new JMenuItem(name);
    if (accel != 0)
      item.setAccelerator(KeyStroke.getKeyStroke(accel, ActionEvent.CTRL_MASK));
    return item;
  }

  void addSourcePane(String name, SourceCodeDocument source) {
    //Create the source pane and configure it.
    if (source == null) source = new SourceCodeDocument();
    JTextPane sourcePane = new JTextPane(source);
    sourcePane.setCaretPosition(0);
    sourcePane.setMargin(new Insets(5,5,5,5));
    sourcePane.addCaretListener(new CaretListener() {
        public void caretUpdate(CaretEvent e) {
          try {
            int line = ((SourceCodeDocument)((JTextPane)e.getSource()).getStyledDocument()).getLineNumber(e.getDot());
            printStatus("Line " + line);
          } catch(BadLocationException ble) {
            ble.printStackTrace(); // TODO: ignore
          }
        }
      }
    );
    updateUndoRedoMenuItems(source);
    MyPane mp = new MyPane(source,sourcePane);
    codePane.addTab(name, null, mp, "Source Code Viewer/Editor");
    codePane.setSelectedComponent(mp);
  }

  void updateUndoRedoMenuItems(SourceCodeDocument source) {
    if (miEditUndo == null) {
      miEditUndo = new JMenuItem(source.getUndoAction());
      miEditRedo = new JMenuItem(source.getRedoAction());
      miEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      mnuEdit.insertSeparator(0);
      mnuEdit.insert(miEditRedo,0);
      mnuEdit.insert(miEditUndo,0);
    } else {
      miEditUndo.setAction(source.getUndoAction());
      miEditRedo.setAction(source.getRedoAction());
    }
  }

  ///////////////////////////////////////////////////////////////////
  // user actions

  // checks the dirty flag;
  // if set, prompt for saving and does it if user agrees; also clears source.
  // Returns false if user cancels the operations.
  boolean checkSave() {
    MyPane mp = (MyPane)codePane.getSelectedComponent();
    if (mp == null) return true;
    if (mp.source.isDirty()) {
      Object[] options = {"Yes", "No", "Cancel"};
      int n = JOptionPane.showOptionDialog(this,
                  "The file has been changed.\n" +
                  "Do you like to save the changes?\n" +
                  "If you click No, they will be lost!",
                  "Save Changes?",
                  JOptionPane.YES_NO_CANCEL_OPTION,
                  JOptionPane.QUESTION_MESSAGE,
                  null,
                  options,
                  options[0]);

      switch (n) {
      case JOptionPane.YES_OPTION:
        saveFile(false,mp.source);
        break;
      case JOptionPane.NO_OPTION:
        break;
      case JOptionPane.CANCEL_OPTION:
        return false;
      }
    }

    setTitle(windowTitleBase);
    mp.source.clear();
    return true;
  }

  void newFile() {
    addSourcePane("Code " + (newFileSeq++), null);
  }

  javax.swing.filechooser.FileFilter getFileFilter() {
    return new javax.swing.filechooser.FileFilter() {
      public boolean accept(File file) {
        if (file.isDirectory())
          return true;
        String fname = file.getName().toLowerCase();
        return fname.endsWith(".judo") || fname.endsWith(".jud")  || fname.endsWith(".jd");
      }
      public String getDescription() { return "Judy Scripts (*.judo, *.jud, *.jd)"; }
    };
  }

  File pwd = new File(Lib.getCurrentDir());

  void openFile(String fileName) {
    try {
      if (fileName != null) {
        addSourcePane(getFileName(fileName), new SourceCodeDocument(fileName));
      } else {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(pwd);
        fc.setFileFilter(getFileFilter());
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File f = fc.getSelectedFile();
          pwd = f.getParentFile();
          addSourcePane(f.getName(), new SourceCodeDocument(f));
          setTitle(windowTitleBase + " - " + f.getName());
        }
      }
    } catch(Exception e) {
      printException(e, "File read failed: ");
    }
  }

  void closeFile() {
    if (!checkSave()) return;
    int idx = codePane.getSelectedIndex();
    if (idx >= 0)
      codePane.remove(idx);
  }

  void saveFile(boolean as, SourceCodeDocument doc) {
    if (doc == null) {
      MyPane mp = (MyPane)codePane.getSelectedComponent();
      if (mp == null) return;
      doc = mp.source;
    }
    if (!doc.isDirty() && !as) return;

    try {
      if (!as && doc.hasFile()) {
        doc.save();
      } else {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(getFileFilter());
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File f = fc.getSelectedFile();
          doc.save(f);
          setTitle(windowTitleBase + " - " + f.getName());
        }
      }
    } catch(Exception e) {
      printException(e, "File save failed: ");
    }
  }

  void saveAllFiles() {
    int cnt = codePane.getTabCount();
    for (int i=0; i<cnt; i++) {
      MyPane mp = (MyPane)codePane.getComponentAt(i);
      saveFile(false,mp.source);
    }
  }

  void run() {
    MyPane mp = (MyPane)codePane.getSelectedComponent();
    if (mp == null) return;
    try {
      printStatus("Running ...");
      outputPane.printlnInfo("");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
      String filename = null;
      String pathname = null;
      if (mp.source.hasFile()) {
        filename = mp.source.getFile().getName();
        pathname = mp.source.getFile().getParent();
      }
      outputPane.printlnInfo("=====================");
      if (filename != null) outputPane.printlnInfo(" " + filename);
      outputPane.printlnInfo(" " + sdf.format(new Date()));
      outputPane.printlnInfo("=====================");
      Script script = JudoParser.parse(filename, pathname, mp.source.getReader(false), null, 0, true);
      RuntimeGlobalContext rtc = new RuntimeGlobalContext((String[])null, script);

      rtc.setOut(outputPane.getOutWriter());
      rtc.setErr(outputPane.getErrWriter());
      rtc.setLog(outputPane.getLogWriter());

      //rtc.setDebugger(this);
      script.start(rtc);
      printStatus("Done.");
    } catch(Throwable t) {
      printStatus("Failed.");
      printException(t,null);
      //t.printStackTrace();
    }
  }

  void shutdown() {
    if (!checkSave()) return;
    System.exit(0);
  }

  void printException(Throwable t, String title) {
    if (title != null)
      outputPane.printlnTitle(title);
    String s = t.getMessage();
    if (StringUtils.isBlank(s))
      s = t.getClass().getName();
    outputPane.printlnInfo(s);
  }

  public void printStatus(String msg) { status.setText(msg); }

  public static String getFileName(String fname) {
    int lastIdx = fname.lastIndexOf("/");
    if (lastIdx < 0)
      lastIdx = fname.lastIndexOf("\\");
    if (lastIdx < 0) return fname;
    return fname.substring(lastIdx+1);
  }

  ////////////////////////////////////////
  // The standard main method.
  //
  public static void main(String[] args) {
    final SimpleStudio frame = new SimpleStudio(args);

    frame.setBounds(50,50,800,500);
    //frame.pack();
    frame.setVisible(true);
  }
}
