/*
 * Copyright 2010-2015, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2015
 */
package org.sikuli.script;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.ResourceLoader;
import org.sikuli.basics.Settings;
import org.sikuli.basics.SysJNA;

/**
 * INTERNAL USE: Intended to concentrate all, that is needed at startup of sikulix or sikulixapi
 */
public class RunTime {

//<editor-fold defaultstate="collapsed" desc="logging">
  private final String me = "RunTime%s: ";
  private int lvl = 3;
  private int minLvl = lvl;
  private static String preLogMessages = "";

  private void log(int level, String message, Object... args) {
    Debug.logx(level, String.format(me, runType) + message, args);
  }

  private void logp(String message, Object... args) {
    if (runningWinApp) {
      log(0, message, args);
    } else {
      System.out.println(String.format(message, args));
    }
  }

  private void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }

  public void terminate(int retval, String msg) {
    log(-1, "*** terminating: " + msg);
    System.exit(retval);
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="instance">
  /**
   * INTERNAL USE
   */
  private RunTime() {
  }

  public enum Type {

    IDE, API, SETUP, INIT
  }

  private enum theSystem {

    WIN, MAC, LUX, FOO
  }

  /**
   * INTERNAL USE to initialize the runtime environment for SikuliX<br>
   * for public use: use RunTime.get() to get the existing instance
   *
   * @param typ IDE or API
   * @return the RunTime singleton instance
   */
  public static synchronized RunTime get(Type typ) {
    if (runTime == null) {
      runTime = new RunTime();
      debugLevelSaved = Debug.getDebugLevel();
      debugLogfileSaved = Debug.logfile;
      runTime.loadOptions(typ);
      int dl = runTime.getOptionNumber("debug.level");
      if (dl > 0 && Debug.getDebugLevel() < 2) {
        Debug.setDebugLevel(dl);
      }
      if (Type.SETUP.equals(typ)) {
        Debug.setDebugLevel(3);
      }
      if (Debug.getDebugLevel() > 1) {
        runTime.dumpOptions();
      }
      Settings.init(); // force Settings initialization
      runTime.init(typ);
      if (Type.IDE.equals(typ)) {
        runTime.initIDEbefore();
        runTime.initAPI();
        runTime.initIDEafter();
      } else {
        runTime.initAPI();
        if (Type.SETUP.equals(typ)) {
          runTime.initSetup();
        }
      }
    }
    if (testingWinApp && !runTime.runningWindows) {
      runTime.terminate(1, "***** for testing winapp: not running on Windows");
    }
    return runTime;
  }

  /**
   * get the initialized RunTime singleton instance
   *
   * @return
   */
  public static synchronized RunTime get() {
    if (runTime == null) {
      return get(Type.API);
    }
    return runTime;
  }

  /**
   * INTERNAL USE get a new initialized RunTime singleton instance
   *
   * @return
   */
  public static synchronized RunTime reset(Type typ) {
    if (runTime != null) {
      preLogMessages += "RunTime: resetting RunTime instance;";
      if (Sikulix.testNumber == 1) {
        Debug.setDebugLevel(debugLevelSaved);
      }
      Debug.setLogFile(debugLogfileSaved);
      runTime = null;
    }
    return get(typ);
  }

  /**
   * INTERNAL USE get a new initialized RunTime singleton instance
   *
   * @return
   */
  public static synchronized RunTime reset() {
    return reset(Type.API);
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="variables">
  private static RunTime runTime = null;
  private static int debugLevelSaved;
  private static String debugLogfileSaved;
  public static boolean testing = false;
  public static boolean testingWinApp = false;

  private Type runType = Type.INIT;

  public String sxBuild = "";
  public String sxBuildStamp = "";
  public String jreVersion = java.lang.System.getProperty("java.runtime.version");
  public Preferences optionsIDE = Preferences.userNodeForPackage(Sikulix.class);
  public ClassLoader classLoader = RunTime.class.getClassLoader();
  public String baseJar = "";
  public String userName = "";
  public String BaseTempPath = "";

  private Class clsRef = RunTime.class;
  private List<URL> classPath = new ArrayList<URL>();
  public File fTempPath = null;
  public File fBaseTempPath = null;
  public File fLibsFolder = null;
  private Map<String, Boolean> libsLoaded = new HashMap<String, Boolean>();
  public File fUserDir = null;
  public File fWorkDir = null;
  public File fTestFolder = null;
  public File fTestFile = null;

  private File fOptions = null;
  private Properties options = null;
  private String fnOptions = "SikulixOptions.txt";

  public File fSxBase = null;
  public File fSxBaseJar = null;
  public File fSxProject = null;
  public String fpContent = "sikulixcontent";

  public boolean runningJar = true;
  public boolean runningWindows = false;
  public boolean runningMac = false;
  public boolean runningLinux = false;
  public boolean runningWinApp = false;
  public boolean runningMacApp = false;
  private theSystem runningOn = theSystem.FOO;
  private final String osNameSysProp = System.getProperty("os.name");
  private final String osVersionSysProp = System.getProperty("os.version");
  public String javaShow = "not-set";
  public int javaArch = 32;
  public int javaVersion = 0;
  public String osName = "NotKnown";
  public String osVersion = "";
  private String appType = null;

//</editor-fold>
  
//<editor-fold defaultstate="collapsed" desc="global init">
  private void init(Type typ) {

//<editor-fold defaultstate="collapsed" desc="general">
    if ("winapp".equals(getOption("testing"))) {
      log(lvl, "***** for testing: simulating WinApp");
      testingWinApp = true;
    }
    for (String line : preLogMessages.split(";")) {
      if (!line.isEmpty()) {
        log(lvl, line);
      }
    }
    log(lvl, "global init: entering as: %s", typ);

    sxBuild = Settings.SikuliVersionBuild;
    sxBuildStamp = sxBuild.replace("_", "").replace("-", "").replace(":", "").substring(0, 12);

    String os = osNameSysProp.toLowerCase();
    if (os.startsWith("mac")) {
      osName = "Mac OSX";
      runningMac = true;
    } else if (os.startsWith("windows")) {
      osName = "Windows";
      runningWindows = true;
    } else if (os.startsWith("linux")) {
      osName = "Linux";
      runningLinux = true;
    }

    if (System.getProperty("user.name") != null) {
      userName = System.getProperty("user.name");
    }
    if (userName.isEmpty()) {
      userName = "unknown";
    }

    String tmpdir = System.getProperty("java.io.tmpdir");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fTempPath = new File(tmpdir);
    } else {
      terminate(1, "init: java.io.tmpdir not valid (null or empty");
    }
    fBaseTempPath = new File(fTempPath, "Sikulix");
    fBaseTempPath.mkdirs();
    BaseTempPath = fBaseTempPath.getAbsolutePath();
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="classpath">        
    try {
      if (Type.IDE.equals(typ)) {
        clsRef = Class.forName("org.sikuli.ide.SikuliIDE");
      } else if (Type.SETUP.equals(typ)) {
        clsRef = Class.forName("org.sikuli.setup.RunSetup");
      }
    } catch (Exception ex) {
    }
    CodeSource codeSrc = clsRef.getProtectionDomain().getCodeSource();
    String base = null;
    if (codeSrc != null && codeSrc.getLocation() != null) {
      base = codeSrc.getLocation().getPath();
    }
    appType = "from a jar";
    if (base != null) {
      fSxBaseJar = new File(base);
      String jn = fSxBaseJar.getName();
      fSxBase = fSxBaseJar.getParentFile();
      log(lvl, "runs as %s in: %s", jn, fSxBase.getAbsolutePath());
      if (jn.contains("classes")) {
        runningJar = false;
        fSxProject = fSxBase.getParentFile().getParentFile();
        log(lvl, "not jar - supposing Maven project: %s", fSxProject);
        appType = "in Maven project from classes";
      } else if ("target".equals(fSxBase.getName())) {
        fSxProject = fSxBase.getParentFile().getParentFile();
        log(lvl, "folder target detected - supposing Maven project: %s", fSxProject);
        appType = "in Maven project from some jar";
      } else {
        if (runningWindows) {
          if (jn.endsWith(".exe")) {
            runningWinApp = true;
            runningJar = false;
            appType = "as application .exe";
          }
        } else if (runningMac) {
          if (fSxBase.getAbsolutePath().contains("SikuliX.app/Content")) {
            runningMacApp = true;
            appType = "as application .app";
            if (!fSxBase.getAbsolutePath().startsWith("/Applications")) {
              appType += " (not from /Applications folder)";
            }
          }
        }
      }
    } else {
      terminate(1, "no valid Java context for SikuliX available (java.security.CodeSource.getLocation() is null)");
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="versions">
    String vJava = System.getProperty("java.runtime.version");
    String vVM = System.getProperty("java.vm.version");
    String vClass = System.getProperty("java.class.version");
    String vSysArch = System.getProperty("os.arch");
    javaVersion = Integer.parseInt(vJava.substring(2, 3));
    if (vSysArch.contains("64")) {
      javaArch = 64;
    }

    javaShow = String.format("java %d-%d version %s vm %s class %s arch %s",
            javaVersion, javaArch, vJava, vVM, vClass, vSysArch);

    if (Debug.getDebugLevel() > minLvl) {
      dumpSysProps();
    }

    osVersion = osVersionSysProp;
    String sysName = "";
    if (runningWindows) {
      runningOn = theSystem.WIN;
      sysName = "windows";
    } else if (runningMac) {
      runningOn = theSystem.MAC;
      sysName = "mac";
    } else if (runningLinux) {
      runningOn = theSystem.LUX;
      sysName = "linux";
    } else {
      terminate(-1, "running on not supported System: %s");
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="libs export">
    boolean shouldExport = false;
    URL uLibsFrom = null;
    fLibsFolder = new File(fTempPath, "SikulixLibs_" + sxBuildStamp);
    if (!Type.SETUP.equals(typ)) {
      if (testing) {
        logp("***** for testing");
        FileManager.deleteFileOrFolder(fLibsFolder.getAbsolutePath());
      }
      if (!fLibsFolder.exists()) {
        fLibsFolder.mkdirs();
        if (!fLibsFolder.exists()) {
          terminate(1, "libs folder not available: " + fLibsFolder.toString());
        }
        log(lvl, "new libs folder at: %s", fLibsFolder);
        String[] fpList = fTempPath.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            if (name.contains("SikulixLibs")) {
              return true;
            }
            return false;
          }
        });
        if (fpList.length > 1) {
          for (String entry : fpList) {
            if (entry.endsWith(sxBuildStamp)) {
              continue;
            }
            FileManager.deleteFileOrFolder(new File(fTempPath, entry).getAbsolutePath());
          }
        }
        shouldExport = true;
      } else {
        log(lvl, "exists libs folder at: %s", fLibsFolder);
        if (!checkLibs(fLibsFolder)) {
          FileManager.deleteFileOrFolder(fLibsFolder.getAbsolutePath());
          fLibsFolder.mkdirs();
          shouldExport = true;
          if (!fLibsFolder.exists()) {
            terminate(1, "libs folder not available: " + fLibsFolder.toString());
          }
        }
      }
    }
    if (!Type.SETUP.equals(typ) && shouldExport) {
      String sysShort = "win";
      String fpJarLibs = "/META-INF/libs/";
      if (!runningWinApp && !testingWinApp) {
        sysShort = runningOn.toString().toLowerCase();
        fpJarLibs += sysName + "/libs" + javaArch;        
      } else {
        fpJarLibs += "windows";
      }      
      String fpLibsFrom = "";
      if (runningJar) {
        fpLibsFrom = fSxBaseJar.getAbsolutePath();
      } else {
          String fSrcFolder = typ.toString();
          if (Type.SETUP.toString().equals(fSrcFolder)) {
            fSrcFolder = "Setup";
          }
          fpLibsFrom = fSxBaseJar.getPath().replace(fSrcFolder, "Libs" + sysShort) + "/";
      }
      if (testing && !runningJar) {
        if (testingWinApp || testSwitch()) {
          logp("***** for testing: exporting from classes");
        } else {
          
          logp("***** for testing: exporting from jar");
          fpLibsFrom = new File(fSxProject,
                  String.format("Libs%s/target/sikulixlibs%s-1.1.0.jar", sysShort, sysShort)).getAbsolutePath();
        }
      }
      log(lvl, "now exporting libs");
      if (!fpLibsFrom.isEmpty()) {
        addToClasspath(fpLibsFrom);
      }
      uLibsFrom = clsRef.getResource(fpJarLibs);
      if (testing || uLibsFrom == null) {
        dumpClassPath();
      }
      if (uLibsFrom == null) {
        terminate(1, "libs to export not found on above classpath: " + fpJarLibs);
      }
      log(lvl, "libs to export are at:\n%s", uLibsFrom);
      if (runningWinApp || testingWinApp) {
        String libsAccepted = "libs" + javaArch;
        extractResourcesToFolder(fpJarLibs, fLibsFolder, new LibsFilter(libsAccepted));
        File fCurrentLibs = new File(fLibsFolder, libsAccepted);
        if (FileManager.xcopy(fCurrentLibs, fLibsFolder)) {
          FileManager.deleteFileOrFolder(fCurrentLibs);
        } else {
          terminate(1, "could not create libs folder for Windows --- see log");
        }
      } else {
        extractResourcesToFolder(fpJarLibs, fLibsFolder, null);
      }
      
    }
    if (!Type.SETUP.equals(typ)) {
      for (String aFile : fLibsFolder.list()) {
        libsLoaded.put(aFile, false);
      }
      if (runningWindows) {
        if (!addToWindowsSystemPath(fLibsFolder)) {
          terminate(1, "Problems setting up on Windows - see errors");
        }
      }
    }
//</editor-fold>

    runType = typ;
    if (Debug.getDebugLevel() == minLvl) {
      show();
    }
    log(lvl, "global init: leaving");
  }
  
  class LibsFilter implements FilenameFilter {  
    String sAccept = "";    
    public LibsFilter(String toAccept) {
      sAccept = toAccept;
    }
    @Override
    public boolean accept(File dir, String name) {
      if (dir.getPath().contains(sAccept)) return true;
      return false;
    }
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="init for IDE">
  private File isRunning = null;
  private FileOutputStream isRunningFile = null;

  private void initIDEbefore() {
    log(lvl, "initIDEbefore: entering");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        log(lvl, "final cleanup");
        if (isRunning != null) {
          try {
            isRunningFile.close();
          } catch (IOException ex) {
          }
          isRunning.delete();
        }
        FileManager.cleanTemp();
      }
    });

    new File(BaseTempPath).mkdirs();
    isRunning = new File(BaseTempPath, "sikuli-ide-isrunning");
    try {
      isRunning.createNewFile();
      isRunningFile = new FileOutputStream(isRunning);
      if (null == isRunningFile.getChannel().tryLock()) {
        Sikulix.popError("Terminating on FatalError: IDE already running");
        System.exit(1);
      }
    } catch (Exception ex) {
      Sikulix.popError("Terminating on FatalError: cannot access IDE lock for/n" + isRunning);
      System.exit(1);
    }

    if (jreVersion.startsWith("1.6")) {
      String jyversion = "";
      Properties prop = new Properties();
      String fp = "org/python/version.properties";
      InputStream ifp = null;
      try {
        ifp = classLoader.getResourceAsStream(fp);
        if (ifp != null) {
          prop.load(ifp);
          ifp.close();
          jyversion = prop.getProperty("jython.version");
        }
      } catch (IOException ex) {
      }
      if (!jyversion.isEmpty() && !jyversion.startsWith("2.5")) {
        Sikulix.popError(String.format("The bundled Jython %s\n"
                + "cannot be used on Java 6!\n"
                + "Run setup again in this environment.\n"
                + "Click OK to terminate now", jyversion));
        System.exit(1);
      }
    }

    Settings.isRunningIDE = true;

    if (runningMac) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      if (!runningMacApp) {
        if (!Sikulix.popAsk("This use of SikuliX is not supported\n"
                + "and might lead to misbehavior!\n"
                + "Click YES to continue (you should be sure)\n"
                + "Click NO to terminate and check the situation.")) {
          System.exit(1);
        }
      }
    }

    log(lvl, "initIDEbefore: leaving");
  }

  private void initIDEafter() {
//    log(lvl, "initIDEafter: entering");
//    log(lvl, "initIDEafter: leaving");
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="init for API">
  private void initAPI() {
//    log(lvl, "initAPI: entering");
//    log(lvl, "initAPI: leaving");
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="init for Setup">
  private void initSetup() {
//    log(lvl, "initSetup: entering");
//    log(lvl, "initSetup: leaving");
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="helpers">
  /**
   * INTERNAL USE: to check whether we are running in compiled classes context
   *
   * @return true if the code source location is a folder ending with classes (Maven convention)
   */
  public boolean isRunningFromJar() {
    return runningJar;
  }

  public void show() {
    if (hasOptions()) {
      dumpOptions();
    }
    logp("***** show environment for %s", runType);
    logp("user.home: %s", fUserDir);
    logp("user.dir (work dir): %s", fWorkDir);
    logp("user.name: %s", userName);
    logp("java.io.tmpdir: %s", fTempPath);
    logp("running %dBit on %s (%s) %s", javaArch, osName, osVersion, appType);
    logp(javaShow);
    logp("libs folder: %s", fLibsFolder);
    if (runningJar) {
      logp("executing jar: %s", fSxBaseJar);
    }
    if (Debug.getDebugLevel() > minLvl - 1) {
      dumpClassPath("sikulix");
    }
    logp("***** show environment end");
  }

  public boolean testSwitch() {
    if (0 == (new Date().getTime() / 10000) % 2) {
      return true;
    }
    return false;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="native libs handling">
  /**
   * INTERNAL USE: load a native library from the libs folder
   *
   * @param libname name of library
   */
  public static void loadLibrary(String libname) {
    if (RunTime.get().loadLib(libname)) {
      return;
    }
    ResourceLoader.get().check(Settings.SIKULI_LIB);
    ResourceLoader.get().loadLib(libname);
  }

  private boolean loadLib(String libName) {
    if (runningWindows) {
      libName += ".dll";
    } else if (runningMac) {
      libName = "lib" + libName + ".dylib";
    } else if (runningLinux) {
      libName = "lib" + libName + ".so";
    }
    File fLib = new File(fLibsFolder, libName);
    Boolean vLib = libsLoaded.get(libName);
    if (vLib == null || !fLib.exists()) {
      terminate(1, String.format("lib: %s not available in %s", libName, fLibsFolder));
    }
    String msg = "loadLib: %s";
    int level = lvl;
    if (vLib) {
      level++;
      msg += " already loaded";
    }
    log(level, msg, libName);
    if (vLib) {
      return true;
    }
    try {
      System.load(new File(fLibsFolder, libName).getAbsolutePath());
    } catch (Error e) {
      log(-1, "Problematic lib: %s (...TEMP...)", fLib);
      log(-1, "%s loaded, but it might be a problem with needed dependent libraries\nERROR: %s",
              libName, e.getMessage().replace(fLib.getAbsolutePath(), "...TEMP..."));
      terminate(1, "problem with native library: " + libName);
    }
    libsLoaded.put(libName, true);
    return true;
  }

  private boolean checkLibs(File flibsFolder) {
    // 1.1-MadeForSikuliX64M.txt
    String name = String.format("1.1-MadeForSikuliX%d%s.txt", javaArch, runningOn.toString().substring(0, 1));
    if (!new File(flibsFolder, name).exists()) {
      log(lvl, "libs folder empty or has wrong content");
      return false;
    }
    return true;
  }

  private boolean addToWindowsSystemPath(File fLibsFolder) {
    String syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
    if (syspath == null) {
      terminate(1, "addToWindowsSystemPath: cannot access system path");
    } else {
      String libsPath = (fLibsFolder.getAbsolutePath()).replaceAll("/", "\\");
      if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
        if (!SysJNA.WinKernel32.setEnvironmentVariable("PATH", libsPath + ";" + syspath)) {
          Sikulix.terminate(999);
        }
        syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
        if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
          log(-1, "addToWindowsSystemPath: adding to system path did not work:\n%s", syspath);
          terminate(1, "addToWindowsSystemPath: did not work - see error");
        }
        log(lvl, "addToWindowsSystemPath: added to systempath:\n%s", libsPath);
      }
    }
    if (!checkJavaUsrPath(fLibsFolder)) {
      return false;
    }
    return true;
  }

  private boolean checkJavaUsrPath(File fLibsFolder) {
    String fpLibsFolder = fLibsFolder.getAbsolutePath();
    Field usrPathsField = null;
    boolean contained = false;
    try {
      usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    } catch (NoSuchFieldException ex) {
      log(-1, "checkJavaUsrPath: get\n%s", ex);
    } catch (SecurityException ex) {
      log(-1, "checkJavaUsrPath: get\n%s", ex);
    }
    if (usrPathsField != null) {
      usrPathsField.setAccessible(true);
      try {
        //get array of paths
        String[] javapaths = (String[]) usrPathsField.get(null);
        //check if the path to add is already present
        for (String p : javapaths) {
          if (new File(p).equals(fLibsFolder)) {
            contained = true;
            break;
          }
        }
        //add the new path
        if (!contained) {
          final String[] newPaths = Arrays.copyOf(javapaths, javapaths.length + 1);
          newPaths[newPaths.length - 1] = fpLibsFolder;
          usrPathsField.set(null, newPaths);
          log(lvl, "checkJavaUsrPath: added to ClassLoader.usrPaths");
          contained = true;
        }
      } catch (IllegalAccessException ex) {
        log(-1, "checkJavaUsrPath: set\n%s", ex);
      } catch (IllegalArgumentException ex) {
        log(-1, "checkJavaUsrPath: set\n%s", ex);
      }
      return contained;
    }
    return false;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="options handling">
  private void loadOptions(Type typ) {

    String tmpdir = System.getProperty("user.home");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fUserDir = new File(tmpdir);
    } else {
      fUserDir = new File(fTempPath, "SikuliXuser_" + userName);
      FileManager.resetFolder(runTime.fUserDir);
      log(-1, "init: user.home not valid (null or empty) - using empty:\n%s", fUserDir);
    }
    fTestFolder = new File(fUserDir, "SikulixTest");
    fTestFile = new File(fTestFolder, "SikulixTest.txt");

    tmpdir = System.getProperty("user.dir");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fWorkDir = new File(tmpdir);
    } else {
      fWorkDir = new File(fTempPath, "SikuliXwork");
      FileManager.resetFolder(fWorkDir);
      log(-1, "init: user.dir (working folder) not valid (null or empty) - using empty:\n%s", fWorkDir);
    }

    File fDebug = new File(fUserDir, "SikulixDebug.txt");
    if (fDebug.exists()) {
      if (Debug.getDebugLevel() == 0) {
        Debug.setDebugLevel(3);
      }
      Debug.setLogFile(fDebug.getAbsolutePath());
      if (Type.IDE.equals(typ)) {
        System.setProperty("sikuli.console", "false");
      }
      logp("auto-debugging with level %d into %s", Debug.getDebugLevel(), fDebug);
    }

    fOptions = new File(fWorkDir, fnOptions);
    if (!fOptions.exists()) {
      fOptions = new File(fUserDir, fnOptions);
      if (!fOptions.exists()) {
        fOptions = null;
      }
    }
    if (fOptions != null) {
      options = new Properties();
      try {
        InputStream is;
        is = new FileInputStream(fOptions);
        options.load(is);
        is.close();
      } catch (Exception ex) {
        log(-1, "while checking Options file: %s", fOptions);
        fOptions = null;
        options = null;
      }
      testing = isOption("testing", false);
      if (testing) {
        Debug.setDebugLevel(3);
      }
      log(lvl, "found Options file at: %s", fOptions);
    }
  }

  /**
   * NOT IMPLEMENTED YET load an options file, that is merged with an existing options store (same key overwrites value)
   *
   * @param fpOptions path to a file containing options
   */
  public void loadOptions(String fpOptions) {
    log(-1, "loadOptions: not yet implemented");
  }

  /**
   * NOT IMPLEMENTED YET saves the current option store to a file (overwritten)
   *
   * @param fpOptions path to a file
   * @return success
   */
  public boolean saveOptions(String fpOptions) {
    log(-1, "saveOptions: not yet implemented");
    return false;
  }

  /**
   * NOT IMPLEMENTED YET saves the current option store to the file it was created from (overwritten)
   *
   * @return success, false if the current store was not created from file
   */
  public boolean saveOptions() {
    log(-1, "saveOptions: not yet implemented");
    return false;
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)
   *
   * @param pName the option key (case-sensitive)
   * @return true only if option exists and has yes or true (not case-sensitive), in all other cases false
   */
  public boolean isOption(String pName) {
    if (options == null) {
      return false;
    }
    String pVal = options.getProperty(pName, "false").toLowerCase();
    if (pVal.isEmpty() || pVal.contains("yes") || pVal.contains("true")) {
      return true;
    }
    return false;
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)
   *
   * @param pName the option key (case-sensitive)
   * @param bDefault the default to be returned if option absent or empty
   * @return true if option has yes or no, false for no or false (not case-sensitive)
   */
  public boolean isOption(String pName, Boolean bDefault) {
    if (options == null) {
      return bDefault;
    }
    String pVal = options.getProperty(pName, bDefault.toString()).toLowerCase();
    if (pVal.isEmpty()) {
      return bDefault;
    } else if (pVal.contains("yes") || pVal.contains("true") || pVal.contains("on")) {
      return true;
    } else if (pVal.contains("no") || pVal.contains("false") || pVal.contains("off")) {
      return false;
    }
    return true;
  }

  /**
   * look into the option file if any (if no option file is found, the option is taken as not existing)
   *
   * @param pName the option key (case-sensitive)
   * @return the associated value, empty string if absent
   */
  public String getOption(String pName) {
    if (options == null) {
      return "";
    }
    String pVal = options.getProperty(pName, "").toLowerCase();
    return pVal;
  }

  /**
   * look into the option file if any (if no option file is found, the option is taken as not existing)<br>
   * side-effect: if no options file is there, an options store will be created in memory<br>
   * in this case and when the option is absent or empty, the given default will be stored<br>
   * you might later save the options store to a file with storeOptions()
   *
   * @param pName the option key (case-sensitive)
   * @param sDefault the default to be returned if option absent or empty
   * @return the associated value, the default value if absent or empty
   */
  public String getOption(String pName, String sDefault) {
    if (options == null) {
      options = new Properties();
      options.setProperty(pName, sDefault);
      return sDefault;
    }
    String pVal = options.getProperty(pName, sDefault).toLowerCase();
    if (pVal.isEmpty()) {
      options.setProperty(pName, sDefault);
      return sDefault;
    }
    return pVal;
  }

  /**
   * store an option key-value pair, overwrites existing value<br>
   * new option store is created if necessary and can later be saved to a file
   *
   * @param pName
   * @param sValue
   */
  public void setOption(String pName, String sValue) {
    if (options == null) {
      options = new Properties();
    }
    options.setProperty(pName, sValue);
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)<br>
   * tries to convert the stored string value into an integer number (gives 0 if not possible)<br>
   *
   * @param pName the option key (case-sensitive)
   * @return the converted integer number, 0 if absent or not possible
   */
  public int getOptionNumber(String pName) {
    if (options == null) {
      return 0;
    }
    String pVal = options.getProperty(pName, "0").toLowerCase();
    int nVal = 0;
    try {
      nVal = Integer.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)<br>
   * tries to convert the stored string value into an integer number (gives 0 if not possible)<br>
   *
   * @param pName the option key (case-sensitive)
   * @param nDefault the default to be returned if option absent, empty or not convertable
   * @return the converted integer number, default if absent, empty or not possible
   */
  public int getOptionNumber(String pName, Integer nDefault) {
    if (options == null) {
      return nDefault;
    }
    String pVal = options.getProperty(pName, nDefault.toString()).toLowerCase();
    int nVal = nDefault;
    try {
      nVal = Integer.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * all options and their values
   *
   * @return a map of key-value pairs containing the found options, empty if no options file found
   */
  public Map<String, String> getOptions() {
    Map<String, String> mapOptions = new HashMap<String, String>();
    if (options != null) {
      Enumeration<?> optionNames = options.propertyNames();
      String optionName;
      while (optionNames.hasMoreElements()) {
        optionName = (String) optionNames.nextElement();
        mapOptions.put(optionName, getOption(optionName));
      }
    }
    return mapOptions;
  }

  /**
   * check whether options are defined
   *
   * @return true if at lest one option defined else false
   */
  public boolean hasOptions() {
    return options != null && options.size() > 0;
  }

  /**
   * all options and their values written to sysout as key = value
   */
  public void dumpOptions() {
    if (options.size() > 0) {
      logp("*** options dump %s", (fOptions != null ? "" : fOptions));
      for (String sOpt : getOptions().keySet()) {
        logp("%s = %s", sOpt, getOption(sOpt));
      }
      logp("*** options dump end");
    }
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="handling resources from classpath">
  /**
   * export all resource files from the given subtree on classpath to the given folder retaining the subtree
   *
   * @param fpRessources path of the subtree relative to root with leading /
   * @param fFolder folder where to export
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public boolean extractResourcesToFolder(String fpRessources, File fFolder, FilenameFilter filter) {
    List<String> content = null;
    content = resourceList(fpRessources, filter);
    if (content == null) {
      return false;
    }
    return extractResourcesToFolderWithList(fpRessources, fFolder, content);
  }
  
  /**
   * export all resource files from the given subtree on classpath to the given folder retaining the subtree
   *
   * @param fpRessources path of the subtree relative to root with leading /
   * @param fFolder folder where to export
   * @param content a list of filenames including a possible subfolder structure
   * @return success
   */
  public boolean extractResourcesToFolderWithList(String fpRessources, File fFolder, List<String> content) {
    int count = 0;
    int ecount = 0;
    String subFolder = "";
    if (content != null && content.size() > 0) {
      for (String eFile : content) {
        if (eFile == null) {
          continue;
        }
        if (eFile.endsWith("/")) {
          subFolder = eFile.substring(0, eFile.length() - 1);
          continue;
        }
        if (!subFolder.isEmpty()) {
          eFile = new File(subFolder, eFile).getPath();
        }
        if (extractResourceToFile(fpRessources, eFile, fFolder, eFile)) {
          log(lvl + 1, "extractResourceToFile done: %s", eFile);
          count++;
        } else {
          ecount++;
        }
      }
    }
    if (ecount > 0) {
      log(lvl, "files exported: %d - skipped: %d from %s to:\n %s", count, ecount, fpRessources, fFolder);
    } else {
      log(lvl, "files exported: %d from: %s to:\n %s", count, fpRessources, fFolder);
    }
    return true;
  }
  
  /**
   * store a resource found on classpath to a file in the given folder
   *
   * @param inPrefix a subtree found in classpath
   * @param inFile the filename combined with the prefix on classpath
   * @param outDir a folder where to export
   * @param outFile the filename for export
   * @return success
   */
  public boolean extractResourceToFile(String inPrefix, String inFile, File outDir, String outFile) {
    InputStream aIS;
    FileOutputStream aFileOS;
    String content = inPrefix + "/" + inFile;
    try {
      content = runningWindows ? content.replace("\\", "/") : content;
      if (!content.startsWith("/")) content = "/" + content;
      aIS = (InputStream) clsRef.getResourceAsStream(content);
      if (aIS == null) {
        throw new IOException("resource not accessible");
      }
      File out = new File(outDir, inFile);
      if (!out.getParentFile().exists()) {
        out.getParentFile().mkdirs();
      }
      aFileOS = new FileOutputStream(out);
      copy(aIS, aFileOS);
      aIS.close();
      aFileOS.close();
    } catch (Exception ex) {
      log(-1, "extractResourceToFile: %s\n%s", content, ex);
      return false;
    }
    return true;
  }

  /**
   * store the content of a resource found on classpath in the returned string
   *
   * @param inPrefix a subtree from root found in classpath (leading /)
   * @param inFile the filename combined with the prefix on classpath
   * @param encoding
   * @return file content
   */
  public String extractResourceToString(String inPrefix, String inFile, String encoding) {
    InputStream aIS = null;
    String out = null;
    String content = inPrefix + "/" + inFile;
    if (!content.startsWith("/")) content = "/" + content;
    try {
      content = runningWindows ? content.replace("\\", "/") : content;
      aIS = (InputStream) clsRef.getResourceAsStream(content);
      if (aIS == null) {
        throw new IOException("resource not accessible");
      }
      if (encoding == null) {
        encoding = "RAW-BYTE";
        out = new String(copy(aIS));
      } else if (encoding.isEmpty()) {
        out = new String(copy(aIS), "UTF-8");
      } else {
        out = new String(copy(aIS), encoding);
      }
      aIS.close();
      aIS = null;
    } catch (Exception ex) {
      log(-1, "extractResourceToString as %s from:\n%s\n%s", encoding, content, ex);
    }
    try {
      if (aIS != null) {
        aIS.close();
      }
    } catch (Exception ex) {}
    return out;
  }

  /**
   * list all files in the folder and it's subtree (files only, no folder names), but the files have the subtree
   * prefixed
   *
   * @param folder folder or jar on classpath
   * @param filter
   * @return the list (might be empty)
   */
  public List<String> resourceList(String folder, FilenameFilter filter) {
    List<String> files = new ArrayList<String>();
    if (!folder.startsWith("/")) folder = "/" + folder;
    URL uFolder = clsRef.getResource(folder);
    if (uFolder == null) {
      return files;
    }
    URL uContentList = clsRef.getResource(folder + "/" + fpContent);
    if (uContentList != null) {
      return doResourceListWithList(folder, files, filter);
    }
    File fFolder = null;
    try {
      fFolder = new File(uFolder.toURI());
      String sFolder = uFolder.getPath();
      if (":".equals(sFolder.substring(2, 3))) {
        sFolder = sFolder.substring(1);
      }
      if (sFolder.endsWith("/")) {
        sFolder = sFolder.substring(0, sFolder.length() - 1);
      }
      files.add(sFolder);
      files =  doResourceListFolder(fFolder, files, filter);
      files.remove(0);
      return files;
    } catch (Exception ex) {
      if (!"jar".equals(uFolder.getProtocol())) {
        log(lvl, "resourceList:\n%s", folder);
        log(-1, "resourceList: URL neither folder nor jar:\n%s", ex);
        return null;
      }
    }
    String[] parts = uFolder.getPath().split("!");
    if (parts.length < 2 || !parts[0].startsWith("file:")) {
      log(lvl, "resourceList:\n%s", folder);
      log(-1, "resourceList: not a valid jar URL: " + uFolder.getPath());
      return null;
    }
    String fpFolder = parts[1];
    return doResourceListJar(uFolder, fpFolder, files, filter);
  }
  
  /**
   * same as resourceList, but the list is written to file with lineseparators of the running system<br>
   * @param folder path of the subtree relative to root with leading /
   * @param target the file to take the list
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public boolean resourceListAsFile(String folder, File target, FilenameFilter filter) {
    String content = resourceListAsString(folder, filter);
    if (content == null) {
      log(-1, "resourceListAsFile: did not work: %s", folder);
      return false;
    }
    try {
      FileManager.deleteFileOrFolder(target.getAbsolutePath());
      target.getParentFile().mkdirs();
      PrintWriter aPW =new PrintWriter(target);
      aPW.write(content);
      aPW.close();
    } catch (Exception ex) {
      log(-1, "resourceListAsFile: %s:\n%s", target, ex);
      return false;
    }
    return true;
  }

  /**
   * same as resourceList, but the list is stored as string with lineseparators of the running system<br>
   * @param folder path of the subtree relative to root with leading /
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return the resulting string
   */
  public String resourceListAsString(String folder, FilenameFilter filter) {
    return resourceListAsString(folder, filter, null);
  }
  
  /**
   * same as resourceList, but the list is stored as string with a given separator<br>
   * @param folder path of the subtree relative to root with leading /
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @param separator to be used to separate the entries 
   * @return the resulting string
   */
  public String resourceListAsString(String folder, FilenameFilter filter, String separator) {
    List<String> aList = resourceList(folder, filter);
    if (aList == null) {
      return null;
    }
    if (separator == null) {
      separator = System.getProperty("line.separator");
    }
    String out = "";
    String subFolder = "";
    if (aList != null && aList.size() > 0) {
      for (String eFile : aList) {
        if (eFile == null) {
          continue;
        }
        if (eFile.endsWith("/")) {
          subFolder = eFile.substring(0, eFile.length() - 1);
          continue;
        }
        if (!subFolder.isEmpty()) {
          eFile = new File(subFolder, eFile).getPath();
        }
        out += eFile.replace("\\", "/") + separator;
      }
    }
    return out;
  }
  
  private List<String> doResourceListFolder(File fFolder, List<String> files, FilenameFilter filter) {
    int localLevel = testing ? lvl : lvl + 1;
    String subFolder = "";
    if (fFolder.isDirectory()) {
      if (!FileManager.pathEquals(fFolder.getPath(), files.get(0))) {
        subFolder = fFolder.getPath().substring(files.get(0).length() + 1).replace("\\", "/") + "/";
        if (filter != null && !filter.accept(new File(files.get(0), subFolder), "")) {
          return files;
        }
      } else {
        logp(localLevel, "scanning folder:\n%s", fFolder);
        subFolder = "/";
        files.add(subFolder);
      }
      String[] subList = fFolder.list();
      for (String entry : subList) {
        File fEntry = new File(fFolder, entry);
        if (fEntry.isDirectory()) {
          files.add(fEntry.getAbsolutePath().substring(1 + files.get(0).length()).replace("\\", "/") + "/");
          doResourceListFolder(fEntry, files, filter);
          files.add(subFolder);
        } else {
          if (filter != null && !filter.accept(fFolder, entry)) {
            continue;
          }
          logp(localLevel, "from %s adding: %s", (subFolder.isEmpty() ? "." : subFolder), entry);
          files.add(fEntry.getAbsolutePath().substring(1 + fFolder.getPath().length()));
        }
      }
    }
    return files;
  }

  private List<String> doResourceListWithList(String folder, List<String> files, FilenameFilter filter) {
    String content = extractResourceToString(folder, fpContent, "");
    String[] contentList = content.split(content.indexOf("\r") != -1 ? "\r\n" : "\n");
    if (filter == null) {
      files.addAll(Arrays.asList(contentList));      
    } else {
      for (String fpFile : contentList) {
        if (filter.accept(new File(fpFile), "")) {
          files.add(fpFile);
        }
      }
    }
    return files;
  }

  private List<String> doResourceListJar(URL uJar, String folder, List<String> files, FilenameFilter filter) {
    ZipInputStream zJar;
    String fpJar = uJar.getPath().split("!")[0];
    int localLevel = testing ? lvl : lvl + 1;
    String fileSep = "/";
    if (!fpJar.endsWith(".jar")) {
      return files;
    }
    logp(localLevel, "scanning jar:\n%s", uJar);
    folder = folder.startsWith("/") ? folder.substring(1) : folder;
    File fFolder = new File(folder);
    File fSubFolder = null;
    ZipEntry zEntry;
    String subFolder = "";
    boolean skip = false;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        if (zEntry.getName().endsWith("/")) {
          continue;
        }
        String zePath = zEntry.getName();
        if (zePath.startsWith(folder)) {
          String zeName = zePath.substring(folder.length() + 1);
          int nSep = zeName.lastIndexOf(fileSep);
          String zefName = zeName.substring(nSep + 1, zeName.length());
          String zeSub = "";
          if (nSep > 0) {
            zeSub = zeName.substring(0, nSep + 1);
            if (!subFolder.equals(zeSub)) {
              subFolder = zeSub;
              fSubFolder = new File(fFolder, subFolder);
              skip = false;
              if (filter != null && !filter.accept(fSubFolder, "")) {
                skip = true;
                continue;
              }
              files.add(zeSub);
            }
            if (skip) {
              continue;
            }
          }
          if (filter != null && !filter.accept(fSubFolder, zefName)) {
            continue;
          }
          files.add(zefName);
          logp(localLevel, "from %s adding: %s", (zeSub.isEmpty() ? "." : zeSub), zefName);
        }
      }
    } catch (Exception ex) {
      log(-1, "doResourceListJar: %s", ex);
      return files;
    }
    return files;
  }

  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] tmp = new byte[8192];
    int len;
    while (true) {
      len = in.read(tmp);
      if (len <= 0) {
        break;
      }
      out.write(tmp, 0, len);
    }
    out.flush();
  }
  
  private byte[] copy(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length = 0;
    while ((length = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, length);
    }
    return baos.toByteArray();
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="classpath handling">
  private void storeClassPath() {
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    classPath = Arrays.asList(sysLoader.getURLs());
  }

  /**
   * print the current classpath entries to sysout
   */
  public void dumpClassPath() {
    dumpClassPath(null);
  }

  /**
   * print the current classpath entries to sysout whose path name contain the given string
   *
   * @param filter the fileter string
   */
  public void dumpClassPath(String filter) {
    filter = filter == null ? "" : filter;
    logp("*** classpath dump %s", filter);
    storeClassPath();
    String sEntry;
    filter = filter.toUpperCase();
    int n = 0;
    for (URL uEntry : classPath) {
      sEntry = uEntry.getPath();
      if (!filter.isEmpty()) {
        if (!sEntry.toUpperCase().contains(filter)) {
          n++;
          continue;
        }
      }
      logp("%3d: %s", n, sEntry);
      n++;
    }
    logp("*** classpath dump end");
  }

  /**
   * check wether a classpath entry contains the given identifying string, stops on first match
   *
   * @param artefact the identifying string
   * @return the absolute path of the entry found
   */
  public String isOnClasspath(String artefact) {
    artefact = FileManager.slashify(artefact, false).toUpperCase();
    String cpe = null;
    if (classPath.isEmpty()) {
      storeClassPath();
    }
    for (URL entry : classPath) {
      String sEntry = FileManager.slashify(new File(entry.getPath()).getPath(), false);
      if (sEntry.toUpperCase().contains(artefact)) {
        cpe = new File(entry.getPath()).getPath();
        break;
      }
    }
    return cpe;
  }

  /**
   * check wether a the given URL is on classpath
   *
   * @param path URL to look for
   * @return true if found else otherwise
   */
  public boolean isOnClasspath(URL path) {
    if (classPath.isEmpty()) {
      storeClassPath();
    }
    for (URL entry : classPath) {
      if (new File(path.getPath()).equals(new File(entry.getPath()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * adds the given folder or jar to the end of the current classpath
   *
   * @param jarOrFolder absolute path to a folder or jar
   * @return success
   */
  public boolean addToClasspath(String jarOrFolder) {
    URL uJarOrFolder = FileManager.makeURL(jarOrFolder);
    if (isOnClasspath(uJarOrFolder)) {
      return true;
    }
    log(lvl, "addToClasspath: %s", uJarOrFolder);
    if (!new File(jarOrFolder).exists()) {
      log(-1, "does not exist - not added");
      return false;
    }
    Method method;
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
    try {
      method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(sysLoader, new Object[]{uJarOrFolder});
    } catch (Exception ex) {
      log(-1, "Did not work: %s", ex.getMessage());
      return false;
    }
    storeClassPath();
    return true;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="system enviroment">
  /**
   * print the current java system properties key-value pairs sorted by key
   */
  public void dumpSysProps() {
    dumpSysProps(null);
  }

  /**
   * print the current java system properties key-value pairs sorted by key but only keys containing filter
   *
   * @param filter the filter string
   */
  public void dumpSysProps(String filter) {
    filter = filter == null ? "" : filter;
    logp("*** system properties dump " + filter);
    Properties sysProps = System.getProperties();
    ArrayList<String> keysProp = new ArrayList<String>();
    Integer nL = 0;
    String entry;
    for (Object e : sysProps.keySet()) {
      entry = (String) e;
      if (entry.length() > nL) {
        nL = entry.length();
      }
      if (filter.isEmpty() || !filter.isEmpty() && entry.contains(filter)) {
        keysProp.add(entry);
      }
    }
    Collections.sort(keysProp);
    String form = "%-" + nL.toString() + "s = %s";
    for (Object e : keysProp) {
      logp(form, e, sysProps.get(e));
    }
    logp("*** system properties dump end" + filter);
  }

  /**
   * checks, whether Java runs with a valid GraphicsEnvironment (usually means real screens connected)
   *
   * @return false if Java thinks it has access to screen(s), true otherwise
   */
  public boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="runcmd">
  /**
   * run a system command finally using Java::Runtime.getRuntime().exec(args) and waiting for completion
   *
   * @param cmd the command as it would be given on command line, quoting is preserved
   * @return the output produced by the command (sysout [+ "*** error ***" + syserr] if the syserr part is present, the
   * command might have failed
   */
  public String runcmd(String cmd) {
    return runcmd(new String[]{cmd});
  }

  /**
   * run a system command finally using Java::Runtime.getRuntime().exec(args) and waiting for completion
   *
   * @param cmd the command as it would be given on command line splitted into the space devided parts, first part is
   * the command, the rest are parameters and their values
   * @return the output produced by the command (sysout [+ "*** error ***" + syserr] if the syserr part is present, the
   * command might have failed
   */
  public String runcmd(String args[]) {
    if (args.length == 0) {
      return "";
    }
    if (args.length == 1) {
      String separator = "\"";
      ArrayList<String> argsx = new ArrayList<String>();
      StringTokenizer toks;
      String tok;
      String cmd = args[0];
      if (Settings.isWindows()) {
        cmd = cmd.replaceAll("\\\\ ", "%20;");
      }
      toks = new StringTokenizer(cmd);
      while (toks.hasMoreTokens()) {
        tok = toks.nextToken(" ");
        if (tok.length() == 0) {
          continue;
        }
        if (separator.equals(tok)) {
          continue;
        }
        if (tok.startsWith(separator)) {
          if (tok.endsWith(separator)) {
            tok = tok.substring(1, tok.length() - 1);
          } else {
            tok = tok.substring(1);
            tok += toks.nextToken(separator);
          }
        }
        argsx.add(tok.replaceAll("%20;", " "));
      }
      args = argsx.toArray(new String[0]);
    }
//    if (args[0].startsWith("#")) {
//      String pgm = args[0].substring(1);
//      args[0] = (new File(libsDir, pgm)).getAbsolutePath();
//      runcmd(new String[]{"chmod", "ugo+x", args[0]});
//    }
    String result = "";
    String error = "*** error ***\n";
    boolean hasError = false;
    int retVal;
    try {
      if (lvl <= Debug.getDebugLevel()) {
        log(lvl, Sikulix.arrayToString(args));
      } else {
        Debug.info("runcmd: " + Sikulix.arrayToString(args));
      }
      Process process = Runtime.getRuntime().exec(args);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String s;
      while ((s = stdInput.readLine()) != null) {
        if (!s.isEmpty()) {
          result += s + "\n";
        }
      }
      if ((s = stdError.readLine()) != null) {
        hasError = true;
        if (!s.isEmpty()) {
          error += s + "\n";
        }
      }
      process.waitFor();
      retVal = process.exitValue();
      process.destroy();
    } catch (Exception e) {
      log(-1, "fatal error: " + e);
      result = String.format(error + "%s", e);
      retVal = 9999;
      hasError = true;
    }
    result = String.format("%d\n%s", retVal, result);
    if (hasError) {
      result += error;
    }
    return result;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="args handling for scriptrunner">
  private String[] args = new String[0];
  private String[] sargs = new String[0];

  public void setArgs(String[] args, String[] sargs) {
    this.args = args;
    this.sargs = sargs;
  }

  public String[] getSikuliArgs() {
    return sargs;
  }

  public String[] getArgs() {
    return args;
  }

  public void printArgs() {
    if (Debug.getDebugLevel() < lvl) {
      return;
    }
    String[] xargs = getSikuliArgs();
    if (xargs.length > 0) {
      Debug.log(lvl, "--- Sikuli parameters ---");
      for (int i = 0; i < xargs.length; i++) {
        Debug.log(lvl, "%d: %s", i + 1, xargs[i]);
      }
    }
    xargs = getArgs();
    if (xargs.length > 0) {
      Debug.log(lvl, "--- User parameters ---");
      for (int i = 0; i < xargs.length; i++) {
        Debug.log(lvl, "%d: %s", i + 1, xargs[i]);
      }
    }
  }

  public static int checkArgs(String[] args) {
    int debugLevel = -1;
    List<String> options = new ArrayList<String>();
    options.addAll(Arrays.asList(args));
    for (int n = 0; n < options.size(); n++) {
      String opt = options.get(n);
      if (!opt.startsWith("-")) {
        continue;
      }
      if (opt.startsWith("-d")) {
        int nD = -1;
        try {
          nD = n + 1 == options.size() ? 1 : Integer.decode(options.get(n + 1));
        } catch (Exception ex) {
          nD = 1;
        }
        if (nD > -1) {
          debugLevel = nD;
          Debug.setDebugLevel(nD);
        }
      }
    }
    return debugLevel;
  }
//</editor-fold>

}
