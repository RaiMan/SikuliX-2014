/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.natives.Vision;

/**
 * This class hides the complexity behind image names given as string.<br>
 * Its companion is ImagePath that maintains a list of places, where images are
 * stored.<br>
 * Another companion (ImageGroup) will allow to look at images in a folder as a
 * group.<br>
 * An Image object:<br>
 * - has a name, either given or taken from the basename<br>
 * - keeps it's in memory buffered image in a configurable cache avoiding reload
 * from source<br>
 * - remembers, where it was found when searched the last time<br>
 * - can be sourced from the filesystem, from jars, from the web and from other
 * in memory images <br>
 * - will have features for basic image manipulation and presentation<br>
 * - contains the stuff to communicate with the underlying OpenCV based search
 * engine <br>
 *
 * This class maintains<br>
 * - a list of all images ever loaded in this session with there source
 * reference and a ref to the image object<br>
 * - a list of all images currently having their content in memory (buffered
 * image) (managed as a configurable cache)<br>
 *
 * Image does not have public nor protected constructors: use create()
 *
 */
public class Image {

  static {
//    FileManager.loadLibrary(Settings.libOpenCV);
  }

  private static String me = "Image";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }

  private static List<Image> images = Collections.synchronizedList(new ArrayList<Image>());
  private static List<Image> imagePurgeList = Collections.synchronizedList(new ArrayList<Image>());
  private static List<String> imageNamePurgeList = Collections.synchronizedList(new ArrayList<String>());
  private static Map<URL, Image> imageFiles = Collections.synchronizedMap(new HashMap<URL, Image>());
  private static Map<String, URL> imageNames = Collections.synchronizedMap(new HashMap<String, URL>());
  private static int KB = 1024;
  private static int MB = KB * KB;
  private static int maxMemory = 64 * MB;
  private static int currentMemory;
  private static String imageFromJar = "__FROM_JAR__";
  private final static String isBImg = "__BufferedImage__";

  private String imageName = null;
  private boolean imageIsText = false;
  private boolean imageIsAbsolute = false;
  private boolean beSilent = false;
  private String filepath = null;
  private URL fileURL = null;
  private BufferedImage bimg = null;
  private long bsize = 0;
  private int bwidth = -1;
  private int bheight = -1;
  private Rectangle lastSeen = null;
  private double lastScore = 0.0;
  private ImageGroup group = null;

  /**
   * to support a raster over the image
   */
  private int rows = 0;
  private int cols = 0;
  private int rowH = 0;
  private int colW = 0;
  private int rowHd = 0;
  private int colWd = 0;

  @Override
  public String toString() {
    return String.format(
            (imageName != null ? imageName : "__UNKNOWN__") + ": (%dx%d)", bwidth, bheight)
            + (lastSeen == null ? ""
            : String.format(" seen at (%d, %d) with %.2f", lastSeen.x, lastSeen.y, lastScore));
  }

  private Image() {
  }

  /**
   * create a new image from a filename <br>
   * file ending .png is added if missing (currently valid: png, jpg, jpeg)<br>
   * filename: [...path.../]name[.png] is searched on current image path and
   * loaded to cache <br>
   * already loaded image with same name is reused (reference) and taken from
   * cache <br>
   * if image not found, it might be a text to be searched (imageIsText = true)
   *
   * @param fName
   * @return an Image object (might not be valid - check with isValid())
   */
  public static Image create(String fName) {
    Image img = get(fName, false);
    return createImageValidate(img);
  }

  /**
   * FOR INTERNAL USE: from IDE - suppresses load error message
   *
   * @param fName
   * @return this
   */
  public static Image createThumbNail(String fName) {
    Image img = get(fName, true);
    return createImageValidate(img);
  }

  /**
   * FOR INTERNAL USE: see get(String, boolean)
   *
   * @param fName
   * @return this
   */
  protected static Image get(String fName) {
    return get(fName, false);
  }

  /**
   * FOR INTERNAL USE: tries to get the image from the cache, if not cached yet:
   * create and load a new image
   *
   * @param fName
   * @param silent true: suppress some error messages
   * @return this
   */
  protected static Image get(String fName, boolean silent) {
    if (fName == null || fName.isEmpty()) {
      return null;
    }
    boolean absoluteFileName = false;
    boolean existsFileName = true;
    Image img = null;
    URL fURL = null;
    String fileName = getImageFilename(fName);
    if (fileName == null) {
      log(-1, "not a valid image type: " + fName);
      fileName = fName;
    } else {
      fileName = FileManager.slashify(fileName, false);
      File imgFile = new File(fileName);
      String fn = fileName;
      if (imgFile.isAbsolute()) {
        if (imgFile.exists()) {
          String bundlePath = ImagePath.getBundlePath();
          if (bundlePath != null && fileName.startsWith(bundlePath)) {
            fileName = new File(fileName).getName();
          } else {
            absoluteFileName = true;
          }
          fURL = FileManager.makeURL(fn);
          imageNames.put(fileName, fURL);
        } else {
          existsFileName = false;
        }
      }
      if (existsFileName) {
        fURL = imageNames.get(fileName);
        if (fURL == null) {
          fURL = ImagePath.find(fileName);
        }
        if (fURL != null) {
          img = imageFiles.get(fURL);
        }
      }
    }
    if (img == null) {
      img = new Image(fileName, fURL);
      img.setIsAbsolute(absoluteFileName);
      img.setBeSilent(silent);
    }
    return img;
  }

  private static String getImageFilename(String fname) {
    //TODO valid imagefile endings - where to store?
    int dot = fname.lastIndexOf(".");
    String ending;
    if (dot > 0) {
      ending = fname.substring(dot).toLowerCase();
      if (!ending.equals(".png") && !ending.equals(".jpg") && !ending.equals(".jepg")) {
        return null;
      }
    } else {
      fname += ".png";
    }
    return fname;
  }

  private Image(String fname, URL fURL) {
    init(fname, fURL);
  }

  private void init(String fileName, URL fURL) {
    imageName = fileName;
    if (imageName.isEmpty() || fURL == null) {
      return;
    }
    fileURL = fURL;
    if ("file".equals(fileURL.getProtocol())) {
      filepath = fileURL.getPath();
    } else if ("jar".equals(fileURL.getProtocol())) {
      filepath = imageFromJar;
    } else {
      //TODO support for http image urls
      log(-1, "URL not supported: " + fileURL);
      return;
    }
    loadImage();
  }

  private BufferedImage loadImage() {
    if (filepath != null) {
      try {
        bimg = ImageIO.read(fileURL);
      } catch (Exception e) {
        if (!beSilent) {
          log(-1, "could not be loaded from " + filepath);
        }
        return null;
      }
      if (imageName != null) {
        imageFiles.put(fileURL, this);
        imageNames.put(imageName, fileURL);
        log(lvl, "added to image list: %s \nwith URL: %s",
                imageName, fileURL);
        bwidth = bimg.getWidth();
        bheight = bimg.getHeight();
        bsize = bimg.getData().getDataBuffer().getSize();
        currentMemory += bsize;
        Image first;
        while (images.size() > 0 && currentMemory > maxMemory) {
          first = images.remove(0);
          currentMemory -= first.bsize;
        }
        images.add(this);
        log(lvl, "loaded %s (%d KB of %d MB (%d / %d %%) (%d))", imageName, (int) (bsize / KB),
                (int) (maxMemory / MB), images.size(), (int) (100 * currentMemory / maxMemory),
                (int) (currentMemory / KB));
      } else {
        log(-1, "ImageName invalid! not cached!");
      }
    }
    return bimg;
  }

  private static Image createImageValidate(Image img) {
    if (img == null) {
      log(-1, "Image not valid, creating empty Image");
      return new Image("", null);
    }
    if (!img.isValid()) {
      if (Settings.OcrTextSearch) {
        img.setIsText(true);
      } else {
        log(-1, "Image not valid, but TextSearch is switched off!");
      }
    }
    return img;
  }

  /**
   * create a new image from the given url <br>
   * file ending .png is added if missing <br>
   * filename: ...url-path.../name[.png] is loaded from the url and and cached
   * <br>
   * already loaded image with same url is reused (reference) and taken from
   * cache
   *
   * @param url
   * @return the image
   */
  public static Image create(URL url) {
    Image img = get(url);
    if (img == null) {
      img = new Image(url);
    }
    return createImageValidate(img);
  }

  protected static Image get(URL imgURL) {
    return imageFiles.get(imgURL);
  }

  private Image(URL fURL) {
    if ("file".equals(fURL.getProtocol())) {
      init(fURL.getPath(), fURL);
    } else {
      init(getNameFromURL(fURL), fURL);
    }
  }

  private static String getNameFromURL(URL fURL) {
    //TODO add handling for http
    if ("jar".equals(fURL.getProtocol())) {
      int n = fURL.getPath().lastIndexOf(".jar!/");
      int k = fURL.getPath().substring(0, n).lastIndexOf("/");
      if (n > -1) {
        return "JAR:" + fURL.getPath().substring(k + 1, n) + fURL.getPath().substring(n + 5);
      }
    }
    return "???:" + fURL.getPath();
  }

  /**
   * create a new image from a buffered image<br>
   * can only be reused with the object reference
   *
   * @param img
   */
  public Image(BufferedImage img) {
    this(img, null);
  }

  /**
   * create a new image from a buffered image<br>
   * giving it a descriptive name for printout and logging <br>
   * can only be reused with the object reference
   *
   * @param img
   * @param name descriptive name
   */
  public Image(BufferedImage img, String name) {
    if (name == null) {
      imageName = isBImg;
    } else {
      imageName = "BImg:" + name;
    }
    filepath = isBImg;
    bimg = img;
    bwidth = bimg.getWidth();
    bheight = bimg.getHeight();
  }

  /**
   * create a new image from a Sikuli ScreenImage (captured)<br>
   * can only be reused with the object reference
   *
   * @param img
   */
  public Image(ScreenImage img) {
    this(img.getImage(), null);
  }

  /**
   * create a new image from a Sikuli ScreenImage (captured)<br>
   * giving it a descriptive name for printout and logging <br>
   * can only be reused with the object reference
   *
   * @param img
   * @param name descriptive name
   */
  public Image(ScreenImage img, String name) {
    this(img.getImage(), name);
  }

  /**
   * Internal Use: IDE: to get rid of cache entries at script save, close or
   * save as
   *
   * @param bundlePath
   */
  public static void purge(String bundlePath) {
    if (imageFiles.isEmpty()) {
      return;
    }
    URL pathURL = FileManager.makeURL(bundlePath);
    if (!ImagePath.getPaths().get(0).pathURL.toExternalForm().equals(pathURL.toExternalForm())) {
      log(-1, "purge: not current bundlepath: " + pathURL);
      return;
    }
    purge(pathURL);
  }

  protected static synchronized void purge(URL pathURL) {
    String pathStr = pathURL.toExternalForm();
    URL imgURL;
    Image img;
    log(lvl, "purge: " + pathStr);
    Iterator<Map.Entry<URL, Image>> it = imageFiles.entrySet().iterator();
    Map.Entry<URL, Image> entry;
    Iterator<Image> bit;
    imagePurgeList.clear();
    imageNamePurgeList.clear();
    while (it.hasNext()) {
      entry = it.next();
      imgURL = entry.getKey();
      if (imgURL.toExternalForm().startsWith(pathStr)) {
        log(lvl, "purge: entry: " + imgURL.toString());
        imagePurgeList.add(entry.getValue());
        imageNamePurgeList.add(entry.getKey().toExternalForm());
        it.remove();
      }
    }
    if (!imagePurgeList.isEmpty()) {
      bit = images.iterator();
      while (bit.hasNext()) {
        img = bit.next();
        if (imagePurgeList.contains(img)) {
          bit.remove();
          log(lvl, "purge: bimg: " + img);
          currentMemory -= img.bsize;
        }
      }
    }
    if (!imageNamePurgeList.isEmpty()) {
      Iterator<Map.Entry<String, URL>> nit = imageNames.entrySet().iterator();
      Map.Entry<String, URL> name;
      while (nit.hasNext()) {
        name = nit.next();
        if (imageNamePurgeList.remove(name.getValue().toExternalForm())) {
          nit.remove();
        }
      }
    }
    log(lvl, "After Purge (%d): Max %d MB (%d / %d %%) (%d))",
            imagePurgeList.size(), (int) (maxMemory / MB), images.size(),
            (int) (100 * currentMemory / maxMemory), (int) (currentMemory / KB));
    imagePurgeList.clear();
    imageNamePurgeList.clear();
  }

  /**
   * Print the current state of the cache, verbosity depends on debug level
   */
  public static void dump() {
    log(0, "--- start of Image dump ---");
    ImagePath.printPaths();
    log(0, "ImageFiles entries: %d", imageFiles.size());
    Iterator<Map.Entry<URL, Image>> it = imageFiles.entrySet().iterator();
    Map.Entry<URL, Image> entry;
    while (it.hasNext()) {
      entry = it.next();
      log(lvl, entry.getKey().toExternalForm());
    }
    log(0, "ImageNames entries: %d", imageNames.size());
    Iterator<Map.Entry<String, URL>> nit = imageNames.entrySet().iterator();
    Map.Entry<String, URL> name;
    while (nit.hasNext()) {
      name = nit.next();
      log(lvl, "%s (%s)", name.getKey(), name.getValue());
    }
    log(0, "Cache state: Max %d MB (entries: %d  used: %d %% %d KB)",
            (int) (maxMemory / MB), images.size(),
            (int) (100 * currentMemory / maxMemory), (int) (currentMemory / KB));
    log(0, "--- end of Image dump ---");
  }

  /**
   * Get the image's descriptive name
   *
   * @return the name
   */
  public String getName() {
    return imageName;
  }

  /**
   *
   * @return the current ImageGroup
   */
  public ImageGroup getGroup() {
    return group;
  }

  /**
   * set the ImageGroup this image should belong to
   *
   * @param group
   */
  public void setGroup(ImageGroup group) {
    this.group = group;
  }

  /**
   * check whether image is available
   *
   * @return true if lodable or is an in memory image
   */
  public boolean isValid() {
    return filepath != null;
  }

  /**
   *
   * @return true if image was given with absolute filepath
   */
  public boolean isAbsolute() {
    return imageIsAbsolute;
  }

  protected void setIsAbsolute(boolean val) {
    imageIsAbsolute = val;
  }

  /**
   *
   * @return true if the given image name did not give a valid image so it might
   * be text to search
   */
  protected boolean isText() {
    return imageIsText;
  }

  /**
   * wether this image's name should be taken as text
   * @param val
   */
  protected void setIsText(boolean val) {
    imageIsText = val;
  }

  /**
   *
   * @return the evaluated url for this image (might be null)
   */
  public URL getURL() {
    return fileURL;
  }

  /**
   * @return the image's absolute filename or null if jar, http or in memory
   * image
   */
  public String getFilename() {
    if (fileURL != null && !"file".equals(fileURL.getProtocol())) {
      return null;
    }
    return filepath;
  }

  /**
   * return the image's BufferedImage (load it if not in cache)
   *
   */
  public BufferedImage get() {
    if (bimg != null) {
      if (!filepath.equals(isBImg)) {
        log(lvl, "getImage from cache: %s\n%s", imageName, (fileURL == null ? filepath : fileURL));
      } else {
        log(lvl, "getImage inMemory: %s", imageName);
      }
      return bimg;
    } else {
      return loadImage();
    }
  }

  /**
   *
   * @return size of image
   */
  public Dimension getSize() {
    return new Dimension(bwidth, bheight);
  }

  /**
   * if the image was already found before
   *
   * @return the rectangle where it was found
   */
  public Rectangle getLastSeen() {
    return lastSeen;
  }

  /**
   * if the image was already found before
   *
   * @return the similarity score
   */
  public double getLastSeenScore() {
    return lastScore;
  }

  /**
   * Internal Use: set the last seen info after a find
   *
   * @param lastSeen
   * @param sim
   */
  protected void setLastSeen(Rectangle lastSeen, double sim) {
    this.lastSeen = lastSeen;
    this.lastScore = sim;
    if (group != null) {
      group.addImageFacts(this, lastSeen, sim);
    }
  }

  /**
   * INTERNAL USE from IDE
   * @param val
   */
  public void setBeSilent(boolean val) {
    beSilent = val;
  }

  /**
   *
   * @param factor
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public BufferedImage resize(float factor) {
    int type;
    BufferedImage bufimg = get();
    type = bufimg.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : bufimg.getType();
    int width = (int) (getSize().getWidth() * factor);
    int height = (int) (getSize().getHeight() * factor);
    BufferedImage resizedImage = new BufferedImage(width, height, type);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(bufimg, 0, 0, width, height, null);
    g.dispose();
    return resizedImage;
  }

  /**
   * create a sub image from this image
   *
   * @param x
   * @param y
   * @param w
   * @param h
   * @return the new image
   */
  public Image getSub(int x, int y, int w, int h) {
    BufferedImage bi = createBufferedImage(w, h);
    Graphics2D g = bi.createGraphics();
    g.drawImage(get().getSubimage(x, y, w, h), 0, 0, null);
    g.dispose();
    return new Image(bi);
  }

  /**
   * create a sub image from this image
   *
   * @param part (the constants Region.XXX as used with region.get())
   * @return the sub image
   */
  public Image getSub(int part) {
    Rectangle r = Region.getRectangle(0, 0, getSize().width, getSize().height, part);
    return getSub(r.x, r.y, r.width, r.height);
  }

  /**
   * store info: this image is divided vertically into n even rows <br>
   * a preparation for using getRow()
   *
   * @param n
   * @return the top row
   */
  public Image setRows(int n) {
    return setRaster(n, 0);
  }

  /**
   * store info: this image is divided horizontally into n even columns <br>
   * a preparation for using getCol()
   *
   * @param n
   * @return the leftmost column
   */
  public Image setCols(int n) {
    return setRaster(0, n);
  }

  /**
   *
   * @return number of eventually defined rows in this image or 0
   */
  public int getRows() {
    return rows;
  }

  /**
   *
   * @return height of eventually defined rows in this image or 0
   */
  public int getRowH() {
    return rowH;
  }

  /**
   *
   * @return number of eventually defined columns in this image or 0
   */
  public int getCols() {
    return cols;
  }

  /**
   *
   * @return width of eventually defined columns in this image or 0
   */
  public int getColW() {
    return colW;
  }

  /**
   * store info: this image is divided into a raster of even cells <br>
   * a preparation for using getCell()
   *
   * @param r
   * @param c
   * @return the top left cell
   */
  public Image setRaster(int r, int c) {
    rows = r;
    cols = c;
    if (r > 0) {
      rowH = (int) (getSize().height / r);
      rowHd = getSize().height - r * rowH;
    }
    if (c > 0) {
      colW = (int) (getSize().width / c);
      colWd = getSize().width - c * colW;
    }
    return getCell(0, 0);
  }

  /**
   * get the specified row counting from 0, if rows or raster are setup <br>negative
   * counts reverse from the end (last = -1) <br>values outside range are 0 or last
   * respectively
   *
   * @param r
   * @return the row as new image or the image itself, if no rows are setup
   */
  public Image getRow(int r) {
    if (rows == 0) {
      return this;
    }
    if (r < 0) {
      r = rows + r;
    }
    r = Math.max(0, r);
    r = Math.min(r, rows - 1);
    return getSub(0, r * rowH, getSize().width, rowH);
  }

  /**
   * get the specified column counting from 0, if columns or raster are setup<br>
   * negative counts reverse from the end (last = -1) <br>values outside range are 0
   * or last respectively
   *
   * @param c
   * @return the column as new image or the image itself, if no columns are
   * setup
   */
  public Image getCol(int c) {
    if (cols == 0) {
      return this;
    }
    if (c < 0) {
      c = cols + c;
    }
    c = Math.max(0, c);
    c = Math.min(c, cols - 1);
    return getSub(c * colW, 0, colW, getSize().height);
  }

  /**
   * get the specified cell counting from (0, 0), if a raster is setup <br>
   * negative counts reverse from the end (last = -1) <br>values outside range are 0
   * or last respectively
   *
   * @param c
   * @return the cell as new image or the image itself, if no raster is setup
   */
  public Image getCell(int r, int c) {
    if (rows == 0) {
      return getCol(c);
    }
    if (cols == 0) {
      return getRow(r);
    }
    if (rows == 0 && cols == 0) {
      return this;
    }
    if (r < 0) {
      r = rows - r;
    }
    if (c < 0) {
      c = cols - c;
    }
    r = Math.max(0, r);
    r = Math.min(r, rows - 1);
    c = Math.max(0, c);
    c = Math.min(c, cols - 1);
    return getSub(c * colW, r * rowH, colW, rowH);
  }

  /**
   * get the OpenCV Mat version of the image's BufferedImage
   *
   * @return OpenCV Mat
   */
  public Mat getMat() {
    return createMat(get());
  }

  protected static Mat createMat(BufferedImage img) {
    if (img != null) {
      Debug timer = Debug.startTimer("Mat create\t (%d x %d) from \n%s", img.getWidth(), img.getHeight(), img);
      Mat mat_ref = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC4);
      timer.lap("init");
      byte[] data;
      BufferedImage cvImg;
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      int[] nBits = {8, 8, 8, 8};
      ColorModel cm = new ComponentColorModel(cs, nBits, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
      SampleModel sm = cm.createCompatibleSampleModel(img.getWidth(), img.getHeight());
      DataBufferByte db = new DataBufferByte(img.getWidth() * img.getHeight() * 4);
      WritableRaster r = WritableRaster.createWritableRaster(sm, db, new Point(0, 0));
      cvImg = new BufferedImage(cm, r, false, null);
      timer.lap("empty");
      Graphics2D g = cvImg.createGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
      timer.lap("created");
      data = ((DataBufferByte) cvImg.getRaster().getDataBuffer()).getData();
      mat_ref.put(0, 0, data);
      Mat mat = new Mat();
      timer.lap("filled");
      Imgproc.cvtColor(mat_ref, mat, Imgproc.COLOR_RGBA2BGR, 3);
      timer.end();
      return mat;
    } else {
      return null;
    }
  }

  /**
   * to get old style OpenCV Mat for FindInput
   *
   * @return SWIG interfaced OpenCV Mat
   * @deprecated
   */
  @Deprecated
  protected org.sikuli.natives.Mat getMatNative() {
    return convertBufferedImageToMat(get());
  }

  protected static org.sikuli.natives.Mat convertBufferedImageToMat(BufferedImage img) {
    if (img != null) {
      byte[] data = convertBufferedImageToByteArray(img);
      return Vision.createMat(img.getHeight(), img.getWidth(), data);
    } else {
      return null;
    }
  }

  protected static byte[] convertBufferedImageToByteArray(BufferedImage img) {
    if (img != null) {
      BufferedImage cvImg = createBufferedImage(img.getWidth(), img.getHeight());
      Graphics2D g = cvImg.createGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
      return ((DataBufferByte) cvImg.getRaster().getDataBuffer()).getData();
    } else {
      return null;
    }
  }

  protected static BufferedImage createBufferedImage(int w, int h) {
    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int[] nBits = {8, 8, 8, 8};
    ColorModel cm = new ComponentColorModel(cs, nBits, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    SampleModel sm = cm.createCompatibleSampleModel(w, h);
    DataBufferByte db = new DataBufferByte(w * h * 4);
    WritableRaster r = WritableRaster.createWritableRaster(sm, db, new Point(0, 0));
    BufferedImage bm = new BufferedImage(cm, r, false, null);
    return bm;
  }

	// **************** for Tesseract4Java ********************
    /**
     * Converts <code>BufferedImage</code> to <code>ByteBuffer</code>.
     *
     * @param bi Input image
     * @return pixel data
     */
    public static ByteBuffer convertImageData(BufferedImage bi) {
        DataBuffer buff = bi.getRaster().getDataBuffer();
        // ClassCastException thrown if buff not instanceof DataBufferByte because raster data is not necessarily bytes.
        // Convert the original buffered image to grayscale.
        if (!(buff instanceof DataBufferByte)) {
            bi = convertImageToGrayscale(bi);
            buff = bi.getRaster().getDataBuffer();
        }
        byte[] pixelData = ((DataBufferByte) buff).getData();
        //        return ByteBuffer.wrap(pixelData);
        ByteBuffer buf = ByteBuffer.allocateDirect(pixelData.length);
        buf.order(ByteOrder.nativeOrder());
        buf.put(pixelData);
        buf.flip();
        return buf;
    }

		/**
     * A simple method to convert an image to gray scale.
     *
     * @param image input image
     * @return a monochrome image
     */
    public static BufferedImage convertImageToGrayscale(BufferedImage image) {
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return tmp;
    }
}
