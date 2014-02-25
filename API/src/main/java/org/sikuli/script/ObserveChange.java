/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.util.List;

/**
 * INTERNAL USE
 */
public class ObserveChange extends ObserveEvent {
   public ObserveChange(List<Match> results, Region r){
      type = Type.CHANGE;
      setChanges(results);
      setRegion(r);
   }

	@Override
   public String toString(){
      return String.format("ChangeEvent on %s | %d changes",
               getRegion(), getChanges().size());
   }
}
