/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;

/**
 * Main pupose is to coordinate the keyboard usage among threads <br>
 * At any one time, the keyboard has one owner (usually a Region object) <br>
 * who exclusively uses the keyboard, all others wait for the keyboard to be free again <br>
 * if more than one possible owner is waiting, the next owner is uncertain <br>
 * It is detected, when the keyboard is usaed externally of the workflow, which can be used for appropriate actions
 * (e.g. pause a script) <br>
 * the keyboard can be blocked for a longer time, so only this owner can use the keyboard (like some transactional
 * processing) <br>
 * Currently deadlocks and infinite waits are not detected, but should not happen ;-) <br>
 * Contained are methods to use the keyboard (click, press, release) as is<br>
 * The keys are specified completely and only as string either litterally by their keynames<br>
 */
public class Keys {

  private static String me = "KeyBoard: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static Keys keys = null;
  private static Device device = null;

  private Keys() {
  }

  public static void init() {
    if (keys == null) {
      keys = new Keys();
      device = new Device(keys);
      log(3, "init");
    }
  }
}
