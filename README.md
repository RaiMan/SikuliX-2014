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

The structure and usage of this repo
------------------------

Each folder in this repo is a Maven project by itself with its own POM, but it needs to be in this folder and POM structure, since there is a super POM, that installs all modules into your local Maven repo and needs the parent POM in SikuliX, to run its own POM successfully.

So if you want to contribute, you should fork and 



