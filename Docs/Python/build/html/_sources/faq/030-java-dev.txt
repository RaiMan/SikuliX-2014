
How to use Sikuli Script in your JAVA programs
==============================================

.. _howtojava:

The core of Sikuli Script is written in Java, which means you can use Sikuli Script as a standard JAVA library in your program. This document lets you know how to do that. 

After having setup Sikuli on your system, as recommended on the `download page <http://sikuli.org/download.shtml>`_, you have to do the following:

1. Include sikuli-script.jar in the CLASSPATH of your Java project.
------------------------------------------------------------------- 

We use Eclipse as an example. After adding sikuli-script.jar or preferably sikuli-java.jar as a library reference into your project, the project hierarchy should look like this.

.. image:: test-sikuli-project.png

2. Import the Sikuli classes you need
-------------------------------------

You can simply "import org.sikuli.script.*" or import the classes you need. In most cases, you would need at least :py:class:`Region` or :py:class:`Screen`.

3. Write code!
--------------

Here is a hello world example on Mac. 
The program clicks on the spotlight icon on the screen, waits until spotlight's input window appears, activates it by clicking and then writes "hello world" into the field and hits ENTER.

.. code-block:: java

	import org.sikuli.script.*;
	
	public class TestSikuli {
	
		public static void main(String[] args) {
			Screen s = new Screen();
			try{
				s.click("imgs/spotlight.png");
				s.wait("imgs/spotlight-input.png");
				s.click();
				s.write("hello world#ENTER.");
			}
			catch(FindFailed e){
				e.printStackTrace();                    
			}	
		}

	}

See also
--------
Be aware, that some method signatures in the Java API differ from the scripting level.
 * `Javadoc of SikuliX <http://sikuli.org/doc/java-x/>`_.
 * :doc:`/sikuli-script-index`.

