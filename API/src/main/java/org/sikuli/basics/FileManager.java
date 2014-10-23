/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2014
 */
package org.sikuli.basics;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.sikuli.script.Sikulix;

/**
 * INTERNAL USE: Support for accessing files and other ressources
 */
public class FileManager {

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static String me = "FileManager";
  private static String mem = "...";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + mem + ": " + message, args);
  }

  private static void log0(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + message, args);
  }
  //</editor-fold>

  static final int DOWNLOAD_BUFFER_SIZE = 153600;
  private static SplashFrame _progress = null;
  private static final String EXECUTABLE = "#executable";

  /**
   * System.load() the given library module <br>
 from standard places (folder libs or SikulixUtil/libs) in the following order<br>
   * 1. -Dsikuli.Home=<br> 2. Environement SIKULIX_HOME<br>
   * 3. parent folder of sikuli-script.jar (or main jar)<br>
   * 4. folder user's home (user.home)<br>
   * 5. current working dir or parent of current working dir<br>
   * 6. standard installation places of Sikuli
   *
	 * @param libname generic library name (no pre nor post fix)
   */
  public static void loadLibrary(String libname) {
    ResourceLoader.get().check(Settings.SIKULI_LIB);
    ResourceLoader.get().loadLib(libname);
  }

  private static int tryGetFileSize(URL url) {
    HttpURLConnection conn = null;
    try {
      if (getProxy() != null) {
        conn = (HttpURLConnection) url.openConnection(getProxy());
      } else {
        conn = (HttpURLConnection) url.openConnection();
      }
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(30000);
      conn.setRequestMethod("HEAD");
      conn.getInputStream();
      return conn.getContentLength();
    } catch (Exception ex) {
//      log0(-1, "Download: getFileSize: not accessible:\n" + ex.getMessage());
      return -1;
    } finally {
      conn.disconnect();
    }
  }

  public static Proxy getProxy() {
    Proxy proxy = Settings.proxy;
    if (!Settings.proxyChecked) {
      String phost = Settings.proxyName;
      String padr = Settings.proxyIP;
      String pport = Settings.proxyPort;
      InetAddress a = null;
      int p = -1;
      if (phost != null) {
        a = getProxyAddress(phost);
      }
      if (a == null && padr != null) {
        a = getProxyAddress(padr);
      }
      if (a != null && pport != null) {
        p = getProxyPort(pport);
      }
      if (a != null && p > 1024) {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(a, p));
        log0(lvl, "Proxy defined: %s : %d", a.getHostAddress(), p);
      }
      Settings.proxyChecked = true;
      Settings.proxy = proxy;
    }
    return proxy;
  }

  public static boolean setProxy(String pName, String pPort) {
    InetAddress a = null;
    String host = null;
    String adr = null;
    int p = -1;
    if (pName != null) {
      a = getProxyAddress(pName);
      if (a == null) {
        a = getProxyAddress(pName);
        if (a != null) {
          adr = pName;
        }
      } else {
        host = pName;
      }
    }
    if (a != null && pPort != null) {
      p = getProxyPort(pPort);
    }
    if (a != null && p > 1024) {
      log0(lvl, "Proxy stored: %s : %d", a.getHostAddress(), p);
      Settings.proxyChecked = true;
      Settings.proxyName = host;
      Settings.proxyIP = adr;
      Settings.proxyPort = pPort;
      Settings.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(a, p));
      PreferencesUser prefs = PreferencesUser.getInstance();
      prefs.put("ProxyName", (host == null ? "" : host));
      prefs.put("ProxyIP", (adr == null ? "" : adr));
      prefs.put("ProxyPort", ""+p);
      return true;
    }
    return false;
  }

  /**
   * download a file at the given url to a local folder
   *
   * @param url a valid url
   * @param localPath the folder where the file should go (will be created if necessary)
   * @return the absolute path to the downloaded file or null on any error
   */
  public static String downloadURL(URL url, String localPath) {
    String[] path = url.getPath().split("/");
    String filename = path[path.length - 1];
    String targetPath = null;
    int srcLength = 1;
    int srcLengthKB = 0;
    int done;
    int totalBytesRead = 0;
    File fullpath = new File(localPath);
    if (fullpath.exists()) {
      if (fullpath.isFile()) {
        log0(-1, "download: target path must be a folder:" + localPath);
        fullpath = null;
      }
    } else {
      if (!fullpath.mkdirs()) {
        log0(-1, "download: could not create target folder: " + localPath);
        fullpath = null;
      }
    }
    if (fullpath != null) {
      srcLength = tryGetFileSize(url);
			if (srcLength < 0) {
				srcLength = 0;
			}
      srcLengthKB = (int) (srcLength / 1024);
      if (srcLength > 0) {
        log0(lvl, "Downloading %s having %d KB", filename, srcLengthKB);
			} else {
        log0(lvl, "Downloading %s with unknown size", filename);
			}
			fullpath = new File(localPath, filename);
			targetPath = fullpath.getAbsolutePath();
			done = 0;
			if (_progress != null) {
				_progress.setProFile(filename);
				_progress.setProSize(srcLengthKB);
				_progress.setProDone(0);
				_progress.setVisible(true);
			}
			InputStream reader = null;
			try {
				FileOutputStream writer = new FileOutputStream(fullpath);
				if (getProxy() != null) {
					reader = url.openConnection(getProxy()).getInputStream();
				} else {
					reader = url.openConnection().getInputStream();
				}
				byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
				int bytesRead = 0;
				long begin_t = (new Date()).getTime();
				long chunk = (new Date()).getTime();
				while ((bytesRead = reader.read(buffer)) > 0) {
					writer.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
					if (srcLength > 0) {
						done = (int) ((totalBytesRead / (double) srcLength) * 100);
					} else {
						done = (int) (totalBytesRead / 1024);
					}
					if (((new Date()).getTime() - chunk) > 1000) {
						if (_progress != null) {
							_progress.setProDone(done);
						}
						chunk = (new Date()).getTime();
					}
				}
				writer.close();
				log0(lvl, "downloaded %d KB to %s", (int) (totalBytesRead / 1024), targetPath);
				log0(lvl, "download time: %d", (int) (((new Date()).getTime() - begin_t) / 1000));
			} catch (Exception ex) {
				log0(-1, "problems while downloading\n" + ex.getMessage());
				targetPath = null;
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
					}
				}
			}
      if (_progress != null) {
        if (targetPath == null) {
          _progress.setProDone(-1);
        } else {
          if (srcLength <= 0) {
            _progress.setProSize((int) (totalBytesRead / 1024));
          }
          _progress.setProDone(100);
        }
        _progress.closeAfter(3);
        _progress = null;
      }
    }
    return targetPath;
  }

  /**
   * download a file at the given url to a local folder
   *
   * @param url a string representing a valid url
   * @param localPath the folder where the file should go (will be created if necessary)
   * @return the absolute path to the downloaded file or null on any error
   */
  public static String downloadURL(String url, String localPath) {
    URL src = null;
    try {
      src = new URL(url);
    } catch (MalformedURLException ex) {
      log0(-1, "download: bad URL: " + url);
      return null;
    }
    return downloadURL(src, localPath);
  }

  public static String downloadURL(String url, String localPath, JFrame progress) {
    _progress = (SplashFrame) progress;
    return downloadURL(url, localPath);
  }

  public static String downloadURLtoString(String src) {
    URL url = null;
    try {
      url = new URL(src);
    } catch (MalformedURLException ex) {
      log0(-1, "download: bad URL: " + src);
      return null;
    }
    String[] path = url.getPath().split("/");
    String filename = path[path.length - 1];
    String target = "";
    int srcLength = 1;
    int srcLengthKB = 0;
		int totalBytesRead = 0;
		srcLength = tryGetFileSize(url);
		if (srcLength > 0) {
			srcLengthKB = (int) (srcLength / 1024);
			log0(lvl, "Downloading %s having %d KB", filename, srcLengthKB);
			InputStream reader = null;
			try {
				if (getProxy() != null) {
					reader = url.openConnection(getProxy()).getInputStream();
				} else {
					reader = url.openConnection().getInputStream();
				}
          byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
          int bytesRead = 0;
		      while ((bytesRead = reader.read(buffer)) > 0) {
            totalBytesRead += bytesRead;
						target += (new String(Arrays.copyOfRange(buffer, 0, bytesRead), StandardCharsets.UTF_8));
          }
			} catch (Exception ex) {
				log0(-1, "problems while downloading\n" + ex.getMessage());
				target= null;
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
					}
				}
			}
    }
    return target;
  }

  /**
   * open the given url in the standard browser
   *
   * @param url string representing a valid url
   * @return false on error, true otherwise
   */
  public static boolean openURL(String url) {
    try {
      URL u = new URL(url);
      Desktop.getDesktop().browse(u.toURI());
    } catch (Exception ex) {
      log0(-1, "show in browser: bad URL: " + url);
      return false;
    }
    return true;
  }

  public static File createTempDir() {
    Random rand = new Random();
    int randomInt = 1 + rand.nextInt();

    File tempDir = new File(Settings.BaseTempPath + File.separator + "tmp-" + randomInt + ".sikuli");
    if (tempDir.exists() == false) {
      tempDir.mkdirs();
    }

    tempDir.deleteOnExit();

    log0(lvl, "tempdir create:\n%s", tempDir);

    return tempDir;
  }

  public static void deleteTempDir(String path) {
    if (!deleteFileOrFolder(path)) {
      log0(-1, "deleteTempDir: not possible");
    }
  }

  public static boolean deleteFileOrFolder(String path, FileFilter filter) {
		log0(lvl, "deleteFileOrFolder: %s\n%s", (filter == null ? "" : "filtered: "), path);
    return doDeleteFileOrFolder(path, filter);
	}

    public static boolean deleteFileOrFolder(String path) {
		log0(lvl, "deleteFileOrFolder: %s", path);
    return doDeleteFileOrFolder(path, null);
  }

	private static boolean doDeleteFileOrFolder(String path, FileFilter filter) {
    File entry = new File(path);
    File f;
    String[] entries;
    boolean somethingLeft = false;
    if (entry.isDirectory()) {
      entries = entry.list();
      for (int i = 0; i < entries.length; i++) {
        f = new File(entry, entries[i]);
        if (filter != null && !filter.accept(f)) {
          somethingLeft = true;
          continue;
        }
        if (f.isDirectory()) {
          if (!doDeleteFileOrFolder(f.getAbsolutePath(), filter)) {
            return false;
          }
        } else {
          try {
            f.delete();
          } catch (Exception ex) {
            log0(-1, "deleteFileOrFolder: " + f.getAbsolutePath() + "\n" + ex.getMessage());
            return false;
          }
        }
      }
    }
    // deletes intermediate empty directories and finally the top now empty dir
    if (!somethingLeft && entry.exists()) {
      try {
        entry.delete();
      } catch (Exception ex) {
        log0(-1, "deleteFileOrFolder: " + entry.getAbsolutePath() + "\n" + ex.getMessage());
        return false;
      }
    }
    return true;
  }

  public static File createTempFile(String suffix) {
    return createTempFile(suffix, null);
  }

  public static File createTempFile(String suffix, String path) {
    String temp1 = "sikuli-";
    String temp2 = "." + suffix;
    File fpath = new File(Settings.BaseTempPath);
    if (path != null) {
      fpath = new File(path);
    }
    try {
      fpath.mkdirs();
      File temp = File.createTempFile(temp1, temp2, fpath);
      temp.deleteOnExit();
      log0(lvl, "tempfile create:\n%s", temp.getAbsolutePath());
      return temp;
    } catch (IOException ex) {
      log0(-1, "createTempFile: IOException: %s\n%s", ex.getMessage(),
              fpath + File.separator + temp1 + "12....56" + temp2);
      return null;
    }
  }

  public static String saveTmpImage(BufferedImage img) {
    return saveTmpImage(img, null);
  }

  public static String saveTmpImage(BufferedImage img, String path) {
    File tempFile;
    try {
      tempFile = createTempFile("png", path);
      if (tempFile != null) {
        ImageIO.write(img, "png", tempFile);
        return tempFile.getAbsolutePath();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void unzip(String zip, String path)
          throws IOException, FileNotFoundException {
    final int BUF_SIZE = 2048;
    FileInputStream fis = new FileInputStream(zip);
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
      int count;
      byte data[] = new byte[BUF_SIZE];
      FileOutputStream fos = new FileOutputStream(
              new File(path, entry.getName()));
      BufferedOutputStream dest = new BufferedOutputStream(fos, BUF_SIZE);
      while ((count = zis.read(data, 0, BUF_SIZE)) != -1) {
        dest.write(data, 0, count);
      }
      dest.close();
    }
    zis.close();
  }

  public static void xcopy(String src, String dest) throws IOException {
		doXcopy(new File(src), new File(dest), null);
	}

  public static void xcopy(String src, String dest, FileFilter filter) throws IOException {
		doXcopy(new File(src), new File(dest), filter);
	}

  private static void doXcopy(File fSrc, File fDest, FileFilter filter) throws IOException {
    if (fSrc.getAbsolutePath().equals(fDest.getAbsolutePath())) {
      return;
    }
    if (fSrc.isDirectory()) {
			if (filter == null || filter.accept(fSrc)) {
				if (!fDest.exists()) {
					fDest.mkdirs();
				}
				String[] children = fSrc.list();
				for (String child : children) {
					if (child.equals(fDest.getName())) {
						continue;
					}
					doXcopy(new File(fSrc, child), new File(fDest, child), filter);

				}
			}
		} else {
			if (filter == null || filter.accept(fSrc)) {
				if (fDest.isDirectory()) {
					fDest = new File(fDest, fSrc.getName());
				}
				InputStream in = new FileInputStream(fSrc);
				OutputStream out = new FileOutputStream(fDest);
				// Copy the bits from instream to outstream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
		}
  }

  /**
   * Copy a file *src* to the path *dest* and check if the file name conflicts. If a file with the
   * same name exists in that path, rename *src* to an alternative name.
	 * @param src source file
	 * @param dest destination path
	 * @return the destination file if ok, null otherwise
	 * @throws java.io.IOException on failure
   */
  public static File smartCopy(String src, String dest) throws IOException {
    File fSrc = new File(src);
    String newName = fSrc.getName();
    File fDest = new File(dest, newName);
    if (fSrc.equals(fDest)) {
      return fDest;
    }
    while (fDest.exists()) {
      newName = getAltFilename(newName);
      fDest = new File(dest, newName);
    }
    xcopy(src, fDest.getAbsolutePath());
    if (fDest.exists()) {
      return fDest;
    }
    return null;
  }

  public static String convertStreamToString(InputStream is) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return sb.toString();
  }

  public static String getAltFilename(String filename) {
    int pDot = filename.lastIndexOf('.');
    int pDash = filename.lastIndexOf('-');
    int ver = 1;
    String postfix = filename.substring(pDot);
    String name;
    if (pDash >= 0) {
      name = filename.substring(0, pDash);
      ver = Integer.parseInt(filename.substring(pDash + 1, pDot));
      ver++;
    } else {
      name = filename.substring(0, pDot);
    }
    return name + "-" + ver + postfix;
  }

  public static boolean exists(String path) {
    File f = new File(path);
    return f.exists();
  }

  public static void mkdir(String path) {
    File f = new File(path);
    if (!f.exists()) {
      f.mkdirs();
    }
  }

  public static String getName(String filename) {
    File f = new File(filename);
    return f.getName();
  }

  public static String slashify(String path, Boolean isDirectory) {
    if (path != null) {
      if (path.contains("%")) {
        try {
          path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception ex) {
					log0(lvl, "slashify: decoding problem with %s\nwarning: filename might not be useable.", path);
        }
      }
      if (File.separatorChar != '/') {
        path = path.replace(File.separatorChar, '/');
      }
      if (isDirectory != null) {
        if (isDirectory) {
          if (!path.endsWith("/")) {
            path = path + "/";
          }
        } else if (path.endsWith("/")) {
          path = path.substring(0, path.length() - 1);
        }
      }
			if (path.startsWith("./")) {
				path = path.substring(2);
			}
      return path;
    } else {
      return "";
    }
  }

	public static String normalize(String filename) {
		return slashify(filename, false);
	}

	public static String normalizeAbsolute(String filename) {
		return slashify(new File(slashify(filename, false)).getAbsolutePath(), false);
	}

	public static boolean isFilenameDotted(String name) {
		String nameParent = new File(name).getParent();
		if (nameParent != null && nameParent.contains(".")) {
			return true;
		}
		return false;
	}

  /**
   * Returns the directory that contains the images used by the ScriptRunner.
   *
   * @param scriptFile The file containing the script.
   * @return The directory containing the images.
   */
  public static File resolveImagePath(File scriptFile) {
    if (!scriptFile.isDirectory()) {
      return scriptFile.getParentFile();
    }
    return scriptFile;
  }

  public static URL makeURL(String fName) {
    return makeURL(fName, "file");
  }

  public static URL makeURL(String fName, String type) {
    try {
			fName = normalizeAbsolute(fName);
			if ("jar".equals(type)) {
				if (!fName.contains("!/")) {
					fName += "!/";
				}
				if (!fName.startsWith("file://")) {
          fName = new URL("file", null, fName).toString();
        }
				return new URL("jar:" + fName);
			}
      return new URL(type, null, fName);
    } catch (MalformedURLException ex) {
      return null;
    }
  }

  public static URL makeURL(URL path, String fName) {
    try {
			if ("file".equals(path.getProtocol())) {
				return new URL("file", null, new File(path.getPath(), fName).getAbsolutePath());
			} else if ("jar".equals(path.getProtocol())) {
				String jp = path.getPath();
				if (!jp.contains("!/")) {
					jp += "!/";
				}
				String jpu = "jar:" + jp + fName;
				return new URL(jpu);
			}
      return new URL(path, slashify(fName, false));
    } catch (MalformedURLException ex) {
      return null;
    }
  }

  public static URL getURLForContentFromURL(URL path, String fName) {
    String type = path.getProtocol();
    URL u = makeURL(new File(path.getPath(), slashify(fName, false)).getPath(), path.getProtocol());
    try {
      u.getContent();
      return u;
    } catch (IOException ex) {
      return null;
    }
  }

	public static boolean checkJarContent(String jarPath, String jarContent) {
		URL jpu = makeURL(jarPath, "jar");
		if (jpu != null && jarContent != null) {
			jpu = makeURL(jpu, jarContent);
		}
		if (jpu != null) {
			try {
			  jpu.getContent();
				return true;
			} catch (IOException ex) {
        ex.getMessage();
			}
		}
		return false;
	}

  public static int getPort(String p) {
    int port;
    int pDefault = 50000;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return -1;
      }
    } else {
      return pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  public static int getProxyPort(String p) {
    int port;
    int pDefault = 8080;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return -1;
      }
    } else {
      return pDefault;
    }
    return port;
  }

  public static String getAddress(String arg) {
    try {
      if (arg == null) {
        return InetAddress.getLocalHost().getHostAddress();
      }
      return InetAddress.getByName(arg).getHostAddress();
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  public static InetAddress getProxyAddress(String arg) {
    try {
      return InetAddress.getByName(arg);
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  public static String saveImage(BufferedImage img, String filename, String bundlePath) {
    final int MAX_ALT_NUM = 3;
    String fullpath = bundlePath;
    File path = new File(fullpath);
    if (!path.exists()) {
      path.mkdir();
    }
    if (!filename.endsWith(".png")) {
      filename += ".png";
    }
    File f = new File(path, filename);
    int count = 0;
    String msg = f.getName() + " exists - using ";
    while (count < MAX_ALT_NUM) {
      if (f.exists()) {
        f = new File(path, FileManager.getAltFilename(f.getName()));
      } else {
        if (count > 0) {
          Debug.log(msg + f.getName() + " (Utils.saveImage)");
        }
        break;
      }
      count++;
    }
    if (count >= MAX_ALT_NUM) {
      f = new File(path, Settings.getTimestamp() + ".png");
      Debug.log(msg + f.getName() + " (Utils.saveImage)");
    }
    fullpath = f.getAbsolutePath();
    fullpath = fullpath.replaceAll("\\\\", "/");
    try {
      ImageIO.write(img, "png", new File(fullpath));
    } catch (IOException e) {
      Debug.error("Util.saveImage: Problem trying to save image file: %s\n%s", fullpath, e.getMessage());
      return null;
    }
    return fullpath;
  }

  //TODO consolidate with FileManager and Settings
  public static void zip(String path, String outZip) throws IOException, FileNotFoundException {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outZip));
    zipDir(path, zos);
    zos.close();
  }

  private static void zipDir(String dir, ZipOutputStream zos) throws IOException {
    File zipDir = new File(dir);
    String[] dirList = zipDir.list();
    byte[] readBuffer = new byte[1024];
    int bytesIn;
    for (int i = 0; i < dirList.length; i++) {
      File f = new File(zipDir, dirList[i]);
      if (f.isFile()) {
        FileInputStream fis = new FileInputStream(f);
        ZipEntry anEntry = new ZipEntry(f.getName());
        zos.putNextEntry(anEntry);
        while ((bytesIn = fis.read(readBuffer)) != -1) {
          zos.write(readBuffer, 0, bytesIn);
        }
        fis.close();
      }
    }
  }

	public static void deleteNotUsedImages(String bundle, Set<String> usedImages) {
		File scriptFolder = new File(bundle);
		if (!scriptFolder.isDirectory()) {
			return;
		}
		String path;
		for (File image : scriptFolder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if ((name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
							return true;
						}
						return false;
					}
				})) {
			if (!usedImages.contains(image.getName())) {
				Debug.log(3, "FileManager: delete not used: %s", image.getName());
				image.delete();
			}
		}
	}

  /**
   * INTERNAL USE
   */
  public static void cleanTemp() {
    for (File f : new File(System.getProperty("java.io.tmpdir")).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.contains("BridJExtractedLibraries")) {
          return true;
        }
        if (name.toLowerCase().contains("sikuli")) {
          return true;
        }
        return false;
      }
    })) {
      Debug.log(4, "cleanTemp: " + f.getName());
      FileManager.deleteFileOrFolder(f.getAbsolutePath());
    }
  }

	public static boolean isBundle(String dir) {
		return dir.endsWith(".sikuli");
	}

//  public static IResourceLoader getNativeLoader(String name, String[] args) {
//    if (nativeLoader != null) {
//      return nativeLoader;
//    }
//    IResourceLoader nl = null;
//    ServiceLoader<IResourceLoader> loader = ServiceLoader.load(IResourceLoader.class);
//    Iterator<IResourceLoader> resourceLoaderIterator = loader.iterator();
//    while (resourceLoaderIterator.hasNext()) {
//      IResourceLoader currentLoader = resourceLoaderIterator.next();
//      if ((name != null && currentLoader.getName().toLowerCase().equals(name.toLowerCase()))) {
//        nl = currentLoader;
//        nl.init(args);
//        break;
//      }
//    }
//    if (nl == null) {
//      log0(-1, "Fatal error 121: Could not load any NativeLoader!");
//      (121);
//    } else {
//      nativeLoader = nl;
//    }
//    return nativeLoader;
//  }
//
  public static String getJarParentFolder() {
    CodeSource src = FileManager.class.getProtectionDomain().getCodeSource();
    String jarParentPath = "--- not known ---";
    String RunningFromJar = "Y";
    if (src.getLocation() != null) {
      String jarPath = src.getLocation().getPath();
      if (!jarPath.endsWith(".jar")) RunningFromJar = "N";
      jarParentPath = FileManager.slashify((new File(jarPath)).getParent(), true);
    } else {
      log(-1, "Fatal Error 101: Not possible to access the jar files!");
      Sikulix.terminate(101);
    }
    return RunningFromJar + jarParentPath;
  }

  public static String getJarPath(Class cname) {
    CodeSource src = cname.getProtectionDomain().getCodeSource();
    if (src.getLocation() != null) {
      return new File(src.getLocation().getPath()).getAbsolutePath();
    }
    return "";
  }

  public static String getJarName(Class cname) {
		String jp = getJarPath(cname);
		if (jp.isEmpty()) {
			return "";
		}
		return new File(jp).getName();
  }

  public static boolean writeStringToFile(String text, String path) {
    PrintStream out = null;
    try {
      out = new PrintStream(new FileOutputStream(path));
      out.print(text);
    } catch (Exception e) {
      log0(-1,"writeStringToFile: did not work: " + path + "\n" + e.getMessage());
    }
    if (out != null) {
      out.close();
      return true;
    }
    return false;
  }

  public static boolean packJar(String folderName, String jarName, String prefix) {
    jarName = FileManager.slashify(jarName, false);
    if (!jarName.endsWith(".jar")) {
      jarName += ".jar";
    }
    folderName = FileManager.slashify(folderName, true);
    if (!(new File(folderName)).isDirectory()) {
      log0(-1, "packJar: not a directory or does not exist: " + folderName);
      return false;
    }
    try {
      File dir = new File((new File(jarName)).getAbsolutePath()).getParentFile();
      if (dir != null) {
        if (!dir.exists()) {
          dir.mkdirs();
        }
      } else {
        throw new Exception("workdir is null");
      }
      log0(lvl, "packJar: %s from %s in workDir %s", jarName, folderName, dir.getAbsolutePath());
      if (!folderName.startsWith("http://") && !folderName.startsWith("https://")) {
        folderName = "file://" + (new File(folderName)).getAbsolutePath();
      }
      URL src = new URL(folderName);
      JarOutputStream jout = new JarOutputStream(new FileOutputStream(jarName));
      addToJar(jout, new File(src.getFile()), prefix);
      jout.close();
    } catch (Exception ex) {
      log0(-1, "packJar: " + ex.getMessage());
      return false;
    }
    log0(lvl, "packJar: completed");
    return true;
  }

  public static boolean buildJar(String jarName, String[] jars, String[] files, String[] prefixs, FileManager.JarFileFilter filter) {
    log0(lvl, "buildJar: " + jarName);
    try {
      JarOutputStream jout = new JarOutputStream(new FileOutputStream(jarName));
      ArrayList done = new ArrayList();
      for (int i = 0; i < jars.length; i++) {
        if (jars[i] == null) {
          continue;
        }
        log0(lvl, "buildJar: adding: " + jars[i]);
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(jars[i]));
        ZipInputStream zin = new ZipInputStream(bin);
        for (ZipEntry zipentry = zin.getNextEntry(); zipentry != null; zipentry = zin.getNextEntry()) {
          if (filter == null || filter.accept(zipentry, jars[i])) {
            if (!done.contains(zipentry.getName())) {
              jout.putNextEntry(zipentry);
              if (!zipentry.isDirectory()) {
                bufferedWrite(zin, jout);
              }
              done.add(zipentry.getName());
              log0(lvl+2, "adding: " + zipentry.getName());
            }
          }
        }
        zin.close();
        bin.close();
      }
      if (files != null) {
        for (int i = 0; i < files.length; i++) {
          log0(lvl, "buildJar: adding: " + files[i]);
          addToJar(jout, new File(files[i]), prefixs[i]);
        }
      }
      jout.close();
    } catch (Exception ex) {
      log0(-1, "buildJar: " + ex.getMessage());
      return false;
    }
    log0(lvl, "buildJar: completed");
    return true;
  }

  /**
   * unpack a jar file to a folder
   * @param jarName absolute path to jar file
   * @param folderName absolute path to the target folder
   * @param del true if the folder should be deleted before unpack
   * @return true if success,  false otherwise
   */
  public static boolean unpackJar(String jarName, String folderName, boolean del) {
    jarName = FileManager.slashify(jarName, false);
    if (!jarName.endsWith(".jar")) {
      jarName += ".jar";
    }
    if (!new File(jarName).isAbsolute()) {
      log(-1, "unpackJar: jar path not absolute");
      return false;
    }
    if (folderName == null) {
      folderName = jarName.substring(0, jarName.length() - 4);
    } else if (!new File(folderName).isAbsolute()) {
      log(-1, "unpackJar: folder path not absolute");
      return false;
    }
    folderName = FileManager.slashify(folderName, true);
    ZipInputStream in;
    BufferedOutputStream out;
    try {
      if (del) {
        FileManager.deleteFileOrFolder(folderName);
      }
      in = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarName)));
      log0(lvl, "unpackJar: %s to %s", jarName, folderName);
      boolean isExecutable;
      int n;
      File f;
      for (ZipEntry z = in.getNextEntry(); z != null; z = in.getNextEntry()) {
        if (z.isDirectory()) {
          (new File(folderName, z.getName())).mkdirs();
        } else {
          n = z.getName().lastIndexOf(EXECUTABLE);
          if (n >= 0) {
            f = new File(folderName, z.getName().substring(0, n));
            isExecutable = true;
          } else {
            f = new File(folderName, z.getName());
            isExecutable = false;
          }
          f.getParentFile().mkdirs();
          out = new BufferedOutputStream(new FileOutputStream(f));
          bufferedWrite(in, out);
          out.close();
          if (isExecutable) {
            f.setExecutable(true, false);
          }
        }
      }
      in.close();
    } catch (Exception ex) {
      log0(-1, "unpackJar: " + ex.getMessage());
      return false;
    }
    log0(lvl, "unpackJar: completed");
    return true;
  }

  private static void addToJar(JarOutputStream jar, File dir, String prefix) throws IOException {
    File[] content;
    prefix = prefix == null ? "" : prefix;
    if (dir.isDirectory()) {
      content  = dir.listFiles();
      for (int i = 0, l = content.length; i < l; ++i) {
        if (content[i].isDirectory()) {
          jar.putNextEntry(new ZipEntry(prefix + (prefix.equals("") ? "" : "/") + content[i].getName() + "/"));
          addToJar(jar, content[i], prefix + (prefix.equals("") ? "" : "/") + content[i].getName());
        } else {
          addToJarWriteFile(jar, content[i], prefix);
        }
      }
    } else {
      addToJarWriteFile(jar, dir, prefix);
    }
  }

  private static void addToJarWriteFile(JarOutputStream jar, File file, String prefix) throws IOException {
    if (file.getName().startsWith(".")) {
      return;
    }
    String suffix = "";
//TODO buildjar: suffix EXECUTABL
//    if (file.canExecute()) {
//      suffix = EXECUTABLE;
//    }
    jar.putNextEntry(new ZipEntry(prefix + (prefix.equals("") ? "" : "/") + file.getName() + suffix));
    FileInputStream in = new FileInputStream(file);
    bufferedWrite(in, jar);
    in.close();
  }

  public interface JarFileFilter {
    public boolean accept(ZipEntry entry, String jarname);
  }

  public interface FileFilter {
    public boolean accept(File entry);
  }

  private static synchronized void bufferedWrite(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024 * 512];
    int read;
    while (true) {
      read = in.read(buffer);
      if (read == -1) {
        break;
      }
      out.write(buffer, 0, read);
    }
    out.flush();
  }

	/**
	 * compares to path strings using java.io.File.equals()
	 * @param path1 string
	 * @param path2 string
	 * @return true if same file or folder
	 */
	public static boolean pathEquals(String path1, String path2) {
    return (new File(path1)).equals(new File(path2));
  }

	public static boolean checkPrereqs() {
		if (Settings.isLinux()) {
			return checkPrereqsLux();
		} else if (Settings.isWindows()) {
			return true;
		} else if (Settings.isMac()) {
			return true;
		}
		return true;
	}

	public static boolean checkPrereqsLux() {
		return true;
	}
}

