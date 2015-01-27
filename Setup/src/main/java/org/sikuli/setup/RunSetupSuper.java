package org.sikuli.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunSetupSuper {
  
	public static void main(String[] args) throws IOException, InterruptedException {
    
    File fWork = new File(System.getProperty("user.dir"));
    String fpSetup = new File(fWork, "target/sikulixsetup-1.1.0-plain.jar").getAbsolutePath();
    String mainClass = "org.sikuli.setup.RunSetup";
    
    String cmd = String.format("java -jar %s %s", fpSetup, mainClass);
    Process proc = Runtime.getRuntime().exec(cmd);
    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String line;
    while(null != (line = in.readLine())) {
      System.out.println(line);
    }
    proc.waitFor();
    System.exit(1);
    
  }
  
}
