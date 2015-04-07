package org.sikuli.script;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.script.ScriptEngine;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;


public class RunServer {
  private static ServerSocket server = null;
  private static PrintWriter out = null;
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
        out = new PrintWriter(socket.getOutputStream());
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
        log(-1, "given port not useable: %s --- using default", p);
        return pDefault;
      }
    } else {
      return pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  static ScriptEngine jsRunner = null;
	static File scriptFolder = null;

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

    boolean isHTTP = false;
    String request;
    String rCommand;
    String[] rArgs;
    String rMessage = "";
		Object evalReturnObject;

    @Override
    public void run() {
			Debug.on(3);
      RunServer.log("now handling client: " + socket);
      while (keepRunning) {
        try {
          String inLine = in.nextLine();
          if (inLine != null) {
            if (!isHTTP) {
              RunServer.log("processing: <%s>", inLine);
            }
						boolean success = true;
            if (inLine.startsWith("GET") && inLine.contains("HTTP")) {
              isHTTP = true;
							request = inLine;
              continue;
            }
            if (isHTTP) {
              if (!inLine.isEmpty()) {
                continue;
              }
            }
            if (!isHTTP) {
              request = "GET /?" + request + " HTTP/1.1";
            }
            String[] parts = request.split("\\s");
            rCommand = parts[1].substring(2).toUpperCase();
            rArgs = new String[0];
            int rArgsCount = parts.length -3;
            if (rArgsCount > 0) {
              rArgs = new String[rArgsCount];
              for (int i = 0; i < rArgsCount; i++) {
                rArgs[i] = parts[i + 2];
              }
            }
            if (rCommand.contains("STOP")) {
              shouldStop = true;
            } else if (rCommand.contains("START")) {
              success = startRunner();
              if (!success) {
                rMessage = "startRunner: not possible";
              }
            } else if (rCommand.contains("SDIR")) {
							scriptFolder = new File(rArgs[0]);
            } else if (rCommand.contains("RUN")) {
							String script = rArgs[0];
							File fScript = new File(scriptFolder, script);
							File fScriptScript = new File(fScript, script + ".js");
							success &= fScript.exists() && fScriptScript.exists();
							if (success) {
								ImagePath.setBundlePath(fScript.getAbsolutePath());
                startRunner();
								if (jsRunner != null) {
                  try {
										evalReturnObject = jsRunner.eval(new java.io.FileReader(fScriptScript));
										rMessage = "runScript: returned: " +
														(evalReturnObject == null ? "null" : evalReturnObject.toString());
										success = success && evalReturnObject != null;
                  } catch (Exception ex) {
                    rMessage = "runScript: script raised exception on run: " + ex.toString();
                    success = false;
                  }
								} else {
									success = false;
								}
							} else {
                rMessage = "runScript: script not found or not valid" + fScriptScript.toString();
              }
						} else {
							if (jsRunner != null) {
                String line = request.substring(6);
                line = line.replace(" HTTP/1.1", "");
                  try {
										evalReturnObject = jsRunner.eval(line);
										rMessage = "runStatement: returned: " +
														(evalReturnObject == null ? "null" : evalReturnObject.toString());
										success = success && evalReturnObject != null;
                  } catch (Exception ex) {
                    rMessage = "runStatement: raised exception on eval: " + ex.toString();
                    success = false;
                  }
							} else {
                rMessage = "runStatement: not possible --- no runner";
								success = false;
							}
						}
            if (isHTTP) {
              String retVal = success ? "HTTP/1.1 200 OK" : "HTTP/1.1 400 NOK";
							String state = success ? "PASS: " : "FAIL: ";
              out.write(retVal + "\r\n\r\n" + request + "\r\n" + state + rMessage + "\r\n");
              out.flush();
              stopRunning();
            } else {
              send(success ? "ok" : "failed");
            }
            if (shouldStop) {
              if (!isHTTP) {
                stopRunning();
              }
              in.close();
              out.close();
              return;
            }
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

    private void send(String response) {
      try {
        out.write(response);
        out.flush();
        RunServer.log("returned: "  + response);
      } catch (Exception ex) {
        RunServer.log(-1, "send: write: Exception: " + ex.getMessage());
      }
    }

    public void stopRunning() {
      if (!isHTTP) {
        RunServer.log("stop client handling requested");
      }
      try {
        socket.close();
      } catch (IOException ex) {
        RunServer.log(-1, "fatal: socket not closeable");
        System.exit(1);
      }
      keepRunning = false;
    }

    private boolean startRunner() {
      if (jsRunner == null) {
        try {
          jsRunner = Runner.initjs();
          String prolog = "";
          prolog = Runner.prologjs(prolog);
          prolog = Runner.prologjs(prolog);
          jsRunner.eval(prolog);
        } catch (Exception ex) {
          rMessage = "startRunner: not possible";
          return false;
        }
      }
      return true;
    }
  }
}
