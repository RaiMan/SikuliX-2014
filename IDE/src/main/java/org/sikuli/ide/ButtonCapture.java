/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2014
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;
import org.sikuli.util.OverlayCapturePrompt;
import org.sikuli.script.ScreenImage;
import org.sikuli.basics.Settings;
import org.sikuli.script.Key;

class ButtonCapture extends ButtonOnToolbar implements ActionListener, Cloneable, EventObserver {

  private static final String me = "ButtonCapture: ";
  protected Element _line;
  protected EditorPane _codePane;
  protected boolean _isCapturing;
  private boolean captureCancelled = false;
  private EditorPatternLabel _lbl = null;

  public ButtonCapture() {
    super();
    URL imageURL = SikuliIDE.class.getResource("/icons/camera-icon.png");
    setIcon(new ImageIcon(imageURL));
    PreferencesUser pref = PreferencesUser.getInstance();
    String strHotkey = Key.convertKeyToText(
            pref.getCaptureHotkey(), pref.getCaptureHotkeyModifiers());
    setToolTipText(SikuliIDE._I("btnCaptureHint", strHotkey));
    setText(SikuliIDE._I("btnCaptureLabel"));
    //setBorderPainted(false);
    //setMaximumSize(new Dimension(26,26));
    addActionListener(this);
    _line = null;
  }

  public ButtonCapture(EditorPane codePane, Element elmLine) {
    this();
    _line = elmLine;
    _codePane = codePane;
    setUI(UIManager.getUI(this));
    setBorderPainted(true);
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    setText(null);
    URL imageURL = SikuliIDE.class.getResource("/icons/capture-small.png");
    setIcon(new ImageIcon(imageURL));
  }

  public ButtonCapture(EditorPatternLabel lbl) {
    // for internal use with the image label __CLICK-TO-CAPTURE__
    super();
    _line = null;
    _codePane = null;
    _lbl = lbl;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debug.log(2, "capture!");
    captureWithAutoDelay();
  }

  public void captureWithAutoDelay() {
    if (_isCapturing) {
      return;
    }
    PreferencesUser pref = PreferencesUser.getInstance();
    int delay = (int) (pref.getCaptureDelay() * 1000.0) + 1;
    capture(delay);
  }

  public void capture(final int delay) {
    if (_isCapturing) {
      return;
    }
    Thread t = new Thread("capture") {
      @Override
      public void run() {
        _isCapturing = true;
        SikuliIDE ide = SikuliIDE.getInstance();
        ide.setVisible(false);
        try {
          Thread.sleep(delay);
        } catch (Exception e) {
        }
        OverlayCapturePrompt p = new OverlayCapturePrompt(null, ButtonCapture.this);
        p.prompt("Select an image");
      }
    };
    t.start();
  }

  //<editor-fold defaultstate="collapsed" desc="RaiMan not used">
	/*public boolean hasNext() {
   * return false;
   * }*/
  /*public CaptureButton getNextDiffButton() {
   * return null;
   * }*/
  /*public void setParentPane(SikuliPane parent) {
   * _codePane = parent;
   * }*/
  /*public void setDiffMode(boolean flag) {
   * }*/
  /*public void setSrcElement(Element elmLine) {
   * _line = elmLine;
   * }*/
  //</editor-fold>
  @Override
  public void update(EventSubject s) {
    if (s instanceof OverlayCapturePrompt) {
      OverlayCapturePrompt cp = (OverlayCapturePrompt) s;
      ScreenImage simg = cp.getSelection();
      String filename = null;
      EditorPane pane = SikuliIDE.getInstance().getCurrentCodePane();

      if (simg != null) {
        int naming = PreferencesUser.getInstance().getAutoNamingMethod();
        if (naming == PreferencesUser.AUTO_NAMING_TIMESTAMP) {
          filename = Settings.getTimestamp();
        } else if (naming == PreferencesUser.AUTO_NAMING_OCR) {
          filename = PatternPaneNaming.getFilenameFromImage(simg.getImage());
          if (filename == null || filename.length() == 0) {
            filename = Settings.getTimestamp();
          }
        } else {
          String nameOCR = "";
          try {
            nameOCR = PatternPaneNaming.getFilenameFromImage(simg.getImage());
          } catch (Exception e) {
          }
          filename = getFilenameFromUser(nameOCR);
        }

        if (filename != null) {
          String fullpath = FileManager.saveImage(simg.getImage(), filename, pane.getSrcBundle());
          if (fullpath != null) {
            captureCompleted(FileManager.slashify(fullpath, false), cp);
            return;
          }
        }
      }
      captureCompleted(null, cp);
    }
  }

  private String getFilenameFromUser(String hint) {
    return (String) JOptionPane.showInputDialog(
            _codePane,
            SikuliIDEI18N._I("msgEnterScreenshotFilename"),
            SikuliIDEI18N._I("dlgEnterScreenshotFilename"),
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            hint);
  }

  public void captureCompleted(String imgFullPath, OverlayCapturePrompt prompt) {
    prompt.close();

    Element src = getSrcElement();
    if (imgFullPath != null) {
      Debug.log(2, "captureCompleted: " + imgFullPath);
      if (src == null) {
        if (_codePane == null) {
          if (_lbl == null) {
            insertAtCursor(SikuliIDE.getInstance().getCurrentCodePane(), imgFullPath);
          } else {
            _lbl.setFile(imgFullPath);
          }
        } else {
          insertAtCursor(_codePane, imgFullPath);
        }
      } else {
        replaceButton(src, imgFullPath);
      }
    } else {
      Debug.log(2, "ButtonCapture: Capture cancelled");
      if (src != null) {
        captureCancelled = true;
        replaceButton(src, "");
        captureCancelled = false;
      }
    }
    _isCapturing = false;
    SikuliIDE ide = SikuliIDE.getInstance();
    ide.setVisible(true);
    ide.requestFocus();
  }

  private Element getSrcElement() {
    return _line;
  }

  private boolean replaceButton(Element src, String imgFullPath) {
    if (captureCancelled) {
      if (_codePane.showThumbs && PreferencesUser.getInstance().getPrefMoreImageThumbs()
              || !_codePane.showThumbs) {
        return true;
      }
    }
    int start = src.getStartOffset();
    int end = src.getEndOffset();
    int old_sel_start = _codePane.getSelectionStart(),
            old_sel_end = _codePane.getSelectionEnd();
    try {
      StyledDocument doc = (StyledDocument) src.getDocument();
      String text = doc.getText(start, end - start);
      Debug.log(3, text);
      for (int i = start; i < end; i++) {
        Element elm = doc.getCharacterElement(i);
        if (elm.getName().equals(StyleConstants.ComponentElementName)) {
          AttributeSet attr = elm.getAttributes();
          Component com = StyleConstants.getComponent(attr);
          boolean isButton = com instanceof ButtonCapture;
          boolean isLabel = com instanceof EditorPatternLabel;
          if (isButton || isLabel && ((EditorPatternLabel) com).isCaptureButton()) {
            Debug.log(5, "button is at " + i);
            int oldCaretPos = _codePane.getCaretPosition();
            _codePane.select(i, i + 1);
            if (!_codePane.showThumbs) {
              _codePane.insertString((new EditorPatternLabel(_codePane, imgFullPath, true)).toString());
            } else {
              if (PreferencesUser.getInstance().getPrefMoreImageThumbs()) {
                com = new EditorPatternButton(_codePane, imgFullPath);
              } else {
                if (captureCancelled) {
                  com = new EditorPatternLabel(_codePane, "");
                } else {
                  com = new EditorPatternLabel(_codePane, imgFullPath, true);
                }
              }
              _codePane.insertComponent(com);
            }
            _codePane.setCaretPosition(oldCaretPos);
            break;
          }
        }
      }
    } catch (BadLocationException ble) {
      Debug.error(me + "Problem inserting Button!\n%s", ble.getMessage());
    }
    _codePane.select(old_sel_start, old_sel_end);
    _codePane.requestFocus();
    return true;
  }

  protected void insertAtCursor(EditorPane pane, String imgFilename) {
    String img = "\"" + (new File(imgFilename)).getName() + "\"";
    if (!pane.showThumbs) {
      pane.insertString(img);
    } else {
      if (PreferencesUser.getInstance().getPrefMoreImageThumbs()) {
        EditorPatternButton comp = EditorPatternButton.createFromFilename(pane, imgFilename, null);
        if (comp != null) {
          pane.insertComponent(comp);
        }
      } else {
        pane.insertComponent(new EditorPatternLabel(pane, imgFilename, true));
      }
    }
//TODO set Caret
    pane.requestFocus();
  }

  @Override
  public String toString() {
    return "\"__CLICK-TO-CAPTURE__\"";
  }
}
