/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.scriptrunner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.apache.commons.cli.CommandLine;
import org.sikuli.basics.CommandArgs;
import org.sikuli.basics.CommandArgsEnum;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.IDESupport;
import org.sikuli.basics.IScriptRunner;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Sikulix;

/**
 * Contains the run class
 */
public class ScriptRunner {

	private static final String me = "ScriptRunner: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static IScriptRunner runner = null;
  private static File imagePath;
  private static Boolean runAsTest;

	public static Map<String, IDESupport> ideSupporter = new HashMap<String, IDESupport>();
	public static Map<String, IScriptRunner> scriptRunner = new HashMap<String, IScriptRunner>();
	private static List<String> supportedRunner = new ArrayList<String>();
  public static boolean systemRedirected = false;

	public static Map<String, String> EndingTypes = new HashMap<String, String>();
	public static Map<String, String> TypeEndings = new HashMap<String, String>();
	public static String CPYTHON = "text/python";
	public static String CRUBY = "text/ruby";
	public static String CPLAIN = "text/plain";
	public static String EPYTHON = "py";
	public static String ERUBY = "rb";
	public static String EPLAIN = "txt";
	public static String RPYTHON = "jython";
	public static String RRUBY = "jruby";
	public static String RDEFAULT = "NotDefined";
	public static String EDEFAULT = EPYTHON;
	public static String TypeCommentToken = "---SikuliX---";
	public static String TypeCommentDefault = "# This script uses %s " + TypeCommentToken + "\n";

  private static boolean isRunningInteractive = false;

  public static void initScriptingSupport() {
    if (scriptRunner.isEmpty()) {
      EndingTypes.put("py", CPYTHON);
      EndingTypes.put("rb", CRUBY);
      EndingTypes.put("txt", CPLAIN);
      for (String k : EndingTypes.keySet()) {
        TypeEndings.put(EndingTypes.get(k), k);
      }
      ServiceLoader<IDESupport> sloader = ServiceLoader.load(IDESupport.class);
      Iterator<IDESupport> supIterator = sloader.iterator();
      while (supIterator.hasNext()) {
        IDESupport current = supIterator.next();
        try {
          for (String ending : current.getEndings()) {
            ideSupporter.put(ending, current);
          }
        } catch (Exception ex) {
        }
      }
      ServiceLoader<IScriptRunner> rloader = ServiceLoader.load(IScriptRunner.class);
      Iterator<IScriptRunner> rIterator = rloader.iterator();
      while (rIterator.hasNext()) {
        IScriptRunner current = rIterator.next();
        String name = current.getName();
        if (name != null && !name.startsWith("Not")) {
          scriptRunner.put(name, current);
        }
      }
    }
    if (scriptRunner.isEmpty()) {
      Debug.error("Settings: No scripting support available. Rerun Setup!");
      Sikulix.popup("No scripting support available. Rerun Setup!", "SikuliX - Fatal Error!");
      System.exit(1);
    } else {
      RDEFAULT = (String) scriptRunner.keySet().toArray()[0];
      EDEFAULT = scriptRunner.get(RDEFAULT).getFileEndings()[0];
      for (IScriptRunner r : scriptRunner.values()) {
        for (String e : r.getFileEndings()) {
          if (!supportedRunner.contains(EndingTypes.get(e))) {
            supportedRunner.add(EndingTypes.get(e));
          }
        }
      }
    }
  }

  public static boolean hasTypeRunner(String type) {
    return supportedRunner.contains(type);
  }

  public static void runningInteractive() {
    isRunningInteractive = true;
  }

  public static boolean getRunningInteractive() {
    return isRunningInteractive;
  }

  private static boolean isRunningScript = false;

  public static void runscript(String[] args) {

    if (isRunningScript) {
      System.out.println("[error] SikuliScript: can only run one at a time!");
      return;
    }

    isRunningScript = true;

    initScriptingSupport();

//    Sikulix.displaySplash(args);

    if (args != null && args.length > 1 && args[0].startsWith("-testSetup")) {
      runner = getScriptRunner(args[1], null, args);
      if (runner == null) {
        args[0] = null;
      } else {
        String[] stmts = new String[0];
        if (args.length > 2) {
          stmts = new String[args.length - 2];
          for (int i = 0; i < stmts.length; i++) {
            stmts[i] = args[i+2];
          }
        }
        if (0 != runner.runScript(null, null, stmts, null)) {
          args[0] = null;
        }
      }
      isRunningScript = false;
      return;
    }

    CommandArgs cmdArgs = new CommandArgs("SCRIPT");
    CommandLine cmdLine = cmdArgs.getCommandLine(CommandArgs.scanArgs(args));
    String cmdValue;

    if (cmdLine == null || cmdLine.getOptions().length == 0) {
      log(-1, "Did not find any valid option on command line!");
      cmdArgs.printHelp();
      System.exit(1);
    }

    if (cmdLine.hasOption(CommandArgsEnum.HELP.shortname())) {
      cmdArgs.printHelp();
      if (runner != null) {
        System.out.println(runner.getCommandLineHelp());
      }
      System.exit(1);
    }

    if (cmdLine.hasOption(CommandArgsEnum.LOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.LOGFILE.longname());
      if (!Debug.setLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.USERLOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.USERLOGFILE.longname());
      if (!Debug.setUserLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.DEBUG.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      if (cmdValue == null) {
        Debug.setDebugLevel(3);
        Settings.LogTime = true;
        if (!Debug.isLogToFile()) {
          Debug.setLogFile("");
        }
      } else {
        Debug.setDebugLevel(cmdValue);
      }
    }

    Settings.setArgs(cmdArgs.getUserArgs(), cmdArgs.getSikuliArgs());
    log(lvl, "CmdOrg: " + System.getenv("SIKULI_COMMAND"));
    Settings.showJavaInfo();
    Settings.printArgs();

//TODO    if (cmdLine.hasOption(CommandArgsEnum.IMAGEPATH.shortname())) {
    if (false) {
//      imagePath = getScriptRunner(cmdLine.getOptionValue(CommandArgsEnum.IMAGEPATH.longname()), null, args);
    } else {
      imagePath = null;
    }

    // select script runner and/or start interactive session
    // option is overloaded - might specify runner for -r/-t
    if (cmdLine.hasOption(CommandArgsEnum.INTERACTIVE.shortname())) {
      System.out.println(String.format(
              "SikuliX Package Build: %s %s", Settings.getVersionShort(), Settings.SikuliVersionBuild));
      int exitCode = 0;
      if (runner == null) {
        String givenRunnerName = cmdLine.getOptionValue(CommandArgsEnum.INTERACTIVE.longname());
        if (givenRunnerName == null) {
          runner = getScriptRunner(RDEFAULT, null, args);
        } else {
          runner = getScriptRunner(givenRunnerName, null, args);
          if (runner == null) {
            System.exit(1);
          }
        }
      }
      if (!cmdLine.hasOption(CommandArgsEnum.RUN.shortname())
              && !cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
        exitCode = runner.runInteractive(cmdArgs.getUserArgs());
        runner.close();
        Sikulix.endNormal(exitCode);
      }
    }

    String givenScriptName = null;
    runAsTest = false;

    if (cmdLine.hasOption(CommandArgsEnum.LOAD.shortname())) {
      String loadScript = FileManager.slashify(cmdLine.getOptionValue(CommandArgsEnum.LOAD.longname()),false);
      log(lvl, "requested to run: " + loadScript);
      givenScriptName = loadScript;
    }

    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      givenScriptName = cmdLine.getOptionValue(CommandArgsEnum.RUN.longname());
    } else if (cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
      givenScriptName = cmdLine.getOptionValue(CommandArgsEnum.TEST.longname());
      log(-1, "Command line option -t: not yet supported! %s", Arrays.asList(args).toString());
      runAsTest = true;
    }

    if (givenScriptName != null) {
      if (givenScriptName.endsWith(".skl")) {
        givenScriptName = FileManager.unzipSKL(givenScriptName);
        if (givenScriptName == null) {
          log(-1, me + "not possible to make .skl runnable!");
          System.exit(1);
        }
      }
      log(lvl, "givenScriptName: " + givenScriptName);
      File sf = new File(givenScriptName);
      File script = getScriptFile(sf, ScriptRunner.runner, args);
      if (script == null) {
        System.exit(1);
      }
      runner = getRunner();
      if (imagePath == null) {
        imagePath = FileManager.resolveImagePath(script);
      }
      Sikulix.callImagePathSetBundlePath(imagePath.getAbsolutePath());
      log(lvl, "Trying to run script: " + script.getAbsolutePath());
      int exitCode = runAsTest
              ? runner.runTest(script, imagePath, cmdArgs.getUserArgs(), null)
              : runner.runScript(script, imagePath, cmdArgs.getUserArgs(), null);
      runner.close();
      Sikulix.endNormal(exitCode);
    } else {
      log(-1, "Nothing to do according to the given commandline options!");
      cmdArgs.printHelp();
      if (runner != null) {
        System.out.println(runner.getCommandLineHelp());
      }
      System.exit(1);
    }
  }

  /**
   * Prints the interactive help from the ScriptRunner.
   */
  public static void shelp() {
    System.out.println(runner.getInteractiveHelp());
  }

  /**
   * Finds a ScriptRunner implementation to execute the script.
   *
   * @param name Name of the ScriptRunner, might be null (then type is used)
   * @param ending fileending of script to run
   * @return first ScriptRunner with matching name or file ending, null if none found
   */
  public static IScriptRunner getScriptRunner(String name, String ending, String[] args) {
    runner = null;
    ServiceLoader<IScriptRunner> loader = ServiceLoader.load(IScriptRunner.class);
    Iterator<IScriptRunner> scriptRunnerIterator = loader.iterator();
    while (scriptRunnerIterator.hasNext()) {
      IScriptRunner currentRunner = scriptRunnerIterator.next();
      if (currentRunner.getName() == null || currentRunner.getName().startsWith("Not")) {
        continue;
      }
      if ((name != null && currentRunner.getName().toLowerCase().equals(name.toLowerCase())) || (ending != null && currentRunner.hasFileEnding(ending) != null)) {
        runner = currentRunner;
        runner.init(args);
        break;
      }
    }
    if (name != null && runner == null) {
      if (args != null && args.length == 1 && "convertSrcToHtml".equals(args[0])) {
        return null;
      }
      if (name != null) {
        log(-1, "Fatal error 121: Could not load script runner with name: %s", name);
        Sikulix.terminate(121);
      } else if (ending != null) {
        log(-1, "Fatal error 120: Could not load script runner for ending: %s", ending);
        Sikulix.terminate(120);
      } else {
        log(-1, "Fatal error 122: While loading script runner with name=%s and ending= %s", name, ending);
        Sikulix.terminate(122);
      }
    }
    return runner;
  }

  public static IScriptRunner setRunner(IScriptRunner _runner) {
    runner = _runner;
    return runner;
  }

  public static IScriptRunner getRunner() {
    return runner;
  }
  
  public static boolean doSomethingSpecial(String action, Object[] args) {
    if (runner == null) {
      return false;
    }   
    return runner.doSomethingSpecial(action, args);
  }

  /**
   * Retrieves the actual script file<br> - from a folder script.sikuli<br>
   * - from a folder script (no extension) (script.sikuli is used, if exists)<br> - from a file
   * script.skl or script.zip (after unzipping to temp)<br> - from a jar script.jar (after
   * preparing as extension)<br>
   *
   * @param scriptName one of the above.
   * @param runner a valid runner if any
   * @param args special use
   * @return The file containing the actual script.
   */
  public static File getScriptFile(File scriptName, IScriptRunner runner, String[] args) {
    if (scriptName == null) {
      return null;
    }
    String script;
    String scriptType;
    File scriptFile = null;
    if (scriptName.getPath().contains("..")) {
      log(-1, "Sorry, scriptnames with double-dot path elements are not supported: %s", scriptName.getPath());
      if (CommandArgs.isIDE()) {
        return null;
      }
      Sikulix.terminate(0);
    }
    int pos = scriptName.getName().lastIndexOf(".");
    if (pos == -1) {
      script = scriptName.getName();
      scriptType = "sikuli";
      scriptName = new File(scriptName.getAbsolutePath() + ".sikuli");
    } else {
      script = scriptName.getName().substring(0, pos);
      scriptType = scriptName.getName().substring(pos + 1);
    }
    if (!scriptName.exists()) {
      log(-1, "Not a valid Sikuli script: " + scriptName.getAbsolutePath());
      if (CommandArgs.isIDE()) {
        return null;
      }
      Sikulix.terminate(0);
    }
    if ("skl".equals(scriptType) || "zip".equals(scriptType)) {
      //TODO unzip to temp and run from there
      return null; // until ready
    }
    if ("sikuli".equals(scriptType)) {
      if (runner == null) {
        File[] content = scriptName.listFiles(new FileFilterScript(script + "."));
        if (content == null || content.length == 0) {
          log(-1, "Script %s \n has no script file %s.xxx", scriptName, script);
          if (args == null) {
            return null;
          } else {
            Sikulix.terminate(0);
          }
        }
        String[] supported = new String[]{"py", "rb"};
        String runType = "py";
        for (File f : content) {
          for (String suffix : supported) {
            if (!f.getName().endsWith("." + suffix)) {
              continue;
            }
            scriptFile = f;
            runType = suffix;
            break;
          }
          if (scriptFile != null) {
            break;
          }
        }
        runner = ScriptRunner.getScriptRunner(null, runType, args);
        if (runner == null) {
          scriptFile = null;
        }
      }
      if (scriptFile == null && runner != null) {
        scriptFile = (new File(scriptName, script + "." + runner.getFileEndings()[0])).getAbsoluteFile();
        if (!scriptFile.exists() || scriptFile.isDirectory()) {
          scriptFile = new File(scriptName, script);
          if (!scriptFile.exists() || scriptFile.isDirectory()) {
            log(-1, "No runnable script found in %s", scriptFile.getAbsolutePath());
            return null;
          }
        }
      }
    }
    if ("jar".equals(scriptType)) {
      //TODO try to load and run as extension
      return null; // until ready
    }
    return scriptFile;
  }

  public static boolean transferScript(String src, String dest) {
    log(lvl, "transferScript: %s\nto: %s", src, dest);
    FileManager.FileFilter filter = new FileManager.FileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().endsWith(".html")) {
          return false;
        } else if (entry.getName().endsWith(".$py.class")) {
          return false;
        } else {
          for (String ending : EndingTypes.keySet()) {
            if (entry.getName().endsWith("." + ending)) {
              return false;
            }
          }
        }
        return true;
      }
    };
    try {
      FileManager.xcopy(src, dest, filter);
    } catch (IOException ex) {
      log(-1, "transferScript: IOError: %s", ex.getMessage(), src, dest);
      return false;
    }
    log(lvl, "transferScript: completed");
    return true;
  }

  private static class FileFilterScript implements FilenameFilter {
    private String _check;
    public FileFilterScript(String check) {
      _check = check;
    }
    @Override
    public boolean accept(File dir, String fileName) {
      return fileName.startsWith(_check);
    }
  }
}


