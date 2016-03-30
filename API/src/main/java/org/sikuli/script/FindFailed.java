/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;

/**
 * implements the SikuliX FindFailed exception class
 * and defines constants and settings for the feature FindFailedResponse
 */
public class FindFailed extends SikuliException {

	/**
	 * default FindFailedResponse is ABORT
	 */
	public static FindFailedResponse defaultFindFailedResponse = FindFailedResponse.ABORT;

	/**
	 * FindFailedResponse PROMPT: should display a prompt dialog with the failing image
	 * having the options retry, skip and abort
	 */
	public static final FindFailedResponse PROMPT = FindFailedResponse.PROMPT;

	/**
	 * FindFailedResponse RETRY: should retry the find op on FindFailed
	 */
	public static final FindFailedResponse RETRY = FindFailedResponse.RETRY;

	/**
	 * FindFailedResponse SKIP: should silently continue on FindFailed
	 */
	public static final FindFailedResponse SKIP = FindFailedResponse.SKIP;

	/**
	 * FindFailedResponse ABORT: should abort the SikuliX application
	 */
	public static final FindFailedResponse ABORT = FindFailedResponse.ABORT;

	/**
	 * FindFailedResponse HANDLE: should call a given handler on FindFailed
	 */
	public static final FindFailedResponse HANDLE = FindFailedResponse.HANDLE;
  
  public static Object handler = null;
  private static Object defaultHandler = null;

  /**
	 * the exception
	 * @param message to be shown
	 */
	public FindFailed(String message) {
    super(message);
    _name = "FindFailed";
  }
  
  public static FindFailed createdefault(Region reg, Image img) {
    String msg = String.format("FindFailed: %s in %s", img, reg);
    return new FindFailed(msg);
  }

  public static FindFailedResponse getResponse() {
    return defaultFindFailedResponse;
  }

  public static FindFailedResponse setResponse(FindFailedResponse response) {
    defaultFindFailedResponse = response;
    return defaultFindFailedResponse;
  }
  
  public static FindFailedResponse setHandler(Object observer) {
    defaultFindFailedResponse = HANDLE;
    if (observer != null && (observer.getClass().getName().contains("org.python")
            || observer.getClass().getName().contains("org.jruby"))) {
      observer = new ObserverCallBack(observer, ObserveEvent.Type.FINDFAILED);
    }
    handler = observer;
    Debug.log(3, "Setting Default FindFailedHandler");
    return defaultFindFailedResponse;
  }
  
  public static Object getHandler() {
    return handler;
  }
  
  public static FindFailedResponse reset() {
    defaultFindFailedResponse = ABORT;
    handler = defaultHandler;
    return defaultFindFailedResponse;
  }
}
