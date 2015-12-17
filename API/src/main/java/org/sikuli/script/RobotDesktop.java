/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.*;
import org.sikuli.script.keyboard.KeyPress;
import org.sikuli.script.keyboard.KeyboardDelegator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * INTERNAL USE Implementation of IRobot making a DesktopRobot using java.awt.Robot
 */
public class RobotDesktop extends Robot implements IRobot {

  final static int MAX_DELAY = 60000;
  private static final ArrayList<Integer> heldKeyCodes = new ArrayList<Integer>();
  public static int stdAutoDelay = 0;
  public static int stdDelay = 10;
  public static int stdMaxElapsed = 1000;
  private static int heldButtons = 0;
  private static String heldKeys = "";
  private static RunTime runTime = RunTime.get();
  private static boolean alwaysNewRobot = false;
  private Screen scr = null;
  private long start;

  public RobotDesktop(Screen screen) throws AWTException {
    super(runTime.getGraphicsDevice(screen.getcurrentID()));
    scr = screen;
  }

  public RobotDesktop() throws AWTException {
    super();
    setAutoDelay(stdAutoDelay);
    setAutoWaitForIdle(false);
  }

  private void logRobot(int delay, String msg) {
    start = new Date().getTime();
    int theDelay = getAutoDelay();
    if (theDelay > 0 && theDelay > delay) {
      Debug.log(0, msg, isAutoWaitForIdle(), theDelay);
    }
  }

  private void logRobot(String msg, int maxElapsed) {
    long elapsed = new Date().getTime() - start;
    if (elapsed > maxElapsed) {
      Debug.log(0, msg, elapsed);
      setAutoDelay(stdAutoDelay);
      setAutoWaitForIdle(false);
    }
  }

  private void doMouseMove(int x, int y) {
    mouseMove(x, y);
  }

  private void fakeHighlight(boolean onOff) {
    if (runTime.runningMac && runTime.isOSX10()) {
      Region reg = Screen.getFakeRegion();
      reg.silentHighlight(onOff);
    }
  }

  private void doMouseDown(int buttons) {
    fakeHighlight(true);
    logRobot(stdAutoDelay, "MouseDown: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    setAutoWaitForIdle(false);
    delay(100);
    fakeHighlight(false);
    delay(100);
    mousePress(buttons);
    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("MouseDown: extended delay: %d", stdMaxElapsed);
  }

  private void doMouseUp(int buttons) {
    logRobot(stdAutoDelay, "MouseUp: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    setAutoWaitForIdle(false);
    mouseRelease(buttons);
    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("MouseUp: extended delay: %d", stdMaxElapsed);
  }

  private void doKeyPress(KeyPress keyPress) {
      logRobot(stdAutoDelay, "KeyPress: WaitForIdle: %s - Delay: %d");
      setAutoDelay(stdAutoDelay);
      setAutoWaitForIdle(false);
      //press all modifiers
      keyPressSequence(keyPress.getModifiers());
      //then press all keys
      keyPressSequence(keyPress.getKeys());
      if (stdAutoDelay == 0) {
          delay(stdDelay);
      }
      logRobot("KeyPress: extended delay: %d", stdMaxElapsed);
  }

  /**
   * Press a sequence of keys in one action, to enable multiple equal key inputs at the same time.
   * @param keys arrays of int values of {@link KeyEvent}
   */
  private void keyPressSequence(int... keys) {
      Set<Integer> pressed = new HashSet<Integer>();
      if (keys != null) {
          for (int key : keys) {
              if (pressed.contains(key)) {
                  //release keys to be able to typ unicode numbers with equal numbers in one action
                  Debug.log(4,"RELEASE_KEY: " + KeyEvent.getKeyText(key));
                  keyRelease(key);
              }
              Debug.log(4, "PRESS_KEY: " + KeyEvent.getKeyText(key));
              keyPress(key);
              pressed.add(key);
          }
      }
  }

  private void doKeyRelease(KeyPress keyPress) {
      logRobot(stdAutoDelay, "KeyRelease: WaitForIdle: %s - Delay: %d");
      setAutoDelay(stdAutoDelay);
      setAutoWaitForIdle(false);
      //release all keys
      keyReleaseSequence(keyPress.getKeys());
      //then release all modifiers
      keyReleaseSequence(keyPress.getModifiers());
      if (stdAutoDelay == 0) {
          delay(stdDelay);
      }
      logRobot("KeyRelease: extended delay: %d", stdMaxElapsed);
  }

  /**
   *  Release a sequence of keys in his reverse order.
   *
   * @param keys arrays of int values of {@link KeyEvent}
   */
  private void keyReleaseSequence(int[] keys) {
      if(keys != null){
          for (int i = keys.length - 1; i >= 0; i--) {
              Debug.log(4, "RELEASE_KEY: " + KeyEvent.getKeyText(keys[i]));
              keyRelease(keys[i]);
          }
      }
  }

  private Robot getRobot() {
    return null;
  }

  @Override
  public boolean isRemote() {
    return false;
  }

  @Override
  public Screen getScreen() {
    return scr;
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
      waitForIdle();
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
//    Rectangle s = scr.getBounds();
    Rectangle cRect = new Rectangle(rect);
//    cRect.translate(-s.x, -s.y);
    BufferedImage img = createScreenCapture(rect);
    Debug.log(4, "RobotDesktop: captureScreen: [%d,%d, %dx%d]",
            rect.x, rect.y, rect.width, rect.height);
    return new ScreenImage(rect, img);
  }

  @Override
  public Color getColorAt(int x, int y) {
    return getPixelColor(x, y);
  }

  @Override
  public void pressModifiers(int modifiers) {
        char key = KeyModifier.lookUpKey(modifiers);
    if (key > 0) {
      doKeyPress(KeyboardDelegator.toJavaKeyCode(key));
    }
  }

  @Override
  public void releaseModifiers(int modifiers) {
    char key = KeyModifier.lookUpKey(modifiers);
    if (key > 0) {
      doKeyRelease(KeyboardDelegator.toJavaKeyCode(key));
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
      doKeyPress(new KeyPress(code));
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
      doKeyRelease(new KeyPress(code));
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

  private void doType(KeyMode mode, KeyPress keyPress) {
      switch (mode) {
          case PRESS_ONLY:
              doKeyPress(keyPress);
              break;
          case RELEASE_ONLY:
              doKeyPress(keyPress);
              break;
          case PRESS_RELEASE:
              doKeyPress(keyPress);
              doKeyRelease(keyPress);
              break;
      }
  }

  @Override
  public void typeChar(char character, KeyMode mode) {
      KeyPress keyPress = KeyboardDelegator.toJavaKeyCode(character);
      Debug.log(3, "Robot: doType: %s %s",character, keyPress);
      doType(mode, keyPress);
  }

  @Override
  public void typeKey(int key) {
    Debug.log(4, "Robot: doType: %s ( %d )", KeyEvent.getKeyText(key), key);
    if (Settings.isMac()) {
        if (key == Key.toJavaKeyCodeFromText("#N.")) {
            doType(KeyMode.PRESS_ONLY, Key.toJavaKeyPressCodeFromText("#C."));
            doType(KeyMode.PRESS_RELEASE, new KeyPress(key));
            doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyPressCodeFromText("#C."));
            return;
        } else if (key == Key.toJavaKeyCodeFromText("#T.")) {
            doType(KeyMode.PRESS_ONLY, Key.toJavaKeyPressCodeFromText("#C."));
            doType(KeyMode.PRESS_ONLY, Key.toJavaKeyPressCodeFromText("#A."));
            doType(KeyMode.PRESS_RELEASE, new KeyPress(key));
            doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyPressCodeFromText("#A."));
            doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyPressCodeFromText("#C."));
            return;
        } else if (key == Key.toJavaKeyCodeFromText("#X.")) {
            key = Key.toJavaKeyCodeFromText("#T.");
            doType(KeyMode.PRESS_ONLY, Key.toJavaKeyPressCodeFromText("#A."));
            doType(KeyMode.PRESS_RELEASE, new KeyPress(key));
            doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyPressCodeFromText("#A."));
            return;
        }
    }
    doType(KeyMode.PRESS_RELEASE, new KeyPress(key));
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
