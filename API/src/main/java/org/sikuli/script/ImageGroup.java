/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.script;

import java.awt.Rectangle;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

  
/**
 * An ImageGroup object represents images, that are all in the same folder.<br>
 * the folder can contain a set of sub folders that can be switched dynamically <br>
 * The folder will be found on the image path.<br>
 * The match data of the images can be stored and reloaded.<br>
 * Its main purpose is to support image sets for different environments.<br>
 * At runtime you can switch between image sets without changing the used image names.<br>
 * Based on the stored match data you might produce a new ImageGroup on the fly.<br>
 */
public class ImageGroup {

  private static Map<String, ImageGroup> imageGroups = 
          Collections.synchronizedMap(new HashMap<String, ImageGroup>());
  
  private String name;
  private URL url;
  private String path;
  private String subSet;
  
  private Map<String, int[]> images = Collections.synchronizedMap(new HashMap<String, int[]>());
  
  private boolean valid;

  /**
   * @return true if this group has been found and is loadable
   */
  public boolean isValid() {
    return valid;
  }
  
  public static ImageGroup open(String name) {
    ImageGroup ig = imageGroups.get(name);
    if (ig != null && ig.isValid()) {
      return ig;
    }
    ig = new ImageGroup(name);
    if (!ig.isValid()) {
      ig = null;
    }
    return ig;
  }
  
  public static void close(ImageGroup ig) {
    ig.images.clear();
    //take from ImagePath and purge
  }
    
  private ImageGroup(String name) {
    init(name, null);
  }
  
  private ImageGroup(String name, String subSet) {
    init(name, subSet);
  }
  
  private void init(String name, String subSet) {
    this.name = name;
    this.path = locate(name);
    url = null;
    this.subSet = subSet;
    valid = false;
    if (path != null) {
      valid = true;
      url = checkURL(path);
      imageGroups.put(name, this);
      use(subSet);
    }    
  }
  
  private static String locate(String name) {
    // find the given folder name on current image path
    return null;
  }  

  private static URL checkURL(String path) {
    // check wether path is an URL-string
    URL purl = null;
    return purl;
  }
  
  public boolean use(String sub) {
    if (sub == null) {
      // either no sub folders or use groupname as default sub
    }
    // add/replace in ImagePath (purge!)
    // save/load imagefacts?
    return true;
  }
  
  // triggered when lastSeen is stored 
  protected int[] addImageFacts(Image img, Rectangle r, double score) {
    int[] facts = new int[5];
    facts[0] = r.x;
    facts[1] = r.y;
    facts[2] = r.width;
    facts[3] = r.height;
    facts[4] = (int) (score*100);
    images.put(img.getName(), facts);
    return facts;
  }
  
  public boolean loadImageFacts() {
    return true;
  }

  public boolean saveImageFacts() {
    return true;
  }
}
