/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.util.*;


/**
 * CANDIDATE FOR DEPRECATION - ONLY HERE TO BE BACKWARD COMPATIBLE
 */
public interface SikuliEventObserver extends EventListener {
   public void targetAppeared(ObserveAppear e);
   public void targetVanished(ObserveVanish e);
   public void targetChanged(ObserveChange e);
}
