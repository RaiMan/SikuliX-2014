/*
 * Copyright 2010-2014, Sikuli.org, Sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

import edu.unh.iol.dlc.VNCScreen;

/**
 * A Region is a rectengular area and lies always completely inside its parent screen
 *
 */
public class Region {

  private static String me = "Region: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  /**
   * The Screen containing the Region
   */
  private IScreen scr;
  protected boolean otherScreen = false;

  /**
   * The ScreenHighlighter for this Region
   */
  private ScreenHighlighter overlay = null;
  /**
   * X-coordinate of the Region
   */
  public int x;
  /**
   * Y-coordinate of the Region
   */
  public int y;
  /**
   * Width of the Region
   */
  public int w;
  /**
   * Height of the Region
   */
  public int h;
  /**
   * Setting, how to react if an image is not found {@link FindFailed}
   */
  private FindFailedResponse findFailedResponse = FindFailed.defaultFindFailedResponse;
  /**
   * Setting {@link Settings}, if exception is thrown if an image is not found
   */
  private boolean throwException = Settings.ThrowException;
  /**
   * Default time to wait for an image {@link Settings}
   */
  private double autoWaitTimeout = Settings.AutoWaitTimeout;
  private float waitScanRate = Settings.WaitScanRate;
  /**
   * Flag, if an observer is running on this region {@link Settings}
   */
  private boolean observing = false;
  private float observeScanRate = Settings.ObserveScanRate;
  private int repeatWaitTime = Settings.RepeatWaitTime;
  /**
   * The {@link Observer} Singleton instance
   */
  private Observer regionObserver = null;

  /**
   * The last found {@link Match} in the Region
   */
  private Match lastMatch = null;
  /**
   * The last found {@link Match}es in the Region
   */
  private Iterator<Match> lastMatches = null;
  private long lastSearchTime;
  private long lastFindTime;

	/**
	 * in case of not found the total wait time
	 * @return the duration of the last find op
	 */
	public long getLastTime() {
		return lastFindTime;
	}

  /**
   * the area constants for use with get()
   */
  public static final int NW = 300, NORTH_WEST = NW, TL = NW;
  public static final int NM = 301, NORTH_MID = NM, TM = NM;
  public static final int NE = 302, NORTH_EAST = NE, TR = NE;
  public static final int EM = 312, EAST_MID = EM, RM = EM;
  public static final int SE = 322, SOUTH_EAST = SE, BR = SE;
  public static final int SM = 321, SOUTH_MID = SM, BM = SM;
  public static final int SW = 320, SOUTH_WEST = SW, BL = SW;
  public static final int WM = 310, WEST_MID = WM, LM = WM;
  public static final int MM = 311, MIDDLE = MM, M3 = MM;
  public static final int TT = 200;
  public static final int RR = 201;
  public static final int BB = 211;
  public static final int LL = 210;
  public static final int NH = 202, NORTH = NH, TH = NH;
  public static final int EH = 221, EAST = EH, RH = EH;
  public static final int SH = 212, SOUTH = SH, BH = SH;
  public static final int WH = 220, WEST = WH, LH = WH;
  public static final int MV = 441, MID_VERTICAL = MV, CV = MV;
  public static final int MH = 414, MID_HORIZONTAL = MH, CH = MH;
  public static final int M2 = 444, MIDDLE_BIG = M2, C2 = M2;
  public static final int EN = NE, EAST_NORTH = NE, RT = TR;
  public static final int ES = SE, EAST_SOUTH = SE, RB = BR;
  public static final int WN = NW, WEST_NORTH = NW, LT = TL;
  public static final int WS = SW, WEST_SOUTH = SW, LB = BL;

  /**
   * to support a raster over the region
   */
  private int rows;
  private int cols = 0;
  private int rowH = 0;
  private int colW = 0;
  private int rowHd = 0;
  private int colWd = 0;

  /**
   * {@inheritDoc}
   *
   * @return the description
   */
  @Override
  public String toString() {
    return String.format("R[%d,%d %dx%d]@%s E:%s, T:%.1f",
            x, y, w, h, (getScreen() == null ? "Screen null" : getScreen().toStringShort()),
            throwException ? "Y" : "N", autoWaitTimeout);
  }

  /**
   *
   * @return a compact description
   */
  public String toStringShort() {
    return String.format("R[%d,%d %dx%d]@S(%s)", x, y, w, h, (getScreen() == null ? "?" : getScreen().getID()));
  }

  //<editor-fold defaultstate="collapsed" desc="OFF: Specials for scripting environment">
  /*
   public Object __enter__() {
   Debug.error("Region: with(__enter__): Trying to make it a Jython Region for with: usage");
   IScriptRunner runner = Settings.getScriptRunner("jython", null, null);
   if (runner != null) {
   Object[] jyreg = new Object[]{this};
   if (runner.doSomethingSpecial("createRegionForWith", jyreg)) {
   if (jyreg[0] != null) {
   return jyreg[0];
   }
   }
   }
   Debug.error("Region: with(__enter__): Sorry, not possible");
   return null;
   }

   public void __exit__(Object type, Object value, Object traceback) {
   Debug.error("Region: with(__exit__): Sorry, not a Jython Region and not posssible!");
   }
   */
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Initialization">
  /**
   * Detects on which Screen the Region is present. The region is cropped to the intersection with the given screen or
   * the screen with the largest intersection
   *
   * @param scr The Screen containing the Region
   */
  public void initScreen(IScreen scr) {
    // check given screen first
    Rectangle rect, screenRect;
    IScreen screen, screenOn;
    if (scr != null) {
      if (scr.isOtherScreen()) {
        if (x < 0) {
          w = w + x;
          x = 0;
        }
        if (y < 0) {
          h = h + y;
          y = 0;
        }
        this.scr = scr;
        this.otherScreen = true;
        return;
      }
      if (scr.getID() > -1) {
        rect = regionOnScreen(scr);
        if (rect != null) {
          x = rect.x;
          y = rect.y;
          w = rect.width;
          h = rect.height;
          this.scr = scr;
          return;
        }
      } else {
        // is ScreenUnion
        return;
      }
    }
    // check all possible screens if no screen was given or the region is not on given screen
    // crop to the screen with the largest intersection
    screenRect = new Rectangle(0, 0, 0, 0);
    screenOn = null;
    if (scr instanceof Screen) {
      for (int i = 0; i < Screen.getNumberScreens(); i++) {
        screen = Screen.getScreen(i);
        rect = regionOnScreen(screen);
        if (rect != null) {
          if (rect.width * rect.height > screenRect.width * screenRect.height) {
            screenRect = rect;
            screenOn = screen;
          }
        }
      }
    } else if (scr instanceof VNCScreen) {
      for (int i = 0; i < VNCScreen.getNumberScreens(); i++) {
        screen = VNCScreen.getScreen(i);
        rect = regionOnScreen(screen);
        if (rect != null) {
          if (rect.width * rect.height > screenRect.width * screenRect.height) {
            screenRect = rect;
            screenOn = screen;
          }
        }
      }
    } else {//when scr == null
      for (int i = 0; i < Screen.getNumberScreens(); i++) {
        screen = Screen.getScreen(i);
        rect = regionOnScreen(screen);
        if (rect != null) {
          if (rect.width * rect.height > screenRect.width * screenRect.height) {
            screenRect = rect;
            screenOn = screen;
          }
        }
      }
    }
    if (screenOn != null) {
      x = screenRect.x;
      y = screenRect.y;
      w = screenRect.width;
      h = screenRect.height;
      this.scr = screenOn;
    } else {
      // no screen found
      this.scr = null;
      Debug.error("Region(%d,%d,%d,%d) outside any screen - subsequent actions might not work as expected", x, y, w, h);
    }
  }

  private Location checkAndSetRemote(Location loc) {
    if (!isOtherScreen()) {
      return loc;
    }
    return loc.setOtherScreen(scr);
  }

	/**
	 * INTERNAL USE:
	 * checks wether this region belongs to a non-Desktop screen
	 * @return true/false
	 */
	public boolean isOtherScreen() {
    return otherScreen;
  }

	/**
	 * INTERNAL USE:
	 * flags this region as belonging to a non-Desktop screen
	 */
	public void setOtherScreen() {
    otherScreen = true;
  }

  /**
   * Checks if the Screen contains the Region.
   *
   * @param screen The Screen in which the Region might be
   * @return True, if the Region is on the Screen. False if the Region is not inside the Screen
   */
  protected Rectangle regionOnScreen(IScreen screen) {
    if (screen == null) {
      return null;
    }
    // get intersection of Region and Screen
    Rectangle rect = screen.getRect().intersection(getRect());
    // no Intersection, Region is not on the Screen
    if (rect.isEmpty()) {
      return null;
    }
    return rect;
  }

	/**
	 * Check wether thie Region is contained by any of the available screens
	 * @return true if yes, false otherwise
	 */
	public boolean isValid() {
		return scr != null;
	}
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructors to be used with Jython">
  /**
   * Create a region with the provided coordinate / size and screen
   *
   * @param X X position
   * @param Y Y position
   * @param W width
   * @param H heigth
   * @param screenNumber The number of the screen containing the Region
   */
  public Region(int X, int Y, int W, int H, int screenNumber) {
    this(X, Y, W, H, Screen.getScreen(screenNumber));
    this.rows = 0;
  }

  /**
   * Create a region with the provided coordinate / size and screen
   *
   * @param X X position
   * @param Y Y position
   * @param W width
   * @param H heigth
   * @param parentScreen the screen containing the Region
   */
  public Region(int X, int Y, int W, int H, IScreen parentScreen) {
    this.rows = 0;
    this.x = X;
    this.y = Y;
    this.w = W > 1 ? W : 1;
    this.h = H > 1 ? H : 1;
    initScreen(parentScreen);
  }

  /**
   * Create a region with the provided coordinate / size
   *
   * @param X X position
   * @param Y Y position
   * @param W width
   * @param H heigth
   */
  public Region(int X, int Y, int W, int H) {
    this(X, Y, W, H, null);
    this.rows = 0;
    log(lvl, "init: (%d, %d, %d, %d)", X, Y, W, H);
  }

  /**
   * Create a region from a Rectangle
   *
   * @param r the Rectangle
   */
  public Region(Rectangle r) {
    this(r.x, r.y, r.width, r.height, null);
    this.rows = 0;
  }

  /**
   * Create a new region from another region<br>including the region's settings
   *
   * @param r the region
   */
  public Region(Region r) {
    this(r.x, r.y, r.w, r.h, r.getScreen());
    this.rows = 0;
    autoWaitTimeout = r.autoWaitTimeout;
    findFailedResponse = r.findFailedResponse;
    throwException = r.throwException;
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Quasi-Constructors to be used in Java">
  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region() {
    this.rows = 0;
  }

  /**
   * Create a region with the provided top left corner and size
   *
   * @param X top left X position
   * @param Y top left Y position
   * @param W width
   * @param H heigth
   * @return then new region
   */
  public static Region create(int X, int Y, int W, int H) {
    return Region.create(X, Y, W, H, null);
  }

  /**
   * Create a region with the provided top left corner and size
   *
   * @param X top left X position
   * @param Y top left Y position
   * @param W width
   * @param H heigth
   * @param scr the source screen
   * @return the new region
   */
  private static Region create(int X, int Y, int W, int H, IScreen scr) {
    return new Region(X, Y, W, H, scr);
  }

  /**
   * Create a region with the provided top left corner and size
   *
   * @param loc top left corner
   * @param w width
   * @param h height
   * @return then new region
   */
  public static Region create(Location loc, int w, int h) {
    return Region.create(loc.x, loc.y, w, h, loc.getScreen());
  }
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the left corner of
   * the new Region.
   */
  public final static int CREATE_X_DIRECTION_LEFT = 0;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the right corner of
   * the new Region.
   */
  public final static int CREATE_X_DIRECTION_RIGHT = 1;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the top corner of the
   * new Region.
   */
  public final static int CREATE_Y_DIRECTION_TOP = 0;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the bottom corner of
   * the new Region.
   */
  public final static int CREATE_Y_DIRECTION_BOTTOM = 1;

  /**
   * create a region with a corner at the given point<br>as specified with x y<br> 0 0 top left<br> 0 1 bottom left<br>
   * 1 0 top right<br> 1 1 bottom right<br>
   *
   * @param loc the refence point
   * @param create_x_direction == 0 is left side !=0 is right side
   * @param create_y_direction == 0 is top side !=0 is bottom side
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public static Region create(Location loc, int create_x_direction, int create_y_direction, int w, int h) {
    int X;
    int Y;
    int W = w;
    int H = h;
    if (create_x_direction == CREATE_X_DIRECTION_LEFT) {
      if (create_y_direction == CREATE_Y_DIRECTION_TOP) {
        X = loc.x;
        Y = loc.y;
      } else {
        X = loc.x;
        Y = loc.y - h;
      }
    } else {
      if (create_y_direction == CREATE_Y_DIRECTION_TOP) {
        X = loc.x - w;
        Y = loc.y;
      } else {
        X = loc.x - w;
        Y = loc.y - h;
      }
    }
    return Region.create(X, Y, W, H, loc.getScreen());
  }

  /**
   * create a region with a corner at the given point<br>as specified with x y<br> 0 0 top left<br> 0 1 bottom left<br>
   * 1 0 top right<br> 1 1 bottom right<br>same as the corresponding create method, here to be naming compatible with
   * class Location
   *
   * @param loc the refence point
   * @param x ==0 is left side !=0 is right side
   * @param y ==0 is top side !=0 is bottom side
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public static Region grow(Location loc, int x, int y, int w, int h) {
    return Region.create(loc, x, y, w, h);
  }

  /**
   * Create a region from a Rectangle
   *
   * @param r the Rectangle
   * @return the new region
   */
  public static Region create(Rectangle r) {
    return Region.create(r.x, r.y, r.width, r.height, null);
  }

  /**
   * Create a region from a Rectangle on a given Screen
   *
   * @param r the Rectangle
   * @param parentScreen the new parent screen
   * @return the new region
   */
  protected static Region create(Rectangle r, IScreen parentScreen) {
    return Region.create(r.x, r.y, r.width, r.height, parentScreen);
  }

  /**
   * Create a region from another region<br>including the region's settings
   *
   * @param r the region
   * @return then new region
   */
  public static Region create(Region r) {
    Region reg = Region.create(r.x, r.y, r.w, r.h, r.getScreen());
    reg.autoWaitTimeout = r.autoWaitTimeout;
    reg.findFailedResponse = r.findFailedResponse;
    reg.throwException = r.throwException;
    return reg;
  }

  /**
   * create a region with the given point as center and the given size
   *
   * @param loc the center point
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public static Region grow(Location loc, int w, int h) {
    int X = loc.x - (int) w / 2;
    int Y = loc.y - (int) h / 2;
    return Region.create(X, Y, w, h, loc.getScreen());
  }

  /**
   * create a minimal region at given point with size 1 x 1
   *
   * @param loc the point
   * @return the new region
   */
  public static Region grow(Location loc) {
    return Region.create(loc.x, loc.y, 1, 1, loc.getScreen());
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="handle coordinates">
  /**
   * check if current region contains given point
   *
   * @param point Point
   * @return true/false
   */
  public boolean contains(Location point) {
    return getRect().contains(point.x, point.y);
  }

  /**
   * check if mouse pointer is inside current region
   *
   * @return true/false
   */
  public boolean containsMouse() {
    return contains(Mouse.at());
  }

  /**
   * new region with same offset to current screen's top left on given screen
   *
   * @param scrID number of screen
   * @return new region
   */
  public Region copyTo(int scrID) {
    return copyTo(Screen.getScreen(scrID));
  }

  /**
   * new region with same offset to current screen's top left on given screen
   *
   * @param screen new parent screen
   * @return new region
   */
  public Region copyTo(IScreen screen) {
    Location o = new Location(getScreen().getBounds().getLocation());
    Location n = new Location(screen.getBounds().getLocation());
    return Region.create(n.x + x - o.x, n.y + y - o.y, w, h, screen);
  }

  /**
   * used in Observer.callChangeObserving, Finder.next to adjust region relative coordinates of matches to
 screen coordinates
   *
   * @param m
   * @return the modified match
   */
  protected Match toGlobalCoord(Match m) {
    m.x += x;
    m.y += y;
    return m;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="handle Settings">
	//TODO should be possible to reset to current global value resetXXX()
	/**
   * true - (initial setting) should throw exception FindFailed if findX unsuccessful in this region<br> false - do not
   * abort script on FindFailed (might leed to null pointer exceptions later)
   *
   * @param flag true/false
   */
  public void setThrowException(boolean flag) {
    throwException = flag;
    if (throwException) {
      findFailedResponse = FindFailedResponse.ABORT;
    } else {
      findFailedResponse = FindFailedResponse.SKIP;
    }
  }

  /**
   * current setting for this region (see setThrowException)
   *
   * @return true/false
   */
  public boolean getThrowException() {
    return throwException;
  }

  /**
   * the time in seconds a find operation should wait for the appearence of the target in this region<br> initial value
   * is the global AutoWaitTimeout setting at time of Region creation
   *
   * @param sec seconds
   */
  public void setAutoWaitTimeout(double sec) {
    autoWaitTimeout = sec;
  }

  /**
   * current setting for this region (see setAutoWaitTimeout)
   *
   * @return value of seconds
   */
  public double getAutoWaitTimeout() {
    return autoWaitTimeout;
  }

  /**
   * FindFailedResponse.<br> ABORT - (initial value) abort script on FindFailed (= setThrowException(true) )<br> SKIP -
   * ignore FindFailed (same as setThrowException(false) )<br>
   * PROMPT - display prompt on FindFailed to let user decide how to proceed<br> RETRY - continue to wait for appearence
   * on FindFailed (caution: endless loop)
   *
   * @param response the FindFailedResponse
   */
  public void setFindFailedResponse(FindFailedResponse response) {
    findFailedResponse = response;
  }

  /**
   *
   * @return the current setting (see setFindFailedResponse)
   */
  public FindFailedResponse getFindFailedResponse() {
    return findFailedResponse;
  }

  /**
   *
   * @return the regions current WaitScanRate
   */
  public float getWaitScanRate() {
    return waitScanRate;
  }

  /**
   * set the regions individual WaitScanRate
   *
   * @param waitScanRate decimal number
   */
  public void setWaitScanRate(float waitScanRate) {
    this.waitScanRate = waitScanRate;
  }

  /**
   *
   * @return the regions current ObserveScanRate
   */
  public float getObserveScanRate() {
    return observeScanRate;
  }

  /**
   * set the regions individual ObserveScanRate
   *
   * @param observeScanRate decimal number
   */
  public void setObserveScanRate(float observeScanRate) {
    this.observeScanRate = observeScanRate;
  }

  /**
   * INTERNAL USE: Observe
   * @return the regions current RepeatWaitTime time in seconds
   */
  public int getRepeatWaitTime() {
    return repeatWaitTime;
  }

  /**
   * INTERNAL USE: Observe
   * set the regions individual WaitForVanish
   *
   * @param time in seconds
   */
  public void setRepeatWaitTime(int time) {
    repeatWaitTime = time;
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getters / setters / modificators">
  /**
   *
   * @return the Screen object containing the region
   */
  public IScreen getScreen() {
    return scr;
  }

  // to avoid NPE for Regions being outside any screen
  private IRobot getRobotForRegion() {
    if (getScreen() == null) {
      return Screen.getPrimaryScreen().getRobot();
    }
    return getScreen().getRobot();
  }

  /**
   *
   * @return the screen, that contains the top left corner of the region. Returns primary screen if outside of any
   * screen.
   * @deprecated Only for compatibility, to get the screen containing this region, use {@link #getScreen()}
   */
  @Deprecated
  public IScreen getScreenContaining() {
    return getScreen();
  }

  /**
   * Sets a new Screen for this region.
   *
   * @param scr the containing screen object
	 * @return
   */
  protected Region setScreen(IScreen scr) {
    initScreen(scr);
    return this;
  }

  /**
   * Sets a new Screen for this region.
   *
   * @param id the containing screen object's id
	 * @return
   */
  protected Region setScreen(int id) {
    return setScreen(Screen.getScreen(id));
  }

  /**
   * synonym for showMonitors
   */
  public void showScreens() {
    Screen.showMonitors();
  }

  /**
   * synonym for resetMonitors
   */
  public void resetScreens() {
    Screen.resetMonitors();
  }

  // ************************************************
  /**
   *
   * @return the center pixel location of the region
   */
  public Location getCenter() {
    return checkAndSetRemote(new Location(getX() + getW() / 2, getY() + getH() / 2));
  }

  /**
   * convenience method
   *
   * @return the region's center
   */
  public Location getTarget() {
    return getCenter();
  }

  /**
   * Moves the region to the area, whose center is the given location
   *
   * @param loc the location which is the new center of the region
   * @return the region itself
   */
  public Region setCenter(Location loc) {
    Location c = getCenter();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(loc.getScreen());
    return this;
  }

  /**
   *
   * @return top left corner Location
   */
  public Location getTopLeft() {
    return checkAndSetRemote(new Location(x, y));
  }

  /**
   * Moves the region to the area, whose top left corner is the given location
   *
   * @param loc the location which is the new top left point of the region
   * @return the region itself
   */
  public Region setTopLeft(Location loc) {
    return setLocation(loc);
  }

  /**
   *
   * @return top right corner Location
   */
  public Location getTopRight() {
    return checkAndSetRemote(new Location(x + w - 1, y));
  }

  /**
   * Moves the region to the area, whose top right corner is the given location
   *
   * @param loc the location which is the new top right point of the region
   * @return the region itself
   */
  public Region setTopRight(Location loc) {
    Location c = getTopRight();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(getScreen());
    return this;
  }

  /**
   *
   * @return bottom left corner Location
   */
  public Location getBottomLeft() {
    return checkAndSetRemote(new Location(x, y + h - 1));
  }

  /**
   * Moves the region to the area, whose bottom left corner is the given location
   *
   * @param loc the location which is the new bottom left point of the region
   * @return the region itself
   */
  public Region setBottomLeft(Location loc) {
    Location c = getBottomLeft();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(getScreen());
    return this;
  }

  /**
   *
   * @return bottom right corner Location
   */
  public Location getBottomRight() {
    return checkAndSetRemote(new Location(x + w - 1, y + h - 1));
  }

  /**
   * Moves the region to the area, whose bottom right corner is the given location
   *
   * @param loc the location which is the new bottom right point of the region
   * @return the region itself
   */
  public Region setBottomRight(Location loc) {
    Location c = getBottomRight();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(null);
    return this;
  }

  // ************************************************
  /**
   *
   * @return x of top left corner
   */
  public int getX() {
    return x;
  }

  /**
   *
   * @return y of top left corner
   */
  public int getY() {
    return y;
  }

  /**
   *
   * @return width of region
   */
  public int getW() {
    return w;
  }

  /**
   *
   * @return height of region
   */
  public int getH() {
    return h;
  }

  /**
   *
   * @param X new x position of top left corner
   */
  public void setX(int X) {
    x = X;
    initScreen(scr);
  }

  /**
   *
   * @param Y new y position of top left corner
   */
  public void setY(int Y) {
    y = Y;
    initScreen(scr);
  }

  /**
   *
   * @param W new width
   */
  public void setW(int W) {
    w = W > 1 ? W : 1;
    initScreen(scr);
  }

  /**
   *
   * @param H new height
   */
  public void setH(int H) {
    h = H > 1 ? H : 1;
    initScreen(scr);
  }

  // ************************************************
  /**
   *
   * @param W new width
   * @param H new height
   * @return the region itself
   */
  public Region setSize(int W, int H) {
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(scr);
    return this;
  }

  /**
   *
   * @return the AWT Rectangle of the region
   */
  public Rectangle getRect() {
    return new Rectangle(x, y, w, h);
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param r the AWT Rectangle to use for position/size
   * @return the region itself
   */
  public Region setRect(Rectangle r) {
    return setRect(r.x, r.y, r.width, r.height);
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param X new x of top left corner
   * @param Y new y of top left corner
   * @param W new width
   * @param H new height
   * @return the region itself
   */
  public Region setRect(int X, int Y, int W, int H) {
    x = X;
    y = Y;
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(getScreen());
    return this;
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param r the region to use for position/size
   * @return the region itself
   */
  public Region setRect(Region r) {
    return setRect(r.x, r.y, r.w, r.h);
  }

  // ****************************************************

/**
   * resets this region (usually a Screen object) to the coordinates of the containing screen
   *
   * Because of the wanted side effect for the containing screen, this should only be used with screen objects.
   * For Region objects use setRect() instead.
   */
  public void setROI() {
    setROI(getScreen().getBounds());
  }

  /**
   * resets this region to the given location, and size <br> this might move the region even to another screen
   *
   * <br>Because of the wanted side effect for the containing screen, this should only be used with screen objects.
	 * <br>For Region objects use setRect() instead.
   *
   * @param X new x
   * @param Y new y
   * @param W new width
   * @param H new height
   */
  public void setROI(int X, int Y, int W, int H) {
    x = X;
    y = Y;
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(getScreen());
  }

  /**
   * resets this region to the given rectangle <br> this might move the region even to another screen
   *
   * <br>Because of the wanted side effect for the containing screen, this should only be used with screen objects.
	 * <br>For Region objects use setRect() instead.
   *
   * @param r AWT Rectangle
   */
  public void setROI(Rectangle r) {
    setROI(r.x, r.y, r.width, r.height);
  }

  /**
   * resets this region to the given region <br> this might move the region even to another screen
   *
   * <br>Because of the wanted side effect for the containing screen, this should only be used with screen objects.
	 * <br>For Region objects use setRect() instead.
   *
   * @param reg Region
   */
  public void setROI(Region reg) {
    setROI(reg.getX(), reg.getY(), reg.getW(), reg.getH());
  }

  /**
   * A function only for backward compatibility - Only makes sense with Screen objects
   *
   * @return the Region being the current ROI of the containing Screen
   */
  public Region getROI() {
    return new Region(getScreen().getRect());
  }

  // ****************************************************
  /**
   *
   * @return the region itself
   * @deprecated only for backward compatibility
   */
  @Deprecated
  public Region inside() {
    return this;
  }

  /**
   * set the regions position<br>this might move the region even to another screen
   *
   * @param loc new top left corner
   * @return the region itself
   * @deprecated to be like AWT Rectangle API use setLocation()
   */
  @Deprecated
  public Region moveTo(Location loc) {
    return setLocation(loc);
  }

  /**
   * set the regions position<br>this might move the region even to another screen
   *
   * @param loc new top left corner
   * @return the region itself
   */
  public Region setLocation(Location loc) {
    x = loc.x;
    y = loc.y;
    initScreen(scr);
    return this;
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param r Region
   * @return the region itself
   * @deprecated to be like AWT Rectangle API use setRect() instead
   */
  @Deprecated
  public Region morphTo(Region r) {
    return setRect(r);
  }

  /**
   * resize the region using the given padding values<br>might be negative
   *
   * @param l padding on left side
   * @param r padding on right side
   * @param t padding at top side
   * @param b padding at bottom side
   * @return the region itself
   */
  public Region add(int l, int r, int t, int b) {
    x = x - l;
    y = y - t;
    w = w + l + r;
    if (w < 1) {
      w = 1;
    }
    h = h + t + b;
    if (h < 1) {
      h = 1;
    }
    initScreen(getScreen());
    return this;
  }

  /**
   * extend the region, so it contains the given region<br>but only the part inside the current screen
   *
   * @param r the region to include
   * @return the region itself
   */
  public Region add(Region r) {
    Rectangle rect = getRect();
    rect.add(r.getRect());
    setRect(rect);
    initScreen(getScreen());
    return this;
  }

  /**
   * extend the region, so it contains the given point<br>but only the part inside the current screen
   *
   * @param loc the point to include
   * @return the region itself
   */
  public Region add(Location loc) {
    Rectangle rect = getRect();
    rect.add(loc.x, loc.y);
    setRect(rect);
    initScreen(getScreen());
    return this;
  }

  // ************************************************
  /**
   * a find operation saves its match on success in the used region object<br>unchanged if not successful
   *
   * @return the Match object from last successful find in this region
   */
  public Match getLastMatch() {
    return lastMatch;
  }

  // ************************************************
  /**
   * a searchAll operation saves its matches on success in the used region object<br>unchanged if not successful
   *
   * @return a Match-Iterator of matches from last successful searchAll in this region
   */
  public Iterator<Match> getLastMatches() {
    return lastMatches;
  }

  // ************************************************
  /**
   * get the last image taken on this regions screen
   *
   * @return the stored ScreenImage
   */
  public ScreenImage getLastScreenImage() {
    return getScreen().getLastScreenImageFromScreen();
  }

  /**
   * stores the lastScreenImage in the current bundle path with a created unique name
   *
   * @return the absolute file name
	 * @throws java.io.IOException if not possible
   */
  public String getLastScreenImageFile() throws IOException {
    return getScreen().getLastScreenImageFile(ImagePath.getBundlePath(), null);
  }

  /**
   * stores the lastScreenImage in the current bundle path with the given name
   *
   * @param name file name (.png is added if not there)
   * @return the absolute file name
	 * @throws java.io.IOException if not possible
   */
  public String getLastScreenImageFile(String name) throws IOException {
    return getScreen().getLastScreenImageFromScreen().getFile(ImagePath.getBundlePath(), name);
  }

  /**
   * stores the lastScreenImage in the given path with the given name
   *
	 * @param path path to use
   * @param name file name (.png is added if not there)
   * @return the absolute file name
	 * @throws java.io.IOException if not possible
   */
  public String getLastScreenImageFile(String path, String name) throws IOException {
    return getScreen().getLastScreenImageFromScreen().getFile(path, name);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="spatial operators - new regions">
  /**
   * check if current region contains given region
   *
   * @param region the other Region
   * @return true/false
   */
  public boolean contains(Region region) {
    return getRect().contains(region.getRect());
  }

  /**
   * create region with same size at top left corner offset
   *
   * @param loc use its x and y to set the offset
   * @return the new region
   */
  public Region offset(Location loc) {
    return Region.create(x + loc.x, y + loc.y, w, h, scr);
  }

  /**
   * create region with same size at top left corner offset
   *
   * @param x horizontal offset
   * @param y vertical offset
   * @return the new region
   */
  public Region offset(int x, int y) {
    return Region.create(this.x + x, this.y + y, w, h, scr);
  }

  /**
   * create a region enlarged 50 pixels on each side
   *
   * @return the new region
   * @deprecated to be like AWT Rectangle API use grow() instead
   */
  @Deprecated
  public Region nearby() {
    return grow(Settings.DefaultPadding, Settings.DefaultPadding);
  }

  /**
   * create a region enlarged range pixels on each side
   *
   * @param range the margin to be added around
   * @return the new region
   * @deprecated to be like AWT Rectangle API use grow() instaed
   */
  @Deprecated
  public Region nearby(int range) {
    return grow(range, range);
  }

  /**
   * create a region enlarged range pixels on each side
   *
   * @param range the margin to be added around
   * @return the new region
   */
  public Region grow(int range) {
    return grow(range, range);
  }

  /**
   * create a region enlarged w pixels on left and right side
   * and h pixels at top and bottom
   *
   * @param w pixels horizontally
   * @param h pixels vertically
   * @return the new region
   */
  public Region grow(int w, int h) {
    Rectangle r = getRect();
    r.grow(w, h);
    return Region.create(r.x, r.y, r.width, r.height, scr);
  }

  /**
   * create a region enlarged l pixels on left and r pixels right side
   * and t pixels at top side and b pixels a bottom side.
   * negative values go inside (shrink)
   *
   * @param l add to the left
   * @param r add to right
   * @param t add above
   * @param b add beneath
   * @return the new region
   */
  public Region grow(int l, int r, int t, int b) {
    return Region.create(x - l, y - t, w + l + r, h + t + b, scr);
  }

  /**
   * point middle on right edge
   * @return point middle on right edge
   */
  public Location rightAt() {
    return rightAt(0);
  }

  /**
   * positive offset goes to the right.
   * might be off current screen
   *
	 * @param offset pixels
   * @return point with given offset horizontally to middle point on right edge
   */
  public Location rightAt(int offset) {
    return checkAndSetRemote(new Location(x + w + offset, y + h / 2));
  }

  /**
   * create a region right of the right side with same height.
   * the new region extends to the right screen border<br>
   * use grow() to include the current region
   *
   * @return the new region
   */
  public Region right() {
    int distToRightScreenBorder = getScreen().getX() + getScreen().getW() - (getX() + getW());
    return right(distToRightScreenBorder);
  }

  /**
   * create a region right of the right side with same height and given width.
   * negative width creates the right part with width inside the region<br>
   * use grow() to include the current region
   *
   * @param width pixels
   * @return the new region
   */
  public Region right(int width) {
    int _x;
    if (width < 0) {
      _x = x + w + width;
    } else {
      _x = x + w;
    }
    return Region.create(_x, y, Math.abs(width), h, scr);
  }

  /**
   *
   * @return point middle on left edge
   */
  public Location leftAt() {
    return leftAt(0);
  }

  /**
   * negative offset goes to the left <br>might be off current screen
   *
	 * @param offset pixels
   * @return point with given offset horizontally to middle point on left edge
   */
  public Location leftAt(int offset) {
    return checkAndSetRemote(new Location(x + offset, y + h / 2));
  }

  /**
   * create a region left of the left side with same height<br> the new region extends to the left screen border<br> use
   * grow() to include the current region
   *
   * @return the new region
   */
  public Region left() {
    int distToLeftScreenBorder = getX() - getScreen().getX();
    return left(distToLeftScreenBorder);
  }

  /**
   * create a region left of the left side with same height and given width<br>
   * negative width creates the left part with width inside the region use grow() to include the current region <br>
   *
   * @param width pixels
   * @return the new region
   */
  public Region left(int width) {
    int _x;
    if (width < 0) {
      _x = x;
    } else {
      _x = x - width;
    }
    return Region.create(getScreen().getBounds().intersection(new Rectangle(_x, y, Math.abs(width), h)), scr);
  }

  /**
   *
   * @return point middle on top edge
   */
  public Location aboveAt() {
    return aboveAt(0);
  }

  /**
   * negative offset goes towards top of screen <br>might be off current screen
   *
	 * @param offset pixels
   * @return point with given offset vertically to middle point on top edge
   */
  public Location aboveAt(int offset) {
    return checkAndSetRemote(new Location(x + w / 2, y + offset));
  }

  /**
   * create a region above the top side with same width<br> the new region extends to the top screen border<br> use
   * grow() to include the current region
   *
   * @return the new region
   */
  public Region above() {
    int distToAboveScreenBorder = getY() - getScreen().getY();
    return above(distToAboveScreenBorder);
  }

  /**
   * create a region above the top side with same width and given height<br>
   * negative height creates the top part with height inside the region use grow() to include the current region
   *
   * @param height pixels
   * @return the new region
   */
  public Region above(int height) {
    int _y;
    if (height < 0) {
      _y = y;
    } else {
      _y = y - height;
    }
    return Region.create(getScreen().getBounds().intersection(new Rectangle(x, _y, w, Math.abs(height))), scr);
  }

  /**
   *
   * @return point middle on bottom edge
   */
  public Location belowAt() {
    return belowAt(0);
  }

  /**
   * positive offset goes towards bottom of screen <br>might be off current screen
   *
	 * @param offset pixels
   * @return point with given offset vertically to middle point on bottom edge
   */
  public Location belowAt(int offset) {
    return checkAndSetRemote(new Location(x + w / 2, y + h - offset));
  }

  /**
   * create a region below the bottom side with same width<br> the new region extends to the bottom screen border<br>
   * use grow() to include the current region
   *
   * @return the new region
   */
  public Region below() {
    int distToBelowScreenBorder = getScreen().getY() + getScreen().getH() - (getY() + getH());
    return below(distToBelowScreenBorder);
  }

  /**
   * create a region below the bottom side with same width and given height<br>
   * negative height creates the bottom part with height inside the region use grow() to include the current region
   *
   * @param height pixels
   * @return the new region
   */
  public Region below(int height) {
    int _y;
    if (height < 0) {
      _y = y + h + height;
    } else {
      _y = y + h;
    }
    return Region.create(x, _y, w, Math.abs(height), scr);
  }

  /**
   * create a new region containing both regions
   *
   * @param ur region to unite with
   * @return the new region
   */
  public Region union(Region ur) {
    Rectangle r = getRect().union(ur.getRect());
    return Region.create(r.x, r.y, r.width, r.height, scr);
  }

  /**
   * create a region that is the intersection of the given regions
   *
   * @param ir the region to intersect with like AWT Rectangle API
   * @return the new region
   */
  public Region intersection(Region ir) {
    Rectangle r = getRect().intersection(ir.getRect());
    return Region.create(r.x, r.y, r.width, r.height, scr);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="parts of a Region">
	/**
	 * select the specified part of the region.
	 *
	 * <br>Constants for the top parts of a region (Usage: Region.CONSTANT)<br>
	 * shown in brackets: possible shortcuts for the part constant<br>
	 * NORTH (NH, TH) - upper half <br>
	 * NORTH_WEST (NW, TL) - left third in upper third <br>
	 * NORTH_MID (NM, TM) - middle third in upper third <br>
	 * NORTH_EAST (NE, TR) - right third in upper third <br>
	 * ... similar for the other directions: <br>
	 * right side: EAST (Ex, Rx)<br>
	 * bottom part: SOUTH (Sx, Bx) <br>
	 * left side: WEST (Wx, Lx)<br>
	 * <br>
	 * specials for quartered:<br>
	 * TT top left quarter<br>
	 * RR top right quarter<br>
	 * BB bottom right quarter<br>
	 * LL bottom left quarter<br>
	 * <br>
	 * specials for the center parts:<br>
	 * MID_VERTICAL (MV, CV) half of width vertically centered <br>
	 * MID_HORIZONTAL (MH, CH) half of height horizontally centered <br>
	 * MID_BIG (M2, C2) half of width / half of height centered <br>
	 * MID_THIRD (MM, CC) third of width / third of height centered <br>
	 * <br>
	 * Based on the scheme behind these constants there is another possible usage:<br>
	 * specify part as e 3 digit integer
	 * where the digits xyz have the following meaning<br>
	 * 1st x: use a raster of x rows and x columns<br>
	 * 2nd y: the row number of the wanted cell<br>
	 * 3rd z: the column number of the wanted cell<br>
	 * y and z are counting from 0<br>
	 * valid numbers: 200 up to 999 (&lt; 200 are invalid and return the region itself) <br>
	 * example: get(522) will use a raster of 5 rows and 5 columns and return the cell in the middle<br>
	 * special cases:<br>
	 * if either y or z are == or &gt; x: returns the respective row or column<br>
	 * example: get(525) will use a raster of 5 rows and 5 columns and return the row in the middle<br>
	 * <br>
	 * internally this is based on {@link #setRaster(int, int) setRaster}
	 * and {@link #getCell(int, int) getCell} <br>
	 * <br>
	 * If you need only one row in one column with x rows or
	 * only one column in one row with x columns you can use {@link #getRow(int, int) getRow} or {@link #getCol(int, int) getCol}
	 * @param part the part to get (Region.PART long or short)
	 * @return new region
	 */
	public Region get(int part) {
		return Region.create(getRectangle(getRect(), part));
	}

	protected static Rectangle getRectangle(Rectangle rect, int part) {
		if (part < 200 || part > 999) {
			return rect;
		}
		Region r = Region.create(rect);
		int pTyp = (int) (part / 100);
		int pPos = part - pTyp * 100;
		int pRow = (int) (pPos / 10);
		int pCol = pPos - pRow * 10;
		r.setRaster(pTyp, pTyp);
		if (pTyp == 3) {
			// NW = 300, NORTH_WEST = NW;
			// NM = 301, NORTH_MID = NM;
			// NE = 302, NORTH_EAST = NE;
			// EM = 312, EAST_MID = EM;
			// SE = 322, SOUTH_EAST = SE;
			// SM = 321, SOUTH_MID = SM;
			// SW = 320, SOUTH_WEST = SW;
			// WM = 310, WEST_MID = WM;
			// MM = 311, MIDDLE = MM, M3 = MM;
			return r.getCell(pRow, pCol).getRect();
		}
		if (pTyp == 2) {
			// NH = 202, NORTH = NH;
			// EH = 221, EAST = EH;
			// SH = 212, SOUTH = SH;
			// WH = 220, WEST = WH;
			if (pRow > 1) {
				return r.getCol(pCol).getRect();
			} else if (pCol > 1) {
				return r.getRow(pRow).getRect();
			}
			return r.getCell(pRow, pCol).getRect();
		}
		if (pTyp == 4) {
			// MV = 441, MID_VERTICAL = MV;
			// MH = 414, MID_HORIZONTAL = MH;
			// M2 = 444, MIDDLE_BIG = M2;
			if (pRow > 3) {
				if (pCol > 3 ) {
					return r.getCell(1, 1).union(r.getCell(2, 2)).getRect();
				}
				return r.getCell(0, 1).union(r.getCell(3, 2)).getRect();
			} else if (pCol > 3) {
				return r.getCell(1, 0).union(r.getCell(2, 3)).getRect();
			}
			return r.getCell(pRow, pCol).getRect();
		}
		return rect;
	}

	/**
	 * store info: this region is divided vertically into n even rows <br>
	 * a preparation for using getRow()
	 *
	 * @param n number of rows
	 * @return the top row
	 */
	public Region setRows(int n) {
		return setRaster(n, 0);
	}

	/**
	 * store info: this region is divided horizontally into n even columns <br>
	 * a preparation for using getCol()
	 *
	 * @param n number of columns
	 * @return the leftmost column
	 */
	public Region setCols(int n) {
		return setRaster(0, n);
	}

	/**
	 *
	 * @return the number of rows or null
	 */
	public int getRows() {
		return rows;
	}

	/**
	 *
	 * @return the row height or 0
	 */
	public int getRowH() {
		return rowH;
	}

	/**
	 *
	 * @return the number of columns or 0
	 */
	public int getCols() {
		return cols;
	}

	/**
	 *
	 * @return the columnwidth or 0
	 */
	public int getColW() {
		return colW;
	}

	/**
	 * Can be used to check, wether the Region currently has a valid raster
	 * @return true if it has a valid raster (either getCols or getRows or both would return &gt; 0)
	 * false otherwise
	 */
	public boolean isRasterValid() {
		return (rows > 0 || cols > 0);
	}

	/**
	 * store info: this region is divided into a raster of even cells <br>
	 * a preparation for using getCell()<br>
	 * @param r number of rows
	 * @param c number of columns
	 * @return the topleft cell
	 */
	public Region setRaster(int r, int c) {
		rows = Math.max(r, h);
		cols = Math.max(c, w);
		if (r > 0) {
			rowH = (int) (h / r);
			rowHd = h - r * rowH;
		}
		if (c > 0) {
			colW = (int) (w / c);
			colWd = w - c * colW;
		}
		return getCell(0, 0);
	}

	/**
	 * get the specified row counting from 0, if rows or raster are setup negative counts reverse from the end (last = -1)
	 * values outside range are 0 or last respectively
	 *
	 * @param r row number
	 * @return the row as new region or the region itself, if no rows are setup
	 */
	public Region getRow(int r) {
		if (rows == 0) {
			return this;
		}
		if (r < 0) {
			r = rows + r;
		}
		r = Math.max(0, r);
		r = Math.min(r, rows - 1);
		return Region.create(x, y + r * rowH, w, rowH);
	}

	public Region getRow(int r, int n) {
		return this;
	}

	/**
	 * get the specified column counting from 0, if columns or raster are setup negative counts reverse from the end (last
	 * = -1) values outside range are 0 or last respectively
	 *
	 * @param c column number
	 * @return the column as new region or the region itself, if no columns are setup
	 */
	public Region getCol(int c) {
		if (cols == 0) {
			return this;
		}
		if (c < 0) {
			c = cols + c;
		}
		c = Math.max(0, c);
		c = Math.min(c, cols - 1);
		return Region.create(x + c * colW, y, colW, h);
	}

	/**
	 * divide the region in n columns and select column c as new Region
	 * @param c the column to select counting from 0 or negative to count from the end
	 * @param n how many columns to devide in
	 * @return the selected part or the region itself, if parameters are invalid
	 */
	public Region getCol(int c, int n) {
		Region r = new Region(this);
		r.setCols(n);
		return r.getCol(c);
	}


	/**
	 * get the specified cell counting from (0, 0), if a raster is setup <br>
	 * negative counts reverse from the end (last = -1) values outside range are 0 or last respectively
	 *
	 * @param r row number
	 * @param c column number
	 * @return the cell as new region or the region itself, if no raster is setup
	 */
	public Region getCell(int r, int c) {
		if (rows == 0) {
			return getCol(c);
		}
		if (cols == 0) {
			return getRow(r);
		}
		if (rows == 0 && cols == 0) {
			return this;
		}
		if (r < 0) {
			r = rows - r;
		}
		if (c < 0) {
			c = cols - c;
		}
		r = Math.max(0, r);
		r = Math.min(r, rows - 1);
		c = Math.max(0, c);
		c = Math.min(c, cols - 1);
		return Region.create(x + c * colW, y + r * rowH, colW, rowH);
	}
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="highlight">
  protected void updateSelf() {
    if (overlay != null) {
      highlight(false, null);
      highlight(true, null);
    }
  }

  /**
   * Toggle the regions Highlight visibility (red frame)
   *
   * @return the region itself
   */
  public Region highlight() {
    // Pass true if overlay is null, false otherwise
    highlight(overlay == null, null);
    return this;
  }

  /**
   * Toggle the regions Highlight visibility (frame of specified color)<br>
   * allowed color specifications for frame color: <br>
   * - a color name out of: black, blue, cyan, gray, green, magenta, orange, pink, red, white, yellow
   * (lowercase and uppercase can be mixed, internally transformed to all uppercase) <br>
   * - these colornames exactly written: lightGray, LIGHT_GRAY, darkGray and DARK_GRAY <br>
   * - a hex value like in HTML: #XXXXXX (max 6 hex digits)
   * - an RGB specification as: #rrrgggbbb where rrr, ggg, bbb are integer values in range 0 - 255
   * padded with leading zeros if needed (hence exactly 9 digits)
   * @param color Color of frame
   * @return the region itself
   */
  public Region highlight(String color) {
    // Pass true if overlay is null, false otherwise
    highlight(overlay == null, color);
    return this;
  }

  /**
   * Sets the regions Highlighting border
   *
   * @param toEnable set overlay enabled or disabled
   * @param color Color of frame (see method highlight(color))
   */
  private Region highlight(boolean toEnable, String color) {
    if (isOtherScreen()) {
      return this;
    }
    Debug.action("toggle highlight " + toString() + ": " + toEnable +
                (color != null ? " color: " + color : ""));
    if (toEnable) {
      overlay = new ScreenHighlighter(getScreen(), color);
      overlay.highlight(this);
    } else {
      if (overlay != null) {
        overlay.close();
        overlay = null;
      }
    }
    return this;
  }


  /**
   * show the regions Highlight for the given time in seconds (red frame)
   * if 0 - use the global Settings.SlowMotionDelay
   *
   * @param secs time in seconds
   * @return the region itself
   */
  public Region highlight(float secs) {
    return highlight(secs, null);
  }

  /**
   * show the regions Highlight for the given time in seconds (frame of specified color)
   * if 0 - use the global Settings.SlowMotionDelay
   *
   * @param secs time in seconds
   * @param color Color of frame (see method highlight(color))
   * @return the region itself
   */
  public Region highlight(float secs, String color) {
    if (isOtherScreen()) {
      return this;
    }
    if (secs < 0.1) {
      return highlight((int) secs, color);
    }
    Debug.action("highlight " + toString() + " for " + secs + " secs" +
          (color != null ? " color: " + color : ""));
    ScreenHighlighter _overlay = new ScreenHighlighter(getScreen(), color);
    _overlay.highlight(this, secs);
    return this;
  }

  /**
   * hack to implement the getLastMatch() convenience 0 means same as highlight() &lt; 0 same as highlight(secs) if
   * available the last match is highlighted
   *
   * @param secs seconds
   * @return this region
   */
  public Region highlight(int secs) {
    return highlight(secs, null);
  }


  /**
   * Show highlight in selected color
   *
   * @param secs time in seconds
   * @param color Color of frame (see method highlight(color))
   * @return this region
   */
  public Region highlight(int secs, String color) {
    if (isOtherScreen()) {
      return this;
    }
    if (secs > 0) {
      return highlight((float) secs, color);
    }
    if (lastMatch != null) {
      if (secs < 0) {
        return lastMatch.highlight((float) -secs, color);
      }
      return lastMatch.highlight(Settings.DefaultHighlightTime, color);
    }
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="find public methods">
  /**
   * WARNING: wait(long timeout) is taken by Java Object as final. This method catches any interruptedExceptions
   *
   * @param timeout The time to wait
   */
  public void wait(double timeout) {
    try {
      Thread.sleep((long) (timeout * 1000L));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the image of the given Pattern, String or Image
   *
   * @param target The Pattern, String or Image
   * @return the Image
   */
  private <PSI> Image getImage(PSI target) {
    if (target instanceof Pattern) {
      return ((Pattern) target).getImage();
    } else if (target instanceof String) {
      return Image.get((String) target);
    } else if (target instanceof Image) {
      return (Image) target;
    } else {
      return null;
    }
  }

  /**
   * return false to skip <br>
   * return true to try again <br>
   * throw FindFailed to abort
   *
   * @param target Handles a failed find action
   * @throws FindFailed
   */
  private <PSI> boolean handleFindFailed(PSI target) throws FindFailed {
    return handleFindFailedShowDialog(target, false);
  }

  private <PSI> boolean handleFindFailedQuietly(PSI target) {
    try {
      return handleFindFailedShowDialog(target, false);
    } catch (FindFailed ex) {
    }
    return false;
  }

  private <PSI> boolean handleFindFailedImageMissing(PSI target) {
    boolean shouldHandle = false;
    try {
      shouldHandle = handleFindFailedShowDialog(target, true);
    } catch (FindFailed ex) {
      return false;
    }
    if (!shouldHandle) {
      return false;
    }
    getRobotForRegion().delay(500);
    ScreenImage img = getScreen().userCapture("capture missing image");
    if (img != null) {
      String path = ImagePath.getBundlePath();
      if (path == null) {
        return false;
      }
      String imgName = (String) target;
      img.getFile(path, imgName);
      return true;
    }
    return false;
  }

  private boolean handleFindFailedShowDialog(Object target, boolean shouldCapture) throws FindFailed {
    FindFailedResponse response;
    if (findFailedResponse == FindFailedResponse.PROMPT) {
      FindFailedDialog fd = new FindFailedDialog(target, shouldCapture);
      fd.setVisible(true);
      response = fd.getResponse();
      fd.dispose();
      wait(0.5);
    } else {
      response = findFailedResponse;
    }
    if (response == FindFailedResponse.SKIP) {
      return false;
    } else if (response == FindFailedResponse.RETRY) {
      return true;
		} else if (response == FindFailedResponse.ABORT) {
			String targetStr = target.toString();
			if (target instanceof String) {
				targetStr = targetStr.trim();
			}
			throw new FindFailed(String.format("can not find %s in %s", targetStr, this.toStringShort()));
		}
    return false;
  }

  /**
   * finds the given Pattern, String or Image in the region and returns the best match. If AutoWaitTimeout
   * is set, this is equivalent to wait(). Otherwise only one search attempt will be done.
   *
	 * @param <PSI> Pattern, String or Image
   * @param target A search criteria
   * @return If found, the element. null otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Match find(PSI target) throws FindFailed {
    if (autoWaitTimeout > 0) {
      return wait(target, autoWaitTimeout);
    }
    lastMatch = null;
		String targetStr = target.toString();
		if (target instanceof String) {
			targetStr = targetStr.trim();
		}
    while (true) {
      try {
        log(2, "find: waiting 0 secs for %s to appear in %s", targetStr, this.toStringShort());
        lastMatch = doFind(target, null);
      } catch (IOException ex) {
        if (ex instanceof IOException) {
          if (handleFindFailedImageMissing(target)) {
            continue;
          }
        }
        throw new FindFailed(ex.getMessage());
      }
      if (lastMatch != null) {
        Image img = getImage(target);
        lastMatch.setImage(img);
        if (img != null) {
          img.setLastSeen(lastMatch.getRect(), lastMatch.getScore());
        }
	      log(lvl, "find: %s has appeared \nat %s", targetStr, lastMatch);
        return lastMatch;
      }
	    log(2, "find: %s has not appeared [%d msec]", targetStr, lastFindTime);
      if (!handleFindFailed(target)) {
        return null;
      }
    }
  }

  /**
   * finds all occurences of the given Pattern, String or Image in the region and returns an Iterator of Matches.
	 *
   *
	 * @param <PSI> Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Iterator<Match> findAll(PSI target) throws FindFailed {
    lastMatches = null;
		String targetStr = target.toString();
		if (target instanceof String) {
			targetStr = targetStr.trim();
		}
    while (true) {
      try {
        if (autoWaitTimeout > 0) {
          RepeatableFindAll rf = new RepeatableFindAll(target);
          rf.repeat(autoWaitTimeout);
          lastMatches = rf.getMatches();
        } else {
          lastMatches = doFindAll(target, null);
        }
      } catch (Exception ex) {
        if (ex instanceof IOException) {
          if (handleFindFailedImageMissing(target)) {
            continue;
          }
        }
        throw new FindFailed(ex.getMessage());
      }
      if (lastMatches != null) {
        return lastMatches;
      }
      if (!handleFindFailed(target)) {
        return null;
      }
    }
  }

  /**
   * Waits for the Pattern, String or Image to appear until the AutoWaitTimeout value is exceeded.
   *
	 * @param <PSI> Pattern, String or Image
   * @param target The target to search for
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target) throws FindFailed {
    return wait(target, autoWaitTimeout);
  }

  /**
   * Waits for the Pattern, String or Image to appear or timeout (in second) is passed
   *
	 * @param <PSI> Pattern, String or Image
   * @param target The target to search for
   * @param timeout Timeout in seconds
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target, double timeout) throws FindFailed {
    RepeatableFind rf;
    lastMatch = null;
		String targetStr = target.toString();
		if (target instanceof String) {
			targetStr = targetStr.trim();
		}
    while (true) {
      try {
        log(2, "find: waiting %.1f secs for %s to appear in %s", timeout, targetStr, this.toStringShort());
        rf = new RepeatableFind(target);
        rf.repeat(timeout);
        lastMatch = rf.getMatch();
      } catch (Exception ex) {
        if (ex instanceof IOException) {
          if (handleFindFailedImageMissing(target)) {
            continue;
          }
        }
        throw new FindFailed(ex.getMessage());
      }
      if (lastMatch != null) {
        lastMatch.setImage(rf._image);
        if (rf._image != null) {
          rf._image.setLastSeen(lastMatch.getRect(), lastMatch.getScore());
        }
        log(lvl, "find: %s has appeared \nat %s", targetStr, lastMatch);
        break;
      }
	    log(2, "find: %s has not appeared [%d msec]", targetStr, lastFindTime);
      if (!handleFindFailed(target)) {
        return null;
      }
    }
    return lastMatch;
  }

//TODO 1.2.0 Region.compare as time optimized Region.exists
	/**
	 * time optimized Region.exists, when image-size == region-size<br>
	 * 1.1.x: just using exists(img, 0), sizes not checked
	 * @param img image file name
	 * @return the match or null if not equal
	 */
		public Match compare(String img) {
		return compare(Image.create(img));
	}

	/**
	 * time optimized Region.exists, when image-size == region-size<br>
	 * 1.1.x: just using exists(img, 0), sizes not checked
	 * @param img Image object
	 * @return the match or null if not equal
	 */
	public Match compare(Image img) {
		return exists(img, 0);
	}

	/**
   * Check if target exists (with the default autoWaitTimeout)
   *
	 * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target) {
    return exists(target, autoWaitTimeout);
  }

  /**
   * Check if target exists with a specified timeout<br>
   * timout = 0: returns immediately after first search
   *
	 * @param <PSI> Pattern, String or Image
   * @param target The target to search for
   * @param timeout Timeout in seconds
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target, double timeout) {
    lastMatch = null;
		String targetStr = target.toString();
		if (target instanceof String) {
			targetStr = targetStr.trim();
		}
    while (true) {
      try {
        log(2, "exists: waiting %.1f secs for %s to appear in %s", timeout, targetStr, this.toStringShort());
        RepeatableFind rf = new RepeatableFind(target);
        if (rf.repeat(timeout)) {
          lastMatch = rf.getMatch();
          Image img = getImage(target);
          lastMatch.setImage(img);
          if (img != null) {
            img.setLastSeen(lastMatch.getRect(), lastMatch.getScore());
          }
	        log(lvl, "exists: %s has appeared \nat %s", targetStr, lastMatch);
          return lastMatch;
        } else {
          if (!handleFindFailedQuietly(target)) {
            break;
          }
        }
      } catch (Exception ex) {
        if (ex instanceof IOException) {
          if (!handleFindFailedImageMissing(target)) {
            break;
          }
        }
      }
    }
    log(2, "exists: %s has not appeared [%d msec]", targetStr, lastFindTime);
    return null;
  }

  /**
   * Use findText() instead of find() in cases where the given string could be misinterpreted as an image filename
   *
   * @param text text
   * @param timeout time
   * @return the matched region containing the text
	 * @throws org.sikuli.script.FindFailed if not found
   */
  public Match findText(String text, double timeout) throws FindFailed {
    // the leading/trailing tab is used to internally switch to text search directly
    return wait("\t" + text + "\t", timeout);
  }

  /**
   * Use findText() instead of find() in cases where the given string could be misinterpreted as an image filename
   *
   * @param text text
   * @return the matched region containing the text
	 * @throws org.sikuli.script.FindFailed if not found
   */
  public Match findText(String text) throws FindFailed {
    return findText(text, autoWaitTimeout);
  }

  /**
   * Use findAllText() instead of findAll() in cases where the given string could be misinterpreted as an image filename
   *
   * @param text text
   * @return the matched region containing the text
	 * @throws org.sikuli.script.FindFailed  if not found
   */
  public Iterator<Match> findAllText(String text) throws FindFailed {
    // the leading/trailing tab is used to internally switch to text search directly
    return findAll("\t" + text + "\t");
  }

  /**
   * waits until target vanishes or timeout (in seconds) is
   * passed (AutoWaitTimeout)
   *
	 * @param <PSI> Pattern, String or Image
	 * @param target The target to wait for it to vanish
   * @return true if the target vanishes, otherwise returns false.
   */
  public <PSI> boolean waitVanish(PSI target) {
    return waitVanish(target, autoWaitTimeout);
  }

  /**
   * waits until target vanishes or timeout (in seconds) is
   * passed
   *
	 * @param <PSI> Pattern, String or Image
	 * @param target Pattern, String or Image
	 * @param timeout time in seconds
   * @return true if target vanishes, false otherwise and if imagefile is missing.
   */
  public <PSI> boolean waitVanish(PSI target, double timeout) {
    while (true) {
      try {
        log(lvl, "waiting for " + target + " to vanish");
        RepeatableVanish r = new RepeatableVanish(target);
        if (r.repeat(timeout)) {
          log(lvl, "" + target + " has vanished");
          return true;
        }
        log(lvl, "" + target + " has not vanished before timeout");
        return false;
      } catch (Exception ex) {
        if (ex instanceof IOException) {
          if (handleFindFailedImageMissing(target)) {
            continue;
          }
        }
        break;
      }
    }
    return false;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="find internal methods">
  /**
   * Match doFind( Pattern/String/Image ) finds the given pattern on the screen and returns the best match without
   * waiting.
   */
  private <PSI> Match doFind(PSI ptn, RepeatableFind repeating) throws IOException {
    Finder f = null;
    Match m = null;
    boolean findingText = false;
    lastFindTime = (new Date()).getTime();
    ScreenImage simg;
    if (repeating != null && repeating._finder != null) {
      simg = getScreen().capture(x, y, w, h);
      f = repeating._finder;
      f.setScreenImage(simg);
      f.setRepeating();
      lastSearchTime = (new Date()).getTime();
      f.findRepeat();
    } else {
      Image img = null;
      if (ptn instanceof String) {
        if (((String) ptn).startsWith("\t") && ((String) ptn).endsWith("\t")) {
          findingText = true;
        } else {
          img = Image.create((String) ptn);
          if (img.isValid()) {
            lastSearchTime = (new Date()).getTime();
            f = checkLastSeenAndCreateFinder(img, repeating.getFindTimeOut(), null);
            if (!f.hasNext()) {
              f.find(img);
            }
          } else if (img.isText()) {
            findingText = true;
          } else {
            throw new IOException("Region: doFind: Image not loadable: " + (String) ptn);
          }
        }
				if (findingText) {
					if (TextRecognizer.getInstance() != null) {
						log(lvl, "doFind: Switching to TextSearch");
						f = new Finder(getScreen().capture(x, y, w, h), this);
						lastSearchTime = (new Date()).getTime();
						f.findText((String) ptn);
					}
        }
      } else if (ptn instanceof Pattern) {
        if (((Pattern) ptn).isValid()) {
          img = ((Pattern) ptn).getImage();
          lastSearchTime = (new Date()).getTime();
          f = checkLastSeenAndCreateFinder(img, repeating.getFindTimeOut(), (Pattern) ptn);
          if (!f.hasNext()) {
            f.find((Pattern) ptn);
          }
        } else {
          throw new IOException("Region: doFind: Image not loadable: " + (String) ptn);
        }
      } else if (ptn instanceof Image) {
        if (((Image) ptn).isValid()) {
          img = ((Image) ptn);
          lastSearchTime = (new Date()).getTime();
          f = checkLastSeenAndCreateFinder(img, repeating.getFindTimeOut(), null);
          if (!f.hasNext()) {
            f.find(img);
          }
        } else {
          throw new IOException("Region: doFind: Image not loadable: " + (String) ptn);
        }
      } else {
        log(-1, "doFind: invalid parameter: %s", ptn);
        Sikulix.terminate(999);
      }
      if (repeating != null) {
        repeating._finder = f;
        repeating._image = img;
      }
    }
    lastSearchTime = (new Date()).getTime() - lastSearchTime;
    lastFindTime = (new Date()).getTime() - lastFindTime;
    if (f.hasNext()) {
      m = f.next();
      m.setTimes(lastFindTime, lastSearchTime);
    }
    return m;
  }

  private Finder checkLastSeenAndCreateFinder(Image img, double findTimeout, Pattern ptn) {
    if (!Settings.UseImageFinder && Settings.CheckLastSeen && null != img.getLastSeen()) {
      Region r = Region.create(img.getLastSeen());
      if (this.contains(r)) {
        Finder f = null;
        if (this.scr instanceof VNCScreen) {
          f = new Finder(new VNCScreen().capture(r), r);
        } else {
          f = new Finder(new Screen().capture(r), r);
        }
        if (ptn == null) {
          f.find(new Pattern(img).similar(Settings.CheckLastSeenSimilar));
        } else {
          f.find(new Pattern(ptn).similar(Settings.CheckLastSeenSimilar));
        }
        if (f.hasNext()) {
          log(lvl, "checkLastSeen: still there");
          return f;
        }
        log(lvl, "checkLastSeen: not there");
      }
    }
    if (Settings.UseImageFinder) {
      ImageFinder f = new ImageFinder(this);
      f.setFindTimeout(findTimeout);
      return f;
    } else {
      return new Finder(getScreen().capture(x, y, w, h), this);
    }
  }

  /**
   * Match findAllNow( Pattern/String/Image ) finds all the given pattern on the screen and returns the best matches
   * without waiting.
   */
  private <PSI> Iterator<Match> doFindAll(PSI ptn, RepeatableFindAll repeating) throws IOException {
    boolean findingText = false;
    Finder f;
    ScreenImage simg = getScreen().capture(x, y, w, h);
    if (repeating != null && repeating._finder != null) {
      f = repeating._finder;
      f.setScreenImage(simg);
      f.setRepeating();
      f.findAllRepeat();
    } else {
      f = new Finder(simg, this);
      Image img = null;
      if (ptn instanceof String) {
        if (((String) ptn).startsWith("\t") && ((String) ptn).endsWith("\t")) {
          findingText = true;
        } else {
          img = Image.create((String) ptn);
          if (img.isValid()) {
            f.findAll(img);
          } else if (img.isText()) {
            findingText = true;
          } else {
            throw new IOException("Region: doFind: Image not loadable: " + (String) ptn);
          }
        }
        if (findingText) {
          f.findAllText((String) ptn);
        }
      } else if (ptn instanceof Pattern) {
        if (((Pattern) ptn).isValid()) {
          img = ((Pattern) ptn).getImage();
          f.findAll((Pattern) ptn);
        } else {
          throw new IOException("Region: doFind: Image not loadable: " + (String) ptn);
        }
      } else if (ptn instanceof Image) {
        if (((Image) ptn).isValid()) {
          img = ((Image) ptn);
          f.findAll((Image) ptn);
        } else {
          throw new IOException("Region: doFind: Image not loadable: " + (String) ptn);
        }
      } else {
        log(-1, "doFind: invalid parameter: %s", ptn);
        Sikulix.terminate(999);
      }
      if (repeating != null) {
        repeating._finder = f;
        repeating._image = img;
      }
    }
    if (f.hasNext()) {
      return f;
    }
    return null;
  }

  // Repeatable Find ////////////////////////////////
  private abstract class Repeatable {

    private double findTimeout;

    abstract void run() throws Exception;

    abstract boolean ifSuccessful();

    double getFindTimeOut() {
      return findTimeout;
    }

    // return TRUE if successful before timeout
    // return FALSE if otherwise
    // throws Exception if any unexpected error occurs
    boolean repeat(double timeout) throws Exception {
      findTimeout = timeout;
      int MaxTimePerScan = (int) (1000.0 / waitScanRate);
      int timeoutMilli = (int) (timeout * 1000);
      long begin_t = (new Date()).getTime();
      do {
        long before_find = (new Date()).getTime();
        run();
        if (ifSuccessful()) {
          return true;
        } else if (timeoutMilli < MaxTimePerScan || Settings.UseImageFinder) {
          // instant return on first search failed if timeout very small or 0
          // or when using new ImageFinder
          return false;
        }
        long after_find = (new Date()).getTime();
        if (after_find - before_find < MaxTimePerScan) {
          getRobotForRegion().delay((int) (MaxTimePerScan - (after_find - before_find)));
        } else {
          getRobotForRegion().delay(10);
        }
      } while (begin_t + timeout * 1000 > (new Date()).getTime());
      return false;
    }
  }

  private class RepeatableFind extends Repeatable {

    Object _target;
    Match _match = null;
    Finder _finder = null;
    Image _image = null;

    public <PSI> RepeatableFind(PSI target) {
      _target = target;
    }

    public Match getMatch() {
      if (_finder != null) {
        _finder.destroy();
      }
      return (_match == null) ? _match : new Match(_match);
    }

    @Override
    public void run() throws IOException {
      _match = doFind(_target, this);
    }

    @Override
    boolean ifSuccessful() {
      return _match != null;
    }
  }

  private class RepeatableVanish extends RepeatableFind {

    public <PSI> RepeatableVanish(PSI target) {
      super(target);
    }

    @Override
    boolean ifSuccessful() {
      return _match == null;
    }
  }

  private class RepeatableFindAll extends Repeatable {

    Object _target;
    Iterator<Match> _matches = null;
    Finder _finder = null;
    Image _image = null;

    public <PSI> RepeatableFindAll(PSI target) {
      _target = target;
    }

    public Iterator<Match> getMatches() {
      return _matches;
    }

    @Override
    public void run() throws IOException {
      _matches = doFindAll(_target, this);
    }

    @Override
    boolean ifSuccessful() {
      return _matches != null;
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Find internal support">
//  private <PatternStringRegionMatch> Region getRegionFromTarget(PatternStringRegionMatch target) throws FindFailed {
//    if (target instanceof Pattern || target instanceof String || target instanceof Image) {
//      Match m = find(target);
//      if (m != null) {
//        return m.setScreen(scr);
//      }
//      return null;
//    }
//    if (target instanceof Region) {
//      return ((Region) target).setScreen(scr);
//    }
//    return null;
//  }

  private <PSIMRL> Location getLocationFromTarget(PSIMRL target) throws FindFailed {
    if (target instanceof Pattern || target instanceof String || target instanceof Image) {
      Match m = find(target);
      if (m != null) {
        if (isOtherScreen()) {
          return m.getTarget().setOtherScreen(scr);
        } else {
          return m.getTarget();
        }
      }
      return null;
    }
    if (target instanceof Match) {
      return ((Match) target).getTarget();
    }
    if (target instanceof Region) {
      return ((Region) target).getCenter();
    }
    if (target instanceof Location) {
      return new Location((Location) target);
    }
    return null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Observing">

	  protected Observer getObserver() {
    if (regionObserver == null) {
      regionObserver = new Observer(this);
    }
    return regionObserver;
  }

  /**
   * evaluate if at least one event observer is defined for this region (the observer need not be running)
   * @return true, if the region has an observer with event observers
   */
  public boolean hasObserver() {
    if (regionObserver != null) {
      return regionObserver.hasObservers();
    }
    return false;
  }

  /**
   *
   * @return true if an observer is running for this region
   */
  public boolean isObserving() {
    return observing;
  }

  /**
   *
   * @return true if any events have happened for this region, false otherwise
   */
  public boolean hasEvents() {
    return Observing.hasEvents(this);
  }

  /**
   * the region's events are removed from the list
   * @return the region's happened events as array if any (size might be 0)
   */
  public ObserveEvent[] getEvents() {
    return Observing.getEvents(this);
  }

  /**
   * the event is removed from the list
   * @param name event's name
   * @return the named event if happened otherwise null
   */
  public ObserveEvent getEvent(String name) {
    return Observing.getEvent(name);
  }

  /**
   * set the observer with the given name inactive (not checked while observing)
   * @param name observers name
   */
  public void setInactive(String name) {
    if (!hasObserver()) {
      return;
    }
    Observing.setActive(name, false);
  }

  /**
   * set the observer with the given name active (not checked while observing)
   * @param name  observers name
   */
  public void setActive(String name) {
    if (!hasObserver()) {
      return;
    }
    Observing.setActive(name, true);
  }

  /**
   * a subsequently started observer in this region should wait for target
   * and notify the given observer about this event<br>
   * for details about the observe event handler: {@link ObserverCallBack}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public <PSI> String onAppear(PSI target, Object observer) {
    return onAppearDo(target, observer);
  }

  /**
   * a subsequently started observer in this region should wait for target
   * success and details about the event can be obtained using @{link Observing}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the event's name
   */
  public <PSI> String onAppear(PSI target) {
    return onAppearDo(target, null);
  }

  /**
   *INTERNAL USE ONLY: for use with scripting API bridges
   * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public <PSI> String onAppearJ(PSI target, Object observer) {
    return onAppearDo(target, observer);
  }

  private <PSI> String onAppearDo(PSI target, Object observer) {
    String name = Observing.add(this,
            (ObserverCallBack) observer, ObserveEvent.Type.APPEAR, target);
    log(lvl, "%s: onAppear%s: %s with: %s", toStringShort(),
            (observer == null ? "" : " with callback"), name, target);
    return name;
  }

  /**
   * a subsequently started observer in this region should wait for the target to vanish
   * and notify the given observer about this event<br>
   * for details about the observe event handler: {@link ObserverCallBack}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public <PSI> String onVanish(PSI target, Object observer) {
    return onVanishDo(target, observer);
  }

  /**
   * a subsequently started observer in this region should wait for the target to vanish
   * success and details about the event can be obtained using @{link Observing}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the event's name
   */
  public <PSI> String onVanish(PSI target) {
    return onVanishDo(target, null);
  }

  /**
   *INTERNAL USE ONLY: for use with scripting API bridges
   * @param <PSI> Pattern, String or Image
   * @param target Pattern, String or Image
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public <PSI> String onVanishJ(PSI target, Object observer) {
    return onVanishDo(target, observer);
  }

  private <PSI> String onVanishDo(PSI target, Object observer) {
    String name = Observing.add(this,
            (ObserverCallBack) observer, ObserveEvent.Type.VANISH, target);
    log(lvl, "%s: onVanish%s: %s with: %s", toStringShort(),
            (observer == null ? "" : " with callback"), name, target);
    return name;
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region
   * and notify the given observer about this event
   * for details about the observe event handler: {@link ObserverCallBack}
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @param threshold minimum size of changes (rectangle threshhold x threshold)
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public String onChange(int threshold, Object observer) {
    return onChangeDo(threshold, observer);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region
   * success and details about the event can be obtained using @{link Observing}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @param threshold minimum size of changes (rectangle threshhold x threshold)
   * @return the event's name
   */
  public String onChange(int threshold) {
    return onChangeDo(threshold, null);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region
   * and notify the given observer about this event <br>
   * minimum size of changes used: Settings.ObserveMinChangedPixels
   * for details about the observe event handler: {@link ObserverCallBack}
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public String onChange(Object observer) {
    return onChangeDo(0, observer);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region
   * success and details about the event can be obtained using @{link Observing}<br>
   * minimum size of changes used: Settings.ObserveMinChangedPixels
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @return the event's name
   */
  public String onChange() {
    return onChangeDo(0, null);
  }

  /**
   *INTERNAL USE ONLY: for use with scripting API bridges
   * @param minSize value
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public String onChangeJ(int minSize, Object observer) {
    if (minSize == 0) {
      return onChangeDo(Settings.ObserveMinChangedPixels, observer);
    } else {
      return onChangeDo(minSize, observer);
    }
  }

  public String onChangeDo(int threshold, Object observer) {
    String name = Observing.add(this, (ObserverCallBack) observer, ObserveEvent.Type.CHANGE, threshold);
    log(lvl, "%s: onChange%s: %s minSize: %d", toStringShort(),
            (observer == null ? "" : " with callback"), name, threshold);
    return name;
  }

  /**
   * start an observer in this region that runs forever (use stopObserving() in handler)
   * for details about the observe event handler: {@link ObserverCallBack}
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @return false if not possible, true if events have happened
   */
  public boolean observe() {
    return observe(Float.POSITIVE_INFINITY);
  }

  /**
   * start an observer in this region for the given time
   * for details about the observe event handler: {@link ObserverCallBack}
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @param secs time in seconds the observer should run
   * @return false if not possible, true if events have happened
   */
  public boolean observe(double secs) {
    return observeDo(secs);
  }

  /**
   *INTERNAL USE ONLY: for use with scripting API bridges
   * @param secs time in seconds the observer should run
   * @param bg run in background true/false
   * @return false if not possible, true if events have happened
   */
  public boolean observeJ(double secs, boolean bg) {
    if (bg) {
      return observeInBackground(secs);
    } else {
      return observeDo(secs);
    }
  }

  private boolean observeDo(double secs) {
    if (regionObserver == null) {
      Debug.error("Region: observe: Nothing to observe (Region might be invalid): " + this.toStringShort());
      return false;
    }
    if (observing) {
      Debug.error("Region: observe: already running for this region. Only one allowed!");
      return false;
    }
    log(lvl, "observe: starting in " + this.toStringShort() + " for " + secs + " seconds");
    int MaxTimePerScan = (int) (1000.0 / observeScanRate);
    long begin_t = (new Date()).getTime();
    long stop_t;
    if (secs > Long.MAX_VALUE) {
      stop_t = Long.MAX_VALUE;
    } else {
      stop_t = begin_t + (long) (secs * 1000);
    }
    regionObserver.initialize();
    observing = true;
    Observing.addRunningObserver(this);
    while (observing && stop_t > (new Date()).getTime()) {
      long before_find = (new Date()).getTime();
      ScreenImage simg = getScreen().capture(x, y, w, h);
      if (!regionObserver.update(simg)) {
        observing = false;
        break;
      }
      if (!observing) {
        break;
      }
      long after_find = (new Date()).getTime();
      try {
        if (after_find - before_find < MaxTimePerScan) {
          Thread.sleep((int) (MaxTimePerScan - (after_find - before_find)));
        }
      } catch (Exception e) {
      }
    }
    boolean observeSuccess = false;
    if (observing) {
      observing = false;
      log(lvl, "observe: stopped due to timeout in "
              + this.toStringShort() + " for " + secs + " seconds");
    } else {
      log(lvl, "observe: ended successfully: " + this.toStringShort());
      observeSuccess = Observing.hasEvents(this);
    }
    return observeSuccess;
  }

  /**
   * start an observer in this region for the given time that runs in background
   * for details about the observe event handler: {@link ObserverCallBack}
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   * @param secs time in seconds the observer should run
   * @return false if not possible, true otherwise
   */
  public boolean observeInBackground(double secs) {
    if (observing) {
      Debug.error("Region: observeInBackground: already running for this region. Only one allowed!");
      return false;
    }
    log(lvl, "entering observeInBackground for %f secs", secs);
    Thread observeThread = new Thread(new ObserverThread(secs));
    observeThread.start();
    log(lvl, "observeInBackground now running");
    return true;
  }

  private class ObserverThread implements Runnable {
    private double time;

    ObserverThread(double time) {
      this.time = time;
    }

    @Override
    public void run() {
      observe(time);
    }
  }

  /**
   * stops a running observer
   */
  public void stopObserver() {
    log(lvl, "observe: request to stop observer for " + this.toStringShort());
    observing = false;
  }

   /**
   * stops a running observer printing an info message
   * @param message text
   */
  public void stopObserver(String message) {
    Debug.info(message);
    stopObserver();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Mouse actions - clicking">
  private Location checkMatch() {
    if (lastMatch != null) {
      return lastMatch.getTarget();
    }
    return getTarget();
  }

  /**
   * move the mouse pointer to region's last successful match <br>use center if no lastMatch <br>
   * if region is a match: move to targetOffset <br>same as mouseMove
   *
   * @return 1 if possible, 0 otherwise
   */
  public int hover() {
    try { // needed to cut throw chain for FindFailed
      return hover(checkMatch());
    } catch (FindFailed ex) {
    }
    return 0;
  }

  /**
   * move the mouse pointer to the given target location<br> same as mouseMove<br> Pattern or Filename - do a find
   * before and use the match<br> Region - position at center<br> Match - position at match's targetOffset<br> Location
   * - position at that point<br>
   *
   * @param <PFRML> to search: Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int hover(PFRML target) throws FindFailed {
    log(lvl, "hover: " + target);
    return mouseMove(target);
  }

  /**
   * left click at the region's last successful match <br>use center if no lastMatch <br>if region is a match: click
   * targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int click() {
    try { // needed to cut throw chain for FindFailed
      return click(checkMatch(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * left click at the given target location<br> Pattern or Filename - do a find before and use the match<br> Region -
   * position at center<br> Match - position at match's targetOffset<br>
   * Location - position at that point<br>
   *
   * @param <PFRML> to search: Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int click(PFRML target) throws FindFailed {
    return click(target, 0);
  }

  /**
   * left click at the given target location<br> holding down the given modifier keys<br>
   * Pattern or Filename - do a find before and use the match<br> Region - position at center<br>
   * Match - position at match's targetOffset<br> Location - position at that point<br>
   *
   * @param <PFRML> to search: Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int click(PFRML target, int modifiers) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = Mouse.click(loc, InputEvent.BUTTON1_MASK, modifiers, false, this);

    //TODO      SikuliActionManager.getInstance().clickTarget(this, target, _lastScreenImage, _lastMatch);
    return ret;
  }

  /**
   * double click at the region's last successful match <br>use center if no lastMatch <br>if region is a match: click
   * targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int doubleClick() {
    try { // needed to cut throw chain for FindFailed
      return doubleClick(checkMatch(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * double click at the given target location<br> Pattern or Filename - do a find before and use the match<br> Region -
   * position at center<br> Match - position at match's targetOffset<br>
   * Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int doubleClick(PFRML target) throws FindFailed {
    return doubleClick(target, 0);
  }

  /**
   * double click at the given target location<br> holding down the given modifier keys<br>
   * Pattern or Filename - do a find before and use the match<br> Region - position at center<br > Match - position at
   * match's targetOffset<br> Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int doubleClick(PFRML target, int modifiers) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = Mouse.click(loc, InputEvent.BUTTON1_MASK, modifiers, true, this);

    //TODO      SikuliActionManager.getInstance().doubleClickTarget(this, target, _lastScreenImage, _lastMatch);
    return ret;
  }

  /**
   * right click at the region's last successful match <br>use center if no lastMatch <br>if region is a match: click
   * targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int rightClick() {
    try { // needed to cut throw chain for FindFailed
      return rightClick(checkMatch(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * right click at the given target location<br> Pattern or Filename - do a find before and use the match<br> Region -
   * position at center<br> Match - position at match's targetOffset<br > Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int rightClick(PFRML target) throws FindFailed {
    return rightClick(target, 0);
  }

  /**
   * right click at the given target location<br> holding down the given modifier keys<br>
   * Pattern or Filename - do a find before and use the match<br> Region - position at center<br > Match - position at
   * match's targetOffset<br> Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
    public <PFRML> int rightClick(PFRML target, int modifiers) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = Mouse.click(loc, InputEvent.BUTTON3_MASK, modifiers, false, this);

    //TODO      SikuliActionManager.getInstance().rightClickTarget(this, target, _lastScreenImage, _lastMatch);
    return ret;
  }

  /**
   * time in milliseconds to delay between button down/up at next click only (max 1000)
   *
   * @param millisecs value
   */
  public void delayClick(int millisecs) {
    Settings.ClickDelay = millisecs;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Mouse actions - drag & drop">
  /**
   * Drag from region's last match and drop at given target <br>applying Settings.DelayAfterDrag and DelayBeforeDrop
   * <br> using left mouse button
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int dragDrop(PFRML target) throws FindFailed {
    return dragDrop(lastMatch, target);
  }

  /**
   * Drag from a position and drop to another using left mouse button<br>applying Settings.DelayAfterDrag and
   * DelayBeforeDrop
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param t1 source position
   * @param t2 destination position
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int dragDrop(PFRML t1, PFRML t2) throws FindFailed {
    Location loc1 = getLocationFromTarget(t1);
    Location loc2 = getLocationFromTarget(t2);
    if (loc1 != null && loc2 != null) {
      IRobot r = loc1.getRobotForPoint("drag");
      if (r == null) {
        return 0;
      }
      Mouse.use(this);
      r.smoothMove(loc1);
      r.mouseDown(InputEvent.BUTTON1_MASK);
      r.delay((int) (Settings.DelayAfterDrag * 1000));
      r = loc2.getRobotForPoint("drop");
      if (r == null) {
        Mouse.let(this);
        return 0;
      }
      r.smoothMove(loc2);
      r.delay((int) (Settings.DelayBeforeDrop * 1000));
      r.mouseUp(InputEvent.BUTTON1_MASK);
      Mouse.let(this);
      return 1;
    }
    return 0;
  }

  /**
   * Prepare a drag action: move mouse to given target <br>press and hold left mouse button <br >wait
   * Settings.DelayAfterDrag
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int drag(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    if (loc != null) {
      IRobot r = loc.getRobotForPoint("drag");
      if (r == null) {
        return 0;
      }
      Mouse.use(this);
      r.smoothMove(loc);
      r.mouseDown(InputEvent.BUTTON1_MASK);
      r.delay((int) (Settings.DelayAfterDrag * 1000));
      r.waitForIdle();
      return 1;
    }
    return 0;
  }

  /**
   * finalize a drag action with a drop: move mouse to given target <br>wait Settings.DelayBeforeDrop <br>release the
   * left mouse button
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed  if not found
   */
  public <PFRML> int dropAt(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    if (loc != null) {
      IRobot r = loc.getRobotForPoint("drop");
      if (r == null) {
        return 0;
      }
      r.smoothMove(loc);
      r.delay((int) (Settings.DelayBeforeDrop * 1000));
      r.mouseUp(InputEvent.BUTTON1_MASK);
      r.waitForIdle();
      Mouse.let(this);
      return 1;
    }
    return 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Mouse actions - low level + Wheel">
  /**
   * press and hold the specified buttons - use + to combine Button.LEFT left mouse button Button.MIDDLE middle mouse
   * button Button.RIGHT right mouse button
   *
   * @param buttons spec
   */
  public void mouseDown(int buttons) {
    Mouse.down(buttons, this);
  }

  /**
   * release all currently held buttons
   */
  public void mouseUp() {
    Mouse.up(0, this);
  }

  /**
   * release the specified mouse buttons (see mouseDown) if buttons==0, all currently held buttons are released
   *
   * @param buttons spec
   */
  public void mouseUp(int buttons) {
    Mouse.up(buttons, this);
  }

  /**
   * move the mouse pointer to the region's last successful match<br>same as hover<br>
   *
   * @return 1 if possible, 0 otherwise
   */
  public int mouseMove() {
    if (lastMatch != null) {
      try {
        return mouseMove(lastMatch);
      } catch (FindFailed ex) {
        return 0;
      }
    }
    return 0;
  }

  /**
   * move the mouse pointer to the given target location<br> same as hover<br> Pattern or Filename - do a find before
   * and use the match<br> Region - position at center<br> Match - position at match's targetOffset<br>
   * Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int mouseMove(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    return Mouse.move(loc, this);
  }

  /**
   * Move the wheel at the current mouse position<br> the given steps in the given direction: <br >Button.WHEEL_DOWN,
   * Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps the number of steps
   * @return 1 in any case
   */
  public int wheel(int direction, int steps) {
    Mouse.wheel(direction, steps, this);
    return 1;
  }

  /**
   * move the mouse pointer to the given target location<br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location target
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps the number of steps
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    if (loc != null) {
      Mouse.use(this);
      Mouse.keep(this);
      Mouse.move(loc, this);
      Mouse.wheel(direction, steps, this);
      Mouse.let(this);
      return 1;
    }
    return 0;
  }

	/**
	 *
	 * @return current location of mouse pointer
	 * @deprecated use {@link Mouse#at()} instead
	 */
	@Deprecated
	public static Location atMouse() {
    return Mouse.at();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Keyboard actions + paste">
  /**
   * press and hold the given key use a constant from java.awt.event.KeyEvent which might be special in the current
   * machine/system environment
   *
   * @param keycode Java KeyCode
   */
  public void keyDown(int keycode) {
    getRobotForRegion().keyDown(keycode);
  }

  /**
   * press and hold the given keys including modifier keys <br>use the key constants defined in class Key, <br>which
   * only provides a subset of a US-QWERTY PC keyboard layout <br>might be mixed with simple characters
   * <br>use + to concatenate Key constants
   *
   * @param keys valid keys
   */
  public void keyDown(String keys) {
    getRobotForRegion().keyDown(keys);
  }

  /**
   * release all currently pressed keys
   */
  public void keyUp() {
    getRobotForRegion().keyUp();
  }

  /**
   * release the given keys (see keyDown(keycode) )
   *
   * @param keycode Java KeyCode
   */
  public void keyUp(int keycode) {
    getRobotForRegion().keyUp(keycode);
  }

  /**
   * release the given keys (see keyDown(keys) )
   *
   * @param keys valid keys
   */
  public void keyUp(String keys) {
    getRobotForRegion().keyUp(keys);
  }

  /**
   * Compact alternative for type() with more options <br>
   * - special keys and options are coded as #XN. or #X+ or #X- <br>
   * where X is a refrence for a special key and N is an optional repeat factor <br>
   * A modifier key as #X. modifies the next following key<br>
   * the trailing . ends the special key, the + (press and hold) or - (release) does the same, <br>
   * but signals press-and-hold or release additionally.<br>
   * except #W / #w all special keys are not case-sensitive<br>
   * a #wn. inserts a wait of n millisecs or n secs if n less than 60 <br>
   * a #Wn. sets the type delay for the following keys (must be &gt; 60 and denotes millisecs)
   * - otherwise taken as normal wait<br>
   * Example: wait 2 secs then type CMD/CTRL - N then wait 1 sec then type DOWN 3 times<br>
   * Windows/Linux: write("#w2.#C.n#W1.#d3.")<br>
   * Mac: write("#w2.#M.n#W1.#D3.")<br>
   * for more details about the special key codes and examples consult the docs <br>
   * @param text a coded text interpreted as a series of key actions (press/hold/release)
   * @return 0 for success 1 otherwise
   */
  public int write(String text) {
    Debug.info("Write: " + text);
    char c;
    String token, tokenSave;
    String modifier = "";
    int k;
    IRobot robot = getRobotForRegion();
    int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
    Settings.TypeDelay = 0.0;
    robot.typeStarts();
    for (int i = 0; i < text.length(); i++) {
      log(lvl, "write: (%d) %s", i, text.substring(i));
      c = text.charAt(i);
      token = null;
      boolean isModifier = false;
      if (c == '#') {
        if (text.charAt(i + 1) == '#') {
          log(3, "write at: %d: %s", i, c);
          i += 1;
          continue;
        }
        if (text.charAt(i + 2) == '+' || text.charAt(i + 2) == '-') {
          token = text.substring(i, i + 3);
          isModifier = true;
        } else if (-1 < (k = text.indexOf('.', i))) {
          if (k > -1) {
            token = text.substring(i, k + 1);
            if (token.length() > Key.keyMaxLength || token.substring(1).contains("#")) {
              token = null;
            }
          }
        }
      }
      Integer key = -1;
      if (token == null) {
        log(3, "%d: %s", i, c);
      } else {
        log(lvl, "write: token at %d: %s", i, token);
        int repeat = 0;
        if (token.toUpperCase().startsWith("#W")) {
          if (token.length() > 3) {
            i += token.length() - 1;
            int t = 0;
            try {
              t = Integer.parseInt(token.substring(2, token.length() - 1));
            } catch (NumberFormatException ex) {
            }
            if ((token.startsWith("#W") && t > 60) || pause > 20) {
              pause = 20 + (t > 1000 ? 1000 : t);
              log(lvl, "write: type delay: " + t);
            } else {
              log(lvl, "write: wait: " + t);
              robot.delay((t < 60 ? t * 1000 : t));
            }
            continue;
          }
        }
        tokenSave = token;
        token = token.substring(0, 2).toUpperCase() + ".";
        if (Key.isRepeatable(token)) {
          try {
            repeat = Integer.parseInt(tokenSave.substring(2, tokenSave.length() - 1));
          } catch (NumberFormatException ex) {
            token = tokenSave;
          }
        } else if (tokenSave.length() == 3 && Key.isModifier(tokenSave.toUpperCase())) {
          i += tokenSave.length() - 1;
          modifier += tokenSave.substring(1, 2).toUpperCase();
          continue;
        } else {
          token = tokenSave;
        }
        if (-1 < (key = Key.toJavaKeyCodeFromText(token))) {
          if (repeat > 0) {
            log(3, "write: %s Repeating: %d", token, repeat);
          } else {
            log(3, "write: %s", tokenSave);
            repeat = 1;
          }
          i += tokenSave.length() - 1;
          if (isModifier) {
            if (tokenSave.endsWith("+")) {
              robot.keyDown(key);
            } else {
              robot.keyUp(key);
            }
            continue;
          }
          if (repeat > 1) {
            for (int n = 0; n < repeat; n++) {
              robot.typeKey(key.intValue());
            }
            continue;
          }
        }
      }
      if (!modifier.isEmpty()) {
        log(3, "write: modifier + " + modifier);
        for (int n = 0; n < modifier.length(); n++) {
          robot.keyDown(Key.toJavaKeyCodeFromText(String.format("#%s.", modifier.substring(n, n + 1))));
        }
      }
      if (key > -1) {
        robot.typeKey(key.intValue());
      } else {
        robot.typeChar(c, IRobot.KeyMode.PRESS_RELEASE);
      }
      if (!modifier.isEmpty()) {
        log(3, "write: modifier - " + modifier);
        for (int n = 0; n < modifier.length(); n++) {
          robot.keyUp(Key.toJavaKeyCodeFromText(String.format("#%s.", modifier.substring(n, n + 1))));
        }
      }
      robot.delay(pause);
      modifier = "";
    }

    robot.typeEnds();
    robot.waitForIdle();
    return 0;
  }

  /**
   * enters the given text one character/key after another using keyDown/keyUp
   * <br>about the usable Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC
   * keyboard layout<br>the text is entered at the current position of the focus/carret
   *
   * @param text containing characters and/or Key constants
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text) {
    try {
      return keyin(null, text, 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * enters the given text one character/key after another using keyDown/keyUp<br>while holding down the given modifier
   * keys <br>about the usable Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC
   * keyboard layout<br>the text is entered at the current position of the focus/carret
   *
   * @param text containing characters and/or Key constants
   * @param modifiers constants according to class KeyModifiers
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text, int modifiers) {
    try {
      return keyin(null, text, modifiers);
    } catch (FindFailed findFailed) {
      return 0;
    }
  }

  /**
   * enters the given text one character/key after another using
   *
   * keyDown/keyUp<br>while holding down the given modifier keys <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout<br>the text is entered at the current
   * position of the focus/carret
   *
   *
   * @param text containing characters and/or Key constants
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text, String modifiers) {
    String target = null;
    int modifiersNew = Key.convertModifiers(modifiers);
    if (modifiersNew == 0) {
      target = text;
      text = modifiers;
    }
    try {
      return keyin(target, text, modifiersNew);
    } catch (FindFailed findFailed) {
      return 0;
    }
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br>enters the given text one
   * character/key after another using keyDown/keyUp <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param text containing characters and/or Key constants
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text) throws FindFailed {
    return keyin(target, text, 0);
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br>enters the given text one
   * character/key after another using keyDown/keyUp <br>while holding down the given modifier keys<br>about the usable
   * Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param text containing characters and/or Key constants
   * @param modifiers constants according to class KeyModifiers
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text, int modifiers) throws FindFailed {
    return keyin(target, text, modifiers);
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br>enters the given text one
   * character/key after another using keyDown/keyUp <br>while holding down the given modifier keys<br>about the usable
   * Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param text containing characters and/or Key constants
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text, String modifiers) throws FindFailed {
    int modifiersNew = Key.convertModifiers(modifiers);
    return keyin(target, text, modifiersNew);
  }

  private <PFRML> int keyin(PFRML target, String text, int modifiers)
          throws FindFailed {
    if (target != null && 0 == click(target, 0)) {
      return 0;
    }
    if (text != null && !"".equals(text)) {
      String showText = "";
      for (int i = 0; i < text.length(); i++) {
        showText += Key.toJavaKeyCodeText(text.charAt(i));
      }
      String modText = "";
      String modWindows = null;
      if ((modifiers & KeyModifier.WIN) != 0) {
        modifiers -= KeyModifier.WIN;
        modifiers |= KeyModifier.META;
        log(lvl, "Key.WIN as modifier");
        modWindows = "Windows";
      }
      if (modifiers != 0) {
        modText = String.format("( %s ) ", KeyEvent.getKeyModifiersText(modifiers));
        if (modWindows != null) {
          modText = modText.replace("Meta", modWindows);
        }
      }
      Debug.action("%s TYPE \"%s\"", modText, showText);
      log(lvl, "%s TYPE \"%s\"", modText, showText);
      IRobot r = getRobotForRegion();
      int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
      Settings.TypeDelay = 0.0;
      r.typeStarts();
      for (int i = 0; i < text.length(); i++) {
        r.pressModifiers(modifiers);
        r.typeChar(text.charAt(i), IRobot.KeyMode.PRESS_RELEASE);
        r.releaseModifiers(modifiers);
        r.delay(pause);
      }
      r.typeEnds();
      r.waitForIdle();
      return 1;
    }

    return 0;
  }

  /**
   * time in milliseconds to delay between each character at next type only (max 1000)
   *
   * @param millisecs value
   */
  public void delayType(int millisecs) {
    Settings.TypeDelay = millisecs;
  }

  /**
   * pastes the text at the current position of the focus/carret <br>using the clipboard and strg/ctrl/cmd-v (paste
   * keyboard shortcut)
   *
   * @param text a string, which might contain unicode characters
   * @return 0 if possible, 1 otherwise
   */
  public int paste(String text) {
    try {
      return paste(null, text);
    } catch (FindFailed ex) {
      return 1;
    }
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br> and then pastes the text <br>
   * using the clipboard and strg/ctrl/cmd-v (paste keyboard shortcut)
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location target
   * @param target Pattern, Filename, Text, Region, Match or Location
   * @param text a string, which might contain unicode characters
   * @return 0 if possible, 1 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int paste(PFRML target, String text) throws FindFailed {
    click(target, 0);
    if (text != null) {
      App.setClipboard(text);
      int mod = Key.getHotkeyModifier();
      IRobot r = getRobotForRegion();
      r.keyDown(mod);
      r.keyDown(KeyEvent.VK_V);
      r.keyUp(KeyEvent.VK_V);
      r.keyUp(mod);
      return 1;
    }
    return 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="OCR - read text from Screen">
  /**
   * STILL EXPERIMENTAL: tries to read the text in this region<br> might contain misread characters, NL characters and
   * other stuff, when interpreting contained grafics as text<br>
   * Best results: one line of text with no grafics in the line
   *
   * @return the text read (utf8 encoded)
   */
  public String text() {
    if (Settings.OcrTextRead) {
      ScreenImage simg = getScreen().capture(x, y, w, h);
      TextRecognizer tr = TextRecognizer.getInstance();
      if (tr == null) {
        Debug.error("text: text recognition is now switched off");
        return "--- no text ---";
      }
      String textRead = tr.recognize(simg);
      log(lvl, "text: #(" + textRead + ")#");
      return textRead;
    }
    Debug.error("text: text recognition is currently switched off");
    return "--- no text ---";
  }

  /**
   * VERY EXPERIMENTAL: returns a list of matches, that represent single words, that have been found in this region<br>
   * the match's x,y,w,h the region of the word<br> Match.getText() returns the word (utf8) at this match<br>
   * Match.getScore() returns a value between 0 ... 1, that represents some OCR-confidence value<br > (the higher, the
   * better the OCR engine thinks the result is)
   *
   * @return a list of matches
   */
  public List<Match> listText() {
    if (Settings.OcrTextRead) {
      ScreenImage simg = getScreen().capture(x, y, w, h);
      TextRecognizer tr = TextRecognizer.getInstance();
      if (tr == null) {
        Debug.error("text: text recognition is now switched off");
        return null;
      }
      log(lvl, "listText: scanning %s", this);
      return tr.listText(simg, this);
    }
    Debug.error("text: text recognition is currently switched off");
    return null;
  }
  //</editor-fold>
}
