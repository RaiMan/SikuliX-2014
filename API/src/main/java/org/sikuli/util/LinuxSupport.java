/*
 * Copyright 2010-2015, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2015
 */
package org.sikuli.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import javax.swing.JFrame;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.FileManager.FileFilter;
import org.sikuli.basics.ResourceLoader;
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
  private static final String buildFolderSrc = "Build/Source";
  private static final String buildFolderInclude = "Build/Include";
  private static final String[] buildSrcFiles
          = new String[]{"cvgui.cpp", "finder.cpp",
            "pyramid-template-matcher.cpp", "sikuli-debug.cpp", "tessocr.cpp",
            "vision.cpp", "visionJAVA_wrap.cxx"};
  private static final String buildCppFix = "g++ -c -O3 -fPIC -MMD -MP %s "; // $includeParm
  private static final String buildFolder = "Build";
  private static final String buildFolderStuff = "Build/Stuff";
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

  public static void setWorkDir(String workDir) {
    fWorkDir = new File(workDir);
  }
  
  public static void setLibsDir(File libsDir) {
    fLibs = libsDir;
  }
  
  public static boolean existsLibs() {
    return new File(fLibs, libVision).exists() || new File(fLibs, libGrabKey).exists();
  }
  
  static class LibsFilter implements FilenameFilter {    
    String libName = "";
    String osArch = "";
    public LibsFilter(String name, String arch) {
      libName = name;
      osArch = arch;
    }
    @Override
    public boolean accept(File dir, String name) {
      if (dir.getName().contains("libs" +  osArch) && name.contains(libName)) {
        return true;
      }
      return false;
    }
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
          runTime.extractResourcesToFolderFromJar(libsJar, "/sikulixlibs/linux", fLibs, new LibsFilter(exLib, osArch));
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
      cmdRet = ResourceLoader.get().runcmd("ldconfig -p");
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
      cmdRet = ResourceLoader.get().runcmd("wmctrl -m");
      if (cmdRet.contains(runTime.runCmdError)) {
        logPlus(-1, "checking: wmctrl not available or not working");
      } else {
        logPlus(lvl, "checking: wmctrl seems to be available");
      }
      cmdRet = ResourceLoader.get().runcmd("xdotool version");
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
    cmdRet = ResourceLoader.get().runcmd("readelf -d " + lib);
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
    
    return checkSuccess;
  }
  
  public static boolean runLdd(File lib) {
    // ldd -r lib
    // undefined symbol: _ZN2cv3MatC1ERKS0_RKNS_5Rect_IiEE	(./libVisionProxy.so)
    String cmdRet = ResourceLoader.get().runcmd("ldd -r " + lib);
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
    File build = new File(fWorkDir, buildFolder);
    File source = new File(fWorkDir, buildFolderSrc);
    File stuff = new File(fWorkDir, buildFolderStuff);
    File incl = new File(fWorkDir, buildFolderInclude);
    
    File javaHome = new File(System.getProperty("java.home"));
    logPlus(lvl, "home of java: %s", javaHome);
    File javaInclude = null;
    File javaIncludeLinux = null;
  
    logPlus(lvl, "starting inline build: libVisionProxy.so");
    
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
      logPlus(lvl, "buildVision: JDK: not found - using the bundled include files");
      buildInclude += " -I" + incl.getAbsolutePath();
      exportIncludeJava = true;
    } else {
      log(lvl, "JDK: found at: %s", javaHome);
      buildInclude += String.format("-I%s -I%s ",
              javaInclude.getAbsolutePath(), javaIncludeLinux.getAbsolutePath());
    }
    
    boolean exportIncludeOpenCV = false;
    boolean exportIncludeTesseract = false;
    
    String inclLib = "opencv2";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      logPlus(lvl, "buildVision: opencv-include: not found - using the bundled include files");
      exportIncludeOpenCV = true;
      if (!exportIncludeJava) {
        buildInclude += " -I" + incl.getAbsolutePath();
      }
    }
    
    inclLib = "tesseract";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      logPlus(lvl, "buildVision: tesseract-include: not found - using the bundled include files");
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
    log(lvl, "buildVision: setting up the compile commands");
    for (String sFile : buildSrcFiles) {
      buildCompile += String.format("echo ----- %s\n", sFile);
      buildCompile += String.format(buildCppFix, buildInclude);
      mfFile = new File(stuff, sFile).getAbsolutePath();
      srcFile = new File(source, sFile).getAbsolutePath();
      buildCompile += String.format(buildCppMods, mfFile, mfFile, srcFile);
      buildCompile += "\n";
      buildLink += mfFile + ".o ";
    }
    log(lvl, "buildVision: setting up the link command");
    buildLink += libOpenCVcore + " ";
    buildLink += libOpenCVhighgui + " ";
    buildLink += libOpenCVimgproc + " ";
    buildLink += libTesseract + " ";
    String libVisionPath = new File(build, libVision).getAbsolutePath();
    buildLink += "-o " + libVisionPath;
    
    File cmdFile = new File(build, "runBuild");
//    ResourceLoader rl = ResourceLoader.forJar(srcjar);
//    if (rl != null) {
      FileManager.deleteFileOrFolder(build.getAbsolutePath());
      build.mkdirs();
      source.mkdirs();
      stuff.mkdirs();
      
      PrintStream out = null;
      log(lvl, "-------------- content of created build script");
      try {
        out = new PrintStream(new FileOutputStream(cmdFile));
        out.println("echo ----------- COMPILING");
        out.print(buildCompile);
        log(lvl, "----------- COMPILING\n%s", buildCompile);
        out.println("echo ----------- LINKING");
        out.println(buildLink);
        log(lvl, "----------- LINKING\n%s\n----------- SCRIPT END", buildLink);
        out.close();
        logPlus(lvl, "buildVision: build script written to: %s", cmdFile);
      } catch (Exception ex) {
        logPlus(-1, "buildVision: cannot write %s", cmdFile);
        return false;
      }
      boolean success = true;
      success &= (null != runTime.extractResourcesToFolderFromJar(srcjar, 
              "/srcnativelibs/Vision", source, null));
//      rl.export("srcnativelibs/Vision#", source.getAbsolutePath());
      if (exportIncludeJava) {
        success &= (null != runTime.extractResourcesToFolderFromJar(srcjar, 
                "/srcnativelibs/VisionInclude/Java", incl, null));
//        rl.export("srcnativelibs/VisionInclude/Java#", incl.getAbsolutePath());
      }
      if (exportIncludeOpenCV) {
        success &= (null != runTime.extractResourcesToFolderFromJar(srcjar, 
                "/srcnativelibs/VisionInclude/OpenCV", incl, null));
//        rl.export("srcnativelibs/VisionInclude/OpenCV#", incl.getAbsolutePath());
      }
      if (exportIncludeTesseract) {
        success &= (null != runTime.extractResourcesToFolderFromJar(srcjar, 
                "/srcnativelibs/VisionInclude/Tesseract", incl, null));
//        rl.export("srcnativelibs/VisionInclude/Tesseract#", incl.getAbsolutePath());
      }
      cmdFile.setExecutable(true);
    if (!success) {
      logPlus(-1, "buildVision: cannot export lib sources");
      return false;
    }
    
    JFrame spl = null;
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
