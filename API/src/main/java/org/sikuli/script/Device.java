/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;

/**
 *
 * @author RaiMan
 */
public class Device {
  
  protected static String me = "Device";
  protected static final int lvl = 3;

  protected static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }

  private static volatile Object device = null;
  
  protected boolean inUse = false;
  protected boolean keep = false;
  protected Object owner = null;
  private static boolean blocked = false;
  private static boolean suspended = false;
  protected Location lastPos = null;
  
  protected Device(Mouse m) {
    device = m;
  }
  
  public boolean isInUse() {
    return inUse;
  }

  public static boolean isSuspended() {
    return suspended;
  }

  public static boolean isBlocked() {
    return blocked;
  }

  /**
   * to block the device globally <br>
   * only the contained device methods without owner will be granted
   *
   * @return success
   */
  public static boolean block() {
    return block(null);
  }

  /**
   * to block the device globally for the given owner <br>
   * only the contained mouse methods having the same owner will be granted
   *
   * @param owner Object
   * @return success
   */
  public static boolean block(Object owner) {
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
  public static boolean unblock() {
    return unblock(null);
  }

  /**
   * free the mouse globally for this owner after a block(owner)
   *
   * @param owner Object
   * @return success (false means: not blocked currently for this owner)
   */
  public static boolean unblock(Object owner) {
    if (owner == null) {
      owner = get();
    } else if (owner instanceof Region) {
      if (((Region) owner).isOtherScreen()) {
        return false;
      }
    }
    if (blocked && get().owner == owner) {
      blocked = false;
      get().let(owner);
      return true;
    }
    return false;
  }

  protected static boolean use() {
    return get().use(null);
  }

  protected synchronized boolean use(Object owner) {
    if (owner == null) {
      owner = this;
    } else if (owner instanceof Region) {
      if (((Region) owner).isOtherScreen()) {
        return false;
      }
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
      checkLastPos();
      keep = false;
      this.owner = owner;
      log(lvl+1, "use start: %s", owner);
      return true;
    }
    log(-1, "synch problem - use start: %s", owner);
    return false;
  }

  protected void checkLastPos(){}
  
  protected synchronized boolean keep(Object ownerGiven) {
    if (ownerGiven == null) {
      ownerGiven = this;
    } else if (ownerGiven instanceof Region) {
      if (((Region) ownerGiven).isOtherScreen()) {
        return false;
      }
    }
    if (inUse && owner == ownerGiven) {
      keep = true;
      log(lvl+1, "use keep: %s", ownerGiven);
      return true;
    }
    return false;
  }

  protected static boolean let() {
    return get().let(null);
  }

  protected synchronized boolean let(Object owner) {
    if (owner == null) {
      owner = this;
    } else if (owner instanceof Region) {
      if (((Region) owner).isOtherScreen()) {
        return false;
      }
    }
    if (inUse && this.owner == owner) {
      if (keep) {
        keep = false;
        return true;
      }
      lastPos = getLocation();
      inUse = false;
      this.owner = null;
      notify();
      log(lvl+1, "use stop: %s", owner);
      return true;
    }
    return false;
  }
  
  protected Location getLocation(){
    return lastPos;
  }
}
