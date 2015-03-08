/*
 * Copyright 2010-2015, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2015
 */
package org.sikuli.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.RunTime;

/**
 * INTERNAL USE: all things needed with Linux at setup or runtime
 */
public class LinuxSupport {

  static final RunTime runTime = RunTime.get();

	//<editor-fold defaultstate="collapsed" desc="new logging concept">
	private static final String me = "LinuxSupport: ";
	private static int lvl = 3;
//  private static String osArch;
	public static void log(int level, String message, Object... args) {
		Debug.logx(level,	me + message, args);
	}

  private static void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private static void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }

  private static void logPlus(int level, String message, Object... args) {
    String sout = Debug.logx(level, me + ": " + message, args);
    if (logToFile) {
      System.out.println(sout);
    }
  }

  private static boolean logToFile = false;
  public static void setLogToFile(boolean state) {
    logToFile = state;
  }
	//</editor-fold>

  static File fWorkDir = null;
  private static final String buildFolder = "Build";
  private static final String buildFolderSrc = "Build/Source";
  private static final String buildFolderInclude = "Build/Include";
  private static final String buildFolderTarget = "Build/Target";
  static File fLibs = null;
  public static final String libVision = "libVisionProxy.so";
  public static final String libGrabKey = "libJXGrabKey.so";
  static boolean libSearched = false;

  private static String libOpenCVcore = "";
  private static String libOpenCVimgproc = "";
  private static String libOpenCVhighgui = "";
  private static String libTesseract = "";
  private static boolean opencvAvail = true;
  private static boolean tessAvail = true;
  private static final String buildCppMods = "-MF %s.o.d -o %s.o %s";
  private static final String[] buildSrcFiles
          = new String[]{"cvgui.cpp", "finder.cpp",
            "pyramid-template-matcher.cpp", "sikuli-debug.cpp", "tessocr.cpp",
            "vision.cpp", "visionJAVA_wrap.cxx"};
  private static final String buildCppFix = "g++ -c -O3 -fPIC -MMD -MP %s "; // $includeParm
  private static String buildCompile = "";
  private static String buildLink = "g++ -shared -s -fPIC -dynamic ";
  private static final String[] libsExport = new String[]{null, null};
  private static final String[] libsCheck = new String[]{null, null};

  public static String getLinuxDistro() {
    if (!runTime.runningLinux) {
      return "";
    }
    String result = runTime.runcmd("lsb_release -i -r -s");
    String linuxDistro = result.replaceAll("\n", " ").trim();
    if (linuxDistro.contains("*** error ***")) {
      log(lvl, "command returns error: lsb_release -i -r -s\n%s", result);
      linuxDistro = "???DISTRO???";
    }
    return linuxDistro;
  }

  public static void setWorkDir(File workDir) {
    fWorkDir = workDir;
  }

  public static void setLibsDir(File libsDir) {
    fLibs = libsDir;
  }

  public static boolean existsLibs() {
    return new File(fLibs, libVision).exists() || new File(fLibs, libGrabKey).exists();
  }

  public static boolean processLibs1(String libsJar, final String osArch) {
    boolean shouldExport = false;
    boolean shouldBuildVisionNow = false;

    if (!fLibs.exists()) {
      fLibs.mkdirs();
    }
    if (fLibs.exists()) {
      if (!new File(fLibs, libVision).exists()) {
        libsExport[0] = libVision;
        shouldExport = true;
      }
      if (!new File(fLibs, libGrabKey).exists()) {
        libsExport[1] = libGrabKey;
        shouldExport = true;
      }
      if (shouldExport) {
        for (String exLib : libsExport) {
          if (exLib == null) {
            continue;
          }
          runTime.extractResourcesToFolderFromJar(libsJar, "/sikulixlibs/linux/libs" + osArch,
                  fLibs, null);
//          FileManager.unpackJar(libsJar, fLibs.getAbsolutePath(), false, true, new LibsFilter(exLib, osArch));
        }
      }
      libsCheck[0] = new File(fLibs, libVision).getAbsolutePath();
      libsCheck[1] = new File(fLibs, libGrabKey).getAbsolutePath();
      File fLibCheck;
      for (int i = 0; i < libsCheck.length; i++) {
        fLibCheck = new File(libsCheck[i]);
        if (fLibCheck.exists()) {
          if (!checklibs(fLibCheck)) {
//TODO why? JXGrabKey unresolved: pthread
            if (i == 0) {
              if (libsExport[i] == null) {
                logPlus(-1, "provided %s might not be useable on this Linux - see log", fLibCheck.getName());
              } else {
                logPlus(-1, "bundled %s might not be useable on this Linux - see log", fLibCheck.getName());
              }
              shouldBuildVisionNow = true;
            }
          }
        } else {
          log(-1, "check not possible for\n%s", fLibCheck);
        }
      }
    } else {
      log(-1, "check useability of libs: problems with libs folder\n%s", fLibs);
    }
    return shouldBuildVisionNow;
  }

  public static boolean processLibs2() {
    boolean libsProvided = false;
    for (String exLib : libsExport) {
      if (exLib == null) {
        libsProvided = true;
        continue;
      }
      FileManager.deleteFileOrFolder(new File(fLibs, exLib).getAbsolutePath());
    }
    return libsProvided;
  }

  public static boolean checklibs(File lib) {
    String cmdRet;
    String[] retLines;
    boolean checkSuccess = true;

    if (!libSearched) {
      logPlus(lvl, "checking: availability of OpenCV and Tesseract");
      logPlus(lvl, "checking: scanning loader cache (ldconfig -p)");
      cmdRet = runTime.runcmd("ldconfig -p");
      if (cmdRet.contains(runTime.runCmdError)) {
        logPlus(-1, "checking: ldconfig returns error:\ns", cmdRet);
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
          logPlus(-1, "checking: OpenCV not in loader cache (see doc-note on OpenCV)");
          opencvAvail = checkSuccess = false;
        } else {
          logPlus(lvl, "checking: found OpenCV libs:\n%s\n%s\n%s",
                  libOpenCVcore, libOpenCVhighgui, libOpenCVimgproc);
        }
        if (libTesseract == null) {
          logPlus(-1, "checking: Tesseract not in loader cache (see doc-note on Tesseract)");
          tessAvail = checkSuccess = false;
        } else {
          logPlus(lvl, "checking: found Tesseract lib:\n%s", libTesseract);
        }
      }

      // checking wmctrl, xdotool
      cmdRet = runTime.runcmd("wmctrl -m");
      if (cmdRet.contains(runTime.runCmdError)) {
        logPlus(-1, "checking: wmctrl not available or not working");
      } else {
        logPlus(lvl, "checking: wmctrl seems to be available");
      }
      cmdRet = runTime.runcmd("xdotool version");
      if (cmdRet.contains(runTime.runCmdError)) {
        logPlus(-1, "checking: xdotool not available or not working");
      } else {
        logPlus(lvl, "checking: xdotool seems to be available");
      }

      libSearched = true;
    }

    logPlus(lvl, "checking\n%s", lib);
    // readelf -d lib
    // 0x0000000000000001 (NEEDED)             Shared library: [libtesseract.so.3]
    cmdRet = runTime.runcmd("readelf -d " + lib);
    if (cmdRet.contains(runTime.runCmdError)) {
      logPlus(-1, "checking: readelf returns error:\ns", cmdRet);
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
      log(lvl, libsNeeded);
    }

    if (!runLdd(lib)) {
      checkSuccess = false;
    }

//    return false; // for testing
    return checkSuccess;
  }

  public static boolean runLdd(File lib) {
    // ldd -r lib
    // undefined symbol: _ZN2cv3MatC1ERKS0_RKNS_5Rect_IiEE	(./libVisionProxy.so)
    String cmdRet = runTime.runcmd("ldd -r " + lib);
    String[] retLines;
    boolean success = true;
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
      logPlus(lvl, "checking: should work: %s", libName);
    } else {
      logPlus(-1, "checking: might not work, has undefined symbols: %s", libName);
      log(lvl, "%s", libsMissing);
      success = false;
    }
    return success;
  }

  public static boolean buildVision(String srcjar) {
    File fTarget = new File(fWorkDir, buildFolderTarget);
    File fSource = new File(fWorkDir, buildFolderSrc);
    File fInclude = new File(fWorkDir, buildFolderInclude);

    File javaHome = new File(System.getProperty("java.home"));
    logPlus(lvl, "starting inline build: libVisionProxy.so");
    logPlus(lvl, "java.home from environment: %s", javaHome);
  
    if (!new File(javaHome, "bin/javac").exists()) {
      javaHome = javaHome.getParentFile();
    }
    if (!new File(javaHome, "bin/javac").exists()) {
      javaHome = null;
    }
    if (javaHome != null) {
      if (!new File(javaHome, "include/jni.h").exists()) {
        javaHome = null;
      }
    }

    String inclUsr = "/usr/include";
    String inclUsrLocal = "/usr/local/include";

    boolean exportIncludeJava = false;
    if (javaHome == null) {
      logPlus(lvl, "buildVision: JDK: not found - using the bundled include files");
      exportIncludeJava = true;
    } else {
      log(lvl, "JDK: found at: %s", javaHome);
    }

    boolean exportIncludeOpenCV = false;
    boolean exportIncludeTesseract = false;

    String inclLib = "opencv2";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      logPlus(lvl, "buildVision: opencv-include: not found - using the bundled include files");
      exportIncludeOpenCV = true;
    }

    inclLib = "tesseract";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      logPlus(lvl, "buildVision: tesseract-include: not found - using the bundled include files");
      exportIncludeTesseract = true;
    }

    File cmdFile = new File(fWorkDir, "runBuild");
    String libVisionPath = new File(fTarget, libVision).getAbsolutePath();
    String sRunBuild = runTime.extractResourceToString("/Support/Linux", "runBuild", "");

    log(lvl, "-------------- content of created build script");

    boolean success = true;
    success &= (null != runTime.extractResourcesToFolderFromJar(srcjar,
            "/srcnativelibs/Vision", fSource, null));
    if (exportIncludeJava) {
      success &= (null != runTime.extractResourcesToFolderFromJar(srcjar,
              "/srcnativelibs/VisionInclude/Java", fInclude, null));
    }
    if (exportIncludeOpenCV) {
      success &= (null != runTime.extractResourcesToFolderFromJar(srcjar,
              "/srcnativelibs/VisionInclude/OpenCV", fInclude, null));
    }
    if (exportIncludeTesseract) {
      success &= (null != runTime.extractResourcesToFolderFromJar(srcjar,
              "/srcnativelibs/VisionInclude/Tesseract", fInclude, null));
    }
    cmdFile.setExecutable(true);
    if (!success) {
      logPlus(-1, "buildVision: cannot export lib sources");
      return false;
    }

    if (opencvAvail && tessAvail) {
      logPlus(lvl, "buildVision: running build script");
      String cmdRet = runTime.runcmd(cmdFile.getAbsolutePath());
      if (cmdRet.contains(runTime.runCmdError)) {
        logPlus(-1, "buildVision: build script returns error:\n%s", cmdRet);
        return false;
      } else {
        logPlus(lvl, "buildVision: checking created libVisionProxy.so");
        if (!runLdd(new File(libVisionPath))) {
          logPlus(-1, "------- output of the build run\n%s", cmdRet);
          return false;
        }
      }
    }
    try {
      fLibs.mkdirs();
      FileManager.xcopy(libVisionPath, new File(fLibs, libVision).getAbsolutePath());
    } catch (IOException ex) {
      logPlus(-1, "could not copy built libVisionProxy.so to libs folder\n%s", ex.getMessage());
      return false;
    }
    logPlus(lvl, "ending inline build: success: libVisionProxy.so");
    return true;
  }

}
