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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.sikuli.script.RunTime;
import org.sikuli.basics.Settings;
import org.sikuli.script.Sikulix;
import org.sikuli.util.LinuxSupport;
import static org.sikuli.util.LinuxSupport.buildVision;

public class RunSetup {

  private static String downloadedFiles;
  private static boolean noSetup = false;
  private static boolean backUpExists = false;
  private static String workDir;
  private static File fWorkDir;
  private static String logfile;
  private static String version;
//TODO wrong if version number parts have more than one digit
  private static String minorversion;
  private static String majorversion;
  private static String updateVersion;
  private static String downloadMavenSnapshot = "";
  private static String downloadMavenRelease = "";
  private static String downloadIDE;
  private static String downloadAPI;
  private static String downloadRServer;
  private static String downloadJython;
  private static String downloadJRuby;
  private static String downloadJRubyAddOns;
  private static String downloadMacAppSuffix = "-9.jar";
  private static String downloadTessSuffix = "-8.jar";
  private static String localAPI = "sikulixapi.jar";
  private static String localIDE = "sikulix.jar";
  private static String setupName;
  private static String localSetup;
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
  private static boolean hasOptions = false;
  private static String runningJar;
  private static List<String> options = new ArrayList<String>();
  private static JFrame splash = null;
  private static String me = "RunSetup";
  private static int lvl = 2;
  private static String msg;
  private static boolean shouldPackLibs = true;
  private static long start;
  private static boolean runningSetup = false;
  private static boolean logToFile = true;
  private static boolean forSystemWin = false;
  private static boolean forSystemMac = false;
  private static boolean forSystemLux = false;
  private static String libsMac = "sikulixlibsmac";
  private static String libsWin = "sikulixlibswin";
  private static String libsLux = "sikulixlibslux";
  private static String apiJarName = "sikulixapi";
  private static File folderLibs;
  private static File folderLibsWin;
  private static File folderLibsLux;
  private static String linuxDistro = "*** testing Linux ***";
  private static String osarch;
//TODO set true to test on Mac
  private static boolean isLinux = false;

  // -MF $externals/$fn.o.d -o $externals/$fn.o $src/Vision/$fn
  private static boolean libsProvided = false;
  private static String[] addonFileList = new String[]{null, null, null, null, null};
  private static String[] addonFilePrefix = new String[]{null, null, null, null, null};
  private static int addonVision = 0;
  private static int addonGrabKey = 1;
  private static int addonWindows = 2;
  private static boolean libSearched = false;
  private static boolean checkSuccess = true;
  private static boolean shouldBuildVision = false;
  private static boolean notests = false;
  private static boolean clean = false;
  private static File winLibs = null;
  private static RunTime runTime;
  private static Map<String, String> addFilesToJar = new HashMap<String, String>();

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static void logp(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + message, args);
  }

  private static void logPlus(int level, String message, Object... args) {
    String sout = Debug.logx(level, me + ": " + message, args);
    if (logToFile) {
      System.out.println(sout);
    }
  }
//</editor-fold>

  public static void main(String[] args) throws IOException {

    runTime = RunTime.get(RunTime.Type.SETUP);

//    logp("**** command line args: %d", args.length);
//    if (args.length > 0) {
//      int i = 0;
//      for (String arg : args) {
//        logp("%3d: %s", i++, arg);
//      }
//    }
    version = runTime.getVersionShort();
//TODO wrong if version number parts have more than one digit
    minorversion = runTime.getVersionShort().substring(0, 5);
    majorversion = runTime.getVersionShort().substring(0, 3);
    downloadIDE = version + "-1.jar";
    downloadAPI = version + "-2.jar";
    downloadRServer = version + "-3.jar";
    downloadJython = new File(runTime.SikuliJythonMaven).getName();
    downloadJRuby = new File(runTime.SikuliJRubyMaven).getName();
    downloadJRubyAddOns = version + "-6.jar";
    setupName = "sikulixsetup-" + version;
    localSetup = setupName + ".jar";

    CodeSource codeSrc = RunSetup.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      codeSrc.getLocation();
    } else {
      log(-1, "Fatal Error 201: Not possible to accessjar file for RunSetup.class");
      Sikulix.terminate(201);
    }

    if (runTime.SikuliVersionBetaN > 0 && runTime.SikuliVersionBetaN < 99) {
      updateVersion = String.format("%d.%d.%d-Beta%d",
              runTime.SikuliVersionMajor, runTime.SikuliVersionMinor, runTime.SikuliVersionSub,
              1 + runTime.SikuliVersionBetaN);
    } else if (runTime.SikuliVersionBetaN < 1) {
      updateVersion = String.format("%d.%d.%d",
              runTime.SikuliVersionMajor, runTime.SikuliVersionMinor,
              1 + runTime.SikuliVersionSub);
    } else {
      updateVersion = String.format("%d.%d.%d",
              runTime.SikuliVersionMajor, 1 + runTime.SikuliVersionMinor, 0);
    }

    options.addAll(Arrays.asList(args));

    //<editor-fold defaultstate="collapsed" desc="options return version">
    if (args.length > 0 && "build".equals(args[0])) {
      System.out.println(runTime.SikuliVersionBuild);
      System.exit(0);
    }

    if (args.length > 0 && "pversion".equals(args[0])) {
      System.out.println(runTime.SikuliProjectVersion);
      System.exit(0);
    }

    if (args.length > 0 && "uversion".equals(args[0])) {
      System.out.println(runTime.SikuliProjectVersionUsed);
      System.exit(0);
    }

    if (args.length > 0 && "version".equals(args[0])) {
      System.out.println(runTime.getVersionShort());
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

    if (options.size() > 0 && "noSetup".equals(options.get(0))) {
      noSetup = true;
      options.remove(0);
    }

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
            notests = true;
          } else if (val.toLowerCase().startsWith("clean")) {
            clean = true;
          }
        }
        options.clear();
      }
    }

    runningJar = runTime.fSxBaseJar.getAbsolutePath();

    localLogfile = "SikuliX-" + version + "-SetupLog.txt";

    if (options.size() > 0) {
      popError("invalid command line options - terminating");
      System.exit(999);
    }

    //<editor-fold defaultstate="collapsed" desc="general preps">
    Settings.runningSetup = true;
    Settings.LogTime = true;

    if (runTime.runningInProject) {
      fWorkDir = new File(runTime.fSxBase, "Setup");
    } else {
      fWorkDir = runTime.fSxBase;
    }
    workDir = fWorkDir.getAbsolutePath();

    osarch = "" + runTime.javaArch;
    if (runTime.runningLinux) {
      linuxDistro = LinuxSupport.getLinuxDistro();
      logPlus(lvl, "LinuxDistro: %s (%s-Bit)", linuxDistro, osarch);
      isLinux = true;
    }
    
    if (runTime.runningInProject) {
      if (!hasOptions || clean) {
        if (noSetup) {
          log(lvl, "creating Setup folder - not running setup");
        } else {
          log(lvl, "have to create Setup folder before running setup");
        }
        if (!createSetupFolder(workDir)) {
          log(-1, "createSetupFolder: did not work- terminating");
          System.exit(1);
        }
        if (noSetup) {
          System.exit(0);
        }
        logToFile = false;
      }
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
      logPlus(lvl, "... starting with: " + Sikulix.arrayToString(args));
    } else {
      logPlus(lvl, "... starting with no args given");
    }

    logPlus(lvl, "Setup: %s %s in folder:\n%s", runTime.getVersionShort(), runTime.SikuliVersionBuild, workDir);

    File localJarIDE = new File(workDir, localIDE);
    File localJarAPI = new File(workDir, localAPI);

    folderLibs = new File(workDir, "Downloads/sikulixlibs");
    folderLibsWin = new File(folderLibs, "windows");
    folderLibsLux = new File(folderLibs, "linux");
    LinuxSupport.setWorkDir(workDir);
    LinuxSupport.setLibsDir(folderLibsLux);
    LinuxSupport.setLogToFile(logToFile);

    //TODO Windows 8 HKLM/SOFTWARE/JavaSoft add Prefs ????
    boolean success;
    if (!libsProvided && LinuxSupport.existsLibs()) {
      if (popAsk(String.format("Found a libs folder at\n%s\n"
              + "Click YES to use the contained libs "
              + "for setup (be sure they are useable).\n"
              + "Click NO to make a clean setup (libs are deleted).", folderLibsLux))) 
      {
        libsProvided = true;
      } else {
        FileManager.resetFolder(folderLibsLux);
      }
    }

    if (!hasOptions) {
      if (localJarIDE.exists() || localJarAPI.exists()) {
        String ask1 = "You have " + runTime.getVersion()
                + "\nClick YES if you want to run setup again\n"
                + "This will download fresh versions of the selected stuff.\n"
                + "Your current stuff will be saved to folder BackUp.\n\n"
                + "If you cancel the setup later or it is not successful\n"
                + "the saved stuff will be restored from folder BackUp\n\n";
        if (!popAsk(ask1)) {
          userTerminated("Do not run setup again");
        }
        reset(-1);
      }
    } else {
      if (!clean) {
        reset(-1);
      }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="display setup options">
    String proxyMsg = "";

    if (!hasOptions) {
      getIDE = false;
      getJython = false;
      getAPI = false;
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
      winSU.suVersion.setText(runTime.getVersionShort() + "   (" + runTime.SikuliVersionBuild + ")");

      // running system
      Settings.getOS();
      msg = runTime.osName + " " + Settings.getOSVersion();
      if (isLinux) {
        msg += " (" + linuxDistro + ")";
      }
      winSU.suSystem.setText(msg);
      logPlus(lvl, "RunningSystem: " + msg);

      // folder running in
      winSU.suFolder.setText(workDir);
      logPlus(lvl, "parent of jar/classes: %s", workDir);

      // running Java
      String osarch = System.getProperty("os.arch");
      msg = "Java " + Settings.JavaVersion + " (" + Settings.JavaArch + ") " + Settings.JREVersion;
      winSU.suJava.setText(msg);
      logPlus(lvl, "RunningJava: " + msg);

      PreferencesUser prefs = PreferencesUser.getInstance();
      boolean prefsHaveProxy = false;
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
          logPlus(lvl, "Requested to run with proxy: %s ", Settings.proxy);
          proxyMsg = "... using proxy: " + Settings.proxy;
        }
      } else if (prefsHaveProxy) {
        prefs.put("ProxyName", "");
        prefs.put("ProxyPort", "");
      }
      Settings.proxyChecked = true;
      //</editor-fold>

//			File fPrefs = new File(workDir, "SikuliPrefs.txt");
//			prefs.exportPrefs(fPrefs.getAbsolutePath());
//			BufferedReader pInp = null;
//			try {
//				pInp = new BufferedReader(new FileReader(fPrefs));
//				String line;
//				while (null != (line = pInp.readLine())) {
//					if (!line.contains("entry")) {
//						continue;
//					}
//					if (logToFile) {
//						log(lvl, "Prefs: " + line.trim());
//					}
//				}
//				pInp.close();
//			} catch (Exception ex) {
//			}
//			FileManager.deleteFileOrFolder(fPrefs.getAbsolutePath());
      

//<editor-fold defaultstate="collapsed" desc="evaluate setup options">
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
    }
    
    downloadedFiles = "";
    if (getIDE || getAPI || getRServer) {
      
      if (!proxyMsg.isEmpty()) {
        msg += proxyMsg + "\n";
      }
      if (forAllSystems)
        msg += "\n--- Native support libraries for all systems (sikulixlibs...)\n";
      else {
        msg += "\n--- Native support libraries for " + runTime.osName + " (sikulixlibs...)\n";
      }
      if (getIDE) {
        downloadedFiles += downloadIDE + " ";
        downloadedFiles += downloadAPI + " ";
        msg += "\n--- Package 1 ---\n"
                + downloadIDE + " (IDE/Scripting)\n"
                + downloadAPI + " (Java API)";
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
        if (Settings.isMac()) {
          msg += "\n - creating Mac application";
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
//</editor-fold>
    
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
      if (!isLinux) {
        forSystemWin = Settings.isWindows();
        forSystemMac = Settings.isMac();
      }
    }
    File libDownloaded;

    //<editor-fold defaultstate="collapsed" desc="we are on Linux">
    if (forSystemLux || forAllSystems) {
      jarsList[8] = new File(workDir, libsLux + ".jar").getAbsolutePath();
      libDownloaded = new File(dlDir, libsLux + "-" + version + ".jar");
      if (!takeAlreadyDownloaded(libDownloaded, libsLux)) {
        downloadOK &= getSikulixJarFromMaven(libsLux, dlDir, null, libsLux);
      } else {
        copyFromDownloads(libDownloaded, libsLux, jarsList[8]);
      }
      if (isLinux) {
        boolean shouldBuildVisionNow = LinuxSupport.processLibs1(jarsList[8], osarch);
        libsProvided = LinuxSupport.processLibs2();
        boolean shouldTerminate = false;
        if (shouldBuildVisionNow) {
          logPlus(-1, "A bundled lib could not be checked or does not work.");
          if (!popAsk("The bundled/provided libVisionProxy.so might not work."
                  + "\nShould we try to build it now?"
                  + "\nClick YES to try a build"
                  + "\nClick NO to terminate and correct the problems.")) {
            shouldTerminate = true;
            shouldBuildVisionNow = false;
          }
        }
        if (shouldBuildVisionNow || (hasOptions && shouldBuildVision)) {
          logPlus(lvl, "Trying to build libVisionProxy.so");
          libsProvided = buildVision(jarsList[8]);
          shouldTerminate |= !libsProvided;
        }
        if (!libsProvided) {
          FileManager.deleteFileOrFolder(folderLibsLux.getAbsolutePath());
        }
        if (shouldTerminate) {
          terminate("Correct the problems with the bundled/provided libs and try again");
        }
      }
    }
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="other downloads">
		if (forSystemWin || forAllSystems) {
			jarsList[6] = new File(workDir, libsWin + ".jar").getAbsolutePath();
			libDownloaded = new File(dlDir, libsWin + "-" + version + ".jar");
			if (!takeAlreadyDownloaded(libDownloaded, libsWin)) {
				downloadOK &= getSikulixJarFromMaven(libsWin, dlDir, null, libsWin);
			} else {
				copyFromDownloads(libDownloaded, libsWin, jarsList[6]);
			}

      if (!forAllSystems) {
        String libsWin = "sikulixlibs/windows";     
        folderLibsWin = new File(workDir, libsWin);
        FileManager.resetFolder(folderLibsWin);
        String aJar = "/" + FileManager.slashify(jarsList[6], false);
        if (null == runTime.resourceListAsSikulixContentFromJar(aJar, libsWin, folderLibsWin, null)) {
          terminate("libswin content list not created", 999);
        }
        addonFileList[addonWindows] = new File(folderLibsWin, runTime.fpContent).getAbsolutePath();
        addonFilePrefix[addonWindows] = libsWin;
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

    if (getIDE || getAPI) {
      localJar = new File(workDir, localAPI).getAbsolutePath();
      downloadOK &= download(runTime.downloadBaseDir, dlDir, downloadAPI, localJar, "Java API");
    }
    if (getIDE) {
      localJar = new File(workDir, localIDE).getAbsolutePath();
      dlOK = download(runTime.downloadBaseDir, dlDir, downloadIDE, localJar, "IDE/Scripting");
      downloadOK &= dlOK;
    }
    if (getJython) {
      targetJar = new File(workDir, localJython).getAbsolutePath();
      if (Settings.isJava6()) {
        logPlus(lvl, "running on Java 6: need to use Jython 2.5 - which is downloaded");
        downloadOK &= getJarFromMaven(runTime.SikuliJythonMaven25, dlDir, targetJar, "Jython");
      } else {
        downloadOK &= getJarFromMaven(runTime.SikuliJythonMaven, dlDir, targetJar, "Jython");
      }
    }
    if (getJRuby) {
      targetJar = new File(workDir, localJRuby).getAbsolutePath();
      downloadOK &= getJarFromMaven(runTime.SikuliJRubyMaven, dlDir, targetJar, "JRuby");
      if (downloadOK && getJRubyAddOns) {
        targetJar = new File(workDir, localJRubyAddOns).getAbsolutePath();
        downloadOK &= download(runTime.downloadBaseDir, dlDir, downloadJRubyAddOns, targetJar, "JRubyAddOns");
      }
    }
    if (getRServer) {
      targetJar = new File(workDir, localRServer).getAbsolutePath();
      downloadOK = download(runTime.downloadBaseDir, dlDir, downloadRServer, targetJar, "RemoteServer");
      downloadOK &= dlOK;
    }
		//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="download for tesseract">
    if (getTess) {
      String langTess = "eng";
      targetJar = new File(workDir, localTess).getAbsolutePath();
      String xTess = runTime.tessData.get(langTess);
      String[] xTessNames = xTess.split("/");
      String xTessName = xTessNames[xTessNames.length - 1];
      String tessFolder = "tessdata-" + langTess;
      downloadOK &= download(xTess, dlDir, null, "nocopy", null);
      log(lvl, "trying to extract from: %s", xTessName);
      Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
      archiver.extract(new File(dlDir, "tesseract-ocr-3.02.eng.tar.gz"), new File(dlDir));
      File fTess = new File(dlDir, "tesseract-ocr/tessdata");
      if (!fTess.exists()) {
        logPlus(-1, "Download: tessdata: version: eng - did not work");
        downloadOK = false;
      } else {
        File fTessData = new File(dlDir, tessFolder);
        log(lvl, "preparing the tessdata stuff in:\n%s", fTessData.getAbsolutePath());
        FileManager.resetFolder(fTessData);
        FileManager.xcopy(fTess.getAbsolutePath(), fTessData.getAbsolutePath());
        FileManager.deleteFileOrFolder(fTess.getParent());
        runTime.extractResourcesToFolder("sikulixtessdata", fTessData, null);
        log(lvl, "finally preparing %s", localTess);
        fTargetJar = (new File(workDir, localTemp));
        targetJar = fTargetJar.getAbsolutePath();
        String tessJar = new File(workDir, localTess).getAbsolutePath();

        if (runTime.runningWindows) {
          success = runTime.addToClasspath(fTessData.getParent());
          runTime.resourceListAsSikulixContent(tessFolder, fTessData, null);
        }

        downloadOK &= FileManager.buildJar("#" + targetJar, new String[]{},
                new String[]{fTessData.getAbsolutePath()},
                new String[]{"sikulixtessdata"}, null);
        downloadOK &= handleTempAfter(targetJar, tessJar);

        FileManager.deleteFileOrFolder(fTessData.getAbsolutePath());
      }
    }
    //</editor-fold>

    if (!downloadedFiles.isEmpty()) {
      logPlus(lvl, "Download ended");
      logPlus(lvl, "Downloads for selected options:\n" + downloadedFiles);
      logPlus(lvl, "Download page: " + runTime.downloadBaseDirWeb);
    }
    if (!downloadOK) {
      popError("Some of the downloads did not complete successfully.\n"
              + "Check the logfile for possible error causes.\n\n"
              + "If you think, setup's inline download is blocked somehow on\n"
              + "your system, you might download the appropriate raw packages manually\n"
              + "into the folder Downloads in the setup folder and run setup again.\n\n"
              + "download page: " + runTime.downloadBaseDirWeb + "\n"
              + "files to download (information is in the setup log file too)\n"
              + downloadedFiles
              + "\n\nBe aware: The raw packages are not useable without being processed by setup!\n\n"
              + "For other reasons, you might simply try to run setup again.");
      terminate("download not completed successfully");
    }

    //<editor-fold defaultstate="collapsed" desc="create jars and add needed stuff">
    if (!getIDE && !getAPI) {
      logPlus(lvl, "Nothing else to do");
      System.exit(0);
    }

    if (isLinux) {
      if (libsProvided) {
        shouldPackLibs = false;
      }
      if (!shouldPackLibs) {
        addonFileList[addonVision] = new File(folderLibsLux, LinuxSupport.libVision).getAbsolutePath();
        addonFileList[addonGrabKey] = new File(folderLibsLux, LinuxSupport.libGrabKey).getAbsolutePath();
        for (int i = 0; i < 2; i++) {
          if (!new File(addonFileList[i]).exists()) {
            addonFileList[i] = null;
          }
        }
        String libPrefix = "sikulixlibs/linux/libs" + osarch;
        log(lvl, "Provided libs will be stored at %s", libPrefix);
        addonFilePrefix[addonVision] = libPrefix;
        addonFilePrefix[addonGrabKey] = libPrefix;
      }
    }

    success = true;
    FileManager.JarFileFilter libsFilter = new FileManager.JarFileFilter() {
      @Override
      public boolean accept(ZipEntry entry, String jarname) {
        if (!forAllSystems) {
          if (forSystemWin) {
            if (entry.getName().startsWith("sikulixlibs/mac")
                    || entry.getName().startsWith("sikulixlibs/linux")
                    || entry.getName().endsWith("sikulixfoldercontent")
                    || entry.getName().startsWith("jxgrabkey")) {
              return false;
            }
          } else if (forSystemMac) {
            if (entry.getName().startsWith("sikulixlibs/windows")
                    || entry.getName().startsWith("sikulixlibs/linux")
                    || entry.getName().startsWith("com.melloware.jintellitype")
                    || entry.getName().startsWith("jxgrabkey")) {
              return false;
            }
          } else if (forSystemLux) {
            if (entry.getName().startsWith("sikulixlibs/windows")
                    || entry.getName().startsWith("sikulixlibs/mac")
                    || entry.getName().startsWith("com.melloware.jintellitype")) {
              return false;
            }
          }
        }
        if (forSystemLux || forAllSystems) {
          if (!shouldPackLibs && entry.getName().contains(LinuxSupport.libVision)
                  && entry.getName().contains("libs" + osarch)) {
            if (new File(folderLibsLux, LinuxSupport.libVision).exists()) {
              log(lvl, "Found provided lib: %s (libs%s)", LinuxSupport.libVision, osarch);
              return false;
            } else {
              return true;
            }
          }
          if (!shouldPackLibs && entry.getName().contains(LinuxSupport.libGrabKey)
                  && entry.getName().contains("libs" + osarch)) {
            if (new File(folderLibsLux, LinuxSupport.libGrabKey).exists()) {
              log(lvl, "Found provided lib: %s (libs%s)", LinuxSupport.libGrabKey, osarch);
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

    if (forSystemWin) {

    }

    if (success && getAPI) {
      logPlus(lvl, "adding needed stuff to sikulixapi.jar");
      localJar = (new File(workDir, localAPI)).getAbsolutePath();
      targetJar = (new File(workDir, localTemp)).getAbsolutePath();
      success &= FileManager.buildJar("#" + targetJar, jarsList,
              addonFileList, addonFilePrefix, libsFilter);
      addonFileList[addonWindows] = null;
      addonFilePrefix[addonWindows] = null;
      success &= handleTempAfter(targetJar, localJar);
    }

    if (success && getIDE) {
      logPlus(lvl, "adding needed stuff to sikulix.jar");
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
              targetJar, jarsList, addonFileList, addonFilePrefix, libsFilter);
      success &= handleTempAfter(targetJar, localJar);

      if (Settings.isMac()) {
        logPlus(lvl, "making the Mac application Sikulix.app");
        String macAppContentOrg = "macapp";
        File fMacApp = new File(workDir, "SikuliX.app");
        if (null == runTime.extractResourcesToFolder(macAppContentOrg, fMacApp, null)) {
          log(-1, "did not work");
        } else {
          File fMacAppjar = new File(fMacApp, "Contents/Java/" + localIDE);
          new File(fMacApp, "run").setExecutable(true);
          new File(fMacApp, "Contents/MacOS/JavaAppLauncher").setExecutable(true);
          fMacAppjar.getParentFile().mkdirs();
          FileManager.xcopy(new File(localJar), fMacAppjar);
        }
      }
    }

    for (int i = (getAPI ? 2 : 1); i < jarsList.length; i++) {
      if (jarsList[i] != null) {
        new File(jarsList[i]).delete();
      }
    }
    closeSplash(splash);

    if (success && getIDE && !Settings.isMac()) {
      logPlus(lvl, "processing commandfiles");
      splash = showSplash("Now processing commandfiles.", "please wait - may take some seconds ...");
      if (Settings.isWindows()) {
        runTime.extractResourceToFile("Commands/windows", runsikulix + ".cmd", fWorkDir);
      } else if (isLinux) {
        runTime.extractResourceToFile("Commands/linux", runsikulix, fWorkDir);
        new File(fWorkDir, runsikulix).setExecutable(true);
        new File(fWorkDir, localIDE).setExecutable(true);
      }
      closeSplash(splash);
    }
    if (!success) {
      popError("Bad things happened trying to add native stuff to selected jars --- terminating!");
      terminate("Adding stuff to jars did not work");
    }
    restore(true); //to get back the stuff that was not changed
    //</editor-fold>

    if (!notests && runTime.isHeadless()) {
      log(lvl, "Running headless --- skipping tests");
    }

    FileManager.deleteFileOrFolder(folderLibsWin);

    //<editor-fold defaultstate="collapsed" desc="api test">
    boolean runAPITest = false;
    if (getAPI && !notests && !runTime.isHeadless()) {
      logPlus(lvl, "Trying to run functional test: JAVA-API");
      splash = showSplash("Trying to run functional test(s)", "Java-API: org.sikuli.script.Sikulix.testSetup()");
      if (!runTime.addToClasspath(localJarAPI.getAbsolutePath())) {
        closeSplash(splash);
        log(-1, "Java-API test: ");
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + (logfile == null ? "printout" : logfile));
        terminate("Functional test JAVA-API did not work", 1);
      }
      try {
        log(lvl, "trying to run org.sikuli.script.Sikulix.testSetup()");
        runTime.makeLibsFolder();
        runTime.extractResourcesToFolderFromJar("sikulixapi.jar", "Lib", new File(fWorkDir, "Lib"), null);
        Class sysclass = URLClassLoader.class;
        Class SikuliCL = sysclass.forName("org.sikuli.script.Sikulix");
        log(lvl, "class found: " + SikuliCL.toString());
        Method method = null;
        if (hasOptions) {
          method = SikuliCL.getDeclaredMethod("testSetupSilent", new Class[0]);
        } else {
          method = SikuliCL.getDeclaredMethod("testSetup", new Class[0]);
        }
        log(lvl, "getMethod: " + method.toString());
        method.setAccessible(true);
        closeSplash(splash);
        log(lvl, "invoke: " + method.toString());
        Object ret = method.invoke(null, new Object[0]);
        if (!(Boolean) ret) {
          throw new Exception("testSetup returned false");
        }
      } catch (Exception ex) {
        closeSplash(splash);
        log(-1, ex.getMessage());
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + (logfile == null ? "printout" : logfile));
        terminate("Functional test Java-API did not work", 1);
      }
      runAPITest = true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ide test">
    if (getIDE && !notests && !runTime.isHeadless()) {
      success = true;
      if (!runTime.addToClasspath(localJarIDE.getAbsolutePath())) {
        closeSplash(splash);
        popError("Something serious happened! Sikuli not useable!\n"
                + "Check the error log at " + (logfile == null ? "printout" : logfile));
        terminate("Functional test IDE did not work", 1);
      }
      if (!runAPITest) {
        runTime.makeLibsFolder();
      }
      String testMethod;
      if (getJython) {
        if (hasOptions) {
          testMethod = "print \"testSetup: Jython: success\"";
        } else {
          testMethod = "Sikulix.testSetup(\"Jython Scripting\")";
        }
        logPlus(lvl, "Jython: Trying to run functional test: running script statements via SikuliScript");
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
          success &= false;
          log(-1, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test Jython did not work", 1);
        }
      }
      if (getJRuby) {
        if (hasOptions) {
          testMethod = "print \"testSetup: JRuby: success\"";
        } else {
          testMethod = "Sikulix.testSetup(\"JRuby Scripting\")";
        }
        logPlus(lvl, "JRuby: Trying to run functional test: running script statements via SikuliScript");
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
          success &= false;
          log(-1, "content of returned error's (%s) message:\n%s", ex, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test JRuby did not work", 1);
        }
      }
      if (success && Settings.isMac()) {
        popInfo("You now have the IDE as SikuliX.app\n"
                + "It is recommended to move SikuliX.app\n"
                + "to the /Applications folder.");
      }
    }
    //</editor-fold>

    if (!notests) {
      splash = showSplash("Setup seems to have ended successfully!",
              "Detailed information see: " + (logfile == null ? "printout" : logfile));
      start += 2000;

      closeSplash(splash);
    }

    log(lvl,
            "... SikuliX Setup seems to have ended successfully ;-)");

    System.exit(RunTime.testing ? 1 : 0);
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
    String projectDir = runTime.fSxProject.getAbsolutePath();
    boolean success = true;
    File fTargetDir = new File(path);
    if (fTargetDir.exists()) {
      FileManager.deleteFileOrFolder(fTargetDir,
              new FileManager.FileFilter() {
                @Override
                public boolean accept(File entry) {
                  if (entry.getPath().contains(addSeps("/Downloads/"))) {
                    if (entry.getName().contains("tess")) {
                      return false;
                    }
                    if (entry.getPath().contains(addSeps("/sikulixlibs/linux"))) {
                      return false;
                    }
                  }
                  return true;
                }
              });
    }
    fTargetDir.mkdirs();

    File fSetup = getProjectJarFile(projectDir, "Setup", "sikulixsetup", "-plain.jar");
    success &= fSetup != null;
    File fIDEPlus = getProjectJarFile(projectDir, "IDEPlus", "sikulix-plus", "-ide-fat.jar");
    success &= fIDEPlus != null;
    File fAPIPlus = getProjectJarFile(projectDir, "APIPlus", "sikulixapi-plus", "-plain.jar");
    success &= fAPIPlus != null;
    File fLibsmac = getProjectJarFile(projectDir, "Libsmac", libsMac, ".jar");
    success &= fLibsmac != null;
    File fLibswin = getProjectJarFile(projectDir, "Libswin", libsWin, ".jar");
    success &= fLibswin != null;
    File fLibslux = getProjectJarFile(projectDir, "Libslux", libsLux, ".jar");
    success &= fLibslux != null;

    File fJythonJar = new File(runTime.SikuliJython);
    if (!noSetup && !fJythonJar.exists()) {
      log(lvl, "createSetupFolder: missing: " + fJythonJar.getAbsolutePath());
      success = false;
    }
    File fJrubyJar = new File(runTime.SikuliJRuby);
    if (!noSetup && !fJrubyJar.exists()) {
      log(lvl, "createSetupFolder: missing " + fJrubyJar.getAbsolutePath());
      success = false;
    }

    if (success) {
      File fDownloads = new File(fTargetDir, "Downloads");
      fDownloads.mkdir();
      String fname = null;
      try {
        FileManager.xcopy(fSetup, new File(fTargetDir, localSetup));
        FileManager.xcopy(fIDEPlus, new File(fDownloads, downloadIDE));
        FileManager.xcopy(fAPIPlus, new File(fDownloads, downloadAPI));

        for (File fEntry : new File[]{fLibsmac, fLibswin, fLibslux}) {
          fname = fEntry.getAbsolutePath();
          FileManager.xcopy(fEntry, new File(fDownloads, fEntry.getName()));
        }

        if (!noSetup) {
          FileManager.xcopy(fJythonJar, new File(fDownloads, downloadJython));
          FileManager.xcopy(fJrubyJar, new File(fDownloads, downloadJRuby));
        }

//TODO JRubyAddOns
        String jrubyAddons = "sikulixjrubyaddons-" + runTime.SikuliProjectVersion + "-plain.jar";
        File fJRubyAddOns = new File(projectDir, "JRubyAddOns/target/" + jrubyAddons);
        fname = fJRubyAddOns.getAbsolutePath();
        if (fJRubyAddOns.exists()) {
//            FileManager.xcopy(fJRubyAddOns, new File(fDownloads, downloadJRubyAddOns));
        }
      } catch (Exception ex) {
        log(-1, "createSetupFolder: copying files did not work: %s", fname);
        success = false;
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
    return String.format("%s-%s%s", jarFilePre, runTime.SikuliProjectVersion, jarFileSuf);
  }

  private static boolean handleTempAfter(String temp, String target) {
    boolean success = true;
    logPlus(lvl, "renaming sikulixtemp.jar to target jar: %s", new File(target).getName());
    FileManager.deleteFileOrFolder("#" + target);
    success &= !new File(target).exists();
    if (success) {
      success &= (new File(temp)).renameTo(new File(target));
      if (!success) {
        logPlus(lvl, "rename did not work --- trying copy");
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
          logPlus(-1, "did not work");
          terminate("");
        }
      }
    }
    return success;
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
      logPlus(lvl, msgp);
      if (pp.isEmpty()) {
        popError(String.format("Proxy specification invalid: %s (%s)", pn, pp));
        logPlus(-1, "Terminating --- Proxy invalid");
        return false;
      } else {
        if (!popAsk(msgp)) {
          logPlus(-1, "Terminating --- User did not accept Proxy: %s %s", pn, pp);
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
      logPlus(-1, "User requested termination");
    }
    if (!backUpExists) {
      return;
    }
    String backup = new File(workDir, "Backup").getAbsolutePath();
    if (new File(backup, localIDE).exists() && !new File(workDir, localIDE).exists()) {
      log(lvl, "restoring from backup " + localIDE);
      new File(backup, localIDE).renameTo(new File(workDir, localIDE));
    }
    if (new File(backup, localAPI).exists() && !new File(workDir, localAPI).exists()) {
      log(lvl, "restoring from backup " + localAPI);
      new File(backup, localAPI).renameTo(new File(workDir, localAPI));
    }
    if (new File(backup, localTess).exists() && !new File(workDir, localTess).exists()) {
      log(lvl, "restoring from backup " + localTess);
      new File(backup, localTess).renameTo(new File(workDir, localTess));
    }
    if (new File(backup, localRServer).exists() && !new File(workDir, localRServer).exists()) {
      log(lvl, "restoring from backup " + localRServer);
      new File(backup, localRServer).renameTo(new File(workDir, localRServer));
    }
    String folder = "Lib";
    if (new File(backup, folder).exists() && !new File(workDir, folder).exists()) {
      log(lvl, "restoring from backup " + "folder " + folder);
      new File(backup, folder).renameTo(new File(workDir, folder));
    }
    folder = "libs";
    if (new File(backup, folder).exists() && !new File(workDir, folder).exists()) {
      log(lvl, "restoring from backup " + "folder " + folder);
      new File(backup, folder).renameTo(new File(workDir, folder));
    }
//    FileManager.deleteFileOrFolder(new File(workDir, "Backup").getAbsolutePath());
    FileManager.deleteFileOrFolder(new File(workDir, "SikuliPrefs.txt").getAbsolutePath());
  }

  private static void reset(int type) {
    if (!hasOptions) {
      logPlus(3, "requested to reset: " + workDir);
      String message = "";
      if (type <= 0) {
        message = "You decided to run setup again!\n";
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
        } else if (folderLibsLux.getAbsolutePath().equals(entry.getAbsolutePath())) {
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
    logPlus(3, "backup completed!");
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
    logPlus(3, "\npopError: " + packMessage(msg));
    if (!hasOptions) {
      Sikulix.popError(msg, "SikuliX-Setup: having problems ...");
    }
  }

  private static void popInfo(String msg) {
    logPlus(3, "\npopInfo: " + packMessage(msg));
    if (!hasOptions) {
      Sikulix.popup(msg, "SikuliX-Setup: info ...");
    }
  }

  private static boolean popAsk(String msg) {
    logPlus(3, "\npopAsk: " + packMessage(msg));
    if (hasOptions) {
      return true;
    }
    return Sikulix.popAsk(msg, "SikuliX-Setup: question ...");
  }

  private static JFrame showSplash(String title, String msg) {
    if (hasOptions) {
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
        logPlus(lvl, "SilentSetup: Downloading: %s", itemName);
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
    boolean develop = !runTime.isVersionRelease();
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
      mPath = runTime.dlMavenSnapshot + dlMavenSnapshotPrefix;
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
    return download(runTime.dlMavenRelease + src, target, null, targetJar, itemName);
  }

  private static void userTerminated(String msg) {
    if (!msg.isEmpty()) {
      logPlus(lvl, msg);
    }
    logPlus(lvl, "User requested termination.");
    System.exit(0);
  }

  private static void prepTerminate(String msg) {
    if (msg.isEmpty()) {
      restore(true);
    } else {
      logPlus(-1, msg);
      logPlus(-1, "... terminated abnormally :-(");
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
