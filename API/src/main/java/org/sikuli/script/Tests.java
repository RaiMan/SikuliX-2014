package org.sikuli.script;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

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
    File fSetupJar = null;
    if (rt.fSxProject == null) {
      fSetupJar = new File(rt.fSxBase, "sikulixsetup-1.1.0.jar");
    } else {
      fSetupJar = new File(rt.fSxProject, "Setup/target/sikulixsetup-1.1.0-plain.jar");
    }
    if (!fSetupJar.exists()) {
      log(-1, "cannot run - missing: %s", fSetupJar);
      System.exit(1);
    }
    rt.addToClasspath(fSetupJar.getPath());
    if (Debug.getDebugLevel() > 1) {
      rt.dumpClassPath();
    }
    Screen s = new Screen();
    ImagePath.add("org.sikuli.setup.RunSetup/Images");
    s.exists("SikuliLogo");
    s.exists("SikuliLogo");
    s.highlight(-2);
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
    if (!rt.resourceListAsFile("Lib", rt.fTestFile, null)) {
      msg = "did not work";
    }
    log(lvl, "*** %s *** test03_ResourceListToFile: Lib to:\n%s", msg, rt.fTestFile);
  }  

  public static void test05_MakeLibswinContentFile() {
    rt = RunTime.get();
    String msg = "worked";
    addJarToCP("Libswin", null);
    File content = new File(rt.fTestFolder, "sikulixcontent");
    if (!rt.resourceListAsFile("META-INF/libs/windows", content, null)) {
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
}
