/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

/**
 * A screen represents a physical monitor with its coordinates and size according to the global
 * point system: the screen areas are grouped around a point (0,0) like in a cartesian system (the
 * top left corner and the points contained in the screen area might have negative x and/or y values)
 * <br >The screens are arranged in an array (index = id) and each screen is always the same object
 * (not possible to create new objects).
 * <br>A screen inherits from class Region, so it can be used as such in all aspects. If you need
 * the region of the screen more than once, you have to create new ones based on the screen.
 * <br>The so called primary screen is the one with top left (0,0) and has id 0.
 */
public class Screen extends Region implements EventObserver, IScreen {

  private static String me = "Screen";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }
  
  protected static GraphicsEnvironment genv = null;
  protected static GraphicsDevice[] gdevs;
  protected static Rectangle[] gdevsBounds;
  private static Robot mouseRobot;
  protected static Screen[] screens;
  protected static int primaryScreen = -1;
  private static int waitForScreenshot = 300;
  protected IRobot robot = null;
  protected int curID = -1;
  protected int oldID = 0;
  protected GraphicsDevice curGD = null;
  protected boolean waitPrompt;
  protected OverlayCapturePrompt prompt;
  private String promptMsg = "Select a region on the screen";
  private ScreenImage lastScreenImage = null;
  private static Point lastMousePosition = null;

  //<editor-fold defaultstate="collapsed" desc="Initialization">

  static {
    initScreens(false);
  }

//  private static void initScreens() {
//    initScreens(false);
//  }

  private static void initScreens(boolean reset) {
    if (genv != null && !reset) {
      return;
    }
    genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    gdevs = genv.getScreenDevices();
    gdevsBounds = new Rectangle[gdevs.length];
    screens = new Screen[gdevs.length];
    if (gdevs.length == 0) {
      Debug.error("Screen: initScreens: GraphicsEnvironment has no screens");
      SikuliX.endFatal(999);
    }
    primaryScreen = -1;
    for (int i = 0; i < getNumberScreens(); i++) {
      gdevsBounds[i] = gdevs[i].getDefaultConfiguration().getBounds();
      if (gdevsBounds[i].contains(new Point(0, 0))) {
        if (primaryScreen < 0) {
          primaryScreen = i;
          log(lvl, "initScreens: ScreenDevice %d contains (0,0) --- will be used as primary", i);
      } else {
          log(lvl, "initScreens: ScreenDevice %d too contains (0,0)!", i);
        }
      }
    }
    if (primaryScreen < 0) {
      Debug.log("Screen: initScreens: no ScreenDevice contains (0,0) --- using first ScreenDevice as primary");
      primaryScreen = 0;
    }
//    if (primaryScreen > 0) {
//      GraphicsDevice gd0 = gdevs[primaryScreen];
//      for (int i = primaryScreen; i > 0; i--) {
//        gdevs[i] = gdevs[i-1];
//      }
//      gdevs[0] = gd0;
//    }
    int is;
    for (int i = 0; i < screens.length; i++) {
      is = i;
//      if (i == primaryScreen) {
//        is = 0;
//      } else if (i < primaryScreen) {
//        is = i + 1;
//      } else {
//        is = i;
//      }
      screens[is] = new Screen(is, true);
      screens[is].initScreen();
    }
//    primaryScreen = 0;
    if (!reset) {
      log(lvl - 1, "initScreens: basic initialization (%d Screen(s) found)", gdevs.length);
      log(lvl, "*** monitor configuration (primary: %d) ***", primaryScreen);
      for (int i = 0; i < gdevs.length; i++) {
        log(lvl, "%d: %s", i, screens[i].toStringShort());
      }
      log(lvl, "*** end monitor configuration ***");
    }
    try {
      mouseRobot = new Robot();
      mouseRobot.setAutoDelay(10);
    } catch (AWTException e) {
      Debug.error("Can't initialize global Robot for Mouse: " + e.getMessage());
      SikuliX.endFatal(999);
    }
    if (0 < getNumberScreens()) {
      log(lvl, "*** checking: how to click on the available screens");
      Location lnow = Mouse.at();
      float mmd = Settings.MoveMouseDelay;
      Settings.MoveMouseDelay = 0f;
      Screen s0 = Screen.getPrimaryScreen();
      Location lc = null, lcn = null;
      for (Screen s : screens) {
        try {
          lc = s.getCenter();
          s0.hover(lc);
          lcn = Mouse.at();
        } catch (Exception ex) {
        }
        if (!lc.equals(lcn)) {
          log(lvl, "*** checking: %s center: (%d, %d) --- NOT OK:  (%d, %d)",
                  s.toStringShort(), lc.x, lc.y, lcn.x, lcn.y);
        } else {
          log(lvl, "*** checking: %s center: (%d, %d) --- OK", s.toStringShort(), lc.x, lc.y);
        }
      }
      try {
        s0.hover(lnow);
      } catch (FindFailed ex) {
      }
      Settings.MoveMouseDelay = mmd;
    }
  }
  
  protected static Robot getMouseRobot() {
    return mouseRobot;
  }
          
  // hack to get an additional internal constructor for the initialization
  private Screen(int id, boolean init) {
    super();
    curID = id;
  }

  /**
   * Is the screen object at the given id
   *
   * @param id
   * @throws Exception TODO: implement an own Exception instead of using the Exception class
   */
  public Screen(int id) throws Exception {
    super();
//    initScreens();
    if (id < 0 || id >= gdevs.length) {
      throw new IllegalArgumentException("Screen ID " + id + " not in valid range (between 0 and " + (gdevs.length - 1));
    }
    curID = id;
    initScreen();
  }

	/**
	 * INTERNAL USE
	 * collect all physical screens to one big region<br />
	 * This is under evaluation, wether it really makes sense
	 * @param isScreenUnion
	 */
	public Screen(boolean isScreenUnion) {
    super();
  }

	/**
	 * INTERNAL USE
	 * collect all physical screens to one big region<br />
	 * This is under evaluation, wether it really makes sense
	 */
  public void setAsScreenUnion() {
    oldID = curID;
    curID = -1;
  }

	/**
	 * INTERNAL USE
	 * reset from being a screen union to the screen used before
	 */
  public void setAsScreen() {
    curID = oldID;
  }

  /**
   * Is the screen object having the top left corner as (0,0). If such a screen does not exist it is
   * the screen with id 0.
   */
  public Screen() {
    super();
    curID = getPrimaryId();
    initScreen();
  }

  /**
   * {@inheritDoc} TODO: remove this method if it is not needed
   */
  @Override
  protected void initScreen(Screen scr) {
    updateSelf();
  }

  private void initScreen() {
    curGD = gdevs[curID];
    Rectangle bounds = getBounds();
    x = (int) bounds.getX();
    y = (int) bounds.getY();
    w = (int) bounds.getWidth();
    h = (int) bounds.getHeight();
    try {
      robot = new RobotDesktop(this);
      robot.setAutoDelay(10);
    } catch (AWTException e) {
      Debug.error("Can't initialize Java Robot on Screen " + curID + ": " + e.getMessage());
      robot = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Screen getScreen() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Region setScreen(Screen s) {
    throw new UnsupportedOperationException("The setScreen() method cannot be called from a Screen object.");
  }
  
  public static ScreenUnion allMonitors() {
    return new ScreenUnion();
  }

  /**
   * show the current monitor setup
   */
  public static void showMonitors() {
//    initScreens();
    Debug.info("*** monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.info("*** Primary is Screen %d", Screen.getPrimaryId());
    for (int i = 0; i < gdevs.length; i++) {
      Debug.info("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.info("*** end monitor configuration ***");
  }

  /**
   * re-initialize the monitor setup (e.g. when it was changed while running)
   */
  public static void resetMonitors() {
    Debug.error("*** BE AWARE: experimental - might not work ***");
    Debug.error("Re-evaluation of the monitor setup has been requested");
    Debug.error("... Current Region/Screen objects might not be valid any longer");
    Debug.error("... Use existing Region/Screen objects only if you know what you are doing!");
    initScreens(true);
    Debug.info("*** new monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.info("*** Primary is Screen %d", Screen.getPrimaryId());
    for (int i = 0; i < gdevs.length; i++) {
      Debug.info("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.error("*** end new monitor configuration ***");
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getters setters">
  protected boolean useFullscreen() {
    return false;
  }

  private static int getValidID(int id) {
//    initScreens();
    if (id < 0 || id >= gdevs.length) {
      Debug.error("Screen: invalid screen id %d - using primary screen", id);
      return primaryScreen;
    }
    return id;
  }

  /**
   *
   * @return number of available screens
   */
  public static int getNumberScreens() {
//    initScreens();
    return gdevs.length;
  }

  /**
   *
   * @return the id of the screen at (0,0), if not exists 0
   */
  public static int getPrimaryId() {
//    initScreens();
    return primaryScreen;
  }

  /**
   *
   * @return the screen at (0,0), if not exists the one with id 0
   */
  public static Screen getPrimaryScreen() {
    return screens[primaryScreen];
  }

  /**
   *
   * @param id of the screen
   * @return the screen with given id, the primary screen if id is invalid
   */
  public static Screen getScreen(int id) {
    return screens[getValidID(id)];
  }

  /**
   *
   * @param id
   * @return the physical coordinate/size <br>as AWT.Rectangle to avoid mix up with getROI
   */
  public static Rectangle getBounds(int id) {
    return gdevsBounds[getValidID(id)];
  }

  /**
   * each screen has exactly one robot (internally used for screen capturing)
   * <br>available as a convenience for those who know what they are doing. Should not be needed
   * normally.
   *
   * @param id
   * @return the AWT.Robot of the given screen, if id invalid the primary screen
   */
  public static IRobot getRobot(int id) {
    return getScreen(id).getRobot();
  }

  /**
   *
	 * @return the id
   */
  public int getID() {
    return curID;
  }

  /**
   * INTERNAL USE: to be compatible with ScreenUnion
   * @param x
   * @param y
   * @return
   */
  protected int getIdFromPoint(int x, int y) {
    return curID;
  }

  /**
   *
	 * @return active GraphicsDevice
   */
  public GraphicsDevice getGraphicsDevice() {
    return curGD;
  }

  /**
   * Gets the Robot of this Screen.
   *
   * @return The Robot for this Screen
   */
  @Override
  public IRobot getRobot() {
    return robot;
  }
  /**
	 *
	 * @return the screen's rectangle
	 */
  @Override
  public Rectangle getBounds() {
    return gdevsBounds[curID];
  }

  /**
   * creates a region on the current screen with the given coordinate/size. The coordinate is
   * translated to the current screen from its relative position on the screen it would have been
   * created normally.
   *
   * @param loc
   * @param width
   * @param height
   * @return the new region
   */
  public Region newRegion(Location loc, int width, int height) {
    return Region.create(loc.copyTo(this), width, height);
  }

  protected ScreenImage getLastScreenImageFromScreen() {
    return lastScreenImage;
  }
  /**
   * creates a location on the current screen with the given point. The coordinate is translated to
   * the current screen from its relative position on the screen it would have been created
   * normally.
   *
   * @param loc
   * @return the new location
   */
  public Location newLocation(Location loc) {
    return (new Location(loc)).copyTo(this);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Capture - SelectRegion">
  /**
   * create a ScreenImage with the physical bounds of this screen
   *
   * @return the image
   */
  @Override
  public ScreenImage capture() {
    return capture(getRect());
  }

  /**
   * create a ScreenImage with given coordinates on this screen.
   *
   * @param x x-coordinate of the region to be captured
   * @param y y-coordinate of the region to be captured
   * @param w width of the region to be captured
   * @param h height of the region to be captured
   * @return the image of the region
   */
  @Override
  public ScreenImage capture(int x, int y, int w, int h) {
    Rectangle rect = newRegion(new Location(x, y), w, h).getRect();
    return capture(rect);
  }

  /**
   * create a ScreenImage with given rectangle on this screen.
   *
   * @param rect The Rectangle to be captured
   * @return the image of the region
   */
  @Override
  public ScreenImage capture(Rectangle rect) {
    log(lvl + 1, "Screen.capture: (%d,%d) %dx%d", rect.x, rect.y, rect.width, rect.height);
    ScreenImage simg = robot.captureScreen(rect);
    lastScreenImage = simg;
    return simg;
  }

  /**
   * create a ScreenImage with given region on this screen
   *
   * @param reg The Region to be captured
   * @return the image of the region
   */
  @Override
  public ScreenImage capture(Region reg) {
    return capture(reg.getRect());
  }

  /**
   * interactive capture with predefined message: lets the user capture a screen image using the
   * mouse to draw the rectangle
   *
   * @return the image
   */
  public ScreenImage userCapture() {
    return userCapture(promptMsg);
  }

  /**
   * interactive capture with given message: lets the user capture a screen image using the mouse to
   * draw the rectangle
   *
   * @return the image
   */
  public ScreenImage userCapture(final String msg) {
    waitPrompt = true;
    Thread th = new Thread() {
      @Override
      public void run() {
        if ("".equals(msg)) {
          prompt = new OverlayCapturePrompt(null, Screen.this);
          prompt.prompt(promptMsg);
       } else {
          prompt = new OverlayCapturePrompt(Screen.this, Screen.this);
          prompt.prompt(msg);
        }
      }
    };
    th.start();
    try {
      int count = 0;
      while (waitPrompt) {
        Thread.sleep(100);
        if (count++ > waitForScreenshot) {
          return null;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    ScreenImage ret = prompt.getSelection();
    lastScreenImage = ret;
    prompt.close();
    return ret;
  }

  /**
   * interactive region create with predefined message: lets the user draw the rectangle using the
   * mouse
   *
   * @return the region
   */
  public Region selectRegion() {
    return selectRegion("Select a region on the screen");
  }

  /**
   * interactive region create with given message: lets the user draw the rectangle using the mouse
   *
   * @return the region
   */
  public Region selectRegion(final String msg) {
    ScreenImage sim = userCapture(msg);
    if (sim == null) {
      return null;
    }
    Rectangle r = sim.getROI();
    return Region.create((int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight());
  }

  /**
   * Internal use only
   *
   * @param s
   */
  @Override
  public void update(EventSubject s) {
    waitPrompt = false;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Visual effects">
  protected void showTarget(Location loc) {
    showTarget(loc, Settings.SlowMotionDelay);
  }

  protected void showTarget(Location loc, double secs) {
    if (Settings.isShowActions()) {
      ScreenHighlighter overlay = new ScreenHighlighter(this, null);
      overlay.showTarget(loc, (float) secs);
    }
  }
  //</editor-fold>

  @Override
  public String toString() {
    Rectangle r = getBounds();
    return String.format("S(%d)[%d,%d %dx%d] E:%s, T:%.1f",
            curID, (int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight(),
            getThrowException() ? "Y" : "N", getAutoWaitTimeout());
  }

  /**
   * only a short version of toString()
   *
   * @return like S(0) [0,0, 1440x900]
   */
  public String toStringShort() {
    Rectangle r = getBounds();
    return String.format("S(%d)[%d,%d %dx%d]",
            curID, (int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight());
  }
}
