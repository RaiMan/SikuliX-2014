/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.android;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.script.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

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

  private static RunTime rt = null;

  public static void main(String[] args) throws FindFailed {

    boolean runTests = true;

    ADBScreen aScr = startTest();

    if (aScr.isValid()) {
      if (runTests) {

        basicTest(aScr);

        ADBScreen.stop();

        System.exit(0);
      }
    } else {
      System.exit(1);
    }

    // ********* playground
  }

  private static ADBScreen startTest() {
    Debug.on(3);
    rt = RunTime.get();
    ADBScreen adbs = new ADBScreen();
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
    log(lvl, "**************** running basic test");
    adbs.swipeLeft();
    adbs.swipeRight();
    adbs.wait(1f);
    ScreenImage sIMg = adbs.userCapture("Android");
    sIMg.save(RunTime.get().fSikulixStore.getAbsolutePath(), "android");
    adbs.tap(new Image(sIMg));
  }
}
