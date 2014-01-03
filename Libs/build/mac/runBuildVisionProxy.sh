#!/bin/sh
# this script compiles and links a Sikuli module on Mac
# the module name
mod=libVisionProxy.dylib 

# native sources
src=$DEVNATIVE

# folder for intermediate stuff (not synched on github)
externals=stuff/_ext/$mname
rm -f -R $externals
mkdir -p $externals

# needed includes (have to be checked/adapted on your system)
iany=/usr/local/include
icv=$iany/opencv2
itess=$iany/tesseract
ijava=/System/Library/Frameworks/JavaVM.framework/Headers

# modules the linker should know (have to be checked/adapted on your system)
lnkTess=$DEVLIBS/libtesseract.3.dylib 
lnkCVall=$DEVLIBS/libopencv_java248.dylib

# the compile steps
echo -- cvgui
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/cvgui.o.d -o $externals/cvgui.o $src/Vision/cvgui.cpp

echo -- finder
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/finder.o.d -o $externals/finder.o $src/Vision/finder.cpp

echo -- pyramid-template-matcher
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/pyramid-template-matcher.o.d -o $externals/pyramid-template-matcher.o $src/Vision/pyramid-template-matcher.cpp

echo -- sikuli-debug
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/sikuli-debug.o.d -o $externals/sikuli-debug.o $src/Vision/sikuli-debug.cpp

echo -- tessocr
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/tessocr.o.d -o $externals/tessocr.o $src/Vision/tessocr.cpp

echo -- vision
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/vision.o.d -o $externals/vision.o $src/Vision/vision.cpp

echo -- visionJAVA_wrap
g++ -c -O3 -I$iany -I$icv -I$itess -I$ijava -fPIC  -MMD -MP -MF $externals/visionJAVA_wrap.o.d -o $externals/visionJAVA_wrap.o $src/Vision/visionJAVA_wrap.cxx

echo -- finally linking
g++ -o $DEVLIBS/$mod $externals/cvgui.o $externals/finder.o $externals/pyramid-template-matcher.o $externals/sikuli-debug.o $externals/tessocr.o $externals/vision.o $externals/visionJAVA_wrap.o $lnkTess $lnkCVall -dynamic -dynamiclib -install_name $mod -Wl,-S -fPIC 
