SikuliX-2014 (current version 1.1.0)
============

NOT stable currently <br /> do not fork yet nor download for testing
============

This Maven multi-module setup contains everything to <br />
**build the ready-to-use packages [available on Launchpad](https://launchpad.net/sikuli)** <br />(this is at least the intention at time of final release end January 2014).

**Forking and/or downloading this repo only makes sense:** 
 - if you want to get a knowledge about the internals of Sikuli
 - if you want to create your own packages containing Sikuli features 
 - if you want to contribute.

Even if you want to develop in Java or any Java aware scripting language (Jython, JRuby, Scala. Closure, ...) it is strongly recommended to start with sikuli-java.jar (run Sikuli setup with option 4).

**To get the ready-to-use packages (IDE, Script, Java/Jython support) <br />it is still recommended [to start here](http://www.sikuli.org/download.html).**

The structure of this repo
------------------------

Each folder (module) in this repo is a Maven project by itself with its own POM, but it needs to be in this folder and POM structure, since there is a super POM on the first level, that is the parent POM for all the other child POMs and installs all modules into your local Maven repo. For detailed usage information look further below.

<hr/>

**--- The top level modules (representing the Sikuli features) ---**

**Module API**

The Java implementation comprising the API to access the top elements (Screen, Region, Pattern, Match, Image, ...) and their methods allowing to search for images and to act on points and matches simulating mouse and keyboard.

The ready-to-use package `sikuli-java.jar` provides this API for Java programming and any Java aware scripting languages.

The ready-to-use package `sikuli-script.jar` as a convenience/backward-compatibility comes with the bundled Jython interpreter, thus allowing to run Sikuli scripts out of the box from the commandline and providing an interactive Sikuli aware Jython shell.<br />
This package will vanish in the long range, since it is fully contained in the IDE package now.

**Module IDE**

Implements a GUI using Java, that allows to edit and run Sikuli scripts (currently only Jython is supported). It is an easy to use IDE focusing on the handling of the screenshots and images used in the typical Sikuli workflows.

The package `sikuli-ide.jar` is the top level package containing all other options. It can be used from commandline in the same way as `sikuli-script.jar` and hence usually only this IDE package is needed.

<hr/>

**--- The helper/utility modules (intended for internal use only) ---**

**Module Jython**

Implements the Jython support for the IDE and for running scripts using Jython as scripting language.<br />
(contained in packages sikuli-ide.jar and sikuli-script.jar)

**Module Basics**

Implements basic ustility and helper features used in the top level packages (basic file and folder handling, download features, jar access and handling, export of native libraries, parameter and preferences handling, update and extension handling, ...) and hence it is contained in all packages.

As a special feature it comprises the `sikuli-setup.jar`, which is run after download to build the wanted Sikuli packages and make them ready-to-use on the specific system (Windows, Mac or Linux).

**Module OpenCV**

Sikuli's image search is based on respective features of [OpenCV](http://opencv.org). Starting with version 2.4.6 OpenCV provides a self-contained JNI interface to the OpenCV native libraries, allowing to use OpenCV features directly in Java (and hence making C++ programming obsolete for this).

This module contains a specially configured Java/JNI OpenCV package (built using the standard OpenCV configure/make workflow) for use with the OpenCV features currently needed by Sikuli (core, imgproc, feature2d and highgui). The corresponding native library pack (currently Mac only) is contained in the module Libs.

With the final availability of version 1.1.0 the implementation of the OpenCV usage will be moved completely to the Java level. Until then the historical implementation in C++ is activated in the standard. The usage of the new implementation (in the new classes ImageFinder and ImageFind) can be switched on optinally for testing and developement.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed features.

**Module Natives**

Contains the Java sources interface classes (JNI based, mainly SWIG generated) and the C++ sources providing the implementation of the OpenCV and Tesseract usage and the implementation of some system specific features (HotKeyHandling, App class support,...).

A maven based build workflow for the native libraries (libVisionProxy, lib...Util and hotky support on Mac) is available in the module Libs, which is also the target module for the prebuilt libraries finally bundled with the top level packages.

<hr/>

**--- The modules used to support package production (container jars) ---**

**Module Libs**

The prebuilt native libraries for Windows, Mac and Linux (partially).<br />
(contained in `sikuli-setup.jar`)

**Module Tesseract**

Currently as a convenience the standard tessdata folder needed for using Tesseract 3.0.<br />
(will be downloaded on request during a Sikuli setup)

**Module MacApp**

A template Sikuli-IDE.app, that is downloaded on request and made ready-to-use during Sikuli setup.

**Module Docs**

The source files for the textual documentation (built with PythonSphinx based on .rst files) and a ready-to-use HTML version as well as a HTML version of the JavaDocs of the main public Java API.<br />
(is downloaded and made ready-to-use-locally during Sikuli setup)

<hr/>

**--- Modules being AddOns or Extensions --**

**Module Guide**

**Module Remote**

<hr/>

Usage - basic information
-------------------------

If you intend to compile and build the modules after having downloaded this repo, you need a valid Maven 3 installation and for editing, testing and integration some IDE, that is enabled for working with Maven projects. <br />(I myself use NetBeans 7.4, which supports Maven by default)

**Take care** Even if you only want to work on one of the modules (e.g. API), the modules should not be moved around, but stay in the structure of the downloaded repo. Each module depends on the parent POM in the root and the ready-to-use-jar-production POMs additionally on the assembly descriptors plain.xml and complete.xml.

**--- Mandatory first step**

In the root directory of the repo run <br />
`mvn clean install`<br />
which builds all modules and installs the artifacts into your local Maven repository.

If you want jars containing the sources of the respective modules and/or containing the javadocs you can use the following profile switches:<br />
`mvn clean install -PwithSource,withDocs`<br />
(but for local usages, there might not be any sense in that ;-)

**Be aware** This mandatory first step will add "tons" of additional stuff from Maven Central repository to your local Maven repository, especially when you are a first time Maven user.

**--- Basic compile/package/install for each module**

You have 3 options, to selectively run the POM of a specific module alone:
 1. in the root folder run<br />`mvn -pl <ModuleName> [clean] [compile|package|install]` <br />where `<ModuleName>` is the respective folder name
 2. in the module's folder run <br />`mvn [clean] [compile|package|install]`
 3. use the respective build steps in your IDE being in one of the module projects

You might additionally use the above mentioned profile switches.

**--- Running the IDE or running scripts ---**

In this Maven context the only runnable (means: should be run or makes sense to run) module is IDE (besides Basics with RunSetup), but this is sufficient, to get access to and test every aspect of Sikuli, since you can start the IDE, run scripts directly as well and start an interactive Jython session.

Being in module IDE firing the RUN button in an IDE should start the Sikuli IDE showing the splash screen, that allows to enter parameters (and thus allowing to run scripts using option -r).

The following Java settings are relevant for running Sikuli IDE from an IDE or using Maven:
 - -Dsikuli.Debug=3 sets a higher debug level from beginning (to debug startup problems)
 - -Dsikuli.console=false the equivalent of the option -c (script output goes to console)
 - -Dsikuli.FromCommandLine suppresses the startup splash screen (set parameters in IDE project run setup instead)

Debugging should work without problems, when starting the module IDE in debug mode.

The mudule IDE POM contains the exec-maven-plugin, so to run the Sikuli IDE you can use being in the IDE folder<br />
`mvn exec:java -Dsikuli.FromCommandLine -Dexec.args="args for Sikuli"`

or this being in the root folder <br />
`mvn -pl IDE exec:java -Dsikuli.FromCommandLine -Dexec.args="args for Sikuli"`

As it is standard with Maven, all -D parameters go to Java system properties, wheras the content of -Dexec.args string will be given to the args array for the main method of the startup class.

**--- How to produce the ready to use jars ...**

... `sikuli-ide.jar`, `sikuli-script.jar` and `sikuli-java.jar`

[please look here](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-produce-the-ready_to_use-jar-packages)
