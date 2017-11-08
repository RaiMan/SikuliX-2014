package com.sikulix.mavenrun;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.sikuli.script.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.HotkeyEvent;
import org.sikuli.basics.HotkeyListener;
import org.sikuli.basics.HotkeyManager;
import org.sikuli.basics.Settings;

public class MavenRun {

  static Screen s = new Screen();
  static boolean shouldExit = false;

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  private static void terminate(int retVal, String msg, Object... args) {
    p(msg, args);
    System.exit(retVal);
  }

  public static void main(String[] args) throws FindFailed, IOException {
    Debug.on(3);
    Sikulix.popup("hello");
  }
}
