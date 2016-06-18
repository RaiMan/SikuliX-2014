/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
package org.sikuli.basics;

public abstract class HotkeyListener {

  /**
   * Override this to implement your own hotkey handler.
   *
   * @param e HotkeyEvent
   */
  abstract public void hotkeyPressed(HotkeyEvent e);

  /**
   * INTERNAL USE: system specific handler implementation
   *
   * @param e HotkeyEvent
   */
  public void invokeHotkeyPressed(final HotkeyEvent e) {
    Thread hotkeyThread = new Thread() {
      @Override
      public void run() {
        hotkeyPressed(e);
      }
    };
    hotkeyThread.start();
  }
}
