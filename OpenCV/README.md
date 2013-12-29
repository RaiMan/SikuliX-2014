Sikuli OpenCV 2014 (version 1.1.x)
===

Sikuli's image search is based on respective features of [OpenCV](http://opencv.org). Starting with version 2.4.6 OpenCV provides a self-contained JNI interface to the OpenCV native libraries, allowing to use OpenCV features directly in Java (and hence making C++ programming obsolete for this).

This module contains a specially configured Java/JNI OpenCV package (built using the standard OpenCV configure/make workflow) for use with the OpenCV features currently needed by Sikuli (core, imgproc, feature2d and highgui). The corresponding native library pack (currently Mac only) is contained in the module Libs.

With the final availability of version 1.1.0 the implementation of the OpenCV usage will be moved completely to the Java level. Until then the historical implementation in C++ is activated in the standard. The usage of the new implementation (in the new classes ImageFinder and ImageFind) can be switched on optinally for testing and developement.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed features.

For more information on preparation and usage of the new OpenCV Java API [look here ...](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-prepare-and-use-the-new-OpenCV-Java-API)

