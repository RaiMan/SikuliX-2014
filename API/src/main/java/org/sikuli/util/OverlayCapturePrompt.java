/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.util;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import org.sikuli.natives.SysUtil;
import org.sikuli.script.IScreen;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.ScreenUnion;

/**
 * INTERNAL USE implements the screen overlay used with the capture feature
 */
public class OverlayCapturePrompt extends OverlayTransparentWindow implements EventSubject {

  final static float MIN_DARKER_FACTOR = 0.6f;
  final static long MSG_DISPLAY_TIME = 2000;
  final static long WIN_FADE_IN_TIME = 200;
  static final Font fontMsg = new Font("Arial", Font.PLAIN, 60);
  static final Color selFrameColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
  static final Color selCrossColor = new Color(1.0f, 0.0f, 0.0f, 0.6f);
  static final Color screenFrameColor = new Color(1.0f, 0.0f, 0.0f, 0.6f);
  private Rectangle screenFrame = null;
  static final BasicStroke strokeScreenFrame = new BasicStroke(5);
  static final BasicStroke _StrokeCross = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{2f}, 0);
  static final BasicStroke bs = new BasicStroke(1);
  private EventObserver obs;
  private IScreen scrOCP;
  private BufferedImage scr_img = null;
  private BufferedImage scr_img_darker = null;
  private BufferedImage bi = null;
  private float darker_factor;
  private Rectangle rectSelection;
  private int srcScreenId = 0;
  private Location srcScreenLocation = null;
  private Location destScreenLocation = null;
  private int srcx, srcy, destx, desty;
  private boolean canceled = false;
  private String promptMsg;
  private boolean didPurgeMessage = false;
  private boolean dragging = false;

  public OverlayCapturePrompt(IScreen scr, EventObserver ob) {
    super();
    init(scr, ob);
  }

  private void init(IScreen scr, EventObserver ob) {
    addObserver(ob);
    if (Screen.getNumberScreens() > 1) {
      scrOCP = new ScreenUnion();
    } else {
      scrOCP = Screen.getPrimaryScreen();
    }
    canceled = false;
    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    rectSelection = new Rectangle();
    addMouseListener(new MouseAdapter() {

      @Override
      public void mouseMoved(java.awt.event.MouseEvent e) {
        if (promptMsg == null) {
          return;
        }
        promptMsg = null;
        repaint();
      }

      @Override
      public void mousePressed(java.awt.event.MouseEvent e) {
        if (scr_img == null) {
          return;
        }
        if (promptMsg != null) {
          promptMsg = null;
          didPurgeMessage = true;
        }
        destx = srcx = e.getX();
        desty = srcy = e.getY();
        srcScreenId = scrOCP.getIdFromPoint(srcx, srcy);
        srcScreenLocation = new Location(srcx + scrOCP.getX(), srcy + scrOCP.getY());
        Debug.log(2, "CapturePrompt: started at (%d,%d) as %s on %d", srcx, srcy,
                srcScreenLocation.toStringShort(), srcScreenId);
        repaint();
      }

      @Override
      public void mouseReleased(java.awt.event.MouseEvent e) {
        if (scr_img == null) {
          return;
        }
        if (!dragging && didPurgeMessage) {
          didPurgeMessage = false;
          return;
        }
        if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
          canceled = true;
          Debug.log(2, "CapturePrompt: aborted using right mouse button");
        } else {
          destScreenLocation = new Location(destx + scrOCP.getX(), desty + scrOCP.getY());
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
        if (scr_img == null) {
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
          if (promptMsg != null) {
            promptMsg = null;
            repaint();
            return;
          }
          canceled = true;
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
    scr_img = null;
    scr_img_darker = null;
    bi = null;
  }

  @Override
  public void addObserver(EventObserver o) {
    obs = o;
  }

  @Override
  public void notifyObserver() {
    obs.update(this);
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
    captureScreen(scrOCP);
    Rectangle rect = new Rectangle(scrOCP.getBounds());
    this.setBounds(rect);
    promptMsg = msg;
    Debug.log(3, "CapturePrompt: [%d,%d (%d, %d)] %s", rect.x, rect.y, rect.width, rect.height, promptMsg);
    this.setVisible(true);
    if (!Settings.isJava7()) {
      if (Settings.isMac()) {
        SysUtil.getOSUtil().bringWindowToFront(this, false);
      }
    }
    this.requestFocus();
  }

  private void captureScreen(IScreen scr) {
    ScreenImage simg = scr.capture();
    scr_img = simg.getImage();
    darker_factor = 0.6f;
    RescaleOp op = new RescaleOp(darker_factor, 0, null);
    scr_img_darker = op.filter(scr_img, null);
  }

  public ScreenImage getSelection() {
    if (canceled) {
      return null;
    }
    BufferedImage cropImg = cropSelection();
    if (cropImg == null) {
      return null;
    }
    rectSelection.x += scrOCP.getX();
    rectSelection.y += scrOCP.getY();
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
              scr_img.getSubimage(rectSelection.x, rectSelection.y, w, h),
              null, 0, 0);
    } catch (RasterFormatException e) {
      Debug.error(e.getMessage());
    }
    crop_g2d.dispose();
    return crop;
  }

  void drawMessage(Graphics2D g2d) {
    if (promptMsg == null) {
      return;
    }
    g2d.setFont(fontMsg);
    g2d.setColor(new Color(1f, 1f, 1f, 1));
    int sw = g2d.getFontMetrics().stringWidth(promptMsg);
    int sh = g2d.getFontMetrics().getMaxAscent();
    Rectangle ubound = (new ScreenUnion()).getBounds();
    for (int i = 0; i < Screen.getNumberScreens(); i++) {
      Rectangle bound = Screen.getBounds(i);
      int cx = bound.x + (bound.width - sw) / 2 - ubound.x;
      int cy = bound.y + (bound.height - sh) / 2 - ubound.y;
      g2d.drawString(promptMsg, cx, cy);
    }
  }

  private void drawSelection(Graphics2D g2d) {
    if (srcx != destx || srcy != desty) {
      int x1 = (srcx < destx) ? srcx : destx;
      int y1 = (srcy < desty) ? srcy : desty;
      int x2 = (srcx > destx) ? srcx : destx;
      int y2 = (srcy > desty) ? srcy : desty;

      rectSelection.x = x1;
      rectSelection.y = y1;
      rectSelection.width = (x2 - x1) + 1;
      rectSelection.height = (y2 - y1) + 1;
      if (rectSelection.width > 0 && rectSelection.height > 0) {
        g2d.drawImage(scr_img.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1),
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
    g2d.setColor(screenFrameColor);
    g2d.setStroke(strokeScreenFrame);
    if (screenFrame == null) {
      screenFrame = Screen.getBounds(scrId);
      Rectangle ubound = scrOCP.getBounds();
      screenFrame.x -= ubound.x;
      screenFrame.y -= ubound.y;
      int sw = (int) (strokeScreenFrame.getLineWidth() / 2);
      screenFrame.x += sw;
      screenFrame.y += sw;
      screenFrame.width -= sw * 2;
      screenFrame.height -= sw * 2;
    }
    g2d.draw(screenFrame);
  }

  @Override
  public void paint(Graphics g) {
    if (scr_img != null) {
      Graphics2D g2dWin = (Graphics2D) g;
      if (bi == null) {
        bi = new BufferedImage(scrOCP.getW(),
                scrOCP.getH(), BufferedImage.TYPE_INT_RGB);
      }
      Graphics2D bfG2 = bi.createGraphics();
      bfG2.drawImage(scr_img_darker, 0, 0, this);
      drawMessage(bfG2);
      drawSelection(bfG2);
      g2dWin.drawImage(bi, 0, 0, this);
      setVisible(true);
    } else {
      setVisible(false);
    }
  }
}
