/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

/**
 * INTERNAL USE
 */
public class ObserveVanish extends ObserveEvent {
   public ObserveVanish(Object ptn, Match m, Region r){
      super(ptn, m, r);
      type = Type.VANISH;
   }
}

