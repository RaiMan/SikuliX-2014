/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.awt.Window;

public class WinUtil implements OSUtil {

  @Override
  public String getLibName() {
    return "WinUtil";
  }

  @Override
  public int open(String appName) {
    return openApp(appName);
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
  public int close(String appName) {
    return closeApp(appName);
  }

  @Override
  public int close(int pid) {
    return closeApp(pid);
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
