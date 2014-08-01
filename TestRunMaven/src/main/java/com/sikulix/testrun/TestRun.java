package com.sikulix.testrun;

import org.sikuli.script.*;
import org.sikuli.basics.Debug;

public class TestRun
{
    public static void main( String[] args )
    {
        Debug.test( "SikuliX 2014 TestRun: hello" );
				Debug.setDebugLevel(3);
				//Sikulix.init();
				Debug.test("setting logger");
				Debug.setLogger(new TestRun());
				Debug.test("setting logger all redirect");
				Debug.setLoggerAll("info");
				Debug.test("testing redirection");
				Debug.info("test redirection info");
				Debug.action("test redirection info");
				Debug.error("test redirection info");
        Debug.test( "SikuliX 2014 TestRun: end" );
    }

	public void info(String msg) {
		System.out.println("[TEST] myLogger.info: " + msg);
	}
}
