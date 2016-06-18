/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2014
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
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.sikuli.script.RunTime;
//import org.sikuli.script.RunTime;
import org.sikuli.script.Sikulix;

public class ResourceLoader {

  private static ResourceLoader resourceLoader = null;

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static String me = "ResourceLoader";
  private static String mem = "...";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + mem + ": " + message, args);
  }

  private static void log0(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + message, args);
  }
  //</editor-fold>

  private final String loaderName = "basic";
  private static final String NL = String.format("%n");
  private StringBuffer alreadyLoaded = new StringBuffer("");
  private ClassLoader cl;
  private CodeSource codeSrc;
  private String jarParentPath = null;
  private String jarPath = null;
	private URL apiJarURL = null;
  private URL jarURL = null;
  private URL libsURL = null;
  private URL tessURL = null;
  private URL currentURL;
  private String fileList = "/filelist.txt";
  private static final String sikhomeEnv = System.getenv("SIKULIX_HOME");
  private static final String sikhomeProp = System.getProperty("sikuli.Home");
  private static final String userdir = System.getProperty("user.dir");
  private static final String userhome = System.getProperty("user.home");
  private String libPath = null;
  private String libPathFallBack = null;
  private File libsDir = null;
//  private static final String checkFileNameAll = RunTime.get().getVersionShortBasic() + "-MadeForSikuliX";
//  private final String checkFileNameMac = checkFileNameAll + "64M.txt";
//  private final String checkFileNameW32 = checkFileNameAll + "32W.txt";
//  private final String checkFileNameW64 = checkFileNameAll + "64W.txt";
//  private final String checkFileNameL32 = checkFileNameAll + "32L.txt";
//  private final String checkFileNameL64 = checkFileNameAll + "64L.txt";
  private String checkFileName = null;
  private String checkLib = null;
  private final String checkLibWindows = "JIntellitype";
  private static final String prefixSikuli = "SikuliX";
  private static final String suffixLibs = "libs";
  private static final String libSub = prefixSikuli + "/" + suffixLibs;
  private String userSikuli = null;
  public boolean extractingFromJar = false;
  private boolean runningSikulixapi = false;
  private static boolean itIsJython = false;
  /**
   * Mac: standard place for native libs
   */
  private static final String libPathMac = Settings.appPathMac + "/libs";
  /**
   * in-jar folder to load other ressources from
   */
  private static final String jarResources = "META-INF/res/";
  /**
   * in-jar folder to load native libs from
   */
  private static final String libSourcebase = "META-INF/libs/";
  private static final String libSource32 = libSourcebase + "%s/libs32/";
  private static final String libSource64 = libSourcebase + "%s/libs64/";
  private String libSource;

  private String osarch;
  private String javahome;

	private boolean initDone = false;
	private boolean usrPathProblem = false;

  private ResourceLoader() {
//    log0(lvl, "SikuliX Package Build: %s %s", RunTime.get().getVersionShort(), RunTime.get().SikuliVersionBuild);
    cl = this.getClass().getClassLoader();
    codeSrc = this.getClass().getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      jarURL = codeSrc.getLocation();
      jarPath = jarURL.getPath();
      jarParentPath = FileManager.slashify((new File(jarPath)).getParent(), true);
      if (jarPath.endsWith(".jar")) {
//TODO evaluation of running situation should be put in one place
        extractingFromJar = true;
        if (jarPath.contains("sikulixapi")) {
          runningSikulixapi = true;
          org.sikuli.script.Sikulix.setRunningSikulixapi(true);
        }
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
      Sikulix.terminate(101);
    }
  }

  public static ResourceLoader get() {
    if (resourceLoader == null) {
      resourceLoader = new ResourceLoader();
    }
    return resourceLoader;
  }

//<editor-fold defaultstate="collapsed" desc="obsolete">
//public static ResourceLoader forJar(String jarName) {
//    ResourceLoader rl = get();
//    URL jar = null;
//    if (new File(jarName).isAbsolute()) {
//      try {
//        jar = new URL("file", null, jarName);
//      } catch (MalformedURLException ex) {
//        log(-1, "%s", ex);
//      }
//    } else {
//      jar = getJarFromClasspath(jarName);
//    }
//    if (jar != null) {
//      rl.setCurrentJar(jar);
//      return rl;
//    } else {
//      return null;
//    }
//  }

//  private static URL getJarFromClasspath(String jarName) {
//    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//    URL[] urls = sysLoader.getURLs();
//    URL jarurl = null;
//    for (URL url : urls) {
//      if (url.getPath().toLowerCase().contains(jarName)) {
//        jarurl = url;
//        break;
//      }
//    }
//    return jarurl;
//  }
//
//  private void setCurrentJar(URL jar) {
//    currentURL = jar;
//  }
//</editor-fold>

	public URL setApiJarURL(String pURL) {
    apiJarURL = null;
		try {
			apiJarURL = (new URI("file", pURL, null)).toURL();
		} catch (Exception ex) {
		}
    return apiJarURL;
	}

//<editor-fold defaultstate="collapsed" desc="now implemented in RunTime">
//  public boolean check(String what) {
//    mem = "check";
//
//    if (!what.equals(Settings.SIKULI_LIB)) {
//      log(-1, "Currently only Sikuli libs supported!");
//      return false;
//    }
//
//    if (initDone) {
//      return true;
//    }
//
//    if (libPath == null || libsDir == null) {
//      libPath = null;
//      libsDir = null;
//      File libsfolder;
//      String libspath;
//
//      if (System.getProperty("sikuli.DoNotExport") == null && !isFatJar() && !Settings.isWinApp) {
//        libsURL = null;
////TODO evaluation of running situation should be put in one place
//        String jarName = "";
//        String libsJarName = "";
//        String tessJarName = "";
//        if (apiJarURL != null) {
//          libsURL = apiJarURL;
//        } else {
//          if (jarPath.contains("API")) {
//            log(-1, "The jar in use was not built with setup!\n"
//                    + "We might be running from local Maven repository?\n" + jarPath);
//            jarName = "API";
//            libsJarName = "Libs" + Settings.getShortOS();
//            tessJarName = "Tesseract";
//          }
//          if (runningSikulixapi) {
//            log(3, "The jar in use is some sikulixapi.jar\n%s", jarPath);
//            libsJarName = "sikulixlibs" + Settings.getShortOS();
//            jarName = "sikulixapi";
//            tessJarName = "sikulixtessdata";
//          }
//          if (!jarName.isEmpty()) {
//            try {
//              libsURL = new URL(jarURL.toString().replace(jarName, libsJarName));
//              if (!Sikulix.addToClasspath(libsURL.getPath())
//                      || !org.sikuli.script.Sikulix.isOnClasspath(libsJarName)) {
//                libsURL = null;
//              }
//            } catch (Exception ex) {
//              log(-1, "\n%s", ex);
//            }
//          }
//        }
//        if (libsURL == null) {
//          log(-1, "Terminating: The jar was not built with setup nor "
//                  + "can the libs be exported from classpath!\n" + jarPath);
//          Sikulix.terminate(999);
//        }
//      }
//
//      // check the bit-arch
//      osarch = System.getProperty("os.arch");
//      log(lvl - 1, "we are running on arch: " + osarch);
//      javahome = FileManager.slashify(System.getProperty("java.home"), true);
//      log(lvl - 1, "using Java at: " + javahome);
//
//      if (userhome != null) {
//        if (Settings.isWindows()) {
//          userSikuli = System.getenv("USERPROFILE");
//          if (userSikuli != null) {
//            userSikuli = FileManager.slashify(userSikuli, true) + prefixSikuli;
//          }
//        } else {
//          userSikuli = FileManager.slashify(userhome, true) + prefixSikuli;
//        }
//      }
//
//      //  Mac specific
//      if (Settings.isMac()) {
//        if (!osarch.contains("64")) {
//          log(-1, "Mac: only 64-Bit supported");
//          Sikulix.terminate(999);
//        }
//        libSource = String.format(libSource64, "mac");
//        checkFileName = checkFileNameMac;
//        checkLib = "VisionProxy";
////TODO libs dir fallback
////        if ((new File(libPathMac)).exists()) {
////          libPathFallBack = libPathMac;
////        }
//      }
//
//      // Windows specific
//      if (Settings.isWindows()) {
//        if (osarch.contains("64")) {
//          libSource = String.format(libSource64, "windows");
//          checkFileName = checkFileNameW64;
////TODO libs dir fallback
////          if ((new File(libPathWin)).exists()) {
////            libPathFallBack = libPathWin;
////          }
//        } else {
//          libSource = String.format(libSource32, "windows");
//          checkFileName = checkFileNameW32;
////TODO libs dir fallback
////          if ((new File(libPathWin)).exists()) {
////            libPathFallBack = libPathWin;
////          } else if ((new File(libPathWin32)).exists()) {
////            libPathFallBack = libPathWin32;
////          }
//        }
//        checkLib = "VisionProxy";
//      }
//
//      // Linux specific
//      if (Settings.isLinux()) {
//        if (osarch.contains("64")) {
//          libSource = String.format(libSource64, "linux");
//          checkFileName = checkFileNameL64;
//        } else {
//          libSource = String.format(libSource32, "linux");
//          checkFileName = checkFileNameL32;
//        }
//        checkLib = "VisionProxy";
//      }
//
//      if (!Settings.isWinApp && !Settings.runningSetup) {
//        // check Java property sikuli.home
//        if (sikhomeProp != null) {
//          libspath = FileManager.slashify(sikhomeProp, true) + "libs";
//          if ((new File(libspath)).exists()) {
//            libPath = libspath;
//          }
//          log(lvl, "Exists Property.sikuli.Home? %s: %s", libPath == null ? "NO" : "YES", libspath);
//          libsDir = checkLibsDir(libPath);
//        }
//
//        // check environmenet SIKULIX_HOME
//        if (libPath == null && sikhomeEnv != null) {
//          libspath = FileManager.slashify(sikhomeEnv, true) + "libs";
//          if ((new File(libspath)).exists()) {
//            libPath = libspath;
//          }
//          log(lvl, "Exists Environment.SIKULIX_HOME? %s: %s", libPath == null ? "NO" : "YES", libspath);
//          libsDir = checkLibsDir(libPath);
//        }
//
//        // check parent folder of jar file
//        if (libPath == null && jarPath != null) {
//          if (extractingFromJar) {
//            if (libsURL == null || runningSikulixapi) {
//              if (Settings.isMacApp) {
//                libsfolder = new File(libPathMac);
//              } else {
//                libsfolder = (new File(jarParentPath, "libs"));
//              }
//              if (libsfolder.exists()) {
//                libPath = libsfolder.getAbsolutePath();
//              } else if (runningSikulixapi) {
//                if (!libsfolder.mkdirs()) {
//                  log(-1, "running some sikulixapi.jar: cannot create libs folder in\n%s", jarParentPath);
//                } else {
//                  libPath = libsfolder.getAbsolutePath();
//                }
//              }
//              log(lvl, "Exists libs folder at location of jar? %s: %s", libPath == null ? "NO" : "YES", jarParentPath);
//              libsDir = checkLibsDir(libPath);
//              if (libsDir == null) {
//                if (System.getProperty("sikuli.DoNotExport") != null) {
//                  log(-1, "No valid libs folder with option sikuli.DoNotExport");
//                  System.exit(1);
//                }
//              }
//            }
//          } else {
//            log(lvl, "not running from jar: " + jarParentPath);
//          }
//        }
//
//        // check the users home folder
//        if (!Settings.runningSetup && !runningSikulixapi && libPath == null && userSikuli != null) {
//          File ud = new File(userSikuli, suffixLibs);
//          if (ud.exists()) {
//            libPath = ud.getAbsolutePath();
//          }
//          log(lvl, "Exists libs folder in user home folder? %s: %s", libPath == null ? "NO" : "YES",
//                  ud.getAbsolutePath());
//          libsDir = checkLibsDir(libPath);
//        }
//
//        // check the working directory and its parent
//        if (!Settings.runningSetup && !runningSikulixapi && libPath == null && userdir != null) {
//          File wd = new File(userdir);
//          File wdpl = null;
//          File wdp = new File(userdir).getParentFile();
//          File wdl = new File(FileManager.slashify(wd.getAbsolutePath(), true) + libSub);
//          if (wdp != null) {
//            wdpl = new File(FileManager.slashify(wdp.getAbsolutePath(), true) + libSub);
//          }
//          if (wdl.exists()) {
//            libPath = wdl.getAbsolutePath();
//          } else if (wdpl != null && wdpl.exists()) {
//            libPath = wdpl.getAbsolutePath();
//          }
//          log(lvl, "Exists libs folder in working folder or its parent? %s: %s", libPath == null ? "NO" : "YES",
//                  wd.getAbsolutePath());
//          libsDir = checkLibsDir(libPath);
//        }
//
//        if (!Settings.runningSetup && !runningSikulixapi && libPath == null && libPathFallBack != null) {
//          libPath = libPathFallBack;
//          log(lvl, "Checking available fallback for libs folder: " + libPath);
//          libsDir = checkLibsDir(libPath);
//        }
//      }
//    }
//
//    if (libsDir == null && libPath != null) {
//      log(lvl, "libs dir is empty, has wrong content or is outdated");
//      log(lvl, "Trying to extract libs to: " + libPath);
//      if (!FileManager.deleteFileOrFolder(libPath,
//              new FileManager.FileFilter() {
//                @Override
//                public boolean accept(File entry) {
//                  if (entry.getPath().contains("tessdata")) {
//                    return false;
//                  }
//                  return true;
//                }
//              })) {
//        log(-1, "Fatal Error 102: not possible to empty libs dir");
//        Sikulix.terminate(102);
//      }
//      File dir = (new File(libPath));
//      dir.mkdirs();
//      if (extractLibs(dir.getParent(), libSource) == null) {
//        log(-1, "... not possible!");
//        libPath = null;
//      } else {
//        libsDir = checkLibsDir(libPath);
//      }
//    }
//
//    //<editor-fold defaultstate="collapsed" desc="libs dir finally invalid">
//    if (libPath == null && !Settings.isWinApp) {
//      log(-1, "No valid libs path available until now!");
//      File jarPathLibs = null;
//
//      if (libPath == null && jarParentPath != null) {
//        if ((jarPath.endsWith(".jar") && libsURL == null) || apiJarURL != null) {
//          log(-2, "Please wait! Trying to extract libs to jar parent folder: \n" + jarParentPath);
//          jarPathLibs = extractLibs((new File(jarParentPath)).getAbsolutePath(), libSource);
//          if (jarPathLibs == null) {
//            log(-1, "not possible!");
//          } else {
//            libPath = jarPathLibs.getAbsolutePath();
//          }
//        } else if (Settings.runningSetupInValidContext) {
//          log(-2, "Please wait! Trying to extract libs to working folder: \n" + jarParentPath);
//          jarPathLibs = extractLibs(Settings.runningSetupInContext, libSource);
//          if (jarPathLibs == null) {
//            log(-1, "not possible!");
//          } else {
//            libPath = jarPathLibs.getAbsolutePath();
//          }
//        }
//      }
//      if (libPath == null && userSikuli != null) {
//        log(-2, "Please wait! Trying to extract libs to user home: \n" + userSikuli);
//        File userhomeLibs = extractLibs((new File(userSikuli)).getAbsolutePath(), libSource);
//        if (userhomeLibs == null) {
//          log(-1, "not possible!");
//        } else {
//          libPath = userhomeLibs.getAbsolutePath();
//        }
//      }
//      libsDir = checkLibsDir(libPath);
//      if (libPath == null || libsDir == null) {
//        log(-1, "Fatal Error 103: No valid native libraries folder available - giving up!");
//        Sikulix.terminate(103);
//      }
//    }
//    //</editor-fold>
//
//    if (Settings.isWinApp) {
//      String osArchNum = osarch.contains("64") ? "64" : "32";
//      log(lvl, "isWinApp: checking libs folder");
//      String jarLibs = "META-INF/libs/windows/libs" + osArchNum + "/";
//      String fContent = jarLibs + "sikulixfoldercontent";
//      String sContent = FileManager.extractResourceAsLines(fContent);
//      File fpLibsWin = new File(Settings.getInstallBase(), "libs");
//      log(lvl, "isWinApp: creating libs folder at: \n%s", fpLibsWin);
//      boolean success = true;
//      for (String fName : sContent.split("\\n")) {
//        log(lvl + 1, "libs export: %s", fName);
//        success &= FileManager.extractResource(jarLibs + fName, new File(fpLibsWin, fName));
//      }
//      if (!success) {
//        log(-1, "isWinApp: problems creating libs folder");
//        Sikulix.terminate(999);
//      }
//      libPath = fpLibsWin.getAbsolutePath();
//      libsDir = checkLibsDir(libPath);
//      if (libsDir == null) {
//        log(-1, "isWinApp: finally giving up");
//        Sikulix.terminate(999);
//      }
//    }
//
//    if (Settings.isLinux()) {
//      File libsLinux = new File(libsDir.getParent(), "libsLinux/libVisionProxy.so");
//      if (libsLinux.exists()) {
//        log(lvl, "Trying to use provided library at: " + libsLinux.getAbsolutePath());
//        try {
//          FileManager.xcopy(libsLinux.getAbsolutePath(),
//                  new File(libPath, "libVisionProxy.so").getAbsolutePath());
//        } catch (IOException ex) {
//          log(-1, "... did not work: " + ex.getMessage());
//          Sikulix.terminate(999);
//        }
//      }
//    }
//
////    if (itIsJython) {
////      export("Lib/sikuli", libsDir.getParent());
////      itIsJython = false;
////    }
//    if (Settings.OcrDataPath == null && System.getProperty("sikuli.DoNotExport") == null) {
//      if (Settings.isWindows() || Settings.isMac()) {
//        Settings.OcrDataPath = libPath;
//      } else {
//        Settings.OcrDataPath = "/usr/local/share";
//      }
//    }
//
//    initDone = true;
//    return libsDir != null;
//  }

//  private File checkLibsDir(String path) {
//    String memx = mem;
//    mem = "checkLibsDir";
//    File dir = null;
//    if (path != null) {
//      log(lvl, "trying: " + path);
//      if (Settings.isWindows() && !initDone) {
//        log(lvl, "Running on Windows - checking system path!");
//        String syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
//        if (syspath == null) {
//          Sikulix.terminate(999);
//        } else {
//          path = (new File(path).getAbsolutePath()).replaceAll("/", "\\");
//          if (!syspath.toUpperCase().contains(path.toUpperCase())) {
//            if (!SysJNA.WinKernel32.setEnvironmentVariable("PATH", path + ";" + syspath)) {
//              Sikulix.terminate(999);
//            }
//            log(lvl, "Added libs dir to path: " + path);
//            syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
//            if (!syspath.toUpperCase().contains(path.toUpperCase())) {
//              log(-1, "Adding to path did not work:\n%s", syspath);
//              Sikulix.terminate(999);
//            }
//            log(lvl, syspath.substring(0, Math.min(path.length()+50, syspath.length())) + "...");
//          }
//        }
//				if (!checkJavaUsrPath()) {
//					usrPathProblem = true;
//				}
//      }
//      if (System.getProperty("sikuli.DoNotExport") != null) {
//        dir = new File(path);
//      } else {
//        File checkFile = (new File(FileManager.slashify(path, true) + checkFileName));
//        if (checkFile.exists()) {
//          if ((new File(jarPath)).lastModified() > checkFile.lastModified()) {
//            log(lvl + 1, "libs folder outdated!");
//          } else {
//            //convenience: jawt.dll in libsdir avoids need for java/bin in system path
//            if (Settings.isWindows()) {
//              String lib = "jawt.dll";
//              try {
//                extractResource(javahome + "bin/" + lib, new File(libPath, lib), false);
//                log(lvl + 1, "copied to libs: jawt.dll");
//              } catch (IOException ex) {
//                log(-1, "Fatal error 107: problem copying " + lib + "\n" + ex.getMessage());
//                Sikulix.terminate(107);
//              }
//            }
//            loadLib(checkLib);
//            log(lvl, "Using libs at: " + path);
//            dir = new File(path);
//          }
//        } else {
//          if (Settings.isWindows()) {
//            // might be wrong arch
//            if ((new File(FileManager.slashify(path, true) + checkFileNameW32)).exists()
//                    || (new File(FileManager.slashify(path, true) + checkFileNameW64)).exists()) {
//              log(lvl + 1, "libs dir contains wrong arch for " + osarch);
//            }
//          } else {
//            log(-1, "Not a valid libs dir for SikuliX (" + osarch + "): " + path);
//          }
//        }
//      }
//    }
//    mem = memx;
//    return dir;
//  }

//	private boolean checkJavaUsrPath() {
//		if (Settings.isWindows() && libPath != null) {
//			log(lvl, "checking ClassLoader.usrPaths having: %s", libPath);
//			Field usrPathsField = null;
//			try {
//				usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
//			} catch (NoSuchFieldException ex) {
//				log(-1, ex.getMessage());
//			} catch (SecurityException ex) {
//				log(-1, ex.getMessage());
//			}
//			boolean contained = false;
//			if (usrPathsField != null) {
//				usrPathsField.setAccessible(true);
//				try {
//					//get array of paths
//					String[] javapaths = (String[]) usrPathsField.get(null);
//					//check if the path to add is already present
//					for (String p : javapaths) {
//						if (FileManager.pathEquals(p, libPath)) {
//							contained = true;
//							break;
//						}
//					}
//					//add the new path
//					if (!contained) {
//						final String[] newPaths = Arrays.copyOf(javapaths, javapaths.length + 1);
//						newPaths[newPaths.length - 1] = libPath;
//						usrPathsField.set(null, newPaths);
//						log(lvl, "added to ClassLoader.usrPaths");
//					}
//				} catch (IllegalAccessException ex) {
//					log(-1, ex.getMessage());
//				} catch (IllegalArgumentException ex) {
//					log(-1, ex.getMessage());
//				}
//				return true;
////				//check the new path
////				if (!contained) {
////					try {
////						System.loadLibrary(checkLibWindows);
////					} catch (java.lang.UnsatisfiedLinkError ex) {
////						log(-1, "adding to ClassLoader.usrPaths did not work:\n" + ex.getMessage());
////						System.exit(1);
////					}
////				}
//			}
//		}
//		return false;
//	}
//
//  private boolean isFatJar() {
//    if (extractingFromJar) {
//      try {
//        ZipInputStream zip = new ZipInputStream(jarURL.openStream());
//        ZipEntry ze;
//        while ((ze = zip.getNextEntry()) != null) {
//          String entryName = ze.getName();
//          if (entryName.startsWith(libSourcebase)) {
//            return true;
//          }
//        }
//      } catch (IOException e) {
//        return false;
//      }
//    }
//    return false;
//  }
//
//  public File getLibsDir() {
//    return libsDir;
//  }
//</editor-fold>

  public boolean export(URL fromURL, String res, String targetPath) {
    currentURL = fromURL;
    boolean success = export(res, targetPath);
    currentURL = null;
    return success;
  }
  /**
   * @param res what to export
   * @param targetPath target folder
   * @return success
   */
  public boolean export(String res, String targetPath) {
    String memx = mem;
    mem = "export";
    log(lvl, "Trying to access package for exporting: %s to:\n%s", res, targetPath);
    boolean fastReturn = false;
    String pre = null, suf = "", tok = res;
    String[] parts;
    if (res.indexOf("#") != -1) {
      tok = res.replace("#", "/");
      if (tok.startsWith("/")) {
        tok = tok.substring(1);
      }
      parts = res.split("#");
      if (parts.length > 2) {
        log(-1, "export: invalid resource: %s", res);
        return false;
      }
      if (parts.length > 1) {
				if (!parts[0].isEmpty()) {
					pre = parts[0];
					suf = parts[1];
				} else if (!parts[1].isEmpty()) {
					pre = "";
					suf = parts[1];
				}
			} else {
				pre = tok;
			}
			fastReturn = true;
			log(lvl, "export with #: %s (%s)-(%s) as %s", res, pre, suf, tok);
    }
    if (currentURL == null) {
      currentURL = jarURL;
    }
    extractingFromJar = currentURL.getPath().endsWith(".jar");
    List<String[]> entries = makePackageFileList(currentURL, tok, true);
    if (entries == null || entries.isEmpty()) {
      return false;
    }
    String targetName = null, source = null;
    File targetFile;
    long targetDate, entryDate;
    for (String[] e : entries) {
      try {
        entryDate = Long.valueOf(e[1]);
        source = e[0];
        if (pre != null) {
          parts = source.split(tok);
          if (parts.length == 2) {
            targetName = suf + parts[1];
          } else {
            targetName = suf;
          }
          targetFile = new File(targetPath, targetName);
        } else {
          targetFile = new File(targetPath, source);
        }
        if (targetFile.exists()) {
          targetDate = targetFile.lastModified();
        } else {
          targetDate = 0;
          targetFile.getParentFile().mkdirs();
        }
        if (targetDate == 0 || targetDate < entryDate) {
          extractResource(source, targetFile, extractingFromJar);
          log(lvl + 1, "is dated: %s (%d)", entryDate, targetDate);
        } else {
          log(lvl + 1, "already in place: " + targetName);
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

  public void setItIsJython() {
      itIsJython = true;
  }

  public String runcmd(String cmd) {
    return runcmd(new String[]{cmd});
  }

  public String runcmd(String args[]) {
		return RunTime.get().runcmd(args);
//<editor-fold defaultstate="collapsed" desc="obsolete">
//		if (args.length == 0) {
//      return "";
//    }
//    if (args.length == 1) {
//      String separator = "\"";
//      ArrayList<String> argsx = new ArrayList<String>();
//      StringTokenizer toks;
//      String tok;
//      String cmd = args[0];
//      if (Settings.isWindows()) {
//        cmd = cmd.replaceAll("\\\\ ", "%20;");
//      }
//      toks = new StringTokenizer(cmd);
//      while (toks.hasMoreTokens()) {
//        tok = toks.nextToken(" ");
//        if (tok.length() == 0) {
//          continue;
//        }
//        if (separator.equals(tok)) {
//          continue;
//        }
//        if (tok.startsWith(separator)) {
//          if (tok.endsWith(separator)) {
//            tok = tok.substring(1, tok.length() - 1);
//          } else {
//            tok = tok.substring(1);
//            tok += toks.nextToken(separator);
//          }
//        }
//        argsx.add(tok.replaceAll("%20;", " "));
//      }
//      args = argsx.toArray(new String[0]);
//    }
//    if (args[0].startsWith("#")) {
//      String pgm = args[0].substring(1);
//      args[0] = (new File(libsDir, pgm)).getAbsolutePath();
//      runcmd(new String[]{"chmod", "ugo+x", args[0]});
//    }
//    String memx = mem;
//    mem = "runcmd";
//    String result = "";
//    String error = "*** error ***" + NL;
//    try {
//			if (lvl <= Debug.getDebugLevel()) {
//				log(lvl, Sikulix.arrayToString(args));
//			} else {
//				Debug.info("runcmd: " + Sikulix.arrayToString(args));
//			}
//      Process process = Runtime.getRuntime().exec(args);
//      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
//      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//      String s;
//      while ((s = stdInput.readLine()) != null) {
//        if (!s.isEmpty()) {
//          result += s + NL;
//        }
//      }
//      if ((s = stdError.readLine()) != null) {
//        result = error + result;
//        result += s;
//      }
//    } catch (Exception e) {
//      log(-1, "fatal error: " + e.getMessage());
//      result = error + e.getMessage();
//    }
//    mem = memx;
//    return result;
//</editor-fold>
  }

  /**
   * make sure, a native library is available and loaded
   *
   * @param libname System.loadLibrary() compatible library name
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
      log(lvl + 1, "Is already loaded: " + libname);
      mem = memx;
      return;
    }
    log(lvl + 1, libname);
    if (libPath == null) {
      log(-1, "Fatal Error 108: No libs directory available");
      Sikulix.terminate(108);
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
        Sikulix.terminate(109);
      } else {
        lib = mappedlib;
        log(lvl, "Linux: %s \nnot bundled - trying to load from system paths", lib);
//TODO try to find it on LD_LIBRARY_PATH
				Sikulix.terminate(109);
      }
    } else {
      log(lvl + 1, "Found: " + libname + " at " + lib);
    }
    try {
      System.load(lib);
    } catch (Error e) {
      log(-1, "Fatal Error 110: loading: " + mappedlib);
      log(-1, "Since native library was found at %s\n it might be a problem with needed dependent libraries\nERROR: %s",
              libPath, e.getMessage());
      if (Settings.isWindows()) {
        log(-1, "Check, wether a valid Sikuli libs folder is in system path at runtime!");
        if (Settings.runningSetup) {
          log(-1, "Running Setup: ignoring this error for now");
          mem = memx;
          return;
        }
      }
      Sikulix.terminate(110);
    }
    log(lvl, "Now loaded: %s from: \n%s", libname, lib);
    mem = memx;
  }

  private File extractLibs(String targetDir, String libSource) {
    String memx = mem;
    mem = "extractLibs";
    if (apiJarURL != null) {
      libsURL = apiJarURL;
      targetDir = new File(libsURL.getPath()).getParent();
      extractingFromJar = true;
    }
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
          log(lvl + 1, "is from: %s (%d)", e[1], targetDate);
        } else {
          log(lvl + 1, "already in place: " + targetName);
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
        log(lvl, "from:\n%s", FileManager.slashify(jar.getPath(), false));
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
        log(lvl, "from: " + folder.getAbsolutePath());
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
    log0(lvl + 1, "Extracting resource from: " + resourcename);
    log0(lvl + 1, "Extracting to: " + outputfile.getAbsolutePath());
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
   * copy an InputStream to an OutputStream.
   *
   * @param in InputStream to copy from
   * @param out OutputStream to copy to
   * @throws IOException if there's an error
   */
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
  }
}
