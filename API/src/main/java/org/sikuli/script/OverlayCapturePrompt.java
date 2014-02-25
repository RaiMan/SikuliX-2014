/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import org.sikuli.natives.SysUtil;

/**
 * INTERNAL USE
 * implements the screen overlay used with the capture feature
 */
public class OverlayCapturePrompt extends OverlayTransparentWindow implements EventSubject {

  static Color _overlayColor = new Color(0F, 0F, 0F, 0.6F);
  final static float MIN_DARKER_FACTOR = 0.6f;
  final static long MSG_DISPLAY_TIME = 2000;
  final static long WIN_FADE_IN_TIME = 200;
  static final Font fontMsg = new Font("Arial", Font.PLAIN, 60);
  static final Color selFrameColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
  static final Color selCrossColor = new Color(1.0f, 0.0f, 0.0f, 0.6f);
  static final Color screenFrameColor = new Color(1.0f, 0.0f, 0.0f, 0.6f);
  static final BasicStroke strokeScreenFrame = new BasicStroke(5);
  static final BasicStroke _StrokeCross = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{2f}, 0);
  static final BasicStroke bs = new BasicStroke(1);
  EventObserver _obs;
  Screen _scr;
  BufferedImage _scr_img = null;
  BufferedImage _darker_screen = null;
  BufferedImage bi = null;
  float _darker_factor;
  Rectangle rectSelection;
  int srcScreenId = 0;
  Location srcScreenLocation = null;
  Location destScreenLocation = null;
  ScreenUnion srcScreenUnion = null;
  int srcx, srcy, destx, desty;
  boolean _canceled = false;
  String _msg;
  boolean didPurgeMessage = false;
  boolean dragging = false;

  public OverlayCapturePrompt(Screen scr, EventObserver ob) {
    init(scr, ob);
  }

  private void init(Screen scr, EventObserver ob) {
    addObserver(ob);
    if (scr == null) {
      if (Screen.getNumberScreens() > 1) {
        scr = new ScreenUnion();
      } else {
        scr = Screen.getPrimaryScreen();
      }
    }
    _scr = scr;

    _canceled = false;
    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    rectSelection = new Rectangle();
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseMoved(java.awt.event.MouseEvent e) {
        if (_msg == null) {
          return;
        }
        _msg = null;
        repaint();
      }

      @Override
      public void mousePressed(java.awt.event.MouseEvent e) {
        if (_scr_img == null) {
          return;
        }
        if (_msg != null) {
          _msg = null;
          didPurgeMessage = true;
        }
        destx = srcx = e.getX();
        desty = srcy = e.getY();
        srcScreenUnion = new ScreenUnion();
        srcScreenId = srcScreenUnion.getIdFromPoint(srcx, srcy);
        srcScreenLocation = new Location(srcx + srcScreenUnion.getBounds().x,
                srcy + srcScreenUnion.getBounds().y);
        Debug.log(2, "CapturePrompt: started at (%d,%d) as %s on %d", srcx, srcy,
                srcScreenLocation.toStringShort(), srcScreenId);
        repaint();
      }

      @Override
      public void mouseReleased(java.awt.event.MouseEvent e) {
        if (_scr_img == null) {
          return;
        }
        if (!dragging && didPurgeMessage) {
          didPurgeMessage = false;
          return;
        }
        if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
          _canceled = true;
          Debug.log(2, "CapturePrompt: aborted using right mouse button");
        } else {
          destScreenLocation = new Location(destx + srcScreenUnion.getBounds().x,
                  desty + srcScreenUnion.getBounds().y);
          Debug.log(2, "CapturePrompt: finished at (%d,%d) as %s on %d", destx, desty,
                  destScreenLocation.toStringShort(), srcScreenId);
        }
        setVisible(false);
        notifyObserver();
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(java.awt.event.MouseEvent e) {
        if (_scr_img == null) {
          return;
        }
        dragging = true;
        destx = e.getX();
        desty = e.getY();
        repaint();
      }
    });

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          if (_msg != null) {
            _msg = null;
            repaint();
            return;
          }
          _canceled = true;
          Debug.log(2, "CapturePrompt: aborted using key ESC");
          setVisible(false);
          notifyObserver();
        }
      }
    });
  }

  @Override
  public void close() {
    Debug.log(2, "CapturePrompt.close: freeing resources");
    dispose();
    _scr_img = null;
    _darker_screen = null;
    bi = null;
  }

  @Override
  public void addObserver(EventObserver o) {
    _obs = o;
  }

  @Override
  public void notifyObserver() {
    _obs.update(this);
  }

  public void prompt(String msg, int delayMS) {
    try {
      Thread.sleep(delayMS);
    } catch (InterruptedException ie) {
    }
    prompt(msg);
  }

  public void prompt(int delayMS) {
    prompt(null, delayMS);
  }

  public void prompt() {
    prompt(null);
  }

  public void prompt(String msg) {
    captureScreen(_scr);
    this.setBounds(_scr.getBounds());
    this.setAlwaysOnTop(true);
    _msg = msg;
    Debug.log(2, "CapturePrompt: " + _msg);
    this.setVisible(true);
    if (!Settings.isJava7()) {
      if (Settings.isMac()) {
        SysUtil.getOSUtil().bringWindowToFront(this, false);
      }
    }
    this.requestFocus();
  }

  private void captureScreen(Screen scr) {
    ScreenImage simg = scr.capture();
    _scr_img = simg.getImage();

    _darker_factor = 0.6f;
    RescaleOp op = new RescaleOp(_darker_factor, 0, null);
    _darker_screen = op.filter(_scr_img, null);

  }

  public ScreenImage getSelection() {
    if (_canceled) {
      return null;
    }
    BufferedImage cropImg = cropSelection();
    if (cropImg == null) {
      return null;
    }
    rectSelection.x += _scr.getBounds().x;
    rectSelection.y += _scr.getBounds().y;
    ScreenImage ret = new ScreenImage(rectSelection, cropImg);
    return ret;
  }

  private BufferedImage cropSelection() {
    int w = rectSelection.width, h = rectSelection.height;
    if (w <= 0 || h <= 0) {
      return null;
    }
    BufferedImage crop = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D crop_g2d = crop.createGraphics();
    try {
      crop_g2d.drawImage(
              _scr_img.getSubimage(rectSelection.x, rectSelection.y, w, h),
              null, 0, 0);
    } catch (RasterFormatException e) {
      e.printStackTrace();
    }
    crop_g2d.dispose();
    /*
     try{
     ImageIO.write(crop, "png", new File("debug_crop.png"));
     }
     catch(IOException e){}
     */
    return crop;
  }

  void drawMessage(Graphics2D g2d) {
    if (_msg == null) {
      return;
    }
    g2d.setFont(fontMsg);
    g2d.setColor(new Color(1f, 1f, 1f, 1));
    int sw = g2d.getFontMetrics().stringWidth(_msg);
    int sh = g2d.getFontMetrics().getMaxAscent();
    Rectangle ubound = (new ScreenUnion()).getBounds();
    for (int i = 0; i < Screen.getNumberScreens(); i++) {
      Rectangle bound = Screen.getBounds(i);
      int cx = bound.x + (bound.width - sw) / 2 - ubound.x;
      int cy = bound.y + (bound.height - sh) / 2 - ubound.y;
      g2d.drawString(_msg, cx, cy);
    }
  }

  private void drawSelection(Graphics2D g2d) {
    if (srcx != destx || srcy != desty) {
      int x1 = (srcx < destx) ? srcx : destx;
      int y1 = (srcy < desty) ? srcy : desty;
      int x2 = (srcx > destx) ? srcx : destx;
      int y2 = (srcy > desty) ? srcy : desty;

      if (Screen.getNumberScreens() > 1) {
        Rectangle selRect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        Rectangle ubound = (new ScreenUnion()).getBounds();
        selRect.x += ubound.x;
        selRect.y += ubound.y;
        Rectangle inBound = selRect.intersection(Screen.getBounds(srcScreenId));
        x1 = inBound.x - ubound.x;
        y1 = inBound.y - ubound.y;
        x2 = x1 + inBound.width - 1;
        y2 = y1 + inBound.height - 1;
      }

      rectSelection.x = x1;
      rectSelection.y = y1;
      rectSelection.width = (x2 - x1) + 1;
      rectSelection.height = (y2 - y1) + 1;

      if (rectSelection.width > 0 && rectSelection.height > 0) {
        g2d.drawImage(_scr_img.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1),
                null, x1, y1);
      }

      g2d.setColor(selFrameColor);
      g2d.setStroke(bs);
      g2d.draw(rectSelection);

      int cx = (x1 + x2) / 2;
      int cy = (y1 + y2) / 2;
      g2d.setColor(selCrossColor);
      g2d.setStroke(_StrokeCross);
      g2d.drawLine(cx, y1, cx, y2);
      g2d.drawLine(x1, cy, x2, cy);

      if (Screen.getNumberScreens() > 1) {
        drawScreenFrame(g2d, srcScreenId);
      }
    }
  }

  private void drawScreenFrame(Graphics2D g2d, int scrId) {
    Rectangle rect = Screen.getBounds(scrId);
    Rectangle ubound = (new ScreenUnion()).getBounds();
    g2d.setColor(screenFrameColor);
    g2d.setStroke(strokeScreenFrame);
    rect.x -= ubound.x;
    rect.y -= ubound.y;
    int sw = (int) (strokeScreenFrame.getLineWidth() / 2);
    rect.x += sw;
    rect.y += sw;
    rect.width -= sw * 2;
    rect.height -= sw * 2;
    g2d.draw(rect);
  }

  @Override
  public void paint(Graphics g) {
    if (_scr_img != null) {
      Graphics2D g2dWin = (Graphics2D) g;
      if (bi == null) {
        bi = new BufferedImage(_scr.getBounds().width,
                _scr.getBounds().height, BufferedImage.TYPE_INT_RGB);
      }
      Graphics2D bfG2 = bi.createGraphics();
      bfG2.drawImage(_darker_screen, 0, 0, this);
      drawMessage(bfG2);
      drawSelection(bfG2);
      g2dWin.drawImage(bi, 0, 0, this);
      setVisible(true);
    } else {
      setVisible(false);
    }
  }
}
