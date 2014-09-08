/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.sikuli.basics.Settings;


public class SikuliIDEFileChooser {

	static final int FILES = JFileChooser.FILES_ONLY;
	static final int DIRS = JFileChooser.DIRECTORIES_ONLY;
	static final int DIRSANDFILES = JFileChooser.FILES_AND_DIRECTORIES;
	static final int SAVE = FileDialog.SAVE;
	static final int LOAD = FileDialog.LOAD;
	Frame _parent;
  boolean accessingAsFile = false;

	public SikuliIDEFileChooser(Frame parent) {
		_parent = parent;
	}

	public SikuliIDEFileChooser(Frame parent, boolean accessingAsFile) {
		_parent = parent;
    this.accessingAsFile = accessingAsFile;
	}

	private File showFileChooser(String msg, int mode, GeneralFileFilter[] filters, int selectionMode) {
		if (Settings.isMac()) {
			if ((Settings.isJava7() && selectionMode == DIRS)) {
        if (accessingAsFile) {
          return showJFileChooser(msg, mode, filters, FILES);
        } else {
          return showJFileChooser(msg, mode, filters, DIRS);
        }
			} else if ((Settings.isJava7() && selectionMode == FILES)) {
        return showJFileChooser(msg, mode, filters, FILES);
      } else {
//TODO Mac Java7: FileDialog not taking bundles as files
				FileDialog fd = new FileDialog(_parent, msg, mode);
				for (GeneralFileFilter filter : filters) {
					fd.setFilenameFilter(filter);
				}
				fd.setVisible(true);
				if (fd.getFile() == null) {
					return null;
				}
				return new File(fd.getDirectory(), fd.getFile());
			}
		}
		return showJFileChooser(msg, mode, filters, selectionMode);
	}

	private File showJFileChooser(String msg, int mode, GeneralFileFilter[] filters, int selectionMode) {
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
		fchooser.setAcceptAllFileFilterUsed(false);
		for (GeneralFileFilter filter : filters) {
			fchooser.setFileFilter(filter);
		}
		fchooser.setFileSelectionMode(selectionMode);
		fchooser.setSelectedFile(null);
		if (fchooser.showDialog(_parent, null) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File ret = fchooser.getSelectedFile();
		String dir = ret.getParent();
		PreferencesUser.getInstance().put("LAST_OPEN_DIR", dir);
		return ret;
	}

	public File loadImage() {
		return showFileChooser("Open a Image File", LOAD,
						new GeneralFileFilter[]{
							new GeneralFileFilter("jpg", "JPEG Image Files (*.jpg)"),
							new GeneralFileFilter("png", "PNG Image Files (*.png)")
						}, FILES);
	}

	public File load() {
    String type = "Sikuli source (*.sikuli)";
    String title = "Open a Sikuli Source folder";
    if (accessingAsFile) {
      type = "Sikuli source file (*.sikuli)";
      title = "Open a Sikuli Source file";
    }
		return showFileChooser(title, LOAD,
						new GeneralFileFilter[]{
							new GeneralFileFilter("sikuli", type)
						}, DIRS);
	}

	public File save() {
    String type = "Sikuli source (*.sikuli)";
    String title = "Save a Sikuli Source folder";
    if (accessingAsFile) {
      type = "Sikuli source file (*.sikuli)";
      title = "Save a Sikuli Source file";
    }
		return showFileChooser(title, SAVE,
						new GeneralFileFilter[]{
							new GeneralFileFilter("sikuli", type)
						}, DIRS);
	}

	public File export() {
		return showFileChooser("Export a Sikuli packed source file", SAVE,
						new GeneralFileFilter[]{
							new GeneralFileFilter("skl", "Sikuli packed src (*.skl)")
						}, FILES);
	}
}
class GeneralFileFilter extends FileFilter implements FilenameFilter {

	private String _ext, _desc;

	public GeneralFileFilter(String ext, String desc) {
		_ext = ext;
		_desc = desc;
	}

	@Override
	public boolean accept(File dir, String fname) {
		int i = fname.lastIndexOf('.');
		if (i > 0 && i < fname.length() - 1) {
			String ext = fname.substring(i + 1).toLowerCase();
			if (ext.equals(_ext)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			String ext = s.substring(i + 1).toLowerCase();
			if (ext.equals(_ext)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return _desc;
	}
}
