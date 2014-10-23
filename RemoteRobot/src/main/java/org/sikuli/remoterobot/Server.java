package org.sikuli.remoterobot;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.ImageIcon;


public class Server {
  private static final String key = "KEY";
  private static final String kType = "TYPE";
  private static final String mouse = "MOUSE";
  private static final String mMove = "MOVE";
  private static final String mClick = "CLICK";
  private static final String cSystem = "SYSTEM";
  private static final String capture = "CAPTURE";
  private static final String cBounds = "BOUNDS";
  
  public static int LEFT = InputEvent.BUTTON1_MASK;
  public static int MIDDLE = InputEvent.BUTTON2_MASK;
  public static int RIGHT = InputEvent.BUTTON3_MASK;
  public static int WHEEL_UP = -1;
  public static int WHEEL_DOWN = 1;
  
  private static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
  private static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
  private static final Rectangle SCREEN = new Rectangle(0, 0, WIDTH, HEIGHT);
  private static ServerSocket server = null;
  private static Robot robot;
  private static ObjectOutputStream out = null;
  private static Scanner in = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;
  
//TODO set loglevel at runtime
  private static int logLevel = 0;
  private static void log(int lvl, String message, Object... args) {
    if (lvl < 0 || lvl >= logLevel) {
      System.out.println((lvl < 0 ? "[error] " : "[info] ") + 
              String.format("ScreenRemoteServer: " + message, args));
    }
  }
  private static void log(String message, Object... args) {
    log(0, message, args);
  }
  
  public static void main(String[] args) {
    
    int port = getPort(args.length > 0 ? args[0] : null);
    try {
      try {
        if (port > 0) {
          server = new ServerSocket(port);
        }
      } catch (Exception ex) {
        log(-1, "Starting: " + ex.getMessage());
      }
      if (server == null) {
        log(-1, "could not be started on port: " + (args.length > 0 ? args[0] : null));
        System.exit(1);
      }
      while (true) {
        log("now waiting on port: %d at %s", port, InetAddress.getLocalHost().getHostAddress());
        Socket socket = server.accept();
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new Scanner(socket.getInputStream());
        robot = new Robot();
        HandleClient client = new HandleClient(socket);
        isHandling = true;
        while (true) {
          if (socket.isClosed()) {
            shouldStop = client.getShouldStop();
            break;
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
          }
        }
        if (shouldStop) {
          break;
        }
      }
    } catch (Exception e) {
    }
    if (!isHandling) {
      log(-1, "start handling not possible: " + port);
    }
    log("now stopped on port: " + port);
  }
  
  private static int getPort(String p) {
    int port;
    int pDefault = 50000;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return -1;
      }
    } else {
      return pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  private static class HandleClient implements Runnable {

    private volatile boolean keepRunning;
    Thread thread;
    Socket socket;
    Boolean shouldStop = false;

    public HandleClient(Socket sock) {
      init(sock);
    }
    
    private void init(Socket sock) {
      socket = sock;
      if (in == null || out == null) {
        Server.log(-1, "communication not established");
        System.exit(1);
      }
      thread = new Thread(this, "HandleClient");
      keepRunning = true;
      thread.start();
    }
    
    public boolean getShouldStop() {
      return shouldStop;
    }

    @Override
    public void run() {
      String e;
      Server.log("now handling client: " + socket);
      while (keepRunning) {
        try {
          e = in.nextLine();
          if (e != null) {
            Server.log("processing: " + e);
            if (e.contains("EXIT")) {
              stopRunning();
              in.close();
              out.close();
              if (e.contains("STOP")) {
                Server.log("stop server requested");
                shouldStop = true;
              }
              return;
            }
            if (e.startsWith(capture)) {
              sendImage(e.replace(capture, "").trim());
            } else if (e.startsWith(cBounds)) {
              getBounds(e);
            } else if (e.startsWith(cSystem)) {
              getSystem();
            } else if (e.startsWith(kType)) {
              doType(e);
            } else if (e.startsWith(key)) {
              doKey(e);
            } else if (e.startsWith(mMove)) {
              doMove(e);
            } else if (e.startsWith(mClick)) {
              doClick(e);
            } else if (e.equals(mouse)) {
              send(MouseInfo.getPointerInfo().getLocation());
            } else if (e.startsWith(mouse)) {
              doMouse(e);
            }
          }
        } catch (Exception ex) {
          Server.log(-1, "Exception while processing\n" + ex.getMessage());
          stopRunning();
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        stopRunning();
      }
    }
    
    private void getSystem() {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.startsWith("mac")) {
        os = "MAC";
      } else if (os.startsWith("windows")) {
        os = "WINDOWS";
      } else if (os.startsWith("linux")) {
        os = "LINUX";
      } else {
        os = "NOTSUPPORTED";
      }
      GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gdevs = genv.getScreenDevices();
      send(os + " " + gdevs.length);
    }

    private void getBounds(String bnd) {
      int screen = 0;
      if (bnd.length() > cBounds.length()) {
        try {
          screen = Integer.parseInt(bnd.substring(cBounds.length()));
        } catch (Exception ex) {}        
      }
      GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gdevs = genv.getScreenDevices();
      if (screen >= gdevs.length) {
        send(null);
      }
      send(gdevs[screen].getDefaultConfiguration().getBounds());
    }
    
    private void sendImage(String area) {
      BufferedImage img;
      Rectangle rect;
      if (area.substring(7).isEmpty()) {
        img = robot.createScreenCapture(SCREEN);
      } else {
        rect = evalRectangle(area);
        if (rect == null) {
          Server.log(-1, "Capture: invalid rectangle: " + area);          
          rect = SCREEN;
        } 
        Server.log("Capture: " + rect);          
        img = robot.createScreenCapture(rect);
      }
      ImageIcon imgObject = new ImageIcon(img);
      send(imgObject);
    }
    
    private Rectangle evalRectangle(String area) {
      int x, y, w, h;
      if (!area.contains(" ")) {
        return null;
      }
      String[] rect = area.split(" ");
      if (rect.length != 4) {
        return null;
      }
      try {
        x = Integer.parseInt(rect[0]);
        y = Integer.parseInt(rect[1]);
        w = Integer.parseInt(rect[2]);
        h = Integer.parseInt(rect[3]);
      } catch (NumberFormatException ex) {
        return null;
      }
      if (w < 0 || h <0 || x > WIDTH-1 || y > HEIGHT-1) {
        return null;
      }
      x = x < 0 ? 0 : x; 
      y = y < 0 ? 0 : y; 
      w = w > WIDTH - x ? WIDTH - x : w;
      h = h > HEIGHT - y ? HEIGHT - y : h;
      return new Rectangle(x, y, w, h);
    }
    
    private void doType(String keys) {
      keys = keys.replace(kType, "").trim();
      if (!keys.contains(" ")) {
        send("nok");
      } else {
        String[] actions = keys.split(" ");
        for (String a : actions) {
          if (!doKeyDo(a)) {
            send("nok");
            return;
          }
        }
        send("ok");
      }
    }
    
    private void doKey(String action) {
      action = action.replace(key, "").trim();
      if (doKeyDo(action)) {
        send("ok");
      } else {
        send("nok");
      }
    }
    
    private boolean doKeyDo(String action) {
      if (action.length() < 2) {
        return false;
      }
      String cmd = "" + action.charAt(0);
      int key;
      try {
        key = Integer.parseInt(action.substring(1));
      } catch (NumberFormatException ex) {
        return false;
      }
      if ("P".equals(cmd)) {
        robot.keyPress(key);
        robot.waitForIdle();
        log("keyPress: " + key);
      } else if ("R".equals(cmd)) {
        robot.keyRelease(key);
        robot.waitForIdle();
        log("KeyRelease: " + key);
      } else if ("W".equals(cmd)) {
        robot.delay(key);
      } else {
        return false;
      }
      return true;
    }
    
    private void doClick(String click) {
      click = click.replace(mClick, "").trim();
      if (!click.contains(" ")) {
        send("nok");
      } else {
        String[] actions = click.split(" ");
        for (String a : actions) {
          if (!doMouseDo(a)) {
            send("nok");
            return;
          }
        }
        send("ok");
      }
    }
    
    private void doMouse(String buttons) {
      buttons = buttons.replace(mouse, "").trim();
      if (doMouseDo(buttons)) {
        send("ok");
      } else {
        send("nok");
      }
    }

    private boolean doMouseDo(String action) {
      if (action.length() < 2) {
        return false;
      }
      String cmd = "" + action.charAt(0);
      int btn;
      try {
        btn = Integer.parseInt(action.substring(1));
      } catch (NumberFormatException ex) {
        btn = LEFT;
      }
      if (btn == 0) {
        return true;
      }
      if ("D".equals(cmd)) {
        robot.mousePress(btn);
        robot.waitForIdle();
        log("mousePress: " + btn);
      } else if ("U".equals(cmd)) {
        robot.mouseRelease(btn);
        robot.waitForIdle();
        log("mouseRelease: " + btn);
      } else if ("W".equals(cmd)) {
        robot.delay(btn);
      } else {
        return false;
      }
      return true;
    }
    
    private void doMove(String loc) {
      loc = loc.replace(mMove, "").trim();
      if (!loc.contains(" ")) {
        send("nok");
      } else {
        int x = Integer.parseInt(loc.substring(0, loc.indexOf(" ")));
        int y = Integer.parseInt(loc.substring(loc.indexOf(" ")+1));
        robot.mouseMove(x, y);
        robot.waitForIdle();
        send("ok");
      }
    }
    
    private void send(Object o) {
      try {
        out.writeObject(o);
        out.flush();
        if (o instanceof ImageIcon) {
          Server.log("returned: Image(%dx%d)", 
                  ((ImageIcon) o).getIconWidth(), ((ImageIcon) o).getIconHeight());          
        } else {
          Server.log("returned: "  + o);
        }
      } catch (IOException ex) {
        Server.log(-1, "send: writeObject: Exception: " + ex.getMessage());
      }
    }

    public void stopRunning() {
      Server.log("stop client handling requested");
      try {
        socket.close();
      } catch (IOException ex) {
        Server.log(-1, "fatal: socket not closeable");
        System.exit(1);
      }
      keepRunning = false;
    }
  }
}
