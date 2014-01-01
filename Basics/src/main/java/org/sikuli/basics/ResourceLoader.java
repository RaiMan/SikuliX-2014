/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2012
 */
package org.sikuli.basics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceLoader implements IResourceLoader {

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private String me = "ResourceLoaderBasic";
  private String mem = "...";
  private int lvl = 3;

  private void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + mem + ": " + message, args);
  }

  private void log0(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }
  //</editor-fold>
  
  private String loaderName = "basic";
  private static final String NL = String.format("%n");
  private static final String cmdRegCheck = "reg QUERY HKCU";
  private static final String cmdRegQuery = "reg QUERY %s /v %s";
  private static final String cmdRegAdd = "reg ADD %s /v %s /t %s /f /d %s ";
  private Map<String, String[]> regMap = new HashMap<String, String[]>();
  private StringBuffer alreadyLoaded = new StringBuffer("");
  private ClassLoader cl;
  private CodeSource codeSrc;
  private String jarParentPath = null;
  private String jarPath = null;
  private URL jarURL = null;
  private URL libsURL = null;
  private URL tessURL = null;
  private String fileList = "/filelist.txt";
  private static final String sikhomeEnv = System.getenv("SIKULIX_HOME");
  private static final String sikhomeProp = System.getProperty("sikuli.Home");
  private static final String userdir = System.getProperty("user.dir");
  private static final String userhome = System.getProperty("user.home");
  private String libPath = null;
  private String libPathFallBack = null;
  private File libsDir = null;
  private static final String checkFileNameAll = Settings.getVersionShortBasic() + "-MadeForSikuliX";
  private String checkFileNameMac = checkFileNameAll + "64M.txt";
  private String checkFileNameW32 = checkFileNameAll + "32W.txt";
  private String checkFileNameW64 = checkFileNameAll + "64W.txt";
  private String checkFileNameL32 = checkFileNameAll + "32L.txt";
  private String checkFileNameL64 = checkFileNameAll + "64L.txt";
  private String checkFileName = null;
  private String checkLib = null;
  private static final String prefixSikuli = "SikuliX";
  private static final String suffixLibs = "/libs";
  private static final String libSub = prefixSikuli + suffixLibs;
  private String userSikuli = null;
  private boolean extractingFromJar = false;
  private boolean itIsJython = false;
  /**
   * Mac: standard place for native libs
   */
  private static String libPathMac = Settings.appPathMac + "/libs";
  /**
   * in-jar folder to load other ressources from
   */
  private static String jarResources = "META-INF/res/";
  /**
   * in-jar folder to load native libs from
   */
  private static final String libSourcebase = "META-INF/libs/";
  private static String libSource32 = libSourcebase + "%s/libs32/";
  private static String libSource64 = libSourcebase + "%s/libs64/";
  private String libSource;
  
  private String osarch;
  private String javahome;

  public ResourceLoader() {
    log0(lvl, "SikuliX Package Build: %s %s", Settings.getVersionShort(), RunSetup.timestampBuilt);
    cl = this.getClass().getClassLoader();
    codeSrc = this.getClass().getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      jarURL = codeSrc.getLocation();
      jarPath = jarURL.getPath();
      jarParentPath = FileManager.slashify((new File(jarPath)).getParent(), true);
      if (jarPath.endsWith(".jar")) {
        extractingFromJar = true;
      } else {
        jarPath = FileManager.slashify((new File(jarPath)).getAbsolutePath(), true);
      }
      if (Settings.isMac()) {
        if (jarParentPath.startsWith(Settings.appPathMac)) {
          log0(lvl, "Sikuli-IDE is running from /Applications folder");
          Settings.isMacApp = true;
        }
      }
    } else {
      log(-1, "Fatal Error 101: Not possible to access the jar files!");
      SikuliX.terminate(101);
    }
    regMap.put("EnvPath", new String[]{"HKEY_CURRENT_USER\\Environment", "PATH", "REG_EXPAND_SZ"});
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(String[] args) {
    //Debug.log(lvl, "%s: %s: init", me, loaderName);
  }

  private boolean isFatJar() {
    if (extractingFromJar) {
      try {
        ZipInputStream zip = new ZipInputStream(jarURL.openStream());
        ZipEntry ze;
        while ((ze = zip.getNextEntry()) != null) {
          String entryName = ze.getName();
          if (entryName.startsWith(libSourcebase)) {
            return true;
          }
        }
      } catch (IOException e) {
        return false;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void check(String what) {
    mem = "check";

    if (!what.equals(Settings.SIKULI_LIB)) {
      log(-1, "Currently only Sikuli libs supported!");
      return;
    }

    if (System.getProperty("sikuli.DoNotExport") == null && !isFatJar()) {
      libsURL = null;
      if (jarPath.contains("Basics")) {
        try {
          SikuliX.addToClasspath(jarPath.replace("Basics", "Libs"));
          libsURL = new URL(jarURL.toString().replace("Basics", "Libs"));
          tessURL = new URL(jarURL.toString().replace("Basics", "Tesseract"));
          log(-1, "The jar in use was not built with setup!\n"
                  + "We might be running from local Maven repository?\n" + jarPath);
        } catch (Exception ex) {
        }
      }
      if (libsURL == null) {
        RunSetup.popError("Terminating: The jar in use was not built with setup!\n" + jarPath);
        System.exit(1);
      }
    }

    if (libPath == null || libsDir == null) {
      libPath = null;
      libsDir = null;
      File libsfolder;
      String libspath;

      // check the bit-arch
      osarch = System.getProperty("os.arch");
      log(lvl - 1, "we are running on arch: " + osarch);
      javahome = FileManager.slashify(System.getProperty("java.home"), true);
      log(lvl - 1, "using Java at: " + javahome);

      if (userhome != null) {
        if (Settings.isWindows()) {
          userSikuli = System.getenv("HOMEPATH");
          if (userSikuli != null) {
            userSikuli = FileManager.slashify(userSikuli, true) + prefixSikuli;
          }
        } else {
          userSikuli = FileManager.slashify(userhome, true) + prefixSikuli;
        }
      }

      //  Mac specific 
      if (Settings.isMac()) {
        if (!osarch.contains("64")) {
          log(-1, "Mac: only 64-Bit supported");
          SikuliX.terminate(0);
        }
        libSource = String.format(libSource64, "mac");
        checkFileName = checkFileNameMac;
        checkLib = "MacUtil";
//TODO libs dir fallback
//        if ((new File(libPathMac)).exists()) {
//          libPathFallBack = libPathMac;
//        }
      }

      // Windows specific 
      if (Settings.isWindows()) {
        if (osarch.contains("64")) {
          libSource = String.format(libSource64, "windows");
          checkFileName = checkFileNameW64;
//TODO libs dir fallback
//          if ((new File(libPathWin)).exists()) {
//            libPathFallBack = libPathWin;
//          }
        } else {
          libSource = String.format(libSource32, "windows");
          checkFileName = checkFileNameW32;
//TODO libs dir fallback
//          if ((new File(libPathWin)).exists()) {
//            libPathFallBack = libPathWin;
//          } else if ((new File(libPathWin32)).exists()) {
//            libPathFallBack = libPathWin32;
//          }
        }
        checkLib = "WinUtil";
      }

      // Linux specific
      if (Settings.isLinux()) {
        if (osarch.contains("64")) {
          libSource = String.format(libSource64, "linux");
          checkFileName = checkFileNameL64;
        } else {
          libSource = String.format(libSource32, "linux");
          checkFileName = checkFileNameL32;
        }
        checkLib = "JXGrabKey";
      }

      if (!Settings.runningSetup) {
        // check Java property sikuli.home
        if (sikhomeProp != null) {
          libspath = FileManager.slashify(sikhomeProp, true) + "libs";
          if ((new File(libspath)).exists()) {
            libPath = libspath;
          }
          log(lvl, "Exists Property.sikuli.Home? %s: %s", libPath == null ? "NO" : "YES", libspath);
          libsDir = checkLibsDir(libPath);
        }

        // check environmenet SIKULIX_HOME
        if (libPath == null && sikhomeEnv != null) {
          libspath = FileManager.slashify(sikhomeEnv, true) + "libs";
          if ((new File(libspath)).exists()) {
            libPath = libspath;
          }
          log(lvl, "Exists Environment.SIKULIX_HOME? %s: %s", libPath == null ? "NO" : "YES", libspath);
          libsDir = checkLibsDir(libPath);
        }

        // check parent folder of jar file
        if (libPath == null && jarPath != null) {
          if (jarPath.endsWith(".jar")) {
            if (libsURL == null) {
              String lfp = jarParentPath + "libs";
              libsfolder = (new File(lfp));
              if (libsfolder.exists()) {
                libPath = lfp;
              }
              if (Settings.isMacApp) {
                libPath = libPathMac;
              }
              log(lvl, "Exists libs folder at location of jar? %s: %s", libPath == null ? "NO" : "YES", jarParentPath);
              libsDir = checkLibsDir(libPath);
              if (libsDir == null && System.getProperty("sikuli.DoNotExport") != null) {
                log(-1, "No valid libs folder with option sikuli.DoNotExport");
                System.exit(1);
              }
            }
          } else {
            log(lvl, "not running from jar: " + jarParentPath);
          }
        }

        // check the users home folder
        if (libPath == null && userSikuli != null) {
          File ud = new File(userSikuli + suffixLibs);
          if (ud.exists()) {
            libPath = ud.getAbsolutePath();
          }
          log(lvl, "Exists libs folder in user home folder? %s: %s", libPath == null ? "NO" : "YES",
                  ud.getAbsolutePath());
          libsDir = checkLibsDir(libPath);
        }

        // check the working directory and its parent
        if (libPath == null && userdir != null) {
          File wd = new File(userdir);
          File wdp = new File(userdir).getParentFile();
          File wdl = new File(FileManager.slashify(wd.getAbsolutePath(), true) + libSub);
          File wdpl = new File(FileManager.slashify(wdp.getAbsolutePath(), true) + libSub);
          if (wdl.exists()) {
            libPath = wdl.getAbsolutePath();
          } else if (wdpl.exists()) {
            libPath = wdpl.getAbsolutePath();
          }
          log(lvl, "Exists libs folder in working folder or its parent? %s: %s", libPath == null ? "NO" : "YES",
                  wd.getAbsolutePath());
          libsDir = checkLibsDir(libPath);
        }

        if (libPath == null && libPathFallBack != null) {
          libPath = libPathFallBack;
          log(lvl, "Checking available fallback for libs folder: " + libPath);
          libsDir = checkLibsDir(libPath);
        }
      }
    }

    if (libsDir == null && libPath != null) {
      log(-1, "libs dir is empty, has wrong content or is outdated");
      log(-2, "Please wait! Trying to extract libs to: " + libPath);
      if (!FileManager.deleteFileOrFolder(libPath,
              new FileManager.fileFilter() {
        @Override
        public boolean accept(File entry) {
          if (entry.getPath().contains("tessdata")
                  || entry.getPath().contains("Lib")) {
            return false;
          }
          return true;
        }
      })) {
        log(-1, "Fatal Error 102: not possible to empty libs dir");
        RunSetup.popError("Problem with SikuliX libs folder - see error log");
        SikuliX.terminate(102);
      }
      File dir = (new File(libPath));
      dir.mkdirs();
      if (extractLibs(dir.getParent(), libSource) == null) {
        log(-1, "not possible!");
        libPath = null;
      }
      libsDir = checkLibsDir(libPath);
    }

    //<editor-fold defaultstate="collapsed" desc="libs dir finally invalid">
    if (libPath == null) {
      log(-1, "No valid libs path available until now!");
      if (libPath == null && jarParentPath != null) {
        if (jarPath.endsWith(".jar") && libsURL == null) {
          log(-2, "Please wait! Trying to extract libs to jar parent folder: " + jarParentPath);
          File jarPathLibs = extractLibs((new File(jarParentPath)).getAbsolutePath(), libSource);
          if (jarPathLibs == null) {
            log(-1, "not possible!");
          } else {
            libPath = jarPathLibs.getAbsolutePath();
          }
        }
      }
      if (libPath == null && userSikuli != null) {
        log(-2, "Please wait! Trying to extract libs to user home: " + userSikuli);
        File userhomeLibs = extractLibs((new File(userSikuli)).getAbsolutePath(), libSource);
        if (userhomeLibs == null) {
          log(-1, "not possible!");
        } else {
          libPath = userhomeLibs.getAbsolutePath();
        }
      }
      libsDir = checkLibsDir(libPath);
      if (libPath == null || libsDir == null) {
        log(-1, "Fatal Error 103: No valid native libraries folder available - giving up!");
        RunSetup.popError("Problem with SikuliX libs folder - see error log");
        SikuliX.terminate(103);
      }
    }
    //</editor-fold>

    if (Settings.isLinux()) {
      File libsLinux = new File(libsDir.getParent(), "libsLinux/libVisionProxy.so");
      if (libsLinux.exists()) {
        log(lvl, "Trying to use provided library at: " + libsLinux.getAbsolutePath());
        try {
          FileManager.xcopy(libsLinux.getAbsolutePath(),
                  new File(libPath, "libVisionProxy.so").getAbsolutePath(), null);
        } catch (IOException ex) {
          log(-1, "... did not work: " + ex.getMessage());
          RunSetup.popError("Provided libVisionProxy not useable - see error log");
          SikuliX.terminate(0);
        }
      }
    }

    //convenience: jawt.dll in libsdir avoids need for java/bin in system path
    if (Settings.isWindows()) {
      String lib = "jawt.dll";
      try {
        extractResource(javahome + "bin/" + lib, new File(libPath, lib), false);
      } catch (IOException ex) {
        log(-1, "Fatal error 107: problem copying " + lib + "\n" + ex.getMessage());
        RunSetup.popError("Trying to add jawt.dll from Java at\n"
                + javahome + " to SikuliX libs folder ..."
                + "... but did not work - see error log");
        SikuliX.terminate(107);
      }
    }

    if (itIsJython) {
      export("Lib/sikuli", libsDir.getParent());
    }

    if (Settings.OcrDataPath == null && System.getProperty("sikuli.DoNotExport") == null) {
      if (Settings.isWindows() || Settings.isMac()) {
        Settings.OcrDataPath = libPath;
      } else {
        Settings.OcrDataPath = "/usr/local/share";
      }
      log(lvl, "If OCR/Text activated: Using as OCR directory (tessdata): " + Settings.OcrDataPath);
    }
  }

  private File checkLibsDir(String path) {
    String memx = mem;
    mem = "checkLibsDir";
    File dir = null;
    if (path != null) {
      log(lvl, path);
      if (!Settings.runningSetup && Settings.isWindows()) {
        // is on system path?
        String syspath = System.getenv("PATH");
        path = (new File(path).getAbsolutePath()).replaceAll("/", "\\");
        if (!syspath.toUpperCase().contains(path.toUpperCase())) {
          String error = "*** error ***";
          log(-1, "libs dir is not on system path: " + path);
          if (Debug.getDebugLevel() >= lvl) {
            for (String e : syspath.split(";")) {
              System.out.println(e);
            }
          }
          log(-2, "Please wait! Trying to add it to user's path");
          if (runcmd(cmdRegCheck).startsWith(error)) {
            log(-1, "Fatal Error 104: Not possible to access registry!");
            RunSetup.popError("Trying to add SikuliX libs to user path\n"
                    + "But registry not accessible - see error log");
            SikuliX.terminate(104);
          }
          String[] val = regMap.get("EnvPath");
          String envPath = "";
          String newPath;
          int step = 0;
          String cmdQ = String.format(cmdRegQuery, val[0], val[1]);
          String regResult = runcmd(cmdQ);
          if (regResult.startsWith(error)) {
            log(lvl, "users PATH seems to be empty: " + regResult);
            step = 1;
          } else {
            String[] regResultLines = regResult.split(NL);
            for (String line : regResultLines) {
              line = line.trim();
              if (step == 0 && line.startsWith(val[0])) {
                step = 1;
                continue;
              }
              if (step == 1 && line.startsWith(val[1]) && line.contains(val[2])) {
                step = 1;
                envPath = line;
                continue;
              }
              if (!line.isEmpty()) {
                log(lvl, line);
              }
            }
          }
          if (step == 0) {
            log(-1, "Fatal Error 105: Not possible to get user's PATH from registry");
            RunSetup.popError("Trying to add SikuliX libs to user path\n"
                    + "But registry not accessible - see error log");
            SikuliX.terminate(105);
          } else {
            if (!envPath.isEmpty()) {
              envPath = envPath.substring(envPath.indexOf(val[2]) + val[2].length()).trim();
              log(lvl, "current:(%s %s): %s", val[0], val[1], envPath);
              if (envPath.toUpperCase().contains(path.toUpperCase())) {
                log(-1, "Logout and Login again! (Since libs folder is in user's path, but not activated)");
                RunSetup.popInfo("Please Logout and Login again!\n\n"
                        + "SikuliX libs path: " + path + "\n"
                        + "is in System Path environment settings, \n"
                        + "but not seen in current runtime environment.\n"
                        + "Logout/Login should fix this problem.");
                SikuliX.terminate(0);
              }
            }
            newPath = path.trim() + (envPath.isEmpty() ? "" : ";" + envPath);
            String finalPath = newPath.replaceAll(" ", "%20;");
            String cmdA = String.format(cmdRegAdd, val[0], val[1], val[2], finalPath);
            regResult = runcmd(cmdA);
            log(lvl, regResult);
            regResult = runcmd(cmdQ);
            log(lvl, "Changed to: " + regResult);
            if (!regResult.contains(path)) {
              RunSetup.popError("Trying to add SikuliX libs to user path\n"
                      + "But registry not accessible - see error log");
              log(-1, "Fatal error 106: libs folder could not be added to PATH - giving up!");
              SikuliX.terminate(106);
            }
          }
          log(-1, "Successfully added the libs folder to users PATH!\n" + ""
                  + "RESTART all processes/IDE's using Sikuli for new PATH to be used!/n"
                  + "For usages from command line logout/login might be necessary!");
          RunSetup.popInfo("Please Logout and Login again!\n\n"
                  + "SikuliX libs path: " + path + "\n"
                  + "was added to user path. \n"
                  + "Logout/Login should activate this setting.");
          SikuliX.terminate(0);
        }
      }
      if (System.getProperty("sikuli.DoNotExport") != null) {
        dir = new File(path);
      } else {
        File checkFile = (new File(FileManager.slashify(path, true) + checkFileName));
        if (checkFile.exists()) {
          if ((new File(jarPath)).lastModified() > checkFile.lastModified()) {
            log(-1, "libs folder outdated!");
          } else {
            loadLib(checkLib);
            log(lvl, "Using libs at: " + path);
            dir = new File(path);
          }
        } else {
          if (Settings.isWindows()) {
            // might be wrong arch
            if ((new File(FileManager.slashify(path, true) + checkFileNameW32)).exists()
                    || (new File(FileManager.slashify(path, true) + checkFileNameW64)).exists()) {
              log(-1, "libs dir contains wrong arch for " + osarch);
            }
          } else {
            log(-1, "Not a valid libs dir for SikuliX (" + osarch + "): " + path);
          }
        }
      }
    }
    mem = memx;
    return dir;
  }

  //<editor-fold defaultstate="collapsed" desc="overwritten">
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean export(String res, String target) {
    String memx = mem;
    mem = "export";
    log(lvl, "Trying to access package for exporting: %s\nto: %s", res, target);
    String resOrg = res;
    boolean fastReturn = false;
    int prefix = 1 + resOrg.indexOf("#");
    if (prefix > 0) {
      res = res.replace("#", "/");
      fastReturn = true;
    }
    if (!extractingFromJar) {
      prefix += jarPath.length();
      if (Settings.isWindows()) {
        prefix -= 1;
      }
    }
    URL currentURL = jarURL;
//TODO special export cases from jars not on class path
    if (res.contains("tessdata")) {
      currentURL = tessURL;
    }
    List<String[]> entries = makePackageFileList(currentURL, res, true);
    if (entries == null || entries.isEmpty()) {
      return false;
    }
    String targetName = null;
    File targetFile;
    long targetDate;
    for (String[] e : entries) {
      try {
        if (prefix > 0) {
          targetFile = new File(target, e[0].substring(prefix));
        } else {
          targetFile = new File(target, e[0]);
        }
        if (targetFile.exists()) {
          targetDate = targetFile.lastModified();
        } else {
          targetDate = 0;
          targetFile.getParentFile().mkdirs();
        }
        if (targetDate == 0 || targetDate < Long.valueOf(e[1])) {
          extractResource(e[0], targetFile, extractingFromJar);
          log(lvl + 2, "is from: %s (%d)", e[1], targetDate);
        } else {
          log(lvl + 2, "already in place: " + targetName);
          if (fastReturn) {
            return true;
          }
        }
      } catch (IOException ex) {
        log(-1, "IO-problem extracting: %s\n%s", targetName, ex.getMessage());
        mem = memx;
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void install(String[] args) {
    mem = "install";
    log(lvl, "entered");
    //extractLibs(args[0]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean doSomethingSpecial(String action, Object[] args) {
    if ("loadLib".equals(action)) {
      loadLib((String) args[0]);
      return true;
    } else if ("runcmd".equals(action)) {
      String retval = runcmd((String[]) args);
      args[0] = retval;
      return true;
    } else if ("checkLibsDir".equals(action)) {
      return (libsDir != null);
    } else if ("itIsJython".equals(action)) {
      itIsJython = true;
      return true;
    } else if ("exportTessdata".equals(action)) {
      if (tessURL != null) {
        SikuliX.addToClasspath(jarPath.replace("Basics", "Tesseract"));
      }
      if (!new File(Settings.OcrDataPath, "tessdata").exists()) {
        log(lvl, "Trying to extract tessdata folder since it does not exist yet.");
        export("META-INF/libs#tessdata", libPath);
      }
      return true;
    } else {
      return false;
    }
  }

  private String runcmd(String cmd) {
    return runcmd(new String[]{cmd});
  }

  private String runcmd(String args[]) {
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
    if (args[0].startsWith("#")) {
      String pgm = args[0].substring(1);
      args[0] = (new File(libsDir, pgm)).getAbsolutePath();
      runcmd(new String[]{"chmod", "ugo+x", args[0]});
    }
    String memx = mem;
    mem = "runcmd";
    String result = "";
    String error = "*** error ***" + NL;
    try {
      log(lvl, SikuliX.arrayToString(args));
      Debug.info("runcmd: " + SikuliX.arrayToString(args));
      Process process = Runtime.getRuntime().exec(args);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String s;
      while ((s = stdInput.readLine()) != null) {
        if (!s.isEmpty()) {
          result += s + NL;
        }
      }
      if ((s = stdError.readLine()) != null) {
        result = error + result;
        result += s;
      }
    } catch (Exception e) {
      log(-1, "fatal error: " + e.getMessage());
      result = error + e.getMessage();
    }
    mem = memx;
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return loaderName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getResourceTypes() {
    return Settings.SIKULI_LIB;
  }

  /**
   * make sure, a native library is available and loaded
   *
   * @param libname System.loadLibrary() compatible library name
   * @return the extracted File object
   * @throws IOException
   */
  public void loadLib(String libname) {
    String memx = mem;
    mem = "loadLib";
    if (libname == null || "".equals(libname)) {
      log(-1, "libname == null");
      mem = memx;
      return;
    }
    if (alreadyLoaded.indexOf("*" + libname) < 0) {
      alreadyLoaded.append("*").append(libname);
    } else {
      log(lvl, "Is already loaded: " + libname);
      mem = memx;
      return;
    }
    log(lvl, libname);
    if (libPath == null) {
      log(-1, "Fatal Error 108: No libs directory available");
      RunSetup.popError("Problem with SikuliX libs folder - see error log");
      SikuliX.terminate(108);
    }
    String mappedlib = System.mapLibraryName(libname);
    if (Settings.isMac()) {
      if (mappedlib.endsWith(".jnilib")) {
        mappedlib = mappedlib.replace(".jnilib", ".dylib");
      }
    }
    String lib = new File(libPath, mappedlib).getAbsolutePath();
    if (!new File(lib).exists()) {
      if (!Settings.isLinux()) {
        log(-1, "Fatal Error 109: not found: " + lib);
        RunSetup.popError("Problem with SikuliX libs folder - see error log");
        SikuliX.terminate(109);
      } else {
        lib = mappedlib;
        log(lvl, "Linux: %s not bundled - trying to load from system paths", lib);
      }
    } else {
      log(lvl, "Found: " + libname + " at " + lib);
    }
    try {
      System.load(lib);
    } catch (Error e) {
      log(-1, "Fatal Error 110: loading: " + mappedlib);
      log(-1, "Since native library was found, it might be a problem with needed dependent libraries\n%s",
              e.getMessage());
      if (Settings.isWindows()) {
        log(-1, "Check, wether a valid Sikuli libs folder is in system path at runtime!");
        if (Settings.runningSetup) {
          log(-1, "Running Setup: ignoring this error for now");
          mem = memx;
          return;
        }
      }
      RunSetup.popError("Problem with SikuliX libs folder - see error log");
      SikuliX.terminate(110);
    }
    log(lvl, "Now loaded: " + libname);
    mem = memx;
  }
  //</editor-fold>

  private File extractLibs(String targetDir, String libSource) {
    String memx = mem;
    mem = "extractLibs";
    List<String[]> libsList = makePackageFileList(
            (libsURL != null) ? libsURL : jarURL, libSource, false);
    if (libsList == null) {
      mem = memx;
      return null;
    }
    targetDir = FileManager.slashify(targetDir, true);
    String targetDirLibs = targetDir + "libs";
    (new File(targetDirLibs)).mkdirs();
    String targetName = null;
    File targetFile;
    long targetDate;
    for (String[] e : libsList) {
      try {
        targetName = e[0].substring(e[0].lastIndexOf("/") + 1);
        targetFile = new File(targetDirLibs, targetName);
        if (targetFile.exists()) {
          targetDate = targetFile.lastModified();
        } else {
          targetDate = 0;
        }
        if (targetDate == 0 || targetDate < Long.valueOf(e[1])) {
          extractResource(e[0], targetFile, extractingFromJar);
          log(lvl + 2, "is from: %s (%d)", e[1], targetDate);
        } else {
          log(lvl + 2, "already in place: " + targetName);
        }
      } catch (IOException ex) {
        log(lvl, "IO-problem extracting: %s\n%s", targetName, ex.getMessage());
        mem = memx;
        return null;
      }
    }
    mem = memx;
    return new File(targetDirLibs);
  }

  private List<String[]> makePackageFileList(URL jar, String path, boolean deep) {
    List<String[]> fList = new ArrayList<String[]>();
    int iFile = 0;
    if (extractingFromJar) {
      try {
        ZipInputStream zip = new ZipInputStream(jar.openStream());
        ZipEntry ze;
        log(lvl, "Accessing jar: " + jar.toString());
        while ((ze = zip.getNextEntry()) != null) {
          String entryName = ze.getName();
          if (entryName.startsWith(path)
                  && !entryName.endsWith("/")) {
            log(lvl + 2, "%d: %s", iFile, entryName);
            fList.add(new String[]{FileManager.slashify(entryName, false),
              String.format("%d", ze.getTime())});
            iFile++;
          }
        }
        log(lvl, "Found %d Files in %s", iFile, path);
      } catch (IOException e) {
        log(-1, "Did not work!\n%s", e.getMessage());
        return null;
      }
    } else {
      String p = FileManager.slashify(jar.getPath(), false);
      //TODO hack: to get folder Commands and Lib from Basics 
      if (path.startsWith("Commands/") || path.startsWith("Lib/")) {
        p = p.replace("Natives", "Basics");
      }
      File folder = new File(p, path);
      if (folder.isFile()) {
        fList.add(new String[]{FileManager.slashify(folder.getAbsolutePath(), false),
          String.format("%d", folder.lastModified())});
        log(lvl, "Found 1 file in %s", path);
      } else {
        log(lvl, "accessing folder: " + folder.getAbsolutePath());
        for (File f : getDeepFileList(folder, deep)) {
          log(lvl + 2, "file: " + f.getAbsolutePath());
          fList.add(new String[]{FileManager.slashify(f.getAbsolutePath(), false),
            String.format("%d", f.lastModified())});
          iFile++;
        }
        log(lvl, "Found %d file(s) in %s", iFile, path);
      }
    }
    return fList;
  }

  private List<File> getDeepFileList(File entry, boolean deep) {
    List<File> filelist = new ArrayList<File>();
    if (entry.isDirectory()) {
      for (File f : entry.listFiles()) {
        if (f.isDirectory()) {
          if (!deep) {
            continue;
          }
          filelist.addAll(getDeepFileList(f, deep));
        } else {
          filelist.add(f);
        }
      }
    }
    return filelist;
  }

  /**
   * extract a resource to a writable file
   *
   * @param resourcename the name of the resource on the classpath
   * @param outputfile the file to copy to
   * @return the extracted file
   * @throws IOException
   */
  private File extractResource(String resourcename, File outputfile, boolean isJar) throws IOException {
    InputStream in;
    if (isJar) {
      in = cl.getResourceAsStream(resourcename);
    } else {
      in = new FileInputStream(resourcename);
    }
    if (in == null) {
      throw new IOException("Resource " + resourcename + " not on classpath");
    }
    if (!outputfile.getParentFile().exists()) {
      outputfile.getParentFile().mkdirs();
    }
    log0(lvl + 2, "Extracting resource from: " + resourcename);
    log0(lvl + 2, "Extracting to: " + outputfile.getAbsolutePath());
    copyResource(in, outputfile);
    return outputfile;
  }

  private void copyResource(InputStream in, File outputfile) throws IOException {
    OutputStream out = null;
    try {
      out = new FileOutputStream(outputfile);
      copy(in, out);
    } catch (IOException e) {
      log0(-1, "copyResource Not possible: " + e.getMessage());
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Extract files from a jar using a list of files in a file (def. filelist.txt)
   *
   * @param srcPath from here
   * @param localPath to there (if null, create a default in temp folder)
   * @return the local path to the extracted resources
   * @throws IOException
   */
  private String extractWithList(String srcPath, String localPath) throws IOException {
    mem = "extractWithList";
    if (localPath == null) {
      localPath = Settings.BaseTempPath + File.separator + "sikuli" + File.separator + srcPath;
      new File(localPath).mkdirs();
    }
    log(lvl, "From " + srcPath + " to " + localPath);
    localPath = FileManager.slashify(localPath, true);
    BufferedReader r = new BufferedReader(new InputStreamReader(
            cl.getResourceAsStream(srcPath + fileList)));
    if (r == null) {
      log(-1, "File containing file list not found: " + fileList);
      return null;
    }
    String line;
    InputStream in;
    while ((line = r.readLine()) != null) {
      String fullpath = localPath + line;
      log(lvl, "extracting: " + fullpath);
      File outf = new File(fullpath);
      outf.getParentFile().mkdirs();
      in = cl.getResourceAsStream(srcPath + line);
      if (in != null) {
        copyResource(in, outf);
      } else {
        log(-1, "Not found");
      }
    }
    return localPath;
  }

  /**
   * copy an InputStream to an OutputStream.
   *
   * @param in InputStream to copy from
   * @param out OutputStream to copy to
   * @throws IOException if there's an error
   */
  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] tmp = new byte[8192];
    int len = 0;
    while (true) {
      len = in.read(tmp);
      if (len <= 0) {
        break;
      }
      out.write(tmp, 0, len);
    }
  }
}
