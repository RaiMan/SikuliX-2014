/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.util;

/**
 * INTERNAL USE
 */
public interface EventSubject {

  public void addObserver(EventObserver o);

  public void notifyObserver();
}
