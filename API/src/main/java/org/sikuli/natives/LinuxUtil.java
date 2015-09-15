/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.natives;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.sikuli.script.App;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LinuxUtil implements OSUtil {

	private static boolean wmctrlAvail = true;
	private static boolean xdoToolAvail = true;

	@Override
    public void checkLibAvailability() {
      List<CommandLine> commands = Arrays.asList(
              CommandLine.parse("wmctrl -m"),
              CommandLine.parse("xdotool version"),
              CommandLine.parse("killall --version")
      );
      for (CommandLine cmd : commands) {
        try {
          DefaultExecutor executor = new DefaultExecutor();
          executor.setExitValue(0);
          //suppress system output
          executor.setStreamHandler(new PumpStreamHandler(null));
          executor.execute(cmd);
        } catch (IOException e) {
          String executable = cmd.toStrings()[0];
          if (executable.equals("wmctrl")) {
            wmctrlAvail = false;
          }
          if (executable.equals("xdotool")) {
            xdoToolAvail = false;
          }
          throw new NativeCommandException("[error] checking: command '" + executable + "' is not executable, please check if it is installed and available!");
        }
      }
    }

	private boolean wmctrlIsAvail(String f) {
		if (wmctrlAvail) {
			return true;
		}
		System.out.println("[error] " + f + ": wmctrl not available or not working");
		return false;
	}

  @Override
  public App.AppEntry getApp(int appPID, String appName) {
    return new App.AppEntry(appName, "" + appPID, "", "", "");
  }
  
  @Override
  public int isRunning(App.AppEntry app) {
    return -1;
  }

  @Override
  public int open(String appName) {
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
  public int open(App.AppEntry app) {
    return open(app.execName);
  }

  @Override
  public int switchto(String appName, int winNum) {
		if (!wmctrlIsAvail("switchApp")) {
			return -1;
		}
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
  public int switchto(String appName) {
    return switchto(appName, 0);
  }

  @Override
  public int switchto(App.AppEntry app, int num) {
    return switchto(app.execName, num);
  }
  @Override
  public int close(String appName) {
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

  @Override
  public int close(App.AppEntry app) {
    return close(app.execName);
  }

  @Override
  public Map<Integer, String[]> getApps(String name) {
    return null;
  }

  private enum SearchType {

    APP_NAME,
    WINDOW_ID,
    PID
  };

  @Override
  public Rectangle getFocusedWindow() {
		if (!xdoToolAvail) {
			System.out.println("[error] getFocusedWindow: xdotool not available or not working");
			return null;
		}
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
  public int close(int pid) {
		if (!wmctrlIsAvail("closeApp")) {
			return -1;
		}
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
  public int switchto(int pid, int num) {
		if (!wmctrlIsAvail("switchApp")) {
			return -1;
		}
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
