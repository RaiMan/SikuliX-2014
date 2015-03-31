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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;

public class JythonHelper {

  static RunTime runTime = RunTime.get();

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
  List<String> sysArgv = new ArrayList<String>();
  int nPathAdded = 0;
  int nPathSaved = -1;
  static Class[] nc = new Class[0];
  static Class[] nc1 = new Class[1];
  static Class cInterpreter = null;
  static Class cList = null;
  static Class cPy = null;
  static Class cPyFunction = null;
  static Class cPyMethod = null;
  static Class cPyInstance = null;
  static Class cPyObject = null;
  static Class cPyString = null;
  static Method mLen, mGet, mSet, mAdd, mRemove, mClear;
  static Method mGetSystemState, mExec, mExecfile;
  static Field PI_path;

  private JythonHelper(){}

  public static JythonHelper get() {
    if (instance == null) {
      instance = new JythonHelper();
      instance.log(lvl, "init: starting");
      try {
        cInterpreter = Class.forName("org.python.util.PythonInterpreter");
      } catch (Exception ex) {
        String sJython = new File(runTime.SikuliJython).getName();
        File fJython = new File(runTime.fSikulixDownloadsGeneric, sJython);
        if (fJython.exists()) {
          runTime.addToClasspath(fJython.getAbsolutePath());
          runTime.dumpClassPath();
        } else {
          instance.log(-1, "Not possible to get a Jython on classpath!");
          cInterpreter = null;
        }
      }
      try {
        cInterpreter = Class.forName("org.python.util.PythonInterpreter");     
        mGetSystemState = cInterpreter.getMethod("getSystemState", nc);
        mExec = cInterpreter.getMethod("exec", new Class[] {String.class});
        mExecfile = cInterpreter.getMethod("execfile", new Class[] {String.class});
        Constructor PI_new = cInterpreter.getConstructor(nc);
        interpreter = PI_new.newInstance(null);
        cList = Class.forName("org.python.core.PyList");
        cPy = Class.forName("org.python.core.Py");
        cPyFunction = Class.forName("org.python.core.PyFunction");
        cPyMethod = Class.forName("org.python.core.PyMethod");
        cPyInstance = Class.forName("org.python.core.PyInstance");
        cPyObject = Class.forName("org.python.core.PyObject");
        cPyString = Class.forName("org.python.core.PyString");
        mLen = cList.getMethod("__len__", nc);
        mClear = cList.getMethod("clear", nc);
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

  private void noOp() {} // for debugging as breakpoint

  class PyInstance {
    Object inst = null;
		Method mGetAttr = null;
		Method mInvoke = null;
    public PyInstance(Object i) {
      inst = i;
			cPyInstance.cast(inst);
			try {
				mGetAttr = cPyInstance.getMethod("__getattr__", String.class);
				mInvoke = cPyInstance.getMethod("invoke", String.class, cPyObject);
			} catch (Exception ex) {
				noOp();
			}
    }
		public Object get() {
			return inst;
		}
    Object __getattr__(String mName) {
      if (mGetAttr == null) {
				return null;
			}
			Object method = null;
			try {
				method = mGetAttr.invoke(inst, mName);
			} catch (Exception ex) {}
			return method;
    }
    public void invoke(String mName, Object arg) {
      if (mInvoke != null) {
				try {
					mInvoke.invoke(inst, mName, arg);
				} catch (Exception ex) {
					noOp();
				}
			}
    }
  }

  class PyFunction {
    public String __name__;
    Object func = null;
		Method mCall = null;
		Method mCall1 = null;
    public PyFunction(Object f) {
      func = f;
			try {
				cPyFunction.cast(func);
				mCall = cPyFunction.getMethod("__call__");
				mCall1 = cPyFunction.getMethod("__call__", cPyObject);
			} catch (Exception ex) {
				func = null;
			}
			if (func == null) {
				try {
					func = f;
					cPyMethod.cast(func);
					mCall = cPyMethod.getMethod("__call__");
					mCall1 = cPyMethod.getMethod("__call__", cPyObject);
				} catch (Exception ex) {
					func = null;
				}
			}
    }
    void __call__(Object arg) {
			if (mCall1 != null) {
				try {
				mCall1.invoke(func, arg);
				} catch (Exception ex) {}
			}
    }
    void __call__() {
			if (mCall != null) {
				try {
				mCall.invoke(func);
				} catch (Exception ex) {}
			}
    }
  }

  class Py {
		Method mJava2py = null;
		public Py() {
			try {
				mJava2py = cPy.getMethod("java2py", Object.class);
			} catch (Exception ex) {
				noOp();
			}
		}
    Object java2py(Object arg) {
			if (mJava2py == null) {
				return null;
			}
			Object pyObject = null;
			try {
				pyObject = mJava2py.invoke(null, arg);
			} catch (Exception ex) {
				noOp();
			}
      return pyObject;
    }
  }

  class PyString {
    String aString = "";
		Object pyString = null;
    public PyString(String s) {
      aString = s;
			try {
				pyString = cPyString.getConstructor(String.class).newInstance(aString);
			} catch (Exception ex) {}
    }
		public Object get() {
			return pyString;
		}
  }
  
  public boolean exec(String code) {
    try {
      mExec.invoke(interpreter, code);
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  public boolean execfile(String fpScript) {
    try {
      mExecfile.invoke(interpreter, fpScript);
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

//TODO check signature (instance method)
  public boolean checkCallback(Object[] args) {
    PyInstance inst = new PyInstance(args[0]);
    String mName = (String) args[1];
    Object method = inst.__getattr__(mName);
    if (method == null || !method.getClass().getName().contains("PyMethod")) {
      log(-100, "checkCallback: Object: %s, Method not found: %s", inst, mName);
      return false;
    }
    return true;
  }

	public boolean runLoggerCallback(Object[] args) {
    PyInstance inst = new PyInstance(args[0]);
    String mName = (String) args[1];
    String msg = (String) args[2];
    Object method = inst.__getattr__(mName);
    if (method == null || !method.getClass().getName().contains("PyMethod")) {
      log(-100, "runLoggerCallback: Object: %s, Method not found: %s", inst, mName);
      return false;
    }
    try {
      PyString pmsg = new PyString(msg);
      inst.invoke(mName, pmsg.get());
    } catch (Exception ex) {
      log(-100, "runLoggerCallback: invoke: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  public boolean runObserveCallback(Object[] args) {
    PyFunction func = new PyFunction(args[0]);
    boolean success = true;
    try {
      func.__call__(new Py().java2py(args[1]));
    } catch (Exception ex) {
//      if (!"<lambda>".equals(func.__name__)) {
      if (!func.toString().contains("<lambda>")) {
        log(-1, "runObserveCallback: jython invoke: %s", ex.getMessage());
        return false;
      }
      success = false;
    }
    if (success) {
      return true;
    }
    try {
      func.__call__();
    } catch (Exception ex) {
      log(-1, "runObserveCallback: jython invoke <lambda>: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  //TODO implement generalized callback
  public boolean runCallback(Object[] args) {
    PyInstance inst = (PyInstance) args[0];
    String mName = (String) args[1];
    Object method = inst.__getattr__(mName);
    if (method == null || !method.getClass().getName().contains("PyMethod")) {
      log(-1, "runCallback: Object: %s, Method not found: %s", inst, mName);
      return false;
    }
    try {
      PyString pmsg = new PyString("not yet supported");
      inst.invoke(mName, pmsg.get());
    } catch (Exception ex) {
      log(-1, "runCallback: invoke: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  public static JythonHelper set(Object ip) {
    JythonHelper.get();
    interpreter = ip;
    return instance;
  }

  public boolean load(String fpJarOrFolder) {
//##
//# loads a Sikuli extension (.jar) from
//#  1. user's sikuli data path
//#  2. bundle path
//#
//def load(jar):
//    def _load(abspath):
//        if os.path.exists(abspath):
//            if not abspath in sys.path:
//                sys.path.append(abspath)
//            return True
//        return False
//
//    if JythonHelper.load(jar):
//        return True
//
//    if _load(jar):
//        return True
//    path = getBundlePath()
//    if path:
//        jarInBundle = os.path.join(path, jar)
//        if _load(jarInBundle):
//            return True
//    path = ExtensionManager.getInstance().getLoadPath(jar)
//    if path and _load(path):
//        return True
//    return False
    log(lvl, "load: to be loaded:\n%s", fpJarOrFolder);
    String fpBundle = ImagePath.getBundlePath();
    File fJar = new File(FileManager.normalizeAbsolute(fpJarOrFolder, false));
    if (!fJar.exists()) {
      fJar = new File(fpBundle, fpJarOrFolder);
      fJar = new File(FileManager.normalizeAbsolute(fJar.getPath(), false));
      if (!fJar.exists()) {
        fJar = new File(runTime.fSikulixExtensions, fpJarOrFolder);
        if (!fJar.exists()) {
					fJar = new File(runTime.fSikulixLib, fpJarOrFolder);
					if (!fJar.exists()) {
						fJar = null;
					}
        }
      }
    }
    if (fJar != null) {
      if (runTime.addToClasspath(fJar.getPath())) {
        if (!hasSysPath(fJar.getPath())) {
          insertSysPath(fJar);
        }
      } else {
        log(-1, "load: not possible");
        return false;
      }
    } else {
      log(-1, "load: could not be found - even not in bundle nor in Lib nor in Extensions");
      return false;
    }
    return true;
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

  public void getSysArgv() {
    sysArgv = new ArrayList<String>();
    if (null == cInterpreter) {
      sysArgv = null;
      return;
    }
    try {
      Object aState  = mGetSystemState.invoke(interpreter, (Object[]) null);
      Field fArgv = aState.getClass().getField("argv");
      Object pyArgv = fArgv.get(aState);
      Integer argvLen = (Integer) mLen.invoke(pyArgv, (Object[]) null);
      for (int i = 0; i < argvLen; i++) {
        String entry = (String) mGet.invoke(pyArgv, i);
        log(lvl + 1, "sys.path[%2d] = %s", i, entry);
        sysArgv.add(entry);
      }
    } catch (Exception ex) {
      sysArgv = null;
    }
  }
  
  public void setSysArgv(String[] args) {
    if (null == cInterpreter || null == sysArgv) {
      return;
    }
    try {
      Object aState  = mGetSystemState.invoke(interpreter, (Object[]) null);
      Field fArgv = aState.getClass().getField("argv");
      Object pyArgv = fArgv.get(aState);
      mClear.invoke(pyArgv, null);
      for (String arg : args) {
        mAdd.invoke(pyArgv, arg);
      }
    } catch (Exception ex) {
      sysArgv = null;
    }
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
