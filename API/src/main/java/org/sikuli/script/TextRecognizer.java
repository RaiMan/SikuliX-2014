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
import java.awt.image.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import org.sikuli.natives.Mat;
import org.sikuli.natives.OCRWord;
import org.sikuli.natives.OCRWords;
import org.sikuli.natives.Vision;

// Singleton
public class TextRecognizer {

  protected static TextRecognizer _instance = null;

  static {
    FileManager.loadLibrary("VisionProxy");
  }

  protected TextRecognizer() {
    init();
  }
  boolean _init_succeeded = false;

  public void init() {
    String path;
    File fpath;
    path = FileManager.slashify(Settings.OcrDataPath, true);
    fpath = new File(path, "tessdata");
    if (!fpath.exists()) {
      Settings.OcrDataPath = null;
      Debug.error("TextRecognizer: init: tessdata folder not found at %s", path);
      Settings.OcrTextRead = false;
      Settings.OcrTextSearch = false;
    } else {
      Debug.log(2, "OCR data path: " + path);
      Vision.initOCR(FileManager.slashify(Settings.OcrDataPath, true));
      _init_succeeded = true;
      Debug.log(2, "TextRecognizer: inited.");
    }
  }

  public static TextRecognizer getInstance() {
    if (_instance == null) {
      _instance = new TextRecognizer();
      if (!_instance._init_succeeded ) _instance = null;
    }
    return _instance;
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
