#!/bin/sh
# this script compiles and links a Sikuli module on Linux 32 or 64Bit

# variable DEVNATIVE is set outside - the native source files
# variable JDK is set outside - Java JDK
# variable LIBS is set outside - link libraries
# variable DEVLIBS is set outside - the target folder for the linked module

# native Sikuli sources
src=$DEVNATIVE

# folder(s) containing the header files
includeParm="-I$JDK/include -I$JDK/include/linux -I/usr/include -I/usr/local/include"

# folder for intermediate stuff (not synched on github)
externals=stuff/_ext/VisionProxy
rm -f -R $externals
mkdir -p $externals

# the compile steps
echo -- cvgui
g++ -c -O3 -fPIC -MMD -MP -MF $externals/cvgui.o.d $includeParm -o $externals/cvgui.o $src/Vision/cvgui.cpp

echo -- finder
g++ -c -O3 -fPIC -MMD -MP -MF $externals/finder.o.d $includeParm -o $externals/finder.o $src/Vision/finder.cpp

echo -- pyramid-template-matcher
g++ -c -O3 -fPIC -MMD -MP -MF $externals/pyramid-template-matcher.o.d $includeParm -o $externals/pyramid-template-matcher.o $src/Vision/pyramid-template-matcher.cpp

echo -- sikuli-debug
g++ -c -O3 -fPIC -MMD -MP -MF $externals/sikuli-debug.o.d $includeParm -o $externals/sikuli-debug.o $src/Vision/sikuli-debug.cpp

echo -- tessocr
g++ -c -O3 -fPIC -MMD -MP -MF $externals/tessocr.o.d $includeParm -o $externals/tessocr.o $src/Vision/tessocr.cpp

echo -- vision
g++ -c -O3 -fPIC -MMD -MP -MF $externals/vision.o.d $includeParm -o $externals/vision.o $src/Vision/vision.cpp

echo -- visionJAVA_wrap
g++ -c -O3 -fPIC -MMD -MP -MF $externals/visionJAVA_wrap.o.d $includeParm -o $externals/visionJAVA_wrap.o $src/Vision/visionJAVA_wrap.cxx

echo -- finally linking
g++  -shared -s -fPIC -dynamic -o $DEVLIBS/libVisionProxy.so $externals/cvgui.o $externals/finder.o $externals/pyramid-template-matcher.o $externals/sikuli-debug.o $externals/tessocr.o $externals/vision.o $externals/visionJAVA_wrap.o $LIBS/libtesseract.so $LIBS/libopencv_core.so $LIBS/libopencv_highgui.so $LIBS/libopencv_imgproc.so 
