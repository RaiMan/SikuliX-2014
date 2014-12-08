#!/bin/sh
# this script compiles and links a Sikuli module on Linux 32 or 64Bit

# variable DEVNATIVE is set outside - the native source files
# variable JDK is set outside - Java JDK
# variable LIBS is set outside - link libraries

# abort on the first error
set -e

# native Sikuli sources
src=$DEVNATIVE

# folder(s) containing the header files
includeParm="-I$JDK/include -I$JDK/include/linux -I/usr/include -I/usr/local/include"

# folder for intermediate stuff (not synched on github)
externals=stuff/_ext/VisionProxy
rm -f -R $externals
mkdir -p $externals

list=(
  cvgui.cpp
  finder.cpp
  pyramid-template-matcher.cpp
  sikuli-debug.cpp
  tessocr.cpp
  vision.cpp
  visionJAVA_wrap.cxx
)

link_str="-shared -s -fPIC -dynamic -o libVisionProxy.so "

for fn in "${list[@]}"; do
  echo "--  $fn"
  g++ -c -O3 -fPIC -MMD -MP -MF $externals/$fn.o.d $includeParm -o $externals/$fn.o $src/Vision/$fn
  link_str+=" $externals/$fn.o "
done

echo -- finally linking
rm -f libVisionProxy.so
link_str+="/usr/lib/x86_64-linux-gnu/libopencv_core.so "
link_str+="/usr/lib/x86_64-linux-gnu/libopencv_highgui.so "
link_str+="/usr/lib/x86_64-linux-gnu/libopencv_imgproc.so "
link_str+="/usr/lib/libtesseract.so "
echo $link_str
g++ $link_str 

if [ -e libVisionProxy.so ]; then
  echo -- checking created libVisionProxy.so
  undefined=`ldd -r libVisionProxy.so | grep -c "undefined symbol:"`
  if [ "undefined" == "0" ]; then
    if [ -e $DEVLIBS ]; then    
      cp libVisionProxy.so $DEVLIBS
    fi
    echo -- should be useable
  else
    echo -- not useable - has unresolved symbols
    undefined=`ldd -r libVisionProxy.so | grep "undefined symbol:"`
  fi
else
  echo -- error building libVisionProxy.so
fi
