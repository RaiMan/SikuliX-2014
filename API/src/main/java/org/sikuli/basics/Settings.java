/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.basics;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

/**
 * This is the container for all
 */
public class Settings {

	public static int SikuliVersionMajor;
	public static int SikuliVersionMinor;
	public static int SikuliVersionSub;
	public static int SikuliVersionBetaN;
	public static String SikuliProjectVersionUsed = "";
	public static String SikuliProjectVersion = "";
	public static String SikuliVersionBuild;
	public static String SikuliVersionType;
	public static String SikuliVersionTypeText;
	public static String downloadBaseDirBase;
	public static String downloadBaseDirWeb;
	public static String downloadBaseDir;
	// used for download of production versions
	private static final String dlProdLink = "https://launchpad.net/raiman/sikulix2013+/";
	private static final String dlProdLink1 = ".0";
	private static final String dlProdLink2 = "/+download/";
	// used for download of development versions (nightly builds)
	private static final String dlDevLink = "http://nightly.sikuli.de/";
	private static final String dlMavenLink = "http://search.maven.org/remotecontent?filepath=";

	private static String me = "Settings";
	private static int lvl = 3;

	private static void log(int level, String message, Object... args) {
		Debug.logx(level, level < 0 ? "error" : "debug",
						me + ": " + message, args);
	}

	public static int breakPoint = 0;
	public static boolean handlesMacBundles = true;
	public static boolean runningSetup = false;
	private static final PreferencesUser prefs = PreferencesUser.getInstance();

	/**
	 * location of folder Tessdata
	 */
	public static String OcrDataPath;
	/**
	 * standard place in the net to get information about extensions<br>
	 * needs a file extensions.json with content<br>
	 * {"extension-list":<br>
	 * &nbsp;{"extensions":<br>
	 * &nbsp;&nbsp;[<br>
	 * &nbsp;&nbsp;&nbsp;{<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"name":"SikuliGuide",<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"version":"0.3",<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"description":"visual annotations",<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"imgurl":"somewhere in the net",<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"infourl":"http://doc.sikuli.org",<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"jarurl":"---extensions---"<br>
	 * &nbsp;&nbsp;&nbsp;},<br>
	 * &nbsp;&nbsp;]<br>
	 * &nbsp;}<br>
	 * }<br>
	 * imgurl: to get an icon from<br>
	 * infourl: where to get more information<br>
	 * jarurl: where to download the jar from (no url: this standard place)<br>
	 */
	public static String SikuliRepo;
	public static String SikuliLocalRepo = "";
	private static String[] args = new String[0];
	private static String[] sargs = new String[0];
	public static String[] ServerList = {"http://dl.dropboxusercontent.com/u/42895525/SikuliX"};
	private static String sversion;
	private static String bversion;
	public static String SikuliVersionDefault;
	public static String SikuliVersionBeta;
	public static String SikuliVersionDefaultIDE;
	public static String SikuliVersionBetaIDE;
	public static String SikuliVersionDefaultScript;
	public static String SikuliVersionBetaScript;
	public static String SikuliVersion;
	public static String SikuliVersionIDE;
	public static String SikuliVersionScript;
	public static String SikuliJythonVersion;
	public static String SikuliJython;
	public static String SikuliJRubyVersion;
	public static String SikuliJRuby;
	//TODO needed ???
	public static final String libOpenCV = "libopencv_java248";

	public static String osName;
	public static final float FOREVER = Float.POSITIVE_INFINITY;
	public static final int JavaVersion = Integer.parseInt(java.lang.System.getProperty("java.version").substring(2, 3));
	public static final String JREVersion = java.lang.System.getProperty("java.runtime.version");
  public static final String JavaArch = 	System.getProperty("os.arch");

  public static String SikuliVersionLong;
  public static String SikuliSystemVersion;
  public static String SikuliJavaVersion;

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

	private static Preferences options = Preferences.userNodeForPackage(Sikulix.class);

	public static Map<String, IDESupport> ideSupporter = new HashMap<String, IDESupport>();
	public static Map<String, IScriptRunner> scriptRunner = new HashMap<String, IScriptRunner>();
  public static boolean systemRedirected = false;
	private static List<String> supportedRunner = new ArrayList<String>();
	public static Map<String, String> EndingTypes = new HashMap<String, String>();
	public static Map<String, String> TypeEndings = new HashMap<String, String>();
	public static String CPYTHON = "text/python";
	public static String CRUBY = "text/ruby";
	public static String CPLAIN = "text/plain";
	public static String EPYTHON = "py";
	public static String ERUBY = "rb";
	public static String EPLAIN = "txt";
	public static String RPYTHON = "jython";
	public static String RRUBY = "jruby";
	public static String RDEFAULT = "NotDefined";
	public static String EDEFAULT = EPYTHON;
	public static String TypeCommentToken = "---SikuliX---";
	public static String TypeCommentDefault = "# This script uses %s " + TypeCommentToken + "\n";

	static {
		if (System.getProperty("user.name") != null && !"".equals(System.getProperty("user.name"))) {
			UserName = System.getProperty("user.name");
		}

		BaseTempPath = new File(System.getProperty("java.io.tmpdir"), UserName).getAbsolutePath();

		// TODO check existence of an extension repository
		SikuliRepo = null;

		// set the version strings
		Properties prop = new Properties();
		String svf = "sikulixversion.txt";
		try {
			InputStream is;
			is = Settings.class.getClassLoader().getResourceAsStream("Settings/" + svf);
			prop.load(is);
			String svt = prop.getProperty("sikulixdev");
			SikuliVersionMajor = Integer.decode(prop.getProperty("sikulixvmaj"));
			SikuliVersionMinor = Integer.decode(prop.getProperty("sikulixvmin"));
			SikuliVersionSub = Integer.decode(prop.getProperty("sikulixvsub"));
			SikuliVersionBetaN = Integer.decode(prop.getProperty("sikulixbeta"));
			String ssxbeta = "";
			if (SikuliVersionBetaN > 0) {
				ssxbeta = String.format("-Beta%d", SikuliVersionBetaN);
			}
			SikuliVersionBuild = prop.getProperty("sikulixbuild");
			log(lvl + 1, "%s version from %s: %d.%d.%d%s build: %s", svf,
							SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub, ssxbeta,
							SikuliVersionBuild, svt);
			sversion = String.format("%d.%d.%d",
							SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub);
			bversion = String.format("%d.%d.%d-Beta%d",
							SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub, SikuliVersionBetaN);
			SikuliVersionDefault = "Sikuli " + sversion;
			SikuliVersionBeta = "Sikuli " + bversion;
			SikuliVersionDefaultIDE = "Sikuli IDE " + sversion;
			SikuliVersionBetaIDE = "Sikuli IDE " + bversion;
			SikuliVersionDefaultScript = "Sikuli Script " + sversion;
			SikuliVersionBetaScript = "Sikuli Script " + bversion;

			if ("release".equals(svt)) {
				downloadBaseDirBase = dlProdLink;
				downloadBaseDirWeb = downloadBaseDirBase + getVersionShortBasic() + dlProdLink1;
				downloadBaseDir = downloadBaseDirWeb + dlProdLink2;
        SikuliVersionType = "";
        SikuliVersionTypeText = "";
			} else {
				downloadBaseDirBase = dlDevLink;
				downloadBaseDirWeb = dlDevLink;
				downloadBaseDir = dlDevLink;
        SikuliVersionTypeText = "nightly";
        SikuliVersionBuild += SikuliVersionTypeText;
        SikuliVersionType = svt;
			}
			if (SikuliVersionBetaN > 0) {
				SikuliVersion = SikuliVersionBeta;
				SikuliVersionIDE = SikuliVersionBetaIDE;
				SikuliVersionScript = SikuliVersionBetaScript;
        SikuliVersionLong = bversion + "(" + SikuliVersionBuild + ")";
			} else {
				SikuliVersion = SikuliVersionDefault;
				SikuliVersionIDE = SikuliVersionDefaultIDE;
				SikuliVersionScript = SikuliVersionDefaultScript;
        SikuliVersionLong = sversion + "(" + SikuliVersionBuild + ")";
			}
			SikuliProjectVersionUsed = prop.getProperty("sikulixvused");
			SikuliProjectVersion = prop.getProperty("sikulixvproject");
      String osn = "UnKnown";
      String os = System.getProperty("os.name").toLowerCase();
      if (os.startsWith("mac")) {
        osn = "Mac";
      } else if (os.startsWith("windows")) {
        osn = "Windows";
      } else if (os.startsWith("linux")) {
        osn = "Linux";
      }

			SikuliLocalRepo = prop.getProperty("sikulixlocalrepo");
			SikuliJythonVersion = prop.getProperty("sikulixvjython");
			SikuliJython=SikuliLocalRepo + "/org/Python/jython-standalone/" +
							 SikuliJythonVersion + "/jython-standalone-" + SikuliJythonVersion + ".jar";
			SikuliJRubyVersion = prop.getProperty("sikulixvjruby");
			SikuliJRuby=SikuliLocalRepo + "/org/JRuby/jruby-complete/" +
							 SikuliJRubyVersion + "/jruby-complete-" + SikuliJRubyVersion + ".jar";

      SikuliSystemVersion = osn + System.getProperty("os.version");
      SikuliJavaVersion = "Java" + JavaVersion + "(" + JavaArch + ")" + JREVersion;
//TODO this should be in RunSetup only
//TODO debug version: where to do in sikulixapi.jar
//TODO need a function: reveal all environment and system information
//      log(lvl, "%s version: downloading from %s", svt, downloadBaseDir);
		} catch (Exception e) {
			Debug.error("Settings: load version file %s did not work", svf);
			Sikulix.terminate(999);
		}

		EndingTypes.put("py", CPYTHON);
		EndingTypes.put("rb", CRUBY);
		EndingTypes.put("txt", CPLAIN);
		for (String k : EndingTypes.keySet()) {
			TypeEndings.put(EndingTypes.get(k), k);
		}
	}

  public static String getSystemInfo() {
    return String.format("%s/%s/%s", SikuliVersionLong, SikuliSystemVersion, SikuliJavaVersion);
  }

  public static void getStatus() {
    log(lvl, "***** Information Dump *****");
    log(lvl, "*** SystemInfo\n%s", getSystemInfo());
    System.getProperties().list(System.out);
    log(lvl, "*** System Environment");
    for (String key : System.getenv().keySet()) {
      System.out.println(String.format("%s = %s", key, System.getenv(key)));
    }
    log(lvl, "*** Java Class Path");
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    for (int i = 0; i < urls.length; i++) {
      System.out.println(String.format("%d: %s", i, urls[i]));
    }
    log(lvl, "***** Information Dump ***** end *****");
  }

	public static void initScriptingSupport() {
		if (scriptRunner.size() == 0) {
			ServiceLoader<IDESupport> sloader = ServiceLoader.load(IDESupport.class);
			Iterator<IDESupport> supIterator = sloader.iterator();
			while (supIterator.hasNext()) {
				IDESupport current = supIterator.next();
				try {
					for (String ending : current.getEndings()) {
						ideSupporter.put(ending, current);
					}
				} catch (Exception ex) {
				}
			}
			ServiceLoader<IScriptRunner> rloader = ServiceLoader.load(IScriptRunner.class);
			Iterator<IScriptRunner> rIterator = rloader.iterator();
			while (rIterator.hasNext()) {
				IScriptRunner current = rIterator.next();
				String name = current.getName();
				if (name != null && !name.startsWith("Not")) {
					scriptRunner.put(name, current);
				}
			}
		}
		if (scriptRunner.size() == 0) {
			Debug.error("Settings: No scripting support available. Rerun Setup!");
			Sikulix.popup("No scripting support available. Rerun Setup!", "SikuliX - Fatal Error!");
			System.exit(1);
		} else {
			RDEFAULT = (String) scriptRunner.keySet().toArray()[0];
			EDEFAULT = scriptRunner.get(RDEFAULT).getFileEndings()[0];
			for (IScriptRunner r : scriptRunner.values()) {
				for (String e : r.getFileEndings()) {
					if (!supportedRunner.contains(EndingTypes.get(e))) {
						supportedRunner.add(EndingTypes.get(e));
					}
				}
			}
		}
	}

	public static boolean hasTypeRunner(String type) {
		return supportedRunner.contains(type);
	}

  public static String getValidImageFilename(String fname) {
    String validEndings = ".png.jpg.jpeg";
    String defaultEnding = ".png";
    int dot = fname.lastIndexOf(".");
    String ending = defaultEnding;
    if (dot > 0) {
      ending = fname.substring(dot);
      if (validEndings.contains(ending.toLowerCase())) {
        return fname;
      }
    } else {
      fname += ending;
      return fname;
    }
    return "";
  }

	public static final int ISWINDOWS = 0;
	public static final int ISMAC = 1;
	public static final int ISLINUX = 2;
	public static final int ISNOTSUPPORTED = 3;
	public static boolean isMacApp = false;
	public static final String appPathMac = "/Applications/SikuliX-IDE.app/Contents";

	public static boolean ThrowException = true; // throw FindFailed exception
	public static float AutoWaitTimeout = 3f; // in seconds
	public static float WaitScanRate = 3f; // frames per second
	public static float ObserveScanRate = 3f; // frames per second
	public static int ObserveMinChangedPixels = 50; // in pixels
	public static int RepeatWaitTime = 1; // wait 1 second for visual to vanish after action
	public static double MinSimilarity = 0.7;
	public static boolean CheckLastSeen = true;
	public static float CheckLastSeenSimilar = 0.95f;
	public static boolean UseImageFinder = false;

	public static double DelayBeforeDrop = 0.3;
	public static double DelayAfterDrag = 0.3;

	/**
	 * Specify a delay between the key presses in seconds as 0.nnn. This only
	 * applies to the next type and is then reset to 0 again. A value &gt; 1 is cut
	 * to 1.0 (max delay of 1 second)
	 */
	public static double TypeDelay = 0.0;
	/**
	 * Specify a delay between the mouse down and up in seconds as 0.nnn. This
	 * only applies to the next click action and is then reset to 0 again. A value
	 * &gt; 1 is cut to 1.0 (max delay of 1 second)
	 */
	public static double ClickDelay = 0.0;

	public static String BundlePath = null;
	public static boolean OcrTextSearch = false;
	public static boolean OcrTextRead = false;
	public static String OcrLanguage = "eng";

	/**
	 * true = start slow motion mode, false: stop it (default: false) show a
	 * visual for SlowMotionDelay seconds (default: 2)
	 */
	public static boolean TRUE = true;
	public static boolean FALSE = false;

	private static boolean ShowActions = false;

	public static boolean isShowActions() {
		return ShowActions;
	}

	public static void setShowActions(boolean ShowActions) {
		if (ShowActions) {
			MoveMouseDelaySaved = MoveMouseDelay;
		} else {
			MoveMouseDelay = MoveMouseDelaySaved;
		}
		Settings.ShowActions = ShowActions;
	}

	public static float SlowMotionDelay = 2.0f; // in seconds
	public static float MoveMouseDelay = 0.5f; // in seconds
	private static float MoveMouseDelaySaved = MoveMouseDelay;

	/**
	 * true = highlight every match (default: false) (show red rectangle around)
	 * for DefaultHighlightTime seconds (default: 2)
	 */
	public static boolean Highlight = false;
	public static float DefaultHighlightTime = 2f;
	public static float WaitAfterHighlight = 0.3f;
	public static boolean ActionLogs = true;
	public static boolean InfoLogs = true;
	public static boolean DebugLogs = false;
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
	 * @return absolute path to the user's extension path
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

	public static String getShortOS() {
		if (isWindows()) {
			return "win";
		}
		if (isMac()) {
			return "mac";
		}
		return "lux";
	}

	public static String getOSVersion() {
		return System.getProperty("os.version");
	}

	public static String getVersion() {
		return SikuliVersion;
	}

	public static String getVersionShort() {
		if (SikuliVersionBetaN > 0 && SikuliVersionBetaN < 99) {
			return bversion;
		} else {
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

	public static void printArgs() {
		if (Debug.getDebugLevel() < lvl) {
			return;
		}
		String[] args = Settings.getSikuliArgs();
		if (args.length > 0) {
			Debug.log(lvl, "--- Sikuli parameters ---");
			for (int i = 0; i < args.length; i++) {
				Debug.log(lvl, "%d: %s", i + 1, args[i]);
			}
		}
		args = Settings.getArgs();
		if (args.length > 0) {
			Debug.log(lvl, "--- User parameters ---");
			for (int i = 0; i < args.length; i++) {
				Debug.log(lvl, "%d: %s", i + 1, args[i]);
			}
		}
	}
}
