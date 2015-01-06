/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2013
 */
package org.sikuli.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.SplashFrame;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.ResourceLoader;
import org.sikuli.basics.Settings;
import org.sikuli.script.Sikulix;

public class RunSetup {

  private static String downloadedFiles;
  private static boolean runningUpdate = false;
  private static boolean isUpdateSetup = false;
  private static boolean runningfromJar = true;
  private static URL runningJarURL;
  private static boolean noSetup = false;
  private static boolean noSetupSilent = false;
  private static boolean backUpExists = false;
  private static String workDir;
  private static String uhome;
  private static String logfile;
  private static String version = Settings.getVersionShort();
//TODO wrong if version number parts have more than one digit
  private static String minorversion = Settings.getVersionShort().substring(0, 5);
  private static String majorversion = Settings.getVersionShort().substring(0, 3);
  private static String updateVersion;
  private static String downloadSetup;
  private static String downloadMavenSnapshot = "";
  private static String downloadMavenRelease = "";
  private static String downloadIDE = version + "-1.jar";
  private static String downloadAPI = version + "-2.jar";
  private static String downloadRServer = version + "-3.jar";
  private static String downloadJython = new File(Settings.SikuliJythonMaven).getName();
  private static String downloadJRuby = new File(Settings.SikuliJRubyMaven).getName();
  private static String downloadJRubyAddOns = version + "-6.jar";
  private static String downloadMacAppSuffix = "-9.jar";
  private static String downloadMacApp = minorversion + downloadMacAppSuffix;
  private static String downloadTessSuffix = "-8.jar";
  private static String downloadTess = minorversion + downloadTessSuffix;
  private static String localAPI = "sikulixapi.jar";
  private static String localIDE = "sikulix.jar";
  private static String localMacApp = "sikulixmacapp.jar";
  private static String localMacAppIDE = "SikuliX-IDE.app/Contents/sikulix.jar";
  private static String folderMacApp = "SikuliX-IDE.app";
  private static String folderMacAppContent = folderMacApp + "/Contents";
  private static String setupName = "sikulixsetup-" + version;
  private static String localSetup = setupName + ".jar";
  private static String localUpdate = "sikulixupdate";
  private static String localTess = "sikulixtessdata.jar";
  private static String localRServer = "sikulixremoterobot.jar";
  private static String localJython = "sikulixjython.jar";
  private static String localJRuby = "sikulixjruby.jar";
  private static String localJRubyAddOns = "sikulixjrubyaddons.jar";
  private static String runsikulix = "runsikulix";
  private static String localLogfile;
  private static SetUpSelect winSU;
  private static JFrame winSetup;
  private static boolean getIDE, getJython, getAPI;
  private static boolean getRServer = false;
  private static boolean forAllSystems = false;
  private static boolean getTess = false;
  private static boolean getJRuby = false;
  private static boolean getJRubyAddOns = false;
  private static String localJar;
  private static boolean test = false;
  private static boolean isUpdate = false;
  private static boolean isBeta = false;
  private static String runningJar;
  private static List<String> options = new ArrayList<String>();
  private static JFrame splash = null;
  private static String me = "RunSetup";
  private static String mem = "...";
  private static int lvl = 2;
  private static String msg;
  private static boolean shouldPackLibs = true;
  private static long start;
  private static boolean runningSetup = false;
  private static boolean generallyDoUpdate = false;
  private static String timestampBuilt = Settings.SikuliVersionBuild;
  private static boolean logToFile = true;
  private static boolean hasOptions = false;
  private static int logLevel = 3;
  private static boolean forSystemWin = false;
  private static boolean forSystemMac = false;
  private static boolean forSystemLux = false;
  private static String libsMac = "sikulixlibsmac";
  private static String libsWin = "sikulixlibswin";
  private static String libsLux = "sikulixlibslux";
  private static String apiJarName = "sikulixapi";
  private static File folderLibs;
  private static String linuxDistro = "*** testing Linux ***";
  private static String osarch;
//TODO set true to test on Mac
  private static boolean isLinux = false;

  // -MF $externals/$fn.o.d -o $externals/$fn.o $src/Vision/$fn
  private static boolean buildComplete = true;
  private static String buildCppMods = "-MF %s.o.d -o %s.o %s";
  private static String buildFolderSrc = "Build/Source";
  private static String buildFolderInclude = "Build/Include";
  private static String[] buildSrcFiles =
          new String[]{"cvgui.cpp", "finder.cpp",
                       "pyramid-template-matcher.cpp", "sikuli-debug.cpp", "tessocr.cpp",
                       "vision.cpp", "visionJAVA_wrap.cxx"};
  private static String buildCppFix = "g++ -c -O3 -fPIC -MMD -MP %s "; // $includeParm
  private static String buildFolder = "Build";
  private static String buildFolderStuff = "Build/Stuff";
  private static String buildCompile = "";
  private static String buildLink = "g++ -shared -s -fPIC -dynamic ";
  private static boolean libsProvided = false;
  private static String libVision = "libVisionProxy.so";
  private static String libGrabKey = "libJXGrabKey.so";
  private static boolean visionProvided = false;
  private static boolean grabKeyProvided = false;
  private static boolean shouldExport = false;
  private static String[] libsFileList = new String[]{null, null};
  private static String[] libsFilePrefix = new String[]{null, null};
  private static String libOpenCVcore = "";
  private static String libOpenCVimgproc = "";
  private static String libOpenCVhighgui = "";
  private static String libTesseract = "";
  private static boolean libSearched = false;
  private static boolean checkSuccess = true;
  private static boolean opencvAvail = true;
  private static boolean tessAvail = true;
  private static String cmdError = "*** error ***";
  private static boolean shouldBuildVision = false;
  private static boolean notests = false;
  private static String currentlib;

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + mem + ": " + message, args);
  }

  private static void log0(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + message, args);
  }

  private static void log1(int level, String message, Object... args) {
    String sout;
    String prefix = level < 0 ? "error" : "debug";
    if (args.length != 0) {
      sout = String.format("[" + prefix + "] " + message, args);
    } else {
      sout = "[" + prefix + "] " + message;
    }
    Debug.logx(level, me + ": " + message, args);
    if (logToFile) {
      System.out.println(sout);
    }
  }
//</editor-fold>

  public static void main(String[] args) throws IOException {
    mem = "main";

    PreferencesUser prefs = PreferencesUser.getInstance();
    boolean prefsHaveProxy = false;

    CodeSource codeSrc = RunSetup.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      runningJarURL = codeSrc.getLocation();
    } else {
      log(-1, "Fatal Error 201: Not possible to accessjar file for RunSetup.class");
      Sikulix.terminate(201);
    }

    if (Settings.SikuliVersionBetaN > 0 && Settings.SikuliVersionBetaN < 99) {
      updateVersion = String.format("%d.%d.%d-Beta%d",
              Settings.SikuliVersionMajor, Settings.SikuliVersionMinor, Settings.SikuliVersionSub,
              1 + Settings.SikuliVersionBetaN);
    } else if (Settings.SikuliVersionBetaN < 1) {
      updateVersion = String.format("%d.%d.%d",
              Settings.SikuliVersionMajor, Settings.SikuliVersionMinor,
              1 + Settings.SikuliVersionSub);
    } else {
      updateVersion = String.format("%d.%d.%d",
              Settings.SikuliVersionMajor, 1 + Settings.SikuliVersionMinor, 0);
    }

    options.addAll(Arrays.asList(args));

    //<editor-fold defaultstate="collapsed" desc="options return version">
    if (args.length > 0 && "build".equals(args[0])) {
      System.out.println(Settings.SikuliVersionBuild);
      System.exit(0);
    }

    if (args.length > 0 && "pversion".equals(args[0])) {
      System.out.println(Settings.SikuliProjectVersion);
      System.exit(0);
    }

    if (args.length > 0 && "uversion".equals(args[0])) {
      System.out.println(Settings.SikuliProjectVersionUsed);
      System.exit(0);
    }

    if (args.length > 0 && "version".equals(args[0])) {
      System.out.println(Settings.getVersionShort());
      System.exit(0);
    }

    if (args.length > 0 && "minorversion".equals(args[0])) {
      System.out.println(minorversion);
      System.exit(0);
    }

    if (args.length > 0 && "majorversion".equals(args[0])) {
      System.out.println(majorversion);
      System.exit(0);
    }

    if (args.length > 0 && "updateversion".equals(args[0])) {
      System.out.println(updateVersion);
      System.exit(0);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="other options">
    if (args.length > 0 && "test".equals(args[0])) {
      test = true;
      options.remove(0);
      if (options.isEmpty()) {
        getIDE = true;
        getJython = true;
        getAPI = true;
      } else {
        if ("jruby".equals(options.get(0))) {
          options.remove(0);
          getIDE = true;
          getJRuby = true;
        }
      }
    }

    if (options.size() > 0 && "noSetup".equals(options.get(0))) {
      noSetup = true;
      options.remove(0);
    }

    if (options.size() > 0 && "update".equals(options.get(0))) {
      runningUpdate = true;
      options.remove(0);
    }

    if (options.size() > 0 && "updateSetup".equals(options.get(0))) {
      isUpdateSetup = true;
      options.remove(0);
    }

    if (options.size() > 1 && "log".equals(options.get(0))) {
      options.remove(0);
      String log = options.remove(0);
      try {
        logLevel = Integer.decode(log);
      } catch (NumberFormatException numberFormatException) {
      }
    }

    //</editor-fold>

//TODO add parameter for proxy settings, linux options
    if (args.length > 0 && "options".equals(args[0])) {
      options.remove(0);
      if (!options.isEmpty()) {
        for (String val : options) {
          if (val.contains("1.1")) {
            hasOptions = true;
            getIDE = true;
            getJython = true;
          } else if (val.contains("1.2")) {
            hasOptions = true;
            getIDE = true;
            getJRuby = true;
          } else if (val.contains("1.3")) {
            hasOptions = true;
            getIDE = true;
            getJRuby = true;
            getJRubyAddOns = true;
          } else if ("2".equals(val)) {
            hasOptions = true;
            getAPI = true;
          } else if ("3".equals(val)) {
            hasOptions = true;
            getTess = true;
          } else if ("4".equals(val)) {
            hasOptions = true;
            forAllSystems = true;
          } else if (val.contains("4.1")) {
            hasOptions = true;
            forSystemWin = true;
          } else if (val.contains("4.2")) {
            hasOptions = true;
            forSystemMac = true;
          } else if (val.contains("4.3")) {
            hasOptions = true;
            forSystemLux = true;
          } else if ("5".equals(val)) {
            hasOptions = true;
            getRServer = true;
          } else if (val.toLowerCase().startsWith("lib")) {
            hasOptions = true;
            libsProvided = true;
          } else if (val.toLowerCase().startsWith("buildv")) {
            hasOptions = true;
            shouldBuildVision = true;
          } else if (val.toLowerCase().startsWith("not")) {
            hasOptions = true;
            notests = true;
          }
        }
        options.clear();
        if (hasOptions) {
          test = true;
        }
      }
    }

    runningJar = FileManager.getJarName(RunSetup.class);

    if (runningJar.isEmpty()) {
      popError("error accessing jar - terminating");
      System.exit(999);
    }
    if (runningJar.startsWith("sikulixupdate")) {
      runningUpdate = true;
    }

    if (runningUpdate) {
      localLogfile = "SikuliX-" + version + "-UpdateLog.txt";
    } else {
      localLogfile = "SikuliX-" + version + "-SetupLog.txt";
    }

    //<editor-fold defaultstate="collapsed" desc="option makeJar">
    String baseDir = null;
    if (options.size() > 0 && options.get(0).equals("makeJar")) {
      options.remove(0);
      String todo, jarName, folder;
      while (options.size() > 0) {
        todo = options.get(0);
        options.remove(0);
				//***
        // unpack or pack a jar to/from a folder
        //***
        if (todo.equals("unpack") || todo.equals("pack")) {
          if (options.size() < 1) {
            log0(-1, todo + ": invalid options! need a jar");
            System.exit(0);
          }
          jarName = options.get(0);
          options.remove(0);
          if (jarName.endsWith(".jar")) {
            if (options.size() < 1) {
              log0(-1, todo + ": invalid options! need a folder");
              System.exit(0);
            }
            folder = options.get(0);
            options.remove(0);
          } else {
            folder = jarName;
            jarName += ".jar";
          }
          if (options.size() > 0) {
            baseDir = options.get(0);
            options.remove(0);
            if (!new File(baseDir).isAbsolute()) {
              baseDir = new File(workDir, baseDir).getAbsolutePath();
            }
          }
          if (!new File(folder).isAbsolute()) {
            if (baseDir == null) {
              baseDir = workDir;
            }
            folder = new File(baseDir, folder).getAbsolutePath();
          }
          if (!new File(jarName).isAbsolute()) {
            if (baseDir == null) {
              baseDir = workDir;
            }
            jarName = new File(baseDir, jarName).getAbsolutePath();
          }
          if (todo.equals("unpack")) {
            log0(3, "requested to unpack %s \nto %s", jarName, folder);
            FileManager.unpackJar(jarName, folder, true, false, null);
          } else {
            String jarBack = jarName.substring(0, jarName.length() - 4) + "-backup.jar";
            try {
              FileManager.xcopy(jarName, jarBack);
            } catch (IOException ex) {
              log(-1, "could not create backUp - terminating");
              System.exit(0);
            }
            log0(3, "requested to pack %s \nfrom %s\nbackup to: %s", jarName, folder, jarBack);
            FileManager.packJar(folder, jarName, "");
          }
          log0(3, "completed!");
          continue;
					//***
          // build a jar by combining other jars (optionally filtered) and/or folders
          //***
        } else if (todo.equals("buildJar")) {
          // build jar arg0
          if (options.size() < 2) {
            log0(-1, "buildJar: invalid options!");
            System.exit(0);
          }
          jarName = options.get(0);
          options.remove(0);
          folder = options.get(0);
          options.remove(0);
          log0(3, "requested to build %s to %s", jarName, folder);
          // action
          log0(3, "completed!");
          continue;
        } else {
          log0(-1, "makejar: invalid option: " + todo);
          System.exit(0);
        }
      }
      System.exit(0);
    }
    //</editor-fold>

    if (options.size() > 0) {
      popError("invalid command line options - terminating");
      System.exit(999);
    }

    //<editor-fold defaultstate="collapsed" desc="general preps">
    Settings.runningSetup = true;
    Settings.LogTime = true;
    Debug.setDebugLevel(logLevel);

    uhome = System.getProperty("user.home");
    runningJar = FileManager.getJarPath(RunSetup.class);
    workDir = new File(runningJar).getParent();

    osarch = Settings.JavaArch;
    osarch = osarch.contains("64") ? "64" : "32";
    if (Settings.isLinux()) {
      String result = ResourceLoader.get().runcmd("lsb_release -i -r -s");
      linuxDistro = result.replaceAll("\n", " ").trim();
      if (linuxDistro.contains("*** error ***")) {
        linuxDistro = "***UNKNOWN***";
      }
      log1(lvl, "LinuxDistro: %s (%s-Bit)", linuxDistro, osarch);
      isLinux = true;
    }

    if (!runningJar.endsWith(".jar") || runningJar.endsWith("-plain.jar")) {
      if (!hasOptions) {
        if (noSetup) {
          log(lvl, "creating Setup folder - not running setup");
        } else {
          log(lvl, "have to create Setup folder before running setup");
        }
        if (!createSetupFolder("")) {
          log(-1, "createSetupFolder: did not work- terminating");
          System.exit(1);
        }
//        workDir = FileManager.slashify(workDir, true) + "Setup";
        if (noSetup) {
          System.exit(0);
        }
        logToFile = false;
      } else {
        workDir += "/Setup";
        new File(workDir).mkdirs();
      }
      Settings.runningSetupInValidContext = true;
      Settings.runningSetupInContext = workDir;
    }

    if (logToFile) {
      logfile = (new File(workDir, localLogfile)).getAbsolutePath();
      if (!Debug.setLogFile(logfile)) {
        popError(workDir + "\n... folder we are running in must be user writeable! \n"
                + "please correct the problem and start again.");
        System.exit(0);
      }
    }

    if (args.length > 0) {
      log1(lvl, "... starting with: " + Sikulix.arrayToString(args));
    } else {
      log1(lvl, "... starting with no args given");
    }

    if (logToFile && !hasOptions) {
      Settings.getStatus();
    }
//</editor-fold>

    log1(lvl, "Setup in: %s\nusing: %s", workDir, (runningJar.contains("classes") ? "Development Project" : runningJar));
    log1(lvl, "SikuliX Setup Build: %s %s", Settings.getVersionShort(), Settings.SikuliVersionBuild);

    File localJarIDE = new File(workDir, localIDE);
    File localJarAPI = new File(workDir, localAPI);
    File localMacFolder = new File(workDir, folderMacApp);

    folderLibs = new File(workDir, "libs");

    //TODO Windows 8 HKLM/SOFTWARE/JavaSoft add Prefs ????
    boolean success;
    if (isLinux && !libsProvided) {
      visionProvided = new File(folderLibs, libVision).exists();
      grabKeyProvided = new File(folderLibs, libGrabKey).exists();
      if (visionProvided || grabKeyProvided) {
        success = popAsk(String.format("Found a libs folder at\n%s\n"
                + "Click YES to use the contained libs "
                + "for setup (be sure they are useable).\n"
                + "Click NO to make a clean setup (libs are deleted).", folderLibs));
        if (success) {
          libsProvided = true;
        } else {
          FileManager.deleteFileOrFolder(folderLibs.getAbsolutePath());
        }
      }
    }

    //<editor-fold defaultstate="collapsed" desc="checking update/beta">
    if (!hasOptions) {
      if (!runningUpdate && !isUpdateSetup) {
        String uVersion = "";
        String msgFooter = "You have " + Settings.getVersion()
                + "\nClick YES, if you want to install ..."
                + "\ncurrent stuff will be saved to BackUp."
                + "\n... Click NO to skip ...";
        if (localJarIDE.exists() || localJarAPI.exists() || localMacFolder.exists()) {
          int avail = -1;
          boolean someUpdate = false;
          String ask1 = "You have " + Settings.getVersion()
                  + "\nClick YES if you want to run setup again\n"
                  + "This will download fresh versions of the selected stuff.\n"
                  + "Your current stuff will be saved to folder BackUp.\n\n"
                  + "If you cancel the setup later or it is not successful\n"
                  + "the saved stuff will be restored from folder BackUp\n\n";
          if (!popAsk(ask1)) {
            userTerminated("Do not run setup again");
          }
          //<editor-fold defaultstate="collapsed" desc="update - currently deactivated">
  //				String ask2 = "Click YES to get info on updates or betas.\n"
  //								+ "or click NO to terminate setup now.";
  //				if (generallyDoUpdate && popAsk(ask2)) {
  //					splash = showSplash("Checking for update or beta versions! (you have " + version + ")",
  //									"please wait - may take some seconds ...");
  //					AutoUpdater au = new AutoUpdater();
  //					avail = au.checkUpdate();
  //					closeSplash(splash);
  //					if (avail > 0) {
  //						if (avail == AutoUpdater.BETA || avail == AutoUpdater.SOMEBETA) {
  //							someUpdate = true;
  //							uVersion = au.getBetaVersion();
  //							if (popAsk("Version " + uVersion + " is available\n" + msgFooter)) {
  //								isBeta = true;
  //							}
  //						}
  //						if (avail > AutoUpdater.FINAL) {
  //							avail -= AutoUpdater.SOMEBETA;
  //						}
  //						if (avail > 0 && avail != AutoUpdater.BETA) {
  //							someUpdate = true;
  //							if (popAsk(au.whatUpdate + "\n" + msgFooter)) {
  //								isUpdate = true;
  //								uVersion = au.getVersionNumber();
  //							}
  //						}
  //					}
  //					if (!someUpdate) {
  //						popInfo("No suitable update or beta available");
  //						userTerminated("No suitable update or beta available");
  //					}
  //				}
          //</editor-fold>
          if (!isBeta && !isUpdate) {
            reset(-1);
          } else {
            //<editor-fold defaultstate="collapsed" desc="update - currently deactivated">
            log0(lvl, "%s is available", uVersion);
            if (uVersion.equals(updateVersion)) {
              reset(avail);
              Settings.downloadBaseDir = Settings.downloadBaseDirBase + uVersion.substring(0, 3) + "/";
              downloadSetup = "sikuli-update-" + uVersion + ".jar";
              if (!download(Settings.downloadBaseDir, workDir, downloadSetup,
                      new File(workDir, downloadSetup).getAbsolutePath(), "")) {
                restore(true);
                popError("Download did not complete successfully.\n"
                        + "Check the logfile for possible error causes.\n\n"
                        + "If you think, setup's inline download from Dropbox is blocked somehow on,\n"
                        + "your system, you might download manually (see respective FAQ)\n"
                        + "For other reasons, you might simply try to run setup again.");
                terminate("download not completed successfully");
              }
              popInfo("Now you can run the update process:\n"
                      + "DoubleClick " + "sikuli-update-" + uVersion + ".jar"
                      + "\nin folder " + workDir + "\n\nPlease click OK before proceeding!");
              terminate("");
            } else {
              popError("downloadable update: " + uVersion + "\nexpected update: " + updateVersion
                      + "\n do not match --- terminating --- pls. report");
              terminate("update versions do not match");
            }
            //</editor-fold>
          }
        }
      } else {
        //<editor-fold defaultstate="collapsed" desc="update - currently deactivated">
        log0(lvl, "Update started");
        if (!generallyDoUpdate) {
          terminate("Switched Off: Run update!");
        }
        if (!popAsk("You requested to run an Update now"
                + "\nYES to continue\nNO to terminate")) {
          userTerminated("");
        }
        //</editor-fold>
      }
    } else {
      reset(-1);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="dispatching external setup run - currently not possible (update)">
    if (!isUpdateSetup && !runningSetup) {
//      String[] cmd = null;
//      File fCmd = null;
//      String runSetupOption = "";
//      if (isRunningUpdate()) {
//        runSetupOption = "updateSetup";
//      } else if (!runningSetup) {
//        runSetupOption = "runningSetup";
//      }
//      if (Settings.isWindows()) {
//        log0(lvl, "Extracting runSetup.cmd");
//        String syspath = System.getenv("PATH");
//        for (String p : syspath.split(";")) {
//          log0(lvl, "syspath: " + p);
//        }
//        loader.export("Commands/windows#runSetup.cmd", workDir);
//        fCmd = new File(workDir, "runSetup.cmd");
//        cmd = new String[]{"cmd", "/C", "start", "cmd", "/K", fCmd.getAbsolutePath(), runSetupOption};
//      } else if (runningUpdate) {
//        log0(lvl, "Extracting runSetup");
//        fCmd = new File(workDir, "runSetup");
//        loader.export("Commands/"
//                + (Settings.isMac() ? "mac#runSetup" : "linux#runSetup"), workDir);
//        loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", fCmd.getAbsolutePath()});
//        if (Settings.isMac()) {
//          cmd = new String[]{"/bin/sh", fCmd.getAbsolutePath(), runSetupOption};
//        } else {
//          cmd = new String[]{"/bin/bash", fCmd.getAbsolutePath(), runSetupOption};
//        }
//      }
//      if ((Settings.isWindows() || runningUpdate) && (fCmd == null || !fCmd.exists())) {
//        String msg = "Fatal error 002: runSetup(.cmd) could not be exported to " + workDir;
//        log0(-1, msg);
//        popError(msg);
//        System.exit(2);
//      }
//      if (runningUpdate) {
//        localSetup = "sikuli-setup-" + updateVersion.substring(0, 3) + ".jar";
//        FileManager.deleteFileOrFolder(new File(workDir, localSetup).getAbsolutePath());
//        log0(lvl, "Update: trying to dowload the new sikuli-setup.jar version " + updateVersion.substring(0, 3));
//        downloadSetup = "sikuli-setup-" + updateVersion + ".jar";
//        downloadBaseDir = downloadBaseDirBase + updateVersion.substring(0, 3) + "/";
//        if (!download(downloadBaseDir, workDir, downloadSetup,
//                new File(workDir, localSetup).getAbsolutePath())) {
//          restore();
//          popError("Download did not complete successfully.\n"
//                  + "Check the logfile for possible error causes.\n\n"
//                  + "If you think, setup's inline download from Dropbox is blocked somehow on,\n"
//                  + "your system, you might download manually (see respective FAQ)\n"
//                  + "For other reasons, you might simply try to run setup again.");
//          terminate("download not completed successfully");
//        }
//      }
//      if (cmd != null) {
//        if (runningUpdate && !popAsk("Continue Update after download success?"
//                + "\nYES to continue\nNO to terminate")) {
//          userTerminated("after download success");
//        }
//        log0(lvl, "dispatching external setup run");
//        if (runningfromJar) {
//          loader.doSomethingSpecial("runcmd", cmd);
//          System.exit(0);
//        }
//      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="setup preps and display options">
    String proxyMsg = "";

    if (!test) {
      getIDE = false;
      getJython = false;
      getAPI = false;
    }
    if (!test) {
      if (!isUpdateSetup) {
        winSetup = new JFrame("SikuliX-Setup");
        Border rpb = new LineBorder(Color.YELLOW, 8);
        winSetup.getRootPane().setBorder(rpb);
        Container winCP = winSetup.getContentPane();
        winCP.setLayout(new BorderLayout());
        winSU = new SetUpSelect();
        winCP.add(winSU, BorderLayout.CENTER);
        winSU.option2.setSelected(true);
        winSetup.pack();
        winSetup.setLocationRelativeTo(null);
        winSetup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        winSetup.setVisible(true);

        //setup version basic
        winSU.suVersion.setText(Settings.getVersionShort() + "   (" + Settings.SikuliVersionBuild + ")");

        // running system
        Settings.getOS();
        msg = Settings.osName + " " + Settings.getOSVersion();
        if (isLinux) {
          msg += " (" + linuxDistro + ")";
        }
        winSU.suSystem.setText(msg);
        log1(lvl, "RunningSystem: " + msg);

        // folder running in
        winSU.suFolder.setText(workDir);
        log1(lvl, "parent of jar/classes: %s", workDir);

        // running Java
        String osarch = System.getProperty("os.arch");
        msg = "Java " + Settings.JavaVersion + " (" + Settings.JavaArch + ") " + Settings.JREVersion;
        winSU.suJava.setText(msg);
        log1(lvl, "RunningJava: " + msg);

        String pName = prefs.get("ProxyName", "");
        String pPort = prefs.get("ProxyPort", "");
        if (!pName.isEmpty() && !pPort.isEmpty()) {
          prefsHaveProxy = true;
          winSU.pName.setText(pName);
          winSU.pPort.setText(pPort);
        }

        winSU.addPropertyChangeListener("background", new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent pce) {
            winSetup.setVisible(false);
          }
        });

        while (true) {
          if (winSU.getBackground() == Color.YELLOW) {
            break;
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
          }
        }

        pName = winSU.pName.getText();
        pPort = winSU.pPort.getText();
        if (!pName.isEmpty() && !pPort.isEmpty()) {
          if (FileManager.setProxy(pName, pPort)) {
            log1(lvl, "Requested to run with proxy: %s ", Settings.proxy);
            proxyMsg = "... using proxy: " + Settings.proxy;
          }
        } else if (prefsHaveProxy) {
          prefs.put("ProxyName", "");
          prefs.put("ProxyPort", "");
        }
        Settings.proxyChecked = true;
      }

      File fPrefs = new File(workDir, "SikuliPrefs.txt");
      prefs.exportPrefs(fPrefs.getAbsolutePath());
      BufferedReader pInp = null;
      try {
        pInp = new BufferedReader(new FileReader(fPrefs));
        String line;
        while (null != (line = pInp.readLine())) {
          if (!line.contains("entry")) {
            continue;
          }
          if (logToFile) {
            log(lvl, "Prefs: " + line.trim());
          }
        }
        pInp.close();
      } catch (Exception ex) {
      }
      FileManager.deleteFileOrFolder(fPrefs.getAbsolutePath());

      if (!isUpdateSetup) {
        if (winSU.option1.isSelected()) {
          getIDE = true;
          if (winSU.option2.isSelected()) {
            getJython = true;
          }
          if (winSU.option3.isSelected()) {
            getJRuby = true;
          }
          if (!getJython && !getJRuby) {
            getIDE = false;
          }
        }
        if (winSU.option4.isSelected()) {
          getAPI = true;
        }
        if (winSU.option5.isSelected()) {
          getTess = true;
        }
//        if (winSU.option6.isSelected()) {
//          forAllSystems = true;
//        }
//        if (winSU.option7.isSelected()) {
//          getRServer = true;
//        }

        if (((getTess || forAllSystems) && !(getIDE || getAPI))) {
          popError("You only selected Option 3 !\n"
                  + "This is currently not supported.\n"
                  + "Please start allover again with valid options.\n");
          terminate("");
        }
        msg = "The following file(s) will be downloaded to\n"
                + workDir + "\n";
      } else {
        msg = "The following packages will be updated\n";
        if (Settings.proxy != null) {
          msg += "... using proxy: " + Settings.proxy + "\n";
        }
        if (new File(workDir, localIDE).exists()) {
          getIDE = true;
          msg += "Pack 1: " + localIDE + "\n";
        }
        if (new File(workDir, localAPI).exists()) {
          getAPI = true;
          msg += "Pack 2: " + localAPI + "\n";
        }
        if (new File(workDir, localRServer).exists()) {
          getRServer = true;
          msg += localRServer + "\n";
        }
        if (new File(workDir, localTess).exists()) {
          getTess = true;
          msg += "\n... with Tesseract OCR support\n\n";
        }
        if (popAsk("It cannot be detected, wether your current jars\n"
                + "have been setup for all systems (option 4).\n"
                + "Click YES if you want this option now\n"
                + "Click NO to run normal setup for current system")) {
          forAllSystems = true;
        }
      }
    }

    downloadedFiles = "";
    if (!isUpdateSetup) {
      if (getIDE || getAPI || getRServer) {

        if (!proxyMsg.isEmpty()) {
          msg += proxyMsg + "\n";
        }
        msg += "\n--- Native support libraries for " + Settings.osName + " (sikulixlibs...)\n";
        if (getIDE) {
          downloadedFiles += downloadIDE + " ";
          downloadedFiles += downloadAPI + " ";
          msg += "\n--- Package 1 ---\n" +
                  downloadIDE + " (IDE/Scripting)\n" +
                  downloadAPI + " (Java API)";
          if (getJython) {
            downloadedFiles += downloadJython + " ";
            msg += "\n - with Jython";
          }
          if (getJRuby) {
            downloadedFiles += downloadJRuby + " ";
            msg += "\n - with JRuby";
            if (getJRubyAddOns) {
              downloadedFiles += downloadJRubyAddOns + " ";
              msg += " incl. AddOns";
            }
          }
          msg += "\n";
        }
        if (getAPI) {
          msg += "\n--- Package 2 ---\n" + downloadAPI;
          if (!getIDE) {
            downloadedFiles += downloadAPI + " ";
            msg += " (Java API)";
          } else {
            msg += " (done in package 1)";
          }
        }
        if (getTess || getRServer) {
          if (getIDE || getAPI) {
            msg += "\n";
          }
          msg += "\n--- Additions ---";
          if (getTess) {
            downloadedFiles += "tessdata-eng" + " ";
            msg += "\n" + "tessdata-eng" + " (Tesseract)";
          }
          if (getRServer) {
            downloadedFiles += downloadRServer + " ";
            msg += "\n" + downloadRServer + " (RemoteServer)";
          }
        }
      }
    }

    if (getIDE || getAPI || getRServer) {
      msg += "\n\nOnly click NO, if you want to terminate setup now!\n"
              + "Click YES even if you want to use local copies in Downloads!";
      if (!popAsk(msg)) {
        terminate("");
      }
    } else {
      popError("Nothing selected! You might try again ;-)");
      terminate("");
    }

    // downloading
    ResourceLoader loader = ResourceLoader.get();
    String localTemp = "sikulixtemp.jar";
    String[] jarsList = new String[]{
      null, // ide
      null, // api
      null, // tess
      null, // jython
      null, // jruby
      null, // jruby+
      null, // libwin
      null, // libmac
      null // liblux
    };
    localJar = null;
    File fTargetJar;
    String targetJar;
    boolean downloadOK = true;
    boolean dlOK = true;
    File fDLDir = new File(workDir, "Downloads");
    fDLDir.mkdirs();
    String dlDir = fDLDir.getAbsolutePath();
    if (!forSystemWin && !forSystemMac && !forSystemLux) {
      forSystemLux = isLinux;
      if (! isLinux) {
        forSystemWin = Settings.isWindows();
        forSystemMac = Settings.isMac();
      }
    }
    File libDownloaded;
    if (forSystemWin || forAllSystems) {
      jarsList[6] = new File(workDir, libsWin + ".jar").getAbsolutePath();
      libDownloaded = new File(dlDir, libsWin + "-" + version + ".jar");
      if (!takeAlreadyDownloaded(libDownloaded, libsWin)) {
        downloadOK &= getSikulixJarFromMaven(libsWin, dlDir, null, libsWin);
      } else {
        copyFromDownloads(libDownloaded, libsWin, jarsList[6]);
      }
    }
    if (forSystemMac || forAllSystems) {
      jarsList[7] = new File(workDir, libsMac + ".jar").getAbsolutePath();
      libDownloaded = new File(dlDir, libsMac + "-" + version + ".jar");
      if (!takeAlreadyDownloaded(libDownloaded, libsMac)) {
        downloadOK &= getSikulixJarFromMaven(libsMac, dlDir, null, libsMac);
      } else {
        copyFromDownloads(libDownloaded, libsMac, jarsList[7]);
      }
    }
    if (forSystemLux || forAllSystems) {
      jarsList[8] = new File(workDir, libsLux + ".jar").getAbsolutePath();
      libDownloaded = new File(dlDir, libsLux + "-" + version + ".jar");
      if (!takeAlreadyDownloaded(libDownloaded, libsLux)) {
        downloadOK &= getSikulixJarFromMaven(libsLux, dlDir, null, libsLux);
      } else {
        copyFromDownloads(libDownloaded, libsLux, jarsList[8]);
      }
      // check bundled and/or provided libs
      if (!folderLibs.exists()) {
        folderLibs.mkdirs();
      }
      if (!folderLibs.exists()) {
        log(-1, "Linux: check useability of libs: problems with libs folder\n%s", folderLibs);
      } else {
        String[] libsExport = new String[]{null, null};
        String[] libsCheck = new String[]{null, null};
        if (!new File(folderLibs, libVision).exists()) {
          libsExport[0] = libVision;
          shouldExport = true;
        }
        if (!new File(folderLibs, libGrabKey).exists()) {
          libsExport[1] = libGrabKey;
          shouldExport = true;
        }
        if (shouldExport) {
          for (String exLib : libsExport) {
            if (exLib == null) {
              continue;
            }
            currentlib = exLib;
            FileManager.unpackJar(jarsList[8], folderLibs.getAbsolutePath(),
                    false, true, new FileManager.JarFileFilter() {
              @Override
              public boolean accept(ZipEntry entry, String jarname) {
                if (entry.getName().contains("libs" + osarch + "/" + currentlib)) {
                  return true;
                }
                return false;
              }
            });
          }
        }
        libsCheck[0] = new File(folderLibs, libVision).getAbsolutePath();
        libsCheck[1] = new File(folderLibs, libGrabKey).getAbsolutePath();
        File fLibCheck;
        boolean shouldTerminate = false;
        boolean shouldBuildVisionNow = false;
        for (int i = 0; i < libsCheck.length; i++) {
          fLibCheck = new File(libsCheck[i]);
          if (fLibCheck.exists()) {
            if (!checklibs(fLibCheck)) {
//TODO why? JXGrabKey unresolved: pthread
							if (i == 0) {
								if (libsExport[i] == null) {
									log1(-1, "provided %s might not be useable on this Linux - see log", fLibCheck.getName());
								} else {
									log1(-1, "bundled %s might not be useable on this Linux - see log", fLibCheck.getName());
								}
								shouldBuildVisionNow = true;
							}
            }
          } else {
            log(-1, "check not possible for\n%s", fLibCheck);
          }
        }
        for (String exLib : libsExport) {
          if (exLib == null) {
            libsProvided = true;
            continue;
          }
          FileManager.deleteFileOrFolder(new File(folderLibs, exLib).getAbsolutePath());
        }
				if (shouldBuildVisionNow) {
					log1(-1, "A bundled lib could not be checked or does not work.");
					if (!popAsk("The bundled/provided libVisionProxy.so might not work."
									+ "\nShould we try to build it now?"
									+ "\nClick YES to try a build"
									+ "\nClick NO to terminate and correct the problems.")) {
						shouldTerminate = true;
						shouldBuildVisionNow = false;
					}
				}
        if (shouldBuildVisionNow || (hasOptions && shouldBuildVision)) {
					log1(lvl, "Trying to build libVisionProxy.so");
					shouldTerminate |= !buildVision(jarsList[8]);
        }
        if (! libsProvided) {
          FileManager.deleteFileOrFolder(folderLibs.getAbsolutePath());
        }
        if (shouldTerminate) {
					terminate("Correct the problems with the bundled/provided libs and try again");
        }
      }
    }

    if (getIDE || getAPI) {
      localJar = new File(workDir, localAPI).getAbsolutePath();
      downloadOK &= download(Settings.downloadBaseDir, dlDir, downloadAPI, localJar, "Java API");
    }
    if (getIDE) {
      localJar = new File(workDir, localIDE).getAbsolutePath();
      dlOK = download(Settings.downloadBaseDir, dlDir, downloadIDE, localJar, "IDE/Scripting");
      downloadOK &= dlOK;
    }
    if (getJython) {
      targetJar = new File(workDir, localJython).getAbsolutePath();
      if (Settings.isJava6()) {
        log1(lvl, "running on Java 6: need to use Jython 2.5 - which is downloaded");
        downloadOK &= getJarFromMaven(Settings.SikuliJythonMaven25, dlDir, targetJar, "Jython");
      } else {
        downloadOK &= getJarFromMaven(Settings.SikuliJythonMaven, dlDir, targetJar, "Jython");
      }
    }
    if (getJRuby) {
      targetJar = new File(workDir, localJRuby).getAbsolutePath();
      downloadOK &= getJarFromMaven(Settings.SikuliJRubyMaven, dlDir, targetJar, "JRuby");
      if (downloadOK && getJRubyAddOns) {
        targetJar = new File(workDir, localJRubyAddOns).getAbsolutePath();
        downloadOK &= download(Settings.downloadBaseDir, dlDir, downloadJRubyAddOns, targetJar, "JRubyAddOns");
      }
    }
    if (getTess) {
      String langTess = "eng";
      targetJar = new File(workDir, localTess).getAbsolutePath();
      String xTess = Settings.tessData.get(langTess);
      String[] xTessNames = xTess.split("/");
      String xTessName = xTessNames[xTessNames.length - 1];
      downloadOK &= download(xTess, dlDir, null, "nocopy", null);
      log(lvl, "trying to extract from: %s", xTessName);
      Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
      archiver.extract(new File(dlDir, "tesseract-ocr-3.02.eng.tar.gz"), new File(dlDir));
      File fTess = new File(dlDir, "tesseract-ocr/tessdata");
      if (!fTess.exists()) {
        log1(-1, "Download: tessdata: version: eng - did not work");
        downloadOK = false;
      } else {
        File fTessData = new File(dlDir, "tessdata-" + langTess);
        log(lvl, "preparing the tessdata stuff in %s", fTessData.getAbsolutePath());
        fTessData.mkdirs();
        FileManager.xcopy(fTess.getAbsolutePath(), fTessData.getAbsolutePath());
        FileManager.deleteFileOrFolder(fTess.getParent());
        loader.export(runningJarURL, "tessdata#", fTessData.getAbsolutePath());
        log(lvl, "finally preparing %s", localTess);
        fTargetJar = (new File(workDir, localTemp));
        targetJar = fTargetJar.getAbsolutePath();
        String tessJar = new File(workDir, localTess).getAbsolutePath();
        downloadOK &= FileManager.buildJar(targetJar, new String[]{},
                new String[]{fTessData.getAbsolutePath()},
                new String[]{"META-INF/libs/tessdata"}, null);
        downloadOK &= handleTempAfter(targetJar, tessJar);
        FileManager.deleteFileOrFolder(fTessData.getAbsolutePath());
      }
    }
    if (getRServer) {
      targetJar = new File(workDir, localRServer).getAbsolutePath();
      downloadOK = download(Settings.downloadBaseDir, dlDir, downloadRServer, targetJar, "RemoteServer");
      downloadOK &= dlOK;
    }
    if (!downloadedFiles.isEmpty()) {
      log1(lvl, "Download ended");
      log1(lvl, "Downloads for selected options:\n" + downloadedFiles);
      log1(lvl, "Download page: " + Settings.downloadBaseDirWeb);
    }
    if (!downloadOK) {
      popError("Some of the downloads did not complete successfully.\n"
              + "Check the logfile for possible error causes.\n\n"
              + "If you think, setup's inline download is blocked somehow on\n"
              + "your system, you might download the appropriate raw packages manually\n"
              + "into the folder Downloads in the setup folder and run setup again.\n\n"
              + "download page: " + Settings.downloadBaseDirWeb + "\n"
              + "files to download (information is in the setup log file too)\n"
              + downloadedFiles
              + "\n\nBe aware: The raw packages are not useable without being processed by setup!\n\n"
              + "For other reasons, you might simply try to run setup again.");
      terminate("download not completed successfully");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="create jars and add needed stuff">
    if (!getIDE && !getAPI) {
      log1(lvl, "Nothing else to do");
      System.exit(0);
    }

    if (isLinux) {
      if (libsProvided) {
        shouldPackLibs = false;
      }
      if (!shouldPackLibs) {
        libsFileList[0] = new File(folderLibs, libVision).getAbsolutePath();
        libsFileList[1] = new File(folderLibs, libGrabKey).getAbsolutePath();
        for (int i = 0; i < 2; i++) {
          if (!new File(libsFileList[i]).exists()) {
            libsFileList[i] = null;
          }
        }
        String libPrefix = "META-INF/libs/linux/libs" + osarch;
        log(lvl, "Provided libs will be stored at %s", libPrefix);
        libsFilePrefix[0] = libPrefix;
        libsFilePrefix[1] = libPrefix;
      }
    }

    success = true;
    FileManager.JarFileFilter libsFilter = new FileManager.JarFileFilter() {
      @Override
      public boolean accept(ZipEntry entry, String jarname) {
        if (forSystemWin) {
          if (entry.getName().startsWith("META-INF/libs/mac")
                  || entry.getName().startsWith("META-INF/libs/linux")
                  || entry.getName().startsWith("jxgrabkey")) {
            return false;
          }
        } else if (forSystemMac) {
          if (entry.getName().startsWith("META-INF/libs/windows")
                  || entry.getName().startsWith("META-INF/libs/linux")
                  || entry.getName().startsWith("com.melloware.jintellitype")
                  || entry.getName().startsWith("jxgrabkey")) {
            return false;
          }
        } else if (forSystemLux) {
          if (entry.getName().startsWith("META-INF/libs/windows")
                  || entry.getName().startsWith("META-INF/libs/mac")
                  || entry.getName().startsWith("com.melloware.jintellitype")) {
            return false;
          }
        }
        if (forSystemLux || forAllSystems) {
          if (!shouldPackLibs && entry.getName().contains(libVision)
                  && entry.getName().contains("libs" + osarch)) {
            if (new File(folderLibs, libVision).exists()) {
              log(lvl, "Found provided lib: %s (libs%s)", libVision, osarch);
              return false;
            } else {
              return true;
            }
          }
          if (!shouldPackLibs && entry.getName().contains(libGrabKey)
                  && entry.getName().contains("libs" + osarch)) {
            if (new File(folderLibs, libGrabKey).exists()) {
              log(lvl, "Found provided lib: %s (libs%s)", libGrabKey, osarch);
              return false;
            } else {
              return true;
            }
          }
        }
        return true;
      }
    };

    splash = showSplash("Now adding needed stuff to selected jars.", "please wait - may take some seconds ...");

    jarsList[1] = (new File(workDir, localAPI)).getAbsolutePath();

    if (getTess) {
      jarsList[2] = (new File(workDir, localTess)).getAbsolutePath();
    }

    if (success && getAPI) {
      log1(lvl, "adding needed stuff to sikulixapi.jar");
      localJar = (new File(workDir, localAPI)).getAbsolutePath();
      targetJar = (new File(workDir, localTemp)).getAbsolutePath();
      success &= FileManager.buildJar(targetJar, jarsList,
              libsFileList, libsFilePrefix, libsFilter);
      success &= handleTempAfter(targetJar, localJar);
    }

    if (success && getIDE) {
      log1(lvl, "adding needed stuff to sikulix.jar");
      localJar = (new File(workDir, localIDE)).getAbsolutePath();
      jarsList[0] = localJar;
      if (getJython) {
        jarsList[3] = (new File(workDir, localJython)).getAbsolutePath();
      }
      if (getJRuby) {
        jarsList[4] = (new File(workDir, localJRuby)).getAbsolutePath();
        if (getJRubyAddOns) {
          jarsList[5] = (new File(workDir, localJRubyAddOns)).getAbsolutePath();
        }
      }
      targetJar = (new File(workDir, localTemp)).getAbsolutePath();
      success &= FileManager.buildJar(
              targetJar, jarsList, libsFileList, libsFilePrefix, libsFilter);
      success &= handleTempAfter(targetJar, localJar);
    }

    if (getIDE && Settings.isMac()) {
      log1(lvl, "making the Mac application Sikulix.app");
    }

    for (int i = (getAPI ? 2 : 1); i < jarsList.length; i++) {
      if (jarsList[i] != null) {
        new File(jarsList[i]).delete();
      }
    }
    closeSplash(splash);

    if (success && getIDE) {
      log1(lvl, "exporting commandfiles");
      splash = showSplash("Now exporting commandfiles.", "please wait - may take some seconds ...");
      if (Settings.isWindows()) {
        loader.export(runningJarURL, "Commands/windows#" + runsikulix + ".cmd", workDir);
      } else if (Settings.isMac()) {
        loader.export(runningJarURL, "Commands/mac#" + runsikulix, workDir);
        ResourceLoader.get().runcmd(new String[]{"chmod", "ugo+x", new File(workDir, runsikulix).getAbsolutePath()});
      } else if (isLinux) {
        loader.export(runningJarURL, "Commands/linux#" + runsikulix, workDir);
        ResourceLoader.get().runcmd(new String[]{"chmod", "ugo+x", new File(workDir, runsikulix).getAbsolutePath()});
        ResourceLoader.get().runcmd(new String[]{"chmod", "ugo+x", new File(workDir, localIDE).getAbsolutePath()});
      }
      closeSplash(splash);
    }
    if (!success) {
      popError("Bad things happened trying to add native stuff to selected jars --- terminating!");
      terminate("Adding stuff to jars did not work");
    }
    restore(true); //to get back the stuff that was not changed
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="api test">
    if (!notests && getAPI) {
      log1(lvl, "Trying to run functional test: JAVA-API");
      splash = showSplash("Trying to run functional test(s)", "Java-API: org.sikuli.script.Sikulix.testSetup()");
      if (!Sikulix.addToClasspath(localJarAPI.getAbsolutePath())) {
        closeSplash(splash);
        log0(-1, "Java-API test: ");
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + (logfile == null ? "printout" : logfile));
        terminate("Functional test JAVA-API did not work", 1);
      }
      URL apiJarUrl = loader.setApiJarURL(FileManager.slashify(localJarAPI.getAbsolutePath(), false));
      try {
        log0(lvl, "trying to run org.sikuli.script.Sikulix.testSetup()");
        if (getTess) {
          loader.export(apiJarUrl, "META-INF#libs/tessdata/", workDir);
          getTess = false;
        }
        loader.export(apiJarUrl, "#Lib/", workDir);
        Class sysclass = URLClassLoader.class;
        Class SikuliCL = sysclass.forName("org.sikuli.script.Sikulix");
        log0(lvl, "class found: " + SikuliCL.toString());
        Method method = null;
        if (test) {
          method = SikuliCL.getDeclaredMethod("testSetupSilent", new Class[0]);
        } else {
          method = SikuliCL.getDeclaredMethod("testSetup", new Class[0]);
        }
        log0(lvl, "getMethod: " + method.toString());
        method.setAccessible(true);
        closeSplash(splash);
        log0(lvl, "invoke: " + method.toString());
        Object ret = method.invoke(null, new Object[0]);
        if (!(Boolean) ret) {
          throw new Exception("testSetup returned false");
        }
      } catch (Exception ex) {
        closeSplash(splash);
        log0(-1, ex.getMessage());
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + (logfile == null ? "printout" : logfile));
        terminate("Functional test Java-API did not work", 1);
      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ide test">
    if (!notests && getIDE) {
      if (!Sikulix.addToClasspath(localJarIDE.getAbsolutePath())) {
        closeSplash(splash);
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + (logfile == null ? "printout" : logfile));
        terminate("Functional test IDE did not work", 1);
      }
      URL ideJarUrl = loader.setApiJarURL(FileManager.slashify(localJarIDE.getAbsolutePath(), false));
      if (getTess) {
        loader.export(ideJarUrl, "META-INF#libs/tessdata/", workDir);
      }
      String testMethod;
      if (getJython) {
        if (test) {
          testMethod = "print \"testSetup: Jython: success\"";
        } else {
          testMethod = "Sikulix.testSetup(\"Jython Scripting\")";
        }
        log1(lvl, "Jython: Trying to run functional test: running script statements via SikuliScript");
        splash = showSplash("Jython Scripting: Trying to run functional test",
                "Running script statements via SikuliScript");
        try {
          String testargs[] = new String[]{"-testSetup", "jython", testMethod};
          closeSplash(splash);
          runScriptTest(testargs);
          if (null == testargs[0]) {
            throw new Exception("testSetup ran with problems");
          }
        } catch (Exception ex) {
          closeSplash(splash);
          log0(-1, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test Jython did not work", 1);
        }
      }
      if (getJRuby) {
        if (test) {
          testMethod = "print \"testSetup: JRuby: success\"";
        } else {
          testMethod = "Sikulix.testSetup(\"JRuby Scripting\")";
        }
        log1(lvl, "JRuby: Trying to run functional test: running script statements via SikuliScript");
        splash = showSplash("JRuby Scripting: Trying to run functional test",
                "Running script statements via SikuliScript");
        try {
          String testargs[] = new String[]{"-testSetup", "jruby", testMethod};
          closeSplash(splash);
          runScriptTest(testargs);
          if (null == testargs[0]) {
            throw new Exception("testSetup ran with problems");
          }
        } catch (Exception ex) {
          closeSplash(splash);
          log0(-1, "content of returned error's (%s) message:\n%s", ex, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test JRuby did not work", 1);
        }
      }
    }
    //</editor-fold>

    splash = showSplash("Setup seems to have ended successfully!",
            "Detailed information see: " + (logfile == null ? "printout" : logfile));
    start += 2000;

    closeSplash(splash);

    log0(lvl,
            "... SikuliX Setup seems to have ended successfully ;-)");

    System.exit(0);
  }

  private static boolean checklibs(File lib) {
    String cmdRet;
    String[] retLines;

    if (!libSearched) {
      log1(lvl, "checking: availability of OpenCV and Tesseract");
      log1(lvl, "checking: scanning loader cache (ldconfig -p)");
      cmdRet = ResourceLoader.get().runcmd("ldconfig -p");
      if (cmdRet.contains(cmdError)) {
        log1(-1, "checking: ldconfig returns error:\ns", cmdRet);
        checkSuccess = false;
      } else {
        String[] libs = cmdRet.split("\n");
        for (String libx : libs) {
          libx = libx.trim();
          if (!libx.startsWith("lib")) {
            continue;
          }
          if (libx.startsWith("libopencv_core.so.")) {
            libOpenCVcore = libx.split("=>")[1].trim();
          } else if (libx.startsWith("libopencv_highgui.so.")) {
            libOpenCVhighgui = libx.split("=>")[1].trim();
          } else if (libx.startsWith("libopencv_imgproc.so.")) {
            libOpenCVimgproc = libx.split("=>")[1].trim();
          } else if (libx.startsWith("libtesseract.so.")) {
            libTesseract = libx.split("=>")[1].trim();
          }
        }
        if (libOpenCVcore == null || libOpenCVhighgui == null || libOpenCVimgproc == null) {
          log1(-1, "checking: OpenCV not in loader cache (see doc-note on OpenCV)");
          opencvAvail = checkSuccess = false;
        } else {
          log1(lvl, "checking: found OpenCV libs:\n%s", libOpenCVcore);
        }
        if (libTesseract == null) {
          log1(-1, "checking: Tesseract not in loader cache (see doc-note on Tesseract)");
          tessAvail = checkSuccess = false;
        } else {
          log1(lvl, "checking: found Tesseract lib:\n%s", libTesseract);
        }
      }

      // checking wmctrl
      cmdRet = ResourceLoader.get().runcmd("wmctrl -m");
      if (cmdRet.contains(cmdError)) {
        log1(-1, "checking: wmctrl not available or not working");
        checkSuccess = false;
      } else {
        log1(lvl, "checking: wmctrl seems to be available");
      }
      libSearched = true;
    }

    log1(lvl, "checking\n%s", lib);
    // readelf -d lib
    // 0x0000000000000001 (NEEDED)             Shared library: [libtesseract.so.3]
    cmdRet = ResourceLoader.get().runcmd("readelf -d " + lib);
    if (cmdRet.contains(cmdError)) {
      log1(-1, "checking: readelf returns error:\ns", cmdRet);
      checkSuccess = false;
    } else {
      retLines = cmdRet.split("\n");
      String libsNeeded = "";
      for (String line : retLines) {
        if (line.contains("(NEEDED)")) {
          line = line.split("\\[")[1].replace("]", "");
          libsNeeded += line + ":";
        }
      }
      log0(lvl, libsNeeded);
    }

    if (!runLdd(lib)) {
      checkSuccess = false;
    }

    return checkSuccess;
  }

  private static boolean runLdd(File lib) {
    // ldd -r lib
    // undefined symbol: _ZN2cv3MatC1ERKS0_RKNS_5Rect_IiEE	(./libVisionProxy.so)
    String cmdRet = ResourceLoader.get().runcmd("ldd -r " + lib);
    String[] retLines;
    boolean success = true;
    if (cmdRet.contains(cmdError)) {
      log1(-1, "checking: ldd returns error:\ns", cmdRet);
      success = false;
    } else {
      retLines = cmdRet.split("\n");
      String libName = lib.getName();
      String libsMissing = "";
      for (String line : retLines) {
        if (line.contains("undefined symbol:") && line.contains(libName)) {
          line = line.split("symbol:")[1].trim().split("\\s")[0];
          libsMissing += line + ":";
        }
      }
      if (libsMissing.isEmpty()) {
        log1(lvl, "checking: should work: %s", libName);
      } else {
        log1(-1, "checking: might not work, has undefined symbols: %s", libName);
        log0(lvl, "%s", libsMissing);
        success = false;
      }
    }
    return success;
  }

  private static boolean buildVision(String srcjar) {
    File build = new File(workDir, buildFolder);
    File source = new File(workDir, buildFolderSrc);
    File stuff = new File(workDir, buildFolderStuff);
    File incl = new File(workDir, buildFolderInclude);

    File javaHome = new File(System.getProperty("java.home"));
    File javaInclude = null;
    File javaIncludeLinux = null;

    log1(lvl, "starting inline build: libVisionProxy.so");

    ResourceLoader rl = ResourceLoader.forJar(srcjar);

    if (!new File(javaHome, "bin/javac").exists()) {
      javaHome = javaHome.getParentFile();
    }
    if (!new File(javaHome, "bin/javac").exists()) {
      javaHome = null;
    }
    if (javaHome != null) {
      javaInclude = new File(javaHome, "include");
      javaIncludeLinux = new File(javaInclude, "linux");
      if (!new File(javaInclude, "jni.h").exists()) {
        javaHome = null;
      }
    }

    String buildInclude = "";
    String inclUsr = "/usr/include";
    String inclUsrLocal = "/usr/local/include";

    boolean exportIncludeJava = false;
    if (javaHome == null) {
      //log(lvl, "JDK: not found - set JAVA_HOME to a valid JDK");
      //buildComplete = false;
      log0(lvl, "buildVision: JDK: not found - using the bundled include files");
      buildInclude += " -I" + incl.getAbsolutePath();
      exportIncludeJava = true;
    } else {
      log0(lvl, "JDK: found at: %s", javaHome);
      buildInclude += String.format("-I%s -I%s ",
              javaInclude.getAbsolutePath(), javaIncludeLinux.getAbsolutePath());
    }

    boolean exportIncludeOpenCV = false;
    boolean exportIncludeTesseract = false;

    String inclLib = "opencv2";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      exportIncludeOpenCV = true;
      if (!exportIncludeJava) {
        buildInclude += " -I" + incl.getAbsolutePath();
      }
    }

    inclLib = "tesseract";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      exportIncludeTesseract = true;
      if (!exportIncludeOpenCV && !exportIncludeJava) {
        buildInclude += " -I" + incl.getAbsolutePath();
      }
    }

    if (!exportIncludeOpenCV || !exportIncludeTesseract) {
      buildInclude += " -I" + inclUsr + " -I" + inclUsrLocal;
    }

    String mfFile;
    String srcFile;
    log0(lvl, "buildVision: setting up the compile commands");
    for (String sFile : buildSrcFiles) {
      buildCompile += String.format("echo ----- %s\n", sFile);
      buildCompile += String.format(buildCppFix, buildInclude);
      mfFile = new File(stuff, sFile).getAbsolutePath();
      srcFile = new File(source, sFile).getAbsolutePath();
      buildCompile += String.format(buildCppMods, mfFile, mfFile, srcFile);
      buildCompile += "\n";
      buildLink += mfFile + ".o ";
    }
    log0(lvl, "buildVision: setting up the link command");
    buildLink += libOpenCVcore + " ";
    buildLink += libOpenCVhighgui + " ";
    buildLink += libOpenCVimgproc + " ";
    buildLink += libTesseract + " ";
    String libVisionPath = new File(build, libVision).getAbsolutePath();
    buildLink += "-o " + libVisionPath;
    File cmdFile = null;
    if (rl != null) {
      FileManager.deleteFileOrFolder(build.getAbsolutePath());
      build.mkdirs();
      source.mkdirs();
      if (stuff.exists()) {
        FileManager.deleteFileOrFolder(stuff.getAbsolutePath());
      }
      stuff.mkdirs();
      cmdFile = new File(build, "runBuild");

      PrintStream out = null;
      try {
        out = new PrintStream(new FileOutputStream(cmdFile));
        out.println("echo ----------- COMPILING");
        out.print(buildCompile);
        out.println("echo ----------- LINKING");
        out.println(buildLink);
        out.close();
        log0(lvl, "buildVision: build script written to: %s", cmdFile);
      } catch (Exception ex) {
        log0(-1, "buildVision: cannot write %s", cmdFile);
        return false;
      }
      rl.export("srcnativelibs/Vision#", source.getAbsolutePath());
      if (exportIncludeJava) {
        rl.export("srcnativelibs/VisionInclude/Java#", incl.getAbsolutePath());
      }
      if (exportIncludeOpenCV) {
        rl.export("srcnativelibs/VisionInclude/OpenCV#", incl.getAbsolutePath());
      }
      if (exportIncludeTesseract) {
        rl.export("srcnativelibs/VisionInclude/Tesseract#", incl.getAbsolutePath());
      }
      cmdFile.setExecutable(true);
    } else {
      log1(-1, "buildVision: cannot export lib sources");
      return false;
    }

      JFrame spl = null;
      if (opencvAvail && tessAvail) {
      spl = showSplash(libVision, "running build script");
      log1(lvl,"buildVision: running build script");
      String cmdRet = rl.runcmd(cmdFile.getAbsolutePath());
      if (cmdRet.contains(cmdError)) {
        log1(-1, "buildVision: build script returns error:\ns", cmdRet);
        closeSplash(spl);
        return false;
      } else {
        log1(lvl,"buildVision: checking created libVisionProxy.so");
        if (!runLdd(new File(libVisionPath))) {
          log1(-1,"------- output of the build run\n%s", cmdRet);
          closeSplash(spl);
          return false;
        }
      }
    }
    try {
      folderLibs.mkdirs();
      FileManager.xcopy(libVisionPath, new File(folderLibs, libVision).getAbsolutePath());
    } catch (IOException ex) {
      log1(-1, "could not copy built libVisionProxy.so to libs folder\n%s", ex.getMessage());
      closeSplash(spl);
      return false;
    }
    libsProvided = true;
    closeSplash(spl);
    log1(lvl, "ending inline build: success: libVisionProxy.so");
    return true;
  }

  private static void runScriptTest(String[] testargs) {
    try {
      Class sysclass = URLClassLoader.class;
      Class scriptRunner = sysclass.forName("org.sikuli.scriptrunner.ScriptRunner");
      Method mGetApplication = scriptRunner.getDeclaredMethod("runscript",
              new Class[]{String[].class});
      mGetApplication.invoke(null, new Object[]{testargs});
    } catch (Exception ex) {
      log(lvl, "runScriptTest: error: %s", ex.getMessage());
    }
  }

  private static String addSeps(String item) {
    if (Settings.isWindows()) {
      return item.replace("/", "\\");
    }
    return item;
  }

  private static boolean createSetupFolder(String path) {
    String projectDir = null, targetDir = null;
    boolean success = true;
    boolean doit = true;
    File setupjar = null;
    if (path.isEmpty()) {
      if (runningJar.endsWith("classes") || runningJar.endsWith("-plain.jar")) {
        projectDir = new File(workDir).getParentFile().getParentFile().getAbsolutePath();
      } else {
        success = false;
      }
      if (success) {
        setupjar = new File(new File(projectDir, "Setup/target"),
                localSetup.replace(".jar", "-plain.jar"));
        if (!setupjar.exists()) {
          success = false;
        }
      }
      if (success) {
        if (new File(projectDir, "Setup").exists()) {
          File ftargetDir = new File(projectDir, "Setup/target/Setup");
          if (ftargetDir.exists()) {
            FileManager.deleteFileOrFolder(ftargetDir.getAbsolutePath(),
                    new FileManager.FileFilter() {
                      @Override
                      public boolean accept(File entry) {
                        if (isLinux && entry.getPath().contains(addSeps("/libs/"))) {
                          return false;
                        }
                        if (entry.getPath().contains(addSeps("/Downloads/"))) {
                          if (entry.getName().contains("tess")) {
                            return false;
                          }
                        }
                        return true;
                      }
                    });
          }
          ftargetDir.mkdirs();
          targetDir = ftargetDir.getAbsolutePath();
        } else {
          success = false;
        }
      }
      if (!success) {
        log(-1, "createSetupFolder: Setup folder or %s missing", setupjar.getAbsolutePath());
        return false;
      }

      File fLibsmac, fLibswin, fLibslux, jythonJar, jrubyJar;

      File fIDEPlus = getProjectJarFile(projectDir,
              "IDEPlus", "sikulix-plus", "-ide-fat.jar");
      success &= fIDEPlus != null;
      File fAPIPlus = getProjectJarFile(projectDir,
              "APIPlus", "sikulixapi-plus", "-plain.jar");
      success &= fAPIPlus != null;

      fLibsmac = getProjectJarFile(projectDir,
              "Libsmac", libsMac, ".jar");
      success &= fLibsmac != null;
      fLibswin = getProjectJarFile(projectDir,
              "Libswin", libsWin, ".jar");
      success &= fLibswin != null;
      fLibslux = getProjectJarFile(projectDir,
              "Libslux", libsLux, ".jar");
      success &= fLibslux != null;

      if (success && !noSetup) {
        jythonJar = new File(Settings.SikuliJython);
        if (!jythonJar.exists()) {
          Debug.log(lvl, "createSetupFolder: missing: " + jythonJar.getAbsolutePath());
          success = false;
        }

        jrubyJar = new File(Settings.SikuliJRuby);
        if (!jrubyJar.exists()) {
          Debug.log(lvl, "createSetupFolder: missing " + jrubyJar.getAbsolutePath());
          success = false;
        }
      } else {
        jythonJar = jrubyJar = null;
      }

      if (success) {
        File fDownloads = new File(targetDir, "Downloads");
        fDownloads.mkdir();
        String fname = null;
        try {
          fname = setupjar.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(targetDir, localSetup).getAbsolutePath());
          fname = fIDEPlus.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(fDownloads, downloadIDE).getAbsolutePath());
          fname = fAPIPlus.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(fDownloads, downloadAPI).getAbsolutePath());

          // copy the library jars
          String fshort;
          for (File fEntry : new File[]{fLibsmac, fLibswin, fLibslux}) {
            fname = fEntry.getAbsolutePath();
            fshort = fEntry.getName();
            FileManager.xcopy(fname,
                    new File(fDownloads, fshort).getAbsolutePath());
          }

          if (!noSetup) {
            fname = jythonJar.getAbsolutePath();
            FileManager.xcopy(fname,
                    new File(fDownloads, downloadJython).getAbsolutePath());
            fname = jrubyJar.getAbsolutePath();
            FileManager.xcopy(fname,
                    new File(fDownloads, downloadJRuby).getAbsolutePath());
          }

//TODO JRubyAddOns
          String jrubyAddons = "sikulixjrubyaddons-" + Settings.SikuliProjectVersion + "-plain.jar";
          File fJRubyAddOns = new File(projectDir, "JRubyAddOns/target/" + jrubyAddons);
          fname = fJRubyAddOns.getAbsolutePath();
          File sname = new File(fDownloads, downloadJRubyAddOns);
          if (fJRubyAddOns.exists()) {
//            FileManager.xcopy(fname, sname.getAbsolutePath());
          }

//TODO remote robot
          fname = new File(projectDir, "RemoteRobot/target/"
                  + "sikulixremoterobot-" + Settings.SikuliProjectVersion + ".jar").getAbsolutePath();
//          FileManager.xcopy(fname, new File(fDownloads, downloadRServer).getAbsolutePath());

        } catch (Exception ex) {
          log(-1, "createSetupFolder: copying files did not work: %s", fname);
          success = false;
        }
      }
      if (success) {
        workDir = targetDir;
      }
    }
    return success;
  }

  private static File getProjectJarFile(String project, String jarFileDir, String jarFilePre, String jarFileSuf) {
    String jarFileName = getProjectJarFileName(jarFilePre, jarFileSuf);
    File fJarFile = new File(project, jarFileDir + "/target/" + jarFileName);
    if (!fJarFile.exists()) {
      log(-1, "createSetupFolder: missing: " + fJarFile.getAbsolutePath());
      return null;
    } else {
      return fJarFile;
    }
  }

  private static String getProjectJarFileName(String jarFilePre, String jarFileSuf) {
    return String.format("%s-%s%s", jarFilePre, Settings.SikuliProjectVersion, jarFileSuf);
  }

  private static boolean handleTempAfter(String temp, String target) {
    boolean success = true;
    log1(lvl, "renaming temp file to target jar:\n%s", target);
    FileManager.deleteFileOrFolder(target);
    success &= !new File(target).exists();
    if (success) {
      success &= (new File(temp)).renameTo(new File(target));
      if (!success) {
        log1(lvl, "rename did not work --- trying copy");
        try {
          FileManager.xcopy(new File(temp).getAbsolutePath(), target);
          success = new File(target).exists();
          if (success) {
            FileManager.deleteFileOrFolder(new File(temp).getAbsolutePath());
            success = !new File(temp).exists();
          }
        } catch (IOException ex) {
          success &= false;
        }
        if (!success) {
          log1(-1, "did not work");
          terminate("");
        }
      }
    }
    return success;
  }

  private static boolean isRunningUpdate() {
    return runningUpdate;
  }

  private static boolean getProxy(String pn, String pp) {
    if (!pn.isEmpty()) {
      Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
      if (p.matcher(pn).matches()) {
        Settings.proxyIP = pn;
      } else {
        Settings.proxyName = pn;
      }
      String msgp = String.format("Requested to use this Proxy: %s (%s)", pn, pp);
      log1(lvl, msgp);
      if (pp.isEmpty()) {
        popError(String.format("Proxy specification invalid: %s (%s)", pn, pp));
        log1(-1, "Terminating --- Proxy invalid");
        return false;
      } else {
        if (!popAsk(msgp)) {
          log1(-1, "Terminating --- User did not accept Proxy: %s %s", pn, pp);
          return false;
        }
      }
      Settings.proxyPort = pp;
      return true;
    }
    return false;
  }

  protected static void restore(boolean regular) {
    if (!regular) {
      log1(-1, "User requested termination");
    }
    if (!backUpExists) {
      return;
    }
    String backup = new File(workDir, "Backup").getAbsolutePath();
    if (new File(backup, localIDE).exists() && !new File(workDir, localIDE).exists()) {
      log0(lvl, "restoring from backup " + localIDE);
      new File(backup, localIDE).renameTo(new File(workDir, localIDE));
    }
    if (new File(backup, localAPI).exists() && !new File(workDir, localAPI).exists()) {
      log0(lvl, "restoring from backup " + localAPI);
      new File(backup, localAPI).renameTo(new File(workDir, localAPI));
    }
    if (new File(backup, localTess).exists() && !new File(workDir, localTess).exists()) {
      log0(lvl, "restoring from backup " + localTess);
      new File(backup, localTess).renameTo(new File(workDir, localTess));
    }
    if (new File(backup, localRServer).exists() && !new File(workDir, localRServer).exists()) {
      log0(lvl, "restoring from backup " + localRServer);
      new File(backup, localRServer).renameTo(new File(workDir, localRServer));
    }
    String folder = "Lib";
    if (new File(backup, folder).exists() && !new File(workDir, folder).exists()) {
      log0(lvl, "restoring from backup " + "folder " + folder);
      new File(backup, folder).renameTo(new File(workDir, folder));
    }
    folder = "libs";
    if (new File(backup, folder).exists() && !new File(workDir, folder).exists()) {
      log0(lvl, "restoring from backup " + "folder " + folder);
      new File(backup, folder).renameTo(new File(workDir, folder));
    }
//    FileManager.deleteFileOrFolder(new File(workDir, "Backup").getAbsolutePath());
    FileManager.deleteFileOrFolder(new File(workDir, "SikuliPrefs.txt").getAbsolutePath());
  }

  private static void reset(int type) {
    if (!hasOptions) {
      log1(3, "requested to reset: " + workDir);
      String message = "";
      if (type <= 0) {
        message = "You decided to run setup again!\n";
      } else if (isBeta) {
        message = "You decided to install a beta version!\n";
      } else if (isUpdate) {
        message = "You decided to install a new version!\n";
      }
      File fBackup = new File(workDir, "BackUp");
      if (fBackup.exists()) {
        if (!popAsk(message + "A backup folder exists and will be purged!\n"
                + "Click YES if you want to proceed.\n"
                + "Click NO, to first save the current backup folder and come back. ")) {
          System.exit(0);
        }
      }
      splash = showSplash("Now creating backup and cleaning setup folder", "please wait - may take some seconds ...");
      String backup = fBackup.getAbsolutePath();
      FileManager.deleteFileOrFolder(backup, new FileManager.FileFilter() {
        @Override
        public boolean accept(File entry) {
          return true;
        }
      });
      try {
        FileManager.xcopy(workDir, backup);
      } catch (IOException ex) {
        popError("Reset: Not possible to backup:\n" + ex.getMessage());
        terminate("Reset: Not possible to backup:\n" + ex.getMessage());
      }
    }

    FileManager.deleteFileOrFolder(workDir, new FileManager.FileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().equals(localSetup)) {
          return false;
        } else if (workDir.equals(entry.getAbsolutePath())) {
          return false;
        } else if (folderLibs.getAbsolutePath().equals(entry.getAbsolutePath())) {
          return !libsProvided;
        } else if ("BackUp".equals(entry.getName())) {
          return false;
        } else if ("Downloads".equals(entry.getName())) {
          return false;
        } else if (entry.getName().contains("SetupLog")) {
          return false;
        }
        return true;
      }
    });

    if (hasOptions) {
      return;
    }

    closeSplash(splash);
    log1(3, "backup completed!");
    backUpExists = true;
  }

  protected static void helpOption(int option) {
    String m;
    String om = "";
    m = "\n-------------------- Some Information on this option, that might "
            + "help to decide, wether to select it ------------------";
    switch (option) {
      case (1):
        om = "Package 1: You get SikuliX (sikulix.jar) which supports all usages of Sikuli";
//              -------------------------------------------------------------
        m += "\nIt is recommended for people new to Sikuli to get a feeling about the features";
        m += "\n - and those who want to develop Sikuli scripts with the Sikuli IDE";
        m += "\n - and those who want to run Sikuli scripts from commandline.";
        m += "\nDirectly supported scripting languages are Jython and JRuby (you might choose one of them or even both)";
        m += "\n\nFor those who know ;-) additionally you can ...";
        m += "\n- develop Java programs with Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n- develop in any Java aware scripting language adding Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n\nSpecial INFO for Jython, JRuby and Java developement";
        m += "\nIf you want to use standalone Jython/JRuby or want to develop in Java in parallel,";
        m += "\nyou should select Package 2 additionally (Option 2)";
        m += "\nIn these cases, Package 1 (SikuliX) can be used for image management and for small tests/trials.";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
//				if (Settings.isMac()) {
//					m += "\n\nSpecial info for Mac systems:";
//					m += "\nFinally you will have a Sikuli-IDE.app in the setup working folder.";
//					m += "\nTo use it, just move it into the Applications folder.";
//					m += "\nIf you need to run stuff from commandline or want to use Sikuli with Java,";
//					m += "\nyou have the following additionally in the setup folder:";
//					m += "\nrunIDE: the shellscript to run scripts and";
//					m += "\nsikulix.jar: for all other purposes than IDE and running scripts";
//					m += "\nMind the above special info about Jython, JRuby and Java developement too.";
//				}
        break;
      case (2):
        om = "Package 2: To support developement in Java or any Java aware scripting language. you get sikulixapi.jar."
                + "\nYou might want Package 1 (SikuliX) additionally to use the IDE for managing the images or some trials.";
//              -------------------------------------------------------------
        m += "\nThe content of this package is stripped down to what is needed to develop in Java"
                + " or any Java aware scripting language \n(no IDE, no bundled script run support for Jython/JRuby)";
        m += "\n\nHence this package is not runnable and must be in the class path to use it"
                + " for developement or at runtime";
        m += "\n\nSpecial info for usage with Jython/JRuby: It contains the Sikuli Jython/JRuby API ..."
                + "\n... and adds itself to Jython/JRuby path at runtime"
                + "\n... and exports the Sikuli Jython/JRuby modules to the folder Libs at runtime"
                + "\nthat helps to setup the auto-complete in IDE's like NetBeans, Eclipse ...";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        break;
      case (3):
        om = "To get the additional Tesseract stuff into your packages to use the OCR engine";
//              -------------------------------------------------------------
        m += "\nOnly makes sense for Windows and Mac,"
                + "\nsince for Linux the complete install of Tesseract is your job.";
        m += "\nFeel free to add this to your packages, \n...but be aware of the restrictions, oddities "
                + "and bugs with the current OCR and text search feature.";
        m += "\nIt adds more than 10 MB to your jars and the libs folder at runtime."
                + "\nSo be sure, that you really want to use it!";
        m += "\n\nIt is NOT recommended for people new to Sikuli."
                + "\nYou might add this feature later after having gathered some experiences with Sikuli";
        break;
      case (4):
        om = "To prepare the selected packages to run on all supported systems";
//              -------------------------------------------------------------
        m += "\nWith this option NOT selected, the setup process will only add the system specific"
                + " native stuff \n(Windows: support for both Java 32-Bit and Java 64-Bit is added)";
        m += "\n\nSo as a convenience you might select this option to produce jars, that are"
                + " useable out of the box on Windows, Mac and Linux.";
        m += "\nThis is possible now, since the usage of Sikuli does not need any system specific"
                + " preparations any more. \nJust use the package (some restrictions on Linux though).";
        m += "\n\nSome scenarios for usages in different system environments:";
        m += "\n- download or use the jars from a central network place ";
        m += "\n- use the jars from a stick or similar mobile medium";
        m += "\n- deploying Sikuli apps to be used all over the place";
        break;
      case (5):
        om = "To try out the experimental remote robot feature";
//              -------------------------------------------------------------
        m += "\nYou might start the downloaded jar on any system, that is reachable "
                + "\nby other systems in your network via TCP/IP (hostname or IP-address)."
                + "\nusing: java -jar sikulixremoterobot.jar"
                + "\n\nThe server is started and listens on a port (default 50000) for incoming requests"
                + "\nto use the mouse or keyboard or send back a screenshot."
                + "\nOn the client side a Sikuli script has to initiate a remote screen with the "
                + "\nrespective IP-address and port of a running server and on connection success"
                + "\nthe remote system can be used like a local screen/mouse/keyboard."
                + "\n\nCurrently all basic operations like find, click, type ... are supported,"
                + "\nbut be aware, that the search ops are done on the local system based on "
                + "\nscreenshots sent back from the remote system on request."
                + "\n\nMore information: https://github.com/RaiMan/SikuliX-Remote";
        break;
    }
    popInfo("asking for option " + option + ": " + om + "\n" + m);
  }

  private static String packMessage(String msg) {
    msg = msg.replace("\n\n", "\n");
    msg = msg.replace("\n\n", "\n");
    if (msg.startsWith("\n")) {
      msg = msg.substring(1);
    }
    if (msg.endsWith("\n")) {
      msg = msg.substring(0, msg.length() - 1);
    }
    return "--------------------\n" + msg + "\n--------------------";
  }

  private static void popError(String msg) {
    log1(3, "\npopError: " + packMessage(msg));
    if (!test) {
      Sikulix.popError(msg, "SikuliX-Setup: having problems ...");
    }
  }

  private static void popInfo(String msg) {
    log1(3, "\npopInfo: " + packMessage(msg));
    if (!test) {
      Sikulix.popup(msg, "SikuliX-Setup: info ...");
    }
  }

  private static boolean popAsk(String msg) {
    log1(3, "\npopAsk: " + packMessage(msg));
    if (test) {
      return true;
    }
    return Sikulix.popAsk(msg, "SikuliX-Setup: question ...");
  }

  private static JFrame showSplash(String title, String msg) {
    if (test) {
      return null;
    }
    start = (new Date()).getTime();
    return new SplashFrame(new String[]{"splash", "# " + title, "#... " + msg});
  }

  private static void closeSplash(JFrame splash) {
    if (splash == null) {
      return;
    }
    long elapsed = (new Date()).getTime() - start;
    if (elapsed < 3000) {
      try {
        Thread.sleep(3000 - elapsed);
      } catch (InterruptedException ex) {
      }
    }
    splash.dispose();
  }

  private static boolean download(String sDir, String tDir, String item, String jar, String itemName) {
    boolean shouldDownload = true;
    String dlSource;
    if (item == null) {
      String[] items = sDir.split("/");
      item = items[items.length - 1];
      dlSource = sDir;
    } else {
      if (!sDir.endsWith("/")) {
        sDir += "/";
      }
      dlSource = sDir + item;
    }
    if (jar == null) {
      jar = item;
    } else if ("nocopy".equals(jar)) {
      jar = null;
    }
    if (itemName == null) {
      itemName = item;
    }
    File downloaded = new File(tDir, item);
    shouldDownload = !takeAlreadyDownloaded(downloaded, itemName);
    String fname = null;
    if (shouldDownload) {
      if (hasOptions) {
        log1(lvl, "SilentSetup: Downloading: %s", itemName);
        fname = FileManager.downloadURL(dlSource, tDir, null);
      } else {
        JFrame progress = new SplashFrame("download");
        fname = FileManager.downloadURL(dlSource, tDir, progress);
        progress.dispose();
      }
      if (null == fname) {
        terminate(String.format("Fatal error 001: not able to download: %s", item), 1);
      }
    }
    if (jar != null) {
      copyFromDownloads(downloaded, item, jar);
      if (!shouldDownload) {
        downloadedFiles = downloadedFiles.replace(item + " ", "");
      }
    }
    return true;
  }

  private static boolean takeAlreadyDownloaded(File artefact, String itemName) {
    if (artefact.exists()) {
      return popAsk("Setup/Downloads folder has: " + itemName + "\n"
              + artefact.getAbsolutePath()
              + "\nClick YES, if you want to use this for setup processing\n\n"
              + "... or click NO, to download a fresh copy");
    }
    return false;
  }

  private static void copyFromDownloads(File artefact, String item, String jar) {
    try {
      FileManager.xcopy(artefact.getAbsolutePath(), jar);
    } catch (IOException ex) {
      terminate("Unable to copy from Downloads: "
              + artefact.getAbsolutePath() + "\n" + ex.getMessage());
    }
    log(lvl, "Copied from Downloads: " + item);
  }

  private static boolean getSikulixJarFromMaven(String src, String targetDir,
          String targetJar, String itemName) {
    boolean develop = !Settings.isVersionRelease();
    String mPath = "";
    String xml;
    String xmlContent;
    String timeStamp = "";
    String buildNumber = "";
    String mJar = "";
    String sikulixMavenGroup = "com/sikulix/";
    if (targetJar == null) {
      targetJar = new File(workDir, itemName + ".jar").getAbsolutePath();
    }
    if (develop) {
      String dlMavenSnapshotPath = version + "-SNAPSHOT";
      String dlMavenSnapshotXML = "maven-metadata.xml";
      String dlMavenSnapshotPrefix = String.format("%s%s/%s/", sikulixMavenGroup, src, dlMavenSnapshotPath);
      mPath = Settings.dlMavenSnapshot + dlMavenSnapshotPrefix;
      xml = mPath + dlMavenSnapshotXML;
      xmlContent = FileManager.downloadURLtoString(xml);
      Matcher m = Pattern.compile("<timestamp>(.*?)</timestamp>").matcher(xmlContent);
      if (m.find()) {
        timeStamp = m.group(1);
        m = Pattern.compile("<buildNumber>(.*?)</buildNumber>").matcher(xmlContent);
        if (m.find()) {
          buildNumber = m.group(1);
        }
      }
      if (!timeStamp.isEmpty() && !buildNumber.isEmpty()) {
        mJar = String.format("%s-%s-%s-%s.jar", src, version, timeStamp, buildNumber);
        log(lvl, "getMavenJar: %s", mJar);
      } else {
        log(-1, "Maven download: could not get timestamp or buildnumber from:"
                + "\n%s\nwith content:\n", xml, xmlContent);
      }
      return download(mPath + mJar, targetDir, null, targetJar, itemName);
    } else {
      mPath = String.format("%s%s/%s/", sikulixMavenGroup, src, version);
      mJar = String.format("%s-%s.jar", src, version);
      return getJarFromMaven(mPath + mJar, targetDir, targetJar, itemName);
    }
  }

  private static boolean getJarFromMaven(String src, String target,
          String targetJar, String itemName) {
    return download(Settings.dlMavenRelease + src, target, null, targetJar, itemName);
  }

  private static void userTerminated(String msg) {
    if (!msg.isEmpty()) {
      log1(lvl, msg);
    }
    log1(lvl, "User requested termination.");
    System.exit(0);
  }

  private static void prepTerminate(String msg) {
    if (msg.isEmpty()) {
      restore(true);
    } else {
      log1(-1, msg);
      log1(-1, "... terminated abnormally :-(");
      popError("Something serious happened! Sikuli not useable!\n"
              + "Check the error log at " + (logfile == null ? "printout" : logfile));
    }
  }

  private static void terminate(String msg) {
    prepTerminate(msg);
    System.exit(0);
  }

  private static void terminate(String msg, int ret) {
    prepTerminate(msg);
    System.exit(ret);
  }
}
