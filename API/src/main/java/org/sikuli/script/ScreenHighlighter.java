/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.HashSet;
import java.util.Set;

public class ScreenHighlighter extends OverlayTransparentWindow implements MouseListener {

  static Color _overlayColor = new Color(0F, 0F, 0F, 0.6F);
  static Color _transparentColor = new Color(0F, 0F, 0F, 0.5F);
  static Color _targetColor = new Color(1F, 0F, 0F, 0.7F);
  final static int TARGET_SIZE = 50;
  final static int DRAGGING_TIME = 200;
  static int MARGIN = 20;
  static Set<ScreenHighlighter> _opened = new HashSet<ScreenHighlighter>();
  Screen _scr;
  BufferedImage _screen = null;
  BufferedImage _darker_screen = null;
  BufferedImage bi = null;
  int srcx, srcy, destx, desty;
  Location _lastTarget;
  boolean _borderOnly = false;
  boolean _native_transparent = false;
  boolean _double_buffered = false;
  OverlayAnimator _anim;
  BasicStroke _StrokeCross = new BasicStroke(1);
  BasicStroke _StrokeCircle = new BasicStroke(2);
  BasicStroke _StrokeBorder = new BasicStroke(3);
  OverlayAnimator _aniX, _aniY;

  public ScreenHighlighter(Screen scr) {
    _scr = scr;
    init();
    setVisible(false);
    setAlwaysOnTop(true);
  }

  private void init() {
    _opened.add(this);
    if (Settings.isLinux()) {
      _double_buffered = true;
    } else if (Settings.isMac()) {
      _native_transparent = true;
    }
//		getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
//		((JPanel) getContentPane()).setDoubleBuffered(true);
    addMouseListener(this);
  }

  @Override
  public void close() {
    setVisible(false);
    _opened.remove(this);
    clean();
    try {
      Thread.sleep((int) (Settings.WaitAfterHighlight > 0.3f ? Settings.WaitAfterHighlight * 1000 - 300 : 300));
    } catch (InterruptedException e) {
    }
  }

  private void closeAfter(float secs) {
    try {
      Thread.sleep((int) secs * 1000 - 300);
    } catch (InterruptedException e) {
    }
    close();
  }

  public static void closeAll() {
    if (_opened.size() > 0) {
      Debug.log(3, "ScreenHighlighter: Removing all highlights");
      for (ScreenHighlighter s : _opened) {
        if (s.isVisible()) {
          s.setVisible(false);
          s.clean();
        }
      }
      _opened.clear();
    }
  }

  private void clean() {
    dispose();
    _screen = null;
    _darker_screen = null;
    bi = null;
  }

  //<editor-fold defaultstate="collapsed" desc="mouse events not implemented">
  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
  //</editor-fold>

  @Override
  public void mouseClicked(MouseEvent e) {
    setVisible(false);
  }

  public void highlight(Region r_) {
    if (Settings.isLinux()) {
      Debug.error("highlight does not work on Linux.");
      return;
    }
    _borderOnly = true;
    Region r;
    if (_native_transparent) {
      r = r_;
    } else {
      r = r_.grow(3);
      captureScreen(r.x, r.y, r.w, r.h);
    }
    setLocation(r.x, r.y);
    setSize(r.w, r.h);
    this.setBackground(_transparentColor);
    setVisible(true);
    requestFocus();
  }

  public void highlight(Region r_, float secs) {
    highlight(r_);
    closeAfter(secs);
  }

  public void showTarget(Location loc, float secs) {
    final int w = TARGET_SIZE, h = TARGET_SIZE;
    int x = loc.x - w / 2, y = loc.y - w / 2;
    _lastTarget = loc;
    Debug.log(2, "showTarget " + x + " " + y + " " + w + " " + h);
    showWindow(x, y, w, h, secs);
  }

  private void captureScreen(int x, int y, int w, int h) {
    ScreenImage img = _scr.capture(x, y, w, h);
    _screen = img.getImage();
    float scaleFactor = .6f;
    RescaleOp op = new RescaleOp(scaleFactor, 0, null);
    _darker_screen = op.filter(_screen, null);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d;
    if (_native_transparent || _screen != null) {
      if (_double_buffered) {
        if (bi == null || bi.getWidth(this) != getWidth()
                || bi.getHeight(this) != getHeight()) {
          bi = new BufferedImage(
                  getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D bfG2 = bi.createGraphics();
        g2d = bfG2;
      } else {
        g2d = (Graphics2D) g;
      }
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
      g2d.fillRect(0, 0, getWidth(), getHeight());
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      if (_borderOnly) {
        if (!_native_transparent) {
          g2d.drawImage(_screen, 0, 0, this);
        }
        drawBorder(g2d);
      } else {
        if (!_native_transparent) {
          g2d.drawImage(_screen, 0, 0, this);
        }
        drawTarget(g2d);
      }
      if (_double_buffered) {
        ((Graphics2D) g).drawImage(bi, 0, 0, this);
      }
      if (!isVisible()) {
        setVisible(true);
      }
    } else {
      if (isVisible()) {
        setVisible(false);
      }
    }
  }

  private void drawBorder(Graphics2D g2d) {
    g2d.setColor(_targetColor);
    g2d.setStroke(_StrokeBorder);
    int w = (int) _StrokeBorder.getLineWidth();
    g2d.drawRect(w / 2, w / 2, getWidth() - w, getHeight() - w);
  }

  private void drawTarget(Graphics2D g2d) {
    int r = TARGET_SIZE / 2;
    g2d.setColor(Color.black);
    g2d.setStroke(_StrokeCross);
    g2d.drawLine(0, r, r * 2, r);
    g2d.drawLine(r, 0, r, r * 2);

    g2d.setColor(_targetColor);
    g2d.setStroke(_StrokeCircle);
    drawCircle(r, r, r - 4, g2d);
    drawCircle(r, r, r - 10, g2d);
  }

  private void drawCircle(int x, int y, int radius, Graphics g) {
    g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
  }

  private void showWindow(int x, int y, int w, int h, float secs) {
    if (!_native_transparent) {
      captureScreen(x, y, w, h);
    }
    setLocation(x, y);
    setSize(w, h);
    this.setBackground(_targetColor);
    this.repaint();
    setVisible(true);
    requestFocus();
    closeAfter(secs);
  }
}
