package org.sikuli.tesseract;

import com.sun.jna.Pointer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.sikuli.basics.Debug;

import org.sikuli.script.*;

public class Run
{
  static String basePath = "/Users/rhocke/SikuliX";
  static String libPath = basePath + "/libs";
  static String datapath = libPath + "/tessdata";
  static String testFile = "/tesstest.png";
  static String language = "eng";
  static TessAPI.TessBaseAPI handle;

  public static void main( String[] args ) throws IOException
  {
    Debug.setDebugLevel(3);
    Screen s = new Screen();
    new Finder();

    System.setProperty("jna.encoding", "UTF8");
    System.setProperty("jna.library.path", libPath);
    handle = TessAPI.TessBaseAPICreate();
    TessAPI.TessBaseAPIInit3(handle, datapath, language);

    // ScreenImage si = s.userCapture();
    BufferedImage image = ImageIO.read(new File(basePath + testFile)); //si.getImage();

    ByteBuffer buf = Image.convertImageData(image);
    int bpp = image.getColorModel().getPixelSize();
    int bytespp = bpp / 8;
    int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);

    TessAPI.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO);
    TessAPI.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
    // TessAPI.TessBaseAPISetRectangle(handle, 0, 0, 1024, 800);
    Pointer utf8Text = TessAPI.TessBaseAPIGetUTF8Text(handle);

    String result = utf8Text.getString(0);
    System.out.println(testFile + " returned: ***[\n" + result + "\n]***");

    TessAPI.TessDeleteText(utf8Text);
    TessAPI.TessBaseAPIDelete(handle);
  }
// von Manish https://www.linkedin.com/profile/view?id=58640428  
//Match ss = sikuliScreen.find(ImagePath); 
//
//int x1 = ss.x; 
//int y1 = ss.y; 
//int w1 = ss.w; 
//int h1 = ss.h+1; 
//Rectangle rectOne = new Rectangle(); 
//
//rectOne.x=x1;; 
//rectOne.y = y1; 
//
//rectOne.height = h1; 
//rectOne.width = w1; 
//Thread.sleep(5000); 
//BufferedImage capture = new Robot().createScreenCapture(rectOne); 
//File n1 = File.createTempFile("screenshot", ".png"); 
//ImageIO.write(capture, "png", n1); 
//BufferedImage n2=ImageIO.read(n1); 
////define a property to animate 
//w1=n2.getWidth(); 
//h1=n2.getHeight(); 
//BufferedImage n3=new 
//BufferedImage(w1, h1, BufferedImage.TYPE_BYTE_GRAY); 
//Font font11=new Font("Calibri",Font.BOLD,40); 
//Graphics2D g11 = n3.createGraphics(); 
//g11.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
//RenderingHints.VALUE_INTERPOLATION_BILINEAR); 
//g11.setBackground(Color.BLACK); 
//g11.setColor(Color.WHITE); 
//g11.setFont(font11); 
//g11.drawImage(n2, 0, 0, null); 
//g11.dispose(); 
//capture = new Robot().createScreenCapture(rectOne); 
//File n4 = File.createTempFile("screenshot", ".png"); 
//
//ImageIO.write(n3, "PNG", n4); 
//BufferedImage n6=ImageIO.read(n4); 
//result=TextRecognizer.getInstance().recognize(n6); 
//System.out.println(result); 
//try 
//{ 
//result=result.replaceAll("\\|\\]","0"); 
//} 
//catch(Exception ex) 
//{ 
//result=result; 
//} 
//
//
//Image img=n6.getScaledInstance(-1, h1+13, Image.SCALE_SMOOTH); 
//BufferedImage bufferedScaled = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB); 
//bufferedScaled.getGraphics().drawImage(img, 0, 0, null); 
//bufferedScaled.getGraphics().dispose(); 
//capture = new Robot().createScreenCapture(rectOne); 
//File n44 = File.createTempFile("screenshot", ".png"); 
//ImageIO.write(bufferedScaled, "PNG", n44); 
//result1=TextRecognizer.getInstance().recognize(bufferedScaled); 
//result1=result1.trim();
}
