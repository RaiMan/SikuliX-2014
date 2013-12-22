/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.ide;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import org.sikuli.script.EventObserver;
import org.sikuli.script.EventSubject;
import org.sikuli.script.OverlayCapturePrompt;
import org.sikuli.script.ScreenImage;

/**
 *
 * @author rhocke
 */
public class EditorRegionLabel extends JLabel implements MouseListener, EventObserver {

  protected String pyText;
  protected String oldPyText = null;
  private EditorPane editor;
  private final Color bc = Color.BLACK;
  private final Color bcs = Color.RED;
  private Color fc;
  private final Color fcs = Color.RED;
  private final Border paddingBorder = BorderFactory.createEmptyBorder(0, 4, 0, 3);
  private final Border border = BorderFactory.createLineBorder(bc);
  private final Border borders = BorderFactory.createLineBorder(bcs);
  private final Border bfinal = BorderFactory.createCompoundBorder(paddingBorder, border);
  private final Border bfinals = BorderFactory.createCompoundBorder(paddingBorder, borders);

  private SikuliIDEPopUpMenu popMenu = null;
  private boolean wasPopup = false;

  EditorRegionLabel() {
  }

  EditorRegionLabel(EditorPane pane, String lblText) {
    init(pane, lblText);
  }

  EditorRegionLabel(EditorPane pane, String lblText, String oldText) {
    oldPyText = oldText;
    init(pane, lblText);
  }

  private void init(EditorPane pane, String lblText) {
    editor = pane;
    pyText = lblText;
    setFont(new Font(editor.getFont().getFontName(), Font.PLAIN, editor.getFont().getSize()));
    setBorder(bfinal);
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    addMouseListener(this);
    setText(pyText.replaceAll("Region", "").replaceAll("\\(", "").replaceAll("\\)", ""));
  }
  
  public boolean isRegionLabel() {
    return true;
  }

  public static EditorRegionLabel labelFromString(EditorPane parentPane, String str) {
    EditorRegionLabel reg = new EditorRegionLabel(parentPane, str);
    return reg;
  }

  @Override
  public void update(EventSubject s) {
    if (s instanceof OverlayCapturePrompt) {
      OverlayCapturePrompt cp = (OverlayCapturePrompt) s;
      ScreenImage r = cp.getSelection();
      cp.close();
      if (r != null) {
        try {
          Thread.sleep(300);
        } catch (InterruptedException ie) {
        }
        doUpdate(r);
      }
    }
    SikuliIDE.getInstance().setVisible(true);
  }

  public void doUpdate(ScreenImage simg) {
    Rectangle roi = simg.getROI();
    pyText = String.format("%d,%d,%d,%d",
            (int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height);
    setText(pyText);
    pyText = "Region(" + pyText + ")";
  }

  @Override
  public String toString() {
    return pyText;
  }

  @Override
  public void mousePressed(MouseEvent me) {
    checkPopup(me);
  }

  @Override
  public void mouseReleased(MouseEvent me) {
     checkPopup(me);
 }

  private void checkPopup(MouseEvent me) {
    if (me.isPopupTrigger()) {
      popMenu = editor.getPopMenuImage();
      if (popMenu != null) {
        wasPopup = true;
        popMenu.show(this, me.getX(), me.getY());
      }
      return;
    }
  }

  @Override
  public void mouseClicked(MouseEvent me) {
    if (wasPopup) {
      wasPopup = false;
      return;
    }
    SikuliIDE ide = SikuliIDE.getInstance();
    EditorPane codePane = ide.getCurrentCodePane();
    ide.setVisible(false);
    setForeground(fc);
    setBorder(bfinal);
    OverlayCapturePrompt prompt = new OverlayCapturePrompt(null, this);
    prompt.prompt(SikuliIDE._I("msgCapturePrompt"), 500);
  }

  @Override
  public void mouseEntered(MouseEvent me) {
    setForeground(fcs);
    setBorder(bfinals);
  }

  @Override
  public void mouseExited(MouseEvent me) {
    setForeground(fc);
    setBorder(bfinal);
  }
}
