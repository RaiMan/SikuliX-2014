/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Debug;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * stores a BufferedImage usually ceated by screen capture,
 * the screen rectangle it was taken from and
 * the filename, where it is stored as PNG (only if requested)
 *
 * @author RaiMan
 */
public class ScreenImage {

	/**
	 * x, y, w, h of the stored ROI
	 *
	 */
	public int x, y, w, h;
	protected Rectangle _roi;
	protected BufferedImage _img;
	protected String _filename = null;

	/**
	 * create ScreenImage with given
	 *
	 * @param roi the rectangle it was taken from
	 * @param img the BufferedImage
	 */
	public ScreenImage(Rectangle roi, BufferedImage img) {
		_img = img;
		_roi = roi;
		x = (int) roi.getX();
		y = (int) roi.getY();
		w = (int) roi.getWidth();
		h = (int) roi.getHeight();
	}

	/**
	 * creates the PNG tempfile only when needed.
	 *
	 * @return absolute path to stored tempfile
	 * @throws IOException
	 * @deprecated use getFile() instead
	 */
	@Deprecated
	public String getFilename() throws IOException {
		return getFile();
	}

	/**
	 * stores the image as PNG file in the standard temp folder
	 * with a created filename (sikuli-image-#unique-random#.png)
	 * if not yet stored before
	 *
	 * @return absolute path to stored file
	 * @throws IOException
	 */
	public String getFile() {
    if (_filename == null) {
      _filename = FileManager.saveTmpImage(_img);
    }
    return _filename;
  }

	/**
	 * stores the image as PNG file in the given path
	 * with a created filename (sikuli-image-#unique-random#.png)
	 *
	 * @param path valid path string
	 * @return absolute path to stored file
	 * @throws IOException
	 */
  public String getFile(String path) {
    try {
      File tmp = File.createTempFile("sikuli-image-", ".png", new File(path));
      createFile(tmp);
    } catch (IOException iOException) {
      Debug.error("ScreenImage.getFile: IOException", iOException);
      return null;
    }
    return _filename;
  }

	/**
	 * stores the image as PNG file in the given path
	 * with the given filename
	 *
	 * @param path valid path string
	 * @param name filename (.png is added if not present)
	 * @return absolute path to stored file
	 * @throws IOException
	 */
	public String getFile(String path, String name) {
    if (name == null) {
      name = Settings.getTimestamp() + ".png";
    } else if (!name.endsWith(".png")) {
			name += ".png";
		}
    try {
      File tmp = new File(path, name);
      createFile(tmp);
    } catch (IOException iOException) {
      Debug.error("ScreenImage.getFile: IOException", iOException);
      return null;
    }
		return _filename;
	}

	// store image to given path if not yet stored
	private void createFile(File tmp) throws IOException {
		String filename = tmp.getAbsolutePath();
		if (_filename == null || !filename.equals(_filename)) {
			ImageIO.write(_img, "png", tmp);
			_filename = filename;
		}
	}

	/**
	 *
	 * @return the stored image in memory
	 */
	public BufferedImage getImage() {
		return _img;
	}

	/**
	 *
	 * @return the screen rectangle, the iamge was created from
	 */
	public Rectangle getROI() {
		return _roi;
	}
}
