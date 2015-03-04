/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.util.Date;
import org.sikuli.basics.Debug;

/**
 *
 * @author RaiMan
 */
public class Device {

  static RunTime runTime = RunTime.get();

  private static String me = "Device: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private Object device = null;
  private String devName = "Device";

  protected boolean inUse = false;
  protected boolean keep = false;
  protected Object owner = null;
  private boolean blocked = false;
  private boolean suspended = false;
  protected Location lastPos = null;
  protected boolean isMouse = false;

  protected int MouseMovedIgnore = 0;
  protected int MouseMovedShow = 1;
  protected int MouseMovedPause = 2;
  protected int MouseMovedAction = 3;
  protected int mouseMovedResponse = MouseMovedIgnore;
  protected ObserverCallBack mouseMovedCallback = null;

  protected Device(Mouse m) {
    device = m;
    devName = "Mouse";
  }

  protected Device(Keys k) {
    device = k;
    devName = "KeyBoard";
  }

  protected Device(Screen s) {
    device = s;
    devName = "Screen";
  }

  public boolean isInUse() {
    return inUse;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public boolean isBlocked() {
    return blocked;
  }

	public boolean isNotLocal(Object owner) {
		if (owner instanceof Region) {
      if (((Region) owner).isOtherScreen()) {
        return true;
      }
    } else if (owner instanceof Location) {
      if (((Location) owner).isOtherScreen()) {
        return true;
      }
    }
		return false;
	}

  /**
   * to block the device globally <br>
   * only the contained device methods without owner will be granted
   *
   * @return success
   */
  public boolean block() {
    return block(null);
  }

  /**
   * to block the device globally for the given owner <br>
   * only the contained mouse methods having the same owner will be granted
   *
   * @param owner Object
   * @return success
   */
  public boolean block(Object owner) {
    if (use(owner)) {
      blocked = true;
      return true;
    } else {
      return false;
    }
  }

  /**
   * free the mouse globally after a block()
   *
   * @return success (false means: not blocked currently)
   */
  public boolean unblock() {
    return unblock(null);
  }

  /**
   * free the mouse globally for this owner after a block(owner)
   *
   * @param ownerGiven Object
   * @return success (false means: not blocked currently for this owner)
   */
	public boolean unblock(Object ownerGiven) {
		if (ownerGiven == null) {
			ownerGiven = device;
		} else if (isNotLocal(ownerGiven)) {
			return false;
		}
		if (blocked && owner == ownerGiven) {
			blocked = false;
			let(ownerGiven);
			return true;
		}
		return false;
	}

  protected boolean use() {
    return use(null);
  }

  protected synchronized boolean use(Object owner) {
    if (owner == null) {
      owner = this;
		} else if (isNotLocal(owner)) {
			return false;
		}
    if ((blocked || inUse) && this.owner == owner) {
      return true;
    }
    while (inUse) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    if (!inUse) {
      inUse = true;
      if (isMouse) {
        checkLastPos();
      }
      keep = false;
      this.owner = owner;
      log(lvl + 1, "%s: use start: %s", devName, owner);
      return true;
    }
    log(-1, "synch problem - use start: %s", owner);
    return false;
  }

  protected synchronized boolean keep(Object ownerGiven) {
    if (ownerGiven == null) {
      ownerGiven = this;
		} else if (isNotLocal(ownerGiven)) {
			return false;
		}
    if (inUse && owner == ownerGiven) {
      keep = true;
      log(lvl + 1, "%s: use keep: %s", devName, ownerGiven);
      return true;
    }
    return false;
  }

  protected boolean let() {
    return let(null);
  }

  protected synchronized boolean let(Object owner) {
    if (owner == null) {
      owner = this;
		} else if (isNotLocal(owner)) {
			return false;
		}
    if (inUse && this.owner == owner) {
      if (keep) {
        keep = false;
        return true;
      }
      if (isMouse) {
        lastPos = getLocation();
      }
      inUse = false;
      this.owner = null;
      notify();
      log(lvl + 1, "%s: use stop: %s", devName, owner);
      return true;
    }
    return false;
  }

  protected Location getLocation() {
    PointerInfo mp = MouseInfo.getPointerInfo();
    if (mp != null) {
      return new Location(MouseInfo.getPointerInfo().getLocation());
    } else {
      Debug.error("Mouse: not possible to get mouse position (PointerInfo == null)");
      return null;
    }
  }

  private void checkLastPos() {
    if (lastPos == null) {
      return;
    }
    Location pos = getLocation();
    if (pos != null && (lastPos.x != pos.x || lastPos.y != pos.y)) {
      log(lvl, "%s: moved externally: now (%d,%d) was (%d,%d) (mouseMovedResponse %d)",
              devName, pos.x, pos.y, lastPos.x, lastPos.y, mouseMovedResponse);
      if (mouseMovedResponse > 0) {
        showMousePos(pos.getPoint());
      }
      if (mouseMovedResponse == MouseMovedPause) {
				while (pos.x > 0 && pos.y > 0) {
					delay(500);
					pos = getLocation();
					showMousePos(pos.getPoint());
				}
				if (pos.x < 1) {
					return;
				}
				log(-1, "Terminating in MouseMovedResponse = Pause");
				Sikulix.endError(1);
      }
      if (mouseMovedResponse == MouseMovedAction) {
//TODO implement 3
        if (mouseMovedCallback != null) {
          mouseMovedCallback.happened(new ObserveEvent("MouseMoved", ObserveEvent.Type.GENERIC,
                  lastPos, new Location(pos), null, (new Date()).getTime()));
        }
      }
    }
  }

  private static void showMousePos(Point pos) {
    Location lPos = new Location(pos);
    Region inner = lPos.grow(20).highlight();
    delay(500);
    lPos.grow(40).highlight(1);
    delay(500);
    inner.highlight();
  }

  protected static void delay(int time) {
    if (time == 0) {
      return;
    }
    if (time < 60) {
      time = time * 1000;
    }
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
    }
  }
}
