/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Desktop;
import java.awt.Rectangle;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import org.sikuli.natives.OSUtil;
import org.sikuli.natives.SysUtil;

/**
 * App implements features to manage (open, switch to, close) applications.
 * on the system we are running on and
 * to access their assets like windows
 * <br>
 * TAKE CARE: function behavior differs depending on the running system
 * (cosult the docs for more info)
 */
public class App {
  
  static RunTime runTime = RunTime.get();

  private static final OSUtil _osUtil = SysUtil.getOSUtil();
  private String appNameGiven;
  private String appName;
  private String appWindow;
  private int appPID;
  private static final Map<Type, String> appsWindows; 
  private static final Map<Type, String> appsMac;
  private static final Region aRegion = new Region();

  static {
//TODO Sikuli hangs if App is used before Screen
    new Screen();
		String libName = _osUtil.getLibName();
		if (!libName.isEmpty()) {
			RunTime.loadLibrary(libName);
		}
    appsWindows = new HashMap<Type, String>();
    appsWindows.put(Type.EDITOR, "Notepad");
    appsWindows.put(Type.BROWSER, "Google Chrome");
    appsWindows.put(Type.VIEWER, "");
    appsMac = new HashMap<Type, String>();
    appsMac.put(Type.EDITOR, "TextEdit");
    appsMac.put(Type.BROWSER, "Safari");
    appsMac.put(Type.VIEWER, "Preview");
}
  
  public static enum Type {
    EDITOR, BROWSER, VIEWER
  }
    
  public static Region start(Type appType) {
    App app = null;
    Region win;
    try {
      if (Type.EDITOR.equals(appType)) {
        if (runTime.runningMac) {
          app = new App(appsMac.get(appType));
          if (app.window() != null) {
            app.focus();
            aRegion.wait(0.5);
            win = app.window();
            aRegion.click(win);
            aRegion.write("#M.a#B.");
            return win;
          } else {
            app.open();
            win = app.waitForWindow();
            app.focus();
            aRegion.wait(0.5);
            aRegion.click(win);
            return win;
          }
        }      
        if (runTime.runningWindows) {
          app = new App(appsWindows.get(appType));
          if (app.window() != null) {
            app.focus();
            aRegion.wait(0.5);
            win = app.window();
            aRegion.click(win);
            aRegion.write("#C.a#B.");
            return win;
          } else {
            app.open();
            win = app.waitForWindow();
            app.focus();
            aRegion.wait(0.5);
            aRegion.click(win);
            return win;
          }
        }      
      } else if (Type.BROWSER.equals(appType)) {
        if (runTime.runningWindows) {
          app = new App(appsWindows.get(appType));
          if (app.window() != null) {
            app.focus();
            aRegion.wait(0.5);
            win = app.window();
            aRegion.click(win);
//            aRegion.write("#C.a#B.");
            return win;
          } else {
            app.open();
            win = app.waitForWindow();
            app.focus();
            aRegion.wait(0.5);
            aRegion.click(win);
            return win;
          }
        }      
        return null;
      } else if (Type.VIEWER.equals(appType)) {
        return null;
      }
    } catch (Exception ex) {}
    return null;
  }
    
  public Region waitForWindow() {
    return waitForWindow(5);
  }

  public Region waitForWindow(int seconds) {
    Region win = null;
    while ((win = window()) == null && seconds > 0) {
      aRegion.wait(0.5);
      seconds -= 0.5;
    }
    return win;
  }
  
  public static boolean openLink(String url) {
    if (!Desktop.isDesktopSupported()) {
      return false;
    }
    try {
      Desktop.getDesktop().browse(new URI(url));
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  private static Region asRegion(Rectangle r) {
    if (r != null) {
      return Region.create(r);
    } else {
      return null;
    }
  }

  static class AppEntry {
    public String name;
    public String window;
    public int pid;
    
    public AppEntry(String theName, String thePID, String theWindow) {
      name = theName;
      window = theWindow;
      pid = -1;
      try {
        pid = Integer.parseInt(thePID);
      } catch (Exception ex) {}
    }
  }

	/**
	 * creates an instance for an app with this name
	 * (nothing done yet)
	 *
	 * @param name name
	 */
	public App(String name) {
    appNameGiven = name;
    appName = name;
    appPID = -1;
    appWindow = "";
    AppEntry app = getApp(appNameGiven);
    if (app != null) {
      appName = app.name;
      appPID = app.pid;
      appWindow = app.window;
    }
  }

  public App(int pid) {
    appNameGiven = "FromPID";
    appName = "";
    appPID = pid;
    appWindow = "";
    init(pid);
  }
  
  private void init(int pid) {
    AppEntry app = getApp(pid);
    if (app != null) {
      appName = app.name;
      appPID = app.pid;
      appWindow = app.window;
    }    
  }
  
  private static Map<String, AppEntry> getApps() {
    Map<String, AppEntry> apps = new HashMap<String, AppEntry>();
    if (runTime.runningWindows) {
      String cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\"";
      String result = runTime.runcmd(cmd);
      String[] lines = result.split("\r\n");
      if ("0".equals(lines[0].trim())) {
        for (int nl = 1; nl < lines.length; nl++) {
          String[] parts = lines[nl].split("\"");
          String theWindow = parts[parts.length - 1];
          if (theWindow.trim().startsWith("N/A")) {
            continue;
          }
          apps.put(parts[1], new AppEntry(parts[1], parts[3] , theWindow));
        }
      } else {
        Debug.logp(result);
      }
    }
    return apps;
  }
  
  private static AppEntry getApp(Object filter) {
    AppEntry app = null;
    String name = "";
    int pid = -1;
    if (filter instanceof String) {
      name = (String) filter;
    } else if (filter instanceof Integer) {
      pid = (Integer) filter;
    } else {
      return app;
    }
    if (runTime.runningWindows) {
      String cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\"";
      String result = runTime.runcmd(cmd);
      String[] lines = result.split("\r\n");
      if ("0".equals(lines[0].trim())) {
        for (int nl = 1; nl < lines.length; nl++) {
          String[] parts = lines[nl].split("\"");
          String theWindow = parts[parts.length - 1];
          String theName = parts[1];
          String thePID = parts[3];
          if (theWindow.trim().startsWith("N/A")) {
            continue;
          }
          if (!name.isEmpty()) {
            if (theName.toUpperCase().contains(name.toUpperCase()) ||
                    theWindow.contains(name)) {
              return new AppEntry(theName, thePID , theWindow);
            }
          } else {
            try {
              if (Integer.parseInt(thePID) == pid) {
                return new AppEntry(theName, thePID , theWindow);
              }
            } catch (Exception ex) {}
          }
        }
      } else {
        Debug.logp(result);
      }
    }
    return app;
  }
  
  public int getPID() {
    return appPID;
  }
  
  public String getName() {
    return appName;
  }
  
  public String getWindow() {
    return appWindow;
  }
  
	/**
	 * creates an instance for an app with this name and tries to open it
	 * @param appName name
	 * @return the App instance or null on failure
	 */
	public static App open(String appName) {
    App theApp = new App(appName);
    if (theApp.appPID > -1) {
      return theApp;
    }
    return theApp.open();
  }

	/**
	 * tries to open the app defined by this App instance
	 * @return this or null on failure
	 */
	public App open() {
    if (!runTime.runningMac) {
      if (appPID == -1) {
        int pid = _osUtil.open(appNameGiven);
        if (pid == 0) {
          Debug.error("App.open failed: " + appNameGiven + " not found");
          return null;
        }
        appPID = pid;
      }
      init(appPID);
    } else {
      if (runTime.runningMacApp && runTime.osVersion.startsWith("10.10.")) {
        if (Runner.runas(String.format("tell app \"%s\" to activate", appNameGiven)) < 0) {
          return null;
        }
      }
      if (_osUtil.open(appNameGiven) < 0) {
        Debug.error("App.open failed: " + appNameGiven + " not found");
        return null;
      }
    }
    Debug.action("App.open " + this.toString());
    return this;
  }

	/**
	 * tries to identify a running app with the given name
	 * and then tries to close it
	 * @param appName name
	 * @return 0 for success -1 otherwise
	 */
	public static int close(String appName) {
    int ret = _osUtil.close(appName);
    if (ret > -1) {
      Debug.action("App.close " + appName);
    } else {
      Debug.error("App.close %s did not work", appName);
    }
    return ret;
  }

	/**
	 * tries to close the app defined by this App instance
	 * @return this or null on failure
	 */
	public int close() {
    if (appPID > -1) {
      int ret = _osUtil.close(appPID);
      if (ret >= 0) {
        Debug.action("App.close " + this.toString());
        appPID = -1;
        return ret;
      }
    }
    return close(appNameGiven);
  }

	/**
	 * tries to identify a running app with name and
	 * if not running tries to open it
	 * and tries to make it the foreground application
	 * bringing its topmost window to front
	 * @param appName name
	 * @return the App instance or null on failure
	 */
	public static App focus(String appName) {
    return (new App(appName)).focus();
  }

	/**
	 * tries to identify a running app with name and
	 * if not running tries to open it
	 * and tries to make it the foreground application
	 * bringing its window with the given number to front
	 * @param appName name
	 * @param num window
	 * @return the App instance or null on failure
	 */
  public static App focus(String appName, int num) {
    return (new App(appName)).focus(num);
  }

	/**
	 * tries to make it the foreground application
	 * bringing its topmost window to front
	 * @return the App instance or null on failure
	 */
	public App focus() {
    if (appPID > -1) {
      init(appPID);
    }
    return focus(0);
  }

	/**
	 * tries to make it the foreground application
	 * bringing its window with the given number to front
	 * @param num window
	 * @return the App instance or null on failure
	 */
  public App focus(int num) {
    Debug.action("App.focus " + this.toString() + " #" + num);
    if (appPID != 0) {
      if (_osUtil.switchto(appPID, num) == 0) {
        Debug.error("App.focus failed: " + appNameGiven
                + "(" + appPID + ") not found");
        return null;
      }
    } else {
      boolean failed = false;
      if (Settings.isWindows()) {
        appPID = _osUtil.switchto(appNameGiven, num);
        if (appPID == 0) {
          failed = true;
        }
      } else {
        if (_osUtil.switchto(appNameGiven, num) < 0) {
          failed = true;
        }
      }
      if (failed) {
        Debug.error("App.focus failed: " + appNameGiven + " not found");
        return null;
      }
    }
    return this;
  }

	/**
	 * evaluates the region currently occupied
	 * by the topmost window of this App instance.
	 * The region might not be fully visible, not visible at all
	 * or invalid with respect to the current monitor configuration (outside any screen)
	 * @return the region
	 */
	public Region window() {
    if (appPID != 0) {
      return asRegion(_osUtil.getWindow(appPID));
    }
    return asRegion(_osUtil.getWindow(appNameGiven));
  }

	/**
	 * evaluates the region currently occupied
	 * by the window with the given number of this App instance.
	 * The region might not be fully visible, not visible at all
	 * or invalid with respect to the current monitor configuration (outside any screen)
	 * @param winNum window
	 * @return the region
	 */
  public Region window(int winNum) {
    if (appPID != 0) {
      return asRegion(_osUtil.getWindow(appPID, winNum));
    }
    return asRegion(_osUtil.getWindow(appNameGiven, winNum));
  }

	/**
	 * evaluates the region currently occupied by the systemwide frontmost window
	 * (usually the one that has focus for mouse and keyboard actions)
	 * @return the region
	 */
	public static Region focusedWindow() {
    return asRegion(_osUtil.getFocusedWindow());
  }

	/**
	 * evaluates the current textual content of the system clipboard
	 * @return the textual content
	 */
	public static String getClipboard() {
    Transferable content = Clipboard.getSystemClipboard().getContents(null);
    try {
      if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        return (String) content.getTransferData(DataFlavor.stringFlavor);
      }
    } catch (UnsupportedFlavorException e) {
      Debug.error("Env.getClipboard: UnsupportedFlavorException: " + content);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

	/**
	 * sets the current textual content of the system clipboard to the given text
	 * @param text text
	 */
	public static void setClipboard(String text) {
    Clipboard.putText(Clipboard.PLAIN, Clipboard.UTF8,
            Clipboard.CHAR_BUFFER, text);
  }

  @Override
  public String toString() {
    return String.format("%s [%d:%s (%s)]", appNameGiven, appPID, appName, appWindow);
  }

	private static class Clipboard {

   public static final TextType HTML = new TextType("text/html");
   public static final TextType PLAIN = new TextType("text/plain");

   public static final Charset UTF8 = new Charset("UTF-8");
   public static final Charset UTF16 = new Charset("UTF-16");
   public static final Charset UNICODE = new Charset("unicode");
   public static final Charset US_ASCII = new Charset("US-ASCII");

   public static final TransferType READER = new TransferType(Reader.class);
   public static final TransferType INPUT_STREAM = new TransferType(InputStream.class);
   public static final TransferType CHAR_BUFFER = new TransferType(CharBuffer.class);
   public static final TransferType BYTE_BUFFER = new TransferType(ByteBuffer.class);

   private Clipboard() {
   }

   /**
    * Dumps a given text (either String or StringBuffer) into the Clipboard, with a default MIME type
    */
   public static void putText(CharSequence data) {
      StringSelection copy = new StringSelection(data.toString());
      getSystemClipboard().setContents(copy, copy);
   }

   /**
    * Dumps a given text (either String or StringBuffer) into the Clipboard with a specified MIME type
    */
   public static void putText(TextType type, Charset charset, TransferType transferType, CharSequence data) {
      String mimeType = type + "; charset=" + charset + "; class=" + transferType;
      TextTransferable transferable = new TextTransferable(mimeType, data.toString());
      getSystemClipboard().setContents(transferable, transferable);
   }

   public static java.awt.datatransfer.Clipboard getSystemClipboard() {
      return Toolkit.getDefaultToolkit().getSystemClipboard();
   }

   private static class TextTransferable implements Transferable, ClipboardOwner {
      private String data;
      private DataFlavor flavor;

      public TextTransferable(String mimeType, String data) {
         flavor = new DataFlavor(mimeType, "Text");
         this.data = data;
      }

     @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{flavor, DataFlavor.stringFlavor};
      }

     @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
         boolean b = this.flavor.getPrimaryType().equals(flavor.getPrimaryType());
         return b || flavor.equals(DataFlavor.stringFlavor);
      }

     @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
         if (flavor.isRepresentationClassInputStream()) {
            return new StringReader(data);
         }
         else if (flavor.isRepresentationClassReader()) {
            return new StringReader(data);
         }
         else if (flavor.isRepresentationClassCharBuffer()) {
            return CharBuffer.wrap(data);
         }
         else if (flavor.isRepresentationClassByteBuffer()) {
            return ByteBuffer.wrap(data.getBytes());
         }
         else if (flavor.equals(DataFlavor.stringFlavor)){
            return data;
         }
         throw new UnsupportedFlavorException(flavor);
      }

     @Override
      public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
      }
   }

   /**
    * Enumeration for the text type property in MIME types
    */
   public static class TextType {
      private String type;

      private TextType(String type) {
         this.type = type;
      }

     @Override
      public String toString() {
         return type;
      }
   }

   /**
    * Enumeration for the charset property in MIME types (UTF-8, UTF-16, etc.)
    */
   public static class Charset {
      private String name;

      private Charset(String name) {
         this.name = name;
      }

     @Override
      public String toString() {
         return name;
      }
   }

   /**
    * Enumeration for the transferScriptt type property in MIME types (InputStream, CharBuffer, etc.)
    */
   public static class TransferType {
      private Class dataClass;

      private TransferType(Class streamClass) {
         this.dataClass = streamClass;
      }

      public Class getDataClass() {
         return dataClass;
      }

     @Override
      public String toString() {
         return dataClass.getName();
      }
   }

}

}
