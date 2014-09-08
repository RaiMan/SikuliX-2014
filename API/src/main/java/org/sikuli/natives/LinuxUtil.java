/*
 * Copyright 2010-2014, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.natives;

import org.sikuli.natives.OSUtil;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.*;

public class LinuxUtil implements OSUtil {

	@Override
	public String getLibName() {
		return "";
	}

  @Override
  public int switchApp(String appName, int winNum) {
    try {
      String cmd[] = {"wmctrl", "-a", appName};
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] switchApp:\n" + e.getMessage());
      return -1;
    }
  }

  @Override
  public int switchApp(String appName) {
    return switchApp(appName, 0);
  }

  @Override
  public int openApp(String appName) {
    try {
      String cmd[] = {"sh", "-c", "(" + appName + ") &\necho -n $!"};
      Process p = Runtime.getRuntime().exec(cmd);

      InputStream in = p.getInputStream();
      byte pidBytes[] = new byte[64];
      int len = in.read(pidBytes);
      String pidStr = new String(pidBytes, 0, len);
      int pid = Integer.parseInt(new String(pidStr));
      p.waitFor();
      return pid;
      //return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] openApp:\n" + e.getMessage());
      return 0;
    }
  }

  @Override
  public int closeApp(String appName) {
    try {
      String cmd[] = {"killall", appName};
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] closeApp:\n" + e.getMessage());
      return -1;
    }
  }

  private enum SearchType {

    APP_NAME,
    WINDOW_ID,
    PID
  };

  @Override
  public Rectangle getFocusedWindow() {
    String cmd[] = {"xdotool", "getactivewindow"};
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      InputStream in = p.getInputStream();
      BufferedReader bufin = new BufferedReader(new InputStreamReader(in));
      String str = bufin.readLine();
      long id = Integer.parseInt(str);
      String hexid = String.format("0x%08x", id);
      return findRegion(hexid, 0, SearchType.WINDOW_ID);
    } catch (IOException e) {
      System.out.println("[error] getFocusedWindow:\n" + e.getMessage());
      return null;
    }
  }

  @Override
  public Rectangle getWindow(String appName) {
    return getWindow(appName, 0);
  }

  private Rectangle findRegion(String appName, int winNum, SearchType type) {
    String[] winLine = findWindow(appName, winNum, type);
    if (winLine.length >= 7) {
      int x = new Integer(winLine[3]);
      int y = Integer.parseInt(winLine[4]);
      int w = Integer.parseInt(winLine[5]);
      int h = Integer.parseInt(winLine[6]);
      return new Rectangle(x, y, w, h);
    }
    return null;
  }

  private String[] findWindow(String appName, int winNum, SearchType type) {
    //Debug.log("findWindow: " + appName + " " + winNum + " " + type);
    String[] found = {};
    int numFound = 0;
    try {
      String cmd[] = {"wmctrl", "-lpGx"};
      Process p = Runtime.getRuntime().exec(cmd);
      InputStream in = p.getInputStream();
      BufferedReader bufin = new BufferedReader(new InputStreamReader(in));
      String str;

      int slash = appName.lastIndexOf("/");
      if (slash >= 0) {
        // remove path: /usr/bin/....
        appName = appName.substring(slash + 1);
      }

      if (type == SearchType.APP_NAME) {
        appName = appName.toLowerCase();
      }
      while ((str = bufin.readLine()) != null) {
        //Debug.log("read: " + str);
        String winLine[] = str.split("\\s+");
        boolean ok = false;

        if (type == SearchType.WINDOW_ID) {
          if (appName.equals(winLine[0])) {
            ok = true;
          }
        } else if (type == SearchType.PID) {
          if (appName.equals(winLine[2])) {
            ok = true;
          }
        } else if (type == SearchType.APP_NAME) {
          String pidFile = "/proc/" + winLine[2] + "/status";
          char buf[] = new char[1024];
          FileReader pidReader = null;
          try {
            pidReader = new FileReader(pidFile);
            pidReader.read(buf);
            String pidName = new String(buf);
            String nameLine[] = pidName.split("[:\n]");
            String name = nameLine[1].trim();
            if (name.equals(appName)) {
              ok = true;
            }

          } catch (FileNotFoundException e) {
            // pid killed before we could read /proc/
          } finally {
            if (pidReader != null) {
              pidReader.close();
            }
          }

          if (!ok && winLine[7].toLowerCase().indexOf(appName) >= 0) {
            ok = true;
          }
        }

        if (ok) {
          if (numFound >= winNum) {
            //Debug.log("Found window" + winLine);
            found = winLine;
            break;
          }
          numFound++;
        }
      }
      in.close();
      p.waitFor();
    } catch (Exception e) {
      System.out.println("[error] findWindow:\n" + e.getMessage());
      return null;
    }
    return found;
  }

  @Override
  public Rectangle getWindow(String appName, int winNum) {
    return findRegion(appName, winNum, SearchType.APP_NAME);
  }

  @Override
  public Rectangle getWindow(int pid) {
    return getWindow(pid, 0);
  }

  @Override
  public Rectangle getWindow(int pid, int winNum) {
    return findRegion("" + pid, winNum, SearchType.PID);
  }

  @Override
  public int closeApp(int pid) {
    String winLine[] = findWindow("" + pid, 0, SearchType.PID);
    if (winLine == null) {
      return -1;
    }
    String cmd[] = {"wmctrl", "-ic", winLine[0]};
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] closeApp:\n" + e.getMessage());
      return -1;
    }
  }

  @Override
  public int switchApp(int pid, int num) {
    String winLine[] = findWindow("" + pid, num, SearchType.PID);
    if (winLine == null) {
      return -1;
    }
    String cmd[] = {"wmctrl", "-ia", winLine[0]};
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] switchApp:\n" + e.getMessage());
      return -1;
    }
  }

  @Override
  public void bringWindowToFront(Window win, boolean ignoreMouse) {
  }
}
