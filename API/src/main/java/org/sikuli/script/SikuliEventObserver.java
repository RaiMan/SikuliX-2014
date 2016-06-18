/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
package org.sikuli.script;

import java.util.*;

/**
 * see @{link SikuliEventAdapter}
 * @deprecated
 */
@Deprecated
public interface SikuliEventObserver extends EventListener {
   public void targetAppeared(ObserveEvent e);
   public void targetVanished(ObserveEvent e);
   public void targetChanged(ObserveEvent e);
}
