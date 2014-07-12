[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://www.sikuli.org) SikuliX-2014 (current version 1.1.0)
============

1.1.0-Beta3 is under development
-----------------
The **latest stable version** is **SikuliX 1.0.1** [download here](https://launchpad.net/sikuli/+download)<br><br>
**If you want to test the head of developement without the need to build from sources:** <br>
[look here: I have setup a page with nightly builds](http://nightly.sikuli.de)<br>
**At your own risk ;-) Take care for your existing work - be prepared to restore your stuff!!**
<hr />
**SikuliX is completely free Open Source in all aspects** - [see details](http://sikulix.com/disclaimer)

... based on Sikuli Script that was discontinued mid 2012 - [see the sources](https://github.com/sikuli/sikuli)
<hr/>

This Maven multi-module setup contains everything ...
-------------
**... to build the ready-to-use SikuliX packages as they are [available on Launchpad](https://launchpad.net/sikuli)**

`It is highly recommended to first look through this README, before clicking around`

**Forking and/or downloading this repo only makes sense:**
 - if you want to get a knowledge about the internals of Sikuli
 - if you want to create your own packages containing Sikuli features
 - if you want to contribute.

<hr />
**To get the ready-to-use packages (IDE, Script, Java/Jython support) <br />it is still recommended [to start here](http://www.sikulix.com/download.html).**
<hr />

**Usage docs now on [ReadTheDocs](http://sikulix-2014.readthedocs.org/en/latest/#) (work in progress)**

**Tools I use for developement:** <br />
IDE with Maven support: [NetBeans 7.4](https://netbeans.org) (using Java 7 on all systems)<br />
GitHub support: [SourceTree]() (Mac + Windows)<br />
CI Service: [Travis CI](http://travis-ci.com) <br />
Doc Service: [Read the Docs](https://readthedocs.org)<br />
Main WebSite Service: [Weebly](http://www.weebly.com)<br />
Private complementing Websites: [Host Europe](https://www.hosteurope.de)

The structure of this repo
------------------------

Each folder (module) in this repo is a Maven project by itself with its own POM, but it needs to be in this folder and POM structure, since there is a super POM on the first level, that is the parent POM for all the other child POMs and installs all modules into your local Maven repo. For detailed usage information look further below.

<hr/>

**--- The top level modules (representing the Sikuli features) ---**

**Module API**

The Java implementation comprising the API to access the top elements (Screen, Region, Pattern, Match, Image, ...) and their methods allowing to search for images and to act on points and matches simulating mouse and keyboard.

The ready-to-use package `sikulixapi.jar` provides this API for Java programming and any Java aware scripting languages.

**Module IDE**

Implements a GUI using Java, that allows to edit and run Sikuli scripts (currently Jython and JRuby are supported). It is an easy to use IDE focusing on the handling of the screenshots and images used in the typical Sikuli workflows.

The package `sikulix.jar` is the top level package containing all other options (hence the follow up of `sikuli-ide.jar` known from former releases).

After setup this package `sikulix.jar` contains the selected scripting interpreter(s) (Jython and/or JRuby), thus allowing to run Sikuli scripts out of the box from the commandline and providing interactive Sikuli aware scripting shells (hence it includes the functionality known from the `sikuli-script.jar` of former Sikuli(X) releases and is used the same way).
<hr/>

**--- The helper/utility modules (intended for internal use only) ---**

**Module Setup**

It produces the fat jar `sikulixsetup.jar` being the root downloadable artefact. It is needed to setup the SikuliX packages to be used on the local systems. Though the preferred setup is to let setup download the needed stuff on the fly, there is the possibility to run setup completely local/offline after having downloaded the needed stuff manually ([look here ...](http://www.sikulix.com/quickstart.html#qs2)) 

**Module Jython**

Implements Jython support for the IDE and for running scripts using Python as scripting language.<br />
(contained in package sikulix.jar)

**Module JRuby**

Implements JRuby support for the IDE and for running scripts using Ruby as scripting language.<br />
(optionally contained in package sikulix.jar)

**Module Basics**

Implements basic utility and helper features used in the top level packages (basic file and folder handling, download features, jar access and handling, export of native libraries, parameter and preferences handling, update and extension handling, ...) and hence it is contained in all packages.

As a special feature it comprises the `sikulixsetup.jar`, which is run after download to build the wanted SikuliX packages and make them ready-to-use on the specific system (Windows, Mac or Linux).

**Module OpenCV4SikuliX**

Sikuli's image search is based on features of [OpenCV](http://opencv.org). Starting with version 2.4.6 OpenCV provides a self-contained JNI interface to the OpenCV native libraries, allowing to use OpenCV features directly in Java (and hence making C++ programming obsolete for this).

This module contains a specially configured Java/JNI OpenCV package (built using the standard OpenCV configure/make workflow) for use with the OpenCV features currently needed by Sikuli (core, imgproc, feature2d and highgui). The corresponding native library pack (currently Mac only) is contained in the module Libs.

With the availability of the final version 1.1.0 the implementation of the OpenCV usage will be moved completely to the Java level. Until then the existing implementation in C++ is activated in the standard. The usage of the new implementation (in the new classes ImageFinder and ImageFind) can be switched on optinally for testing and developement.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed libraries ready.

For more information on preparation and usage of the new OpenCV Java API [look here ...](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-prepare-and-use-the-new-OpenCV-Java-API)

**Module Tesseract4SikuliX**

This is an adaption of the work [Tess4J](http://tess4j.sourceforge.net) to the needs of this project, to allow the use of relevant Tesseract features directly from the Java level. The implementation is on level Tesseract 3.02 and uses JNA direct mapping to access the native functions in the library libtesseract.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed needed libraries ready.

For more information on preparation and usage of the new Tesseract Java API [look here ...](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-prepare-and-use-the-new-Tesseract-Java-API)

**Module Natives**

Contains the Java sources interface classes (JNI based, mainly SWIG generated) and the C++ sources providing the implementation of the OpenCV and Tesseract usage and the implementation of some system specific features (HotKeyHandling, App class support,...).

A maven based build workflow for the native libraries (libVisionProxy, lib...Util and hotky support on Mac) is available in the module Libs, which is also the target module for the prebuilt libraries finally bundled with the top level packages.

**Module Jygments4SikuliX**

This is an adaption of the work [Jygments](https://code.google.com/p/jygments/) to the needs of SikuliX: it contains lexer/parser/formatter features and is a port from Python to Java of the well known Pygments tool, that is widely used for syntax highlighting and formatting of program code. In SikuliX it is intended to be used for syntax highlighting and other purposes, where scripting language grammar awareness is needed.

<hr/>

**--- The modules used to support package production (container jars) ---**

**Module Libs**

The prebuilt native libraries for Windows, Mac and Linux (partially).<br />
(contained in `sikulixsetup.jar`)

It contains a Maven workflow to execute build scripts for the native libraries on the system running on:
 - Windows (not yet ready): WinUtil.dll, VisionProxy.dll
 - Mac: libMacHotkeyManager.dylib, libMacUtil.dylib, libVisionProxy.dylib
 - Linux: libVisionProxy.so

To run it from the project root folder:<br />
`mvn -f Libs/x*`

The build scripts are in the respective system folder in folder `build` <br />
The built libraries will directly go to the respective resources folder. <br />
In case of errors you have to check the prerequisites: [look here ...](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-prepare-for-and-build-the-native-libraries)

For Linux there will still be a supplemental package available on the download page, that allows to build libVisionProxy.so

**Module Tesseract**

Currently as a convenience the standard tessdata folder needed for using Tesseract 3.0.<br />
(will be downloaded on request during a Sikuli setup)

**Module MacApp**

A template Sikuli-IDE.app, that is downloaded on request and made ready-to-use during Sikuli setup.

**Modules ...Fat**

Existing for IDE, API, Jython and JRuby. These build so called fat jars, that contain all needed dependency jars and are only intended for the build/setup process.

**Module Docs** (not Maven-ized yet)

The source files for the textual documentation (built with PythonSphinx based on .rst files) and a ready-to-use HTML version as well as a HTML version of the JavaDocs of the main public Java API.<br />
(is downloaded and made ready-to-use-locally during Sikuli setup)

<hr/>

**--- Modules being AddOns or Extensions --**

**Module Guide**

**Module Remote**

<hr/>

Usage - basic information
-------------------------

If you intend to compile and build the modules after having downloaded this repo, you should have a valid Maven 3 installation and for editing, testing and integration some IDE, that is enabled for working with Maven projects. <br />(I myself use NetBeans 7.4, which supports Maven by default)

**Take care** Even if you only want to work on one of the modules (e.g. API), the modules should not be moved around, but stay in the structure of the downloaded repo. Each module depends on the parent POM in the root as well as the ready-to-use-jar-production POMs, that additionally depend on the assembly descriptors.

**--- Mandatory first step**

In the root directory of the repo run <br />
`mvn clean install`<br />
which builds all modules and installs the artifacts into your local Maven repository.

**Be aware** This mandatory first step will add "tons" of additional stuff from Maven Central repository to your local Maven repository, especially when you are a first time Maven user.

If you want jars containing the sources of the respective modules and/or containing the javadocs you can use the following profile switches: `mvn clean install -PwithSource,withDocs` (but locally, there might not be much sense in that ;-)

More details for Maven aspects you can find [here ...](https://github.com/RaiMan/SikuliX-2014/wiki/More-information-related-to-the-Maven-aspects-in-this-project)

**--- How to produce the ready to use jars ---**

... `sikulix.jar` and `sikulixapi.jar`

[please look here](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-produce-the-ready_to_use-jar-packages)
