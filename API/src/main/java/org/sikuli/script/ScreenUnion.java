/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import java.awt.Rectangle;

/**
 * CANDIDATE FOR DEPRECATION
 * INTERNAL USE
 * An extension of DesktopScreen, that uses all active monitors as one big screen
 *
 * TO BE EVALUATED: is this really needed?
 */
public class ScreenUnion extends Screen {

  private Rectangle _bounds;

  public ScreenUnion() {
    super(true);
    Rectangle r = getBounds();
    x = r.x;
    y = r.y;
    w = r.width;
    h = r.height;
  }

  public int getIdFromPoint(int x, int y) {
    Rectangle sr = getBounds();
    int _x = x + getBounds().x;
    int _y = y + getBounds().y;
    for (int i = 0; i < getNumberScreens(); i++) {
      if (Screen.getScreen(i).contains(new Location(_x, _y))) {
        Debug.log(3, "ScreenUnion: getIdFromPoint: " +
                     "(%d, %d) as (%d, %d) in (%d, %d, %d, %d) on %d",
                       x, y, _x, _y, sr.x, sr.y, sr.width, sr.height, i);
        return i;
      }
    }
    Debug.log(3, "ScreenUnion: getIdFromPoint: " +
                 "(%d, %d) as (%d, %d) in (%d, %d, %d, %d) on ???",
                   x, y, _x, _y, sr.x, sr.y, sr.width, sr.height);
    return 0;
  }

  @Override
  public Rectangle getBounds() {
    if (_bounds == null) {
      _bounds = new Rectangle();
      for (int i = 0; i < Screen.getNumberScreens(); i++) {
        _bounds = _bounds.union(Screen.getBounds(i));
      }
    }
    return _bounds;
  }

  @Override
  public ScreenImage capture(Rectangle rect) {
    Debug.log(3, "ScreenUnion: capture: " + rect);
    return Region.create(rect).getScreen().capture(rect);
  }

  @Override
  public boolean useFullscreen() {
    return false;
  }
}
