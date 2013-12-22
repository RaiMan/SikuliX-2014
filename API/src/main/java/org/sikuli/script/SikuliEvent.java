/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.util.List;

public class SikuliEvent {

  public enum Type {
    APPEAR, VANISH, CHANGE, GENERIC
  }
  
  public Type type;
  public Region region = null;
  // AppearEvent must have a match
  // VanishEvent may have a match, depending on if the pattern appeared before
  public Match match = null;
  // ChangeEvent has 0+ changes.
  public List<Match> changes = null;
  // the pattern for observing this event
  public Object pattern = null;

  public SikuliEvent() {
  }

  public SikuliEvent(Object ptn, Match m, Region r) {
    region = r;
    match = m;
    pattern = ptn;
  }

  public Region getRegion() {
    return region;
  }

  public Match getMatch() {
    return match;
  }

  public Pattern getPattern() {
    if (pattern.getClass().isInstance("")) {
      return (new Pattern((String) pattern));
    } else {
      return (new Pattern((Pattern) pattern));
    }
  }

  public void repeat() {
    repeat(0);
  }
  
  public void repeat(long secs) {
    region.getEvtMgr().repeat(type, pattern, match, secs); 
  }
  
  public int getCount() {
    if (type == Type.CHANGE) {
      return 0;
    } else {
      return region.getEvtMgr().getCount(pattern);
    } 
  }
  
  public void stopObserver() {
    region.stopObserver();
  }
  
  @Override
  public String toString() {
    return String.format("SikuliEvent(%s) on %s with %s having last match: %s",
            type, region, pattern, match);
  }
}
