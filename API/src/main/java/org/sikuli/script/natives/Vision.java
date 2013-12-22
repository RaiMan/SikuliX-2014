
/*
 * DEPRECATED: for backward comatibility only
*/
package org.sikuli.script.natives;

import org.sikuli.basics.Settings;

public class Vision {
  @Deprecated
  public static void setParameter(String param, float val) {
    Settings.setVisionParameter(param, val);
  }

  @Deprecated  
  public static float getParameter(String param) {
    return Settings.getVisionParameter(param);
  }
}
