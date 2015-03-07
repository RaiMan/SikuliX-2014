package org.sikuli.script;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import org.sikuli.basics.Debug;

public class Commands {

  private static int lvl = 2;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "Commands: " + message, args);
  }
	private static void logCmd(String cmd, Object... args) {
		String msg = cmd + ": ";
		if (args.length == 0) {
			log(lvl, msg + "no-args");
		} else {
			for (int i = 0; i < args.length; i++) {
				msg += "%s ";
			}
			log(lvl, msg, args);
		}
	}
	int i = 0;

  private static Region scr = new Screen();
  private static Region scrSaved = null;
  
  private static RunTime runTime = RunTime.get();

  public static boolean isNashorn() {
    return runTime.isJava8();
  }

  public static Object call(String function, Object... args) {
    Method m = null;
    Object retVal = null;
    int count = 0;
    for (Object aObj : args) {
      if (aObj == null || aObj.getClass().getName().endsWith("Undefined")) {
        break;
      }
			if (aObj instanceof String && ((String) aObj).contains("undefined")) {
				break;
			}
      count++;
    }
    Object[] newArgs = new Object[count];
    for(int n = 0; n < count; n++) {
      newArgs[n] = args[n];
    }
    try {
      m = Commands.class.getMethod(function, Object[].class);
      retVal = m.invoke(null, (Object) newArgs);
    } catch (Exception ex) {
      m = null;
    }
    return retVal;
  }

//<editor-fold defaultstate="collapsed" desc="conversions">
  private static boolean isNumber(Object aObj) {
    if (aObj instanceof Integer || aObj instanceof Long || aObj instanceof Float || aObj instanceof Double) {
      return true;
    }
    return false;
  }
  
  private static int getInteger(Object aObj, int deflt) {
    Integer val = deflt;
    if (aObj instanceof Integer || aObj instanceof Long) {
      val = (Integer) aObj;
    }
    if (aObj instanceof Float) {
      val = Math.round((Float) aObj);
    }
    if (aObj instanceof Double) {
      val = (int) Math.round((Double) aObj);
    }
    return val;
  }
  
  private static int getInteger(Object aObj) {
    return getInteger(aObj, 0);
  }
  
  private static double getNumber(Object aObj, Double deflt) {
    Double val = deflt;
    if (aObj instanceof Integer) {
      val = 0.0 + (Integer) aObj;
    } else if (aObj instanceof Long) {
      val = 0.0 + (Long) aObj;
    } else if (aObj instanceof Float) {
      val = 0.0 + (Float) aObj;
    } else if (aObj instanceof Double) {
      val = (Double) aObj;
    }
    return val;
  }
  
  private static double getNumber(Object aObj) {
    return getNumber(aObj, 0.0);
  }
//</editor-fold>
  
//<editor-fold defaultstate="collapsed" desc="use/use1">
  public static Region use(Object... args) {
    logCmd("use", args);
    scrSaved = null;
    return usex(args);
  }
  
  public static Region use1(Object... args) {
    logCmd("use1", args);
    scrSaved = scr;
    return usex(args);
  }
  
  public static void restoreUsed() {
    if (scrSaved != null) {
      scr = scrSaved;
      scrSaved = null;
      log(lvl, "restored: %s", scr);
    }
  }
  
  private static Region usex(Object... args) {
    int len = args.length;
    int nScreen = -1;
    if (len == 0 || len > 1) {
      scr = new Screen();
      return scr;
    }
    nScreen = getInteger(args[0], -1);
    if (nScreen > -1) {
      scr = new Screen(nScreen);
    } else {
      Object oReg = args[0];
      if (oReg instanceof Region) {
        scr = (Region) oReg;
      }
    }
    return scr;
  }
//</editor-fold>
  
//<editor-fold defaultstate="collapsed" desc="wait/waitVanish/exists">
  public static Match wait(Object... args) throws FindFailed {
    logCmd("wait", args);
    Object[] realArgs = waitArgs(args);
    return waitx((String) realArgs[0], (Pattern) realArgs[1], (Double) realArgs[2], (Float) realArgs[3]);
  }
  
  private static Match waitx(String image, Pattern pimage, double timeout, float score) throws FindFailed {
    Object aPattern = null;
    if (image != null) {
      if (score > 0) {
        aPattern = new Pattern(image).similar(score);
      } else {
        aPattern = image;
      }
    } else if (pimage != null) {
      aPattern = pimage;
    }
    if (aPattern != null) {
      if (timeout > -1.0) {
        return scr.wait(aPattern, timeout);
      }
      return scr.wait(aPattern);
    }
    return null;
  }
  
  private static Object[] waitArgs(Object... args) {
    int len = args.length;
    String image = "";
    float score = 0.0f;
    double timeout = -1.0f;
    boolean argsOK = true;
    Object[] realArgs = new Object[] {null, null, (Double) (-1.0), (Float) 0f};
    if (len == 0 || len > 3) {
      argsOK = false;
    } else {
      Object aObj = args[0];
      if (aObj == null) {
        return realArgs;
      }
      if (isJSON(aObj)) {
        aObj = fromJSON(aObj);
      }
      if (aObj instanceof String) {
        realArgs[0] = aObj;
      } else if (aObj instanceof Pattern) {
        realArgs[1] = aObj;
        if (len > 1 && isNumber(args[1])) {
          realArgs[2] = (Double) getNumber(args[1]);
        }
      } else if (isNumber(aObj)) {
        scr.wait(getNumber(aObj));
        return null;
      } else {
        argsOK = false;
      }
    }
    if (argsOK && len > 1 && realArgs[1] == null) {
      if (len > 2 && isNumber(args[2])) {
        score = (float) getNumber(args[2]) / 100.0f;
        if (score < 0.7) {
          score = 0.7f;
        } else if (score > 0.99) {
          score = 0.99f;
        }
      }
      if (score > 0.0f) {
        realArgs[3] = (Float) score;
      }
      if (len > 1 && isNumber(args[1])) {
        realArgs[2] = (Double) getNumber(args[1]);
      }
    }
    if (!argsOK) {
      throw new UnsupportedOperationException(
              "Commands.wait: parameters: String/Pattern:image, float:timeout, int:score");
    }
    return realArgs;
  }
  
  public static boolean waitVanish(Object... args) throws FindFailed {
    logCmd("waitVanish", args);
    Object aPattern;
    Object[] realArgs = waitArgs(args);
    String image = (String) realArgs[0];
    Pattern pimage = (Pattern) realArgs[1];
    double timeout = (Double) realArgs[2];
    float score = (Float) realArgs[3];
    if (image != null) {
      if (score > 0) {
        aPattern = new Pattern(image).similar(score);
      } else {
        aPattern = image;
      }
    } else {
      aPattern = pimage;
    }
    if (timeout > -1.0) {
      return scr.waitVanish(aPattern, timeout);
    }
    return scr.waitVanish(aPattern);
  }
  
  public static Match exists(Object... args) {
    logCmd("exists", args);
    Match match = null;
    Object[] realArgs = waitArgs(args);
    if ((Double) realArgs[2] < 0.0) {
      realArgs[2] = 0.0;
    }
    try {
      match = waitx((String) realArgs[0], (Pattern) realArgs[1], (Double) realArgs[2], (Float) realArgs[3]);
    } catch (Exception ex) {
      return null;
    }
    return match;
  }
//</editor-fold>
  
//<editor-fold defaultstate="collapsed" desc="hover/click/doubleClick/rightClick">
  public static Location hover(Object... args) {
    logCmd("hover", args);
    return hoverx(args);
  }
  
  private static Location hoverx(Object... args) {
    int len = args.length;
    Match aMatch;
    if (len == 0 || args[0] == null) {
      Mouse.move(scr.checkMatch());
      return Mouse.at();
    }
    if (len < 4) {
      Object aObj = args[0];
      Location loc = null;
      if (isJSON(aObj)) {
        aObj = fromJSON(aObj);
      }
      if (aObj instanceof String || aObj instanceof Pattern) {
        try {
          aMatch = wait(args);
          Mouse.move(aMatch.getTarget());
        } catch (Exception ex) {
          Mouse.move(scr.checkMatch());
        }
        return Mouse.at();
      } else if (aObj instanceof Region) {
        loc = ((Region) aObj).getTarget();
      } else if (aObj instanceof Location) {
        loc = (Location) aObj;
      }
      if (len > 1) {
        if (isNumber(aObj) && isNumber(args[1])) {
          Mouse.move(scr.checkMatch().offset(getInteger(aObj), getInteger(args[1])));
          return Mouse.at();
        } else if (len == 3 && loc != null && isNumber(args[1]) && isNumber(args[2])) {
          Mouse.move(loc.offset(getInteger(args[1], 0), getInteger(args[2], 0)));
          return Mouse.at();
        }
      }
      if (loc != null) {
        Mouse.move(loc);
        return Mouse.at();
      }
    }
    Mouse.move(scr.checkMatch());
    return Mouse.at();
  }
  
  public static Location click(Object... args) {
    logCmd("click", args);
    Location loc = hoverx(args);
    Mouse.click(null, Button.LEFT, 0, false, null);
    return Mouse.at();
  }
  
  public static Location doubleClick(Object... args) {
    logCmd("doubleClick", args);
    Location loc = hoverx(args);
    Mouse.click(null, Button.LEFT, 0, true, null);
    return Mouse.at();
  }
  
  public static Location rightClick(Object... args) {
    logCmd("rightClick", args);
    Location loc = hoverx(args);
    Mouse.click(null, Button.RIGHT, 0, false, null);
    return Mouse.at();
  }
//</editor-fold>
  
//<editor-fold defaultstate="collapsed" desc="type/write/paste">
  public static boolean type(Object... args) {
    logCmd("type", args);
    Object[] realArgs = typeArgs(args);
    return 0 < scr.type((String) realArgs[0]);
  }
  
  public static boolean paste(Object... args) {
    logCmd("paste", args);
    Object[] realArgs = typeArgs(args);
    return 0 < scr.paste((String) realArgs[0]);
  }
  
  public static boolean write(Object... args) {
    logCmd("write", args);
    Object[] realArgs = typeArgs(args);
    return 0 < scr.write((String) realArgs[0]);
  }
  
  private static Object[] typeArgs(Object... args) {
    Object[] realArgs = new Object[] {null};
    if (! (args[0] instanceof String)) {
      throw new UnsupportedOperationException("Commands.type/paste/write: parameters: String:text");
    }
    realArgs[0] = args[0];
    return realArgs;
  }
//</editor-fold>
  
//<editor-fold defaultstate="collapsed" desc="JSON support">
  public static boolean isJSON(Object aObj) {
    if (aObj instanceof String) {
      return ((String) aObj).startsWith("[\"");
    }
    return false;
  }
  
  public static Object fromJSON(Object aObj) {
    if (!isJSON(aObj)) {
      return null;
    }
    Object newObj = null;
    String[] json = ((String) aObj).split(",");
    String last = json[json.length-1];
    if (!last.endsWith("]")) {
      return null;
    } else {
      json[json.length-1] = last.substring(0, last.length()-1);
    }
    String oType = json[0].substring(2,3);
    if (!"SRML".contains(oType)) {
      return null;
    }
    if ("S".equals(oType)) {
      aObj = new Screen(intFromJSON(json, 5));
      ((Screen) aObj).setRect(rectFromJSON(json));
    } else if ("R".equals(oType)) {
      newObj = new Region(rectFromJSON(json));
    } else if ("M".equals(oType)) {
      double score = dblFromJSON(json, 5)/100;
      newObj = new Match(new Region(rectFromJSON(json)), score);
      ((Match) newObj).setTarget(intFromJSON(json, 6), intFromJSON(json, 7));
    } else if ("L".equals(oType)) {
      newObj = new Location(locFromJSON(json));
    }
    return newObj;
  }
  
  private static Rectangle rectFromJSON(String[] json) {
    int[] vals = new int[4];
    for (int n = 1; n < 5; n++) {
      try {
        vals[n-1] = Integer.parseInt(json[n].trim());
      } catch (Exception ex) {
        vals[n-1] = 0;
      }
    }
    return new Rectangle(vals[0], vals[1], vals[2], vals[3]);
  }
  
  private static Point locFromJSON(String[] json) {
    int[] vals = new int[2];
    for (int n = 1; n < 3; n++) {
      try {
        vals[n-1] = Integer.parseInt(json[n].trim());
      } catch (Exception ex) {
        vals[n-1] = 0;
      }
    }
    return new Point(vals[0], vals[1]);
  }
  
  private static int intFromJSON(String[] json, int pos) {
    try {
      return Integer.parseInt(json[pos].trim());
    } catch (Exception ex) {
      return 0;
    }
  }
  
  private static double dblFromJSON(String[] json, int pos) {
    try {
      return Double.parseDouble(json[pos].trim());
    } catch (Exception ex) {
      return 0;
    }
  }
//</editor-fold>
}
