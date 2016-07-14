/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.script.Image;

import java.awt.*;

/**
 * Created by RaiMan on 12.07.16.
 * <p>
 * Test for the basic ADB based features
 */
public class ADBTest {

  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "ADBDevice: " + message, args);
  }

  private static void logp(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  public static void main(String[] args) throws FindFailed {

    boolean runTests = false;

    ADBScreen aScr = startTest();

    if (aScr.isValid()) {
      if (runTests) {

        basicTest(aScr);
        captureRawTest(aScr);

        System.exit(0);
      }
    } else {
      System.exit(1);
    }

    // ********* playground
    Rectangle rect = null;
    byte[] image = null;
    for (int i = 0; i < 1920; i += 100) {
      rect = new Rectangle(0, i, 1200, 100);
      image = aScr.rawcapture(rect);
    }
  }

  private static ADBScreen startTest() {
    ADBScreen adbs = new ADBScreen();
    Debug.on(3);
    log(lvl, "%s", adbs);
    if (adbs.isValid()) {
      adbs.wakeUp(2);
      adbs.wait(1f);
      adbs.key(adbs.HOME);
      adbs.wait(1f);
    }
    return adbs;
  }


  private static void basicTest(ADBScreen adbs) throws FindFailed {
    Debug.info("**************** running basic test");
      adbs.swipeLeft();
      adbs.swipeRight();
      adbs.wait(1f);
      ScreenImage sIMg = adbs.userCapture("Android");
      sIMg.save(RunTime.get().fSikulixStore.getAbsolutePath(), "android");
      adbs.tap(new Image(sIMg));
  }

  private static void captureRawTest(ADBScreen adbs) {
    Debug.info("**************** running test raw capture");
    byte[] image = null;
    image = adbs.getDevice().captureDeviceScreenRaw();
    image = adbs.getDevice().captureDeviceScreenRaw(0, 250);
    image = adbs.getDevice().captureDeviceScreenRaw((int) (adbs.h / 2 - 125), 250);
    image = adbs.getDevice().captureDeviceScreenRaw(adbs.h - 250 - 1, 250);
    image = adbs.getDevice().captureDeviceScreenRaw(500, 1000, 250, 250);
    image = adbs.getDevice().captureDeviceScreenRaw(adbs.getCenter().x, adbs.getCenter().y, 1, 1);
  }
}
