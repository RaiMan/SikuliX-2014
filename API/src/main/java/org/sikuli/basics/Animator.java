/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
package org.sikuli.basics;

/**
 * INTERNAL USE
 * allows to implement timed animations (e.g. mouse move)
 */
public interface Animator {

  public float step();

  public boolean running();
}
