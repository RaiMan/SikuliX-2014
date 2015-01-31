package org.sikuli.scriptrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.python.core.PyList;
import org.python.util.PythonInterpreter;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.ide.SikuliIDE;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;

public class JythonHelper {
  
  RunTime runTime = RunTime.get();
  
	//<editor-fold defaultstate="collapsed" desc="new logging concept">
	private static final String me = "JythonSupport: ";
	private int lvl = 3;
	private void log(int level, String message, Object... args) {
		Debug.logx(level,	me + message, args);
	}
  private void logp(String message, Object... args) {
    if (runTime.runningWinApp) {
      log(0, message, args);
    } else {
      System.out.println(String.format(message, args));
    }
  }
	//</editor-fold>

  static JythonHelper instance = null;
  static PythonInterpreter interpreter = null;
  List<String> sysPath = new ArrayList<String>();
  
  private JythonHelper(){}
  
  public static JythonHelper get() {
    if (instance == null) {
      instance = new JythonHelper();
    }
    return instance;
  }

  public static JythonHelper set(PythonInterpreter ip) {
    JythonHelper.get();
    interpreter = ip;
    return instance;
  }
  
  private static void print(String message, Object... args) {
    System.out.println(String.format(message, args));
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
    log(lvl, "findModule: %s", modName);
    String fpBundle = SikuliIDE.getInstance().getCurrentCodePane().getBundlePath();
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

    log(lvl, "loadModulePrepare: %s", modName);
    int nDot = modName.lastIndexOf(".");
    if (nDot > -1) {
      modName = modName.substring(nDot + 1);
    }
    if (!hasSysPath(modPath)) {
      sysPath.add(0, modPath);
      setSysPath();
    }
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
  
  public void dumpState() {
		PyList jypath = interpreter.getSystemState().path;
		PyList jyargv = interpreter.getSystemState().argv;
		int jypathLength = jypath.__len__();
		for (int i = 0; i < jypathLength; i++) {
      String entry = (String) jypath.get(i);
      logp("%2d: %s", i, entry);
		}
  }

  public void getSysPath() {
    sysPath = new ArrayList<String>();
		PyList jypath = interpreter.getSystemState().path;
		int jypathLength = jypath.__len__();
		for (int i = 0; i < jypathLength; i++) {
      String entry = (String) jypath.get(i);
      sysPath.add(entry);
		}
  }

  public void setSysPath() {
		PyList jypath = interpreter.getSystemState().path;
		int jypathLength = jypath.__len__();
		for (int i = 0; i < jypathLength && i < sysPath.size(); i++) {
      jypath.set(i, sysPath.get(i));
		}
    if (jypathLength < sysPath.size()) {
      for (int i = jypathLength; i < sysPath.size(); i++) {
        jypath.add(sysPath.get(i));
      }
    }
  }
  
  public void addSysPath(String fpFolder) {
    
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
        logp("%2d: %s", i, sysPath.get(i));
      }
      log(lvl, "***** Jython sys.path end");
		}
  }
  
  

  
}
