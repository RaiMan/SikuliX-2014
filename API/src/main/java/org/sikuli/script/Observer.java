package org.sikuli.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Observer {
  
  private static class Entry {
    
    private Region region;
    private String name;
    private SikuliEvent.Type type;
    private boolean isActive = true;
    private ObserverCallBack obs;
    
    protected Entry(String name, Region reg, ObserverCallBack obs, SikuliEvent.Type type) {
      this.name = name;
      region = reg;
      this.obs = obs;
      this.type = type;
    }
  }
  
  public static class Event extends SikuliEvent {
    private Entry observer;
    private long time;
    
    public long getTime() {
      return time;
    }
  }
  
  private static List<Entry> observers = Collections.synchronizedList(new ArrayList<Entry>());
  private static List<Event> events = Collections.synchronizedList(new ArrayList<Event>());
  
  /**
   * adds an Observer with a callback to the list
   * 
   * @param reg the observed region
   * @param obs the callback
   * @param type one off Observer.Type.APPEAR, VANISH, CHANGE, GENERIC
   * @return a unique name derived from time or null if not possible
   */
  public static synchronized String add(Region reg, ObserverCallBack obs, SikuliEvent.Type type) {
    String name = createName();
    if (add(name, reg, obs, type)) return name;
    return null;
  }
  
  /**
   * adds an observer to the list having no callback 
   * 
   * @param reg the observed region
   * @param name a unique name
   * @param type one off Observer.Type.APPEAR, VANISH, CHANGE, GENERIC
   * @return the observers name or null if not possible (duplicate?)
   */
  public static synchronized String add(Region reg, String name, SikuliEvent.Type type) {
    if (add(name, reg, null, type)) {
      return name;
    }
    return null;
  }
  
  /**
   * adds an observer of type GNERIC to the list having no callback 
   * 
   * @param name a unique name
   * @return the observers name or null if not possible (duplicate?)
   */
  public static synchronized String add(String name) {
    if (add(name, null, null, SikuliEvent.Type.GENERIC)) {
      return name;
    }
    return null;
  }
  
  private static boolean add(String name, Region reg, ObserverCallBack obs, SikuliEvent.Type type) {
    if (hasName(name, reg)) {
      return false;
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
  
  private static void remove(Entry obs) {
    if (obs.region != null) {
      obs.region.stopObserver();
    }
    observers.remove(obs);
    for (Event ev:events) {
      if (ev.observer == obs) {
        events.remove(ev);
      }
    }
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
    for (Entry e : observers) {
      remove(e);
    }    
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
   * @param pev the event object (SikuliEvent is copied)
   * @return
   */
  public static synchronized long addEvent(String name, Object pev) {
    if (pev instanceof SikuliEvent) {
      Event evt = new Event();
      SikuliEvent event = (SikuliEvent) pev;
      evt.type = event.type;
      evt.changes = event.changes;
      evt.match = event.match;
      evt.pattern = event.pattern;
      evt.region = event.region;
      pev = evt;
    }
    Event ev = (Event) pev;
    ev.observer = get(null, name);
    ev.time = new Date().getTime();
    if (events.add(ev)) {
      return ev.time;
    }
    return 0;
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
        if (ev.observer == obs) {
          if (event == null) {
            event = ev;
            continue;
          }
          if (ev.time > event.time) {
            events.remove(event);
            event = ev;
          }
        }
      }
    }
    if (null != event && remove) {
      events.remove(event);
    }
    return event;
  }
  
  /**
   * remove and return the latest events for that region <br>
   * earlier events are removed
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
      evts.add(getEvent(obs.name, false));
    }
    return evts.toArray(new Event[0]);
  }
}
