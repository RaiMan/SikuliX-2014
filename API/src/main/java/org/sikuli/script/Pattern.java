/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * to define a more complex search target<br>
 * - non-standard minimum similarity <br>
 * - click target other than center <br>
 * - image as in-memory image
 */
public class Pattern {

  private Image image = null;
  private float similarity = (float) Settings.MinSimilarity;
  private Location offset = new Location(0, 0);
  private int waitAfter = 0;

  /**
   * creates empty Pattern object at least setFilename() or setBImage() must be used before the
   * Pattern object is ready for anything
   */
  public Pattern() {
  }

  /**
   * create a new Pattern from another (attribs are copied)
   *
   * @param p
   */
  public Pattern(Pattern p) {
    image = p.getImage();
    similarity = p.similarity;
    offset.x = p.offset.x;
    offset.y = p.offset.y;
  }

  /**
   * create a Pattern with given image<br>
   *
   * @param img
   */
  public Pattern(Image img) {
    image = img;
  }

  /**
   * create a Pattern based on an image file name<br>
   *
   * @param imgpath
   */
  public Pattern(String imgpath) {
    image = Image.create(imgpath);
  }

  /**
   * Pattern from a Java resource (Object.class.getResource)
   *
   */
  public Pattern(URL url) {
    image = Image.create(url);
  }

  /**
   * A Pattern from a BufferedImage
   *
   * @param bimg
   */
  public Pattern(BufferedImage bimg) {
    image = new Image(bimg);
  }

  /**
   * A Pattern from a ScreenImage
   *
   * @param simg
   */
  public Pattern(ScreenImage simg) {
    image = new Image(simg.getImage());
  }

  /**
   * check wether the image is valid
   *
   * @return true if image is useable
   */
  public boolean isValid() {
    return image.isValid();
  }

  /**
   * set a new image for this pattern
   *
   * @param fileName
   * @return the Pattern itself
   */
  public Pattern setFilename(String fileName) {
    image = Image.create(fileName);
    return this;
  }

  /**
   * set a new image for this pattern
   *
   * @param fileURL
   * @return the Pattern itself
   */
  public Pattern setFilename(URL fileURL) {
    image = Image.create(fileURL);
    return this;
  }

  /**
   * set a new image for this pattern
   *
   * @param img
   * @return the Pattern itself
   */
  public Pattern setFilename(Image img) {
    image = img;
    return this;
  }

  /**
   * the current image's absolute filepath
   * <br>will return null, if image is in jar or in web
   * <br>use getFileURL in this case
   *
   * @return might be null
   */
  public String getFilename() {
    return image.getFilename();
  }

  /**
   * the current image's URL
   *
   * @return might be null
   */
  public URL getFileURL() {
    return image.getURL();
  }

  /**
   * sets the minimum Similarity to use with findX
   *
   * @param sim
   * @return the Pattern object itself
   */
  public Pattern similar(float sim) {
    similarity = sim;
    return this;
  }

  /**
   * sets the minimum Similarity to 0.99 which means exact match
   *
   * @return the Pattern object itself
   */
  public Pattern exact() {
    similarity = 0.99f;
    return this;
  }

  /**
   *
   * @return the current minimum similarity
   */
  public float getSimilar() {
    return this.similarity;
  }

  /**
   * set the offset from the match's center to be used with mouse actions
   *
   * @param dx
   * @param dy
   * @return the Pattern object itself
   */
  public Pattern targetOffset(int dx, int dy) {
    offset.x = dx;
    offset.y = dy;
    return this;
  }

  /**
   * set the offset from the match's center to be used with mouse actions
   *
   * @param loc
   * @return the Pattern object itself
   */
  public Pattern targetOffset(Location loc) {
    offset.x = loc.x;
    offset.y = loc.y;
    return this;
  }

  /**
   *
   * @return the current offset
   */
  public Location getTargetOffset() {
    return offset;
  }

  /**
   * ONLY FOR INTERNAL USE! Might vanish without notice!
   *
   * @return might be null
   */
  public BufferedImage getBImage() {
    return image.get();
  }

  /**
   * ONLY FOR INTERNAL USE! Might vanish without notice!
   *
   * @param bimg
   * @return the Pattern object itself
   */
  public Pattern setBImage(BufferedImage bimg) {
    image = new Image(bimg);
    return this;
  }

  /**
   * sets the Pattern's image
   *
   * @param img
   * @return the Pattern object itself
   */
  public Pattern setImage(Image img) {
    image = img;
    return this;
  }

  /**
   * get the Pattern's image
   *
   */
  public Image getImage() {
    return image;
  }

  /**
   * set the seconds to wait, after this pattern is acted on
   *
   * @param secs
   */
  public void setTimeAfter(int secs) {
    waitAfter = secs;
  }

  /**
   * get the seconds to wait, after this pattern is acted on
   */
  public int getTimeAfter() {
    return waitAfter;
  }

  @Override
  public String toString() {
    String ret = "P(" + image.getName()
            + (isValid() ? "" : " -- not valid!")
            + ")";
    ret += " S: " + similarity;
    if (offset.x != 0 || offset.y != 0) {
      ret += " T: " + offset.x + "," + offset.y;
    }
    return ret;
  }
}
