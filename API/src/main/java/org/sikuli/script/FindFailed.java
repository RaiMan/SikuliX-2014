/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

public class FindFailed extends SikuliException {

  public static FindFailedResponse defaultFindFailedResponse = FindFailedResponse.ABORT;
  public static final FindFailedResponse PROMPT = FindFailedResponse.PROMPT;
  public static final FindFailedResponse RETRY = FindFailedResponse.RETRY;
  public static final FindFailedResponse SKIP = FindFailedResponse.SKIP;
  public static final FindFailedResponse ABORT = FindFailedResponse.ABORT;

  public FindFailed(String msg) {
    super(msg);
    _name = "FindFailed";
  }
}
