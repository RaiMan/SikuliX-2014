/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.basics;

public class AnimatorLinear extends AnimatorTimeBased {

  public AnimatorLinear(float beginVal, float endVal, long totalMS) {
    super(new AnimatorLinearInterpolation(beginVal, endVal, totalMS));
  }
}
