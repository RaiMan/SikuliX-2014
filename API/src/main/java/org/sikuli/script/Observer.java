/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.AWTException;
import java.util.*;
import org.sikuli.natives.FindInput;
import org.sikuli.natives.FindResult;
import org.sikuli.natives.FindResults;
import org.sikuli.natives.Mat;
import org.sikuli.natives.Vision;

/**
 * INTERNAL USE implements the observe action for a region and calls the ObserverCallBacks
 */
public class Observer {

  private static String me = "Observer";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }

  protected enum State {

    FIRST, UNKNOWN, MISSING, APPEARED, VANISHED, REPEAT
  }
  private Region observedRegion;
  private Mat lastImgMat = null;
  private org.opencv.core.Mat lastImageMat = null;
  private Map<Object, State> eventStates;
  private Map<Object, Long> repeatWaitTimes;
  private Map<Object, Integer> happenedCount;
  private Map<Object, Match> lastMatches;
  private Map<Object, String> eventNames;
  private Map<Object, Object> appearCallBacks, vanishCallBacks;
  private Map<Integer, Object> changeCallBacks;
  private Map<Integer, Integer> changedCount;
  private Map<Integer, String> onChangeNames;
  private int minChanges;
  private boolean sthgLeft;
  private boolean shouldCheckChanges;

  public Observer(Region region) {
    observedRegion = region;
    eventStates = new HashMap<Object, State>();
    repeatWaitTimes = new HashMap<Object, Long>();
    happenedCount = new HashMap<Object, Integer>();
    lastMatches = new HashMap<Object, Match>();
    eventNames = new HashMap<Object, String>();
    appearCallBacks = new HashMap<Object, Object>();
    vanishCallBacks = new HashMap<Object, Object>();
    changeCallBacks = new HashMap<Integer, Object>();
    changedCount = new HashMap<Integer, Integer>();
    onChangeNames = new HashMap<Integer, String>();
  }

  public void initialize() {
    log(3, "resetting observe states for " + observedRegion.toStringShort());
    sthgLeft = true;
    shouldCheckChanges = true;
    for (Object ptn : eventStates.keySet()) {
      eventStates.put(ptn, State.FIRST);
      happenedCount.put(ptn, 0);
    }
    for (int n : changeCallBacks.keySet()) {
      changedCount.put(n, 0);
    }
  }

  public void setRegion(Region reg) {
    observedRegion = reg;
  }

  public int getCount(Object ptn) {
    return happenedCount.get(ptn);
  }

  public int getChangedCount(int index) {
    return changedCount.get(index);
  }
  
  private <PSC> float getSimiliarity(PSC ptn) {
    float similarity = -1f;
    if (ptn instanceof Pattern) {
      similarity = ((Pattern) ptn).getSimilar();
    }
    if (similarity < 0) {
      similarity = (float) Settings.MinSimilarity;
    }
    return similarity;
  }

  public <PSC> void addAppearObserver(PSC ptn, ObserverCallBack ob, String name) {
    appearCallBacks.put(ptn, ob);
    eventStates.put(ptn, State.FIRST);
    eventNames.put(ptn, name);
  }

  public <PSC> void removeAppearObserver(PSC ptn) {
    Observing.remove(eventNames.get(ptn));
    eventNames.remove(ptn);
    appearCallBacks.remove(ptn);
    eventStates.remove(ptn);
  }

  public <PSC> void addVanishObserver(PSC ptn, ObserverCallBack ob, String name) {
    vanishCallBacks.put(ptn, ob);
    eventStates.put(ptn, State.FIRST);
    eventNames.put(ptn, name);
  }

  public <PSC> void removeVanishObserver(PSC ptn) {
    Observing.remove(eventNames.get(ptn));
    eventNames.remove(ptn);
    vanishCallBacks.remove(ptn);
    eventStates.remove(ptn);
  }

  private void callAppearObserver(Object ptn, Match m) {
    log(lvl, "appeared: %s with: %s\nat: %s", eventNames.get(ptn), ptn, m);
    ObserveAppear observeEvent = new ObserveAppear(ptn, m, observedRegion);
    Object callBack = appearCallBacks.get(ptn);
    Observing.addEvent(eventNames.get(ptn), observeEvent);
    if (callBack != null && callBack instanceof ObserverCallBack) {
      log(lvl, "running call back");
      ((ObserverCallBack) appearCallBacks.get(ptn)).appeared(observeEvent);
    }
  }

  private void callVanishObserver(Object ptn, Match m) {
    log(lvl, "vanished: %s with: %s\nat: %s", eventNames.get(ptn), ptn, m);
    ObserveVanish observeEvent = new ObserveVanish(ptn, m, observedRegion);
    Object callBack = vanishCallBacks.get(ptn);
    Observing.addEvent(eventNames.get(ptn), observeEvent);
    if (callBack != null && callBack instanceof ObserverCallBack) {
      log(lvl, "running call back");
      ((ObserverCallBack) vanishCallBacks.get(ptn)).vanished(observeEvent);
    }
  }

  private void checkPatterns(ScreenImage simg) {
    Finder finder = null;
    if (Settings.UseImageFinder) {
      finder = new ImageFinder(observedRegion);
      ((ImageFinder) finder).setIsMultiFinder();
    } else {
      finder = new Finder(simg, observedRegion);
    }
    String imgOK;
    log(lvl + 1, "checkPatterns entry: sthgLeft: %s isObserving: %s", sthgLeft, observedRegion.isObserving());
    for (Object ptn : eventStates.keySet()) {
      if (eventStates.get(ptn) != State.FIRST
              && eventStates.get(ptn) != State.UNKNOWN
              && eventStates.get(ptn) != State.REPEAT) {
        continue;
      }
      imgOK = null;
      if (ptn instanceof String) {
        imgOK = finder.find((String) ptn);
        Image img = Image.create((String) ptn);
        if (img.isValid()) {
          imgOK = finder.find(img);
        } else if (img.isText()) {
          imgOK = finder.findText((String) ptn);
        }
      } else if (ptn instanceof Pattern) {
        imgOK = finder.find((Pattern) ptn);
      } else if (ptn instanceof Image) {
        imgOK = finder.find((Image) ptn);
      }
      if (null == imgOK) {
        Debug.error("EventMgr: checkPatterns: Image not valid", ptn);
        eventStates.put(ptn, State.MISSING);
        continue;
      }
      if (eventStates.get(ptn) == State.REPEAT) {
        log(lvl, "repeat: checking");
        if (lastMatches.get(ptn).exists(ptn) != null) {
          if ((new Date()).getTime() > repeatWaitTimes.get(ptn)) {
            eventStates.put(ptn, State.APPEARED);
            log(lvl, "repeat: vanish timeout");
            // time out
          } else {
            sthgLeft = true;
          }
          continue; // not vanished within given time or still there
        } else {
          eventStates.put(ptn, State.UNKNOWN);
          sthgLeft = true;
          log(lvl, "repeat: has vanished");
          continue; // has vanished, repeat
        }
      }
      Match m = null;
      boolean hasMatch = false;
      if (finder.hasNext()) {
        m = finder.next();
        if (m.getScore() >= getSimiliarity(ptn)) {
          hasMatch = true;
          lastMatches.put(ptn, m);
        }
      }
      if (hasMatch) {
        log(lvl + 1, "checkPatterns: " + ptn.toString() + " match: "
                + m.toStringShort() + " in " + observedRegion.toStringShort());
      } else if (eventStates.get(ptn) == State.FIRST) {
        log(lvl + 1, "checkPatterns: " + ptn.toString() + " match: "
                + "NO" + " in " + observedRegion.toStringShort());
        eventStates.put(ptn, State.UNKNOWN);
      }
      if (appearCallBacks.containsKey(ptn)) {
        if (eventStates.get(ptn) != State.APPEARED) {
          if (hasMatch) {
            eventStates.put(ptn, State.APPEARED);
            happenedCount.put(ptn, happenedCount.get(ptn) + 1);
            callAppearObserver(ptn, m);
          } else {
            sthgLeft = true;
          }
        }
      } else if (vanishCallBacks.containsKey(ptn)) {
        if (eventStates.get(ptn) != State.VANISHED) {
          if (!hasMatch) {
            eventStates.put(ptn, State.VANISHED);
            happenedCount.put(ptn, happenedCount.get(ptn) + 1);
            callVanishObserver(ptn, lastMatches.get(ptn));
          } else {
            sthgLeft = true;
          }
        }
      }
      if (!observedRegion.isObserving()) {
        break;
      }
    }
    log(lvl + 1, "checkPatterns exit: sthgLeft: %s isObserving: %s", sthgLeft, observedRegion.isObserving());
  }

  public void repeat(ObserveEvent.Type type, Object pattern, Match match, long secs) {
    if (type == ObserveEvent.Type.CHANGE) {
      Debug.error("EventMgr: repeat: CHANGE repeats automatically");
    } else if (type == ObserveEvent.Type.VANISH) {
      Debug.error("EventMgr: repeat: not supported for VANISH");
    } else if (type == ObserveEvent.Type.APPEAR) {
      eventStates.put(pattern, State.REPEAT);
      if (secs <= 0) {
        secs = (long) observedRegion.getWaitForVanish();
      }
      repeatWaitTimes.put(pattern, (new Date()).getTime() + 1000 * secs);
      log(lvl, "repeat: requested for APPEAR: "
              + pattern.toString() + " at " + match.toStringShort() + " after " + secs + " seconds");
      sthgLeft = true;
    }
  }

  public void addChangeObserver(int threshold, ObserverCallBack ob, String name) {
    changeCallBacks.put(new Integer(threshold), ob);
    minChanges = getMinChanges();
    onChangeNames.put(threshold, name);
  }

  public void removeChangeObserver(int threshold) {
    Observing.remove(onChangeNames.get(threshold));
    eventNames.remove(threshold);
    changeCallBacks.remove(new Integer(threshold));
    minChanges = getMinChanges();
  }

  private int getMinChanges() {
    int min = Integer.MAX_VALUE;
    for (Integer n : changeCallBacks.keySet()) {
      if (n < min) {
        min = n;
      }
    }
    return min;
  }

  private int callChangeObserver(FindResults results) {
    int activeChangeCallBacks = 0;
    log(lvl, "changes: %d in: %s", results.size(), observedRegion);
    for (Integer n : changeCallBacks.keySet()) {
      if (changedCount.get(n) == -1) {
        continue;
      }
      activeChangeCallBacks++;
      List<Match> changes = new ArrayList<Match>();
      for (int i = 0; i < results.size(); i++) {
        FindResult r = results.get(i);
        if (r.getW() * r.getH() >= n) {
          changes.add(observedRegion.toGlobalCoord(new Match(r, observedRegion.getScreen())));
        }
      }
      if (changes.size() > 0) {
        changedCount.put(n, changedCount.get(n) + 1);
        ObserveChange observeEvent = new ObserveChange(changes, observedRegion, n);
        Object callBack = changeCallBacks.get(n);
        Observing.addEvent(onChangeNames.get(n), observeEvent);
        if (callBack != null && callBack instanceof ObserverCallBack) {
          log(lvl, "running call back");
          ((ObserverCallBack) changeCallBacks.get(n)).changed(observeEvent);
        } else {
          // onChange only repeated if CallBack given
          changedCount.put(n, -1);
          activeChangeCallBacks--;
        }
      }
    }
    return activeChangeCallBacks;
  }

  private boolean checkChanges(ScreenImage img) {
    boolean changesObserved = true;
    if (Settings.UseImageFinder) {
      //TODO hack to hide the native call - should be at the top
      if (lastImageMat == null) {
        lastImageMat = new org.opencv.core.Mat();
      }
      if (lastImageMat.empty()) {
        lastImageMat = Image.createMat(img.getImage());
        return true;
      }
      ImageFinder f = new ImageFinder(lastImageMat);
      f.setMinChanges(minChanges);
      org.opencv.core.Mat current = Image.createMat(img.getImage());
      if (f.hasChanges(current)) {
        //TODO implement ChangeObserver: processing changes
        log(lvl, "TODO: processing changes");
      }
      lastImageMat = current;
    } else {
      if (lastImgMat == null) {
        lastImgMat = Image.convertBufferedImageToMat(img.getImage());
        return true;
      }
      FindInput fin = new FindInput();
      fin.setSource(lastImgMat);
      Mat target = Image.convertBufferedImageToMat(img.getImage());
      fin.setTarget(target);
      fin.setSimilarity(minChanges);
      FindResults results = Vision.findChanges(fin);
      if (results.size() > 0) {
        if (0 == callChangeObserver(results)) changesObserved = false;
      }
      lastImgMat = target;
    }
    return changesObserved;
  }

  public boolean update(ScreenImage simg) {
    log(lvl + 1, "update entry: sthgLeft: %s obs? %s", sthgLeft, observedRegion.isObserving());
    boolean ret;
    boolean changesObserved;
    ret = sthgLeft;
    if (sthgLeft) {
      sthgLeft = false;
      checkPatterns(simg);
      if (!observedRegion.isObserving()) {
        return false;
      }
    }
    if (observedRegion.isObserving()) {
      ret = sthgLeft;
      if (shouldCheckChanges && changeCallBacks.size() > 0) {
        changesObserved = checkChanges(simg);
        shouldCheckChanges = changesObserved;
        if (!observedRegion.isObserving()) {
          return false;
        }
        ret = changesObserved;
      }
    }
    log(lvl + 1, "update exit: ret: %s", ret);
    return ret;
  }
}
