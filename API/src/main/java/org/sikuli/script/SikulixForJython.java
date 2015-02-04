/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.script;

//import org.sikuli.basics.*;

import java.io.File;
import java.net.URL;

/**
 *
 * Can be used in pure Jython environments to add the Sikuli Python API to sys.path<br>
 * Usage: (before any Sikuli features are used)<br>
 * import org.sikuli.script.SikulixForJython<br>
 * from sikuli import *
 */
public class SikulixForJython {
  
  private static SikulixForJython instance = null;
  private static final int lvl = 3;
  
  static {  
    JythonHelper helper = JythonHelper.get();
    helper.log(lvl, "SikulixForJython: init: starting");
    RunTime runTime = RunTime.get();
    String sikuliStuff = "sikuli/Sikuli";
    File fSikuliStuff = helper.existsSysPathModule(sikuliStuff);
    String libSikuli = "/Lib/" + sikuliStuff + ".py";
    String fpSikuliStuff;
    if (null == fSikuliStuff) {
      URL uSikuliStuff = runTime.resourceLocation(libSikuli);
      if (uSikuliStuff == null) {
        runTime.dumpClassPath();
        helper.terminate(1, "no suitable sikulix...jar on classpath");
      }
      if ("jar".equals(uSikuliStuff.getProtocol())) {
        fpSikuliStuff = uSikuliStuff.getPath().split("!")[0].substring(5); 
        fpSikuliStuff = new File(fpSikuliStuff, "/Lib").getAbsolutePath();
      } else {
        fpSikuliStuff = new File(uSikuliStuff.getPath()).getAbsolutePath();
        fpSikuliStuff = new File(fpSikuliStuff.substring(0, 
                fpSikuliStuff.length() - libSikuli.length()), "Lib").getAbsolutePath();
      }
      if (!helper.hasSysPath(fpSikuliStuff)) {
        helper.log(lvl, "sikuli/*.py not found on current Jython::sys.path");
        helper.addSysPath(fpSikuliStuff);
        if (!helper.hasSysPath(fpSikuliStuff)) {
          helper.terminate(1, "not possible to add to Jython::sys.path:\n%s", fpSikuliStuff);
        }
        helper.log(lvl, "added as Jython::sys.path[0]:\n%s", fpSikuliStuff);
      } else {
        helper.log(lvl, "sikuli/*.py is on Jython::sys.path at:\n%s", fpSikuliStuff);
      }
    }     
    helper.log(lvl, "SikulixForJython: init: success");
  }

  private SikulixForJython() {}
  
  public static SikulixForJython get() {
    if (null == instance) {
      instance = new SikulixForJython();
    }
    return instance;
  }
}
