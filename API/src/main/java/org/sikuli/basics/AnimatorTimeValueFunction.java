/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.basics;

public abstract class AnimatorTimeValueFunction {

  protected float _beginVal, _endVal;
  protected long _totalTime;

  public AnimatorTimeValueFunction(float beginVal, float endVal, long totalTime) {
    _beginVal = beginVal;
    _endVal = endVal;
    _totalTime = totalTime;
  }

  public boolean isEnd(long t) {
    return t >= _totalTime;
  }

  abstract public float getValue(long t);
}
