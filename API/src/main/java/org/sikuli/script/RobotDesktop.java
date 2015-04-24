/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Animator;
import org.sikuli.basics.AnimatorOutQuarticEase;
import org.sikuli.basics.AnimatorTimeBased;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;

/**
 * INTERNAL USE Implementation of IRobot making a DesktopRobot using java.awt.Robot
 */
public class RobotDesktop extends Robot implements IRobot {

  final static int MAX_DELAY = 60000;
  private static int heldButtons = 0;
  private static String heldKeys = "";
  private static final ArrayList<Integer> heldKeyCodes = new ArrayList<Integer>();
  public static int stdAutoDelay = 0;
  public static int stdMaxElapsed = 1000;
  private Screen scr = null;
  private static RunTime runTime = RunTime.get();
  private long start;
  
  private void logRobot(Robot robot, int delay, String msg) {
    start = new Date().getTime();
    int theDelay = robot.getAutoDelay();
    if (theDelay > delay) {
      Debug.log(0, msg, robot.isAutoWaitForIdle(), theDelay);
    }
  }

  private void logRobot(int maxElapsed, String msg) {
    long elapsed = new Date().getTime() - start;
    if (elapsed > maxElapsed) {
      Debug.log(0, msg, elapsed);
    }
  }
  
  @Override
  public boolean isRemote() {
    return false;
  }

  @Override
  public Screen getScreen() {
    return scr;
  }

  public RobotDesktop(Screen screen) throws AWTException {
    super(runTime.getGraphicsDevice(screen.getcurrentID()));
    scr = screen;
  }

  @Override
  public void smoothMove(Location dest) {
    smoothMove(Mouse.at(), dest, (long) (Settings.MoveMouseDelay * 1000L));
  }

  @Override
  public void smoothMove(Location src, Location dest, long ms) {
    Debug.log(4, "RobotDesktop: smoothMove (%.1f): " + src.toString() + "---" + dest.toString(), Settings.MoveMouseDelay);
    if (ms == 0) {
      doMouseMove(dest.x, dest.y);
      checkMousePosition(dest);
      return;
    }
    Animator aniX = new AnimatorTimeBased(
            new AnimatorOutQuarticEase(src.x, dest.x, ms));
    Animator aniY = new AnimatorTimeBased(
            new AnimatorOutQuarticEase(src.y, dest.y, ms));
    float x = 0, y = 0;
    while (aniX.running()) {
      x = aniX.step();
      y = aniY.step();
      doMouseMove((int) x, (int) y);
    }
    checkMousePosition(new Location((int) x, (int) y));
  }

  private void checkMousePosition(Location p) {
    PointerInfo mp = MouseInfo.getPointerInfo();
    Point pc;
    if (mp == null) {
      Debug.error("RobotDesktop: checkMousePosition: MouseInfo.getPointerInfo invalid\nafter move to %s", p);
    } else {
      pc = mp.getLocation();
      if (pc.x != p.x || pc.y != p.y) {
        Debug.error("RobotDesktop: checkMousePosition: should be %s\nbut after move is %s"
								+ "\nPossible cause in case you did not touch the mouse while script was running:\n"
                + " Mouse actions are blocked generally or by the frontmost application."
								+ (Settings.isWindows() ? "\nYou might try to run the SikuliX stuff as admin." : ""),
                p, new Location(pc));
      }
    }
  }

  @Override
  public void mouseDown(int buttons) {
    if (heldButtons != 0) {
      Debug.error("mouseDown: buttons still pressed - using all", buttons, heldButtons);
      heldButtons |= buttons;
    } else {
      heldButtons = buttons;
    }
    doMouseDown(heldButtons);
  }

  @Override
  public int mouseUp(int buttons) {
    if (buttons == 0) {
      doMouseUp(heldButtons);
      heldButtons = 0;
    } else {
      doMouseUp(buttons);
      heldButtons &= ~buttons;
    }
    return heldButtons;
  }
  
  private void doMouseDown(int buttons) {
    logRobot(Screen.getMouseRobot(), stdAutoDelay, "MouseDown: WaitForIdle: %s - Delay: %d");
    Screen.getMouseRobot().mousePress(buttons);
    delay(10);
    logRobot(stdMaxElapsed, "MouseDown: extended delay: %d");
  }

  private void doMouseUp(int buttons) {
    logRobot(Screen.getMouseRobot(), stdAutoDelay, "MouseUp: WaitForIdle: %s - Delay: %d");
    Screen.getMouseRobot().mouseRelease(buttons);
    delay(10);
    logRobot(stdMaxElapsed, "MouseUp: extended delay: %d");
  }

  private void doMouseMove(int x, int y) {
    Screen.getMouseRobot().mouseMove(x, y);
  }

  @Override
  public void clickStarts() {
  }

  @Override
  public void clickEnds() {
  }

  @Override
  public void delay(int ms) {
    if (ms < 0) {
      return;
    }
    while (ms > MAX_DELAY) {
      super.delay(MAX_DELAY);
      ms -= MAX_DELAY;
    }
    super.delay(ms);
  }

  @Override
  public ScreenImage captureScreen(Rectangle rect) {
    Rectangle s = scr.getBounds();
//    rect.translate(-s.x, -s.y);
    BufferedImage img = createScreenCapture(rect);
    Debug.log(3, "RobotDesktop: captureScreen: on %d using %s", scr.getID(), rect);
    return new ScreenImage(rect, img);
  }

  @Override
  public Color getColorAt(int x, int y) {
    return getPixelColor(x, y);
  }

  private void doKeyPress(int keyCode) {
    logRobot(Screen.getMouseRobot(), stdAutoDelay, "KeyPress: WaitForIdle: %s - Delay: %d");
    keyPress(keyCode);
    logRobot(stdMaxElapsed, "KeyPress: extended delay: %d");
  }
  
  private void doKeyRelease(int keyCode) {
    logRobot(Screen.getMouseRobot(), stdAutoDelay, "KeyRelease: WaitForIdle: %s - Delay: %d");
    keyRelease(keyCode);
    logRobot(stdMaxElapsed, "KeyRelease: extended delay: %d");
  }
  
  @Override
  public void pressModifiers(int modifiers) {
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      doKeyPress(KeyEvent.VK_SHIFT);
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      doKeyPress(KeyEvent.VK_CONTROL);
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      doKeyPress(KeyEvent.VK_ALT);
    }
    if ((modifiers & KeyModifier.META) != 0) {
      if (Settings.isWindows()) {
        doKeyPress(KeyEvent.VK_WINDOWS);
      } else {
        doKeyPress(KeyEvent.VK_META);
      }
    }
  }

  @Override
  public void releaseModifiers(int modifiers) {
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      doKeyRelease(KeyEvent.VK_SHIFT);
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      doKeyRelease(KeyEvent.VK_CONTROL);
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      doKeyRelease(KeyEvent.VK_ALT);
    }
    if ((modifiers & KeyModifier.META) != 0) {
      if (Settings.isWindows()) {
        doKeyRelease(KeyEvent.VK_WINDOWS);
      } else {
        doKeyRelease(KeyEvent.VK_META);
      }
    }
  }

  @Override
  public void keyDown(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        if (heldKeys.indexOf(keys.charAt(i)) == -1) {
          Debug.log(4, "press: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.PRESS_ONLY);
          heldKeys += keys.charAt(i);
        }
      }
    }
  }

  @Override
  public void keyDown(int code) {
    if (!heldKeyCodes.contains(code)) {
      doKeyPress(code);
      heldKeyCodes.add(code);
    }
  }

  @Override
  public void keyUp(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        int pos;
        if ((pos = heldKeys.indexOf(keys.charAt(i))) != -1) {
          Debug.log(4, "release: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.RELEASE_ONLY);
          heldKeys = heldKeys.substring(0, pos)
                  + heldKeys.substring(pos + 1);
        }
      }
    }
  }

  @Override
  public void keyUp(int code) {
    if (heldKeyCodes.contains(code)) {
      doKeyRelease(code);
      heldKeyCodes.remove((Object) code);
    }
  }

  @Override
  public void keyUp() {
    keyUp(heldKeys);
    for (int code : heldKeyCodes) {
      keyUp(code);
    }
  }

  private void doType(KeyMode mode, int... keyCodes) {
    if (mode == KeyMode.PRESS_ONLY) {
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyPress(keyCodes[i]);
      }
    } else if (mode == KeyMode.RELEASE_ONLY) {
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyRelease(keyCodes[i]);
      }
    } else {
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyPress(keyCodes[i]);
      }
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyRelease(keyCodes[i]);
      }
    }
  }

  @Override
  public void typeChar(char character, KeyMode mode) {
    Debug.log(4, "Robot: doType: %s ( %d )",
            KeyEvent.getKeyText(Key.toJavaKeyCode(character)[0]).toString(),
            Key.toJavaKeyCode(character)[0]);
    doType(mode, Key.toJavaKeyCode(character));
  }

  @Override
  public void typeKey(int key) {
    Debug.log(4, "Robot: doType: %s ( %d )", KeyEvent.getKeyText(key), key);
    if (Settings.isMac()) {
      if (key == Key.toJavaKeyCodeFromText("#N.")) {
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#C."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#C."));
        return;
      } else if (key == Key.toJavaKeyCodeFromText("#T.")) {
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#C."));
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#C."));
        return;
      } else if (key == Key.toJavaKeyCodeFromText("#X.")) {
        key = Key.toJavaKeyCodeFromText("#T.");
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#A."));
        return;
      }
    }
    doType(KeyMode.PRESS_RELEASE, key);
  }

  @Override
  public void typeStarts() {
  }

  @Override
  public void typeEnds() {
  }

  @Override
  public void cleanup() {
  }
}
