/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

public interface EventSubject {

  public void addObserver(EventObserver o);

  public void notifyObserver();
}
