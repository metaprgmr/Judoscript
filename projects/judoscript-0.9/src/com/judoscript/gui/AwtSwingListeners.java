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


package com.judoscript.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;


// The reason we have this handler rather than implementing an EventDriven is,
// the event handlers are dynamically added, hence it has to be a rtc-based object.

/**
 * Nick name: event.
 */
public final class AwtSwingListeners extends GuiListenerBase
                                     implements ActionListener,  // === AWT
                                                AdjustmentListener,
                                                AWTEventListener,
                                                ComponentListener,
                                                ContainerListener,
                                                FocusListener,
                                                HierarchyBoundsListener,
                                                HierarchyListener,
                                                InputMethodListener,
                                                ItemListener,
                                                KeyListener,
                                                MouseListener,
                                                MouseMotionListener,
                                                TextListener,
                                                WindowListener,
                                                AncestorListener,  // === Swing
                                                CaretListener,
                                                CellEditorListener,
                                                ChangeListener,
                                                //DocumentListener, // not an EventListener
                                                HyperlinkListener,
                                                InternalFrameListener,
                                                ListDataListener,
                                                ListSelectionListener,
                                                MenuDragMouseListener,
                                                MenuKeyListener,
                                                MenuListener,
                                                //MouseInputListener, // Mouse-/MouseMotionListener
                                                PopupMenuListener,
                                                TableColumnModelListener,
                                                TableModelListener,
                                                TreeExpansionListener,
                                                TreeModelListener,
                                                TreeSelectionListener,
                                                TreeWillExpandListener,
                                                UndoableEditListener,
                                                PropertyChangeListener, // === Beans
                                                VetoableChangeListener
{
  ////////////////////////////////////////////////////////////
  // All listener methods -- all delegated to event().
  ////////////////////////////////////////////////////////////

  // ActionListener
  public void actionPerformed(ActionEvent e) { event(e,"Action","actionPerformed"); }

  // AdjustmentListener
  public void adjustmentValueChanged(AdjustmentEvent e) { event(e,"Adjustment","adjustmentValueChanged"); }

  // AWTEventListener
  public void eventDispatched(AWTEvent e) { event(e,"AWTEvent","eventDispatched"); }

  // ComponentListener
  public void componentHidden(ComponentEvent e)  { event(e,"Component","componentHidden"); }
  public void componentMoved(ComponentEvent e)   { event(e,"Component","componentMoved"); }
  public void componentResized(ComponentEvent e) { event(e,"Component","componentResized"); }
  public void componentShown(ComponentEvent e)   { event(e,"Component","componentShown"); }

  // ContainerListener
  public void componentAdded(ContainerEvent e)   { event(e,"Container","compoentAdded"); }
  public void componentRemoved(ContainerEvent e) { event(e,"Container","compoentRemoved"); }

  // FocusListener
  public void focusGained(FocusEvent e) { event(e,"Focus","focusGained"); }
  public void focusLost(FocusEvent e)   { event(e,"Focus","focusLost"); }

  // HierarchyBoundsListener
  public void ancestorMoved(HierarchyEvent e)   { event(e,"HierarchyBounds","ancestorMoved"); }
  public void ancestorResized(HierarchyEvent e) { event(e,"HierarchyBounds","ancestorResized"); }

  // HierarchyListener
  public void hierarchyChanged(HierarchyEvent e) { event(e,"Hierarchy","hierarchyChanged"); }

  // InputMethodListener
  public void caretPositionChanged(InputMethodEvent e)   { event(e,"InputMethod","caretPositionChanged"); }
  public void inputMethodTextChanged(InputMethodEvent e) { event(e,"InputMethod","inputMethodTextChanged"); }

  // ItemListener
  public void itemStateChanged(ItemEvent e) { event(e,"Item","itemStateChanged"); }

  // KeyListener
  public void keyPressed(KeyEvent e)  { event(e,"Key","keyPressed"); }
  public void keyReleased(KeyEvent e) { event(e,"Key","keyReleased"); }
  public void keyTyped(KeyEvent e)    { event(e,"Key","keyTyped"); }

  // MouseListener
  public void mouseClicked(MouseEvent e)  { event(e,"Mouse","mouseClicked"); }
  public void mouseEntered(MouseEvent e)  { event(e,"Mouse","mouseEntered"); }
  public void mouseExited(MouseEvent e)   { event(e,"Mouse","mouseExited"); }
  public void mousePressed(MouseEvent e)  { event(e,"Mouse","mousePressed"); }
  public void mouseReleased(MouseEvent e) { event(e,"Mouse","mouseReleased"); }

  // MouseMotionListener
  public void mouseDragged(MouseEvent e) { event(e,"MouseMotion","mouseDragged"); }
  public void mouseMoved(MouseEvent e)   { event(e,"MouseMotion","mouseMoved"); }

  // TextListener
  public void textValueChanged(TextEvent e) { event(e,"Text","textValueChanged"); }

  // WindowListener
  public void windowActivated(WindowEvent e)   { event(e,"Window","windowActivated"); }
  public void windowClosed(WindowEvent e)      { event(e,"Window","windowClosed"); }
  public void windowClosing(WindowEvent e)     { event(e,"Window","windowClosing"); }
  public void windowDeactivated(WindowEvent e) { event(e,"Window","windowDeactivated"); }
  public void windowDeiconified(WindowEvent e) { event(e,"Window","windowDeiconified"); }
  public void windowIconified(WindowEvent e)   { event(e,"Window","windowIconified"); }
  public void windowOpened(WindowEvent e)      { event(e,"Window","windowOpened"); }

  // Starting Swing

  // AncestorListener
  public void ancestorAdded(AncestorEvent e)   { event(e,"Ancestor","ancestorAdded"); }
  public void ancestorMoved(AncestorEvent e)   { event(e,"Ancestor","ancestorMoved"); }
  public void ancestorRemoved(AncestorEvent e) { event(e,"Ancestor","ancestorRemoved"); }

  // CaretListener
  public void caretUpdate(CaretEvent e) { event(e,"Caret","caretUpdate"); }

  // CellEditorListener
  public void editingCanceled(ChangeEvent e) { event(e,"CellEditor","editingCanceled"); }
  public void editingStopped(ChangeEvent e)  { event(e,"CellEditor","editingStopped"); }

  // ChangeListener
  public void stateChanged(ChangeEvent e) { event(e,"Change","stateChanged"); }

  // DocumentListener
  //public void changedUpdate(DocumentEvent e)
  //public void insertUpdate(DocumentEvent e)
  //public void removeUpdate(DocumentEvent e)

  // HyperlinkListener
  public void hyperlinkUpdate(HyperlinkEvent e) { event(e,"Hyperlink","hyperlinkUpdate"); }

  // InternalFrameListener
  static final String _i_f = "InternalFrame";
  public void internalFrameActivated(InternalFrameEvent e)   { event(e,_i_f,"internalFrameActivated"); }
  public void internalFrameClosed(InternalFrameEvent e)      { event(e,_i_f,"internalFrameClosed"); }
  public void internalFrameClosing(InternalFrameEvent e)     { event(e,_i_f,"internalFrameClosing"); }
  public void internalFrameDeactivated(InternalFrameEvent e) { event(e,_i_f,"internalFrameDeactivated"); }
  public void internalFrameDeiconified(InternalFrameEvent e) { event(e,_i_f,"internalFrameDeiconified"); }
  public void internalFrameIconified(InternalFrameEvent e)   { event(e,_i_f,"internalFrameIconified"); }
  public void internalFrameOpened(InternalFrameEvent e)      { event(e,_i_f,"internalFrameOpened"); }

  // ListDataListener
  public void contentsChanged(ListDataEvent e) { event(e,"ListData","intervalChanged"); }
  public void intervalAdded(ListDataEvent e)   { event(e,"ListData","intervalAdded"); }
  public void intervalRemoved(ListDataEvent e) { event(e,"ListData","intervalRemoved"); }

  // ListSelectionListener
  public void valueChanged(ListSelectionEvent e) { event(e,"ListSelection","valueChanged"); }

  // MenuDragMouseListener
  public void menuDragMouseDragged(MenuDragMouseEvent e)  { event(e,"MenuDragMouse","menuDragMouseDragged"); }
  public void menuDragMouseEntered(MenuDragMouseEvent e)  { event(e,"MenuDragMouse","menuDragMouseEntered"); }
  public void menuDragMouseExited(MenuDragMouseEvent e)   { event(e,"MenuDragMouse","menuDragMouseExited"); }
  public void menuDragMouseReleased(MenuDragMouseEvent e) { event(e,"MenuDragMouse","menuDragMouseReleased"); }

  // MenuKeyListener
  public void menuKeyPressed(MenuKeyEvent e)  { event(e,"MenuKey","menuKeyPressed"); }
  public void menuKeyReleased(MenuKeyEvent e) { event(e,"MenuKey","menuKeyReleased"); }
  public void menuKeyTyped(MenuKeyEvent e)    { event(e,"MenuKey","menuKeyTyped"); }

  // MenuListener
  public void menuCanceled(MenuEvent e)   { event(e,"Menu","menuCanceled"); }
  public void menuDeselected(MenuEvent e) { event(e,"Menu","menuDeselected"); }
  public void menuSelected(MenuEvent e)   { event(e,"Menu","menuSelected"); }

  // PopupMenuListener
  private static final String _ppm = "popupMenu";
  public void popupMenuCanceled(PopupMenuEvent e)            { event(e,"PopupMenu",_ppm+"Canceled"); }
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { event(e,"PopupMenu",_ppm+"WillBecomeInvisible"); }
  public void popupMenuWillBecomeVisible(PopupMenuEvent e)   { event(e,"PopupMenu",_ppm+"WillBecomeVisible"); }

  // TableColumnModelListener
  public void columnAdded(TableColumnModelEvent e)         { event(e,"TableColumnMode","columnAdded"); }
  public void columnMarginChanged(ChangeEvent e)           { event(e,"TableColumnMode","columnMarginChanged"); }
  public void columnMoved(TableColumnModelEvent e)         { event(e,"TableColumnMode","columnMoved"); }
  public void columnRemoved(TableColumnModelEvent e)       { event(e,"TableColumnMode","columnRemoved"); }
  public void columnSelectionChanged(ListSelectionEvent e) { event(e,"TableColumnMode","columnSelectionChanged");}

  // TableModelListener
  public void tableChanged(TableModelEvent e) { event(e,"TableMode","tableChanged"); }

  // TreeExpansionListener
  public void treeCollapsed(TreeExpansionEvent e) { event(e,"TreeExpansion","treeCollapsed"); }
  public void treeExpanded(TreeExpansionEvent e)  { event(e,"TreeExpansion","treeExpanded"); }

  // TreeModelListener
  public void treeNodesChanged(TreeModelEvent e)     { event(e,"TreeMode","treeNodesChanged"); }
  public void treeNodesInserted(TreeModelEvent e)    { event(e,"TreeMode","treeNodesInserted"); }
  public void treeNodesRemoved(TreeModelEvent e)     { event(e,"TreeMode","treeNodesRemoved"); }
  public void treeStructureChanged(TreeModelEvent e) { event(e,"TreeMode","treeStructureChanged"); }

  // TreeSelectionListener
  public void valueChanged(TreeSelectionEvent e) { event(e,"TreeSelection","valueChanged"); }

  // TreeWillExpandListener
  public void treeWillCollapse(TreeExpansionEvent e) { event(e,"TreeWillExpand","treeWillCollapse"); }
  public void treeWillExpand(TreeExpansionEvent e)   { event(e,"TreeWIllExpand","treeWillExpand"); }

  // UndoableEditListener
  public void undoableEditHappened(UndoableEditEvent e) { event(e,"UndoableEdit","undoableEditHappened"); }

  // PropertyChangeListener
  public void propertyChange(PropertyChangeEvent e) { event(e,"PropertyChange","propertyChange"); }

  // VetoableChangeListener
  public void vetoableChange(PropertyChangeEvent e) { event(e,"VetoableChange","vetoableChange"); }

} // end of class AwtSwingListeners.

