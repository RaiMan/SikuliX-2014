/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * INTERNAL USE
 * implements a transparent screen overlay for various purposes
 */
public class OverlayTransparentWindow extends JFrame implements EventSubject {

  static Method __setWindowOpacity = null;
  static Method __setWindowOpaque = null;
  static Method __isTranslucencySupported = null;
  static boolean isInit_getMethods = false;
  private JPanel _panel = null;
  private Color _col = null;
  private OverlayTransparentWindow _win = null;
  private Graphics2D _currG2D = null;
  private EventObserver _obs;

  public OverlayTransparentWindow() {
    init(null, null);
  }

  public OverlayTransparentWindow(Color col, EventObserver o) {
    init(col, o);
  }

  private void init(Color col, EventObserver o) {
    setUndecorated(true);
    setAlwaysOnTop(true);
    if (Settings.JavaVersion < 7) {
      dynGetMethod();
    }
    if (col != null) {
      _obs = o;
      _win = this;
      if (Settings.JavaVersion < 7) {
        _col = col;
        try {
          if (__setWindowOpaque != null) {
            __setWindowOpaque.invoke(null, (Window) this, false);
          } else {
            Debug.error("J6: TransparentWindow.setOpaque: not initialized");
          }
        } catch (Exception e) {
          Debug.error("J6: TransparentWindow.setOpaque: did not work");
        }
      } else {
        try {
          setBackground(col);
        } catch (Exception e) {
          Debug.error("J7: TransparentWindow.setOpaque: did not work");
        }
      }
      _panel = new javax.swing.JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
          if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            _currG2D = g2d;
            if (Settings.JavaVersion < 7) {
              g2d.setColor(_col);
              g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            if (_obs != null) {
              _obs.update(_win);
            }
          } else {
            super.paintComponent(g);
          }
        }
      };
      _panel.setLayout(null);
      add(_panel);
    }
  }

  public JPanel getJPanel() {
    return _panel;
  }

  public Graphics2D getJPanelGraphics() {
    return _currG2D;
  }

  @Override
  public void addObserver(EventObserver o) {
    _obs = o;
  }

  @Override
  public void notifyObserver() {
    _obs.update(this);
  }

  @Override
  public void setOpacity(float alpha) {
    if (Settings.JavaVersion > 6) {
      try {
        Class<?> c = Class.forName("javax.swing.JFrame");
        Method m = c.getMethod("setOpacity", float.class);
        m.invoke(this, alpha);
      } catch (Exception e) {
        Debug.error("J7: TransparentWindow.setOpacity: did not work");
      }
    } else {
      try {
        if (__setWindowOpacity != null) {
          __setWindowOpacity.invoke(null, (Window) this, alpha);
        } else {
          Debug.error("J6: TransparentWindow.setOpacity: not initialized");
        }
      } catch (Exception e) {
        Debug.error("J6: TransparentWindow.setOpacity: did not work");
      }
    }
  }

  public void close() {
    setVisible(false);
    dispose();
  }

  private static Method dynGetMethod() {
    if (!isInit_getMethods) {
      try {
        Class<?> aUC = Class.forName("com.sun.awt.AWTUtilities");
        Class<?> aUC_TL = aUC.getClasses()[0];
        Field[] enums = aUC_TL.getFields();
        Object aUC_TL_TL = null;
        for (Field e : enums) {
          String n = e.getName();
          if ("TRANSLUCENT".equals(n)) {
            aUC_TL_TL = e.get(null);
            break;
          }
        }
        __isTranslucencySupported = aUC.getMethod("isTranslucencySupported", aUC_TL);
        if ((Boolean) __isTranslucencySupported.invoke(null, aUC_TL_TL)) {
          __setWindowOpacity = aUC.getMethod("setWindowOpacity", Window.class, float.class);
          __setWindowOpaque = aUC.getMethod("setWindowOpaque", Window.class, boolean.class);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      isInit_getMethods = true;
    }
    return __setWindowOpacity;
  }
}
