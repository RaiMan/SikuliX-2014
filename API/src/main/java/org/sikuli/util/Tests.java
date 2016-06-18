/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.util;

import java.awt.Desktop;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URI;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.App;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Match;
import org.sikuli.script.Region;
import org.sikuli.script.RunTime;
import org.sikuli.script.Screen;
import org.sikuli.script.Sikulix;

public class Tests {

  private static RunTime rt;

  private static int lvl = 3;
  private static final String prefNonSikuli = "nonSikuli_";

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "API-Tests: " + message, args);
  }

  private static void logp(String message, Object... args) {
    if (rt.runningWinApp) {
      log(0, message, args);
    } else {
      System.out.println(String.format(message, args));
    }
  }

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  private static void terminate(int retVal, String msg, Object... args) {
    p(msg, args);
    System.exit(retVal);
  }

  public static void runTest(int testNumber) {
    rt = RunTime.get();
    Method[] tests = Tests.class.getDeclaredMethods();

    if (testNumber == 0) {
      logp("***** available tests");
    }
    for (Method test : tests) {
      if (!test.getName().startsWith("test")) {
        continue;
      }
      String tName = test.getName().substring(4);
      int tNum = -1;
      try {
        tNum = Integer.decode(tName.substring(0, 2));
      } catch (Exception ex) {
        continue;
      }
      if (testNumber == 0) {
        logp(tName);
        continue;
      }
      if (testNumber == 1 || tNum == testNumber) {
        logp("\n========== running test: %s ==========", test.getName());
        try {
          test.invoke(null, new Object[0]);
        } catch (Exception ex) {
          log(-1, "not possible:\n%s", ex);
        }
      }
    }
  }

  public static void test03_ImageFromJar() {
    rt = RunTime.get();
    File fImageJar = null;
    String imagePath = "org.sikuli.script.Image/ImagesAPI";
    String sJar = "sikulixsetup";
    if (!rt.runningWinApp) {
      if (rt.fSxProject == null) {
        if (null == rt.isOnClasspath("/Setup/target/Setup")) {
          fImageJar = new File(rt.fSxBase, "sikulixsetup-1.1.0.jar");
        }
      } else {
        fImageJar = new File(rt.fSxProject, "Setup/target/sikulixsetup-1.1.0-plain.jar");
      }
      if (fImageJar == null || !fImageJar.exists()) {
        fImageJar = rt.fSxBaseJar;
        String sJarApi = rt.isOnClasspath("sikulixapi");
        if (sJarApi == null) {
          sJar = "sikulixapi";
        } else {
          sJar = null;
        }
        if (sJar != null) {
          logp("terminating: cannot run - missing: the jar with images: %s", sJar);
          System.exit(1);
        }
      } else {
        imagePath = "org.sikuli.setup.RunSetup/Images";
        rt.addToClasspath(fImageJar.getPath());
      }
    }
    logp("******** starting test with imagePath: %s", imagePath);
    rt.dumpClassPath();
    Screen s = new Screen();
    ImagePath.add(imagePath);
    String browser = "Google Chrome";
    if (Desktop.isDesktopSupported()) {
      String lp = "https://launchpad.net/sikuli";
      Desktop dt = Desktop.getDesktop();
      if (dt.isSupported(Desktop.Action.BROWSE)) {
        try{
        dt.browse(new URI(lp));
        } catch (Exception ex) {
          rt.terminate(1, "Desktop.browse: %s", lp);
        }
        App appBrowser = new App(browser);
        while (null == appBrowser.window()) {
          s.wait(1.0);
        }
      }
    }
    App.focus(browser);
    s.wait(1.0);
    Region win = App.focusedWindow();
    win.highlight(2);
    if (null != s.exists("SikuliLogo")) {
      s.exists("SikuliLogo", 0);
      s.highlight(-2);
      if (rt.runningWindows) {
        s.write("#A.#F4.");
      } else if (rt.runningMac) {
        s.write("#M.q");
      } else {
        s.write("#C.q");
      }
      s.wait(1.0);
    }
    logp("******** ending test");
  }

  public static void test02_ExtractResourcesFromClasspath() {
    rt = RunTime.get();
    if (rt.testSwitch()) {
      addToCP("Libswin");
    } else {
      addJarToCP("Libswin", null);
    }
    rt.dumpClassPath();
    File testFolder = new File(rt.fUserDir, "SikulixTest");
    FileManager.deleteFileOrFolder(testFolder.getAbsolutePath());
    rt.extractResourcesToFolder("/META-INF/libs", testFolder, new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.endsWith(".txt")) {
          return false;
        }
        if (name.contains("opencv")) {
          return false;
        }
        if (dir.getName().endsWith("libs32")) {
          return false;
        }
        return true;
      }
    });
  }

  public static void test04_ResourceListToFile() {
    rt = RunTime.get();
    String msg = "worked";
    if (null != rt.resourceListAsFile("Lib", rt.fTestFile, null)) {
      msg = "did not work";
    }
    log(lvl, "*** %s *** test03_ResourceListToFile: Lib to:\n%s", msg, rt.fTestFile);
  }

  public static void test05_MakeLibswinContentFile() {
    rt = RunTime.get();
    String msg = "worked";
    addJarToCP("Libswin", null);
    File content = null;
    String fsContent = "Libswin/src/main/resources/META-INF/libs/windows/" + rt.fpContent;
    if (rt.fSxProject != null) {
      content = new File(rt.fSxProject, fsContent);
    } else {
      content = new File(rt.fTestFolder, rt.fpContent);
    }
    if (null != rt.resourceListAsFile("META-INF/libs/windows", content, new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.contains(rt.fpContent)) {
          return false;
        }
        return true;
      }
    })) {
      msg = "did not work";
    }
    log(lvl, "*** %s *** test05_MakeLibswinContentFile: META-INF/libs/windows to:\n%s", msg, content);
  }

  private static boolean addToCP(String folder) {
    if (rt.fSxProject != null) {
      rt.addToClasspath(new File(rt.fSxProject, folder + "/target/classes").getAbsolutePath());
      return true;
    }
    return false;
  }

  private static boolean addJarToCP(String folder, String filter) {
    if (rt.fSxProject != null) {
      File aFolder = new File(rt.fSxProject, folder + "/target");
      if (!aFolder.exists()) {
        return false;
      }
      for (String sFile : aFolder.list()) {
        if (sFile.endsWith(".jar")) {
          if (filter != null && !filter.isEmpty() && !sFile.contains(filter)) {
            continue;
          }
          rt.addToClasspath(new File(aFolder, sFile.toString()).getAbsolutePath());
          return true;
        }
      }
      return false;
    }
    return false;
  }

	private static void lastScreenImageTest() {
      Screen s = new Screen();
      Debug.on(3);

      ImagePath.add(Sikulix.class.getCanonicalName() + "/ImagesAPI.sikuli");
      File fResults = new File(System.getProperty("user.home"), "SikulixScreenImages");
      FileManager.resetFolder(fResults);
      String fpResults = fResults.getPath();

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
          winBrowser.highlight(-1);
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
          winBrowser.highlight(-1);
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
