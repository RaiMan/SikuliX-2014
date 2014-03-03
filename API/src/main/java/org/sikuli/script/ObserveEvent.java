/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ObserveEvent {

  public enum Type {
    APPEAR, VANISH, CHANGE, GENERIC
  }

  /**
   * the event's type as ObserveEvent.APPEAR, .VANISH, .CHANGE
   */
  public Type type;

  private Region region = null;
  private Object pattern = null;
  private Match match = null;
  private int index = -1;
  private List<Match> changes = null;
  private long time;
  private String name;

  public ObserveEvent() {
  }
  
  /**
   * INTERNAL USE ONLY: creates an observed event
   */
  public ObserveEvent(String name, Object ptn, Match m, Region r) {
    init(name, ptn, m, r);
  }

	private void init(String name, Object ptn, Match m, Region r) {
    this.name = name;
    setRegion(r);
    setMatch(m);
    setPattern(ptn);
    time = new Date().getTime();
	}
  
  /**
   *
   * @return the observer name of this event
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return this event's observer's region
   */
  public Region getRegion() {
    return region;
  }

  protected void setRegion(Region r) {
    region = r;
  }

  /**
   *
   * @return the observed match (APEAR, VANISH)
   */
  public Match getMatch() {
    return match;
  }

  protected void setMatch(Match m) {
    if (null != m) {
      match = new Match(m);
    }
  }

  /**
   *
   * @return the index in the observer map in Observer (CHANGE)
   */
  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  /**
   *
   * @return a list of observed changes as matches (CHANGE)
   */
  public List<Match> getChanges() {
    return changes;
  }

  protected void setChanges(List<Match> c) {
    if (c != null) {
      changes = new ArrayList<Match>();
      changes.addAll(c);
    }
  }

  /**
   *
   * @return the used pattern for this event's observing
   */
  public Pattern getPattern() {
    if (null != pattern) {
      if (pattern.getClass().isInstance("")) {
        return (new Pattern((String) pattern));
      } else {
        return (new Pattern((Pattern) pattern));
      }
    }
    return null;
  }

  protected void setPattern(Object p) {
    if (null != p) {
      if (p.getClass().isInstance("")) {
        pattern = new Pattern((String) p);
      } else {
        pattern = new Pattern((Pattern) p);
      }
    }
  }

  /**
   * tell the observer to repeat this event's observe action immediately
   * after returning from this handler (APPEAR, VANISH)
   */
  public void repeat() {
    repeat(0);
  }

  /**
   * tell the observer to repeat this event's observe action after given time in secs
   * after returning from this handler (APPEAR, VANISH)
   * @param secs
   */
  public void repeat(long secs) {
    region.getObserver().repeat(type, name, match, secs);
  }

  /**
   * @return the number how often this event has already been triggered until now
   */
  public int getCount() {
    if (region.getObserver() == null) {
      return 1;
    }
    if (type == Type.CHANGE) {
      return region.getObserver().getChangedCount(name);
    } else {
      return region.getObserver().getCount(name);
    }
  }

  /**
   * stops the observer after returning from the handler
   */
  public void stopObserver() {
    region.stopObserver();
  }

  public void stopObserver(String msg) {
    region.stopObserver(msg);
  }

  @Override
  public String toString() {
    if (type == Type.CHANGE) {
      return String.format("Event(%s) %s on: %s with: %d count: %d", 
            type, name, region, index, getCount());
    } else {
      return String.format("Event(%s) %s on: %s with: %s match: %s count: %d",
            type, name, region, pattern, match, getCount());
    }
  }
}
