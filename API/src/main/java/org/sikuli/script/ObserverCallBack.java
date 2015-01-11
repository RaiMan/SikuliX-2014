/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.lang.reflect.Method;
import java.util.EventListener;
import org.sikuli.basics.Debug;

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
  
  Object callback = null;
  ObserveEvent.Type obsType = ObserveEvent.Type.GENERIC;
  Object scriptRunner = null;
  Method doSomethingSpecial = null;
  
  public ObserverCallBack(Object callback, ObserveEvent.Type obsType) {
    this.callback = callback;
    this.obsType = obsType;
    try {
      if (callback.getClass().getTypeName().contains("org.python")) {
        Class Scripting = Class.forName("org.sikuli.scriptrunner.ScriptRunner");
        Method getRunner = Scripting.getMethod("getRunner",
                new Class[]{String.class, String.class});
        scriptRunner = getRunner.invoke(Scripting, new Object[]{null, "jython"});
        if (scriptRunner != null) {
          doSomethingSpecial = scriptRunner.getClass().getMethod("doSomethingSpecial",
                  new Class[]{String.class, Object[].class});
        }
      } else {
        Debug.log(-1, "ObserverCallBack: no valid callback: %s", callback);
      }
    } catch (Exception ex) {
      Debug.log(-1, "ObserverCallBack: Jython init: we have problems\n%s", ex.getMessage());
      scriptRunner = null;
    }
  }

  public void appeared(ObserveEvent e) {
    if (scriptRunner != null && ObserveEvent.Type.APPEAR.equals(obsType)) {
      run(e);
    }
  }

  public void vanished(ObserveEvent e) {
    if (scriptRunner != null && ObserveEvent.Type.VANISH.equals(obsType)) {
      run(e);
    }
  }

  public void changed(ObserveEvent e) {
    if (scriptRunner != null && ObserveEvent.Type.CHANGE.equals(obsType)) {
      run(e);
    }
  }

  public void happened(ObserveEvent e) {
    if (scriptRunner != null && ObserveEvent.Type.GENERIC.equals(obsType)) {
      run(e);
    }
  }
  
  private void run(ObserveEvent e) {
    try {
      if (scriptRunner != null) {
        Object[] args = new Object[] {callback, e};
        doSomethingSpecial.invoke(scriptRunner, new Object[]{"runObserveCallback", args});
      }
    } catch (Exception ex) {
      Debug.log(-1, "ObserverCallBack: problem with Jython handler: %s\n%s", callback, ex.getMessage());
    }
  }

  public ObserverCallBack() {
  }
}
