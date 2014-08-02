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
				Debug.setLoggerNoPrefix(new TestRun());
				Debug.test("setting logger all redirect");
				Debug.setLoggerAll("info");
				Debug.test("testing redirection");
				Debug.info("test redirection info");
				Debug.action("test redirection action");
				Debug.error("test redirection error");
				Debug.log("test redirection debug");
        Debug.test( "SikuliX 2014 TestRun: end" );
    }

	public void info(String msg) {
		System.out.println("[TEST] myLogger.info: " + msg);
	}
}
