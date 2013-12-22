/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import org.sikuli.natives.OSUtil;
import org.sikuli.natives.SysUtil;

public class App {

  protected static final OSUtil _osUtil = SysUtil.getOSUtil();
  protected String _appName;
  protected int _pid;

  static {
//TODO Sikuli hangs if App is used before Screen
    new Screen();
  }
  
  private static Region asRegion(Rectangle r) {
    if (r != null) {
      return Region.create(r);
    } else {
      return null;
    }
  }

  public App(String appName) {
    _appName = appName;
    _pid = 0;
  }

  protected App(String appName, int pid) {
    _appName = appName;
    _pid = pid;
  }

  public static App open(String appName) {
    return (new App(appName)).open();
  }

  public static int close(String appName) {
    return _osUtil.closeApp(appName);
  }

  public static App focus(String appName) {
    return (new App(appName)).focus();
  }

  public static App focus(String appName, int num) {
    return (new App(appName)).focus(num);
  }

  public App focus() {
    return focus(0);
  }

  public App focus(int num) {
    Debug.history("App.focus " + this.toString() + " #" + num);
    if (_pid != 0) {
      if (_osUtil.switchApp(_pid, num) == 0) {
        Debug.error("App.focus failed: " + _appName
                + "(" + _pid + ") not found");
        return null;
      }
    } else {
      boolean failed = false;
      if (Settings.isWindows()) {
        _pid = _osUtil.switchApp(_appName, num);
        if (_pid == 0) {
          failed = true;
        }
      } else {
        if (_osUtil.switchApp(_appName, num) < 0) {
          failed = true;
        }
      }
      if (failed) {
        Debug.error("App.focus failed: " + _appName + " not found");
        return null;
      }
    }
    return this;
  }

  public App open() {
    if (Settings.isWindows() || Settings.isLinux()) {
      int pid = _osUtil.openApp(_appName);
      _pid = pid;
      Debug.history("App.open " + this.toString());
      if (pid == 0) {
        Debug.error("App.open failed: " + _appName + " not found");
        return null;
      }
    } else {
      Debug.history("App.open " + this.toString());
      if (_osUtil.openApp(_appName) < 0) {
        Debug.error("App.open failed: " + _appName + " not found");
        return null;
      }
    }
    return this;
  }

  public int close() {
    Debug.history("App.close " + this.toString());
    if (_pid != 0) {
      int ret = _osUtil.closeApp(_pid);
      if (ret >= 0) {
        return ret;
      }
    }
    return close(_appName);
  }

  public String name() {
    return _appName;
  }

  public Region window() {
    if (_pid != 0) {
      return asRegion(_osUtil.getWindow(_pid));
    }
    return asRegion(_osUtil.getWindow(_appName));
  }

  public Region window(int winNum) {
    if (_pid != 0) {
      return asRegion(_osUtil.getWindow(_pid, winNum));
    }
    return asRegion(_osUtil.getWindow(_appName, winNum));
  }

  public static Region focusedWindow() {
    return asRegion(_osUtil.getFocusedWindow());
  }

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

  public static void setClipboard(String text) {
    Clipboard.putText(Clipboard.PLAIN, Clipboard.UTF8,
            Clipboard.BYTE_BUFFER, text);
  }

  @Override
  public String toString() {
    return _appName + "(" + _pid + ")";
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
    * Enumeration for the transfert type property in MIME types (InputStream, CharBuffer, etc.)
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
