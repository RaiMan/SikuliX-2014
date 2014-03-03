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
import org.sikuli.basics.Debug;

/**
 * This class globally collects
 * all running observations and tracks the created events.<br />
 */
public class Observing {

  private static String me = "Observing";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }
  
  private Observing() {
  }

  private static class ObserverEntry {

    private Region region;
    private ObserveEvent.Type type;
    private boolean isActive = true;
    private ObserverCallBack obs;

    protected ObserverEntry(Region reg, ObserverCallBack obs, ObserveEvent.Type type) {
      region = reg;
      this.obs = obs;
      this.type = type;
    }
  }

  private static final Map<String, ObserverEntry> observers = Collections.synchronizedMap(new HashMap<String, ObserverEntry>());
  private static final Map<String, ObserveEvent> events = Collections.synchronizedMap(new HashMap<String, ObserveEvent>());
  private static final List<Region> runningObservers = Collections.synchronizedList(new ArrayList<Region>());

  protected static void addRunningObserver(Region r) {
    runningObservers.add(r);
    log(lvl,"add observer: now running %d observer(s)", runningObservers.size());
  }

  protected static void removeRunningObserver(Region r) {
    runningObservers.remove(r);
    log(lvl, "remove observer: now running %d observer(s)", runningObservers.size());
  }
  
  protected static void stopRunningObservers() {
    if (runningObservers.size() > 0) {
      log(lvl, "stopping %d running observer(s)", runningObservers.size());
      synchronized (runningObservers) {
        for (Region r : runningObservers) {
          r.stopObserver();
        }
        runningObservers.clear();
      }
    }
    Observing.clear();
  }

  /**
   * INTERNAL USE: adds an observer to the list
   *
   * @param reg the observed region
   * @param obs the callback (might be null - observer without call back)
   * @param type one off ObserveEvent.Type.APPEAR, VANISH, CHANGE, GENERIC
   * @param target 
   * @return a unique name derived from time or null if not possible
   */
  public static String add(Region reg, ObserverCallBack obs, ObserveEvent.Type type, Object target) {
    String name = null;
    long now = new Date().getTime();
    while (true) {
      name = "" + now++;
      if (!hasName(name)) {
        break;
      }
    }
    observers.put("" + now, new ObserverEntry(reg, obs, type));
    reg.getObserver().addObserver(target, (ObserverCallBack) obs, name, type);
    return name;
  }

  private static boolean hasName(String name) {
    return observers.containsKey(name);
  }

  /**
   * remove the observer from the list, a region observer will be stopped <br>
   * events for that observer are removed as well
   *
   * @param name name of observer
   * @return success
   */
  public static void remove(String name) {
    if (observers.containsKey(name)) {
      observers.get(name).region.stopObserver();
      observers.remove(name);
      events.remove(name);
    }
  }

  /**
   * stop and remove all observers registered for this region from the list <br>
   * events for those observers are removed as well
   * @param reg
   */
  public static void remove(Region reg) {
    for (String name : reg.getObserver().getNames()) {
      remove(name);
    }
  }

  /**
   * stop and remove all observers and their registered events
   *
   */
  public static void clear() {
    synchronized (observers) {
      for (String name : observers.keySet()) {
        remove(name);
      }
    }
    log(lvl, "as requested: removed all observers");
  }

  /**
   * are their any events registered
   *
   * @return true if yes
   */
  public static boolean hasEvents() {
    return events.size() > 0;
  }

  /**
   * are their any events registered for this region?
   *
   * @return true if yes
   */
  public static boolean hasEvents(Region reg) {
    for (String name : reg.getObserver().getNames()) {
      if (events.containsKey(name)) {
        return true;
      }
    }
     return false;
  }

  /**
   * are their any events registered for the observer having this name?
   *
   * @return true if yes
   */
  public static boolean hasEvent(String name) {
    return events.containsKey(name);
  }

  /**
   * add a new event to the list
   *
   * @param name name of event
   */
  public static void addEvent(ObserveEvent evt) {
    events.put(evt.getName(), evt);
  }

  /**
   * return the events for that region <br>
   * events are removed from the list
   *
   * @return the array of events or size 0 array if none
   */
  public static ObserveEvent[] getEvents(Region reg) {
    List<ObserveEvent> evts = new ArrayList<ObserveEvent>();
    ObserveEvent evt;
    for (String name : reg.getObserver().getNames()) {
      evt = events.get(name);
      if (evt != null) evts.add(evt);
      events.remove(name);
    }
    return evts.toArray(new ObserveEvent[0]);
  }

  /**
   * return the all events (they are preserved) <br>
   *
   * @return the array of events or size 0 array if none
   */
  public static ObserveEvent[] getEvents() {
    List<ObserveEvent> evts = new ArrayList<ObserveEvent>();
    ObserveEvent evt; 
    synchronized (events) {      
      for (String name : events.keySet()) {
        evt = events.get(name);
        if (evt == null) {
          evts.add(evt);
        }
      }
    } 
    return evts.toArray(new ObserveEvent[0]);
  }
  
  public static void clearEvents() {
    events.clear();
  }
}
