/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.EventListener;

import org.sikuli.basics.Debug;
import org.sikuli.util.JLangHelperInterface;
import org.sikuli.util.JRubyHelper;
import org.sikuli.util.JythonHelper;


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
  JLangHelperInterface scriptHelper = null;
  String scriptRunnerType = null;
  Method doSomethingSpecial = null;

    public ObserverCallBack(Object callback, ObserveEvent.Type obsType) {
        this.callback = callback;
        this.obsType = obsType;
        if (callback.getClass().getName().contains("org.python")) {
            scriptRunnerType = "jython";
            scriptHelper = JythonHelper.get();
        } else if (callback.getClass().getName().contains("org.jruby")) {
            scriptRunnerType = "jruby";
            scriptHelper = JRubyHelper.get();
        } else {
            Debug.error("ObserverCallBack: %s init: ScriptRunner not available for class %s", obsType,
                    callback.getClass().getName());
        }
    }

  public void appeared(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.APPEAR.equals(obsType)) {
      run(e);
    }
  }

  public void vanished(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.VANISH.equals(obsType)) {
      run(e);
    }
  }

  public void changed(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.CHANGE.equals(obsType)) {
      run(e);
    }
  }

  public void happened(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.GENERIC.equals(obsType)) {
      run(e);
    }
  }

    private void run(ObserveEvent e) {
        boolean success = true;
        Object[] args = new Object[] { callback, e };
        if (scriptHelper != null) {
            success = scriptHelper.runObserveCallback(args);
            if (!success) {
                Debug.error("ObserverCallBack: problem with scripting handler: %s\n%s",
                        scriptHelper.getClass().getName(),
                        callback.getClass().getName());
            }
        }
    }

  public ObserverCallBack() {
  }
}
