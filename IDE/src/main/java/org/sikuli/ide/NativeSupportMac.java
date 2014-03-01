/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 
 */
package org.sikuli.ide;

import com.apple.eawt.*;

public class NativeSupportMac implements NativeSupport, AboutHandler, PreferencesHandler, QuitHandler {
  
  private boolean debug = false;

  private void p(String msg, Object... args) {
    if (debug) {
      System.out.println(String.format(msg, args));
    }
  }
  
  @Override
  public void handleAbout(AppEvent.AboutEvent evt) {
    p("[debug] IDE: NativeSupportMac: aboutHandler entered");
    SikuliIDE.getInstance().doAbout();
  }

  @Override
  public void handlePreferences(AppEvent.PreferencesEvent evt) {
    p("[debug] IDE: NativeSupportMac: prefsHandler entered");
    SikuliIDE.getInstance().showPreferencesWindow();
  }

  @Override
  public void handleQuitRequestWith(AppEvent.QuitEvent evt, QuitResponse resp) {
    p("[debug] IDE: NativeSupportMac: quitHandler entered");
    if (!SikuliIDE.getInstance().quit()) {
      resp.cancelQuit();
    } else {
      resp.performQuit();
    }
  }
  
  @Override
  public void initApp(boolean d) {
    debug = d;
    Exception e = null;
    try {
      Application.getApplication().setAboutHandler(this);
      Application.getApplication().setPreferencesHandler(this);
      Application.getApplication().setQuitHandler(this);
      p("[debug] IDE: NativeSupportMac: set handler success");
    } catch (Exception ex) {
      e = ex;
    }
    if (e != null) {
      p("[error] IDE: NativeSupportMac: set handler problem\n%s", e.getMessage());
    }
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Sikuli IDE");
  }

  @Override
  public void initIDE(SikuliIDE ide) {
  }
}