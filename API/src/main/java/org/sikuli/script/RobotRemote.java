/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

/**
 * INTERNAL USE - UNDER DEVELOPMENT - EXPERIMENTAL
 * Implemenation of IRobot that redirects actions targeted towards a DesktopRobot
 * to a remote DesktopRobot that in turn is implemented in the package Sikuli-Remote.
 * The remote communication is implemented on sockets in the driving ScreenRemote
 */
public class RobotRemote implements IRobot {

  private static int heldButtons = 0;
  private static String heldKeys = "";
  private static ArrayList<Integer> heldKeyCodes = new ArrayList<Integer>();
  private ScreenRemote scr = null;
  private static final String key = "KEY";
  private static final String mouse = "MOUSE";
  private static final String capture = "CAPTURE";
  private static final String kType = "TYPE";
  private static final String mClick = "CLICK";
  private static final String mMove = "MOVE";
  private static final String cSystem = "SYSTEM";
  private static final String cBounds = "BOUNDS";
  private String clickCommand = null;
  private String typeCommand = null;
  private String result;
  private String system = "";
  private int numberScreens = 0;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "RobotRemote: " + message, args);
  }

  private static void log(String message, Object... args) {
    log(3, message, args);
  }

  public RobotRemote(ScreenRemote sr) {
    init(sr);
  }

  public RobotRemote() {
    init(null);
  }

  private void init(ScreenRemote sr) {
    if (sr == null || !sr.isValid()) {
      log(-1, "RobotRemote: must be created with valid ScreenRemote - not useable");
    }
    scr = sr;
    String sys = (String) send(cSystem);
    if (sys == null || !sys.contains(" ")) {
      log(-1, "could not get remote system - robot might not be useable: " + sys);
    } else {
      system = sys.substring(0, sys.indexOf(" "));
      try {
        numberScreens = Integer.parseInt(sys.substring(sys.indexOf(" ") + 1));
      } catch(Exception ex) {
        log(-1, "could not get remote numberScreens - robot might not be useable: " + sys);
      }
    }
  }

  public boolean isValid() {
    return (scr != null);
  }

  @Override
  public boolean isRemote() {
    return true;
  }

  public String getSystem() {
    return system;
  }

  public int getNumberScreens() {
    return numberScreens;
  }

  public Rectangle getBounds() {
    return getBounds(0);
  }

  public Rectangle getBounds(int screenID) {
    return (Rectangle) send("BOUNDS " + screenID);
  }

  @Override
  public void keyDown(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        if (heldKeys.indexOf(keys.charAt(i)) == -1) {
          Debug.log(5, "press: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.PRESS_ONLY);
          heldKeys += keys.charAt(i);
        }
      }
      waitForIdle();
    }
  }

  @Override
  public void keyUp(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        int pos;
        if ((pos = heldKeys.indexOf(keys.charAt(i))) != -1) {
          Debug.log(5, "release: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.RELEASE_ONLY);
          heldKeys = heldKeys.substring(0, pos)
                  + heldKeys.substring(pos + 1);
        }
      }
      waitForIdle();
    }
  }

  @Override
  public void keyDown(int code) {
    if (!heldKeyCodes.contains(code)) {
      keyPress(code);
      heldKeyCodes.add(code);
    }
    waitForIdle();
  }

  @Override
  public void keyUp(int code) {
    if (heldKeyCodes.contains(code)) {
      keyRelease(code);
      heldKeyCodes.remove((Object) code);
    }
    waitForIdle();
  }

  @Override
  public void keyUp() {
    keyUp(heldKeys);
    for (int code : heldKeyCodes) {
      keyUp(code);
    }
  }

  @Override
  public void pressModifiers(int modifiers) {
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      keyPress(KeyEvent.VK_SHIFT);
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      keyPress(KeyEvent.VK_CONTROL);
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      keyPress(KeyEvent.VK_ALT);
    }
    if ((modifiers & KeyModifier.META) != 0) {
      if (Settings.isWindows()) {
        keyPress(KeyEvent.VK_WINDOWS);
      } else {
        keyPress(KeyEvent.VK_META);
      }
    }
  }

  @Override
  public void releaseModifiers(int modifiers) {
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      keyRelease(KeyEvent.VK_SHIFT);
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      keyRelease(KeyEvent.VK_CONTROL);
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      keyRelease(KeyEvent.VK_ALT);
    }
    if ((modifiers & KeyModifier.META) != 0) {
      if ("WINDOWS".equals(system)) {
        keyRelease(KeyEvent.VK_WINDOWS);
      } else {
        keyRelease(KeyEvent.VK_META);
      }
    }
  }

  @Override
  public void typeChar(char character, KeyMode mode) {
    log(3, "doType: %s ( %d )",
            KeyEvent.getKeyText(Key.toJavaKeyCode(character)[0]).toString(),
            Key.toJavaKeyCode(character)[0]);
    doType(mode, Key.toJavaKeyCode(character));
  }

  @Override
  public void typeKey(int key) {
    if ("MAC".equals(system)) {
      if (key == Key.toJavaKeyCodeFromText("#N.")) {
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#C."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#C."));
      } else if (key == Key.toJavaKeyCodeFromText("#T.")) {
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#C."));
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#C."));
      } else if (key == Key.toJavaKeyCodeFromText("#X.")) {
        key = Key.toJavaKeyCodeFromText("#T.");
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#A."));
      }
    } else {
      doType(KeyMode.PRESS_RELEASE, key);
    }
    log(3, "doType: %s ( %d )", KeyEvent.getKeyText(key), key);
  }

  private void doType(KeyMode mode, int... keyCodes) {
    if (mode == KeyMode.PRESS_ONLY) {
      for (int i = 0; i < keyCodes.length; i++) {
        keyPress(keyCodes[i]);
      }
    } else if (mode == KeyMode.RELEASE_ONLY) {
      for (int i = 0; i < keyCodes.length; i++) {
        keyRelease(keyCodes[i]);
      }
    } else {
      for (int i = 0; i < keyCodes.length; i++) {
        keyPress(keyCodes[i]);
      }
      for (int i = 0; i < keyCodes.length; i++) {
        keyRelease(keyCodes[i]);
      }
    }
  }

  @Override
  public void typeStarts() {
    typeCommand = kType;
  }

  @Override
  public void typeEnds() {
    result = (String) send(typeCommand);
    typeCommand = null;
  }

  private void keyPress(int key) {
    if (typeCommand == null && clickCommand == null) {
      result = (String) send(String.format("%s %d ", key, key));
    } else if (typeCommand != null) {
      typeCommand += " P" + key;
    } else {
      clickCommand += " P" + key;
    }
  }

  private void keyRelease(int k) {
    if (typeCommand == null && clickCommand == null) {
      result = (String) send(String.format("%s %d ", key, k));
    } else if (typeCommand != null) {
      typeCommand += " R" + k;
    } else {
      clickCommand += " R" + k;
    }
  }

  @Override
  public void mouseMove(int x, int y) {
    result = (String) send(String.format("%s %d %d", mMove, x, y));
  }

  @Override
  public void clickStarts() {
    typeCommand = null;
    clickCommand = mClick;
  }

  @Override
  public void clickEnds() {
    result = (String) send(clickCommand);
    clickCommand = null;
  }

  public Location mousePointer() {
    return new Location((Point) send(mouse));
  }

  @Override
  public void mouseDown(int buttons) {
    if (heldButtons != 0) {
      heldButtons |= buttons;
    } else {
      heldButtons = buttons;
    }
    if (clickCommand == null) {
      result = (String) send(String.format("%s D%d", mouse, heldButtons));
    } else {
      clickCommand += " D" + heldButtons;
    }
  }

  @Override
  public int mouseUp(int buttons) {
    int bAfter;
    if (buttons == 0) {
      buttons = heldButtons;
      bAfter = 0;
    } else {
      bAfter = heldButtons & ~buttons;
    }
    if (clickCommand == null) {
      result = (String) send(String.format("%s U%d", mouse, buttons));
    } else {
      clickCommand += " U" + buttons;
    }
    heldButtons = bAfter;
    return heldButtons;
  }

  @Override
  public void mouseWheel(int wheelAmt) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ScreenImage captureScreen(Rectangle r) {
    ImageIcon img = (ImageIcon) send(
            String.format(capture + " %d %d %d %d", r.x, r.y, r.width, r.height));
    ScreenImage simg = null;
    if (img != null) {
      BufferedImage bimg = new BufferedImage(
              img.getIconWidth(), img.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
      bimg.getGraphics().drawImage(img.getImage(), 0, 0, null);
      simg = new ScreenImage(new Rectangle(r.x, r.y, img.getIconWidth(), img.getIconHeight()), bimg);
    }
    return simg;
  }

  @Override
  public void smoothMove(Location dest) {
    mouseMove(dest.x, dest.y);
  }

  @Override
  public void smoothMove(Location src, Location dest, long ms) {
    mouseMove(src.x, src.y);
    delay((int) ms);
    mouseMove(dest.x, dest.y);
  }

  @Override
  public void delay(int ms) {
    if (clickCommand != null) {
      clickCommand += " W" + ms;
    } else if (typeCommand != null) {
      typeCommand += " W" + ms;
    }
  }

  @Override
  public void setAutoDelay(int ms) {
  }

  @Override
  public Color getColorAt(int x, int y) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void cleanup() {
    mouseUp(0);
    keyUp();
  }

  @Override
  public IScreen getScreen() {
    return scr;
  }

  public Object send(String command) {
    if (!scr.isValid()) {
      log(-1, "ScreenRemote not valid - send not possible");
      return null;
    }
    Object res = null;
    try {
      scr.getOut().write(command + "\n");
      scr.getOut().flush();
      log("send: " + command);
      res = scr.getIn().readObject();
      if (res == null) {
        log(-1, "command not successful: " + command);
        return null;
      }
      if (command.startsWith(capture)) {
        if (!res.getClass().equals(ImageIcon.class)) {
          log(-1, "CAPTURE: received: " + res);
          res = null;
        }
      } else if (command.startsWith(cBounds)) {
        if (!res.getClass().equals(Rectangle.class)) {
          log(-1, "BOUNDS: received: " + res);
          res = null;
        }
      } else if (command.equals(mouse)) {
        if (!res.getClass().equals(Point.class)) {
          log(-1, "MOUSE: received: " + res);
          res = null;
        }
      } else {
        if (!res.getClass().equals(String.class)) {
          log(-1, "OTHER: received: " + res);
          res = null;
        }
      }
    } catch (Exception ex) {
      if (command.startsWith("EXIT")) {
        return("ok");
      }
      log(-1, "fatal: while processing:\n" + ex.getMessage());
    }
    return res;
  }

  @Override
  public void waitForIdle() {
  }
}
