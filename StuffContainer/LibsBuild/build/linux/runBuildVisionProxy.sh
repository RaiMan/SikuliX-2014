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

link_str="g++ -shared -s -fPIC -dynamic $(pkg-config --libs opencv tesseract) -o libVisionProxy.so "

for fn in "${list[@]}"; do
  echo "--  $fn"
  g++ -c -O3 -fPIC -MMD -MP -MF $externals/$fn.o.d $includeParm -o $externals/$fn.o $src/Vision/$fn
  link_str+=" $externals/$fn.o "
done

echo -- finally linking
eval $link_str

if [ -e libVisionProxy.so ]; then
  echo -- created libVisionProxy.so
else
  echo -- error building libVisionProxy.so
fi
