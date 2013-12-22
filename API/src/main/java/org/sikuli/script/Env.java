/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.awt.*;
import org.sikuli.basics.HotkeyListener;
import org.sikuli.basics.HotkeyManager;
import org.sikuli.basics.OS;
import org.sikuli.natives.OSUtil;
import org.sikuli.basics.Settings;
import org.sikuli.natives.SysUtil;

/**
 * features moved to other classes, details below with the methods
 * @author RaiMan
 * @deprecated
 */
@Deprecated
public class Env {

  /**
   * @deprecated use Settings.getVersion() instead
   */
  @Deprecated
  public static String SikuliVersion = Settings.SikuliVersion;

  /**
   *
   * @return where we store Sikuli specific data
   * @deprecated use Settings. ... instead
   */
  @Deprecated
  public static String getSikuliDataPath() {
    return Settings.getSikuliDataPath();
  }

  /**
   * @return version
   * @deprecated use Settings.SikuliVersion
   */
  @Deprecated
  public static String getSikuliVersion() {
    return Settings.SikuliVersion;
  }

  /**
   * @return current Location
   * @throws HeadlessException
   * @deprecated use Region.atMouse() ... instead
   */
  @Deprecated
  public static Location getMouseLocation() throws HeadlessException {
    return Region.atMouse();
  }

  @Deprecated
  public static OSUtil getOSUtil() {
    return SysUtil.getOSUtil();
  }

  /**
   * @return version (java: os.version)
   * @deprecated use Settings. ... instead
   */
  @Deprecated
  public static String getOSVersion() {
    return Settings.getOSVersion();
  }

  /**
   * use Settings.isWindows .isMac .isLinux instead
   * @return the OS.XXX
   * @deprecated use the Settings features
   */
  @Deprecated
  public static OS getOS() {
		if (Settings.isWindows()) {
			return OS.WINDOWS;
		} else if (Settings.isMac()) {
			return OS.MAC;
		} else if (Settings.isLinux()) {
			return OS.LINUX;
		} else {
			return OS.NOT_SUPPORTED;
		}
	}

  /**
   * @return true/false
   * @deprecated use Settings. ... instead
   */
  @Deprecated
  public static boolean isWindows() {
    return Settings.isWindows();
  }

  /**
   * @return true/false
   * @deprecated use Settings. ... instead
   */
  @Deprecated
  public static boolean isLinux() {
    return Settings.isLinux();
  }

  /**
   * @return true/false
   * @deprecated use Settings. ... instead
   */
  @Deprecated
  public static boolean isMac() {
    return Settings.isMac();
  }

  /**
   * @return path seperator : or ;
   * @deprecated use Settings.getPathSeparator() ... instead
   */
  @Deprecated
  public static String getSeparator() {
    return Settings.getPathSeparator();
  }

  /**
   *
   * @return content
   * @deprecated use App. ... instead
   */
  @Deprecated
  public static String getClipboard() {
    return App.getClipboard();
  }

  /**
   * set content
   *
   * @param text
   * @deprecated use App. ... instead
   */
  @Deprecated
  public static void setClipboard(String text) {
		App.setClipboard(text);
	}

  /**
   * get the lock state of the given key
   * @param key
   * @return true/false
   * @deprecated use Key. ... instead
   */
  @Deprecated
  public static boolean isLockOn(char key) {
    return Key.isLockOn(key);
  }

  /**
   *
   * @return System dependent key
   * @deprecated use Key. ... instead
   */
  @Deprecated
  public static int getHotkeyModifier() {
    return Key.getHotkeyModifier();
  }

  /**
   *
   * @param key
   * @param modifiers
   * @param listener
   * @return true if ok, false otherwise
   * @deprecated use Key. ... instead
   */
  @Deprecated
  public static boolean addHotkey(String key, int modifiers, HotkeyListener listener) {
    return HotkeyManager.getInstance().addHotkey(key, modifiers, listener);
  }

  /**
   *
   * @param key
   * @param modifiers
   * @param listener
   * @return true if ok, false otherwise
   * @deprecated use Key. ... instead
   */
  @Deprecated
  public static boolean addHotkey(char key, int modifiers, HotkeyListener listener) {
    return HotkeyManager.getInstance().addHotkey(key, modifiers, listener);
  }

  /**
   *
   * @param key
   * @param modifiers
   * @return true if ok, false otherwise
   * @deprecated use Key. ... instead
   */
  @Deprecated
  public static boolean removeHotkey(String key, int modifiers) {
    return HotkeyManager.getInstance().removeHotkey(key, modifiers);
  }

  /**
   *
   * @param key
   * @param modifiers
   * @return true if ok, false otherwise
   * @deprecated use Key. ... instead
   */
  @Deprecated
  public static boolean removeHotkey(char key, int modifiers) {
    return HotkeyManager.getInstance().removeHotkey(key, modifiers);
  }

  public static void cleanUp() {
    HotkeyManager.getInstance().cleanUp();
  }
}
