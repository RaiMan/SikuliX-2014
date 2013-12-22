/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.basics;

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;

public abstract class HotkeyManager {

  protected static HotkeyManager _instance = null;

  public static HotkeyManager getInstance() {
    if (_instance == null) {
/* uncomment for debugging puposes
      if (Settings.isWindows()) {
        _instance = new WindowsHotkeyManager();
      } else if (Settings.isMac()) {
        _instance = new MacHotkeyManager();
      } else if (Settings.isLinux()) {
        _instance = new LinuxHotkeyManager();
      }
      return _instance;
*/
      String cls = getOSHotkeyManagerClass();
      if (cls != null) {
        try {
          Class c = Class.forName(cls);
          Constructor constr = c.getConstructor();
          _instance = (HotkeyManager) constr.newInstance();
        } catch (Exception e) {
          Debug.error("Can't create " + cls + ": " + e.getMessage());
        }
      }
    }
    return _instance;
  }

  private static String getOSHotkeyManagerClass() {
    String pkg = "org.sikuli.basics.";
    int theOS = Settings.getOS();
    switch (theOS) {
      case Settings.ISMAC:
        return pkg + "MacHotkeyManager";
      case Settings.ISWINDOWS:
        return pkg + "WindowsHotkeyManager";
      case Settings.ISLINUX:
        return pkg + "LinuxHotkeyManager";
      default:
        Debug.error("Error: Hotkey registration is not supported on your OS.");
    }
    return null;
  }

  protected String getKeyCodeText(int key) {
    return KeyEvent.getKeyText(key).toUpperCase();
  }

  protected String getKeyModifierText(int modifiers) {
    String txtMod = KeyEvent.getKeyModifiersText(modifiers).toUpperCase();
    if (Settings.isMac()) {
      txtMod = txtMod.replace("META", "CMD");
      txtMod = txtMod.replace("WINDOWS", "CMD");
    } else {
      txtMod = txtMod.replace("META", "WIN");
      txtMod = txtMod.replace("WINDOWS", "WIN");
    }
    return txtMod;
  }

  /**
   * install a hotkey listener.
   *
   * @return true if success. false otherwise.
   */
  public boolean addHotkey(char key, int modifiers, HotkeyListener listener) {
    return addHotkey(""+key, modifiers, listener);
  }

  /**
   * install a hotkey listener.
   *
   * @return true if success. false otherwise.
   */
  public boolean addHotkey(String key, int modifiers, HotkeyListener listener) {
    int[] keyCodes = SikuliX.callKeyToJavaKeyCodeMethod(key.toLowerCase());
    int keyCode = keyCodes[0];
    String txtMod = getKeyModifierText(modifiers);
    String txtCode = getKeyCodeText(keyCode);
    Debug.info("add hotkey: " + txtMod + " " + txtCode);
    return _instance._addHotkey(keyCode, modifiers, listener);
  }
  
    public boolean addHotkey(int htype, HotkeyListener listener) {
      return true;
    }


  /**
   * uninstall a hotkey listener.
   *
   * @return true if success. false otherwise.
   */
  public boolean removeHotkey(char key, int modifiers) {
    return removeHotkey(""+key, modifiers);
  }

  /**
   * uninstall a hotkey listener.
   *
   * @return true if success. false otherwise.
   */
  public boolean removeHotkey(String key, int modifiers) {
    int[] keyCodes = SikuliX.callKeyToJavaKeyCodeMethod(key.toLowerCase());
    int keyCode = keyCodes[0];
    String txtMod = getKeyModifierText(modifiers);
    String txtCode = getKeyCodeText(keyCode);
    Debug.info("remove hotkey: " + txtMod + " " + txtCode);
    return _instance._removeHotkey(keyCode, modifiers);
  }

  abstract public boolean _addHotkey(int keyCode, int modifiers, HotkeyListener listener);

  abstract public boolean _removeHotkey(int keyCode, int modifiers);

  abstract public void cleanUp();
}
