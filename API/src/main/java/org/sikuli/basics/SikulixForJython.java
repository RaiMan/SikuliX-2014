/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.basics;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.CodeSource;

/**
 *
 * Can be used in pure Jython environments to add the Sikuli Python API to sys.path<br>
 * Usage: (before any Sikuli features are used)<br>
 import org.sikuli.basics.SikulixForJython<br>
 * from sikuli import *
 */
public class SikulixForJython {
  
  static {
    String mem = "SikuliXforJython: ";
    String sikuliStuff = "sikuli/Sikuli.py";
    CodeSource src = FileManager.class.getProtectionDomain().getCodeSource();
    if (src.getLocation() != null) {
      String pyLib = FileManager.slashify(src.getLocation().getPath(), true) + "Lib";
      Method pyGetSystemState, pyListAdd, pyListgetArray;
      Field pathFld;
      Object pss;
      Object[] pssPath = null;
      try {
        //PySystemState pss = Py.getSystemState();
        Class PyCl = Class.forName("org.python.core.Py");
        pyGetSystemState = PyCl.getDeclaredMethod("getSystemState", new Class[0]);
        pyGetSystemState.setAccessible(true);        
        pss = pyGetSystemState.invoke(PyCl, new Object[0]);
        Class PssCl = Class.forName("org.python.core.PySystemState");
        pathFld = PssCl.getField("path");
        Class PListCl = Class.forName("org.python.core.PyList");
        pyListAdd = PListCl.getDeclaredMethod("add", new Class[]{Object.class});
        pyListgetArray = PListCl.getDeclaredMethod("toArray", new Class[0]);
        pssPath = (Object[]) pyListgetArray.invoke(pathFld.get(pss), new Object[0]);
        File sik = null;
        String e;
        for (int n = 0; n < pssPath.length; n++) {
          e = (String) pssPath[n];
          Debug.log(3, mem + "sys.path[%d]: " + e.toString(), n);
          if (e.toString().equals(pyLib)) {
            Debug.log(3, "Sikuli Jython API seems to be in sys.path");
            sik = null;
            break;
          }
          sik = new File(FileManager.slashify(e.toString(), true) + sikuliStuff);
          if (sik.exists()) {
            Debug.error("Sikuli Jython API already on sys.path, but not at sikulixapi.jar/Lib");
            Debug.error("Found here: " + FileManager.slashify(e.toString(), true));
            sik = null;
            break;
          }
        }
        if (sik != null) {
          Debug.log(3, mem + "found Sikuli Jython in: \n" + pyLib);
          Debug.log(3, mem + "Trying to add to sys.path");
          pyListAdd.invoke(pathFld.get(pss), new Object[]{pyLib});
          pssPath = (Object[]) pyListgetArray.invoke(pathFld.get(pss), new Object[0]);
          Debug.log(3,mem + "new Jython path:");
          for (int n = 0; n < pssPath.length; n++) {
            e = (String) pssPath[n];
            Debug.log(3, mem + "sys.path[%d]: " + e.toString(), n);
          }
          ResourceLoader.get().setItIsJython();
       }
      } catch (Exception e) {
        Debug.error(mem + "Fatal error: Jython not found on classpath or not accessible - Sikuli might not work");
      }
    } else {
      Debug.error(mem + "package: sikuli/__init__.py not found: Sikuli might not work");
    }
  }
}
