package org.sikuli.script;

import java.lang.reflect.Method;

public class Commands {
  
  private static Region scr = new Screen();
  
  public static Object call (String function, Object... args) {
    Method m = null;
    Object retVal = null;
    int count = 0;
    for (Object aObj : args) {
      if (aObj.getClass().getName().endsWith("Undefined")) {
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
  
  public static Region use(Object... args) {
    int len = args.length;
    int nScreen = -1;
    if (len == 0 || len > 1) {
      scr = new Screen();
      return scr;
    }
    try {
      nScreen = (Integer) args[0];
    } catch (Exception ex) {
    }
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
      if (aObj instanceof String) {
        image = (String) aObj;
      } else if (aObj instanceof Pattern) {
        if (len > 1 && (args[1] instanceof Float || args[1] instanceof Double)) {
          return scr.wait((Pattern) aObj, (Double) args[1]);          
        } else {
          return scr.wait((Pattern) aObj);
        }
      } else if (aObj instanceof Float || aObj instanceof Double) {
        scr.wait((Double) aObj);
        return null;
      } else {
        argsOK = false;
      }
    }
    if (argsOK && len > 1) {
      for (Object aObj : new Object[] {args[1], (len > 2 ? args[2] : null)}) {
        if (null == aObj) {
          continue;
        }
        if (aObj instanceof Float || aObj instanceof Double) {
          timeout = (Double) aObj;
        } else if (aObj instanceof Integer) {
          score = (0f + (Integer) aObj) / 100f;
          if (score < 0.7f) {
            score = 0.7f;
            timeout = 0.0 + (Integer) aObj;
          } else if (score > 0.99f) {
            score = 0.99f;
          }
        } else {
          argsOK = false;
        }
      }
    }
    if (!argsOK) {
      throw new UnsupportedOperationException(
              "Commands.wait: parameters: String:image, float:timeout, int:score");      
    }
    Object aPattern;
    if (score > 0f) {
      aPattern = new Pattern(image).similar(score);
    } else {
      aPattern = image;
    }
    if (timeout > -1f) {
      return scr.wait(aPattern, timeout);
    }
    return scr.wait(aPattern);
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
        if (aObj instanceof Integer && args[1] instanceof Integer) {
          Mouse.move(scr.checkMatch().offset((Integer) aObj, (Integer) args[1]));
          return Mouse.at();
        } else if (len == 3 && loc != null && args[1] instanceof Integer && args[2] instanceof Integer) {
          Mouse.move(loc.offset((Integer) args[1], (Integer) args[2]));
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
}
