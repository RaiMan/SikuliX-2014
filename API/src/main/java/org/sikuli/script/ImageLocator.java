/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;

/**
 * This class is currently used to locate image files in the filesystem <br />
 * and in the internet (the files are cached locally) <br />
 * 
 * @deprecated will completely replaced by the classes Image and ImagePath
 */
@Deprecated
public class ImageLocator {

    static ArrayList<String> pathList = new ArrayList<String>();
    static int firstEntries = 1;
    static File _cache_dir_global = new File(Settings.BaseTempPath, "sikuli_cache/SIKULI_GLOBAL/");
    static Map<URI, String> _cache = new HashMap<URI, String>();

    static {
        pathList.add("");
        resetImagePath("");
        if (pathList.size() >= 1 && "".equals(pathList.get(0))) {
            pathList.set(0, System.getProperty("user.dir"));
        }
        if (!_cache_dir_global.exists()) {
            try {
                _cache_dir_global.mkdir();
            } catch (Exception e) {
                Debug.error("ImageLocator: Local cache dir not possible: " + _cache_dir_global);
                _cache_dir_global = null;
            }
        }
    }

    private static String[] splitImagePath(String path) {
        if (path == null || "".equals(path)) {
            return new String[0];
        }
        path = path.replaceAll("[Hh][Tt][Tt][Pp]://", "__http__//");
        path = path.replaceAll("[Hh][Tt][Tt][Pp][Ss]://", "__https__//");
        String[] pl = path.split(Settings.getPathSeparator());
        File pathName;
        for (int i = 0; i < pl.length; i++) {
            boolean isURL = false;
            path = pl[i];
            if (path.indexOf("__http__") >= 0) {
                path = path.replaceAll("__http__//", "http://");
                isURL = true;
            } else if (path.indexOf("__https__") >= 0) {
                path = path.replaceAll("__https__//", "https://");
                isURL = true;
            }
            if (isURL) {
                if ((path = getURL(path).getPath()) != null) {
                    if (!path.endsWith("/")) {
                        pl[i] = path + "/";
                    }
                } else {
                    pl[i] = null;
                }
            } else {
                pathName = new File(path);
                if (pathName.exists()) {
                    pl[i] = FileManager.slashify(pathName.getAbsolutePath(), true);
                } else {
                    pathList.remove(pl[i]);
                    pl[i] = null;
                }
            }
        }
        return pl;
    }

    private static URL getURL(String s) {
        try {
            URL url = new URL(s);
            return url;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * list entries might be a file path or an url (http/https) <br />list[0]
     * current bundlepath (Settings.BundlePath) (might be null) <br />list[1:n]
     * path's from -DSIKULI_IMAGE_PATH=... <br />list[n+1:] path's added later
     * <br />each path is contained only once <br />each contained file path
     * existed at the time it was added
     *
     * @return list of absolute path strings (a path might not exist any more)
     */
    public static String[] getImagePath() {
        return pathList.toArray(new String[0]);
    }

    private static String addImagePath(String[] pl, boolean first) {
        int addedAt = firstEntries;
        if (addedAt == pathList.size()) {
            first = false;
        }
        String epl;
        File fepl;
        for (int i = 0; i < pl.length; i++) {
            if (pl[i] == null) {
                continue;
            }
            epl = pl[i];
            //fepl = new File(epl);
//TODO handle relative paths
            if (!pathList.contains(epl)) {
                if (!first) {
                    pathList.add(epl);
                } else {
                    pathList.add(addedAt, epl);
                    addedAt++;
                }
            }
        }
        if (pl.length > 0) {
            return pl[0];
        } else {
            return null;
        }
    }

    private static String addImagePath(String path, boolean first) {
        String pl[] = splitImagePath(path);
        removeImagePath(pl);
        return addImagePath(pl, first);
    }

    /**
     * the given path(s) <br />- might be a file path or an url (http/https) <br
     * />- if file path relative: make absolute path based on current working
     * directory <br />- is(are) first removed from the list <br />- and then
     * added to the end of the list (file path: if it exists)
     *
     * @param path absolute or relative path or url <br />might be a path list
     * string with seperator : (or ; Windows)
     */
    public static String addImagePath(String path) {
        return addImagePath(path, false);
    }

    /**
     * the given path(s) <br />- might be a file path or an url (http/https) <br
     * />- if file path relative: make absolute path based on current working
     * directory <br />- is(are) first removed from the list <br />- and then
     * added to the beginning of the list (file path: if it exists) <br />as
     * entry 1 after the current bundlepath (entry 0)
     *
     * @param path absolute or relative path or url <br />might be a path list
     * string with seperator : (or ; Windows)
     */
    public static String addImagePathFirst(String path) {
        return addImagePath(path, true);
    }

    /**
     * the given paths <br />- might be a file path or an url (http/https) <br
     * />- if file path relative: make absolute path based on current working
     * directory <br />- are removed from the list <br />- and then added to the
     * end of the list (file path: if it exists)
     *
     * @param pl absolute or relative paths or urls as string array
     */
    public static String addImagePath(String[] pl) {
        return addImagePath(pl, false);
    }

    /**
     * the given paths <br />- might be a file path or an url (http/https) <br
     * />- if file path relative: make absolute path based on current working
     * directory <br />- are removed from the list <br />- and then added to the
     * beginning of the list (file path: if it exists) <br />as entry 1 after
     * the current bundlepath (entry 0)
     *
     * @param pl absolute or relative paths or urls as string array
     */
    public static String addImagePathFirst(String[] pl) {
        return addImagePath(pl, true);
    }

    /**
     * the given path(s) <br />- might be a file path or an url (http/https) <br
     * />- if file path relative: make absolute path based on current working
     * directory <br />- is/are removed from the list
     *
     * @param path absolute or relative path(s) or url(s) might be a path list
     * string with seperator : (or ; Windows)
     */
    public static void removeImagePath(String path) {
        String pl[] = splitImagePath(path);
        pathList.set(0, null);
        removeImagePath(pl);
        pathList.set(0, Settings.BundlePath + File.separator);
    }

    /**
     * the given path(s) <br />- might be a file path or an url (http/https) <br
     * />- if file path relative: make absolute path based on current working
     * directory <br />- is/are removed from the list
     *
     * @param pl absolute or relative path(s) or url(s) as string array
     */
    public static void removeImagePath(String[] pl) {
        for (int i = 0; i < pl.length; i++) {
            if (pl[i] != null) {
                pathList.remove(pl[i]);
            }
        }
    }

    private static void clearImagePath() {
        Iterator<String> ip = pathList.listIterator(1);
        String p;
        while (ip.hasNext()) {
            p = ip.next();
            if (!p.substring(0, p.length() - 1).endsWith(".sikuli")) {
                ip.remove();
            }
        }
        if (firstEntries == pathList.size()) {
            addImagePath(System.getenv("SIKULI_IMAGE_PATH"));
            addImagePath(System.getProperty("SIKULI_IMAGE_PATH"));
        } else {
            addImagePathFirst(System.getProperty("SIKULI_IMAGE_PATH"));
            addImagePathFirst(System.getenv("SIKULI_IMAGE_PATH"));
        }
    }

    /**
     * the current list is emptied <br />then add -DSIKULI_IMAGE_PATH=... and
     * Env(SIKULI_IMAGE_PATH)
     * <br /> then the given path(s) are added using addImagePath()
     *
     * @param path absolute or relative path(s) or url(s) might be a path list
     * string with seperator : (or ; Windows)
     */
    public static void resetImagePath(String path) {
        clearImagePath();
        String pl[] = splitImagePath(path);
        if (pl.length > 0) {
            pathList.set(0, pl[0]);
            Settings.BundlePath = pl[0].substring(0, pl[0].length() - 1);
            pl[0] = null;
            addImagePath(pl);
        }
    }

    /**
     * the current list is emptied <br />then add -DSIKULI_IMAGE_PATH=... and
     * Env(SIKULI_IMAGE_PATH)
     * <br /> then the given path(s) are added using addImagePath()
     *
     * @param pl absolute or relative path(s) or url(s) as string array
     */
    public static void resetImagePath(String[] pl) {
        clearImagePath();
        addImagePath(pl);
    }

    /**
     * the given path is added to the list replacing the first entry and
     * Settings.BundlePath is replaced as well
     *
     * @param bundlePath a path string relative, absolute, empty or null
     */
    public static void setBundlePath(String bundlePath) {
        String pl[] = splitImagePath(bundlePath);
        if (pl.length > 0) {
            pathList.set(0, pl[0]);
            Settings.BundlePath = pl[0].substring(0, pl[0].length() - 1);
        }
    }

    /**
     *
     * @return the current bundle path
     */
    public static String getBundlePath() {
        return pathList.get(0);
    }

    private static String searchFile(String filename) {
        File f;
        String ret;
        for (Iterator<String> it = pathList.iterator(); it.hasNext();) {
            String path = it.next();
            URL url = getURL(path);
            if (url != null) {
                try {
                    ret = getFileFromURL(new URL(url, filename));
                    if (ret != null) {
                        return ret;
                    }
                } catch (MalformedURLException ex) {
                }
            }
            f = new File(path, filename);
            if (f.exists()) {
                Debug.log(3, "ImageLocator: found " + filename + " in " + path);
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    private static String getFileFromURL(URL url) {
        if (_cache_dir_global == null) {
            Debug.error("ImageLocator.getFileFromURL: Local cache dir not available - cannot download from url" + url);
            return null;
        }
        try {
            URI uri = url.toURI();
            if (_cache.containsKey(uri)) {
                Debug.log(2, "ImageLocator.getFileFromURL: " + uri + " taken from cache");
                return _cache.get(uri);
            }
            String localFile = FileManager.downloadURL(url, _cache_dir_global.getPath());
            if (localFile != null) {
                Debug.log(2, "ImageLocator.getFileFromURL: download " + uri + " to local: " + localFile);
                _cache.put(uri, localFile);
            }
            return localFile;
        } catch (java.net.URISyntaxException e) {
            Debug.log(2, "ImageLocator.getFileFromURL: URI syntax error: " + url + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * findX the file in the following order: <br />1. absolute path or url x)
     * <br />2. bundle path <br />3. ENV[SIKULI_IMAGE_PATH] <br />4. Java
     * -DSIKULI_IMAGE_PATH <br />5. paths added later using addImagePath or via
     * import ... .sikuli
     *
     * @param filename relative, absolute or url
     * @return absolute path string
     * @throws IOException if filename cannot be located <br />x) files behind
     * urls are loaded to local cash
     */
    private static String locate(String filename) throws IOException {
        if (filename != null) {
            String ret;
            URL url = getURL(filename);
            if (url != null) {
                ret = getFileFromURL(url);
                if (ret != null) {
                    return ret;
                }
            }
            File f = new File(filename);
            if (f.isAbsolute()) {
                if (f.exists()) {
                    return f.getAbsolutePath();
                }
            } else {
                ret = searchFile(filename);
                if (ret != null) {
                    return ret;
                }
            }
        } else {
            filename = "*** not known ***";
        }
        throw new FileNotFoundException("ImageLocator.locate: " + filename + " does not exist or cannot be found on ImagePath");
    }

    /**
     * tries to findX the file using locate(filename) and loads it as image if
     * possible
     *
     * @param filename
     * @return image
     */
    public static BufferedImage getImage(String filename) {
        return Image.create(filename).get();
    }
}
