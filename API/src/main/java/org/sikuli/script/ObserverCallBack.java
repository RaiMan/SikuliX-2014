/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

/**
 *
 * Use this class to implement call back methods for the Region observers <br>
 * onAppear, onVanish and onChange <br>
 * by overriding the contained empty methods appeared, vanished and changed
 * 
 * example:<br>
 * aRegion.onAppear(anImage, <br>
 *   new ObserverCallBack() { <br>
 *     <br>
 *     appeared(SikuliEvent e) { <br>
 *       // do something
 *     }
 *   }
 * );
 * when the image appears, your above call back appeared() will be called
 */
public class ObserverCallBack implements SikuliEventObserver {

  @Override
  public void targetAppeared(SikuliEventAppear e) {
    appeared(e);
  }

  @Override
  public void targetVanished(SikuliEventVanish e) {
    vanished(e);
  }

  @Override
  public void targetChanged(SikuliEventChange e) {
    changed(e);
  }

  public void appeared(SikuliEvent e) {
  }

  public void vanished(SikuliEvent e) {
  }

  public void changed(SikuliEvent e) {
  }
  
  public void happened(Observer.Event e) {
    
  }
}
