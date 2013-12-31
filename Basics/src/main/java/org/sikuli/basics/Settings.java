/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.basics;

import java.io.File;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;

public class Settings {

  public static final int SikuliVersionMajor = 1;
  public static final int SikuliVersionMinor = 1;
  public static final int SikuliVersionSub = 0;
  public static final int SikuliVersionBetaN = 1;

  private static String me = "Settings";
  private static String mem = "...";
  private static int lvl = 2;
  public static int breakPoint = 0;
  public static boolean handlesMacBundles = true;
  public static boolean runningSetup = false;
  private static final PreferencesUser prefs = PreferencesUser.getInstance();
  
  /**
   * location of folder Tessdata
   */
  public static String OcrDataPath;
  /**
   * standard place in the net to get information about extensions<br />
   * needs a file extensions.json with content<br />
   * {"extension-list":<br />
   * &nbsp;{"extensions":<br />
   * &nbsp;&nbsp;[<br />
   * &nbsp;&nbsp;&nbsp;{<br />
   * &nbsp;&nbsp;&nbsp;&nbsp;"name":"SikuliGuide",<br />
   * &nbsp;&nbsp;&nbsp;&nbsp;"version":"0.3",<br />
   * &nbsp;&nbsp;&nbsp;&nbsp;"description":"visual annotations",<br />
   * &nbsp;&nbsp;&nbsp;&nbsp;"imgurl":"somewhere in the net",<br />
   * &nbsp;&nbsp;&nbsp;&nbsp;"infourl":"http://doc.sikuli.org",<br />
   * &nbsp;&nbsp;&nbsp;&nbsp;"jarurl":"---extensions---"<br />
   * &nbsp;&nbsp;&nbsp;},<br />
   * &nbsp;&nbsp;]<br />
   * &nbsp;}<br />
   * }<br />
   * imgurl: to get an icon from<br />
   * infourl: where to get more information<br />
   * jarurl: where to download the jar from (no url: this standard place)<br />
   */
  public static String SikuliRepo;
  private static String[] args = new String[0];
  private static String[] sargs = new String[0];
  public static String[] ServerList = {"http://dl.dropboxusercontent.com/u/42895525/SikuliX"};
  private static final String sversion = String.format("%d.%d.%d",
          SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub);
  private static final String bversion = String.format("%d.%d-Beta%d",
          SikuliVersionMajor, SikuliVersionMinor, SikuliVersionBetaN);
  public static final String SikuliVersionDefault = "Sikuli " + sversion;
  public static final String SikuliVersionBeta = "Sikuli " + bversion;
  public static final String SikuliVersionDefaultIDE = "Sikuli IDE " + sversion;
  public static final String SikuliVersionBetaIDE = "Sikuli IDE " + bversion;
  public static final String SikuliVersionDefaultScript = "Sikuli Script " + sversion;
  public static final String SikuliVersionBetaScript = "Sikuli Script " + bversion;
  public static String SikuliVersion;
  public static String SikuliVersionIDE;
  public static String SikuliVersionScript;
  public static final String versionMonth = "November 2013";
  
  
  /**
   * Resource types to be used with IResourceLoader implementations
   */
  public static final String SIKULI_LIB = "*sikuli_lib";
  public static String BaseTempPath;
  public static String UserName = "UnKnown";
  
  public static String proxyName = prefs.get("ProxyName", null);
  public static String proxyIP = prefs.get("ProxyIP", null);
  public static InetAddress proxyAddress = null;
  public static String proxyPort = prefs.get("ProxyPort", null);
  public static boolean proxyChecked = false;
  public static Proxy proxy = null;
  
  private static Preferences options = Preferences.userNodeForPackage(SikuliX.class);

  static {
    mem = "clinit";

    Properties props = System.getProperties(); //for debugging

    if (System.getProperty("user.name") != null && !"".equals(System.getProperty("user.name"))) {
      UserName = System.getProperty("user.name");
    }

    BaseTempPath = System.getProperty("java.io.tmpdir") + File.separator + UserName;

    // TODO check existence of an extension repository
    SikuliRepo = null;

    // set the version strings
    if (SikuliVersionSub == 0 && SikuliVersionBetaN > 0) {
      SikuliVersion = SikuliVersionBeta;
      SikuliVersionIDE = SikuliVersionBetaIDE;
      SikuliVersionScript = SikuliVersionBetaScript;
    } else {
      SikuliVersion = SikuliVersionDefault;
      SikuliVersionIDE = SikuliVersionDefaultIDE;
      SikuliVersionScript = SikuliVersionDefaultScript;
    }
  }
  
  public static final int ISWINDOWS = 0;
  public static final int ISMAC = 1;
  public static final int ISLINUX = 2;
  public static final int ISNOTSUPPORTED = 3;
  public static boolean isMacApp = false;
  public static final String appPathMac = "/Applications/SikuliX-IDE.app/Contents";

  public static String osName;
  public static final float FOREVER = Float.POSITIVE_INFINITY;
  public static final int JavaVersion = Integer.parseInt(java.lang.System.getProperty("java.version").substring(2, 3));
  public static final String JREVersion = java.lang.System.getProperty("java.runtime.version");
  public static boolean ThrowException = true; // throw FindFailed exception
  public static float AutoWaitTimeout = 3f; // in seconds
  public static float WaitScanRate = 3f; // frames per second
  public static float ObserveScanRate = 3f; // frames per second
  public static int ObserveMinChangedPixels = 50; // in pixels
  public static int WaitForVanish = 1; // wait 1 second for visual to vanish after action
  public static double MinSimilarity = 0.7;
  public static boolean CheckLastSeen = true;
  public static float CheckLastSeenSimilar = 0.95f;
  public static boolean UseImageFinder = false;

  public static double DelayBeforeDrop = 0.3;
  public static double DelayAfterDrag = 0.3;
  public static double TypeDelay = 0.0;
  public static double ClickDelay = 0.0;

  public static String BundlePath = null;
  public static boolean OcrTextSearch = false;
  public static boolean OcrTextRead = false;

  /**
   * true = start slow motion mode, false: stop it (default: false) show a visual for
   * SlowMotionDelay seconds (default: 2)
   */
  private static boolean ShowActions = false;

  public static boolean isShowActions() {
    return ShowActions;
  }

  public static void setShowActions(boolean ShowActions) {
    if (ShowActions) {
      MoveMouseDelaySaved = MoveMouseDelay;
    }
    else {
      MoveMouseDelay = MoveMouseDelaySaved;
    }
    Settings.ShowActions = ShowActions;
  }

  public static float SlowMotionDelay = 2.0f; // in seconds
  public static float MoveMouseDelay = 0.5f; // in seconds
  private static float MoveMouseDelaySaved = MoveMouseDelay;
  
  /**
   * true = highlight every match (default: false) (show red rectangle around) for
   * DefaultHighlightTime seconds (default: 2)
   */
  public static boolean Highlight = false;
  public static float DefaultHighlightTime = 2f;
  public static float WaitAfterHighlight = 0.3f;
  public static boolean ActionLogs = true;
  public static boolean InfoLogs = true;
  public static boolean DebugLogs;
  public static boolean ProfileLogs = false;
  public static boolean LogTime = false;
  public static boolean UserLogs = true;
  public static String UserLogPrefix = "user";
  public static boolean UserLogTime = true;
  /**
   * default pixels to add around with nearby() and grow()
   */
  public static final int DefaultPadding = 50;

  public static boolean isJava7() {
    return JavaVersion > 6;
  }

  public static void showJavaInfo() {
    Debug.log(1, "Running on Java " + JavaVersion + " (" + JREVersion + ")");
  }

  public static String getFilePathSeperator() {
    return File.separator;
  }

  public static String getPathSeparator() {
    if (isWindows()) {
      return ";";
    }
    return ":";
  }

  public static String getSikuliDataPath() {
    String home, sikuliPath;
    if (isWindows()) {
      home = System.getenv("APPDATA");
      sikuliPath = "Sikuli";
    } else if (isMac()) {
      home = System.getProperty("user.home")
              + "/Library/Application Support";
      sikuliPath = "Sikuli";
    } else {
      home = System.getProperty("user.home");
      sikuliPath = ".sikuli";
    }
    File fHome = new File(home, sikuliPath);
    return fHome.getAbsolutePath();
  }

  /**
   * returns the absolute path to the user's extension path
   */
  public static String getUserExtPath() {
    String ret = getSikuliDataPath() + File.separator + "extensions";
    File f = new File(ret);
    if (!f.exists()) {
      f.mkdirs();
    }
    return ret;
  }

  public static int getOS() {
    int osRet = ISNOTSUPPORTED;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("mac")) {
      osRet = ISMAC;
      osName = "Mac OSX";
    } else if (os.startsWith("windows")) {
      osRet = ISWINDOWS;
      osName = "Windows";
    } else if (os.startsWith("linux")) {
      osRet = ISLINUX;
      osName = "Linux";
    }
    return osRet;
  }

  public static boolean isWindows() {
    return getOS() == ISWINDOWS;
  }

  public static boolean isLinux() {
    return getOS() == ISLINUX;
  }
  
  public static boolean isMac() {
    return getOS() == ISMAC;
  }

  public static String getOSVersion() {
    return System.getProperty("os.version");
  }

  public static String getVersion() {
    return SikuliVersion;
  }

  public static String getVersionShort() {
    if (SikuliVersionBetaN > 0) {
      return bversion;
    }
    else {
      return sversion;
    }
  }

  public static String getVersionShortBasic() {
      return sversion.substring(0, 3);
  }

  public static void setArgs(String[] args, String[] sargs) {
    Settings.args = args;
    Settings.sargs = sargs;
  }

  public static String[] getArgs() {
    return Settings.args;
  }

  public static String[] getSikuliArgs() {
    return Settings.sargs;
  }

  public static String getTimestamp() {
    return (new Date()).getTime() + "";
  }

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, level < 0 ? "error" : "debug",
            me + ": " + message, args);
  }

  public static void printArgs() {
    if (Debug.getDebugLevel() < 3) {
      return;
    }
    String[] args = Settings.getSikuliArgs();
    if (args.length > 0) {
      Debug.log(3, "--- Sikuli parameters ---");
      for (int i = 0; i < args.length; i++) {
        Debug.log(3, "%d: %s", i + 1, args[i]);
      }
    }
    args = Settings.getArgs();
    if (args.length > 0) {
      Debug.log(3, "--- User parameters ---");
      for (int i = 0; i < args.length; i++) {
        Debug.log(3, "%d: %s", i + 1, args[i]);
      }
    }
  }
}
