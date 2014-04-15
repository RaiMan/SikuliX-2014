/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.util.EventListener;

/**
 * Use this class to implement call back methods for the Region observers
 * onAppear, onVanish and onChange. <br>
 * by overriding the contained empty methods appeared, vanished and changed
 * <pre>
 * example:
 * aRegion.onAppear(anImage,
 *   new ObserverCallBack() {
 *     appeared(ObserveEvent e) {
 *       // do something
 *     }
 *   }
 * );
 * </pre>
 when the image appears, your above call back appeared() will be called
 see {@link ObserveEvent} about the features available in the callback function
 */
public class ObserverCallBack implements EventListener {

  public void appeared(ObserveEvent e) {
  }

  public void vanished(ObserveEvent e) {
  }

  public void changed(ObserveEvent e) {
  }

  public void happened(ObserveEvent e) {
  }
}
