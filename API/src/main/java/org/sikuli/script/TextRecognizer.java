/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.image.BufferedImage;
import org.sikuli.basics.Settings;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Debug;
import org.sikuli.basics.ResourceLoader;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import org.sikuli.natives.Mat;
import org.sikuli.natives.OCRWord;
import org.sikuli.natives.OCRWords;
import org.sikuli.natives.Vision;

/**
 * INTERNAL USE
 *
 * Will be rewritten for use of Tess4J - Java only implementation
 */
public class TextRecognizer {

  private static TextRecognizer _instance = null;
  private static boolean _init_succeeded = false;
	private static int lvl = 3;

  static {
    FileManager.loadLibrary("VisionProxy");
  }

  private TextRecognizer() {
    init();
  }

  private void init() {
    String path;
    File fpath = null;
    if (Settings.OcrDataPath != null) {
      path = FileManager.slashify(Settings.OcrDataPath, true);
      fpath = new File(path, "tessdata");
      if (!fpath.exists()) {
        ResourceLoader.get().export("META-INF#libs/tessdata/", fpath.getAbsolutePath());
      }
      if (!fpath.exists()) {
				Debug.log(lvl, "Trying to download Tesseract language pack eng, "
								+ "since not available yet at:\n%s", Settings.OcrDataPath);
			}
      if (!fpath.exists()) {
        Debug.error("TextRecognizer not working: tessdata folder not available at %s\n", path);
        Settings.OcrTextRead = false;
        Settings.OcrTextSearch = false;
        fpath = null;
      }
			if (fpath != null) {
				Vision.initOCR(FileManager.slashify(Settings.OcrDataPath, true));
				_init_succeeded = true;
				Debug.log(lvl, "TextRecognizer: init OK: using as data folder:\n" + fpath.getAbsolutePath());
			}
    } else {
			Debug.error("TextRecognizer: init: not possible: Settings.OcrDataPath not set");
		}
  }

  public static TextRecognizer getInstance() {
    if (_instance == null) {
      _instance = new TextRecognizer();
    }
    if (!_init_succeeded) {
      return null;
    }
    return _instance;
  }

	public static void reset() {
		_instance = null;
		Vision.setSParameter("OCRLang", Settings.OcrLanguage);
	}

  public enum ListTextMode {
    WORD, LINE, PARAGRAPH
  };

  public List<Match> listText(ScreenImage simg, Region parent) {
    return listText(simg, parent, ListTextMode.WORD);
  }

  //TODO: support LINE and PARAGRAPH
  // listText only supports WORD mode now.
  public List<Match> listText(ScreenImage simg, Region parent, ListTextMode mode) {
    Mat mat = Image.convertBufferedImageToMat(simg.getImage());
    OCRWords words = Vision.recognize_as_ocrtext(mat).getWords();
    List<Match> ret = new LinkedList<Match>();
    for (int i = 0; i < words.size(); i++) {
      OCRWord w = words.get(i);
      Match m = new Match(parent.x + w.getX(), parent.y + w.getY(), w.getWidth(), w.getHeight(),
              w.getScore(), parent.getScreen(), w.getString());
      ret.add(m);
    }
    return ret;
  }

  public String recognize(ScreenImage simg) {
    BufferedImage img = simg.getImage();
    return recognize(img);
  }

  public String recognize(BufferedImage img) {
    if (_init_succeeded) {
      Mat mat = Image.convertBufferedImageToMat(img);
      return Vision.recognize(mat).trim();
    } else {
      return "";
    }
  }

  public String recognizeWord(ScreenImage simg) {
    BufferedImage img = simg.getImage();
    return recognizeWord(img);
  }

  public String recognizeWord(BufferedImage img) {
    if (_init_succeeded) {
      Mat mat = Image.convertBufferedImageToMat(img);
      return Vision.recognizeWord(mat).trim();
    } else {
      return "";
    }
  }
}
