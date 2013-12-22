/*
 * DEPRECATED: for backward comatibility only
*/
package org.sikuli.basics.proxies;

import org.sikuli.basics.Settings;

public class Vision {
  @Deprecated
  private static void setParameter(String param, float val) {
    Settings.setVisionParameter(param, val);
  }

  @Deprecated  
  public static float getParameter(String param) {
    return Settings.getVisionParameter(param);
  }
}
