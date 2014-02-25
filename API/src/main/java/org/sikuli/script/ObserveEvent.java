/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.util.ArrayList;
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
  private Match match = null;
  private List<Match> changes = null;
  private Object pattern = null;

  public ObserveEvent() {
  }

  /**
   * INTERNAL USE ONLY: creates an observed event
   */
  public ObserveEvent(Object ptn, Match m, Region r) {
		init(ptn, m, r);
  }

	private void init(Object ptn, Match m, Region r) {
    setRegion(r);
    setMatch(m);
    setPattern(ptn);
	}

  /**
   *
   * @return this event's observer's region
   */
  public Region getRegion() {
    return region;
  }

  protected void setRegion(Region r) {
    region = new Region(r);
  }

  /**
   *
   * @return the observed match (APEAR, VANISH)
   */
  public Match getMatch() {
    return match;
  }

  protected void setMatch(Match m) {
    match = new Match(m);
  }

  /**
   *
   * @return a list of observed changes as matches (CHANGE)
   */
  public List<Match> getChanges() {
    return changes;
  }

  protected void setChanges(List<Match> c) {
		changes = new ArrayList<Match>();
    changes.addAll(c);
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

  protected void setPattern(Object p) {
    if (p.getClass().isInstance("")) {
			pattern = new Pattern((String) p);
    } else {
			pattern = new Pattern((Pattern) p);
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
