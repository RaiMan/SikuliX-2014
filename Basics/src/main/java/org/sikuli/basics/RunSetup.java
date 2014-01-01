/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * RaiMan 2013
 */
package org.sikuli.basics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class RunSetup {

  private static boolean runningUpdate = false;
  private static boolean isUpdateSetup = false;
  public static String timestampBuilt;
  private static final String tsb = "##--##Mi 27 Nov 2013 15:29:45 CET##--##";
  private static boolean runningfromJar = true;
  private static String workDir;
  private static String uhome;
  private static String logfile;
  private static String version = Settings.getVersionShort();
  private static String majorversion = Settings.getVersionShortBasic();
  private static String updateVersion;
  private static String downloadBaseDirBase = "https://launchpad.net/raiman/sikulix2013+/";
  private static String downloadBaseDir = downloadBaseDirBase + majorversion + ".0/";
  private static String downloadSetup;
  private static String downloadIDE = version + "-1.jar";
  private static String downloadMacApp = version + "-9.jar";
  private static String downloadScript = version + "-2.jar";
  private static String downloadJava = version + "-3.jar";
  private static String downloadTess = version + "-5.jar";
  private static String downloadRServer = version + "-7.jar";
  private static String localJava = "sikuli-java.jar";
  private static String localScript = "sikuli-script.jar";
  private static String localIDE = "sikuli-ide.jar";
  private static String localMacApp = "sikuli-macapp.jar";
  private static String localMacAppIDE = "SikuliX-IDE.app/Contents/sikuli-ide.jar";
  private static String folderMacApp = "SikuliX-IDE.app";
  private static String folderMacAppContent = folderMacApp + "/Contents";
  private static String localSetup = "sikuli-setup-" + majorversion + ".jar";
  private static String localUpdate = "sikuli-update";
  private static String localTess = "sikuli-tessdata.jar";
  private static String localRServer = "sikulix-remoteserver.jar";
  private static String localLogfile;
  private static SetUpSelect winSU;
  private static JFrame winSetup;
  private static boolean getIDE, getScript, getJava, getTess, getRServer;
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
  private static boolean forAllSystems = false;
  private static boolean shouldPackLibs = true;
  private static long start;
  private static boolean runningSetup = false;
  private static boolean generallyDoUpdate = false;

  static {
    timestampBuilt = tsb.substring(6, tsb.length() - 6);
    timestampBuilt = timestampBuilt.substring(
            timestampBuilt.indexOf(" ") + 1, timestampBuilt.lastIndexOf(" "));
    timestampBuilt = timestampBuilt.replaceAll(" ", "").replaceAll(":", "").toUpperCase();
  }

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, level < 0 ? "error" : "debug",
            me + ": " + mem + ": " + message, args);
  }

  private static void log0(int level, String message, Object... args) {
    Debug.logx(level, level < 0 ? "error" : "debug",
            me + ": " + message, args);
  }

  private static void log1(int level, String message, Object... args) {
    String sout;
    String prefix = level < 0 ? "error" : "debug";
    if (args.length != 0) {
      sout = String.format("[" + prefix + "] " + message, args);
    } else {
      sout = "[" + prefix + "] " + message;
    }
    System.out.println(sout);
    Debug.logx(level, level < 0 ? "error" : "debug",
            me + ": " + message, args);
  }
//</editor-fold>

  public static void main(String[] args) {
    mem = "main";

    PreferencesUser prefs = PreferencesUser.getInstance();
    boolean prefsHaveProxy = false;

    if (Settings.SikuliVersionBetaN > 0 && Settings.SikuliVersionBetaN < 999) {
      updateVersion = String.format("%d.%d-Beta%d",
              Settings.SikuliVersionMajor, Settings.SikuliVersionMinor,
              1 + Settings.SikuliVersionBetaN);
    } else if (Settings.SikuliVersionSub > 0) {
      updateVersion = String.format("%d.%d.%d",
              Settings.SikuliVersionMajor, Settings.SikuliVersionMinor,
              1 + Settings.SikuliVersionSub);
    } else {
      updateVersion = String.format("%d.%d.%d",
              Settings.SikuliVersionMajor, Settings.SikuliVersionMinor, 0);
    }

    options.addAll(Arrays.asList(args));

    //<editor-fold defaultstate="collapsed" desc="options special">
    if (args.length > 0 && "version".equals(args[0])) {
      System.out.println(Settings.getVersionShort());
      System.exit(0);
    }

    if (args.length > 0 && "majorversion".equals(args[0])) {
      System.out.println(version.substring(0, 3));
      System.exit(0);
    }

    if (args.length > 0 && "updateversion".equals(args[0])) {
      System.out.println(updateVersion);
      System.exit(0);
    }

    if (args.length > 0 && "test".equals(args[0])) {
      test = true;
      options.remove(0);
    }

    if (args.length > 0 && "runningSetup".equals(args[0])) {
      runningSetup = true;
      options.remove(0);
    }

    if (args.length > 0 && "update".equals(args[0])) {
      runningUpdate = true;
      options.remove(0);
    }


    if (args.length > 0 && "updateSetup".equals(args[0])) {
      isUpdateSetup = true;
      options.remove(0);
    }

    runningJar = FileManager.getJarName();
    if (runningJar.isEmpty()) {
      popError("error accessing jar - terminating");
      System.exit(1);
    }
    if (runningJar.startsWith("sikuli-update")) {
      runningUpdate = true;
    }

    if (runningUpdate) {
      localLogfile = "SikuliX-" + version + "-UpdateLog.txt";
    } else {
      localLogfile = "SikuliX-" + version + "-SetupLog.txt";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="option makeJar">
    if (options.size() > 0 && options.get(0).equals("makeJar")) {
      options.remove(0);
      String todo, jarName, folder;
      while (options.size() > 0) {
        todo = options.get(0);
        options.remove(0);
        //***
        // pack a jar from a folder
        //***
        if (todo.equals("packJar")) {
          if (options.size() < 2) {
            log1(-1, "packJar: invalid options!");
            System.exit(1);
          }
          jarName = FileManager.slashify(options.get(0), false);
          options.remove(0);
          folder = options.get(0);
          options.remove(0);
          log1(3, "requested to pack %s from %s", jarName, folder);
          FileManager.packJar(folder, jarName, null);
          log1(3, "completed!");
          continue;
          //***
          // unpack a jar to a folder
          //***
        } else if (todo.equals("unpackJar")) {
          if (options.size() < 2) {
            log1(-1, "unpackJar: invalid options!");
            System.exit(1);
          }
          jarName = options.get(0);
          options.remove(0);
          folder = options.get(0);
          options.remove(0);
          log1(3, "requested to unpack %s to %s", jarName, folder);
          // action
          log1(3, "completed!");
          continue;
          //***
          // build a jar by combining other jars (optionally filtered) and/or folders
          //***
        } else if (todo.equals("buildJar")) {
          // build jar arg0
          if (options.size() < 2) {
            log1(-1, "buildJar: invalid options!");
            System.exit(1);
          }
          jarName = options.get(0);
          options.remove(0);
          folder = options.get(0);
          options.remove(0);
          log1(3, "requested to build %s to %s", jarName, folder);
          // action
          log1(3, "completed!");
          continue;
        } else {
          log1(-1, "makejar: invalid option: " + todo);
          System.exit(1);
        }
      }
      System.exit(0);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="general preps">
    Settings.runningSetup = true;
    IResourceLoader loader = FileManager.getNativeLoader("basic", args);

    uhome = System.getProperty("user.home");
    workDir = FileManager.getJarParentFolder();
    if (workDir.startsWith("N")) {
      runningfromJar = false;
    }
    workDir = workDir.substring(1);

    if (runningfromJar) {
      logfile = (new File(workDir, localLogfile)).getAbsolutePath();
    } else {
      workDir = (new File(uhome, "SikuliX/Setup")).getAbsolutePath();
      (new File(workDir)).mkdirs();
      logfile = (new File(workDir, localLogfile)).getAbsolutePath();
      popInfo("\n... not running from sikuli-setup.jar - using as download folder\n" + workDir);
    }

    if (!Debug.setLogFile(logfile)) {
      popError(workDir + "\n... folder we are running in must be user writeable! \n"
              + "please correct the problem and start again.");
      System.exit(0);
    }

    Settings.LogTime = true;
    Debug.setDebugLevel(3);
    log0(lvl, "running from: " + runningJar);
    log1(lvl, "SikuliX Setup Build: %s %s", Settings.getVersionShort(), RunSetup.timestampBuilt);

    if (args.length > 0) {
      log1(lvl, "... starting with " + SikuliX.arrayToString(args));
    } else {
      log1(lvl, "... starting with no args given");
    }
    log1(lvl, "user home: %s", uhome);

    File localJarSetup = new File(workDir, localSetup);
    File localJarIDE = new File(workDir, localIDE);
    File localJarScript = new File(workDir, localScript);
    File localJarJava = new File(workDir, localJava);
    File localMacFolder = new File(workDir, folderMacApp);

    //TODO Windows 8 HKLM/SOFTWARE/JavaSoft add Prefs ????

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="checking update/beta">
    if (!runningUpdate && !isUpdateSetup) {
      String uVersion = "";
      String msgFooter = "You have " + Settings.getVersion()
              + "\nClick YES, if you want to install ..."
              + "\ncurrent stuff will be saved to BackUp."
              + "\n... Click NO to skip ...";
      if (localJarIDE.exists() || localJarScript.exists()
              || localJarJava.exists() || localMacFolder.exists()) {
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
        String ask2 = "Click YES to get info on updates or betas.\n"
                + "or click NO to terminate setup now.";
        if (generallyDoUpdate && popAsk(ask2)) {
          splash = showSplash("Checking for update or beta versions! (you have " + version + ")",
                  "please wait - may take some seconds ...");
          AutoUpdater au = new AutoUpdater();
          avail = au.checkUpdate();
          closeSplash(splash);
          if (avail > 0) {
            if (avail == AutoUpdater.BETA || avail == AutoUpdater.SOMEBETA) {
              someUpdate = true;
              uVersion = au.getBetaVersion();
              if (popAsk("Version " + uVersion + " is available\n" + msgFooter)) {
                isBeta = true;
              }
            }
            if (avail > AutoUpdater.FINAL) {
              avail -= AutoUpdater.SOMEBETA;
            }
            if (avail > 0 && avail != AutoUpdater.BETA) {
              someUpdate = true;
              if (popAsk(au.whatUpdate + "\n" + msgFooter)) {
                isUpdate = true;
                uVersion = au.getVersionNumber();
              }
            }
          }
          if (!someUpdate) {
            popInfo("No suitable update or beta available");
            userTerminated("No suitable update or beta available");
          }
        }
        if (!isBeta && !isUpdate) {
          reset(-1);
        } else {
          log1(lvl, "%s is available", uVersion);
          if (uVersion.equals(updateVersion)) {
            reset(avail);
            downloadBaseDir = downloadBaseDirBase + uVersion.substring(0, 3) + "/";
            downloadSetup = "sikuli-update-" + uVersion + ".jar";
            if (!download(downloadBaseDir, workDir, downloadSetup,
                    new File(workDir, downloadSetup).getAbsolutePath())) {
              restore();
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
            System.exit(0);
          } else {
            popError("downloadable update: " + uVersion + "\nexpected update: " + updateVersion
                    + "\n do not match --- terminating --- pls. report");
            terminate("update versions do not match");
          }
        }
      }
    } else {
      log0(lvl, "Update started");
      if (!generallyDoUpdate) {
        terminate("Switched Off: Run update!");
      }
      if (!popAsk("You requested to run an Update now"
              + "\nYES to continue\nNO to terminate")) {
        userTerminated("");
      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="dispatching external setup run">
    if (!isUpdateSetup && !runningSetup) {
      String[] cmd = null;
      File fCmd = null;
      String runSetupOption = "";
      if (isRunningUpdate()) {
        runSetupOption = "updateSetup";
      } else if (!runningSetup) {
        runSetupOption = "runningSetup";
      }
      if (Settings.isWindows()) {
        log1(lvl, "Extracting runSetup.cmd");
        String syspath = System.getenv("PATH");
        for (String p : syspath.split(";")) {
          log1(lvl, "syspath: " + p);
        }
        loader.export("Commands/windows#runSetup.cmd", workDir);
        fCmd = new File(workDir, "runSetup.cmd");
        cmd = new String[]{"cmd", "/C", "start", "cmd", "/K", fCmd.getAbsolutePath(), runSetupOption};
      } else if (runningUpdate) {
        log1(lvl, "Extracting runSetup");
        fCmd = new File(workDir, "runSetup");
        loader.export("Commands/"
                + (Settings.isMac() ? "mac#runSetup" : "linux#runSetup"), workDir);
        loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", fCmd.getAbsolutePath()});
        if (Settings.isMac()) {
          cmd = new String[]{"/bin/sh", fCmd.getAbsolutePath(), runSetupOption};
        } else {
          cmd = new String[]{"/bin/bash", fCmd.getAbsolutePath(), runSetupOption};
        }
      }
      if ((Settings.isWindows() || runningUpdate) && (fCmd == null || !fCmd.exists())) {
        String msg = "Fatal error 002: runSetup(.cmd) could not be exported to " + workDir;
        log0(-1, msg);
        popError(msg);
        System.exit(2);
      }
      if (runningUpdate) {
        localSetup = "sikuli-setup-" + updateVersion.substring(0, 3) + ".jar";
        FileManager.deleteFileOrFolder(new File(workDir, localSetup).getAbsolutePath());
        log1(lvl, "Update: trying to dowload the new sikuli-setup.jar version " + updateVersion.substring(0, 3));
        downloadSetup = "sikuli-setup-" + updateVersion + ".jar";
        downloadBaseDir = downloadBaseDirBase + updateVersion.substring(0, 3) + "/";
        if (!download(downloadBaseDir, workDir, downloadSetup,
                new File(workDir, localSetup).getAbsolutePath())) {
          restore();
          popError("Download did not complete successfully.\n"
                  + "Check the logfile for possible error causes.\n\n"
                  + "If you think, setup's inline download from Dropbox is blocked somehow on,\n"
                  + "your system, you might download manually (see respective FAQ)\n"
                  + "For other reasons, you might simply try to run setup again.");
          terminate("download not completed successfully");
        }
      }
      if (cmd != null) {
        if (runningUpdate && !popAsk("Continue Update after download success?"
                + "\nYES to continue\nNO to terminate")) {
          userTerminated("after download success");
        }
        log1(lvl, "dispatching external setup run");
        if (runningfromJar) {
          loader.doSomethingSpecial("runcmd", cmd);
          System.exit(0);
        }
      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="option setup preps display options">
    String proxyMsg = "";
    if (!isUpdateSetup) {
      popInfo("Please read carefully before proceeding!!");
      winSetup = new JFrame("SikuliX-Setup");
      Border rpb = new LineBorder(Color.YELLOW, 8);
      winSetup.getRootPane().setBorder(rpb);
      Container winCP = winSetup.getContentPane();
      winCP.setLayout(new BorderLayout());
      winSU = new SetUpSelect();
      winCP.add(winSU, BorderLayout.CENTER);
      winSetup.pack();
      winSetup.setLocationRelativeTo(null);
      winSetup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      winSetup.setVisible(true);

      //setup version basic
      winSU.suVersion.setText(Settings.getVersionShort() + "   (" + timestampBuilt + ")");

      // running system
      Settings.getOS();
      msg = Settings.osName + " " + Settings.getOSVersion();
      winSU.suSystem.setText(msg);
      log0(lvl, "RunningSystem: " + msg);

      // folder running in
      winSU.suFolder.setText(workDir);
      log0(lvl, "parent of jar/classes: %s", workDir);

      // running Java
      String osarch = System.getProperty("os.arch");
      msg = "Java " + Settings.JavaVersion + " (" + osarch + ") " + Settings.JREVersion;
      winSU.suJava.setText(msg);
      log0(lvl, "RunningJava: " + msg);

      String pName = prefs.get("ProxyName", "");
      String pPort = prefs.get("ProxyPort", "");
      if (!pName.isEmpty() && !pPort.isEmpty()) {
        prefsHaveProxy = true;
        winSU.pName.setText(pName);
        winSU.pPort.setText(pPort);
      }

      getIDE = false;
      getScript = false;
      getJava = false;
      getTess = false;

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
    fPrefs.deleteOnExit();
    prefs.exportPrefs(fPrefs.getAbsolutePath());
    try {
      BufferedReader pInp = new BufferedReader(new FileReader(fPrefs));
      String line;
      while (null != (line = pInp.readLine())) {
        if (!line.contains("entry")) {
          continue;
        }
        log0(lvl, "Prefs: " + line.trim());
      }
      pInp.close();
    } catch (Exception ex) {
    }
    FileManager.deleteFileOrFolder(fPrefs.getAbsolutePath());
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="option setup: download">
    if (!isUpdateSetup) {
      if (winSU.option1.isSelected() && winSU.option2.isSelected()) {
        if (!popAsk("You either need IDE (1) --OR-- Script (2) !\n"
                + "When proceeding (YES), you only will get the IDE,\n"
                + "which can be used the same way as Script from commandline.\n"
                + "If not sure, click (NO), and run setup again to have a\n"
                + "deeper look at the provided help information (H buttons).")) {
          userTerminated("options 1 and 2 selected");
        }
      }
      if (winSU.option1.isSelected()) {
        getIDE = true;
      }
      if (winSU.option2.isSelected() && !getIDE) {
        getScript = true;
      }
      if (winSU.option3.isSelected()) {
        getJava = true;
      }
      if (winSU.option4.isSelected()) {
        if (getIDE || getScript) {
          if (!popAsk("You selected Option 4, but also Option 1 or 2 !\n"
                  + "When proceeding (YES), you will only get Pack 3.\n"
                  + "If not sure, click (NO), and run setup again to have a\n"
                  + "deeper look at the provided help information (H buttons).")) {
            userTerminated("option 4 selected and options 1 or 2");
          }
        }
        getIDE = false;
        getScript = false;
        getJava = true;
      }
      if (winSU.option5.isSelected()) {
        if (Settings.isLinux()) {
          popInfo("You selected option 5 (Tesseract support)\n"
                  + "On Linux this does not make sense, since it\n"
                  + "is your responsibility to setup Tesseract on your own.\n"
                  + "This option will be ignored.");
        } else {
          getTess = true;
        }
      }
      if (winSU.option6.isSelected()) {
        forAllSystems = true;
      }
      if (winSU.option7.isSelected()) {
        getRServer = true;
      }

      if (((getTess || forAllSystems) && !(getIDE || getScript || getJava))) {
        popError("You only selected Option 5 or 6 !\n"
                + "This is currently not supported.\n"
                + "Please start allover again with valid options.\n");
        System.exit(0);
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
      if (new File(workDir, localScript).exists()) {
        getScript = true;
        msg += "Pack 2: " + localScript + "\n";
      }
      if (new File(workDir, localJava).exists()) {
        getJava = true;
        msg += "Pack 3: " + localJava + "\n";
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
              + "have been setup for all systems (option 6).\n"
              + "Click YES if you want this option now\n"
              + "Click NO to run normal setup for current system")) {
        forAllSystems = true;
      }
    }

    if (!isUpdateSetup) {
      if (getIDE || getScript || getJava || getRServer) {
        if (!proxyMsg.isEmpty()) {
          msg += proxyMsg + "\n";
        }
        if (getIDE) {
          msg += "\n--- Package 1 ---\n" + downloadIDE;
          if (Settings.isMac()) {
            msg += "\n" + downloadMacApp;
          }
        }
        if (getScript) {
          msg += "\n--- Package 2 ---\n" + downloadScript;
        }
        if (getJava) {
          msg += "\n--- Package 3 ---\n" + downloadJava;
        }
        if (getTess || getRServer) {
          msg += "\n--- Additions ---";
          if (getTess) {
            msg += "\n" + downloadTess;
          }
          if (getRServer) {
            msg += "\n" + downloadRServer;
          }
        }
      }
    }

    if (getIDE || getScript || getJava || getRServer) {
      msg += "\n\nOnly click NO, if you want to terminate setup now!\n"
              + "Click YES even if you want to use local copies in Downloads!";
      if (!popAsk(msg)) {
        System.exit(0);
      }
    } else {
      popError("Nothing selected! Sikuli not useable!\nYou might try again ;-)");
      System.exit(1);
    }

    // downloading
    localJar = null;
    String targetJar;
    boolean downloadOK = true;
    boolean dlOK = true;
    if (getIDE) {
      localJar = new File(workDir, localIDE).getAbsolutePath();
      if (!test) {
        dlOK = download(downloadBaseDir, workDir, downloadIDE, localJar);
      }
      downloadOK &= dlOK;
      if (Settings.isMac()) {
        targetJar = new File(workDir, localMacApp).getAbsolutePath();
        if (!test) {
          dlOK = download(downloadBaseDir, workDir, downloadMacApp, targetJar);
        }
        if (dlOK) {
          FileManager.deleteFileOrFolder((new File(workDir, folderMacApp)).getAbsolutePath());
          FileManager.unpackJar(targetJar, workDir, false);
        }
        downloadOK &= dlOK;
      }
    } else if (getScript) {
      localJar = new File(workDir, localScript).getAbsolutePath();
      if (!test) {
        downloadOK = download(downloadBaseDir, workDir, downloadScript, localJar);
      }
      downloadOK &= dlOK;
    }
    if (getJava) {
      targetJar = new File(workDir, localJava).getAbsolutePath();
      if (!test) {
        downloadOK = download(downloadBaseDir, workDir, downloadJava, targetJar);
      }
      downloadOK &= dlOK;
    }
    if (getTess) {
      targetJar = new File(workDir, localTess).getAbsolutePath();
      if (!test) {
        downloadOK = download(downloadBaseDir, workDir, downloadTess, targetJar);
      }
      downloadOK &= dlOK;
    }
    if (getRServer) {
      targetJar = new File(workDir, localRServer).getAbsolutePath();
      if (!test) {
        downloadOK = download(downloadBaseDir, workDir, downloadRServer, targetJar);
      }
      downloadOK &= dlOK;
    }
    log1(lvl, "Download ended");
    if (!test && !downloadOK) {
      popError("Some of the downloads did not complete successfully.\n"
              + "Check the logfile for possible error causes.\n\n"
              + "If you think, setup's inline download is blocked somehow on,\n"
              + "your system, you might download the appropriate raw packages manually and \n"
              + "unzip them into a folder Downloads in the setup folder and run setup again.\n"
              + "Be aware: The raw packages are not useable without being processed by setup!\n\n"
              + "For other reasons, you might simply try to run setup again.");
      terminate("download not completed successfully");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="option setup: add native stuff">
    restore(); //to get back the stuff that was not changed
    if (test && !popAsk("add native stuff --- proceed?")) {
      System.exit(1);
    }

    if (!getIDE && !getScript && !getJava) {
      log1(lvl, "Nothing else to do");
      System.exit(0);
    }

    if (Settings.isLinux()) {
      if (popAsk("If you have provided your own builds\n"
              + "of the native libraries in the system paths:\n"
              + "Click YES if you did (be sure, they are there)\n"
              + "Click NO to pack the bundled libs to the jars.")) {
        shouldPackLibs = false;
      }
    }

    boolean success = true;
    FileManager.JarFileFilter libsFilter = new FileManager.JarFileFilter() {
      @Override
      public boolean accept(ZipEntry entry) {
        if (forAllSystems) {
          if (!shouldPackLibs && entry.getName().startsWith("META-INF/libs/linux")
                  && entry.getName().contains("VisionProxy")) {
            return false;
          }
          return true;
        } else if (Settings.isWindows()) {
          if (entry.getName().startsWith("META-INF/libs/mac")
                  || entry.getName().startsWith("META-INF/libs/linux")) {
            return false;
          }
        } else if (Settings.isMac()) {
          if (entry.getName().startsWith("META-INF/libs/windows")
                  || entry.getName().startsWith("META-INF/libs/linux")) {
            return false;
          }
        } else if (Settings.isLinux()) {
          if (entry.getName().startsWith("META-INF/libs/windows")
                  || entry.getName().startsWith("META-INF/libs/mac")) {
            return false;
          }
          if (!shouldPackLibs && entry.getName().contains("VisionProxy")) {
            return false;
          }
        }
        return true;
      }
    };

    String[] jarsList = new String[]{null, null, null};
    String localTemp = "sikuli-temp.jar";
    String[] localJars = new String[3];
    String localTestJar = null;
    if (getIDE) {
      localJars[0] = localIDE;
      localTestJar = (new File(workDir, localIDE)).getAbsolutePath();
    }
    if (getScript) {
      localJars[1] = localScript;
      localTestJar = (new File(workDir, localScript)).getAbsolutePath();
    }
    if (getJava) {
      localJars[2] = localJava;
    }
    splash = showSplash("Now adding native stuff to selected jars.", "please wait - may take some seconds ...");
    for (String path : localJars) {
      if (path == null) {
        continue;
      }
      log1(lvl, "adding native stuff to " + path);
      localJar = (new File(workDir, path)).getAbsolutePath();
      jarsList[0] = localJar;
      jarsList[1] = (new File(workDir, localSetup)).getAbsolutePath();
      if (!getTess) {
        jarsList[2] = null;
      } else {
        jarsList[2] = (new File(workDir, localTess)).getAbsolutePath();
      }
      targetJar = (new File(workDir, localTemp)).getAbsolutePath();
      success &= FileManager.buildJar(targetJar, jarsList, null, null, libsFilter);
      success &= (new File(localJar)).delete();
      success &= (new File(workDir, localTemp)).renameTo(new File(localJar));
    }

    if (Settings.isMac()
            && getIDE) {
      closeSplash(splash);
      log1(lvl, "preparing Mac app as SikuliX-IDE.app");
      splash = showSplash("Now preparing Mac app SikuliX-IDE.app.", "please wait - may take some seconds ...");
      forAllSystems = false;
      targetJar = (new File(workDir, localMacAppIDE)).getAbsolutePath();
      jarsList = new String[]{(new File(workDir, localIDE)).getAbsolutePath()};
      success &= FileManager.buildJar(targetJar, jarsList, null, null, libsFilter);
    }

    closeSplash(splash);
    if (success && (getIDE || getScript)) {
      log1(lvl, "exporting commandfiles");
      splash = showSplash("Now exporting commandfiles.", "please wait - may take some seconds ...");

      if (Settings.isWindows()) {
        if (getIDE) {
          loader.export("Commands/windows#runIDE.cmd", workDir);
        } else if (getScript) {
          loader.export("Commands/windows#runScript.cmd", workDir);
        }

      } else if (Settings.isMac()) {
        if (getIDE) {
          String fmac = new File(workDir, folderMacAppContent).getAbsolutePath();
          loader.export("Commands/mac#runIDE", fmac);
          loader.export("Commands/mac#runIDE", workDir);
          loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", new File(fmac, "runIDE").getAbsolutePath()});
          loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", new File(workDir, "runIDE").getAbsolutePath()});
//          FileManager.deleteFileOrFolder(new File(workDir, localIDE).getAbsolutePath());
          FileManager.deleteFileOrFolder(new File(workDir, localMacApp).getAbsolutePath());
          localTestJar = new File(fmac, localIDE).getAbsolutePath();
        } else if (getScript) {
          loader.export("Commands/mac#runScript", workDir);
          loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", new File(workDir, "runScript").getAbsolutePath()});
        }

      } else if (Settings.isLinux()) {
        if (getIDE) {
          loader.export("Commands/linux#runIDE", workDir);
          loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", new File(workDir, "runIDE").getAbsolutePath()});
          loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", new File(workDir, localIDE).getAbsolutePath()});
        } else if (getScript) {
          loader.export("Commands/linux#runScript", workDir);
          loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", new File(workDir, "runScript").getAbsolutePath()});
        }
      }
      closeSplash(splash);
    }
    if (!success) {
      popError("Bad things happened trying to add native stuff to selected jars --- terminating!");
      terminate("Adding stuff to jars did not work");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="option setup: environment setup and test">
    log1(lvl, "trying to set up the environment");
    splash = showSplash("Now trying to set up Sikuli environment!", "please wait - may take some seconds ...");
    File folderLibs = new File(workDir, "libs");

    if (folderLibs.exists()) {
      FileManager.deleteFileOrFolder(folderLibs.getAbsolutePath());
    }

    folderLibs.mkdirs();

    loader.check(Settings.SIKULI_LIB);

    if (loader.doSomethingSpecial("checkLibsDir", null)) {
      closeSplash(splash);
      splash = showSplash(" ", "Environment seems to be ready!");
      closeSplash(splash);
    } else {
      closeSplash(splash);
      popError("Something serious happened! Sikuli not useable!\n"
              + "Check the error log at " + logfile);
      terminate("Setting up environment did not work");
    }
    if (getJava) {
      log1(lvl, "Trying to run functional test: JAVA-API");
      splash = showSplash("Trying to run functional test(s)", "Java-API: org.sikuli.script.SikuliX.testSetup()");
      if (!SikuliX.addToClasspath(localJarJava.getAbsolutePath())) {
        closeSplash(splash);
        log0(-1, "Java-API test: ");
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + logfile);
        terminate("Functional test JAVA-API did not work");
      }
      try {
        log0(lvl, "trying to run org.sikuli.script.SikuliX.testSetup()");
        loader.doSomethingSpecial("itIsJython", null); // export Lib folder
        if (getTess) {
          loader.doSomethingSpecial("exportTessdata", null); // export tessdata folder
        }
        Class sysclass = URLClassLoader.class;
        Class SikuliCL = sysclass.forName("org.sikuli.script.SikuliX");
        log0(lvl, "class found: " + SikuliCL.toString());
        Method method = SikuliCL.getDeclaredMethod("testSetup", new Class[0]);
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
                + "Check the error log at " + logfile);
        terminate("Functional test JAVA-API did not work");
      }
    }
    if (getIDE || getScript) {
      log1(lvl, "Trying to run functional test: running Jython statements via SikuliScript");
      splash = showSplash("Trying to run functional test(s)", "running Jython statements via SikuliScript");
      if (!SikuliX.addToClasspath(localTestJar)) {
        closeSplash(splash);
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + logfile);
        terminate("Functional test Jython did not work");
      }
      if (getTess) {
        loader.doSomethingSpecial("exportTessdata", null); // export tessdata folder
      }
      String testSetupSuccess = "Setup: Sikuli seems to work! Have fun!";
      log0(lvl, "trying to run testSetup.sikuli using SikuliScript");
      try {
        String testargs[] = new String[]{"-testSetup", "jython", "popup(\"" + testSetupSuccess + "\")"};
        closeSplash(splash);
        SikuliScript.main(testargs);
        if (null == testargs[0]) {
          throw new Exception("testSetup ran with problems");
        }
      } catch (Exception ex) {
        closeSplash(splash);
        log0(-1, ex.getMessage());
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + logfile);
        terminate("Functional test Jython did not work");
      }
    }
    splash = showSplash("Setup seems to have ended successfully!", "Detailed information see: " + logfile);
    start += 2000;

    closeSplash(splash);

    log1(lvl,
            "... SikuliX Setup seems to have ended successfully ;-)");
    //</editor-fold>

    System.exit(0);
  }

  public static boolean isRunningUpdate() {
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

  protected static void restore() {
    log1(lvl, "restoring from backup");
    String backup = new File(workDir, "Backup").getAbsolutePath();
    if (new File(backup, localIDE).exists() && !new File(workDir, localIDE).exists()) {
      log1(lvl, "restoring " + localIDE);
      new File(backup, localIDE).renameTo(new File(workDir, localIDE));
    }
    if (new File(backup, localScript).exists() && !new File(workDir, localScript).exists()) {
      log1(lvl, "restoring " + localScript);
      new File(backup, localScript).renameTo(new File(workDir, localScript));
    }
    if (new File(backup, localJava).exists() && !new File(workDir, localJava).exists()) {
      log1(lvl, "restoring " + localJava);
      new File(backup, localJava).renameTo(new File(workDir, localJava));
    }
    if (new File(backup, localTess).exists() && !new File(workDir, localTess).exists()) {
      log1(lvl, "restoring " + localTess);
      new File(backup, localTess).renameTo(new File(workDir, localTess));
    }
    if (new File(backup, localRServer).exists() && !new File(workDir, localRServer).exists()) {
      log1(lvl, "restoring " + localRServer);
      new File(backup, localRServer).renameTo(new File(workDir, localRServer));
    }
  }

  private static void reset(int type) {
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
    FileManager.deleteFileOrFolder(backup, new FileManager.fileFilter() {
      @Override
      public boolean accept(File entry) {
        return true;
      }
    });
    try {
      FileManager.xcopyAll(workDir, backup);
    } catch (IOException ex) {
      popError("Reset: Not possible to backup:\n" + ex.getMessage());
      terminate("Reset: Not possible to backup:\n" + ex.getMessage());
    }
    FileManager.deleteFileOrFolder(workDir, new FileManager.fileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().startsWith("run")) {
          return false;
        } else if (entry.getName().equals(localSetup)) {
          return false;
        } else if (isUpdate && entry.getName().equals(localIDE)) {
          return false;
        } else if (isUpdate && entry.getName().equals(localScript)) {
          return false;
        } else if (isUpdate && entry.getName().equals(localJava)) {
          return false;
        } else if (isUpdate && entry.getName().equals(localTess)) {
          return false;
        } else if (isUpdate && entry.getName().equals(localRServer)) {
          return false;
        } else if (workDir.equals(entry.getAbsolutePath())) {
          return false;
        } else if ("BackUp".equals(entry.getName())) {
          return false;
        } else if ("Downloads".equals(entry.getName())) {
          return false;
        } else if (entry.getName().contains("SetupLog")) {
          return false;
        } else if (entry.getName().startsWith(localUpdate)) {
          return false;
        }
        return true;
      }
    });
    closeSplash(splash);
    log1(3, "completed!");
  }

  public static void helpOption(int option) {
    String m;
    String om = "";
    m = "\n-------------------- Some Information on this option, that might "
            + "help to decide, wether to select it ------------------";
    switch (option) {
      case (1):
        om = "Package 1: You get the Sikuli IDE which supports all usages of Sikuli";
//              -------------------------------------------------------------
        m += "\nIt is the largest package of course ...";
        m += "\nIt is recommended for people new to Sikuli "
                + "and those who want to develop scripts with the Sikuli IDE";
        m += "\n\nFor those who know ;-) additionally you can ...";
        m += "\n- use it to run scripts from commandline";
        m += "\n- develop Java programs with Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n- develop in any Java aware scripting language adding Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n\nJython developement: special info:";
        m += "\n If you want to use standalone Jython in parallel, you should select Pack 3 additionally (Option 3)";
        m += "\n\nTo understand the differences, it might be helpful to read the other informations too (Pack 2 and Pack 3)";
        m += "\nBut again: If you want to run scripts from command line, this IDE package is able to do it the same way as Pack 2";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        if (Settings.isMac()) {
          m += "\n\nSpecial info for Mac systems:";
          m += "\nFinally you will have a Sikuli-IDE.app in the setup working folder.";
          m += "\nTo use it, just move it into the Applications folder.";
          m += "\nIf you need to run stuff from commandline or want to use Sikuli with Java,";
          m += "\nyou have the following additionally in the setup folder:";
          m += "\nrunIDE: the shellscript to run scripts and";
          m += "\nsikuli-ide.jar: everything you need for integration with Java developement";
        }
        break;
      case (2):
        om = "Package 2: To allow to run Sikuli scripts from command line (no IDE)"
                + "\n\n... make sure Option 1 (IDE) is not selected, if you really want this now!"
                + "\nIf you really want it too on the same machine, run setup again afterwards from a different folder";
//              -------------------------------------------------------------
        m += "\nThe primary pupose of this package: run Sikuli scripts from command line ;-)";
        m += "\nIt should be used on machines, that only run scripts and where is no need"
                + " to have the IDE or it is even not wanted to have it";
        m += "\n\nFor those who know ;-) additionally you can ...";
        m += "\n- develop Java programs with Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n- develop in any Java aware scripting language adding Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n\nJython developement: special info:";
        m += "\n If you want to use standalone Jython in parallel, you should select Pack 3 additionally (Option 3)";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        break;
      case (3):
        om = "Package 3: ... in addition to Package 1 or Package 2 for use with Jython";
//              -------------------------------------------------------------
        m += "\nThis package is of interest, if you plan to develop Jython scripts outside of the"
                + " SikuliX environment using your own standalon Jython or other IDE's";
        m += "\nThe advantage: since it does not contain the Jython interpreter package, there"
                + " cannot be any collisions on the Python path.";
        m += "\n\nIt contains the Sikuli Jython API, adds itself to Python path at runtime"
                + "\nand exports the Sikuli Python modules to the folder libs/Libs"
                + " that helps to setup the auto-complete in IDE's like NetBeans, Eclipse ...";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        break;
      case (5):
        om = "Package 3: To support developement in Java or any Java aware scripting language"
                + "\n\n( ... make sure neither Option 1 (IDE) nor Option 2 (Script) is selected!"
                + "\nIf you want it additionally to IDE or Script, use the previous Option 3!)";
//              -------------------------------------------------------------
        m += "\nThe content of this package is stripped down to what is needed to develop in Java"
                + " or any Java aware scripting language \n(no IDE, no bundled script run support for Jython)";
        m += "\n\nHence this package is not runnable and must be in the class path to use it"
                + " for developement or at runtime";
        m += "\n\nSpecial info for usage with Jython: It contains the Sikuli Jython API ..."
                + "\n... and adds itself to Python path at runtime"
                + "\n... and exports the Sikuli Python modules to the folder libs/Libs at runtime"
                + "\nthat helps to setup the auto-complete in IDE's like NetBeans, Eclipse ...";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        break;
      case (4):
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
      case (6):
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
      case (7):
        om = "To try out the experimental remote feature";
//              -------------------------------------------------------------
        m += "\nYou might start the downloaded jar on any system, that is reachable "
                + "\nby other systems in your network via TCP/IP (hostname or IP-address)."
                + "\nusing: java -jar sikulix-remoteserver.jar"
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
    if (option == 4 || option == 5) {
      option = option == 4 ? 5 : 4;
    }
    popInfo("asking for option " + option + ": " + om + "\n" + m);
  }

  public static void popError(String msg) {
    log0(-1, msg);
    JOptionPane.showMessageDialog(null, msg, "SikuliX-Setup: having problems ...", JOptionPane.ERROR_MESSAGE);
  }

  public static void popInfo(String msg) {
    JOptionPane.showMessageDialog(null, msg, "SikuliX-Setup: info ...", JOptionPane.PLAIN_MESSAGE);
  }

  public static boolean popAsk(String msg) {
    int ret = JOptionPane.showConfirmDialog(null, msg, "SikuliX-Setup: ... want to proceed? ", JOptionPane.YES_NO_OPTION);
    if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
      return false;
    }
    return true;
  }

  public static JFrame showSplash(String title, String msg) {
    start = (new Date()).getTime();
    return new MultiFrame(new String[]{"splash", "# " + title, "#... " + msg});
  }

  public static void closeSplash(JFrame splash) {
    long elapsed = (new Date()).getTime() - start;
    if (elapsed < 3000) {
      try {
        Thread.sleep(3000 - elapsed);
      } catch (InterruptedException ex) {
      }
    }
    splash.dispose();
  }

  private static boolean download(String sDir, String tDir, String item, String jar) {
    File downloaded = new File(workDir, "Downloads/" + item);
    if (downloaded.exists()) {
      if (popAsk("You already have this in your Setup/Downloads folder:\n"
              + downloaded.getAbsolutePath()
              + "\nClick YES, if you want to use this for setup processing\n\n"
              + "... or click NO, to download a fresh copy")) {
        try {
          FileManager.xcopy(downloaded.getAbsolutePath(), jar, null);
        } catch (IOException ex) {
          terminate("Unable to copy from local Downloads: "
                  + downloaded.getAbsolutePath() + "\n" + ex.getMessage());
        }
        log(lvl, "Copied form local Download: " + item);
        return true;
      }
    }
    JFrame progress = new MultiFrame("download");
    String fname = FileManager.downloadURL(sDir + item, tDir, progress);
    progress.dispose();
    if (null == fname) {
      log1(-1, "Fatal error 001: not able to download: %s", item);
      return false;
    }
    if (!(new File(tDir, item)).renameTo(new File(jar))) {
      log1(-1, "rename to %s did not work", jar);
      return false;
    }
    return true;
  }

  private static void userTerminated(String msg) {
    if (!msg.isEmpty()) {
      log0(lvl, msg);
    }
    log1(lvl, "User requested termination.");
    System.exit(0);
  }

  private static void terminate(String msg) {
    log1(-1, msg);
    log1(-1, "... terminated abnormally :-(");
    popError("Something serious happened! Sikuli not useable!\n"
            + "Check the error log at " + logfile);
    System.exit(1);
  }
}
