/*
 * Copyright 2010-2015, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * RaiMan 2015
 */
package org.sikuli.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
//import org.python.core.PyList;
//import org.python.util.PythonInterpreter;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
//import org.sikuli.ide.SikuliIDE;

public class JythonHelper {
  
  RunTime runTime = RunTime.get();
  
	//<editor-fold defaultstate="collapsed" desc="new logging concept">
	private static final String me = "JythonSupport: ";
	private static int lvl = 3;
	public void log(int level, String message, Object... args) {
		Debug.logx(level,	me + message, args);
	}

  private void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }

  public void terminate(int retVal, String msg, Object... args) {
    runTime.terminate(retVal, me + msg, args);
  }
	//</editor-fold>

  static JythonHelper instance = null;
  static Object interpreter = null;
  List<String> sysPath = new ArrayList<String>();
  int nPathAdded = 0;
  int nPathSaved = -1;
  static Class[] nc = new Class[0];
  static Class cInterpreter = null;
  static Class cList = null;
  static Method mLen, mGet, mSet, mAdd, mRemove;
  static Method mGetSystemState;
  static Field PI_path;
  
  private JythonHelper(){}
  
  public static JythonHelper get() {
    if (instance == null) {
      instance = new JythonHelper();
      instance.log(lvl, "init: starting");
      try {
        cInterpreter = Class.forName("org.python.util.PythonInterpreter");
        mGetSystemState = cInterpreter.getMethod("getSystemState", nc);
        Constructor PI_new = cInterpreter.getConstructor(nc);
        interpreter = PI_new.newInstance(null);
        cList = Class.forName("org.python.core.PyList");
        mLen = cList.getMethod("__len__", nc);
        mGet = cList.getMethod("get", new Class[]{int.class});
        mSet = cList.getMethod("set", new Class[]{int.class, Object.class});
        mAdd = cList.getMethod("add", new Class[]{Object.class});
        mRemove = cList.getMethod("remove", new Class[]{int.class});
      } catch (Exception ex) {
        cInterpreter = null;
      }  
      instance.log(lvl, "init: success");
    }
    if (cInterpreter == null) {
      instance.runTime.terminate(1, "JythonHelper: no Jython on classpath");
    }
    return instance;
  }

  public static JythonHelper set(Object ip) {
    JythonHelper.get();
    interpreter = ip;
    return instance;
  }
  
  public String findModule(String modName, Object packPath, Object sysPath) {

//  module_name = _stripPackagePrefix(module_name)
//  if module_name[0:1] == "*": 
//      return None
//  if package_path:
//      paths = package_path
//  else:
//      paths = sys.path
//  for path in paths:
//      mod = self._find_module(module_name, path)
//      if mod:
//          return mod
//  if Sikuli.load(module_name +".jar"):
//      return None
//  return None
    
    if (modName.endsWith(".*")) {
      return null;
    }
    log(lvl + 1, "findModule: %s", modName);
    String fpBundle = ImagePath.getBundlePath();
    File fParentBundle = null;
    File fModule = null;
    if (fpBundle != null) {
      fParentBundle = new File(fpBundle).getParentFile();
      fModule = existsModule(modName, fParentBundle);
    }
    if (fModule == null) {
      fModule = existsSysPathModule(modName);
      if (fModule == null) {
        return null;
      }
    }
    return fModule.getAbsolutePath();
  }
  
  public String loadModulePrepare(String modName, String modPath) {

//  module_name = _stripPackagePrefix(module_name)
//  ImagePath.add(self.path)
//  Sikuli._addModPath(self.path)
//  return self._load_module(module_name)

    log(lvl + 1, "loadModulePrepare: %s", modName);
    int nDot = modName.lastIndexOf(".");
    if (nDot > -1) {
      modName = modName.substring(nDot + 1);
    }
    addSysPath(modPath);
    ImagePath.add(modPath);
    return modName;
  }
  
  private File existsModule(String mName, File fFolder) {
    if (mName.endsWith(".sikuli") || mName.endsWith(".py")) {
      return null;
    }
    File fSikuli = new File(fFolder, mName + ".sikuli");
    if (fSikuli.exists()) {
      return fSikuli;
    }
    File fPython = new File(fFolder, mName + ".py");
    if (fPython.exists()) {
      return fPython;
    }
    return null;
  }
  
  public void getSysPath() {
    sysPath = new ArrayList<String>();
    if (null == cInterpreter) {
      sysPath = null;
      return;
    }
    try {
      Object aState  = mGetSystemState.invoke(interpreter, (Object[]) null);                
      Field fPath = aState.getClass().getField("path");
      Object pyPath = fPath.get(aState);
      Integer pathLen = (Integer) mLen.invoke(pyPath, (Object[]) null);
      for (int i = 0; i < pathLen; i++) {
        String entry = (String) mGet.invoke(pyPath, i);
        log(lvl + 1, "sys.path[%2d] = %s", i, entry);
        sysPath.add(entry);
      }
    } catch (Exception ex) {
      sysPath = null;
    }    
  }

  public void setSysPath() {
    if (null == cInterpreter || null == sysPath) {
      return;
    }
    try {
      Object aState  = mGetSystemState.invoke(interpreter, (Object[]) null);                
      Field fPath = aState.getClass().getField("path");
      Object pyPath = fPath.get(aState);
      Integer pathLen = (Integer) mLen.invoke(pyPath, (Object[]) null);
  		for (int i = 0; i < pathLen && i < sysPath.size(); i++) {
        String entry = sysPath.get(i);
        log(lvl + 1, "sys.path.set[%2d] = %s", i, entry);
        mSet.invoke(pyPath, i, entry);
  		}
      if (pathLen < sysPath.size()) {
        for (int i = pathLen; i < sysPath.size(); i++) {
          String entry = sysPath.get(i);
          log(lvl + 1, "sys.path.add[%2d] = %s", i, entry);
          mAdd.invoke(pyPath, entry);
        }
      }
      if (pathLen > sysPath.size()) {
        for (int i = sysPath.size(); i < pathLen; i++) {
          String entry = (String) mGet.invoke(pyPath, i);
          log(lvl + 1, "sys.path.rem[%2d] = %s", i, entry);
          mRemove.invoke(pyPath, i);
        }
      }
    } catch (Exception ex) {
      sysPath = null;
    }    
  }
  
  public void addSysPath(String fpFolder) {
    if (!hasSysPath(fpFolder)) {
      sysPath.add(0, fpFolder);
      setSysPath();
      nPathAdded++;
    }
  }
  
  public void putSysPath(String fpFolder, int n) {
    if (n < 1 || n > sysPath.size()) {
      addSysPath(fpFolder);
    } else {
      sysPath.add(n, fpFolder);
      setSysPath();
      nPathAdded++;
    }
  }
  
  public void addSysPath(File fFolder) {
    addSysPath(fFolder.getAbsolutePath());
  }
  
  public void insertSysPath(File fFolder) {
    getSysPath();
    sysPath.add((nPathSaved > -1 ? nPathSaved : 0), fFolder.getAbsolutePath());
    setSysPath();
    nPathSaved = -1;
  }
  
  public void removeSysPath(File fFolder) {
    int n;
    if (-1 < (n = getSysPathEntry(fFolder))) {
      sysPath.remove(n);
      nPathSaved = n;
      setSysPath();
      nPathAdded = nPathAdded == 0 ? 0 : nPathAdded--;
    }
  }
  
  public boolean hasSysPath(String fpFolder) {
    getSysPath();
    for (String fpPath : sysPath) {
      if (FileManager.pathEquals(fpPath, fpFolder)) {
        return true;
      }
    }
    return false;
  }
  
  public int getSysPathEntry(File fFolder) {
    getSysPath();
    int n = 0;
    for (String fpPath : sysPath) {
      if (FileManager.pathEquals(fpPath, fFolder.getAbsolutePath())) {
        return n;
      }
      n++;
    }
    return -1;
  }
  
  public File existsSysPathModule(String modname) {
    getSysPath();
    File fModule = null;
    for (String fpPath : sysPath) {
      fModule = existsModule(modname, new File(fpPath));
      if (null != fModule) {
        break;
      }
    }
    return fModule;
  }
  
  public void showSysPath() {
    if (Debug.is(lvl)) {
      getSysPath();
      log(lvl, "***** Jython sys.path");
      for (int i = 0; i < sysPath.size(); i++) {
        logp(lvl, "%2d: %s", i, sysPath.get(i));
      }
      log(lvl, "***** Jython sys.path end");
		}
  }
}
