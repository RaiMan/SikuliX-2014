package org.sikuli.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.basics.SikuliScript;

public class ImagePath {
  
  private static final String me = "ImagePath";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }
  
  public static class PathEntry {
    public URL pathURL;
    public String pathGiven;

    public PathEntry(String g, URL p) {
      pathGiven = g;
      pathURL = p;
    }
  } 
  
  private static final List<PathEntry> imagePaths = Collections.synchronizedList(new ArrayList<PathEntry>());
  private static String BundlePath = null;

  static {
    imagePaths.add(null);
    BundlePath = Settings.BundlePath;
  }

  /**
   * get the list of path entries
   * @return 
   */
  public static List<PathEntry> getPaths() {
    return imagePaths;
  }
  
  private static int getPathCount() {
    int count = imagePaths.size();
    for (PathEntry path : imagePaths) {
      if (path == null) {
        count--;
      }
    }
    return count;
  }
  
  public static String[] getImagePath() {
    String[] paths = new String[getPathCount()];
    int i = 0;
    for (PathEntry path : imagePaths) {
      if (path == null) {
        continue;
      }
      paths[i++] = path.pathGiven;
    }
    return paths;
  }
  
  /**
   * print the list of path entries
   */
  public static void printPaths() {
    log(0, "ImagePath has %d entries", imagePaths.size());
    log(lvl, "start of list ----------------------------");
    for (PathEntry path : imagePaths) {
      if (path == null) {
        log(lvl, "Path: NULL");
      } else {
        log(lvl, "Path: given: %s\nis: %s", path.pathGiven, path.pathURL.toString());
      }
    }
    log(lvl, "end of list ----------------------------");
  }
  
  public static URL find(String fname) {
    URL fURL = null;
    fname = FileManager.slashify(fname, false);
    if (new File(fname).isAbsolute()) {
      if (new File(fname).exists()) {
        fURL = FileManager.makeURL(fname);
      } else {
        log(-1, "FatalError: not locatable: " + fname);
      }
    } else {
      for (PathEntry path : getPaths()) {
        if (path == null) {
          continue;
        }
        if ("file".equals(path.pathURL.getProtocol())) {
          fURL = FileManager.makeURL(path.pathURL, fname);
          if (new File(fURL.getPath()).exists()) {
            break;
          }
        } else if ("jar".equals(path.pathURL.getProtocol())) {
          fURL = FileManager.getURLForContentFromURL(path.pathURL, fname);
          if (fURL != null) {
            break;
          }
        } else {
          //TODO support for http image path
          log(-1, "URL not supported: " + path.pathURL);
        }
      }
      if (fURL == null) {
        log(-1, "not found on image path: " + fname);
        printPaths();
      }
    }
    return fURL;
  }
  
  public static BufferedReader open(String fname) {
    log(lvl, "open: " + fname);
    BufferedReader br = null;
    URL furl = find(fname);
    if (furl != null) {
      try {
        br = new BufferedReader(new InputStreamReader(furl.openStream()));
      } catch (Exception ex) {
        log(-1, "open: %s", ex.getMessage());
        return null;
      } 
      try {
        br.mark(10);
        if (br.read() < 0) {
          br.close();
          return null;
        }
        br.reset();
      } catch (Exception ex) {
        log(-1, "open: %s", ex.getMessage());
        try {
            br.close();
        } catch (Exception ex1) {
          log(-1, "open: %s", ex1.getMessage());
          return null;
        } 
        return null;
      } 
    }
    return br;
  }
  
  /**
   * see; {@link #add(String, String)} 
   * 
   * @param mainPath a valid classname optionally followed by /subfolder...
   * @return 
   */
  public static boolean add(String mainPath) {
    return add(mainPath, null);
  }

  /**
   * Set the primary image path to the top folder level of a jar based on the given class name (must
   * be found on class path). When not running from a jar (e.g. running in some IDE) the path will be the
   * path to the compiled classes (for Maven based projects this is target/classes that contains all
   * stuff copied from src/main/resources automatically)<br>
   *
   * @param mainPath a valid classname optionally followed by /subfolder...
   * @param altPath alternative image folder, when not running from jar (absolute path) 
   * @return 
   */
  public static boolean add(String mainPath, String altPath) {
    PathEntry path = makePathURL(mainPath, altPath);
    if (path != null) {
      if (hasPath(path.pathURL) < 0) {
        log(lvl, "add: %s", path.pathURL);
        imagePaths.add(path);
      } else {
        log(lvl, "duplicate not added: %s", path.pathURL);
      }
      return true;
    } else {
      log(-1, "addImagePath: not valid: %s", mainPath);      
      return false;
    }
  }
  
  private static int hasPath(URL pURL) {
    PathEntry path = imagePaths.get(0);
    if (path == null) {
      return -1;
    }
    if (path.pathURL.toExternalForm().equals(pURL.toExternalForm())) {
      return 0;
    }
    for (PathEntry p : imagePaths.subList(1, imagePaths.size())) {
      if (p!= null && p.pathURL.toExternalForm().equals(pURL.toExternalForm())) {
        return 1;
      }      
    }
    return -1;
  }

  /**
   * add entry to end of list
   * 
   * @param pURL
   */
  public static void add(URL pURL) {
    imagePaths.add(new PathEntry("__PATH_URL__", pURL));
  }
  
  /**
   * remove entry with given path
   * 
   * @param path
   * @return true on success, false ozherwise
   */
  public static boolean remove(String path) {
    return remove(makePathURL(path, null).pathURL);
  }
  
  /**
   * remove entry with given URL
   * 
   * @param pURL
   * @return true on success, false ozherwise
   */
  public static boolean remove(URL pURL) {
    Iterator<PathEntry> it = imagePaths.iterator();
    PathEntry p, p0;
    p0 = imagePaths.get(0);
    boolean success = false;
    while (it.hasNext()) {
      p = it.next();
      if (!p.pathURL.toExternalForm().equals(pURL.toExternalForm())) {
        continue;
      }
      it.remove();
      Image.purge(p.pathURL);
      success = true;
    }
    if (success) {
      if (imagePaths.isEmpty()) {
        imagePaths.add(p0);
      } else if (!imagePaths.get(0).equals(p0)) {
        imagePaths.add(0, p0);
      }
    }
    return success;
  }
  
  /**
   * empty path list and add given path
   * 
   * @param path
   * @return true on success, false otherwise
   */
  public static boolean reset(String path) {
    reset();
    return setBundlePath(path);
  }
  
  /**
   * empty path list and restore entry 0 (bundlePath) 
   * 
   * @return true
   */
  public static boolean reset() {
    log(lvl, "reset");
    for (PathEntry p : imagePaths) {
      if (p == null) {
        continue;
      }
      Image.purge(p.pathURL);
    }
    PathEntry bp = null;
    if (!imagePaths.isEmpty()) {
      bp = imagePaths.get(0);
    }
    imagePaths.clear();
    imagePaths.add(bp);
    return true;
  }
  
  
	/**
	 * the given path is added to the list replacing the first entry and
	 * Settings.BundlePath is replaced as well
	 *
	 * @param bundlePath a file path string relative or absolute
   * @return true on success, false otherwise
	 */
  public static boolean setBundlePath(String bundlePath) {
    if (bundlePath != null && !bundlePath.isEmpty()) {
      PathEntry path = makePathURL(bundlePath, null);
      if (path != null && "file".equals(path.pathURL.getProtocol())) {
        if (new File(new File(path.pathURL.getPath()).getAbsolutePath()).exists()) {
          imagePaths.set(0, path);
          Settings.BundlePath = path.pathURL.getPath();
          BundlePath = Settings.BundlePath;
          log(3, "new BundlePath: " + Settings.BundlePath);
          return true;
        }
      }
    }
    if (SikuliScript.getRunningInteractive()) {
      log(lvl, "setBundlePath: running interactive: no default bundle path!");      
    } else {
      log(-1, "setBundlePath: Settings not changed: invalid BundlePath: " + bundlePath);
    }
    return false;
  }

  
  /**
   * the resetting version of setBundlePath for IDE usage
   * @param bundlePath
   * @return true on success, false otherwise
   */
  public static boolean resetBundlePath(String bundlePath) {
    if (!FileManager.pathEquals(imagePaths.get(0).pathGiven, bundlePath)) {
      reset();
      return setBundlePath(bundlePath);
    }
    return true;
  }

	/**
	 *
	 * @return the current bundle path or null if invalid
	 */
	public static String getBundlePath() {
    if (imagePaths.get(0) == null) {
      setBundlePath(Settings.BundlePath);
      return BundlePath;
    }
    String path = imagePaths.get(0).pathURL.getPath();
    if (Settings.BundlePath != null && path.equals(Settings.BundlePath)) {
      return BundlePath;
    } else {
      log(-1, "getBundlePath: Settings.BundlePath is invalid: returning working dir\n" +
              "Settings.BundlePath: %s\nImagePaths[0]: %s", 
              Settings.BundlePath, imagePaths.get(0).pathURL.getPath());
      return new File("").getAbsolutePath();
    }
	}
  
  private static PathEntry makePathURL(String mainPath, String altPath) {
    if (new File(mainPath).isAbsolute()) {
      if(new File(mainPath).exists()) {
        mainPath = FileManager.slashify(mainPath, true);
        return new PathEntry(mainPath, FileManager.makeURL(mainPath));
      } else {
        return null;
      }
    }
    Class cls = null;
    String klassName = null;;
    String subPath = "";
    URL pathURL = null;
    int n = mainPath.indexOf("/");
    if (n > 0) {
      klassName = mainPath.substring(0, n);
      if (n < mainPath.length() - 2) {
        subPath = mainPath.substring(n+1);
      }
    } else {
      klassName = mainPath;
    }
    try {
      cls = Class.forName(klassName);
    } catch (ClassNotFoundException ex) { }
    if (cls != null) {
      CodeSource codeSrc = cls.getProtectionDomain().getCodeSource();
      if (codeSrc != null && codeSrc.getLocation() != null) {
        URL jarURL = codeSrc.getLocation();
        if (jarURL.getPath().endsWith(".jar")) {
            pathURL = FileManager.makeURL(FileManager.slashify(jarURL.toString() + "!/" + subPath, true), "jar");
        } else {
          if (altPath == null) {
            altPath = jarURL.getPath();
          }
          if (new File(altPath, subPath).exists()) {
            pathURL = FileManager.makeURL(FileManager.slashify(new File(altPath, subPath).getAbsolutePath(), true));
          }
        }
      }
    } else {
      if (new File(mainPath).exists()) {
        pathURL = FileManager.makeURL(new File(mainPath).getAbsolutePath());
      }
    }
    if (pathURL != null) {
      return new PathEntry(mainPath, pathURL);
    } else {
      return null;
    }
  }
}
