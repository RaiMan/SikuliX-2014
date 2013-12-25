SikuliX-2014 (current version 1.1.0)
============

This Maven multi-module setup contains everything to <br />
**build the ready-to-use packages [available on Launchpad](https://launchpad.net/sikuli)** <br />(this is at least the intention at time of final release end January 2014).

**Forking and/or downloading this repo only makes sense:** 
 - if you want to get a knowledge about the internals of Sikuli
 - if you want to create your own packages containing Sikuli features 
 - if you want to contribute.

Even if you want to develop in Java or any Java aware scripting language (Jython, JRuby, Scala. Closure, ...) it is strongly recommended to start with sikuli-java.jar (run Sikuli setup with option 4).

**To get the ready-to-use packages (IDE, Script, Java/Jython support) it is still recommended [to start here](http://www.sikuli.org/download.html).**

The structure of this repo
------------------------

Each folder (module) in this repo is a Maven project by itself with its own POM, but it needs to be in this folder and POM structure, since there is a super POM on the first level, that is the parent POM for all the other child POMs and installs all modules into your local Maven repo. For detailed usage information look further below.

<hr/>

**--- The top level modules (representing the Sikuli features) ---**

**Module API**

**Module IDE**

<hr/>

**--- The helper/utility modules (intended for internal use only) ---**

**Module Jython**

**Module Basics**

**Module OpenCV**

**Module Natives**

<hr/>

**--- The modules used to support package production ---**

**Module Libs**

**Module Tesseract**

**Module MacApp**

<hr/>

**--- Modules being AddOns or Extensions --**

**Module Guide**

**Module Remote**

<hr/>

Usage - basic information
-------------------------

If you intend to compile and build the modules after having downloaded this repo, you need a valid Maven 3 installation and some IDE, that is enabled for working with Maven projects. <br />(I myself use NetBeans 7.4, which supports Maven by default)

**Mandatory first step**<br />
In the root directory of the repo run <br />
`mvn clean install`<br />
which builds all modules and installs the artifacts into your local Maven repository.

If you want jars containing the sources of the respective modules and or containing the javadocs you can use the following profile switches:<br />
`mvn clean install -PwithSource,withDocs`<br />
(but for local usages, there might not be any sense in that ;-)

**Be aware** This will add "tons" of additional stuff from Maven Central repository to your local Maven repository, especially when you are a first time Maven user.

**Basic compile/package/install for each module**<br />
you have 2 options, to selectively run the POM of a specific module alone:
 1. in the root folder run<br />`mvn -pl<ModuleName> [clean] [compile|package|install]` <br />where `<ModuleName>` is the respective folder name
 2. in the module's folder run <br />`mvn [clean] [compile|package|install]`

where you might additionally use the above mentioned profile switches


