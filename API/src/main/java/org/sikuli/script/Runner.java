package org.sikuli.script;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.cli.CommandLine;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;

public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;
  static final RunTime runTime = RunTime.get();

  public static Map<String, String> EndingTypes = new HashMap<String, String>();
  public static Map<String, String> typeEndings = new HashMap<String, String>();
  public static String ERUBY = "rb";
  public static String EPYTHON = "py";
  public static String EJSCRIPT = "js";
  public static String EPLAIN = "txt";
  public static String EDEFAULT = EPYTHON;
  public static String CPYTHON = "text/python";
  public static String CRUBY = "text/ruby";
  public static String CJSCRIPT = "text/javascript";
  public static String CPLAIN = "text/plain";
  public static String RPYTHON = "jython";
  public static String RRUBY = "jruby";
  public static String RJSCRIPT = "JavaScript";
  public static String RDEFAULT = "NotDefined";

  private static String[] runScripts = null;
  private static String[] testScripts = null;
  private static int lastReturnCode = 0;

  private static String beforeJSjava8 = "load(\"nashorn:mozilla_compat.js\");";
  private static String beforeJS =
          "importPackage(Packages.org.sikuli.script); " +
          "importClass(Packages.org.sikuli.basics.Debug); " +
          "importClass(Packages.org.sikuli.basics.Settings);";

  static {
      EndingTypes.put(EPYTHON, CPYTHON);
      EndingTypes.put(ERUBY, CRUBY);
      EndingTypes.put(EJSCRIPT, CJSCRIPT);
      EndingTypes.put(EPLAIN, CPLAIN);
      for (String k : EndingTypes.keySet()) {
        typeEndings.put(EndingTypes.get(k), k);
      }
  }

  static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  public static String[] evalArgs(String[] args) {
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

    runTime.setArgs(cmdArgs.getUserArgs(), cmdArgs.getSikuliArgs());
    log(lvl, "CmdOrg: " + System.getenv("SIKULI_COMMAND"));
    runTime.printArgs();

    // select script runner and/or start interactive session
    // option is overloaded - might specify runner for -r/-t
    if (cmdLine.hasOption(CommandArgsEnum.INTERACTIVE.shortname())) {
      if (!cmdLine.hasOption(CommandArgsEnum.RUN.shortname())
              && !cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
        runTime.interactiveRunner = cmdLine.getOptionValue(CommandArgsEnum.INTERACTIVE.longname());
        runTime.runningInteractive = true;
        return null;
      }
    }

    String[] runScripts = null;
    runTime.runningTests = false;
    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.RUN.longname());
    } else if (cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.TEST.longname());
      log(-1, "Command line option -t: not yet supported! %s", Arrays.asList(args).toString());
      runTime.runningTests = true;
//TODO run a script as unittest with HTMLTestRunner
      System.exit(1);
    }
    return runScripts;
  }

  public static int run(String givenName) {
    return run(givenName, new String[0]);
  }

  public static int run(String givenName, String[] args) {
    String savePath = ImagePath.getBundlePath();
    int retVal = new RunBox(givenName, args, false).run();
    ImagePath.setBundlePath(savePath);
    return retVal;
  }

  public static int runTest(String givenName) {
    return runTest(givenName, new String[0]);
  }

  public static int runTest(String givenName, String[] args) {
    String savePath = ImagePath.getBundlePath();
    int retVal = new RunBox(givenName, args, true).run();
    ImagePath.setBundlePath(savePath);
    return retVal;
  }

  public static int getLastReturnCode() {
    return lastReturnCode;
  }

	static int runScripts(String[] args) {
		runScripts = Runner.evalArgs(args);
		int exitCode = 0;
		if (runScripts != null && runScripts.length > 0) {
			if (runScripts[0].contains("RunnerServer")) {
				RunnerServer.start(null);
			} else {
				boolean runAsTest = runTime.runningTests;
				for (String givenScriptName : runScripts) {
					if (lastReturnCode == -1) {
						log(lvl, "Exit code -1: Terminating multi-script-run");
						break;
					}
					RunBox rb = new RunBox(givenScriptName, runTime.getSikuliArgs(), runAsTest);
					exitCode = rb.run();
					lastReturnCode = exitCode;
				}
			}
		}
		return exitCode;
	}

  public static File getScriptFile(File fScriptFolder) {
    if (fScriptFolder == null) {
      return null;
    }
    File[] content = FileManager.getScriptFile(fScriptFolder);
    if (null == content) {
      return null;
    }
    File fScript = null;
    for (File aFile : content) {
      for (String suffix : Runner.EndingTypes.keySet()) {
        if (!aFile.getName().endsWith("." + suffix)) {
          continue;
        }
        fScript = aFile;
        break;
      }
      if (fScript != null) {
        break;
      }
    }
    return fScript;
  }

  static ScriptEngine jsRunner = null;

  public static int runjs(File fScript, URL script, String scriptName, String[] args) {
    String initSikulix = "";
    if (jsRunner == null) {
			jsRunner = initjs();
			initSikulix = prologjs(initSikulix);
    }
    try {
			if (null != fScript) {
				File innerBundle = new File(fScript.getParentFile(), scriptName + ".sikuli");
				if (innerBundle.exists()) {
					ImagePath.setBundlePath(innerBundle.getCanonicalPath());
				} else {
					ImagePath.setBundlePath(fScript.getParent());
				}
			} else {
				ImagePath.addHTTP(script.toExternalForm());
			}
			if (!initSikulix.isEmpty()) {
				initSikulix = prologjs(initSikulix);
				jsRunner.eval(initSikulix);
				initSikulix = "";
			}
			if (null != fScript) {
				jsRunner.eval(new java.io.FileReader(fScript));
			} else {
				jsRunner.eval(scriptName);
			}
    } catch (Exception ex) {
      log(-1, "not possible:\n%s", ex);
    }
    return 0;
  }

	public static ScriptEngine initjs() {
		ScriptEngineManager jsFactory = new ScriptEngineManager();
		ScriptEngine jsr = jsFactory.getEngineByName("JavaScript");
		if (jsr != null) {
			log(lvl, "ScriptingEngine started: JavaScript (ending .js)");
		} else {
			runTime.terminate(1, "ScriptingEngine for JavaScript not available");
		}
		return jsr;
	}

	public static String prologjs(String before) {
		String after = before;
		if (after.isEmpty()) {
			if (runTime.isJava8()) {
				after += beforeJSjava8;
			}
			after += beforeJS;
		} else {
			String commands = runTime.extractResourceToString("JavaScript", "commands.js", "");
			if (commands != null) {
				after += commands;
			}
		}
		return after;
	}

  public static Object[] runBoxInit(String givenName, File scriptProject, URL uScriptProject) {
    String gitScripts = "https://github.com/RaiMan/SikuliX-2014/tree/master/TestScripts/"; 
    String givenScriptHost = "";
    String givenScriptFolder = "";
    String givenScriptName = "";
    String givenScriptScript = "";
    String givenScriptType = "sikuli";
    String givenScriptScriptType = RDEFAULT;
    Boolean givenScriptExists = true;
    URL uGivenScript = null;
    URL uGivenScriptFile = null;
    givenScriptName = givenName;
    String[] parts = null;
		int isNet;
    if (givenName.toLowerCase().startsWith("git*")) {
      givenName = gitScripts + givenName.substring(4);
    }
    if (-1 < (isNet = givenName.indexOf("://"))) {
			String payload = givenName.substring(isNet+3);
			payload = payload.replaceFirst("/", "#");
			parts = payload.split("#");
			if (parts.length > 1 && !parts[1].isEmpty()) {
				givenScriptHost = parts[0];
        givenScriptName = new File(parts[1]).getName();
        String fpFolder = new File(parts[1]).getParent();
        if (null != fpFolder && !fpFolder.isEmpty()) {
          givenScriptFolder = FileManager.slashify(fpFolder, true);
          if (givenScriptFolder.startsWith("/")) {
            givenScriptFolder = givenScriptFolder.substring(1);
          }
        }
      }
			String scriptLocation = givenName;
			givenScriptExists = false;
      if (givenScriptHost.contains("github.com")) {
        givenScriptHost = "https://raw.githubusercontent.com/";
        givenScriptFolder = givenScriptFolder.replace("tree/", "");
        if (givenScriptName.endsWith(".zip")) {
          scriptLocation = givenScriptHost + givenScriptFolder + givenScriptName;          
          if (0 < FileManager.isUrlUseabel(scriptLocation)) {
            runTime.terminate(1, ".zip from git not yet supported\n%s", scriptLocation);
          }
        } else {
          givenScriptScript = givenScriptName + "/" + givenScriptName + ".js";
          givenScriptScriptType = RJSCRIPT;
          scriptLocation = givenScriptHost + givenScriptFolder + givenScriptScript;
          String content = "";
          if (0 < FileManager.isUrlUseabel(scriptLocation)) {
            content = FileManager.downloadURLtoString(scriptLocation);
          } else {
            givenScriptExists = false;
          }
          if (!content.isEmpty()) {
            givenScriptType = "JS-NET";
            givenScriptScript = content;
            givenScriptExists = true;
            try {
              uGivenScript = new URL(givenScriptHost + givenScriptFolder + givenScriptName);
            } catch (Exception ex) {
              givenScriptExists = false;
            }
          }
        }
      }
			if (!givenScriptExists) {
				runTime.terminate(1, "given script location not supported or not valid:\n%s", scriptLocation);
			}
    } else {
			boolean sameFolder = givenScriptName.startsWith("./");
			if (sameFolder) {
				givenScriptName = givenScriptName.substring(2);
			}
			if (givenScriptName.startsWith("JS*")) {
				givenScriptName = new File(runTime.fSxProjectTestScriptsJS, givenScriptName.substring(3)).getPath();
			}
			String scriptName = new File(givenScriptName).getName();
			if (scriptName.contains(".")) {
				parts = scriptName.split("\\.");
				givenScriptScript = parts[0];
				givenScriptType = parts[1];
			} else {
				givenScriptScript = scriptName;
			}
			if (sameFolder && scriptProject != null) {
				givenScriptName = new File(scriptProject, givenScriptName).getPath();
			} else if (sameFolder && uScriptProject != null) {
				givenScriptHost = uScriptProject.getHost();
				givenScriptFolder = uScriptProject.getPath().substring(1);
			} else if (scriptProject == null && givenScriptHost.isEmpty()) {
				String fpParent = new File(givenScriptName).getParent();
				if (fpParent == null || fpParent.isEmpty()) {
					scriptProject = null;
				} else {
					scriptProject = new File(givenScriptName).getParentFile();
				}
			}
		}
    Object[] vars = new Object[]{givenScriptHost, givenScriptFolder, givenScriptName, givenScriptScript, givenScriptType,
			givenScriptScriptType, uGivenScript, uGivenScriptFile, givenScriptExists, scriptProject, uScriptProject};
    return vars;
  }

  static class RunBox {

//    static File scriptProject = null;
//    static URL uScriptProject = null;

    RunTime runTime = RunTime.get();
    boolean asTest = false;
    String[] args = new String[0];

    String givenScriptHost = "";
    String givenScriptFolder = "";
    String givenScriptName = "";
    String givenScriptScript = "";
    String givenScriptType = "sikuli";
    String givenScriptScriptType = RDEFAULT;
		URL uGivenScript = null;
    URL uGivenScriptFile = null;
    boolean givenScriptExists = true;

    RunBox(String givenName, String[] givenArgs, boolean isTest) {
      Object[] vars = Runner.runBoxInit(givenName, RunTime.scriptProject, RunTime.uScriptProject);
      givenScriptHost = (String) vars[0];
      givenScriptFolder = (String) vars[1];
      givenScriptName = (String) vars[2];
      givenScriptScript = (String) vars[3];
      givenScriptType = (String) vars[4];
      givenScriptScriptType = (String) vars[5];
      uGivenScript = (URL) vars[6];
      uGivenScriptFile = (URL) vars[7];
      givenScriptExists = (Boolean) vars[8];
      RunTime.scriptProject = (File) vars[9];
      RunTime.uScriptProject = (URL) vars[10];
      args = givenArgs;
      asTest = isTest;
    }

    int run() {
      int exitCode = 0;
      log(lvl, "givenScriptName:\n%s", givenScriptName);
      if (-1 == FileManager.slashify(givenScriptName, false).indexOf("/") && RunTime.scriptProject != null) {
        givenScriptName = new File(RunTime.scriptProject, givenScriptName).getPath();
      }
      if (givenScriptName.endsWith(".skl")) {
        log(-1, "RunBox.run: .skl scripts not yet supported.");
        return -9999;
//        givenScriptName = FileManager.unzipSKL(givenScriptName);
//        if (givenScriptName == null) {
//          log(-1, "not possible to make .skl runnable");
//          return -9999;
//        }
      }
			if ("JS-NET".equals(givenScriptType)) {
				exitCode = Runner.runjs(null, uGivenScript, givenScriptScript, args);
			} else {
				File fScript = Runner.getScriptFile(new File(givenScriptName));
				if (fScript == null) {
					return -9999;
				}
				if (!fScript.getName().endsWith(EJSCRIPT)) {
					log(-1, "only supported currently: %s\n%s", RJSCRIPT, fScript);
					return -9999;
				}
				fScript = new File(FileManager.normalizeAbsolute(fScript.getPath(), true));
				if (null == RunTime.scriptProject) {
					RunTime.scriptProject = fScript.getParentFile().getParentFile();
				}
				log(lvl, "Trying to run script:\n%s", fScript);
				exitCode = Runner.runjs(fScript, null, givenScriptScript, args);
			}
      return exitCode;
    }
  }
}
