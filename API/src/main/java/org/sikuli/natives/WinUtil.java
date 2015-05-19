/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import org.sikuli.basics.Debug;
import org.sikuli.script.App;
import org.sikuli.script.Key;
import org.sikuli.script.RunTime;
import org.sikuli.script.Screen;

public class WinUtil implements OSUtil {

  @Override
  public String getLibName() {
    return "WinUtil";
  }

  @Override
  public App.AppEntry getApp(Object filter) {
    App.AppEntry app = null;
    String name = "";
    String execName = "";
    String options = "";
    int pid = -1;
    String[] parts;
    if (filter instanceof String) {
      name = (String) filter;
      if (name.startsWith("\"")) {
        parts = name.substring(1).split("\"");
        if (parts.length > 1) {
          options = name.substring(parts[0].length() + 3);
          name = "\"" + parts[0] +  "\"";
        }
      } else {
        parts = name.split(" ");
        if (parts.length > 1) {
          options = name.substring(parts[0].length() + 1);
          name = parts[0];
        }
      }
      if (name.startsWith("\"")) {
        execName = new File(name.substring(1, name.length()-1)).getName().toUpperCase();
      } else {
        execName = new File(name).getName().toUpperCase();
      }
    } else if (filter instanceof Integer) {
      pid = (Integer) filter;
    } else {
      return app;
    }
    String cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\"";
    String result = RunTime.get().runcmd(cmd);
    String[] lines = result.split("\r\n");
    if ("0".equals(lines[0].trim())) {
      for (int nl = 1; nl < lines.length; nl++) {
        parts = lines[nl].split("\"");
        String theWindow = parts[parts.length - 1];
        String theName = parts[1];
        String thePID = parts[3];
        if (!name.isEmpty()) {
          if (theName.toUpperCase().contains(execName)
                  || theWindow.contains(name)) {
            return new App.AppEntry(theName, thePID, theWindow, "", "");
          }
        } else {
          try {
            if (Integer.parseInt(thePID) == pid) {
              return new App.AppEntry(theName, thePID, theWindow, "", "");
            }
          } catch (Exception ex) {
          }
        }
      }
    } else {
      Debug.logp(result);
    }
    if (!options.isEmpty()) {
      return new App.AppEntry(name, "", "", "", options);
    }
    return app;
  }

  @Override
  public int open(String appName) {
    return openApp(appName);
  }

  @Override
  public int open(App.AppEntry app) {
    if (app.pid > -1) {
      return switchApp(app.pid, 0);
    }
    String cmd = app.execName;
    if (!app.options.isEmpty()) {
      cmd += " " + app.options;
    }
    return openApp(cmd);
  }

  @Override
  public int switchto(String appName) {
    return switchApp(appName, 0);
  }

  @Override
  public int switchto(String appName, int winNum) {
    return switchApp(appName, winNum);
  }

  @Override
  public int switchto(int pid, int num) {
    return switchApp(pid, num);
  }

  @Override
  public int switchto(App.AppEntry app, int num) {
    if (app.window.startsWith("!")) {
      return switchto(app.window.substring(1), 0);
    }
    if (app.pid > -1) {
      return switchto(app.pid, 0);
    }
    return switchto(app.execName, num);
  }

  @Override
  public int close(String appName) {
    return closeApp(appName);
  }

  @Override
  public int close(int pid) {
    return closeApp(pid);
  }

  @Override
  public int close(App.AppEntry app) {
    if (app.window.startsWith("!")) {
      switchto(app.window.substring(1), 0);
      RunTime.pause(1);
      new Screen().type(Key.F4, Key.ALT);
    }
    if (app.pid > -1) {
      return closeApp(app.pid);
    }
    return closeApp(app.execName);
  }

  public native int switchApp(String appName, int num);

  public native int switchApp(int pid, int num);

  public native int openApp(String appName);

  public native int closeApp(String appName);

  public native int closeApp(int pid);

  @Override
  public Rectangle getWindow(String appName) {
    return getWindow(appName, 0);
  }

  @Override
  public Rectangle getWindow(int pid) {
    return getWindow(pid, 0);
  }

  @Override
  public Rectangle getWindow(String appName, int winNum) {
    long hwnd = getHwnd(appName, winNum);
    return _getWindow(hwnd, winNum);
  }

  @Override
  public Rectangle getWindow(int pid, int winNum) {
    long hwnd = getHwnd(pid, winNum);
    return _getWindow(hwnd, winNum);
  }

  @Override
  public Rectangle getFocusedWindow() {
    Rectangle rect = getFocusedRegion();
    return rect;
  }

  @Override
  public native void bringWindowToFront(Window win, boolean ignoreMouse);

  private static native long getHwnd(String appName, int winNum);

  private static native long getHwnd(int pid, int winNum);

  private static native Rectangle getRegion(long hwnd, int winNum);

  private static native Rectangle getFocusedRegion();

  private Rectangle _getWindow(long hwnd, int winNum) {
    Rectangle rect = getRegion(hwnd, winNum);
    return rect;
  }
}
