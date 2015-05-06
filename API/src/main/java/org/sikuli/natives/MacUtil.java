/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.awt.Window;
import javax.swing.JOptionPane;

public class MacUtil implements OSUtil {

  private static boolean _askedToEnableAX = false;
  private String usedFeature;

	@Override
	public String getLibName() {
		return "MacUtil";
	}

  @Override
  public int open(String appName) {
    if (_openApp(appName)) {
      return 0;
    }
    return -1;
  }

  @Override
  public int switchto(String appName) {
    return open(appName);
  }

  @Override
  public int switchto(int pid, int num) {
    return -1;
  }

  // ignore winNum on Mac
  @Override
  public int switchto(String appName, int winNum) {
    return open(appName);
  }

  @Override
  public int close(String appName) {
    try {
      String cmd[] = {"sh", "-c",
        "ps aux |  grep \"" + appName + "\" | awk '{print $2}' | xargs kill"};
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      return -1;
    }
  }

  @Override
  public int close(int pid) {
    return -1;
  }

  private void checkAxEnabled(String name) {
    if (!System.getProperty("os.name").toLowerCase().startsWith("mac")) {
      return;
    }
    if (Integer.parseInt(System.getProperty("os.version").replace(".", "")) > 108 && !isAxEnabled()) {
      if (name == null) {
        JOptionPane.showMessageDialog(null,
                "This app uses Sikuli feature " + usedFeature + ", which needs\n"
                + "access to the Mac's assistive device support.\n"
                + "You have to explicitly allow this in the System Preferences.\n"
                + "(System Preferences -> Security & Privacy -> Privacy)\n"
                + "Currently we cannot do this for you.\n\n"
                + "Be prepared to get some crash after clicking ok.\n"
                + "Please check the System Preferences and come back.",
                "SikuliX on Mac Mavericks Special", JOptionPane.PLAIN_MESSAGE);
        System.out.println("[error] MacUtil: on Mavericks: no access to assistive device support");
      }
      usedFeature = name;
      return;
    }
    if (!isAxEnabled()) {
      if (_askedToEnableAX) {
        return;
      }
      int ret = JOptionPane.showConfirmDialog(null,
              "You need to enable Accessibility API to use the function \""
              + name + "\".\n"
              + "Should I open te System Preferences for you?",
              "Accessibility API not enabled",
              JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
      if (ret == JOptionPane.YES_OPTION) {
        openAxSetting();
        JOptionPane.showMessageDialog(null,
                "Check \"Enable access for assistant devices\""
                + "in the System Preferences\n and then close this dialog.",
                "Enable Accessibility API", JOptionPane.INFORMATION_MESSAGE);
      }
      _askedToEnableAX = true;
    }
  }

//Mac Mavericks: delete app entry from list - in terminal on one line
//sudo sqlite3 /Library/Application\ Support/com.apple.TCC/Tcc.db
//'delete from access where client like "%part of app name%"'

  @Override
  public Rectangle getWindow(String appName, int winNum) {
    checkAxEnabled("getWindow");
    int pid = getPID(appName);
    return getWindow(pid, winNum);
  }

  @Override
  public Rectangle getWindow(String appName) {
    return getWindow(appName, 0);
  }

  @Override
  public Rectangle getWindow(int pid) {
    return getWindow(pid, 0);
  }

  @Override
  public Rectangle getWindow(int pid, int winNum) {
    Rectangle rect = getRegion(pid, winNum);
    checkAxEnabled(null);
    return rect;
  }

  @Override
  public Rectangle getFocusedWindow() {
    checkAxEnabled("getFocusedWindow");
    Rectangle rect = getFocusedRegion();
    checkAxEnabled(null);
    return rect;
  }

  @Override
  public native void bringWindowToFront(Window win, boolean ignoreMouse);

  public static native boolean _openApp(String appName);

  public static native int getPID(String appName);

  public static native Rectangle getRegion(int pid, int winNum);

  public static native Rectangle getFocusedRegion();

  public static native boolean isAxEnabled();

  public static native void openAxSetting();
}
