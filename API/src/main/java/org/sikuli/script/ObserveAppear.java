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
public class ObserveAppear extends ObserveEvent {

   public ObserveAppear(Object ptn, Match m, Region r){
      super(ptn, m, r);
      type = Type.APPEAR;
   }

}
