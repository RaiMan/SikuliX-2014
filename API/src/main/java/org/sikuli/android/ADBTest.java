/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;

/**
 * Created by RaiMan on 12.07.16.
 *
 * Test for the basic ADB based features
 */
public class ADBTest {

  private static int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "ADBDevice: " + message, args);
  }

  public static void main(String[] args) throws FindFailed {
    ADBScreen adbs = new ADBScreen();
    Debug.on(3);
    log(lvl, "%s", adbs);
    if (adbs.isValid()) {
      adbs.wakeUp(2);
      adbs.swipeLeft();
      adbs.swipeRight();
      adbs.wait(1f);
      ScreenImage sIMg = adbs.userCapture("Android");
      sIMg.save(RunTime.get().fSikulixStore.getAbsolutePath(), "android");
      adbs.tap(new Image(sIMg));
    }
    RunTime.pause(3);
    Debug.off();
  }
}
