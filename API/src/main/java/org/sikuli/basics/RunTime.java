/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2014
 */
package org.sikuli.basics;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
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
  int lvl = 3;
  void log(int level, String message, Object... args) {
    Debug.logx(level, String.format(me, runType) + message, args);
  }
  
  void terminate(int retval, String msg) {
    log(-1, "*** terminating: " + msg);
    System.exit(retval);
  }

  static RunTime runTime = null;
  
  Type runType = Type.INIT;
  public static synchronized RunTime get(Type typ) {
    if (runTime == null) {
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
    
  RunTime() {}
  
  public String jreVersion = java.lang.System.getProperty("java.runtime.version");
	public Preferences options = Preferences.userNodeForPackage(Sikulix.class);
  public ClassLoader classLoader = RunTime.class.getClassLoader();
	public String baseJar = "";
  public String userName = "";
  public String BaseTempPath = "";
  
  List<URL> classPath = new ArrayList<URL>();
  File fTempPath = null;
  File fBaseTempPath = null;
  File fUserDir = null;
  File fWorkDir = null;
  public File fSxBase = null;
  public File fSxBaseJar = null;
  boolean runningJar = true;
  boolean runningWindows = false;
  boolean runningMac = false;
  boolean runningLinux = false;
  boolean runningWinApp = false;
  boolean runningMacApp = false;
  final String osNameSysProp = System.getProperty("os.name");
  final String osVersionSysProp = System.getProperty("os.version");
  String osName = "NotKnown";
  String osVersion = "";

  private void init(Type typ) {
    log(lvl, "global init: entering");
    
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
    
    if (Debug.getDebugLevel() > 3) {
      dumpClassPath("sikuli");
    }
    Class ref = RunTime.class;
    try {
      if (Type.IDE.equals(typ)) {
        ref = Class.forName("org.sikuli.ide.SikuliIDE");
      } else if (Type.SETUP.equals(typ)) {
        ref = Class.forName("org.sikuli.setup.RunSetup");
      }
    } catch (Exception ex) {
    }
    CodeSource codeSrc = ref.getProtectionDomain().getCodeSource();
    String base = null;
    if (codeSrc != null && codeSrc.getLocation() != null) {
        base = codeSrc.getLocation().getPath();
    }
    String appType = "from a jar";
    if (base != null) {
      fSxBaseJar = new File(base); 
      String jn = fSxBaseJar.getName();
      fSxBase = fSxBaseJar.getParentFile();
      log(lvl, "runs as %s in: %s", jn, fSxBase.getAbsolutePath());
      if(jn.contains("classes")) {
        runningJar = false;
        log(lvl, "not running from a jar - supposing Maven project context");
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
    
    if (runningWindows) {
      osVersion = osVersionSysProp;     
    } else if (runningMac) {
      osVersion = osVersionSysProp;    
    } else if (runningLinux) {
      osVersion = osVersionSysProp;
    } else {
      terminate(-1, "running on not supported System: %s");
    }
    log(lvl, "running on %s (%s) %s", osName, osVersion, appType);

    runType = typ;
    log(lvl, "global init: leaving");
  }
  
  private void initAPI() {
    log(lvl, "initAPI: entering");
    log(lvl, "initAPI: leaving");
  }  

  private void initSetup() {
    log(lvl, "initSetup: entering");
    log(lvl, "initSetup: leaving");
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

  private void storeClassPath() {
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    classPath = Arrays.asList(sysLoader.getURLs());
  }
  
  private void dumpClassPath(String filter) {
    if (Debug.getDebugLevel() < 3) {
      return;
    }
    filter = filter == null ? "" : filter; 
    log(lvl, "*** classpath dump %s", filter);
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
      System.out.println(String.format("%3d: %s", n, sEntry));
      n++;
    }
    log(lvl, "*** classpath dump end");
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
      jar = FileManager.slashify(jarFile.getAbsolutePath(), false);
      if (Settings.isWindows()) {
        jar = "/" + jar;
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
