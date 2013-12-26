How Sikuli Works
=================
.. image:: SystemDesign.png

Sikuli Script
-------------
Sikuli Script is a Jython and Java library that automates GUI interaction using image patterns to direct keyboard/mouse events.
The core of Sikuli Script is a Java library that consists of two parts: java.awt.Robot, which delivers keyboard and mouse events to appropriate locations, and a C++ engine based on OpenCV, which searches given image patterns on the screen. The C++ engine is connected to Java via JNI and needs to be compiled for each platform.
On top of the Java library, a thin Jython layer is provided for end-users as a set of simple and clear commands. Therefore, it should be easy to add more thin layers for other languages running on JVM, e.g. JRuby, Scala, Javascript, etc.


The Structure of a Sikuli source folder or zipped file (.sikuli, .skl)
----------------------------------------------------------------------
A Sikuli script (.sikuli) is a directory that contains a Python source file (.py) representing the automation workflow or the test cases and all the image files (.png) used by the source file. All images used in a Sikuli script are simply a path to the .png file in the .sikuli bundle. Therefore, the Python source file can also be edited by any text editor.

While saving a script using Sikuli IDE, an extra HTML file may optionally be created in the .sikuli directory so that users can share a visual copy of the scripts on the web easily.

A Sikuli zipped script (.skl) is simply a zipped file of all files in the .sikuli folder. It is intended for distribution via mail or web upload, can also be run from command line and reopened in the Sikuli IDE. (The previous naming as "Sikuli executable" is deprecated, since this is misleading: people most often thought, it is something like a self-contained and self-running package comparable to a Windows EXE, but it is not).

