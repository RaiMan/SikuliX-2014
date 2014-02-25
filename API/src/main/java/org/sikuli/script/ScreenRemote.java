/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

/**
 * UNDER DEVELOPMENT - EXPERIMENTAL
 * an extension to the DesktopScreen implementation Screen,
 * that allows to redirect the actions to a remote screen/keyboard/mouse
 * in conjunction with the package SikuliX-Remote running on the remote system
 */
public class ScreenRemote extends Screen implements IScreen{

  private ObjectInputStream in = null;
  private OutputStreamWriter out;
  private static Socket socket = null;
  private boolean socketValid;
  private int rw, rh;
  private RobotRemote rrobot;
//  private boolean isRemoteScreen;
  private String system = "";
  private int numberScreens = 0;
  private int curID;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", "ScreenRemote: " + message, args);
  }

  private static void log(String message, Object... args) {
    log(3, message, args);
  }

  public ScreenRemote(String adr, String p) {
    init(adr, p);
  }

  private void init(String adr, String p) {
    socketValid = true;
    rrobot = null;
    setOtherScreen();
    String ip = FileManager.getAddress(adr);
    int port = FileManager.getPort(p);
    if (ip == null || port < 0) {
      log(-1, "fatal: not valid: " + adr + " / " + p);
      System.exit(1);
    }
    try {
      socket = new Socket(ip, port);
    } catch (Exception ex) {
      log(-1, "fatal: no connection: " + adr + " / " + p);
      socketValid = false;
    }
    try {
      if (socketValid) {
        in = new ObjectInputStream(socket.getInputStream());
        out = new OutputStreamWriter(socket.getOutputStream());
        log("connection at: " + socket);
      }
    } catch (Exception ex) {
      log(-1, "fatal: problem starting pipes:\n", ex.getMessage());
      socketValid = false;
    }
    if (socketValid) {
      rrobot = new RobotRemote(this);
      numberScreens = rrobot.getNumberScreens();
      system = rrobot.getSystem();
      log("RobotRemote: System: %s NumberScreens: %d", system, numberScreens);
      Rectangle r = rrobot.getBounds();
      rw = r.width;
      rh = r.height;
      setX(0);
      setY(0);
      setW(rw);
      setH(rh);
    }
  }

  @Override
  public String toString() {
    return String.format("S(R-%d)[%dx%d]", curID, rw, rh);
  }

  @Override
  public String toStringShort() {
    return toString();
  }

  public boolean isValid() {
    return (socketValid && socket != null);
  }

  public ObjectInputStream getIn() {
    return in;
  }

  public OutputStreamWriter getOut() {
    return out;
  }

  public boolean close(boolean stopServer) {
    if (rrobot != null) {
      rrobot.cleanup();
    }
    if (socket != null) {
      try {
        if (stopServer) {
          rrobot.send("EXIT STOP");
        } else {
          rrobot.send("EXIT");
        }
        socket.close();
      } catch (IOException ex) {
        log(-1, "fatal: not closeable: %s\n" + ex.getMessage(), socket);
        return false;
      }
    }
    socket = null;
    socketValid = false;
    rrobot = null;
    return true;
  }

  public boolean close() {
    return close(false);
  }

  @Override
  public IRobot getRobot() {
    return rrobot;
  }

  @Override
  public Rectangle getBounds() {
    return new Rectangle(0, 0, rw, rh);
  }

  @Override
  public ScreenImage capture() {
    return capture(0, 0, rw, rh);
  }

  @Override
  public ScreenImage capture(int x, int y, int w, int h) {
    return rrobot.captureScreen(new Rectangle(x, y, w, h));
  }

  @Override
  public ScreenImage capture(Rectangle r) {
    return capture(r.x, r.y, r.width, r.height);
  }

  @Override
  public ScreenImage capture(Region r) {
    return capture(r.x, r.y, r.w, r.h);
  }

  @Override
  public Location newLocation(Location loc) {
    return loc.setOtherScreen(this);
  }

  public Location newLocation(int x, int y) {
    return new Location(x, y).setOtherScreen(this);
  }

  @Override
  public Region newRegion (Location loc, int w, int h) {
    return new Region(loc.x, loc.y, w, h, loc.getScreen());
  }

  public Region newRegion (int x, int y, int w, int h) {
    return new Region(x, y, w, h, this);
  }

  public Region newRegion (Region r) {
    return new Region(r.x, r.y, r.w, r.h, this);
  }

  public Region newRegion (Rectangle r) {
    return new Region(r.x, r.y, r.width, r.height, this);
  }

  public Location mousePointer() {
    Location loc = rrobot.mousePointer();
    if (loc != null) {
      return loc.setOtherScreen(this);
    } else {
      return null;
    }
  }
}
