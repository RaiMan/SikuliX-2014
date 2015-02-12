package org.sikuli.script;

import java.io.File;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;

  static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  static File getScriptFile(File givenScriptFolder) {
    File[] content = FileManager.getScriptFile(givenScriptFolder);
    return null;
  }
  
  public static int run(String givenName) {
    return run(givenName, new String[0]);
  }

  public static int run(String givenName, String[] args) {
    return 0;
  }

  public static int runTest(String givenName) {
    return runTest(givenName, new String[0]);
  }

  public static int runTest(String givenName, String[] args) {
    return 0;
  }

  static class RunBox {

    static File scriptProject = null;

    RunTime runTime = RunTime.get();
    boolean asTest = false;
    String givenScriptName = "";
    String[] args = new String[0];

    RunBox(String givenName, String[] givenArgs, boolean isTest) {
      givenScriptName = givenName;
      args = givenArgs;
      asTest = isTest;
    }

    int run() {
      int exitCode = 0;
      log(lvl, "givenScriptName:\n%s", givenScriptName);
      if (-1 == FileManager.slashify(givenScriptName, false).indexOf("/") && RunBox.scriptProject != null) {
        givenScriptName = new File(scriptProject, givenScriptName).getPath();
      }
      if (givenScriptName.endsWith(".skl")) {
        givenScriptName = FileManager.unzipSKL(givenScriptName);
        if (givenScriptName == null) {
          log(-1, "not possible to make .skl runnable");
          return -9999;
        }
      }
      File fScript = getScriptFile(new File(givenScriptName));
      if (fScript == null) {
        return -9999;
      }
      fScript = new File(FileManager.normalizeAbsolute(fScript.getPath(), true));
      if (null == scriptProject) {
        scriptProject = fScript.getParentFile().getParentFile();
      }
      log(lvl, "Trying to run script:\n%s", fScript);
//      Object currentRunner = getRunner(fScript.getName(), null);
//      ImagePath.setBundlePath(fScript.getParent());
//      if (asTest) {
//        exitCode = currentRunner.runTest(fScript, null, args, null);
//      } else {
//        exitCode = currentRunner.runScript(fScript, null, args, null);
//      }
//      currentRunner.close();
      return exitCode;
    }
  }
}
