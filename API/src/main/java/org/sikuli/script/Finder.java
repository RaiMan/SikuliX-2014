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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import org.sikuli.natives.FindInput;
import org.sikuli.natives.FindResult;
import org.sikuli.natives.FindResults;
import org.sikuli.natives.TARGET_TYPE;
import org.sikuli.natives.Vision;

public class Finder implements Iterator<Match> {

  private Region _region = null;
  private Pattern _pattern = null;
  private FindInput _findInput = new FindInput();
  private FindResults _results = null;
  private int _cur_result_i;
  private boolean repeating = false;
  private boolean isImageFinder = false;

//TODO Vision.setParameter("GPU", 1);
  static {
    FileManager.loadLibrary("VisionProxy");
  }
    
  /**
   * Just to force library initialization
   */
  public Finder() {}

  /**
   * Finder constructor (finding within an image).
   * <br>internally used with a screen snapshot
   *
   * @param imageFilename a string (name, path, url)
   */
  public Finder(String imageFilename) throws IOException {
    this(imageFilename, null);
  }

  /**
   * Finder constructor (finding within an image within the given region).
   * <br>internally used with a screen snapshot
   *
   * @param imageFilename a string (name, path, url)
   * @param region search Region within image - topleft = (0,0)
   */
  public Finder(String imageFilename, Region region) throws IOException  {
    String fname = Image.create(imageFilename).getFilename();
    _findInput.setSource(fname);
    _region = region;
  }

	/**
	 * Constructor for special use from a BufferedImage
	 *
	 * @param bimg
	 */
	public Finder(BufferedImage bimg) {
    _findInput.setSource(Image.convertBufferedImageToMat(bimg));
	}

  /**
	 * Finder constructor for special use from a ScreenImage
	 *
	 * @param simg
	 */
	public Finder(ScreenImage simg) {
		initScreenFinder(simg, null);
  }

  /**
	 * Finder constructor for special use from a ScreenImage
	 *
	 * @param simg
	 * @param region
	 */
	public Finder(ScreenImage simg, Region region) {
		initScreenFinder(simg, region);
  }

  /**
	 * Finder constructor for special use from an Image
	 *
	 * @param simg
	 */
	public Finder(Image simg) {
    _findInput.setSource(Image.convertBufferedImageToMat(simg.get()));
  }

	private void initScreenFinder(ScreenImage simg, Region region) {
		setScreenImage(simg);
    _region = region;
	}

  /**
   * to explicitly free the Finder's resources
   */
  public void destroy() {
		_findInput.delete();
		_findInput = null;
		_results.delete();
		_results = null;
		_pattern = null;
  }

	/**
	 * internal use: exchange the source image in existing Finder
	 *
	 * @param simg
	 */
	protected void setScreenImage(ScreenImage simg) {
    _findInput.setSource(Image.convertBufferedImageToMat(simg.getImage()));
	}

  /**
	 * internal use: to be able to reuse the same Finder
	 */
	protected void setRepeating() {
    repeating = true;
  }

	/**
	 * internal use: repeat findX with same Finder
	 */
	protected void findRepeat() {
		_results = Vision.find(_findInput);
		_cur_result_i = 0;
	}

  /**
   *
   * @param imageOrText
   */
  public String find(String imageOrText) {
		String target = setTargetSmartly(_findInput, imageOrText);
    if (null == target) {
      return null;
    }
		if (target.equals(imageOrText+"???")) {
			return target;
		}
    _findInput.setSimilarity(Settings.MinSimilarity);
    _results = Vision.find(_findInput);
    _cur_result_i = 0;
    return target;
  }

  /**
   * findX given pattern within the stored image
   *
   * @param aPtn
   */
  public String find(Pattern aPtn) {
    if (aPtn.isValid()) {
      _pattern = aPtn;
      _findInput.setTarget(aPtn.getImage().getMatNative());
      _findInput.setSimilarity(aPtn.getSimilar());
      _results = Vision.find(_findInput);
      _cur_result_i = 0;
      return aPtn.getFilename();
    } else {
      return null;
    }
  }
  
  public String find(Image img) {
    if (img.isValid()) {
      _findInput.setTarget(img.getMatNative());
      _findInput.setSimilarity(Settings.MinSimilarity);
      _results = Vision.find(_findInput);
      _cur_result_i = 0;
      return img.getFilename();
    } else {
      return null;
    }
  }
  
  public String findText(String text) {
    _findInput.setTarget(TARGET_TYPE.TEXT, text);
    _results = Vision.find(_findInput);
    _cur_result_i = 0;
    return text;
  }

  /**
	 * internal use: repeat findX with same Finder
	 */
  protected void findAllRepeat() {
    Debug timing = new Debug();
    timing.startTiming("Finder.findAll");
    _results = Vision.find(_findInput);
    _cur_result_i = 0;
    timing.endTiming("Finder.findAll");
	}

  /**
   *
   * @param imageOrText
   */
  public String findAll(String imageOrText) {
		String target = setTargetSmartly(_findInput, imageOrText);
    if (null == target) {
      return null;
    }
		if (target.equals(imageOrText+"???")) {
			return target;
		}
    Debug timing = new Debug();
    timing.startTiming("Finder.findAll");

    setTargetSmartly(_findInput, imageOrText);
    _findInput.setSimilarity(Settings.MinSimilarity);
    _findInput.setFindAll(true);
    _results = Vision.find(_findInput);
    _cur_result_i = 0;

    timing.endTiming("Finder.findAll");
    return target;
  }

	/**
   *
   * @param aPtn
   */
  public String findAll(Pattern aPtn)  {
    if (aPtn.isValid()) {
      _pattern = aPtn;
      _findInput.setTarget(aPtn.getImage().getMatNative());
      _findInput.setSimilarity(aPtn.getSimilar());
      _findInput.setFindAll(true);
      Debug timing = new Debug();
      timing.startTiming("Finder.findAll");
      _results = Vision.find(_findInput);
      _cur_result_i = 0;
      timing.endTiming("Finder.findAll");
      return aPtn.getFilename();
    } else {
      return null;
    }
  }

  public String findAll(Image img)  {
    if (img.isValid()) {
      _findInput.setTarget(img.getMatNative());
      _findInput.setSimilarity(Settings.MinSimilarity);
      _findInput.setFindAll(true);
      Debug timing = new Debug();
      timing.startTiming("Finder.findAll");
      _results = Vision.find(_findInput);
      _cur_result_i = 0;
      timing.endTiming("Finder.findAll");
      return img.getFilename();
    } else {
      return null;
    }
  }

  public String findAllText(String text) {
    _findInput.setTarget(TARGET_TYPE.TEXT, text);
    _findInput.setFindAll(true);
    Debug timing = new Debug();
    timing.startTiming("Finder.findAll");
    _results = Vision.find(_findInput);
    _cur_result_i = 0;
    timing.endTiming("Finder.findAll");
    return text;
  }

  private String setTargetSmartly(FindInput fin, String target) {
    if (isImageFile(target)) {
      //assume it's a file first
      String filename = Image.create(target).getFilename();
      if (filename != null) {
        fin.setTarget(TARGET_TYPE.IMAGE, filename);
        return filename;
      } else {
        if (!repeating) {
          Debug.error(target
                  + " looks like a file, but not on disk. Assume it's text.");
        }
      }
    }
    if (!Settings.OcrTextSearch) {
      Debug.error("Region.find(text): text search is currently switched off");
      return target + "???";
    } else {
      fin.setTarget(TARGET_TYPE.TEXT, target);
      if (TextRecognizer.getInstance() == null) {
        Debug.error("Region.find(text): text search is now switched off");
        return target + "???";
      }
      return target;
    }
  }

	private static boolean isImageFile(String fname) {
		int dot = fname.lastIndexOf('.');
		if (dot < 0) {
			return false;
		}
		String suffix = fname.substring(dot + 1).toLowerCase();
		if (suffix.equals("png") || suffix.equals("jpg")) {
			return true;
		}
		return false;
	}

  /**
   *
   * @return true if Finder has a next match, false otherwise
   */
  @Override
  public boolean hasNext() {
    if (_results != null && _results.size() > _cur_result_i) {
      return true;
    }
    return false;
  }

  /**
   *
   * @return the next match or null
   */
  @Override
  public Match next() {
    Match ret = null;
    if (hasNext()) {
      FindResult fr = _results.get(_cur_result_i++);
      Screen parentScreen = null;
      if (_region != null) {
        parentScreen = _region.getScreen();        
      }
      ret = new Match(fr, parentScreen);
			fr.delete();
      if (_region != null) {
        ret = _region.toGlobalCoord(ret);
      }
      if (_pattern != null) {
        Location offset = _pattern.getTargetOffset();
        ret.setTargetOffset(offset);
      }
    }
    return ret;
  }

  /**
   * not used
   */
  @Override
  public void remove(){}

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    destroy();
  }


}
