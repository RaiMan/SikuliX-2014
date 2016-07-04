/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.guide;

public interface Transition {

   public interface TransitionListener {
      void transitionOccurred(Object source);
   }

   String waitForTransition(TransitionListener token);

}
