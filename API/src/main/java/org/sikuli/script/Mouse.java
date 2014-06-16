/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

/**
 * Main pupose is to coordinate the mouse usage among threads <br>
 * At any one time, the mouse has one owner (usually a Region object) <br>
 * who exclusively uses the mouse, all others wait for the mouse to be free again <br>
 * if more than one possible owner is waiting, the next owner is uncertain <br>
 * It is detected, when the mouse is moved external from the workflow, which can be
 * used for appropriate actions (e.g. pause a script) <br>
 * the mouse can be blocked for a longer time, so only this owner can use
 * the mouse (like some transactional processing) <br>
 * Currently deadlocks and infinite waits are not detected, but should not happen ;-) <br>
 * Contained are methods to use the mouse (click, move, button down/up) as is
 */
public class Mouse {

  private static Mouse mouse = null;

  private boolean inUse = false;
  private boolean keep = false;
  private Object owner = null;
  private Point lastPos = null;
  private static boolean blocked = false;
  private static boolean suspended = false;

  public static final int MouseMovedIgnore = 0;
  public static final int MouseMovedShow = 1;
  public static final int MouseMovedPause = 2;
  public static final int MouseMovedAction = 3;
  private static int mouseMovedResponse = MouseMovedIgnore;
  private static ObserverCallBack callBack;

  protected Location mousePos;
  protected boolean clickDouble;
  protected int buttons;
  protected int beforeWait;
  protected int innerWait;
  protected int afterWait;

  public static final int LEFT = InputEvent.BUTTON1_MASK;
  public static final int MIDDLE = InputEvent.BUTTON2_MASK;
  public static final int RIGHT = InputEvent.BUTTON3_MASK;
  public static final int WHEEL_UP = -1;
  public static int WHEEL_DOWN = 1;

  private Mouse() {
    this.lastPos = null;
  }

  /**
   * To get the one Mouse instance used for synchronisation
   *
   */
  protected static Mouse get() {
    if (mouse == null) {
      mouse = new Mouse();
    }
    return mouse;
  }
  
  public static void reset() {
    if (mouse == null) {
      return;
    }
    unblock(get().getOwner());
    get().let(get().getOwner());
    get().let(get().getOwner());
    up();
    setMouseMovedResponse(MouseMovedIgnore);
    mouse = null;
  }
  
  private Object getOwner() {
    return owner;
  }

  /**
   * current setting what to do if mouse is moved outside Sikuli's mouse protection
   *
   */
  public static int getMouseMovedResponse() {
    return mouseMovedResponse;
  }

  /**
   * what to do if mouse is moved outside Sikuli's mouse protection <br>
   * - Mouse.MouseMovedIgnore (0) ignore it (default) <br>
   * - Mouse.MouseMovedShow (1) show and ignore it <br>
   * - Mouse.MouseMovedPause (2) show it and pause until user says continue <br>
   * (2 not implemented yet - 1 is used)
   * @param mouseMovedResponse
   */
  public static void setMouseMovedResponse(int mouseMovedResponse) {
    if (mouseMovedResponse > -1 && mouseMovedResponse < 3) {
      Mouse.mouseMovedResponse = mouseMovedResponse;
    }
  }
  
  /**
   * what to do if mouse is moved outside Sikuli's mouse protection <br>
   * only 3 is honored:<br>
   * in case of event the user provided callBack.happened is called
   * @param mouseMovedResponse
   * @param callBack
   */
  public static void setMouseMovedResponse(int mouseMovedResponse, ObserverCallBack callBack) {
    if (mouseMovedResponse == 3) {
      if(callBack != null) {
        Mouse.mouseMovedResponse = 3;
        Mouse.callBack = callBack;
      }
    }
  }

  /**
   * to block the mouse globally <br>
   * only the contained mouse methods without owner will be granted
   *
   * @return success
   */
  public static boolean block() {
    return block(null);
  }

  /**
   * to block the mouse globally for the given owner <br>
   * only the contained mouse methods having the same owner will be granted
   *
   * @return success
   */
  public static boolean block(Object owner) {
    if (Mouse.get().use(owner)) {
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
   * @return success  (false means: not blocked currently for this owner)
   */
  public static boolean unblock(Object owner) {
    if (owner == null) {
      owner = Mouse.get();
    } else if (owner instanceof Region) {
      if (((Region) owner).isOtherScreen()) {
        return false;
      }
    }
    if (blocked && Mouse.get().owner == owner) {
      blocked = false;
      Mouse.get().let(owner);
      return true;
    }
    return false;
  }

  private static boolean use() {
    return Mouse.get().use(null);
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
      Debug.log(3, "Mouse: use start: %s", owner);
      return true;
    }
    Debug.error("Mouse: synch problem - use start: %s", owner);
    return false;
  }

  private static boolean let() {
    return Mouse.get().let(null);
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
      PointerInfo mp = MouseInfo.getPointerInfo();
      if (mp != null) {
        lastPos = MouseInfo.getPointerInfo().getLocation();        
      } else {
        Debug.error("Mouse: not possible to get last mouse position (PointerInfo == null)");
      }
      inUse = false;
      this.owner = null;
      notify();
      Debug.log(3, "Mouse: use stop: %s", owner);
      return true;
    }
    return false;
  }

  protected synchronized boolean keep(Object owner) {
    if (owner == null) {
      owner = this;
    } else if (owner instanceof Region) {
      if (((Region) owner).isOtherScreen()) {
        return false;
      }
    }
    if (inUse && this.owner == owner) {
      keep = true;
      Debug.log(3, "Mouse: use keep: %s", owner);
      return true;
    }
    return false;
  }

  private void checkLastPos() {
    if (lastPos == null) {
      return;
    }
    Point pos = MouseInfo.getPointerInfo().getLocation();
    if (lastPos.x != pos.x || lastPos.y != pos.y) {
      Debug.error("Mouse: moved externally");
      if (mouseMovedResponse > 0) {
        showMousePos(pos);
      }
      if (mouseMovedResponse == 2) {
//TODO implement 2
        return;
      }
      if (mouseMovedResponse == 3) {
//TODO implement 3
        if (callBack != null) {
          callBack.happened(new ObserveEvent("MouseMoved", ObserveEvent.Type.GENERIC, 
                  lastPos, new Location(pos), null, (new Date()).getTime()));
        }
      }
    }
  }
  
  public static boolean hasMoved() {
    Point pos = MouseInfo.getPointerInfo().getLocation();
    if (Mouse.get().lastPos.x != pos.x || Mouse.get().lastPos.y != pos.y) {
      return true;
    }
    return false;
  }

  private void showMousePos(Point pos) {
    Location lPos = new Location(pos);
    Region inner = lPos.grow(20).highlight();
    delay(500);
    lPos.grow(40).highlight(1);
    delay(500);
    inner.highlight();
  }

  private static void delay(int time) {
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

  /**
   * to click (left, right, middle - single or double) at the given location using the given button
   * only useable for local screens
   *
   * timing parameters: <br>
   * - one value <br>
   * &lt; 0 wait before mouse down <br>
   * &gt; 0 wait after mouse up <br>
   * - 2 or 3 values
   * 1st wait before mouse down <br>
   * 2nd wait after mouse up <br>
   * 3rd inner wait (milli secs, cut to 1000): pause between mouse down and up (Settings.ClickDelay)
   *
   * wait before and after: &gt; 59 taken as milli secs - &lt; are seconds
   *
   * @param loc where to click
   * @param action L,R,M left, right, middle - D means double click
   * @param args timing parameters
   * @return the location
   */
  public static Location click(Location loc, String action, Integer... args) {
    if (suspended || loc.isOtherScreen()) {
      return null;
    }
    getArgsClick(Mouse.get(), loc, action, args);
    use();
    delay(Mouse.get().beforeWait);
    Settings.ClickDelay = Mouse.get().innerWait / 1000;
    click(loc, Mouse.get().buttons, 0, Mouse.get().clickDouble, null);
    delay(Mouse.get().afterWait);
    let();
    return loc;
  }

  private static void getArgsClick(Mouse mouse, Location loc, String action, Integer... args) {
    mouse.mousePos = loc;
    mouse.clickDouble = false;
    action = action.toUpperCase();
    if (action.contains("D")) {
      mouse.clickDouble = true;
    }
    mouse.buttons = 0;
    if (action.contains("L")) {
      mouse.buttons += LEFT;
    }
    if (action.contains("M")) {
      mouse.buttons += MIDDLE;
    }
    if (action.contains("R")) {
      mouse.buttons += RIGHT;
    }
    if (mouse.buttons == 0) {
      mouse.buttons = LEFT;
    }
    mouse.beforeWait = 0;
    mouse.innerWait = 0;
    mouse.afterWait = 0;
    if (args.length > 0) {
      if (args.length == 1) {
        if (args[0] < 0) {
          mouse.beforeWait = -args[0];
        } else {
          mouse.afterWait = args[0];
        }
      }
      mouse.beforeWait = args[0];
      if (args.length > 1) {
        mouse.afterWait = args[1];
        if (args.length > 2) {
          mouse.innerWait = args[2];
        }
      }
    }
  }

  protected static int click(Location loc, int buttons, int modifiers, boolean dblClick, Region region) {
    if (loc == null) {
      return 0;
    }
    IRobot r = loc.getRobotForPoint("click");
    if (r == null) {
      return 0;
    }
    get().use(region);
    Debug.action(getClickMsg(loc, buttons, modifiers, dblClick));
    r.smoothMove(loc);
    r.clickStarts();
    r.pressModifiers(modifiers);
    int pause = Settings.ClickDelay > 1 ? 1 : (int) (Settings.ClickDelay * 1000);
    Settings.ClickDelay = 0.0;
    if (dblClick) {
      r.mouseDown(buttons);
      r.mouseUp(buttons);
      r.mouseDown(buttons);
      r.mouseUp(buttons);
    } else {
      r.mouseDown(buttons);
      r.delay(pause);
      r.mouseUp(buttons);
    }
    r.releaseModifiers(modifiers);
    r.clickEnds();
    r.waitForIdle();
    get().let(region);
    return 1;
  }

  private static String getClickMsg(Location loc, int buttons, int modifiers, boolean dblClick) {
    String msg = "";
    if (modifiers != 0) {
      msg += KeyEvent.getKeyModifiersText(modifiers) + "+";
    }
    if (buttons == InputEvent.BUTTON1_MASK && !dblClick) {
      msg += "CLICK";
    }
    if (buttons == InputEvent.BUTTON1_MASK && dblClick) {
      msg += "DOUBLE CLICK";
    }
    if (buttons == InputEvent.BUTTON3_MASK) {
      msg += "RIGHT CLICK";
    } else if (buttons == InputEvent.BUTTON2_MASK) {
      msg += "MID CLICK";
    }
    msg += " on " + loc;
    return msg;
  }

  /**
   * move the mouse to the given location (local and remote)
   *
   * @param loc
   * @return 1 for success, 0 otherwise
   */
  public static int move(Location loc) {
    return move(loc, null);
  }

  protected static int move(Location loc, Region region) {
    if (suspended) {
      return 0;
    }
    if (loc != null) {
      IRobot r = loc.getRobotForPoint("mouseMove");
      if (r == null) {
        return 0;
      }
      if (!r.isRemote()) {
        Mouse.get().use(region);
      }
      r.smoothMove(loc);
      r.waitForIdle();
      if (!r.isRemote()) {
        Mouse.get().let(region);
      }
      return 1;
    }
    return 0;
  }

  /**
   * press and hold the given buttons
   *
   * @param buttons
   */
  public static void down(int buttons) {
    down(buttons, null);
  }

  protected static void down(int buttons, Region region) {
    if (suspended) {
      return;
    }
    get().use(region);
    Screen.getPrimaryScreen().getRobot().mouseDown(buttons);
  }

  /**
   * release all buttons
   *
   */
  public static void up() {
    up(0, null);
  }

  /**
   * release the given buttons
   *
   * @param buttons (0 releases all buttons)
   */
  public static void up(int buttons) {
    up(buttons, null);
  }

  protected static void up(int buttons, Region region) {
    if (suspended) {
      return;
    }
    if (0 == Screen.getPrimaryScreen().getRobot().mouseUp(buttons)) {
      get().let(region);
    }
  }

  /**
   * move mouse using mouse wheel in the given direction the given steps <br>
   * the result is system dependent
   *
   * @param direction
   * @param steps
   */
  public static void wheel(int direction, int steps) {
    wheel(direction, steps, null);
  }

  protected static void wheel(int direction, int steps, Region region) {
    if (suspended) {
      return;
    }
    IRobot r = Screen.getPrimaryScreen().getRobot();
    get().use(region);
    Debug.log(3, "Region: wheel: %s steps: %d",
            (direction == WHEEL_UP ? "WHEEL_UP" : "WHEEL_DOWN"), steps);
    for (int i = 0; i < steps; i++) {
      r.mouseWheel(direction);
      r.delay(50);
    }
    get().let(region);
  }
}
