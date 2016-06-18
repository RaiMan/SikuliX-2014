/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.util.EventListener;

public interface SikuliActionListener extends EventListener{
   public void targetClicked(SikuliAction action);
   public void targetDoubleClicked(SikuliAction action);
   public void targetRightClicked(SikuliAction action);
}
