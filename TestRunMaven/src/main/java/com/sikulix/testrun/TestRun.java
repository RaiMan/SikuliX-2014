package com.sikulix.testrun;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.sikuli.script.*;
import org.sikuli.basics.Debug;

public class TestRun {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }
  
  private static void terminate(int retVal, String msg, Object... args ) {
    p(msg, args);
    System.exit(retVal);
  }

  public static void main(String[] args) throws FindFailed, IOException {

    Screen s = new Screen();
    Debug.on(3);

    ImagePath.add(TestRun.class.getCanonicalName() + "/ImagesAPI.sikuli");
    File fResults = new File(System.getProperty("user.home"), "TestResults");
    fResults.mkdirs();
    String fpResults = fResults.getPath();

    App.focus("Google Chrome");
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