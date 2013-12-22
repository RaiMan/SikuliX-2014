/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.util.*;


public interface SikuliEventObserver extends EventListener {
   public void targetAppeared(SikuliEventAppear e);
   public void targetVanished(SikuliEventVanish e);
   public void targetChanged(SikuliEventChange e);
}
