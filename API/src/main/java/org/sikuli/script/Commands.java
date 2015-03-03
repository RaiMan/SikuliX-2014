package org.sikuli.script;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;

public class Commands {

  private static Region scr = new Screen();
  private static RunTime runTime = RunTime.get();
  
  public static boolean isNashorn() {
    return runTime.isJava8();
  }

  public static Object call(String function, Object... args) {
    Method m = null;
    Object retVal = null;
    int count = 0;
    for (Object aObj : args) {
      if (aObj.getClass().getName().endsWith("Undefined")) {
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

	public static Region use(Object... args) {
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

  public static Match wait(Object... args) throws FindFailed {
    int len = args.length;
    String image = "";
    float score = 0.0f;
    double timeout = -1.0f;
    boolean argsOK = true;
    if (len == 0 || len > 3) {
      argsOK = false;
    } else {
      Object aObj = args[0];
			if (isJSON(aObj)) {
				aObj = fromJSON(aObj);
			}
      if (aObj instanceof String) {
        image = (String) aObj;
      } else if (aObj instanceof Pattern) {
        if (len > 1 && isNumber(args[1])) {
          return scr.wait((Pattern) aObj, getNumber(args[1]));
        } else {
          return scr.wait((Pattern) aObj);
        }
      } else if (isNumber(aObj)) {
        scr.wait(getNumber(aObj));
        return null;
      } else {
        argsOK = false;
      }
    }
    if (argsOK && len > 1) {
      if (len > 2 && isNumber(args[2])) {
        score = (float) getNumber(args[2]) / 100.0f;
        if (score < 0.7) {
          score = 0.7f;
        } else if (score > 0.99) {
          score = 0.99f;
        }
      }
      if (len > 1 && isNumber(args[1])) {
        timeout = getNumber(args[1]);
      }
    }
    if (!argsOK) {
      throw new UnsupportedOperationException(
              "Commands.wait: parameters: String:image, float:timeout, int:score");
    }
    Object aPattern;
    if (score > 0) {
      aPattern = new Pattern(image).similar(score);
    } else {
      aPattern = image;
    }
    if (timeout > -1f) {
      return scr.wait(aPattern, timeout);
    }
    return scr.wait(aPattern);
  }

	public static Match waitVanish(Object... args) {
		return null;
	}

  public static Match exists(Object... args) {
    Match match = null;
    try {
      match = wait(args);
    } catch (Exception ex) {}
    return match;
  }

  public static Location hover(Object... args) {
    int len = args.length;
    if (len < 4) {
      Object aObj = args[0];
      Location loc = null;
			if (isJSON(aObj)) {
				aObj = fromJSON(aObj);
			}
      if (aObj instanceof String || aObj instanceof Pattern) {
        Match aMatch = null;
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
    Location loc = hover(args);
    Mouse.click(null, Button.LEFT, 0, false, null);
    return Mouse.at();
  }

  public static Location doubleClick(Object... args) {
    Location loc = hover(args);
    Mouse.click(null, Button.LEFT, 0, true, null);
    return Mouse.at();
  }

  public static Location rightClick(Object... args) {
    Location loc = hover(args);
    Mouse.click(null, Button.RIGHT, 0, false, null);
    return Mouse.at();
  }

  public static Location type(Object... args) {
    return null;
  }

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
}
