/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

public class RunnerClient {

  private ObjectInputStream in = null;
  private OutputStreamWriter out;
  private static Socket socket = null;
  private boolean socketValid;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "RunnerClient: " + message, args);
  }

  private static void log(String message, Object... args) {
    log(3, message, args);
  }

  public RunnerClient(String adr, String p) {
    init(adr, p);
  }

  private void init(String adr, String p) {
    socketValid = true;
    String ip = FileManager.getAddress(adr);
    int port = FileManager.getPort(p);
    if (ip == null || port < 0) {
      log(-1, "fatal: not valid: " + adr + " / " + p);
      System.exit(1);
    }
    try {
      socket = new Socket(ip, port);
    } catch (Exception ex) {
      log(-1, "fatal: no connection: " + adr + " / " + p);
      socketValid = false;
    }
    try {
      if (socketValid) {
        in = new ObjectInputStream(socket.getInputStream());
        out = new OutputStreamWriter(socket.getOutputStream());
        log("connection at: " + socket);
      }
    } catch (Exception ex) {
      log(-1, "fatal: problem starting pipes:\n", ex.getMessage());
      socketValid = false;
    }
		if (socketValid) {
			Object answer = send("START");
			answer = send("popup(\"Hallo\"");
			close(true);
		}
		System.exit(1);
  }


  public boolean isValid() {
    return (socketValid && socket != null);
  }

  public ObjectInputStream getIn() {
    return in;
  }

  public OutputStreamWriter getOut() {
    return out;
  }

  public Object send(String command) {
    if (!isValid()) {
      log(-1, "ScreenRemote not valid - send not possible");
      return null;
    }
    Object res = null;
    try {
      getOut().write(command + "\n");
      getOut().flush();
      log("send: " + command);
      res = getIn().readObject();
      if (res == null) {
        log(-1, "command not successful: " + command);
        return null;
      } else {
        if (!res.getClass().equals(String.class)) {
          log(-1, "OTHER: received: " + res);
          res = null;
        }
      }
    } catch (Exception ex) {
      if (command.startsWith("EXIT")) {
        return("ok");
      }
      log(-1, "fatal: while processing:\n" + ex.getMessage());
    }
    return res;
  }

  public boolean close(boolean stopServer) {
    if (socket != null) {
      try {
        if (stopServer) {
          send("EXIT STOP");
        } else {
          send("EXIT");
        }
        socket.close();
      } catch (IOException ex) {
        log(-1, "fatal: not closeable: %s\n" + ex.getMessage(), socket);
        return false;
      }
    }
    socket = null;
    socketValid = false;
    return true;
  }

  public boolean close() {
    return close(false);
  }
}
