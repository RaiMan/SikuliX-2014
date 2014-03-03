/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
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
  private final Map<String, State> eventStates;
  private final Map<String, Long> repeatWaitTimes;
  private final Map<String, Match> lastMatches;
  private final Map<String, Object> eventNames;
  private final Map<String, ObserveEvent.Type> eventTypes;
  private final Map<String, Object> eventCallBacks;
  private final Map<String, Integer> happenedCount;
  private final Map<String, Integer> onChangeNames;
  private int minChanges;
  private boolean sthgLeft;
  private boolean shouldCheckChanges;

  public Observer(Region region) {
    observedRegion = region;
    eventStates = Collections.synchronizedMap(new HashMap<String, State>());
    repeatWaitTimes = Collections.synchronizedMap(new HashMap<String, Long>());
    happenedCount = Collections.synchronizedMap(new HashMap<String, Integer>());
    lastMatches = Collections.synchronizedMap(new HashMap<String, Match>());
    eventNames = Collections.synchronizedMap(new HashMap<String, Object>());
    eventTypes = Collections.synchronizedMap(new HashMap<String, ObserveEvent.Type>());
    eventCallBacks = Collections.synchronizedMap(new HashMap<String, Object>());
    onChangeNames = Collections.synchronizedMap(new HashMap<String, Integer>());
  }

  public void initialize() {
    log(3, "resetting observe states for " + observedRegion.toStringShort());
    sthgLeft = true;
    shouldCheckChanges = true;
    for (String name : eventNames.keySet()) {
      eventStates.put(name, State.FIRST);
      happenedCount.put(name, 0);
    }
    for (String name : onChangeNames.keySet()) {
      happenedCount.put(name, 0);
    }
  }

  public String[] getNames() {
    String[] names = new String[eventNames.size() + onChangeNames.size()];
    int i = 0;
    for (String n : eventNames.keySet()) {
      names[i++] = n;
    }
    for (String n : onChangeNames.keySet()) {
      names[i++] = n;
    }
    return names;
  }

  public void setRegion(Region reg) {
    observedRegion = reg;
  }

  public int getCount(String name) {
    return happenedCount.get(name);
  }

  public int getChangedCount(String name) {
    return happenedCount.get(name);
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

  public <PSC> void addObserver(PSC ptn, ObserverCallBack ob, String name, ObserveEvent.Type type) {
    eventCallBacks.put(name, ob);
    eventStates.put(name, State.FIRST);
    eventNames.put(name, ptn);
  }

  public void removeObserver(String name) {
    Observing.remove(name);
    eventNames.remove(name);
    eventCallBacks.remove(name);
    eventStates.remove(name);
  }

  private void callAppearObserver(String name, Match m) {
    Object ptn = eventNames.get(name);
    log(lvl, "appeared: %s with: %s\nat: %s", name, ptn, m);
    ObserveAppear observeEvent = new ObserveAppear(name, ptn, m, observedRegion);
    Object callBack = eventCallBacks.get(name);
    Observing.addEvent(observeEvent);
    if (callBack != null && callBack instanceof ObserverCallBack) {
      log(lvl, "running call back");
      ((ObserverCallBack) callBack).appeared(observeEvent);
    }
  }

  private void callVanishObserver(String name, Match m) {
    Object ptn = eventNames.get(name);
    log(lvl, "vanished: %s with: %s\nat: %s", name, ptn, m);
    ObserveVanish observeEvent = new ObserveVanish(name, ptn, m, observedRegion);
    Object callBack = eventCallBacks.get(name);
    Observing.addEvent(observeEvent);
    if (callBack != null && callBack instanceof ObserverCallBack) {
      log(lvl, "running call back");
      ((ObserverCallBack) callBack).vanished(observeEvent);
    }
  }
  
  private void callEventObserver(String name, Match m) {
    Object ptn = eventNames.get(name);
    log(lvl, "appeared: %s with: %s\nat: %s", name, ptn, m);
    ObserveAppear observeEvent = new ObserveAppear(name, ptn, m, observedRegion);
    Object callBack = eventCallBacks.get(name);
    Observing.addEvent(observeEvent);
    if (callBack != null && callBack instanceof ObserverCallBack) {
      log(lvl, "running call back");
      ((ObserverCallBack) callBack).appeared(observeEvent);
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
    for (String name : eventStates.keySet()) {
      if (eventStates.get(name) != State.FIRST
              && eventStates.get(name) != State.UNKNOWN
              && eventStates.get(name) != State.REPEAT) {
        continue;
      }
      imgOK = null;
      Object ptn = eventNames.get(name);
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
        eventStates.put(name, State.MISSING);
        continue;
      }
      if (eventStates.get(name) == State.REPEAT) {
        log(lvl, "repeat: checking");
        if (lastMatches.get(name).exists(ptn) != null) {
          if ((new Date()).getTime() > repeatWaitTimes.get(name)) {
            eventStates.put(name, State.APPEARED);
            log(lvl, "repeat: vanish timeout");
            // time out
          } else {
            sthgLeft = true;
          }
          continue; // not vanished within given time or still there
        } else {
          eventStates.put(name, State.UNKNOWN);
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
          lastMatches.put(name, m);
        }
      }
      if (hasMatch) {
        log(lvl + 1, "checkPatterns: " + ptn.toString() + " match: "
                + m.toStringShort() + " in " + observedRegion.toStringShort());
      } else if (eventStates.get(ptn) == State.FIRST) {
        log(lvl + 1, "checkPatterns: " + ptn.toString() + " match: "
                + "NO" + " in " + observedRegion.toStringShort());
        eventStates.put(name, State.UNKNOWN);
      }
      if (appearCallBacks.containsKey(name)) {
        if (eventStates.get(name) != State.APPEARED) {
          if (hasMatch) {
            eventStates.put(name, State.APPEARED);
            happenedCount.put(name, happenedCount.get(name) + 1);
            callAppearObserver(name, m);
          } else {
            sthgLeft = true;
          }
        }
      } else if (vanishCallBacks.containsKey(ptn)) {
        if (eventStates.get(ptn) != State.VANISHED) {
          if (!hasMatch) {
            eventStates.put(name, State.VANISHED);
            happenedCount.put(name, happenedCount.get(name) + 1);
            callVanishObserver(name, lastMatches.get(name));
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

  public void repeat(ObserveEvent.Type type, String name, Match match, long secs) {
    if (type == ObserveEvent.Type.CHANGE) {
      Debug.error("EventMgr: repeat: CHANGE repeats automatically");
    } else if (type == ObserveEvent.Type.VANISH) {
      Debug.error("EventMgr: repeat: not supported for VANISH");
    } else if (type == ObserveEvent.Type.APPEAR) {
      eventStates.put(name, State.REPEAT);
      if (secs <= 0) {
        secs = (long) observedRegion.getWaitForVanish();
      }
      repeatWaitTimes.put(name, (new Date()).getTime() + 1000 * secs);
      log(lvl, "repeat: requested for APPEAR: "
              + eventNames.get(name).toString() + " at " + match.toStringShort() + " after " + secs + " seconds");
      sthgLeft = true;
    }
  }

  public void addChangeObserver(int threshold, ObserverCallBack ob, String name) {
    eventCallBacks.put(name, ob);
    minChanges = getMinChanges();
    onChangeNames.put(name, threshold);
  }

//  public void removeChangeObserver(int threshold) {
//    Observing.remove(onChangeNames.get(threshold));
//    onChangeNames.remove(threshold);
//    changeCallBacks.remove(new Integer(threshold));
//    minChanges = getMinChanges();
//  }
//
  private int getMinChanges() {
    int min = Integer.MAX_VALUE;
    int n;
    for (String name : onChangeNames.keySet()) {
      n = onChangeNames.get(name);
      if (n < min) {
        min = n;
      }
    }
    return min;
  }

  private int callChangeObserver(FindResults results) {
    int activeChangeCallBacks = 0;
    int n;
    log(lvl, "changes: %d in: %s", results.size(), observedRegion);
    for (String name : onChangeNames.keySet()) {
      n = onChangeNames.get(name);
      if (happenedCount.get(name) == -1) {
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
        happenedCount.put(name, happenedCount.get(name) + 1);
        ObserveChange observeEvent = new ObserveChange(name, changes, observedRegion, n);
        Object callBack = eventCallBacks.get(name);
        Observing.addEvent(observeEvent);
        if (callBack != null && callBack instanceof ObserverCallBack) {
          log(lvl, "running call back");
          ((ObserverCallBack) callBack).changed(observeEvent);
        } else {
          // onChange only repeated if CallBack given
          happenedCount.put(name, -1);
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
        if (0 == callChangeObserver(results)) {
          changesObserved = false;
        }
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
      if (shouldCheckChanges && onChangeNames.size() > 0) {
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
