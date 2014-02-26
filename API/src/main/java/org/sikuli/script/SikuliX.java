/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.List;
import org.sikuli.basics.CommandArgs;
import org.sikuli.basics.Debug;
import org.sikuli.basics.SikuliScript;

/**
 * INTERNAL USE
 * global services for package API
 */
public class SikuliX {

  private static final String me = "SikuliX: ";
  private static List<Region> runningObservers = new ArrayList<Region>();

  public static void addRunningObserver(Region r) {
    runningObservers.add(r);
    Debug.log(3, me + "add observer: now running %d observer(s)", runningObservers.size());
  }

  public static void removeRunningObserver(Region r) {
    runningObservers.remove(r);
    Debug.log(3, me + "remove observer: now running %d observer(s)", runningObservers.size());
  }

  public static void stopRunningObservers() {
    if (runningObservers.size() > 0) {
      Debug.log(3, me + "stopping %d running observer(s)", runningObservers.size());
      for (Region r : runningObservers) {
        r.stopObserver();
      }
      runningObservers.clear();
    }
    Observing.clear();
  }

  public static void endNormal(int n) {
    Debug.log(3, me + "endNormal: %d", n);
    cleanUp(0);
    System.exit(n);
  }

  public static void endWarning(int n) {
    Debug.log(3, me + "endWarning: %d", n);
    cleanUp(0);
    System.exit(n);
  }

  public static void endError(int n) {
    Debug.log(3, me + "endError: %d", n);
    cleanUp(0);
    System.exit(n);
  }

  public static void endFatal(int n) {
    Debug.error("Terminating SikuliX after a fatal error"
            + (n == 0 ? "" : "(%d)")
            + "! Sorry, but it makes no sense to continue!\n"
            + "If you do not have any idea about the error cause or solution, run again\n"
            + "with a Debug level of 3. You might paste the output to the Q&A board.", n);
    cleanUp(0);
    System.exit(n);
  }

  public static void cleanUp(int n) {
    Debug.log(3, me + "cleanUp: %d", n);
    ScreenHighlighter.closeAll();
    stopRunningObservers();
    if (CommandArgs.isIDE()) {
      //TODO reset selected options to defaults
    }
  }

  public static boolean testSetup() {
    Region r = Region.create(0, 0, 100, 100);
    Image img = new Image(r.getScreen().capture(r).getImage());
    Pattern p = new Pattern(img);
    Finder f = new Finder(img);
    if (null != f.find(p) && f.hasNext()) {
      SikuliScript.popup("Hallo from Java-API.testSetup\nSikuli seems to be working fine!\n\nHave fun!");
      return true;
    }
    return false;
  }
}
