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

    basicTest();

    Debug.info("**************** running test raw capture");
    ADBScreen adbs = new ADBScreen();
    Debug.on(3);
    log(lvl, "%s", adbs);
    byte[] image = null;
    if (adbs.isValid()) {
      adbs.wakeUp(2);
      adbs.wait(1f);
      adbs.key(adbs.HOME);
      adbs.wait(1f);
      image = adbs.getDevice().captureDeviceScreenRaw();
      image = adbs.getDevice().captureDeviceScreenRaw(0, 250);
      image = adbs.getDevice().captureDeviceScreenRaw((int) (adbs.h/2 - 125), 250);
      image = adbs.getDevice().captureDeviceScreenRaw(adbs.h - 250 - 1, 250);
      image = adbs.getDevice().captureDeviceScreenRaw(500, 1000, 250, 250);
      image = adbs.getDevice().captureDeviceScreenRaw(adbs.getCenter().x, adbs.getCenter().y, 1, 1);
    }
    RunTime.pause(3);
    Debug.off();
  }

  private static void basicTest() throws FindFailed {
    Debug.info("**************** running basic test");
    ADBScreen adbs = new ADBScreen();
    Debug.on(3);
    log(lvl, "%s", adbs);
    if (adbs.isValid()) {
      adbs.wakeUp(2);
      adbs.wait(1f);
      adbs.key(adbs.HOME);
      adbs.wait(1f);
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
