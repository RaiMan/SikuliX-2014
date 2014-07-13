/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2014
 */
package org.sikuli.basics;

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to bind hotkeys to hotkey listeners
 */
public abstract class HotkeyManager {

  private static HotkeyManager _instance = null;
  private static Map<Integer, Integer> hotkeys;
  private static Map<Integer, Integer> hotkeysGlobal = new HashMap<Integer, Integer>();
  private static final String HotkeyTypeCapture = "Capture";
  private static int HotkeyTypeCaptureKey;
  private static int HotkeyTypeCaptureMod;
  private static final String HotkeyTypeAbort = "Abort";
  private static int HotkeyTypeAbortKey;
  private static int HotkeyTypeAbortMod;

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
          Debug.error("HotkeyManager: Can't create " + cls + ": " + e.getMessage());
        }
      }
    }
    hotkeys = new HashMap<Integer, Integer>();
    return _instance;
  }

  /**
   * remove all hotkeys and reset HotkeyManager to undefined
   */
  public static void reset() {
    if (_instance == null || hotkeys.isEmpty()) {
      return;
    }
    Debug.log(3, "HotkeyManager: reset - removing all defined hotkeys.");
    boolean res;
    for (Integer k : hotkeys.keySet()) {
      res = _instance._removeHotkey(k, hotkeys.get(k));
      if (!res) {
        Debug.error("HotkeyManager: reset: failed to remove hotkey: %s %s",
                getKeyModifierText(hotkeys.get(k)), getKeyCodeText(k));
      }
    }
    hotkeys = new HashMap<Integer, Integer>();
    _instance = null;
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
        Debug.error("HotkeyManager: Hotkey registration is not supported on your OS.");
    }
    return null;
  }

  protected static String getKeyCodeText(int key) {
    return KeyEvent.getKeyText(key).toUpperCase();
  }

  protected static String getKeyModifierText(int modifiers) {
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
   *
   * @param hotkeyType
   * @param callback
   * @return
   */
  public boolean addHotkey(String hotkeyType, HotkeyListener callback) {
    PreferencesUser pref;
    if (hotkeyType == HotkeyTypeCapture) {
      pref = PreferencesUser.getInstance();
      HotkeyTypeCaptureKey = pref.getCaptureHotkey();
      HotkeyTypeCaptureMod = pref.getCaptureHotkeyModifiers();
      return installHotkey(HotkeyTypeCaptureKey, HotkeyTypeCaptureMod, callback, hotkeyType);
    } else if (hotkeyType == HotkeyTypeAbort) {
      pref = PreferencesUser.getInstance();
      HotkeyTypeAbortKey = pref.getStopHotkey();
      HotkeyTypeAbortMod = pref.getStopHotkeyModifiers();
      return installHotkey(HotkeyTypeAbortKey, HotkeyTypeAbortMod, callback, hotkeyType);
    } else {
      Debug.error("HotkeyManager: addHotkey: using HotkeyType as %s not supported yet", hotkeyType);
      return false;
    }
  }

  /**
   * install a hotkey listener.
   *
   * @return true if success. false otherwise.
   */
  public boolean addHotkey(char key, int modifiers, HotkeyListener callback) {
    return addHotkey("" + key, modifiers, callback);
  }

  /**
   * install a hotkey listener.
   *
   * @param key
   * @param modifiers
   * @param callback
   * @return true if success. false otherwise.
   */
  public boolean addHotkey(String key, int modifiers, HotkeyListener callback) {
    int[] keyCodes = SikuliX.callKeyToJavaKeyCodeMethod(key.toLowerCase());
    int keyCode = keyCodes[0];
    return installHotkey(keyCode, modifiers, callback, "");
  }

  private boolean installHotkey(int key, int mod, HotkeyListener callback, String hotkeyType) {
    boolean res;
    String txtMod = getKeyModifierText(mod);
    String txtCode = getKeyCodeText(key);
    Debug.info("HotkeyManager: add" + hotkeyType + "Hotkey: " + txtMod + " " + txtCode);
    if (hotkeys.containsKey(key) && mod == hotkeys.get(key)) {
      res = _instance._removeHotkey(key, hotkeys.get(key));
      if (!res) {
        Debug.error("HotkeyManager: addHotkey: failed to remove already defined hotkey");
        return false;
      }
    }
    res = _instance._addHotkey(key, mod, callback);
    if (res) {
      if (hotkeyType.isEmpty()) {
        hotkeys.put(key, mod);
      } else {
        hotkeysGlobal.put(key, mod);
      }
    } else {
      Debug.error("HotkeyManager: addHotkey: failed");
    }
    return res;
  }

  public boolean removeHotkey(String hotkeyType) {
    if (hotkeyType == HotkeyTypeCapture) {
      return uninstallHotkey(HotkeyTypeCaptureKey, HotkeyTypeCaptureMod, hotkeyType);
    } else if (hotkeyType == HotkeyTypeAbort) {
      return uninstallHotkey(HotkeyTypeAbortKey, HotkeyTypeAbortMod, hotkeyType);
    } else {
      Debug.error("HotkeyManager: removeHotkey: using HotkeyType as %s not supported yet", hotkeyType);
      return false;
    }
  }

  /**
   * uninstall a hotkey listener.
   *
   * @return true if success. false otherwise.
   */
  public boolean removeHotkey(char key, int modifiers) {
    return removeHotkey("" + key, modifiers);
  }

  /**
   * uninstall a hotkey listener.
   *
   * @param key
   * @param modifiers
   * @return true if success. false otherwise.
   */
  public boolean removeHotkey(String key, int modifiers) {
    int[] keyCodes = SikuliX.callKeyToJavaKeyCodeMethod(key.toLowerCase());
    int keyCode = keyCodes[0];
    return uninstallHotkey(keyCode, modifiers, "");
  }
  
  private boolean uninstallHotkey(int key, int mod, String hotkeyType) {
    boolean res;
    String txtMod = getKeyModifierText(mod);
    String txtCode = getKeyCodeText(key);
    Debug.info("HotkeyManager: remove" + hotkeyType + "Hotkey: " + txtMod + " " + txtCode);
    res = _instance._removeHotkey(key, mod);
    if (res) {
      hotkeys.remove(key);
    } else {
      Debug.error("HotkeyManager: removeHotkey: failed");
    }
    return res;
  }

  abstract public boolean _addHotkey(int keyCode, int modifiers, HotkeyListener callback);

  abstract public boolean _removeHotkey(int keyCode, int modifiers);

  abstract public void cleanUp();
}
