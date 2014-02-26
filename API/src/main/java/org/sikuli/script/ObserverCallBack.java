/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.util.EventListener;

/**
 *
 * Use this class to implement call back methods for the Region observers <br>
 * onAppear, onVanish and onChange <br>
 * by overriding the contained empty methods appeared, vanished and changed
 *
 * example:<br>
 * aRegion.onAppear(anImage, <br>
   new ObservingCallBack() { <br>
 *     <br>
     appeared(ObserveEvent e) { <br>
       // do something
     }
   }
 );
 when the image appears, your above call back appeared() will be called
 see ObserveEvent about the features available in the callback function
 */
public class ObserverCallBack implements EventListener {

  public void appeared(ObserveEvent e) {
  }

  public void vanished(ObserveEvent e) {
  }

  public void changed(ObserveEvent e) {
  }

  public void happened(Observing.Event e) {
  }
}
