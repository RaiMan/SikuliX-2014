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
import java.util.Iterator;
import java.util.List;
import org.sikuli.basics.Debug;

/**
 * This class implements a container that globally collects
 * all running observations.<br />
 */
public class Observing {

  private static String me = "Observing";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }
  
  private static class Entry {

    private Region region;
    private String name;
    private ObserveEvent.Type type;
    private boolean isActive = true;
    private ObserverCallBack obs;

    protected Entry(String name, Region reg, ObserverCallBack obs, ObserveEvent.Type type) {
      this.name = name;
      region = reg;
      this.obs = obs;
      this.type = type;
    }
    
    protected void destroy() {
      region = null;
      name = null;
      type = null;
      obs = null;
    }
  }

  public static class Event extends ObserveEvent {
    private Entry observer = null;
    private long time = 0;
    
    protected Event() {
    }
    
    protected Event(Event evt) {
      observer = evt.observer;
      time = evt.time;
      setRegion(evt.getRegion());
      setMatch(evt.getMatch());
      setChanges(evt.getChanges());
      setPattern(evt.getPattern());
      setIndex(evt.getIndex());
    }

    public long getTime() {
      return time;
    }
    
    protected void destroy() {
      observer = null;
      time = 0;
    }
  }

  private static List<Entry> observers = Collections.synchronizedList(new ArrayList<Entry>());
  private static List<Event> events = Collections.synchronizedList(new ArrayList<Event>());

  /**
   * adds an observer with a callback to the list
   *
   * @param reg the observed region
   * @param obs the callback
   * @param type one off ObserveEvent.Type.APPEAR, VANISH, CHANGE, GENERIC
   * @return a unique name derived from time or null if not possible
   */
  public static synchronized String add(Region reg, ObserverCallBack obs, ObserveEvent.Type type) {
    String name = createName();
    if (add(name, reg, obs, type)) {
      return name;
    }
    return null;
  }

  /**
   * adds an observer to the list having no callback
   *
   * @param reg the observed region
   * @param name a unique name
   * @param type one off Observing.Type.APPEAR, VANISH, CHANGE, GENERIC
   * @return the observers name or null if not possible (duplicate?)
   */
  public static synchronized String add(Region reg, String name, ObserveEvent.Type type) {
    if (add(name, reg, null, type)) {
      return name;
    }
    return null;
  }

  /**
   * adds an observer of type GENERIC to the list having no callback
   *
   * @param name a unique name
   * @return the observers name or null if not possible (duplicate?)
   */
  public static synchronized String add(String name) {
    if (add(name, null, null, ObserveEvent.Type.GENERIC)) {
      return name;
    }
    return null;
  }

  private static boolean add(String name, Region reg, ObserverCallBack obs, ObserveEvent.Type type) {
    if (hasName(name, reg)) {
      return false;
    }
    Iterator<Entry> iter = observers.iterator();
    while (iter.hasNext()) {
      if (iter.next() == null) {
        iter.remove();
      }
    }
    return observers.add(new Entry(name, reg, obs, type));
  }

  private static String createName() {
    String name = null;
    while (null == name) {
      name = "" + new Date().getTime();
      if (!hasName(name, null)) {
        return name;
      } else {
        name = null;
      }
      try {
        Thread.sleep(5);
      } catch(Exception ex) {}
    }
    return null;
  }

  private static boolean hasName(String name, Region reg) {
    for (Entry obs : observers) {
      if (obs.name == null) {
        continue;
      }
      if (name.equals(obs.name)) {
        if (reg != null && reg == obs.region) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * remove the observer from the list, a region observer will be stopped <br>
   * registered events for that observer are removed as well
   *
   * @param name name of observer
   * @return success
   */
  public static boolean remove(String name) {
    return remove(null, name);
  }

  /**
   * stop and remove all observers registered for this region from the list <br>
   * registered events for those observers are removed as well
   *
   * @return success
   */
  public static boolean remove(Region reg) {
    return remove(reg, null);
  }

  /**
   * stop and remove the observer registered for this region from the list <br>
   * registered events for that observer are removed as well
   *
   * @param reg the observed region
   * @param name name of observer
   * @return success
   */
  public static synchronized boolean remove(Region reg, String name) {
    for (Entry obs : observers) {
      if (name != null) {
        if (name.equals(obs.name)) {
          if (reg == null || reg == obs.region) {
           remove(obs);
          }
        }
      } else if (reg != null && reg == obs.region) {
        remove(obs);
      }
    }
    return true;
  }

  private static synchronized void remove(Entry obs) {
    if (obs.region != null) {
      obs.region.stopObserver();
    }
    for (Event ev:events) {
      if (ev.observer == obs) {
        ev.destroy();
      }
    }
    obs.destroy();
  }

  private static Entry get(Region reg, String name) {
    for (Entry obs : observers) {
      if (name != null) {
        if (name.equals(obs.name)) {
          if (reg == null || reg == obs.region) {
           return obs;
          }
        }
      } else if (reg != null && reg == obs.region) {
        return obs;
      }
    }
    return null;
  }

  /**
   * stop and remove all observers and their registered events
   *
   * @return success
   */
  public static synchronized boolean clear() {
    log(lvl, "*** requested ***: remove all observers");
    for (Entry e : observers) {
      remove(e);
    }
    Iterator<Entry> itero = observers.iterator();
    while (itero.hasNext()) {
      if (itero.next() == null) {
        itero.remove();
      }
    }
    Iterator<Event> itere = events.iterator();
    while (itere.hasNext()) {
      if (itere.next() == null) {
        itere.remove();
      }
    }
    log(lvl, "as requested: removed all observers");
    return true;
  }

  /**
   * are their any events registered
   *
   * @return true if yes
   */
  public static synchronized boolean hasEvents() {
    return events.size() > 0;
  }

  /**
   * are their any events registered for this region
   *
   * @return true if yes
   */
  public static synchronized boolean hasEvents(Region reg) {
    return hasEvent(reg, null);
  }

  /**
   * are their any events registered for the observer having this name
   *
   * @return true if yes
   */
  public static synchronized boolean hasEvent(String name) {
    return hasEvent(null, name);
  }

  /**
   * are their any events registered for the region's observer having this name
   *
   * @return true if yes
   */
  public static synchronized boolean hasEvent(Region reg, String name) {
    Entry obs = get(reg, name);
    if (obs == null) {
      return false;
    }
    for (Event ev:events) {
      if (ev.observer == obs) {
        return true;
      }
    }
    return false;
  }

  /**
   * add a new event to the list
   *
   * @param name name of event
   * @param pev the event object (ObserveEvent is copied)
   * @return the time of creation
   */
  public static synchronized long addEvent(String name, Object pev) {
    long t = 0;
    if (pev instanceof ObserveEvent) {
      ObserveEvent evt = (ObserveEvent) new Event();
      ObserveEvent event = (ObserveEvent) pev;
      evt.type = event.type;
      evt.setChanges(event.getChanges());
      evt.setMatch(event.getMatch());
      evt.setPattern(event.getPattern());
      evt.setRegion(event.getRegion());
      pev = evt;
    }
    Event ev = (Event) pev;
    ev.observer = get(null, name);
    ev.time = new Date().getTime();
    if (events.add(ev)) {
      t = ev.time;
    }
    Iterator<Event> iter = events.iterator();
    while (iter.hasNext()) {
      if (iter.next() == null) {
        iter.remove();
      }
    }
    return t;
  }

  /**
   * remove and return the latest event for the named observer <br>
   * earlier events are removed
   *
   * @param name
   * @return the event or null if none registered
   */
  public static synchronized Event getEvent(String name) {
    return getEvent(name, true);
  }

  private static Event getEvent(String name, boolean remove) {
    Entry obs = get(null, name);
    Event event = null;
    if (obs != null) {
      for (Event ev:events) {
        if (ev.observer == null) {
          continue;
        }
        if (ev.observer.name.equals(obs.name)) {
          if (event == null) {
            event = ev;
            continue;
          }
          if (ev.time > event.time) {
            event.destroy();
            event = ev;
          }
        }
      }
    }
    if (null != event && remove) {
      Event ev = new Event(event);
      event.destroy();
      event = ev;
    }
    return event;
  }

  /**
   * remove and return the latest events for that region <br>
   * earlier events are removed as well
   *
   * @return the array of events or size 0 array if none
   */
  public static synchronized Event[] getEvents(Region reg) {
    List<Event> evts = new ArrayList<Event>();
    for (Entry obs:observers) {
      if (reg == obs.region) evts.add(getEvent(obs.name));
    }
    return evts.toArray(new Event[0]);
  }

  /**
   * return the latest events (these are preserved) <br>
   * earlier events for the same observer are removed
   *
   * @return the array of events or size 0 array if none
   */
  public static synchronized Event[] getEvents() {
    List<Event> evts = new ArrayList<Event>();
    for (Entry obs:observers) {
      if (obs.name != null) {
        evts.add(getEvent(obs.name, false));
      }
    }
    return evts.toArray(new Event[0]);
  }
}
