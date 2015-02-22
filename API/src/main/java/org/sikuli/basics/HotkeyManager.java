/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2014, 2015
 */
package org.sikuli.basics;

/**
 * use org.sikuli.script.HotkeyManager instead
 * instance is redirected
 * @deprecated
 */
@Deprecated
public abstract class HotkeyManager extends org.sikuli.script.HotkeyManager {
  
  private static org.sikuli.script.HotkeyManager instance = null;

  public static HotkeyManager getInstance() {
    if (null == instance) {
      instance = org.sikuli.script.HotkeyManager.getInstance();
    }
    return (HotkeyManager) instance;
  }
}
