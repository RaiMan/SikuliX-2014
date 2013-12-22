/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

public class SikuliEventAppear extends SikuliEvent {

   public SikuliEventAppear(Object ptn, Match m, Region r){
      super(ptn, m, r);
      type = Type.APPEAR;
   }

}
