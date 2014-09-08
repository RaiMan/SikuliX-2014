/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * INTERNAL USE
 */
class FindFailedDialog extends JDialog implements ActionListener {

  JButton retryButton;
  JButton skipButton;
  JButton abortButton;
  FindFailedResponse _response;
  boolean isCapture = false;

  public FindFailedDialog(Object target) {
    init(target, false);
  }

  public FindFailedDialog(Object target, boolean isCapture) {
    init(target, isCapture);
  }

  private void init(Object target, boolean isCapture) {
    this.isCapture = isCapture;
    setModal(true);
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    Component targetComp = createTargetComponent(target);
    panel.add(targetComp, BorderLayout.NORTH);
    JPanel buttons = new JPanel();
    String textRetry = "Retry";
    if (isCapture) {
      textRetry = "Capture";
    }
    retryButton = new JButton(textRetry);
    retryButton.addActionListener(this);
    skipButton = new JButton("Skip");
    skipButton.addActionListener(this);
    abortButton = new JButton("Abort");
    abortButton.addActionListener(this);
    buttons.add(retryButton);
    buttons.add(skipButton);
    buttons.add(abortButton);
    panel.add(buttons, BorderLayout.SOUTH);
    add(panel);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        _response = FindFailedResponse.ABORT;
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (retryButton == e.getSource()) {
      _response = FindFailedResponse.RETRY;
    } else if (abortButton == e.getSource()) {
      _response = FindFailedResponse.ABORT;
    } else if (skipButton == e.getSource()) {
      _response = FindFailedResponse.SKIP;
    }
    dispose();
  }

  public FindFailedResponse getResponse() {
    return _response;
  }

  <PatternString> Component createTargetComponent(PatternString target) {
    org.sikuli.script.Image image = null;
    JLabel c = null;
    String targetTyp = "";
    JPanel p;
    if (!isCapture) {
      if (target instanceof Pattern) {
        Pattern pat = (Pattern) target;
        targetTyp = "pattern";
        target = (PatternString) pat.toString();
        image = pat.getImage();
      } else if (target instanceof String) {
        image = org.sikuli.script.Image.get((String) target);
        if (image != null) {
          targetTyp = "image";
        } else {
          c = new JLabel("Sikuli cannot find text:" + (String) target);
          return c;
        }
      } else {
        return null;
      }
    } else {
      c = new JLabel("Request to capture: " + (String) target);
      return c;
    }
    p = new JPanel();
    p.setLayout(new BorderLayout());
    JLabel iconLabel = new JLabel();
    String rescale = "";
    Image bimage = null;
    if (image != null) {
      int w = image.get().getWidth(this);
      int h = image.get().getHeight(this);
      if (w > 500) {
        w = 500;
        h = -h;
        rescale = " (rescaled to 500x...)";
      }
      if (h > 300) {
        h = 300;
        w = -w;
        rescale = " (rescaled to ...x300)";
      }
      if (h < 0 && w < 0) {
        w = 500;
        h = 300;
        rescale = " (rescaled to 500x300)";
      }
      bimage = image.get().getScaledInstance(w, h, Image.SCALE_DEFAULT);
    }
    iconLabel.setIcon(new ImageIcon(bimage));
    c = new JLabel("Sikuli cannot find " + targetTyp + rescale + ".");
    p.add(c, BorderLayout.PAGE_START);
    p.add(new JLabel((String) target));
    p.add(iconLabel, BorderLayout.PAGE_END);
    return p;
  }

  @Override
  public void setVisible(boolean flag) {
    if (flag) {
//TODO Can not be called in the constructor (as JFRrame?)
// Doing so somehow made it impossible to keep
// the dialog always on top.
      pack();
      setAlwaysOnTop(true);
      setResizable(false);
      setLocationRelativeTo(this);
      requestFocus();
    }
    super.setVisible(flag);
  }
}
