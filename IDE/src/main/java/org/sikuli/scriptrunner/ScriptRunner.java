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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.apache.commons.cli.CommandLine;
import org.sikuli.ide.CommandArgs;
import org.sikuli.ide.CommandArgsEnum;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Sikulix;

public class ScriptRunner {

	private static final String me = "ScriptRunner: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static Boolean runAsTest;

	public static Map<String, IScriptRunner> scriptRunner = new HashMap<String, IScriptRunner>();
	private static Map<String, IScriptRunner> supportedRunner = new HashMap<String, IScriptRunner>();
  public static boolean systemRedirected = false;

	public static Map<String, String> EndingTypes = new HashMap<String, String>();
	public static Map<String, String> typeEndings = new HashMap<String, String>();
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
  
  private static String[] runScripts = null;
  private static String[] testScripts = null;
  
  private static boolean isReady = false;


  public static void initScriptingSupport() {
    if (isReady) {
      return;
    }
		log(lvl, "initScriptingSupport: enter");
    if (scriptRunner.isEmpty()) {
      EndingTypes.put("py", CPYTHON);
      EndingTypes.put("rb", CRUBY);
      EndingTypes.put("txt", CPLAIN);
      for (String k : EndingTypes.keySet()) {
        typeEndings.put(EndingTypes.get(k), k);
      }
      ServiceLoader<IScriptRunner> rloader = ServiceLoader.load(IScriptRunner.class);
      Iterator<IScriptRunner> rIterator = rloader.iterator();
      while (rIterator.hasNext()) {
				IScriptRunner current = null;
				try {
					current = rIterator.next();
				} catch (ServiceConfigurationError e) {
					log(lvl, "initScriptingSupport: warning: %s", e.getMessage());
					continue;
				}
        String name = current.getName();
        if (name != null && !name.startsWith("Not")) {
          scriptRunner.put(name, current);
          current.init(null);
					log(lvl, "initScriptingSupport: added: %s", name);
        }
      }
    }
    if (scriptRunner.isEmpty()) {
      Debug.error("Settings: No scripting support available. Rerun Setup!");
      String em = "Terminating: No scripting support available. Rerun Setup!";
      log(-1, em);
      if (Settings.isRunningIDE) {
        Sikulix.popError(em, "IDE has problems ...");
      }
      System.exit(1);
    } else {
      RDEFAULT = (String) scriptRunner.keySet().toArray()[0];
      EDEFAULT = scriptRunner.get(RDEFAULT).getFileEndings()[0];
      for (IScriptRunner r : scriptRunner.values()) {
        for (String e : r.getFileEndings()) {
          if (!supportedRunner.containsKey(EndingTypes.get(e))) {
            supportedRunner.put(EndingTypes.get(e), r);
          }
        }
      }
    }
		log(lvl, "initScriptingSupport: exit with defaultrunner: %s (%s)", RDEFAULT, EDEFAULT);
    isReady = true;
  }

  public static boolean hasTypeRunner(String type) {
    return supportedRunner.containsKey(type);
  }

  public static void runningInteractive() {
    isRunningInteractive = true;
  }

  public static boolean getRunningInteractive() {
    return isRunningInteractive;
  }

  private static boolean isRunningScript = false;

  /**
   * INTERNAL USE: run scripts when sikulix.jar is used on commandline with args -r, -t or -i<br>
   * If you want to use it the args content must be according to the Sikulix command line parameter rules<br>
   * use run(script, args) to run one script from a script or Java program
   * @param args parameters given on commandline 
   */
  public static void runscript(String[] args) {

    if (isRunningScript) {
      System.out.println("[error] SikuliScript: can only run one at a time!");
      return;
    }

    isRunningScript = true;
    initScriptingSupport();
    IScriptRunner currentRunner = null;

    if (args != null && args.length > 1 && args[0].startsWith("-testSetup")) {
      currentRunner = getRunner(null, args[1]);
      if (currentRunner == null) {
        args[0] = null;
      } else {
        String[] stmts = new String[0];
        if (args.length > 2) {
          stmts = new String[args.length - 2];
          for (int i = 0; i < stmts.length; i++) {
            stmts[i] = args[i+2];
          }
        }
        if (0 != currentRunner.runScript(null, null, stmts, null)) {
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
      if (currentRunner != null) {
        System.out.println(currentRunner.getCommandLineHelp());
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

    // select script runner and/or start interactive session
    // option is overloaded - might specify runner for -r/-t
    if (cmdLine.hasOption(CommandArgsEnum.INTERACTIVE.shortname())) {
      System.out.println(String.format(
              "SikuliX Package Build: %s %s", Settings.getVersionShort(), Settings.SikuliVersionBuild));
      int exitCode = 0;
      if (currentRunner == null) {
        String givenRunnerName = cmdLine.getOptionValue(CommandArgsEnum.INTERACTIVE.longname());
        if (givenRunnerName == null) {
          currentRunner = getRunner(null, RDEFAULT);
        } else {
          currentRunner = getRunner(null, givenRunnerName);
          if (currentRunner == null) {
            System.exit(1);
          }
        }
      }
      if (!cmdLine.hasOption(CommandArgsEnum.RUN.shortname())
              && !cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
        exitCode = currentRunner.runInteractive(cmdArgs.getUserArgs());
        currentRunner.close();
        Sikulix.endNormal(exitCode);
      }
    }

    runAsTest = false;

    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.RUN.longname());
    } else if (cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.TEST.longname());
      log(-1, "Command line option -t: not yet supported! %s", Arrays.asList(args).toString());
      runAsTest = true;
//TODO run a script as unittest with HTMLTestRunner
      System.exit(1);
    }

    if (runScripts != null && runScripts.length > 0) {
      int exitCode = 0;
      for (String givenScriptName : runScripts) {
        exitCode = new RunBox(runAsTest).executeScript(givenScriptName, cmdArgs.getUserArgs());
        if ( exitCode == -9999) {
          continue;
        }
      }
      System.exit(exitCode);
    } else {
      log(-1, "Nothing to do with the given commandline options!");
      cmdArgs.printHelp();
      System.exit(1);
    }
  }
    
  /**
   * run a script at scriptPath (.sikuli or .skl)
   * @param scriptPath absolute or relative to working folder
   * @param args parameter given to the script
   * @return exit code
   */
  public static int run(String scriptPath, String[] args) {
    runAsTest = false;
    initScriptingSupport();
    return new RunBox(false).executeScript(scriptPath, args);
  }

  /**
   * run a script at scriptPath (.sikuli or .skl)
   * @param scriptPath absolute or relative to working folder
   * @return exit code
   */
  public static int run(String scriptPath) {
    return run(scriptPath, new String[0]);
  }

  public static IScriptRunner getRunner(String script, String type) {
    IScriptRunner currentRunner = null;
    String ending = null;
    if (script != null) {
      for (String suffix : EndingTypes.keySet()) {
        if (script.endsWith(suffix)) {
          ending = suffix;
          break;
        }        
      }
    } else if (type != null) {
      currentRunner = scriptRunner.get(type);
      if (currentRunner != null) {
        return currentRunner;
      }
      ending = typeEndings.get(type);
      if (ending == null) {
        if (EndingTypes.containsKey(type)) {
          ending = type;
        }
      }
    }
    if (ending != null) {
      for (IScriptRunner r : scriptRunner.values()) {
        if (r.hasFileEnding(ending) != null) {
          currentRunner = r;
          break;
        }
      }
    }
    return currentRunner;
  }

  /**
   * Retrieves the actual script file<br> - from a folder script.sikuli<br>
   * - from a folder script (no extension) (script.sikuli is used, if exists)<br> - from a file
   * script.skl or script.zip (after unzipping to temp)<br> - from a jar script.jar (after
   * preparing as extension)<br>
   *
   * @param scriptProject one of the above.
   * @param args special use
   * @return The file containing the actual script.
   */
  public static File getScriptFile(File scriptProject, String[] args) {
    if (scriptProject == null) {
      return null;
    }
    String script;
    String scriptType;
    File scriptFile = null;
    if (scriptProject.getPath().contains("..")) {
      log(-1, "Sorry, script paths with double-dot path elements are not supported: %s", scriptProject.getPath());
      return null;
    }
    int pos = scriptProject.getName().lastIndexOf(".");
    if (pos == -1) {
      script = scriptProject.getName();
      scriptType = "sikuli";
      scriptProject = new File(scriptProject.getAbsolutePath() + ".sikuli");
    } else {
      script = scriptProject.getName().substring(0, pos);
      scriptType = scriptProject.getName().substring(pos + 1);
    }
    if (!scriptProject.exists()) {
      log(-1, "Not a valid Sikuli script: " + scriptProject.getAbsolutePath());
      return null;
    }
    if ("skl".equals(scriptType) || "zip".equals(scriptType)) {
      String sklPath = FileManager.unzipSKL(scriptProject.getAbsolutePath());
      if (sklPath == null) {
        log(-1, me + "not possible to make .skl runnable!");
        return null;
      }
    } else if ("sikuli".equals(scriptType)) {
      File[] content = scriptProject.listFiles(new FileFilterScript(script + "."));
      if (content == null || content.length == 0) {
        log(-1, "Script %s \n has no script file %s.xxx", scriptProject, script);
        return null;
      }
      String runType = null;
      for (File f : content) {
        for (String suffix : EndingTypes.keySet()) {
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
      if (ScriptRunner.getRunner(null, runType) == null) {
        scriptFile = null;
      }
      if (scriptFile == null && runType == null) {
        scriptFile = new File(scriptProject, script + "." + EDEFAULT).getAbsoluteFile();
        if (!scriptFile.exists() || scriptFile.isDirectory()) {
          scriptFile = new File(scriptProject, script);
          if (!scriptFile.exists() || scriptFile.isDirectory()) {
            log(-1, "No runnable script found in %s", scriptFile.getAbsolutePath());
            return null;
          }
        }
      }
    } else if ("jar".equals(scriptType)) {
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
  
  private static class RunBox {
    
    boolean asTest = false;
    
    private RunBox(boolean isTest) {
      asTest = isTest;
    }

    private int executeScript(String givenScriptName, String[] args) {
      int exitCode;
      if (givenScriptName.endsWith(".skl")) {
        givenScriptName = FileManager.unzipSKL(givenScriptName);
        if (givenScriptName == null) {
          log(-1, me + "not possible to make .skl runnable!");
          return -9999;
        }
      }
      log(lvl, "givenScriptName: " + givenScriptName);
      File sf = new File(givenScriptName);
      File script = getScriptFile(sf, args);
      if (script == null) {
        return -9999;
      }
      IScriptRunner currentRunner = getRunner(script.getName(), null);
      ImagePath.setBundlePath(script.getParent());
      log(lvl, "Trying to run script: " + script.getAbsolutePath());
      if (asTest) {
        exitCode = currentRunner.runTest(script, null, args, null);
      } else {
        exitCode = currentRunner.runScript(script, null, args, null);
      }
      currentRunner.close();
      return exitCode;
    }
  }
}


