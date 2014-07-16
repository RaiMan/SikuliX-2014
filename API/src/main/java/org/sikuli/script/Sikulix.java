/*
 * Copyright 2010-2014, Sikuli.org, Sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.sikuli.basics.CommandArgs;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.HotkeyManager;
import org.sikuli.basics.Settings;

/**
 * global services for package API
 */
public class Sikulix {

  private static final String me = "SikuliX: ";

	public static void main(String[] args) {
		Debug.test(me + "main: nothing to do (yet)");
	}

  /**
   * call this, to initialize Sikuli up to useability
   * @return the primary screen object
   */
  public static Screen init() {
//TODO collect initializations here
    Mouse.init();
    Keys.init();
    return new Screen();
  }

  /**
   * INTERNAL USE:
   * convenience function: runs {@link #cleanUp(int)},
   * prints a message endNormal and terminates with returncode
   * @param n
   */
  public static void endNormal(int n) {
    Debug.log(0, me + "endNormal: %d", n);
    cleanUp(n);
    System.exit(n);
  }

  /**
   * INTERNAL USE:
   * convenience function: runs {@link #cleanUp(int)},
   * prints a message endWarning and terminates with returncode
   *
   * @param n returncode
   */
  public static void endWarning(int n) {
    Debug.log(0, me + "endWarning: %d", n);
    cleanUp(n);
    System.exit(n);
  }

  /**
   * INTERNAL USE:
   * convenience function: runs {@link #cleanUp(int)},
   * prints a message endError and terminates with returncode
   *
   * @param n
   */
  public static void endError(int n) {
    Debug.log(0, me + "endError: %d", n);
    cleanUp(n);
    System.exit(n);
  }

  /**
   * INTERNAL USE:
   * convenience function: runs {@link #cleanUp(int)},
   * prints a message (numbered) fatal error and terminates with returncode
   *
   * @param n
   */
  public static void endFatal(int n) {
    Debug.error("Terminating SikuliX after a fatal error"
            + (n == 0 ? "" : "(%d)")
            + "! Sorry, but it makes no sense to continue!\n"
            + "If you do not have any idea about the error cause or solution, run again\n"
            + "with a Debug level of 3. You might paste the output to the Q&A board.", n);
    cleanUp(n);
    System.exit(n);
  }

  /**
   * INTERNAL USE:
   * resets stateful Sikuli X features:
   * ScreenHighlighter, Observing, Mouse, Key, Hotkeys
   * When in IDE: resets selected options to defaults (TODO)
   *
   * @param n returncode
   */
  public static void cleanUp(int n) {
    Debug.log(3, me + "API cleanUp: %d", n);
    ScreenHighlighter.closeAll();
    Observing.cleanUp();
    Mouse.reset();
    //TODO move to class Keys after implementation
    Screen.getPrimaryScreen().getRobot().keyUp();
    //TODO what about remote screen sessions????
    HotkeyManager.reset();
    if (CommandArgs.isIDE()) {
      //TODO reset selected options to defaults
    }
  }

  /**
   * INTERNAL USE:
 used in setup: tests basic Sikulix features
   *
   * @return success
   */
  public static boolean testSetup() {
    Region r = Region.create(0, 0, 100, 100);
    Image img = new Image(r.getScreen().capture(r).getImage());
    Pattern p = new Pattern(img);
    Finder f = new Finder(img);
    if (null != f.find(p) && f.hasNext()) {
      org.sikuli.basics.Sikulix.popup("Hallo from SkuliX-API.testSetup\nSikuli seems to be working!\n\nHave fun!");
      return true;
    }
    return false;
  }

  /**
   * add a jar to end of classpath at runtime
   *
   * @param jar absolute filename
   * @return success
   */
  public static boolean addToClasspath(String jar) {
    return addToClasspath(jar, 0);
  }

  private static boolean addToClasspath(String jar, int loglevel) {
    Method method;
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    Debug.log(loglevel, "add to classpath: " + jar);
    dumpClasspath(false, loglevel + 1 );
    Class sysclass = URLClassLoader.class;
    try {
      jar = FileManager.slashify(new File(jar).getAbsolutePath(), false);
      if (Settings.isWindows()) {
        jar = "/" + jar;
      }
      URL u = (new URI("file", jar, null)).toURL();
      method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(sysLoader, new Object[]{u});
    } catch (Exception ex) {
      Debug.error("Did not work: %s", ex.getMessage());
      return false;
    }
    urls = sysLoader.getURLs();
    Debug.log(loglevel + 1, "after adding to classpath");
    dumpClasspath(false, loglevel + 1 );
    return true;
  }

  /**
   * print the classpath at runtime
   */
  public static void dumpClasspath() {
    dumpClasspath(true, 0);
  }

  private static void dumpClasspath(boolean verbose, int loglevel) {
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    if (verbose) {
      Debug.log(loglevel, "***** Classpath dump *****");
    }
    for (int i = 0; i < urls.length; i++) {
      Debug.log(loglevel, "%d: %s", i, urls[i]);
    }
    if (verbose) {
      Debug.log(loglevel, "***** Classpath dump ***** end");
    }
  }
}
