/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * A point like AWT.Point using global coordinates, hence modifications might move location out of
 * any screen (not checked as is done with region)
 *
 */
public class Location {
  
  public int x;
  public int y;
  private Screen otherScreen = null;

  /**
   * to allow calculated x and y that might not be integers
   * @param x
   * @param y
   * truncated to the integer part
   */
  public Location(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  /**
   * a new point at the given coordinates
   * @param x
   * @param y
   */
  public Location(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * duplicates the point
   * @param loc
   */
  public Location(Location loc) {
    x = loc.x;
    y = loc.y;
    if (loc.isOtherScreen()) {
      otherScreen = loc.getScreen();
    }
  }

  /**
   * create from AWT point
   * @param point
   */
  public Location(Point point) {
    x = (int) point.x;
    y = (int) point.y;
  }
  
  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
  
  /**
   * get as AWT point
   * @return point
   */
  public Point getPoint() {
    return new Point(x,y);
  }
  
  /**
   * sets the coordinates to the given values (moves it)
   * @param x
   * @param y
   * @return self/this
   */
  public Location setLocation(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * sets the coordinates to the given values (moves it)
   * @param x
   * @param y
   * @return self/this
   */
  public Location setLocation(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
    return this;
  }

  /**
    * Returns null, if outside of any screen<br>
    * subsequent actions might crash
    *
    * @return the screen, that contains the given point.<br>
    */
  public Screen getScreen() {
    Rectangle r;
    if (otherScreen != null) {
      return otherScreen;
    }
    for (int i = 0; i < Screen.getNumberScreens(); i++) {
      r = Screen.getScreen(i).getBounds();
      if (r.contains(this.x, this.y)) {
        return Screen.getScreen(i);
      }
    }
    Debug.error("Location: outside any screen (%s, %s) - subsequent actions might not work as expected", x, y);
    return null;
  }
  
  public Location setOtherScreen(Screen scr) {
    otherScreen = scr;
    return this;
  }
  
  public boolean isOtherScreen() {
    return (otherScreen != null);
  }
  
  private Location setOtherScreen(Location loc) {
    if (loc.isOtherScreen()) {
      setOtherScreen(loc.getScreen());
    }
    return this;
  }

// TODO Location.getColor() implement more support and make it useable
  /**
   * Get the color at the given Point for details: see java.awt.Robot and ...Color
   *
   * @return The Color of the Point
   */
  public Color getColor() {
    if (getScreen() == null) {
      return null;
    }
    return getScreen().getRobot().getColorAt(x, y);
  }

  /**
   * the offset of given point to this Location 
   *
   * @param loc
   * @return relative offset
   */
  public Location getOffset(Location loc) {
    return new Location(loc.x - x, loc.y - y);
  }

  /**
   * create a region with this point as center and the given size
   *
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public Region grow(int w, int h) {
    return Region.grow(this, w, h);
  }

  /**
   * create a region with this point as center and the given size
   *
   * @param wh the width and height
   * @return the new region
   */
  public Region grow(int wh) {
    return grow(wh, wh);
  }

  /**
   * create a region with a corner at this point<br>as specified with x y<br> 0 0 top left<br>
   * 0 1 bottom left<br> 1 0 top right<br> 1 1 bottom right<br>
   *
   * @param CREATE_X_DIRECTION == 0 is left side !=0 is right side, see {@link Region#CREATE_X_DIRECTION_LEFT}, {@link Region#CREATE_X_DIRECTION_RIGHT}
   * @param CREATE_Y_DIRECTION == 0 is top side !=0 is bottom side, see {@link Region#CREATE_Y_DIRECTION_TOP}, {@link Region#CREATE_Y_DIRECTION_BOTTOM}
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public Region grow(int CREATE_X_DIRECTION, int CREATE_Y_DIRECTION, int w, int h) {
    return Region.create(this, CREATE_X_DIRECTION, CREATE_Y_DIRECTION, w, h);
  }

  /**
   * moves the point the given amounts in the x and y direction, might be negative <br>might move
   * point outside of any screen, not checked
   *
   * @param dx
   * @param dy
   * @return the location itself modified
   */
  public Location moveFor(int dx, int dy) {
    x += dx;
    y += dy;
    return this;
  }
  
  /**
   * convenience: like awt point
   * @param dx
   * @param dy
   * @return the location itself modified
   */
  public Location translate(int dx, int dy) {
    return moveFor(dx, dy);
  }

  /**
   * changes the locations x and y value to the given values (moves it) <br>might move point
   * outside of any screen, not checked
   *
   * @param X
   * @param Y
   * @return the location itself modified
   */
  public Location moveTo(int X, int Y) {
    x = X;
    y = Y;
    return this;
  }

  /**
   * convenience: like awt point
   * @param X
   * @param Y
   * @return the location itself modified
   */
  public Location move(int X, int Y) {
    return moveTo(X, Y);
  }

  /**
   * creates a point at the given offset, might be negative <br>might create a point outside of
   * any screen, not checked
   *
   * @param dx
   * @param dy
   * @return new location
   */
  public Location offset(int dx, int dy) {
    return new Location(x + dx, y + dy);
  }

  /**
   * creates a point at the given offset, might be negative <br>might create a point outside of
   * any screen, not checked
   *
   * @param loc
   * @return new location
   */
  public Location offset(Location loc) {
    return new Location(x + loc.x, y + loc.y);
  }

/**
   * creates a point at the given offset to the left, might be negative <br>might create a point
   * outside of any screen, not checked
   *
   * @param dx
   * @return new location
   */
  public Location left(int dx) {
    return new Location(x - dx, y).setOtherScreen(this);
  }

  /**
   * creates a point at the given offset to the right, might be negative <br>might create a point
   * outside of any screen, not checked
   *
   * @param dx
   * @return new location
   */
  public Location right(int dx) {
    return new Location(x + dx, y).setOtherScreen(this);
  }

  /**
   * creates a point at the given offset above, might be negative <br>might create a point outside
   * of any screen, not checked
   *
   * @param dy
   * @return new location
   */
  public Location above(int dy) {
    return new Location(x, y - dy).setOtherScreen(this);
  }

  /**
   * creates a point at the given offset below, might be negative <br>might create a point outside
   * of any screen, not checked
   *
   * @param dy
   * @return new location
   */
  public Location below(int dy) {
    return new Location(x, y + dy).setOtherScreen(this);
  }

  /**
   * new point with same offset to current screen's top left on given screen
   *
   * @param scrID number of screen
   * @return new location
   */
  public Location copyTo(int scrID) {
    return copyTo(Screen.getScreen(scrID));
  }

  /**
   * New point with same offset to current screen's top left on given screen
   *
   * @param screen new parent screen
   * @return new location
   */
  public Location copyTo(Screen screen) {
    Screen s = getScreen();
    s = (s == null ? Screen.getPrimaryScreen() : s);
    Location o = new Location(s.getBounds().getLocation());
    Location n = new Location(screen.getBounds().getLocation());
    return new Location(n.x + x - o.x, n.y + y - o.y);
  }
  
  /**
   * Move the mouse to this location point
   * 
   * @return this
   */
  public Location hover() {
    Mouse.move(this);
    return this;
  }

  /**
   * Move the mouse to this location point and click left
   * 
   * @return this
   */
  public Location click() {
    Mouse.click(this, "L");
    return this;
  }

  /**
   * Move the mouse to this location point and double click left
   * 
   * @return this
   */
  public Location doubleClick() {
    Mouse.click(this, "LD");
    return this;
  }

  /**
   * Move the mouse to this location point and click right
   * 
   * @return this
   */
  public Location rightClick() {
    Mouse.click(this, "R");
    return this;
  }

  /**
   * {@inheritDoc}
   * @return the description
   */
  @Override
  public String toString() {
    Screen s = getScreen();    
    return "L(" + x + "," + y + ")" + 
            ((s == null) ? "" : "@" + s.toStringShort());
  }
  
  /**
   *
   * @return a shorter description
   */
  public String toStringShort() {
    return "L(" + x + "," + y + ")";
  }

  // to avoid NPE for points outside any screen
  protected IRobot getRobotForPoint(String action) {
    if (getScreen() == null) {
      Debug.error("Point %s outside any screen not useable for %s", this, action);
      return null;
    }
    if (!getScreen().isOtherScreen()) {
      getScreen().showTarget(this);
    }
    return getScreen().getRobot();
  }
}
