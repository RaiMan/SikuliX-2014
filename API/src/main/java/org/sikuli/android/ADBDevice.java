/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.script.RunTime;
import org.sikuli.script.ScreenImage;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADBDevice {

  private static int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "ADBDevice: " + message, args);
  }

  private JadbDevice device = null;
  private ADBRobot robot = null;
  private ADBScreen screen = null;

  private static ADBDevice adbDevice = null;

  private ADBDevice() {
  }

  public static ADBDevice get() {
    if (adbDevice == null) {
      adbDevice = new ADBDevice();
      adbDevice.device = ADBClient.getDevice();
      if (adbDevice.device == null) {
        adbDevice = null;
      }
    }
    return adbDevice;
  }

  public ADBRobot getRobot(ADBScreen screen) {
    if (robot == null) {
      this.screen = screen;
      robot = new ADBRobot(screen, this);
    }
    return robot;
  }

  public String getDeviceSerial() {
    return device.getSerial();
  }

  public Rectangle getBounds() {
//    BufferedImage bimg = captureDeviceScreen();
//    return new Rectangle(0, 0, bimg.getWidth(), bimg.getHeight());
    Dimension dim = getDisplayDimension();
    return new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight());
  }

  public ScreenImage captureScreen(Rectangle screenRect) {
    BufferedImage bimg = captureDeviceScreen();
    BufferedImage subBimg = bimg.getSubimage(screenRect.x, screenRect.y, screenRect.width, screenRect.height);
    return new ScreenImage(screenRect, subBimg);
  }

  private BufferedImage captureDeviceScreen() {
    BufferedImage bImg = null;
    Debug timer = Debug.startTimer();
    try {
      InputStream stdout = device.executeShell("screencap", "-p");
      bImg = ImageIO.read(stdout);
      log(lvl, "capture: %d", timer.end());
    } catch (IOException | JadbException e) {
      log(-1, "capture: %s", e);
    }
    return bImg;
  }

  public Boolean isDisplayOn() {
    String dump = dumpsys("power");
    Pattern displayOn = Pattern.compile("Display Power: state=(..)");
    Matcher match = displayOn.matcher(dump);
    if (match.find()) {
      if (match.group(1).contains("ON")) {
        return true;
      }
      return false;
    } else {
      String token = "Display Power: state=OFF";
      log(-1, "isDisplayOn: dumpsys power: token not found: %s", token);
    }
    return null;
  }

  private Dimension getDisplayDimension() {
    String dump = dumpsys("display");
    Dimension dim = null;
    Pattern displayDimension = Pattern.compile("mDefaultViewport=.*?deviceWidth=(\\d*).*?deviceHeight=(\\d*)");
    Matcher match = displayDimension.matcher(dump);
    if (match.find()) {
      int w = Integer.parseInt(match.group(1));
      int h = Integer.parseInt(match.group(2));
      dim = new Dimension(w, h);
    } else {
      String token = "mDefaultViewport= ... deviceWidth=1200, deviceHeight=1920}";
      log(-1, "getDisplayDimension: dumpsys display: token not found: %s", token);
    }
    return dim;
  }

  public String dumpsys(String component) {
    InputStream stdout = null;
    String out = "";
    try {
      if (component == null || component.isEmpty()) {
        component = "power";
      }
      stdout = device.executeShell("dumpsys", component);
      out = inputStreamToString(stdout, "UTF-8");
      //log(lvl, "dumpsys: %s\n%s", component, out);
    } catch (IOException | JadbException e) {
      log(-1, "dumpsys: %s: %s", component, e);
    }
    return out;
  }

  private static final int BUFFER_SIZE = 4 * 1024;

  private static String inputStreamToString(InputStream inputStream, String charsetName) {
    StringBuilder builder = new StringBuilder();
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(inputStream, charsetName);
      char[] buffer = new char[BUFFER_SIZE];
      int length;
      while ((length = reader.read(buffer)) != -1) {
        builder.append(buffer, 0, length);
      }
      return builder.toString();
    } catch (Exception e) {
      return "";
    }
  }

  public void wakeUp(int seconds) {
    int times = seconds * 4;
    try {
      device.executeShell("input", "keyevent", "26");
      while (0 < times--) {
        if (isDisplayOn()) {
          return;
        } else {
          RunTime.pause(0.25f);
        }
      }
    } catch (Exception e) {
      log(-1, "wakeUp: did not work: %s", e);
    }
    log(-1, "wakeUp: timeout: %d seconds", seconds);
  }

  public void inputKeyEvent(int key) {
    try {
      device.executeShell("input", "keyevent", Integer.toString(key));
    } catch (Exception e) {
      log(-1, "inputKeyEvent: %d did not work: %s", e.getMessage());
    }
  }

  public void tap(int x, int y) {
    try {
      device.executeShell("input tap", Integer.toString(x), Integer.toString(y));
    } catch (IOException | JadbException e) {
      log(-1, "tap: %s", e);
    }
  }

  public void swipe(int x1, int y1, int x2, int y2) {
    try {
      device.executeShell("input swipe", Integer.toString(x1), Integer.toString(y1),
              Integer.toString(x2), Integer.toString(y2));
    } catch (IOException | JadbException e) {
      log(-1, "swipe: %s", e);
    }
  }
}
