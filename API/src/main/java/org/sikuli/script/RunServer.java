package org.sikuli.script;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.script.ScriptEngine;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;


public class RunServer {
  private static ServerSocket server = null;
  private static ObjectOutputStream out = null;
  private static Scanner in = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;

//TODO set loglevel at runtime
  private static int logLevel = 0;
  private static void log(int lvl, String message, Object... args) {
    if (lvl < 0 || lvl >= logLevel) {
      System.out.println((lvl < 0 ? "[error] " : "[info] ") +
              String.format("RunnerServer: " + message, args));
    }
  }
  private static void log(String message, Object... args) {
    log(0, message, args);
  }

	private RunServer() {
	}

  public static void run(String[] args) {
		if (args == null) {
			args = new String[0];
		}
    int port = getPort(args.length > 0 ? args[0] : null);
    try {
      try {
        if (port > 0) {
					log(3, "Starting: trying port: %d", port);
          server = new ServerSocket(port);
        }
      } catch (Exception ex) {
        log(-1, "Starting: " + ex.getMessage());
      }
      if (server == null) {
        log(-1, "could not be started");
        System.exit(1);
      }
      while (true) {
        String theIP = InetAddress.getLocalHost().getHostAddress();
        String theServer = String.format("%s %d", theIP, port);
        FileManager.writeStringToFile(theServer, new File(RunTime.get().fSikulixStore, "RunServer"));
        log("now waiting on port: %d at %s", port, theIP);
        Socket socket = server.accept();
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new Scanner(socket.getInputStream());
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
    int pDefault = 50001;
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
		ScriptEngine jsRunner = null;
		File scriptFolder = null;

    public HandleClient(Socket sock) {
      init(sock);
    }

    private void init(Socket sock) {
      socket = sock;
      if (in == null || out == null) {
        RunServer.log(-1, "communication not established");
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
			Debug.on(3);
      String e;
      RunServer.log("now handling client: " + socket);
      while (keepRunning) {
        try {
          e = in.nextLine();
          if (e != null) {
            RunServer.log("processing: " + e);
						boolean success = true;
            if (e.contains("EXIT")) {
              stopRunning();
              in.close();
              out.close();
              if (e.contains("STOP")) {
                RunServer.log("stop server requested");
                shouldStop = true;
              }
              return;
            } else if (e.contains("START")) {
							if (jsRunner == null) {
								jsRunner = Runner.initjs();
								String prolog = "";
								prolog = Runner.prologjs(prolog);
								prolog = Runner.prologjs(prolog);
								jsRunner.eval(prolog);
							}
            } else if (e.contains("SDIR")) {
							scriptFolder = new File(e.substring(5));
            } else if (e.contains("RUN")) {
							String script = e.substring(4);
							File fScript = new File(scriptFolder, script);
							File fScriptScript = new File(fScript, script + ".js");
							success &= fScript.exists() && fScriptScript.exists();
							if (success) {
								ImagePath.setBundlePath(fScript.getAbsolutePath());
								if (jsRunner != null) {
									jsRunner.eval(new java.io.FileReader(fScriptScript));
								} else {
									success = false;
								}
							}
						} else {
							if (jsRunner != null) {
								jsRunner.eval(e);
							}
						}
						send(success ? "ok" : "failed");
          }
        } catch (Exception ex) {
          RunServer.log(-1, "Exception while processing\n" + ex.getMessage());
          stopRunning();
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        stopRunning();
      }
    }

    private void send(Object o) {
      try {
        out.writeObject(o);
        out.flush();
        RunServer.log("returned: "  + o);
      } catch (IOException ex) {
        RunServer.log(-1, "send: writeObject: Exception: " + ex.getMessage());
      }
    }

    public void stopRunning() {
      RunServer.log("stop client handling requested");
      try {
        socket.close();
      } catch (IOException ex) {
        RunServer.log(-1, "fatal: socket not closeable");
        System.exit(1);
      }
      keepRunning = false;
    }
  }
}
