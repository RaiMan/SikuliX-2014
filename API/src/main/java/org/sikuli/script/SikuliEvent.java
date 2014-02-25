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
  
  /**
   * the event's type as SikuliEvent.APPEAR, .VANISH, .CHANGE
   */
  public Type type;
  
  private Region region = null;
  private Match match = null;
  private List<Match> changes = null;
  private Object pattern = null;

  public SikuliEvent() {
  }

  /**
   * INTERNAL USE ONLY: creates an observed event
   */
  public SikuliEvent(Object ptn, Match m, Region r) {
    region = r;
    match = m;
    pattern = ptn;
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
    match = m;
  }

  /**
   *
   * @return a list of observed changes as matches (CHANGE) 
   */
  public List<Match> getChanges() {
    return changes;
  }

  protected void setChanges(List<Match> c) {
    changes = c;
  }

  /**
   * 
   * @return the used pattern for this event's observing
   */
  public Pattern getPattern() {
    if (pattern.getClass().isInstance("")) {
      return (new Pattern((String) pattern));
    } else {
      return (new Pattern((Pattern) pattern));
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
    region.getEvtMgr().repeat(type, pattern, match, secs); 
  }
  
  /**
   * only for (APPEAR, VANISH)
   * @return the number how often this event has already been triggered until now
   */
  public int getCount() {
    if (type == Type.CHANGE) {
      return 0;
    } else {
      return region.getEvtMgr().getCount(pattern);
    } 
  }
  
  /**
   * stops the observer after returning from the handler
   */
  public void stopObserver() {
    region.stopObserver();
  }
  
  @Override
  public String toString() {
    return String.format("SikuliEvent(%s) on %s with %s having last match: %s",
            type, region, pattern, match);
  }
}
