/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.util.List;

public class SikuliEventChange extends SikuliEvent {
   public SikuliEventChange(List<Match> results, Region r){
      type = Type.CHANGE;
      changes = results;
      region = r;
   }

	@Override
   public String toString(){
      return String.format("ChangeEvent on %s | %d changes",
               region, changes.size());
   }
}
