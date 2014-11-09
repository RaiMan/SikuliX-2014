package com.sikulix.testrun;

import edu.unh.iol.dlc.ConnectionController;
import edu.unh.iol.dlc.VNCScreen;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import org.sikuli.script.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

public class TestRun {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  public static void main(String[] args) throws FindFailed, IOException {
    Debug.test("SikuliX 2014 TestRun: hello");
    Debug.setDebugLevel(3);
    Screen s = Sikulix.init();
    ImagePath.add("com.sikulix.testrun.TestRun/images/images.sikuli");
    
    testListText(s);

    System.exit(1);
  }

  public void loggerCallBack(String msg) {
    p("from loggerCallBack: redirection: %s", msg);
  }
  
  private static void testListText(Screen s) {
		TextRecognizer tr = TextRecognizer.getInstance();
    Region reg = s.exists("image").right().highlight(2);
    List<Match> words = tr.listText(s.capture(reg), reg);
    p("words lenght: %d", words.size());
    for (Match m: words) {
      if (m.getScore() > 0.8) {
        p("%s", m.getText());
        m.highlight(1);
      }
    }
  }

  private static void testImageInJar() {
		String img = "image.png";
    Debug.setDebugLevel(3);
//    ImagePath.setBundlePath(imgN);
		Debug.test("***** 1st image");
		Image iimg = Image.create(img);
		Debug.test("***** 2nd image");
		iimg = Image.create(img);
		Image.dump();
  }
  
  public static void testVNC(String[] args) throws FindFailed{

    Socket s = null;
    
    try {
      s = new Socket("192.168.1.17", 5900); //open a socket to vnc server on listening port
      s.setSoTimeout(1000);
      s.setKeepAlive(true); //some socket configuration
    } catch (IOException ex) {
      p("Socket open: failed: %s", ex.getMessage());
    }
    
    ConnectionController cc = new ConnectionController(s);
    cc.openConnection(0); //opens the vnc connection for connection 0, multiple are supported

    cc.setPixelFormat(0, "Truecolor", 32, 0);  //for connection 0, set pixel data to Truecolor, 32 bits per pixel, little endian
    cc.start(0); //start thread that keeps BufferedImage updated by polling server for remote desktop changes
    
    Sikulix.pause(2); //wait for buffered image to be updated before we do sikuli stuff

    VNCScreen vnc = new VNCScreen();  //default constructor uses ConnectionController index 0


    vnc.click(new Pattern("untitled.png"));

    //do other sikuli operations

    cc.closeConnection(0); //clean up socket, stop thread
}

}


// Try and catch blocks for exceptions are omitted from example.  
// The VNC protocol supports multiple pixel formats.  
// Currently, the VNC client code only supports truecolor, 32 bits per pixel, little endian pixel format.  
// Depending on which VNC server you are using, it may initialize the connection with some other pixel format that is not currently supported.  
// In this case, a message is printed to stderr saying "Error: PixelFormat not supported, setPixelFormat required" to let the user know.  
// After that, the setPixelFormat line changes the connection to the format supported by the client.  
// That line could probably be better integrated into Sikuli's 
// logging system but I am less familiar with the details of while level would be appropriate.}
