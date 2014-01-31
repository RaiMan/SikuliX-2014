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

//				ScreenImage si = s.userCapture();
        BufferedImage image = ImageIO.read(new File(basePath + testFile)); //si.getImage();

        ByteBuffer buf = Image.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);

        TessAPI.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO);
        TessAPI.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
//        TessAPI.TessBaseAPISetRectangle(handle, 0, 0, 1024, 800);
        Pointer utf8Text = TessAPI.TessBaseAPIGetUTF8Text(handle);

				String result = utf8Text.getString(0);
        System.out.println(testFile + " returned: ***[\n" + result + "\n]***");

				TessAPI.TessDeleteText(utf8Text);
        TessAPI.TessBaseAPIDelete(handle);
    }
}
