/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2014
 */
package org.sikuli.script;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.ResourceLoader;
import org.sikuli.basics.Settings;
import org.sikuli.script.Sikulix;

/**
 * INTERNAL USE: Intended to concentrate all, that is needed at startup of sikulix or sikulixapi
 */
public class RunTime {

  public static void loadLibrary(String libname) {
    ResourceLoader.get().check(Settings.SIKULI_LIB);
    ResourceLoader.get().loadLib(libname);
  }
  
  final String me = "RunTime%s: ";
  public int lvl = 3;
  public int minLvl = lvl;
  
  void log(int level, String message, Object... args) {
    Debug.logx(level, String.format(me, runType) + message, args);
  }
  
  void logp(String message, Object... args) {
    System.out.println(String.format(message, args));
  }
  
  void terminate(int retval, String msg) {
    log(-1, "*** terminating: " + msg);
    System.exit(retval);
  }

  static RunTime runTime = null;
  public static boolean testing = false;
  
  Type runType = Type.INIT;
  public static synchronized RunTime get(Type typ) {
    if (runTime == null) {
      if (testing) Debug.setDebugLevel(3);
      Settings.init(); // force Settings initialization
      runTime = new RunTime();
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
    return runTime;
  }
  
  public static synchronized RunTime get() {
    return get(Type.API);
  }
  
  public enum Type {
    IDE, API, SETUP, INIT
  }
  
  private enum theSystem {
    WIN, MAC, LUX, FOO
  }
    
  RunTime() {}
  
  public String sxBuild = "";
  public String sxBuildStamp = "";
  public String jreVersion = java.lang.System.getProperty("java.runtime.version");
	public Preferences options = Preferences.userNodeForPackage(Sikulix.class);
  public ClassLoader classLoader = RunTime.class.getClassLoader();
	public String baseJar = "";
  public String userName = "";
  public String BaseTempPath = "";
  
  Class clsRef = RunTime.class;
  List<URL> classPath = new ArrayList<URL>();
  File fTempPath = null;
  File fBaseTempPath = null;
  File fLibsFolder = null;
  File fUserDir = null;
  File fWorkDir = null;
  public File fSxBase = null;
  public File fSxBaseJar = null;
  public File fSxProject = null;
  boolean runningJar = true;
  boolean runningWindows = false;
  boolean runningMac = false;
  boolean runningLinux = false;
  boolean runningWinApp = false;
  boolean runningMacApp = false;
  theSystem runningOn = theSystem.FOO;
  final String osNameSysProp = System.getProperty("os.name");
  final String osVersionSysProp = System.getProperty("os.version");
  public String javaShow = "not-set";
  public int javaArch = 32;
  public int javaVersion = 0;
  public String osName = "NotKnown";
  public String osVersion = "";

  private void init(Type typ) {
    log(lvl, "global init: entering as: %s", typ);
    
    sxBuild = Settings.SikuliVersionBuild;
    sxBuildStamp = sxBuild.replace("_", "").replace("-", "").replace(":", "").substring(0, 12);

        
//<editor-fold defaultstate="collapsed" desc="general">
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
    
    if (System.getProperty("user.name") != null ) {
      userName = System.getProperty("user.name");
    }
    if (userName.isEmpty()) {
      userName = "unknown";
    }
    log(lvl, "user.name: %s", userName);
    
    String tmpdir = System.getProperty("java.io.tmpdir");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fTempPath = new File(tmpdir);
    } else {
      terminate(1, "init: java.io.tmpdir not valid (null or empty");
    }
    fBaseTempPath = new File(fTempPath, "Sikulix");
    fBaseTempPath.mkdirs();
    BaseTempPath = fBaseTempPath.getAbsolutePath();
    log(lvl, "java.io.tmpdir: %s", fTempPath);
    
    tmpdir = System.getProperty("user.home");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fUserDir = new File(tmpdir);
    } else {
      fUserDir = new File(fTempPath, "SikuliXuser_" + userName);
      FileManager.resetFolder(fUserDir);
      log(-1, "init: user.home not valid (null or empty) - using empty:\n%s", fUserDir);
    }
    log(lvl, "user.home: %s", fUserDir);
    
    tmpdir = System.getProperty("user.dir");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fWorkDir = new File(tmpdir);
    } else {
      fWorkDir = new File(fTempPath, "SikuliXwork");
      FileManager.resetFolder(fWorkDir);
      log(-1, "init: user.dir (working folder) not valid (null or empty) - using empty:\n%s", fWorkDir);
    }
    log(lvl, "user.dir (work dir): %s", fWorkDir);
    
    File fDebug = new File(fUserDir, "SikulixDebug.txt");
    if (fDebug.exists()) {
      Debug.setDebugLevel(3);
      Debug.setLogFile(fDebug.getAbsolutePath());
      if (Type.IDE.equals(typ)) {
        System.setProperty("sikuli.console", "false");
      }
      log(lvl, "auto-debugging with level 3 into %s", fDebug);
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="classpath">
    String appType = null;
    
    if (Debug.getDebugLevel() > minLvl) {
      dumpClassPath("sikuli");
    }
    
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
      } else {
        if (runningWindows) {
          if (jn.endsWith(".exe")) {
            runningWinApp = true;
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
    
    javaShow = String.format("versions (%d,%d) java %s vm %s class %s arch %s",
            javaVersion, javaArch, vJava, vVM, vClass, vSysArch);
    log(lvl, javaShow);
    
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
    log(lvl, "running %dBit on %s (%s) %s", javaArch, osName, osVersion, appType);
//</editor-fold>
    
    boolean shouldExport = false;
    URL uLibsFrom = null;
    fLibsFolder = new File(fTempPath, "SikulixLibs_" + sxBuildStamp);
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
          if (name.contains("SikulixLibs")) return true;
          return false;
        }
      });
      if (fpList.length > 1) {
        for (String entry : fpList) {
          if (entry.endsWith(sxBuildStamp)) continue;
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
    if (shouldExport) {
      String sysShort = runningOn.toString().toLowerCase();
      log(lvl, "now exporting libs");
      String fpJarLibs = "/META-INF/libs/" + sysName + "/libs" + javaArch;
      String fpLibsFrom = "";
      if (runningJar) {
        fpLibsFrom = fSxBaseJar.getAbsolutePath();
      } else {
        if (!runningWinApp) {
          fpLibsFrom = fSxBaseJar.getPath().replace(typ.toString(), "Libs" + sysShort) + "/";
        }
      }
      if (testing) {
        long now = (new Date().getTime()/10000)%2;
        if (now == 0) {
          logp("***** for testing: exporting from classes");
        } else {
          logp("***** for testing: exporting from jar");
          fpLibsFrom = new File(fSxProject, 
                  String.format("Libs%s/target/sikulixlibs%s-1.1.0.jar", sysShort, sysShort)).getAbsolutePath();
        }
      }
      if (!Type.SETUP.equals(typ)) {
        if (!fpLibsFrom.isEmpty()) {
          addToClasspath(fpLibsFrom);
        }
        uLibsFrom = clsRef.getResource(fpJarLibs);
        if (uLibsFrom == null) {
          dumpClassPath();
          terminate(1, "libs to export not found on above classpath: " + fpJarLibs);
        }
        log(lvl, "libs to export are at:\n%s", uLibsFrom);
        extractRessourcesToFolder(uLibsFrom, fpJarLibs, fLibsFolder);
      }
    }
    
    runType = typ;
    log(lvl, "global init: leaving");
  }
  
  void extractRessourcesToFolder(URL uFrom, String fpRessources, File fFolder) {
    List<String> content = null;
    content = resourceListDeep(uFrom);
    int count = 0;
    if (content != null && content.size() > 1) {
      log(lvl, "files to export: %d", content.size() - 1);
      content.set(0, null);
      for (String eFile : content) {
        if (eFile == null) {
          continue;
        }
        if (extractResourceToFile(fpRessources, eFile, fFolder, eFile)) {
          log(lvl + 1, "extractResourceToFile done: %s", eFile);
          count++;
        }
      }
    }
    log(lvl, "files exported: %d", count);
  }
  
  boolean extractResourceToFile(String inPrefix, String inFile, File outDir, String outFile) {
    InputStream aIS;
    FileOutputStream aFileOS;
    aIS = (InputStream) clsRef.getResourceAsStream(inPrefix + "/" + inFile);
    File out = new File(outDir, inFile);
    if (!out.getParentFile().exists()) {
      out.getParentFile().mkdirs();
    }
    try {
      aFileOS = new FileOutputStream(out);
      copy(aIS, aFileOS);
      aIS.close();
      aFileOS.close();
    } catch (Exception ex) {
      log(-1, "extractResourceToFile: %s\n%s", outFile, ex);
      return false;
    }
    return true;
  }
  
  void copy(InputStream in, OutputStream out) throws IOException {
    byte[] tmp = new byte[8192];
    int len;
    while (true) {
      len = in.read(tmp);
      if (len <= 0) {
        break;
      }
      out.write(tmp, 0, len);
    }
  }

  public List<String> resourceList(URL folder) {
    log(lvl + 1, "resourceList:\n%s", folder);
    return doResourceList(folder, false);
  }
  
  public List<String> resourceListDeep(URL folder) {
    log(lvl + 1, "resourceListDeep:\n%s", folder);
    return doResourceList(folder, true);
  }
  
  List<String> doResourceList(URL uFolder, boolean deep) {
    List<String> files = new ArrayList<String>();
    File fFolder = null;
    try {
      fFolder = new File(uFolder.toURI());
      files.add(uFolder.getPath());
      return doResourceListFolder(fFolder, deep, files);
    } catch (Exception ex) {
      if (!"jar".equals(uFolder.getProtocol())) {
        terminate(1, "URL(uLibsFrom) neither folder nor jar: " + ex.toString());
      }
    }
    String[] parts = uFolder.getPath().split("!");
    if (parts.length < 2 || !parts[0].startsWith("file:")) {
      terminate(1, "doResourceList: not a valid jar URL: " + uFolder.getPath());
    }
    String fpJar = parts[0].substring(5);
    String fpFolder = parts[1];
    files.add(fpFolder);
    return doResourceListJar(uFolder, fpFolder, deep, files);
  }
  
  List<String> doResourceListFolder(File fFolder, boolean deep, List<String> files) {
    if (fFolder.isDirectory()) {
      if (testing) {
        logp("scanning folder:\n%s", fFolder);
      }
      String[] subList = fFolder.list();
      for (String entry : subList) {
        File fEntry = new File(fFolder, entry);
        if (fEntry.isDirectory()) {
          if (deep) {
            doResourceListFolder(fEntry, deep, files);
          }
        } else {
          if (testing) {
            logp("adding: %s", entry);
          }
          files.add(fEntry.getAbsolutePath().substring(1 + files.get(0).length()));
        }        
      }
    }
    return files;
  }

  List<String> doResourceListJar(URL uJar, String folder, boolean deep, List<String> files) {
    ZipInputStream zJar;
    String fpJar = uJar.getPath().split("!")[0];
    if (!fpJar.endsWith(".jar")) {
      return files;
    }
    folder = folder.startsWith("/") ? folder.substring(1) : folder;
    ZipEntry zEntry;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        String zePath = zEntry.getName();
        if (!zePath.endsWith("/") && zePath.startsWith(folder)) {
          String zeName = zePath.substring(folder.length() + 1);
          if (testing) {
            logp("adding: %s", zeName);
          }
          files.add(zeName);
        }
      }
    } catch (Exception ex) {
      return files;
    }
    return files;
  }

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
			} catch (IOException ex) {}
			if (!jyversion.isEmpty() && !jyversion.startsWith("2.5")) {
				Sikulix.popError(String.format("The bundled Jython %s\n"
								+ "cannot be used on Java 6!\n"
								+ "Run setup again in this environment.\n"
								+ "Click OK to terminate now", jyversion));
				System.exit(1);
			}
		}

    Settings.isRunningIDE = true;
    
    if (Settings.isMac()) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      if (!Settings.isMacApp) {
        if (!Sikulix.popAsk("This use of SikuliX is not supported\n"
                + "and might lead to misbehavior!\n"
                + "Click YES to continue (you should be sure)\n"
                + "Click NO to terminate and check the situation.")) {
          System.exit(1);
        }
      } else {
        log(lvl, "running on Mac as SikuliX.app");
      }
    }
    
    log(lvl, "initIDEbefore: leaving");
  }
  
  private void initIDEafter() {
    log(lvl, "initIDEafter: entering");
    log(lvl, "initIDEafter: leaving");
 }  

  private void initAPI() {
    log(lvl, "initAPI: entering");
    log(lvl, "initAPI: leaving");
  }  

  private void initSetup() {
    log(lvl, "initSetup: entering");
    log(lvl, "initSetup: leaving");
  } 
  
  private boolean checkLibs(File flibsFolder) {
    // 1.1-MadeForSikuliX64M.txt
    String name = String.format("1.1-MadeForSikuliX%d%s.txt", javaArch, runningOn.toString().substring(0, 1));
    if (! new File(flibsFolder, name).exists()) {
      log(lvl, "libs folder empty or has wrong content");
      return false;
    }
    return true;
  }

  private void storeClassPath() {
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    classPath = Arrays.asList(sysLoader.getURLs());
  }
  
  public void dumpClassPath() {
    dumpClassPath(null);
  }

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

  public String isOnClasspath(String artefact) {
    String cpe = null;
    if (classPath.isEmpty()) {
      storeClassPath();
    }
    for (URL e : classPath) {
      if (e.getPath().contains(artefact)) {
        cpe = e.getPath();
      }
    }
    return cpe;
  }
  
  public boolean addToClasspath(String jar) {
    log(lvl, "addToClasspath: " + jar);
		File jarFile = new File(jar);
		if (!jarFile.exists()) {
			log(-1, "does not exist - not added");
			return false;
		}
    Method method;
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
    try {
      if (jar.endsWith(".jar")) {
        jar = FileManager.slashify(jarFile.getAbsolutePath(), false);
        if (Settings.isWindows()) {
          jar = "/" + jar;
        }
      }
      URL u = (new URI("file", jar, null)).toURL();
      
      method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(sysLoader, new Object[]{u});
    } catch (Exception ex) {
      log(-1, "Did not work: %s", ex.getMessage());
      return false;
    }
    storeClassPath();
    return true;
  }
  
  public void dumpSysProps() {
    dumpSysProps(null);
  }

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
  
  
  public boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
  }
  
  public boolean isRunningFromJar() {
    return runningJar;
  }

  public String runcmd(String cmd) {
    return runcmd(new String[]{cmd});
  }

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
      result = String.format(error + "%s",e);
      retVal = 9999;
      hasError = true;
    }
    result = String.format("%d\n%s", retVal, result);
    if (hasError) {
      result += error;
    }
    return result;
  }
}
