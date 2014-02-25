/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

/**
 *
 * @deprecated use ObserverCallBack instead
 */
@Deprecated
public class SikuliEventAdapter implements SikuliEventObserver {

  @Override
  public void targetAppeared(ObserveAppear e) {
    appeared(e);
  }

  @Override
  public void targetVanished(ObserveVanish e) {
    vanished(e);
  }

  @Override
  public void targetChanged(ObserveChange e) {
    changed(e);
  }

  public void appeared(ObserveEvent e) {
  }

  public void vanished(ObserveEvent e) {
  }

  public void changed(ObserveEvent e) {
  }
}
