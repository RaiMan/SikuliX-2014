/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.awt.Window;

public interface OSUtil {
  // Windows: returns PID, 0 if fails
  // Others: return 0 if succeeds, -1 if fails

	public String getLibName();
	
	public int open(String appName);

  // Windows: returns PID, 0 if fails
  // Others: return 0 if succeeds, -1 if fails
  public int switchto(String appName);

  public int switchto(String appName, int winNum);

  //internal use
  public int switchto(int pid, int num);

  // returns 0 if succeeds, -1 if fails
  public int close(String appName);

  //internal use
  public int close(int pid);

  public Rectangle getWindow(String appName);

  public Rectangle getWindow(String appName, int winNum);

  Rectangle getWindow(int pid);

  Rectangle getWindow(int pid, int winNum);

  public Rectangle getFocusedWindow();

  public void bringWindowToFront(Window win, boolean ignoreMouse);
}
