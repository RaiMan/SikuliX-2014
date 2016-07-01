/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.basics;

import java.util.Date;

public class AnimatorTimeBased implements Animator {

  protected long _begin_time;
  protected float _beginVal, _endVal, _stepUnit;
  protected long _totalMS;
  protected boolean _running;
  protected AnimatorTimeValueFunction _func;

  public AnimatorTimeBased(AnimatorTimeValueFunction func) {
    _begin_time = -1;
    _running = true;
    _func = func;
  }

  @Override
  public float step() {
    if (_begin_time == -1) {
      _begin_time = (new Date()).getTime();
      return _func.getValue(0);
    }

    long now = (new Date()).getTime();
    long delta = now - _begin_time;
    float ret = _func.getValue(delta);
    _running = !_func.isEnd(delta);
    return ret;
  }

  @Override
  public boolean running() {
    return _running;
  }
}
