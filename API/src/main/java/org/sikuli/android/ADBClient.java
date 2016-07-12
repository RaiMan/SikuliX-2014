package org.sikuli.android;


import org.sikuli.basics.Debug;
import se.vidstige.jadb.AdbServerLauncher;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;
import java.util.List;

/**
 * Created by tg44 on 2016. 06. 26..
 * Modified by RaiMan
 */
public class ADBClient {

  private static JadbConnection jadb = null;
  private static boolean shouldStopServer = false;
  private static JadbDevice device = null;

  private static void init() {
    getConnection(true);
    if (jadb == null) {
      try {
        new AdbServerLauncher().launch();
        Debug.info("ADBClient: ADBServer started");
        getConnection(false);
        if (jadb != null) {
          shouldStopServer = true;
        }
      } catch (Exception e) {
        Debug.error("ADBClient: ADBServer problem: %s", e.getMessage());
      }
    }
    if (jadb != null) {
      List<JadbDevice> devices = null;
      String serial = null;
      String state = null;
      try {
        devices = jadb.getDevices();
      } catch (Exception e) {
      }
      if (devices != null && devices.size() > 0) {
        device = devices.get(0);
        serial = device.getSerial();
        state = null;
        if (serial != null) {
          try {
            state = device.getState();
          } catch (Exception e) {
            state = "N/A";
          }
        } else {
          device = null;
          Debug.error("ADBClient: init: no devices attached");
        }
      }
      if (device != null) {
        Debug.info("ADBClient: init: attached device: serial(%s) state(%s)", serial, state);
      }
    }
  }

  private static void getConnection(boolean quiet) {
    if (jadb == null) {
      try {
        jadb = new JadbConnection();
        jadb.getHostVersion();
        Debug.info("ADBClient: ADBServer connection established");
      } catch (Exception e) {
        if (!quiet) {
          Debug.error("ADBClient: ADBServer connection not possible: %s", e.getMessage());
        }
        jadb = null;
      }
    }
  }

  public static JadbDevice getDevice() {
    init();
    return device;
  }

  //TODO: get device by id

  public boolean isValid() {
    return jadb != null;
  }

  public boolean hasDevices() {
    return device != null;
  }
}
