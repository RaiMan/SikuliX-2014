/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.ide;

import java.awt.*;
import javax.swing.*;

import com.explodingpixels.macwidgets.plaf.EmphasizedLabelUI;
import org.sikuli.basics.RunSetup;
import org.sikuli.basics.Settings;

class SikuliIDEStatusBar extends JPanel {

  private JLabel _lblMsg;
  private JLabel _lblCaretPos;

  public SikuliIDEStatusBar() {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(10, 20));

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setOpaque(false);
    _lblMsg = new JLabel();
    _lblMsg.setPreferredSize(new Dimension(400, 20));
    _lblMsg.setUI(new EmphasizedLabelUI());
    _lblCaretPos = new JLabel();
    _lblCaretPos.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
    _lblCaretPos.setUI(new EmphasizedLabelUI());
    _lblCaretPos.setFont(UIManager.getFont("Button.font").deriveFont(11.0f));
    setCaretPosition(1, 1);
    resetMessage();
    add(_lblMsg, BorderLayout.WEST);
    add(_lblCaretPos, BorderLayout.LINE_END);
//      add(rightPanel, BorderLayout.EAST);
  }

  public void setCaretPosition(int row, int col) {
    _lblCaretPos.setText(
            SikuliIDEI18N._I("statusLineColumn", row, col));
  }

  public void setMessage(String text) {
    _lblMsg.setText("   " + text);
  }

  public void resetMessage() {
    setMessage(Settings.SikuliVersionIDE + " --- Build: " + RunSetup.timestampBuilt);
  }
//  @Override
//  protected void paintComponent(Graphics g) {
//    super.paintComponent(g);
//    int y = 0;
//    g.setColor(new Color(156, 154, 140));
//    g.drawLine(0, y, getWidth(), y);
//    y++;
//    g.setColor(new Color(196, 194, 183));
//    g.drawLine(0, y, getWidth(), y);
//  }
}
