/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2014
 */
package org.sikuli.basics;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * used as anchor for the preferences store and for global supporting features
 */
public class Sikulix {

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static String me = "SikuliX";
  private static String mem = "...";
  private static int lvl = 3;
  private static String msg;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, level < 0 ? "error" : "debug",
            me + ": " + mem + ": " + message, args);
  }

  private static void log0(int level, String message, Object... args) {
    Debug.logx(level, level < 0 ? "error" : "debug",
            me + ": " + message, args);
  }
  //</editor-fold>

  private static IScriptRunner runner;
  private static final String ScriptSikuliXCL = "org.sikuli.script.Sikulix";
  private static final String ScriptKeyCL = "org.sikuli.script.Key";
  private static Class ScriptCl, KeyCl;
  private static Method endWhat, keyBoardSetup, setBundlePath;
  private static boolean runningSetup = false;
  private static boolean runningFromJar;
  private static String jarPath;
  private static String jarParentPath;

  static {
    CodeSource codeSrc = Sikulix.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      URL jarURL = codeSrc.getLocation();
      jarPath = FileManager.slashify(new File(jarURL.getPath()).getAbsolutePath(), false);
      jarParentPath = (new File(jarPath)).getParent();
      if (jarPath.endsWith(".jar")) {
        runningFromJar = true;
      } else {
        jarPath += "/";
      }
    }
  }

  public static boolean isRunningFromJar() {
    return runningFromJar;
  }

  public static String getJarPath() {
    return jarPath;
  }

  public static String getJarParentPath() {
    return jarParentPath;
  }

  public static void setRunningSetup(boolean _runningSetup) {
    runningSetup = _runningSetup;
  }
  private static JFrame splash = null;
  private static long start = 0;

  public static void displaySplash(String[] args) {
    if (args == null) {
      if (splash != null) {
        splash.dispose();
      }
      if (start > 0) {
        Debug.log(3, "Sikuli-Script startup: " + ((new Date()).getTime() - start));
        start = 0;
      }
      return;
    }
    if (args.length > 0 && (args[0].contains("-testSetup") || args[0].startsWith("-i"))) {
      start = (new Date()).getTime();
      String[] splashArgs = new String[]{
        "splash", "#", "#" + Settings.SikuliVersionScript, "", "#", "#... starting - please wait ..."};
      for (String e : args) {
        splashArgs[3] += e + " ";
      }
      splashArgs[3] = splashArgs[3].trim();
      splash = new MultiFrame(splashArgs);
    }
  }

  public static void displaySplashFirstTime(String[] args) {
    if (args == null) {
      if (splash != null) {
        splash.dispose();
      }
      if (start > 0) {
        Debug.log(3, "Sikuli-IDE environment setup: " + ((new Date()).getTime() - start));
        start = 0;
      }
      return;
    }
    start = (new Date()).getTime();
    String[] splashArgs = new String[]{
      "splash", "#", "#" + Settings.SikuliVersionIDE, "", "#", "#... setting up environement - please wait ..."};
    splash = new MultiFrame(splashArgs);
  }

  private static void callScriptEndMethod(String m, int n) {
    try {
      ScriptCl = Class.forName(ScriptSikuliXCL);
      endWhat = ScriptCl.getMethod(m, new Class[]{int.class});
      endWhat.invoke(ScriptCl, new Object[]{n});
    } catch (Exception ex) {
      Debug.error("BasicsFinalCleanUp: Fatal Error 999: could not be run!");
      System.exit(999);
    }
  }

  public static int[] callKeyToJavaKeyCodeMethod(String key) {
    try {
      KeyCl = Class.forName(ScriptKeyCL);
      keyBoardSetup = KeyCl.getMethod("toJavaKeyCode", new Class[]{String.class});
      return (int[]) keyBoardSetup.invoke(KeyCl, new Object[]{key});
    } catch (Exception ex) {
      Debug.error("Invoke KeyToJavaKeyCodeMethod: Fatal Error 999: could not be run!");
      return null;
    }
  }

  public static void callKeyBoardSetup() {
    try {
      KeyCl = Class.forName(ScriptKeyCL);
      keyBoardSetup = KeyCl.getMethod("keyBoardSetup", new Class[]{});
      keyBoardSetup.invoke(KeyCl, new Object[]{});
    } catch (Exception ex) {
      Debug.error("Invoke Key.keyBoardSetup: Fatal Error 999: could not be run!");
    }
  }

	public static void callImagePathSetBundlePath(String path) {
    try {
      ScriptCl = Class.forName("org.sikuli.script.ImagePath");
      setBundlePath = ScriptCl.getMethod("setBundlePath", new Class[]{String.class});
      setBundlePath.invoke(ScriptCl, new Object[]{path});
    } catch (Exception ex) {
      Debug.error("Invoke ImagePathSetBundlePath: Fatal Error 999: could not be run!");
      System.exit(999);
    }
  }

  public static void pause(int time) {
    try {
      Thread.sleep(time * 1000);
    } catch (InterruptedException ex) {
    }
  }

  public static void endNormal(int n) {
    callScriptEndMethod("endNormal", n);
  }

  public static void endWarning(int n) {
    callScriptEndMethod("endWarning", n);
  }

  public static void endError(int n) {
    callScriptEndMethod("endError", n);
  }

  public static void terminate(int n) {
    Debug.error("Terminating SikuliX after a fatal error"
            + (n == 0 ? "" : "(%d)")
            + "! Sorry, but it makes no sense to continue!\n"
            + "If you do not have any idea about the error cause or solution, run again\n"
            + "with a Debug level of 3. You might paste the output to the Q&A board.", n);
    if (runningSetup) {
      RunSetup.popError("Something serious happened! Sikuli not useable!\n"
              + "Check the error log at " + Debug.logfile);
      System.exit(0);
    }
    cleanUp(0);
    System.exit(1);
  }

  public static void cleanUp(int n) {
    callScriptEndMethod("cleanUp", n);
  }

  /**
   * Finds a ScriptRunner implementation to execute the script.
   *
   * @param name Name of the ScriptRunner, might be null (then type is used)
   * @param ending fileending of script to run
   * @return first ScriptRunner with matching name or file ending, null if none found
   */
  public static IScriptRunner getScriptRunner(String name, String ending, String[] args) {
    runner = null;
    ServiceLoader<IScriptRunner> loader = ServiceLoader.load(IScriptRunner.class);
    Iterator<IScriptRunner> scriptRunnerIterator = loader.iterator();
    while (scriptRunnerIterator.hasNext()) {
      IScriptRunner currentRunner = scriptRunnerIterator.next();
      if (currentRunner.getName().startsWith("Not")) {
        continue;
      }
      if ((name != null && currentRunner.getName().toLowerCase().equals(name.toLowerCase()))
              || (ending != null && currentRunner.hasFileEnding(ending) != null)) {
        runner = currentRunner;
        runner.init(args);
        break;
      }
    }
    if (name != null && runner == null) {
      if (args != null && args.length == 1 && "convertSrcToHtml".equals(args[0])) {
        return null;
      }
      if (name != null) {
        Debug.error("Fatal error 121: Could not load script runner with name: %s", name);
        Sikulix.terminate(121);
      } else if (ending != null) {
        Debug.error("Fatal error 120: Could not load script runner for ending: %s", ending);
        Sikulix.terminate(120);
      } else {
        Debug.error("Fatal error 122: While loading script runner with name=%s and ending= %s", name, ending);
        Sikulix.terminate(122);
      }
    }
    return runner;
  }

  public static IScriptRunner setRunner(IScriptRunner _runner) {
    runner = _runner;
    return runner;
  }

  public static IScriptRunner getRunner() {
    return runner;
  }

  protected static boolean addToClasspath(String jar) {
    log0(lvl, "add to classpath: " + jar);
		File jarFile = new File(jar);
		if (!jarFile.exists()) {
			log0(-1, "does not exist - not added");
			return false;
		}
    Method method;
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    for (int i = 0; i < urls.length; i++) {
      log0(lvl + 1, "%d: %s", i, urls[i]);
    }
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
      log0(-1, ex.getMessage());
      return false;
    }
    urls = sysLoader.getURLs();
    log0(lvl + 1, "after adding to classpath");
    for (int i = 0; i < urls.length; i++) {
      log0(lvl + 1, "%d: %s", i, urls[i]);
    }
    return true;
  }

  public static String[] collectOptions(String type, String[] args) {
    List<String> resArgs = new ArrayList<String>();
    if (args != null) {
      resArgs.addAll(Arrays.asList(args));
    }

    String msg = "-----------------------   You might set some options    -----------------------";
    msg += "\n\n";
    msg += "-r name       ---   Run script name: foo[.sikuli] or foo.skl (no IDE window)";
    msg += "\n";
    msg += "-u [file]        ---   Write user log messages to file (default: <WorkingFolder>/UserLog.txt )";
    msg += "\n";
    msg += "-f [file]         ---   Write Sikuli log messages to file (default: <WorkingFolder>/SikuliLog.txt)";
    msg += "\n";
    msg += "-d n             ---   Set a higher level n for Sikuli's debug messages (default: 0)";
    msg += "\n";
    msg += "-- …more…         All space delimited entries after -- go to sys.argv";
    msg += "\n                           \"<some text>\" makes one parameter (may contain intermediate blanks)";
    msg += "\n\n";
    msg += "-------------------------------------------------------------------------";
    msg += "\n";
    msg += "-d                Special debugging option in case of mysterious errors:";
    msg += "\n";
    msg += "                    Debug level is set to 3 and debug output goes to <WorkingFolder>/SikuliLog.txt";
    msg += "\n";
    msg += "                    Content might be used to ask questions or report bugs";
    msg += "\n";
    msg += "-------------------------------------------------------------------------";
    msg += "\n";
    msg += "                    Just click OK to start IDE with no options - defaults will be used";

    String ret = JOptionPane.showInputDialog(null, msg, "SikuliX: collect runtime options",
            JOptionPane.QUESTION_MESSAGE);

    if (ret == null) {
      return null;
    }
    log0(0, "[" + ret + "]");
    if (!ret.isEmpty()) {
      System.setProperty("sikuli.SIKULI_COMMAND", ret);
      resArgs.addAll(Arrays.asList(ret.split(" +")));
    }
    return resArgs.toArray(new String[0]);
  }

  public static String arrayToString(String[] args) {
    String ret = "";
    for (String s : args) {
      if (s.contains(" ")) {
        s = "\"" + s + "\"";
      }
      ret += s + " ";
    }
    return ret;
  }

  public static boolean exportPrefs(String path) {
    return true;
  }

  public static boolean importPrefs(String path) {
    return true;
  }

  public static boolean popAsk(String msg) {
		return popAsk(msg, null);
	}

  public static boolean popAsk(String msg, String title) {
		if (title == null) title = "... something to decide!";
    int ret = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
    if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
      return false;
    }
    return true;
  }

  public static String popSelect(String msg, String[] options, String preset) {
		return popSelect(msg, null, options, preset);
	}

  public static String popSelect(String msg, String[] options) {
		return popSelect(msg, null, options, options[0]);
	}

	public static String popSelect(String msg, String title, String[] options) {
		return popSelect(msg, title, options, options[0]);
	}

	public static String popSelect(String msg, String title, String[] options, String preset) {
		if (title == null) {
			title = "... something to select!";
		}
		return (String) JOptionPane.showInputDialog(null, msg, title, JOptionPane.PLAIN_MESSAGE, null, options, preset);
	}

  /**
   * request user's input as one line of text <br>
   * with hidden = true: <br>
   * the dialog works as password input (input text hidden a s bullets) <br>
   * take care to destroy the return value as soon as possible (internally security is granted)
   * @param msg
   * @param preset
   * @param title
   * @param hidden
   * @return
   */
  public static String input(String msg, String preset, String title, boolean hidden) {
    if (!hidden) {
      if ("".equals(title)) {
        title = "Sikuli input request";
      }
      return (String) JOptionPane.showInputDialog(null, msg, title, JOptionPane.PLAIN_MESSAGE, null, null, preset);
    } else {
      preset = "";
      JTextArea tm = new JTextArea(msg);
      tm.setColumns(20);
      tm.setLineWrap(true);
      tm.setWrapStyleWord(true);
      tm.setEditable(false);
      tm.setBackground(new JLabel().getBackground());

      JPasswordField pw = new JPasswordField(preset);

      JPanel pnl = new JPanel();
      pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
      pnl.add(pw);
      pnl.add(Box.createVerticalStrut(10));
      pnl.add(tm);

      if (0 == JOptionPane.showConfirmDialog(null, pnl, title, JOptionPane.OK_CANCEL_OPTION)) {
        char[] pwc = pw.getPassword();
        String pwr = "";
        for (int i = 0; i < pwc.length; i++) {
          pwr = pwr + pwc[i];
          pwc[i] = 0;
        }
        return pwr;
      } else {
        return "";
      }
    }
  }

  public static String input(String msg, String title, boolean hidden) {
    return input(msg, "", title, hidden);
  }

  public static String input(String msg, boolean hidden) {
    return input(msg, "", "", hidden);
  }

  public static String input(String msg, String preset, String title) {
    return input(msg, preset, title, false);
  }

  public static String input(String msg, String preset) {
    return input(msg, preset, "", false);
  }

  public static String input(String msg) {
    return input(msg, "", "", false);
  }

  /**
  * Shows a dialog request to enter text in a multiline text field <br>
  * Though not all text might be visible, everything entered is delivered with the returned text <br>
  * The main purpose for this feature is to allow pasting text from somewhere preserving line breaks <br>
  * @param msg the message to display.
  * @param title the title for the dialog (default: Sikuli input request)
  * @param lines the maximum number of lines visible in the text field (default 9)
  * @param width the maximum number of characters visible in one line (default 20)
  * @return The user's input including the line breaks.
  */
  public static String inputText(String msg, String title, int lines, int width) {
    width = Math.max(20, width);
    lines = Math.max(9, lines);
    if ("".equals(title)) {
      title = "Sikuli input request";
    }
    JTextArea ta = new JTextArea("");
    int w = width*ta.getFontMetrics(ta.getFont()).charWidth('m');
    int h = (int) (lines*ta.getFontMetrics(ta.getFont()).getHeight());
    ta.setPreferredSize(new Dimension(w,h));
    ta.setMaximumSize(new Dimension(w,2*h));

    JScrollPane sp = new JScrollPane(ta);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

    JTextArea tm = new JTextArea(msg);
    tm.setColumns(width);
    tm.setLineWrap(true);
    tm.setWrapStyleWord(true);
    tm.setEditable(false);
    tm.setBackground(new JLabel().getBackground());

    JPanel pnl = new JPanel();
    pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
    pnl.add(sp);
    pnl.add(Box.createVerticalStrut(10));
    pnl.add(tm);
    pnl.add(Box.createVerticalStrut(10));

    if (0 == JOptionPane.showConfirmDialog(null, pnl, title, JOptionPane.OK_CANCEL_OPTION)) {
      return ta.getText();
    } else {
      return "";
    }
  }

  public static void popup(String message) {
    popup(message, "Sikuli");
  }

  public static void popup(String message, String title) {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
  }

	public static void popError(String message) {
    popup(message, "Sikuli");
	}

	public static void popError(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

  public static String run(String cmdline) {
    IResourceLoader loader = FileManager.getNativeLoader("basic", new String[0]);
    String[] args = new String[]{cmdline};
    loader.doSomethingSpecial("runcmd", args);
    return args[0];
  }
}
