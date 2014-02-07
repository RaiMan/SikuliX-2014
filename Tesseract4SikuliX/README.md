Tesseract4SikuliX 2014 (version 1.1.x)

This is an adaption of the work [Tess4J](http://tess4j.sourceforge.net) to the needs of this project, to allow the use of relevant Tesseract features directly from the Java level. The implementation is on level Tesseract 3.02 and uses JNA direct mapping to access the native functions in the library libtesseract.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed needed libraries ready.

For more information on preparation and usage of the new Tesseract Java API [look here ...](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-prepare-and-use-the-new-Tesseract-Java-API)

**Module Natives**

Contains the Java sources interface classes (JNI based, mainly SWIG generated) and the C++ sources providing the implementation of the OpenCV and Tesseract usage and the implementation of some system specific features (HotKeyHandling, App class support,...).

A maven based build workflow for the native libraries (libVisionProxy, lib...Util and hotky support on Mac) is available in the module Libs, which is also the target module for the prebuilt libraries finally bundled with the top level packages.
