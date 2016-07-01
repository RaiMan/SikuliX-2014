/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.ide;

import java.security.CodeSource;
import javax.swing.JOptionPane;
import org.sikuli.basics.Settings;
import org.sikuli.script.Screen;

public class Sikulix {

  public static void main(String[] args) {
    String jarName = "";

    CodeSource codeSrc =  SikuliIDE.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      jarName = codeSrc.getLocation().getPath();
    }

    if (jarName.contains("sikulixsetupIDE")) {
      JOptionPane.showMessageDialog(null, "Not useable!\nRun setup first!",
              "sikulixsetupIDE", JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }
//    Screen.ignorePrimaryAtCapture = true;
//    Settings.TraceLogs = true;
    SikuliIDE.run(args);
  }
}
