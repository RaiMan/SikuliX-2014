[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://www.sikuli.org) SikuliX-2014 (version 1.1.x)
============

[![Join the chat at https://gitter.im/RaiMan/SikuliX-2014](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/RaiMan/SikuliX-2014?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is 1.1.2 after [1.1.1 final (available 2017-03-05)](https://launchpad.net/sikuli/sikulix/1.1.1)
-----------------
... a bug-fix release for problems with version 1.1.1

<hr>
**The followup is SikuliX2** [version 2.0.0 in 2017](https://github.com/RaiMan/SikuliX2) 
<hr>

**SNAPSHOTS on OSSRH** (if available)<br>

The repository URL:<br>
`<url>http://oss.sonatype.org/content/groups/public</url>`<br>

The coordinates:
```
<groupId>com.sikulix</groupId>
<artifactId>sikulixapi</artifactId>
<version>1.1.2-SNAPSHOT</version>
```

find an **usage example** in [module TestRunMaven](https://github.com/RaiMan/SikuliX-2014/tree/master/TestRunMaven)

**You might also visit the WIKI**

<hr>
This is the **last version that compiles and runs on Java 6**. SikuliX2 will need Java 1.7+.

**If you want to test the head of developement without the need to build from sources:** <br>
[look here at the page with the nightly builds](http://nightly.sikuli.de)<br>
**At your own risk ;-) Take care for your existing work - be prepared to restore your stuff!!**

Might not be available all the time.
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
**To get the ready-to-use packages (IDE, Script, Java/Jython support) <br />it is still recommended [to start here](http://www.sikulix.com).**
<hr />

**Usage docs now on [ReadTheDocs](http://sikulix-2014.readthedocs.org/en/latest/#) (work in progress)**

**Tools I use for developement:** <br />
IDE with Maven and GitHub support: [IntelliJ IDEA CE](https://www.jetbrains.com/idea/) (using Java 7 and 8)<br />
Doc Service: [Read the Docs](https://readthedocs.org)<br />
WebSite Services: [Host Europe](https://www.hosteurope.de)

The structure of this repo
------------------------

Each folder (module) in this repo is a Maven project by itself with its own POM, but it needs to be in this folder and POM structure, since there is a super POM on the first level, that is the parent POM for all the other child POMs and installs all modules into your local Maven repo. For detailed usage information look further below.

<hr/>

**--- The top level modules (representing the Sikuli features) ---**

Module sikulixapi (folder API) (sikulixapi.jar)
---

**package org.sikuli.script** 

The Java implementation comprising the API to access the top elements (Screen, Region, Pattern, Match, Image, ...) and their methods allowing to search for images and to act on points and matches simulating mouse and keyboard.

The ready-to-use package `sikulixapi.jar` provides this API for Java programming and any Java aware scripting languages.

**package org.sikuli.basics** 

Implements basic utility and helper features used in the top level packages (basic file and folder handling, download features, jar access and handling, export of native libraries, parameter and preferences handling, update and extension handling, ...) and hence it is contained in all packages.

**package org.sikuli.natives** 

Contains the Java sources interface classes (JNI based, mainly SWIG generated) providing the implementation of the OpenCV and Tesseract usage and the implementation of some system specific features (HotKeyHandling, App class support,...).

**package org.opencv. ...** 

Sikuli's image search is based on features of [OpenCV](http://opencv.org). Starting with version 2.4.6 OpenCV provides a self-contained JNI interface to the OpenCV native libraries, allowing to use OpenCV features directly in Java (and hence making C++ programming obsolete for this).

This module contains a specially configured Java/JNI OpenCV package (built using the standard OpenCV configure/make workflow) for use with the OpenCV features currently needed by Sikuli (core, imgproc, feature2d and highgui). The corresponding native library pack (currently Mac only) is contained in the module Libs.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed libraries ready.

Module sikulixide (folder IDE) (sikulix.jar)
---

Implements a GUI using Java, that allows to edit and run Sikuli scripts (currently Jython and JRuby are supported). It is an easy to use IDE focusing on the handling of the screenshots and images used in the typical Sikuli workflows.

The package `sikulix.jar` is the top level package containing all other options (hence the follow up of `sikuli-ide.jar` known from former releases).

After setup this package `sikulix.jar` contains the selected scripting interpreter(s) (Jython and/or JRuby), thus allowing to run Sikuli scripts out of the box from the commandline and providing interactive Sikuli aware scripting shells (hence it includes the functionality known from the `sikuli-script.jar` of former Sikuli(X) releases and is used the same way).

In all cases the Jython and JRuby jar packages are loaded from MavenCentral if needed.

If you want to experiment with the special JRuby support (rSpec, cucumber, ...) you have to look into the modules JRubyAddOns and JRubyGem. Both have to be built manually if needed (not contained in the local developement build).
<hr/>

**--- The helper/utility modules (intended for internal and/or developement use only) ---**

**Module Setup**

It produces the fat jar `sikulixsetup.jar` being the root downloadable artefact. It is needed to setup the SikuliX packages to be used on the local systems. Though the preferred setup is to let setup download the needed stuff on the fly, there is the possibility to run setup completely local/offline after having downloaded the needed stuff manually ([look here ...](http://www.sikulix.com/quickstart.html#qs2)) 

**Modules LibsWin, LibsMac, LibsLux**

The prebuilt native libraries for Windows, Mac and Linux (partially).<br />
(produces `sikulixlibsxxx.jar`)

**Module Jygments4SikuliX**

This is an adaption of the work [Jygments](https://code.google.com/p/jygments/) to the needs of SikuliX: it contains lexer/parser/formatter features and is a port from Python to Java of the well known Pygments tool, that is widely used for syntax highlighting and formatting of program code. In SikuliX it is intended to be used for syntax highlighting and other purposes, where scripting language grammar awareness is needed.

<hr/>

**Module TestRunMaven**

A sample implementation of a Maven project, that loads the sikulixapi.jar from MavenCentral (currently still OSSRH).

<hr/>

Usage - basic information
-------------------------

If you intend to compile and build the modules after having downloaded this repo, you should have a valid Maven 3 installation and for editing, testing and integration some IDE, that is enabled for working with Maven projects and has support for Git repositories (I myself use NetBeans 8, which supports both out of the box).

**Look here for a guide, [how to get on the road with this project using NetBeans](https://github.com/RaiMan/SikuliX-2014/wiki/Work-with-the-sources-in-NetBeans)**.

**Take care** Even if you only want to work on one of the modules (e.g. API), the modules should not be moved around, but stay in the structure of the downloaded repo. Each module depends on the parent POM in the root as well as the ready-to-use-jar-production POMs, that additionally depend on the assembly descriptors.

**--- Mandatory first step**

In the root directory of the repo run <br />
`mvn clean install`<br />
which builds all modules and installs the artifacts into your local Maven repository.

**Be aware** This mandatory first step will add "tons" of additional stuff from Maven Central repository to your local Maven repository, especially when you are a first time Maven user.

More details for Maven aspects you can find [here ...](https://github.com/RaiMan/SikuliX-2014/wiki/More-information-related-to-the-Maven-aspects-in-this-project)

**--- How to produce the ready to use jars ---**

... `sikulix.jar` and `sikulixapi.jar`

[please look here](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-produce-the-ready_to_use-jar-packages)
