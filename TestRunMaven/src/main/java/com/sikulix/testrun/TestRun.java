package com.sikulix.testrun;

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

public class TestRun {

  static Screen s = new Screen();
  static boolean shouldExit = false;

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  private static void terminate(int retVal, String msg, Object... args ) {
    p(msg, args);
    System.exit(retVal);
  }

  public static void main(String[] args) throws FindFailed, IOException {

    Debug.on(3);

    ImagePath.add(TestRun.class.getCanonicalName() + "/ImagesAPI.sikuli");
		s.find("SikuliLogo");
		s.highlight(-2);

  }

  public static void test1() {
    ImagePath.add(TestRun.class.getCanonicalName() + "/ImagesAPI.sikuli");
    File fResults = new File(System.getProperty("user.home"), "TestResults");
    String fpResults = fResults.getPath();
    FileManager.deleteFileOrFolder(fpResults);
    fResults.mkdirs();

    if (Settings.isMac()) {
      App.focus("Safari");
    } else {
      App.focus("Google Chrome");
    }
    String raimanlogo = "raimanlogo";
    Match mFound = null;
    try {
      if (null == s.exists(raimanlogo, 0)) {
        Desktop.getDesktop().browse(new URI("http://sikulix.com"));
        s.wait(raimanlogo, 10);
      }
      s.hover();

      Region winBrowser = App.focusedWindow();

      String image = "btnnightly";
      mFound = winBrowser.exists(image);
      if (null != mFound) {
        p("mFound: %s", mFound);
        p("mFound.Image: %s", mFound.getImage());
        p("mFound.ImageFile: %s", mFound.getImageFilename());
        winBrowser.click();
        winBrowser.getLastScreenImageFile(fpResults, image + "screen.png");
      } else {
        terminate(1, "missing: %s", image);
        System.exit(1);
      }
      image = "nightly";
      mFound = winBrowser.exists(image, 10);
      if (null != mFound) {
        p("mFound: %s", mFound);
        p("mFound.Image: %s", mFound.getImage());
        p("mFound.ImageFile: %s", mFound.getImageFilename());
        winBrowser.getLastScreenImageFile(fpResults, image + "screen.png");
      } else {
        terminate(1, "missing: %s", image);
      }
    } catch (Exception ex) {
      terminate(1, "some problems");
    }
    s.write("#C.w");
    s.wait(2f);
    App.focus("NetBeans");
    System.exit(1);
  }
}
