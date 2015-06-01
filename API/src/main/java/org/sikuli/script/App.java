/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Desktop;
import java.awt.Rectangle;
import org.sikuli.basics.Debug;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Date;
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
  private String appOptions;
  private String appName;
  private String appWindow;
  private int appPID;
  private boolean isImmediate = false;
  private boolean notFound = false;
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
  //<editor-fold defaultstate="collapsed" desc="special app features">
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
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="AppEntry">
  public static class AppEntry {
    public String name;
    public String execName;
    public String options;
    public String window;
    public int pid;
    
    public AppEntry(String theName, String thePID, String theWindow, String theExec, String theOptions) {
      name = theName;
      window = theWindow;
      options = theOptions;
      pid = -1;
      execName = theExec;
      try {
        pid = Integer.parseInt(thePID);
      } catch (Exception ex) {}
    }
  }
  
  public AppEntry makeAppEntry() {
    String name = appName;
    String window = appWindow;
    if (name.isEmpty() && appOptions.isEmpty()) {
      name = appNameGiven;
    }
    if (isImmediate && !window.startsWith("!")) {
      window = "!" + window;
    }
    if (notFound) {
      name = "!" + name;
    }
    return new AppEntry(name, getPID().toString(), window, appNameGiven, appOptions);
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="constructors">
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
    appOptions = "";
    String execName = "";
    if(appNameGiven.startsWith("+")) {
      isImmediate = true;
      appNameGiven = appNameGiven.substring(1);
      appName = appNameGiven;
      String[] parts;
      if (appName.startsWith("\"")) {
        parts = appName.substring(1).split("\"");
        if (parts.length > 1) {
          appOptions = appName.substring(parts[0].length() + 3);
          appName = "\"" + parts[0] +  "\"";
        }
      } else {
        parts = appName.split(" ");
        if (parts.length > 1) {
          appOptions = appName.substring(parts[0].length() + 1);
          appName = parts[0];
        }
      }
      if (appName.startsWith("\"")) {
        execName = appName.substring(1, appName.length()-1);
        appName = new File(execName).getName();
      } else {
        execName = appName;
        appName = new File(appName).getName();
      }
      if (!new File(execName).exists()) {
        appName = "";
        appOptions = "";
        appWindow = "!";
        notFound = true;
        init("!" + appNameGiven);
      }
      Debug.log(3, "App.immediate: %s", appNameGiven);
    } else {
      init(appNameGiven);
    }
    if (appPID > -1) {
      Debug.log(3, "App.create: %s", toStringShort());
    } else {
      if (runTime.runningWindows) {
        if (!isImmediate && appOptions.isEmpty()) {
          int pid = _osUtil.switchto(appNameGiven);
          if (pid > 0) {
            init(pid);
            appWindow = "!" + appNameGiven;
            Debug.log(3, "App.create: %s", toStringShort());
          } else {
            appPID = -1;
            appName = "";
          }
        }
      } else {
        appName = new File(appNameGiven).getName();
        if (runTime.runningMac) {
          appName = appName.replace(".app", "");
        }
      }
    }
  }

  private void init(String name) {
    AppEntry app = _osUtil.getApp(-1, name);
    if (app != null) {
      appName = app.name;
      if (app.options.isEmpty()) {
        appPID = app.pid;
        if (!app.window.contains("N/A")) {
          appWindow = app.window;
          if (notFound) {
            notFound = false;
          }
        }
      } else {
        appOptions = app.options;
        appNameGiven = appName;
      }
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
    AppEntry app =_osUtil.getApp(pid, appName);
    if (app != null) {
      appName = app.name;
      appPID = app.pid;
      if (!app.window.contains("N/A")) {
        appWindow = app.window;
      }
    } else {
      appPID = -1;
    }
  }
  
  private void init() {
    if (appPID > -1) {
      init(appPID);
    } else {
      String name = appName;
      if (name.isEmpty() && appOptions.isEmpty()) name = appNameGiven;
      init(name);
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getter/setter">
  public static void getApps(String name){
    Map<Integer, String[]> theApps = _osUtil.getApps(name);
    int count = 0;
    String[] item; 
    for (Integer pid : theApps.keySet()) {
      item = theApps.get(pid);
      if (pid < 0) {
        pid = -pid;
        Debug.logp("%d:%s (N/A)", pid, item[0]);        
      } else {
        Debug.logp("%d:%s (%s)", pid, item[0], item[1]);
        count++;
      }
    }
    Debug.logp("App.getApps: %d apps (%d having window)", theApps.size(), count);
  }
  
  public static void getApps(){
    getApps(null);
  }
  
  public App setUsing(String options) {
    if (options != null) {
      appOptions = options;
    } else {
      appOptions = "";
    }
    return this;
  }
  
  public Integer getPID() {
    return appPID;
  }

  public String getName() {
    return appName;
  }

  public String getWindow() {
    return appWindow;
  }
  
  public boolean isValid() {
    return !notFound;
  } 
  
  public boolean isRunning() {
    return isRunning(1);
  }
  
  public boolean isRunning(int maxTime) {
    if (!isValid()) {
      return false;
    }
    long wait = -1;
    for (int n = 0; n < maxTime; n++) {
      wait = new Date().getTime();
      int retVal = _osUtil.isRunning(makeAppEntry());
      if (retVal > 0) {
        init();
        break;
      }
      if (n == 0) {
        continue;
      }
      wait = 1000 - new Date().getTime() + wait;
      if (wait > 0) {
        RunTime.pause(wait/1000f);
      }
    }
    return appPID > -1;
  }

  public boolean hasWindow() {
    if (!isValid()) {
      return false;
    }
    init(appName);
    return !getWindow().isEmpty();
  }
  
  @Override
  public String toString() {
    if (!appWindow.startsWith("!")) {
      init();
    }
    return String.format("[%d:%s (%s)] %s", appPID, appName, appWindow, appNameGiven);
  }

  public String toStringShort() {
    return String.format("[%d:%s]", appPID, appName);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="open">
  /**
   * creates an instance for an app with this name and tries to open it
   * @param appName name
   * @return the App instance or null on failure
   */
  public static App open(String appName) {
    return new App("+" + appName).open();
  }

  /**
   * tries to open the app defined by this App instance
   * @return this or null on failure
   */
  public App open() {
    int pid;
    if (isImmediate) {
      pid = _osUtil.open(appNameGiven);
    } else {
      pid = _osUtil.open(makeAppEntry());
      init(pid);
    }
    if (pid < 0) {
      Debug.error("App.open failed: " + appNameGiven + " not found");
      notFound = true;
    } else {
      Debug.action("App.open " + this.toStringShort());
    }
    return this;
  }
  
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="close">
  /**
   * tries to identify a running app with the given name
   * and then tries to close it
   * @param appName name
   * @return 0 for success -1 otherwise
   */
  public static int close(String appName) {
    return new App("+" + appName).close();
  }

  /**
   * tries to close the app defined by this App instance
   * @return this or null on failure
   */
  public int close() {
    if (!isValid()) {
      return 0;
    }
    if (appPID > -1) {
      init(appPID);
    }
    int ret = _osUtil.close(makeAppEntry());
    if (ret > -1) {
      Debug.action("App.close: %s", this.toStringShort());
      appPID = -1;
    } else {
      Debug.error("App.close %s did not work", this);
    }
    return ret;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="focus">
  /**
   * tries to identify a running app with name and
   * if not running tries to open it
   * and tries to make it the foreground application
   * bringing its topmost window to front
   * @param appName name
   * @return the App instance or null on failure
   */
  public static App focus(String appName) {
    return focus(appName, 0);
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
    return (new App("+" + appName)).focus(num);
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
    if (!isValid()) {
      if (!appWindow.startsWith("!")) {
        return this;
      }
    }
    int pid = -1;
    pid = _osUtil.switchto(makeAppEntry(), num);
    if (pid < 0) {
      Debug.error("App.focus failed: " + (num > 0 ? " #" + num : "") + " " + this.toString());
      return null;
    } else {
      Debug.action("App.focus: " + (num > 0 ? " #" + num : "") + " " + this.toStringShort());
      if (appPID < 1) {
        init();
      }
    }
    return this;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="window">
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
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="run">
  public static int lastRunReturnCode = -1;
  public static String lastRunStdout = "";
  public static String lastRunStderr = "";
  public static String lastRunResult = "";
  
  /**
   * the given text is parsed into a String[] suitable for issuing a Runtime.getRuntime().exec(args).
   * quoting is preserved/obeyed. the first item must be an executable valid for the running system.<br>
   * After completion, the following information is available: <br>
   * App.lastRunResult: a string containing the complete result according to the docs of the run() command<br>
   * App.lastRunStdout: a string containing only the output lines that went to stdout<br>
   * App.lastRunStderr: a string containing only the output lines that went to stderr<br>
   * App.lastRunReturnCode: the value, that is returnd as returncode
   * @param cmd the command to run starting with an executable item
   * @return the final returncode of the command execution
   */
  public static int run(String cmd) {
    lastRunResult = runTime.runcmd(cmd);
    String NL = runTime.runningWindows ? "\r\n" : "\n";
    String[] res = lastRunResult.split(NL);
    try {
      lastRunReturnCode = Integer.parseInt(res[0].trim());
    } catch (Exception ex) {}
    lastRunStdout = "";
    lastRunStderr = "";
    boolean isError = false;
    for (int n=1; n < res.length; n++) {
      if (isError) {
        lastRunStderr += res[n] + NL;
        continue;
      }
      if (RunTime.runCmdError.equals(res[n])) {
        isError = true;
        continue;
      }
      lastRunStdout += res[n] + NL;
    }
    return lastRunReturnCode;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="clipboard">
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
//</editor-fold>
}
