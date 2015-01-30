package org.sikuli.scriptrunner;

public class JythonHelper {
  
  private static JythonHelper instance = null;
  
  private JythonHelper(){}
  
  public static JythonHelper get() {
    if (instance == null) {
      instance = new JythonHelper();
    }
    return instance;
  }
  
  private static void print(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  public String find_module(String mname, Object pPath, Object sPath) {
    if (mname.endsWith(".*")) {
      return null;
    }
    print("");
    return null;
  }
  
  
}
