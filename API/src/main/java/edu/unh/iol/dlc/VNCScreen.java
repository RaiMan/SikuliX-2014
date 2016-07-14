/*
 *                       University of New Hampshire
 *                       InterOperability Laboratory
 *                           Copyright (c) 2014
 *
 * This software is provided by the IOL ``AS IS'' and any express or implied
 * warranties, including, but not limited to, the implied warranties of
 * merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the InterOperability Lab be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages.
 *
 * This software may not be resold without the express permission of
 * the InterOperability Lab.
 *
 * Feedback on this code may be sent to Mike Johnson (mjohnson@iol.unh.edu)
 * and dlnalab@iol.unh.edu.
 */
package edu.unh.iol.dlc;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import org.sikuli.util.*;

import org.sikuli.script.Sikulix;

/**
 * The VNCScreen is an implementation of IScreen that uses a VNCRobot to
 * control the VNC connection.
 */
public class VNCScreen extends Region implements EventObserver, IScreen {

  private static String me = "VNCScreen: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  protected static int _primaryScreen = -1;

  protected static Framebuffer[] _gdev;
  protected static Rectangle[] gdevsBounds;
  protected static ConnectionController _genv = null;
  protected static VNCScreen[] screens;
  private static VNCRobot[] mouseRobot;
  private static int waitForScreenshot = 300;

  protected Framebuffer _curGD;
  protected int _curID = 0;
  protected int oldID = 0;
  protected IRobot robot = null;
  protected boolean waitPrompt;
  protected OverlayCapturePrompt prompt;
  private ScreenImage lastScreenImage = null;
  private String promptMsg = "Select a region on the screen";

//Screen Methods**************************************************************/

  static {
    RunTime.loadLibrary("VisionProxy");
    isFake = false;
  }

  private static boolean isFake;
  private static VNCScreen fakeScreen = null;

  private String myIP = "";
  private int myPort = 0;
  private int connNum = 0;

  private static List<VNCScreen> activeScreens = new ArrayList<>();

  public static VNCScreen start(String theIP, int thePort, int theConnectTimeout, int theIOTimeout) {
    log(lvl, "VNCScreen: request for connection %s:%d", theIP, thePort);
    Socket sock = null;
    try {
      sock = new Socket();
      sock.setSoTimeout(theIOTimeout);
      sock.connect(new InetSocketAddress(theIP, thePort), theConnectTimeout * 1000);
    } catch (Exception e) {
      sock = null;
      if (e.getClass().toString().contains("SocketTimeoutException")) {
        log(-1, "VNC init: not possible to connect within %d seconds", theConnectTimeout);
      } else {
        log(-1, "VNC init socket problem: %s", e.getMessage());
      }
    }
    if (sock == null) {
      throw new UnsupportedOperationException("VNCScreen: connection not possible");
    }
    VNCScreen vnc = new VNCScreen(sock);
    if (vnc.isUsable()) {
      if (!activeScreens.contains(vnc)) {
        activeScreens.add(vnc);
      }
      vnc.myIP = theIP;
      vnc.myPort = thePort;
      log(lvl, "VNCScreen: connected %s:%d", theIP, thePort);
      return vnc;
    } else {
      //TODO if not valid (close socket?)
      return null;
    }
  }

  public boolean isUsable() {
    //TODO check useable
    return true;
  }

  public void stop() {
    if (activeScreens.contains(this)) {
      log(lvl, "VNCScreen: stopping connection to %s:%d", myIP, myPort);
      ConnectionController.getActiveController(0).closeConnection(0);
      activeScreens.remove(this);
    }
  }

  public static void cleanUp() {
    if (activeScreens.size() == 0) {
      return;
    }
    List<VNCScreen> toRemove = new ArrayList<>();
    toRemove.addAll(activeScreens);
    for (VNCScreen vnc : toRemove) {
      vnc.stop();
    }
  }

  public VNCScreen() {
    super();
    initScreens();
    _curID = _primaryScreen;
    initScreen();
    super.initScreen(this);
  }

  public VNCScreen(Socket sock) {
    super();
    ConnectionController cc = ConnectionController.getActiveController(0);
    connNum = 0;
    if (cc == null) {
      cc = new ConnectionController(sock);
    } else {
      connNum = cc.newConnection(sock);
    }
    cc.openConnection(connNum);
    cc.start(connNum);
    initScreens();
    _curID = _primaryScreen;
    initScreen();
    super.initScreen(this);
  }

  public VNCScreen(int id) {
    super();
    initScreens();
    if (id < 0 || id >= _gdev.length) {
      throw new IllegalArgumentException("VNCScreen ID " + id + " not in valid range " +
              "(between 0 and " + (_gdev.length - 1));
    }
    _curID = id;
    initScreen();
    super.initScreen(this);
  }

  //TODO prevent initScreens loop
  private VNCScreen(int id, boolean b) {
    super();
    _curID = id;
    initScreen();
    super.initScreen(this);
  }

  private static void initScreens() throws UnsupportedOperationException {

    isFake = System.getProperty("sikuli.vncfake") != null;

    if (isFake) {
      _gdev = new Framebuffer[]{null};
      gdevsBounds = new Rectangle[]{new Rectangle(0, 0, 1024, 768)};
      screens = new VNCScreen[1];
      fakeScreen = new VNCScreen(0, true);
      screens[0] = fakeScreen;
      _primaryScreen = 0;
      log(lvl, "********** running in fake mode: %s", fakeScreen);
      return;
    } else {
      _genv = ConnectionController.getActiveController(0);
      if (_genv == null) {
        throw new UnsupportedOperationException("Did not find any active ConnectionControllers.  " +
                "Cannot use VNCScreen without a ConnectionController instance.");
      }
      _gdev = (Framebuffer[]) _genv.getScreenDevices();

      gdevsBounds = new Rectangle[_gdev.length];
      screens = new VNCScreen[_gdev.length];

      if (_gdev.length == 0) {
        throw new UnsupportedOperationException("VNCScreen: initScreens: GraphicsEnvironment has no screens");
      }

      _primaryScreen = -1;

      boolean hasConnections = false;
      for (int i = 0; i < _gdev.length; i++) {
        GraphicsConfiguration defaultConfiguration = _gdev[i].getDefaultConfiguration();
        if (null == defaultConfiguration) {
          log(-1, "initScreens: %d has no connection yet", i);
          continue;
        }
        hasConnections = true;
        gdevsBounds[i] = defaultConfiguration.getBounds();

        if (gdevsBounds[i].contains(new Point(0, 0))) {
          if (_primaryScreen < 0) {
            _primaryScreen = i;
            log(lvl + 1, "initScreens: ScreenDevice %d contains (0,0) --- will be used as primary", i);
          } else {
            log(lvl + 1, "initScreens: ScreenDevice %d too contains (0,0)!", i);
          }
        }
      }
      if (hasConnections && _primaryScreen < 0) {
        log(lvl + 1, "Screen: initScreens: no ScreenDevice contains (0,0) --- using first ScreenDevice as primary");
        _primaryScreen = 0;
      }
    }

    if (_primaryScreen < 0) {
      throw new UnsupportedOperationException("initScreens: none of the remote screens has a valid connection");
    } else {
      for (int i = 0; i < screens.length; i++) {
        screens[i] = new VNCScreen(i, true);
        screens[i].initScreen();
      }
      for (int i = 0; i < _gdev.length; i++) {
        log(lvl, "%d: %s", i, screens[i].toStringShort());
      }
    }
  }

  private void initScreen() {
    setOtherScreen();
    _curGD = _gdev[_curID];
    Rectangle bounds = getBounds();
    x = (int) bounds.getX();
    y = (int) bounds.getY();
    w = (int) bounds.getWidth();
    h = (int) bounds.getHeight();

    if (isFake) {
      robot = new FakeRobot();
    } else {
      try {
        robot = new VNCRobot(_curGD);
        robot.setAutoDelay(10);
      } catch (AWTException e) {
        Debug.error("Can't initialize Java Robot on VNCScreen " + _curID + ": " + e.getMessage());
        robot = null;
      }
    }
  }

  public static int getNumberScreens() {
    return _gdev.length;
  }

  private static int getValidID(int id) {
    if (id < 0 || id >= _gdev.length) {
      Debug.error("VNCScreen: invalid screen id %d - using primary screen", id);
      return _primaryScreen;
    }
    return id;
  }

  public static int getPrimaryId() {
    return _primaryScreen;
  }

  public static VNCScreen getPrimaryScreen() {
    return screens[_primaryScreen];
  }

  public static VNCScreen getScreen(int id) {
    return screens[getValidID(id)];
  }

  public static Rectangle getBounds(int id) {
    return gdevsBounds[getValidID(id)];
  }

  public static IRobot getRobot(int id) {
    return getScreen(id).getRobot();
  }

  public static void showMonitors() {
    Debug.info("*** monitor configuration [ %s VNCScreen(s)] ***", VNCScreen.getNumberScreens());
    Debug.info("*** Primary is VNCScreen %d", _primaryScreen);
    for (int i = 0; i < _gdev.length; i++) {
      Debug.info("Screen %d: %s", i, VNCScreen.getScreen(i).toStringShort());
    }
    Debug.info("*** end monitor configuration ***");
  }

  public void setAsScreenUnion() {
    oldID = _curID;
    _curID = -1;
  }

  public void setAsScreen() {
    _curID = oldID;
  }

  @Override
  public void initScreen(IScreen scr) {
    updateSelf();
  }

  @Override
  public IScreen getScreen() {
    return this;
  }

  @Override
  protected Region setScreen(IScreen s) {
    throw new UnsupportedOperationException("The setScreen() method cannot be called from a VNCScreen object.");
  }

  protected boolean useFullscreen() {
    return false;
  }

  @Override
  public int getID() {
    return _curID;
  }

  public String getIDString() {
    return String.format("VNC_%d", getID());
  }

  @Override
  public int getIdFromPoint(int x, int y) {
    return _curID;
  }

  public GraphicsDevice getGraphicsDevice() {
    return _curGD;
  }

  @Override
  public IRobot getRobot() {
    return robot;
  }

  @Override
  public Rectangle getBounds() {
    return gdevsBounds[_curID];
  }

  public Region newRegion(int x, int y, int width, int height) {
    return new Region(x, y, width, height, this);
  }

  public Region newRegion(Location loc, int width, int height) {
    return new Region(loc.x, loc.y, width, height, this);
  }

  public Region newRegion(Region r) {
    return new Region(r.x, r.y, r.w, r.h, this);
  }

  public Region newRegion(Rectangle r) {
    return new Region(r.x, r.y, r.width, r.height, this);
  }

  public Location newLocation(Location loc) {
    return loc.setOtherScreen(this);
  }

  public Location newLocation(int x, int y) {
    return new Location(x, y).setOtherScreen(this);
  }

  public Location newLocation(Point p) {
    return new Location(p.x, p.y).setOtherScreen(this);
  }

  @Override
  public ScreenImage getLastScreenImageFromScreen() {
    return lastScreenImage;
  }

  @Override
  public ScreenImage capture() {
    return capture(getRect());
  }

  @Override
  public ScreenImage capture(int x, int y, int w, int h) {
    Rectangle rect = newRegion(new Location(x, y), w, h).getRect();
    return capture(rect);
  }

  @Override
  public ScreenImage capture(Rectangle rect) {
    ScreenImage simg = null;
    if (isFake) {
      simg = FakeRobot.getDesktopRobot().captureScreen(rect);
    } else {
      if (robot != null) {
        log(lvl, "VNCScreen.capture: (%d,%d) %dx%d", rect.x, rect.y, rect.width, rect.height);
      }
      simg = robot.captureScreen(rect);
    }
    lastScreenImage = simg;
    return simg;
  }

  @Override
  public ScreenImage capture(Region reg) {
    return capture(reg.getRect());
  }

  public ScreenImage userCapture() {
    return userCapture(promptMsg);
  }

  @Override
  public ScreenImage userCapture(final String msg) {
    waitPrompt = true;
    Thread th = new Thread() {
      @Override
      public void run() {
        prompt = new OverlayCapturePrompt(VNCScreen.this);
        prompt.prompt(msg);
      }
    };

    th.start();

    boolean hasShot = false;
    ScreenImage simg = null;
    int count = 0;
    while (!hasShot) {
      this.wait(0.1f);
      if (count++ > waitForScreenshot) {
        break;
      }
      if (prompt == null) {
        continue;
      }
      if (prompt.isComplete()) {
        simg = prompt.getSelection();
        if (simg != null) {
          lastScreenImage = simg;
          hasShot = true;
        }
        prompt.close();
      }
    }
    prompt.close();
    prompt = null;
    return simg;
  }

  public Region selectRegion() {
    return selectRegion("Select a region on the screen");
  }

  public Region selectRegion(final String msg) {
    ScreenImage sim = userCapture(msg);
    if (sim == null) {
      return null;
    }
    Rectangle r = sim.getROI();
    return Region.create((int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight());
  }

  @Override
  public void update(EventSubject s) {
    waitPrompt = false;
  }

  @Override
  public void showTarget(Location loc) {
    showTarget(loc, Settings.SlowMotionDelay);
  }

  protected void showTarget(Location loc, double secs) {
    if (Settings.isShowActions()) {
      ScreenHighlighter overlay = new ScreenHighlighter(this, null);
      overlay.showTarget(loc, (float) secs);
    }
  }

  @Override
  public boolean isOtherScreen() {
    return otherScreen;
  }

  @Override
  public Rectangle getRect() {
    return new Rectangle(x, y, w, h);
  }

  @Override
  public int getX() {
    return (int) getBounds().getX();
  }

  @Override
  public int getY() {
    return (int) getBounds().getY();
  }

  @Override
  public int getW() {
    return (int) getBounds().getWidth();
  }

  @Override
  public int getH() {
    return (int) getBounds().getHeight();
  }

  @Override
  public String toString() {
    return toStringShort();
  }

  @Override
  public String toStringShort() {
    Rectangle r = getBounds();
    return String.format("VNC(%d)[%d,%d %dx%d]",
            _curID, (int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight());
  }
}
