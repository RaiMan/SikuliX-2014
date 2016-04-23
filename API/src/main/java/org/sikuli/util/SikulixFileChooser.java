package org.sikuli.util;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.Settings;

public class SikulixFileChooser {
  static final int FILES = JFileChooser.FILES_ONLY;
  static final int DIRS = JFileChooser.DIRECTORIES_ONLY;
  static final int DIRSANDFILES = JFileChooser.FILES_AND_DIRECTORIES;
  static final int SAVE = FileDialog.SAVE;
  static final int LOAD = FileDialog.LOAD;
  Frame _parent;
  boolean accessingAsFile = false;

  public SikulixFileChooser(Frame parent) {
    _parent = parent;
  }

  public SikulixFileChooser(Frame parent, boolean accessingAsFile) {
    _parent = parent;
    this.accessingAsFile = accessingAsFile;
  }

  public File show(String title) {
    File ret = showFileChooser(title, LOAD, DIRSANDFILES);
    return ret;
  }

  public File load() {
    String type = "Sikuli Script (*.sikuli, *.skl)";
    String title = "Open a Sikuli Script";
    File ret = showFileChooser(title, LOAD, DIRS, new GeneralFileFilter("sikuli", type));
    return ret;
  }

  public File save() {
    String type = "Sikuli Script (*.sikuli)";
    String title = "Save a Sikuli Script";    
    File ret = showFileChooser(title, SAVE, DIRS, new GeneralFileFilter("sikuli", type));
    if (isExt(ret.getName(), "skl")) {
      return null;
    }
    return ret;
  }

  public File export() {
    File ret =  showFileChooser("Export as Sikuli packed Script", SAVE, FILES, 
            new GeneralFileFilter("skl", "Sikuli packed Script (*.skl)"));
    return ret;
  }
  
  public File loadImage() {
    return showFileChooser("Open Image File", LOAD, FILES,
              new FileNameExtensionFilter("Image files (jpg, png)", "jpg", "jpeg", "png"));
  }

  private File showFileChooser(String msg, int mode, int selectionMode, Object... filters) {
    if (Settings.isMac() && Settings.isJava7() && selectionMode == DIRS) {
      selectionMode = DIRSANDFILES;
    }
    boolean shouldTraverse = false;
    JFileChooser fchooser = new JFileChooser();
    fchooser.setDialogTitle(msg);
    if (mode == FileDialog.SAVE) {
      fchooser.setDialogType(JFileChooser.SAVE_DIALOG);
    }
    PreferencesUser pref = PreferencesUser.getInstance();
    String last_dir = pref.get("LAST_OPEN_DIR", "");
    if (!last_dir.equals("")) {
      fchooser.setCurrentDirectory(new File(last_dir));
    }
    if (filters.length == 0) {
      fchooser.setAcceptAllFileFilterUsed(true);   
      shouldTraverse = true;
    }
    else {
      fchooser.setAcceptAllFileFilterUsed(false);
      for (Object filter : filters) {
        if (filter instanceof GeneralFileFilter) {
          fchooser.addChoosableFileFilter((GeneralFileFilter) filter);
        } else {
          fchooser.setFileFilter((FileNameExtensionFilter) filter);
          shouldTraverse = true;
        }
      }
    }
    if (shouldTraverse && Settings.isMac()) {
      fchooser.putClientProperty("JFileChooser.packageIsTraversable", "always");
    }
    fchooser.setFileSelectionMode(selectionMode);
    fchooser.setSelectedFile(null);
    if (fchooser.showDialog(_parent, "") != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    File ret = fchooser.getSelectedFile();
    String dir = ret.getParent();
    PreferencesUser.getInstance().put("LAST_OPEN_DIR", dir);
    if (FILES == selectionMode && isExt(ret.getName(), "sikuli")) {
      return null;
    }
    return ret;
  }

  private static boolean isExt(String fName, String givenExt) {
    int i = fName.lastIndexOf('.');
    if (i > 0) {
      if (fName.substring(i + 1).toLowerCase().equals(givenExt)) {
        return true;
      }
      if ("sikuli".equals(givenExt)) {
        if (fName.substring(i + 1).toLowerCase().equals("skl")) {
          return true;
        }
      }
    }
    return false;
  }

  class GeneralFileFilter extends FileFilter implements FilenameFilter {

    private String _ext, _desc;

    public GeneralFileFilter(String ext, String desc) {
      _ext = ext;
      _desc = desc;
    }

    @Override
    public boolean accept(File dir, String fname) {
      return isExt(fname, _ext);
    }

    @Override
    public boolean accept(File f) {
      if (f.isDirectory()) {
        String fname = f.getName();
        if (Settings.isMac() && _desc.contains("*.sikuli") && !isExt(f.getName(), "sikuli")) {
          if (new File(f, fname + ".py").exists()) {
            return true;
          }
        }
      }
      return isExt(f.getName(), _ext);
    }
    
    @Override
    public String getDescription() {
      return _desc;
    }
  }  
}
