#!/bin/sh
# this script compiles and links a Sikuli module on Linux 32 or 64Bit

# abort on the first error
set -e

if [ -z $1 ]; then
  ARCH=`getconf LONG_BIT`
else
  ARCH=$1
fi

if ! [[ "$ARCH" == "32" || "$ARCH" == "64" ]]; then
  printf  "Usage: $0 [ 32 | 64 ]\nSpecify 32 or 64 bit\n"
  exit
fi

DEVJAVA=../../../../API/src/main/java
DEVNATIVE=../../../../Libsvision/src/main/java/native
DEVLIBS=../../../../Libslux/src/main/resources/META-INF/libs/linux/libs$ARCH/

echo -----  Linux build workflow for native modules on $ARCH-bit systems

# trying to find the active JDK
if [ -z "$JDK" ]; then
	for e in `whereis -b javac`; do
		if [ "$e" == "javac:" ]; then
			continue
		else
			jvc=$e
			break
		fi
	done

	jvcx=
	while [ "$jvc" != "" ]; do
		jvcx=$jvc
		jvc=`readlink -n $jvc`
	done

	JDK=
	if [ "$jvcx" != "" ]; then
		JDK=`dirname $jvcx`
		JDK=`dirname $JDK`
	fi
fi

if [ -e $JDK/include/jni.h ]; then
  # folder(s) containing the header files
  echo --- The active JDK seems to be in $JDK
else
  echo --- JDK could not be found - please set the \"JDK\" environment variable
fi

libsOpenCV=
if pkg-config opencv; then
  echo --- OpenCV version $(pkg-config --modversion opencv) found
  libsOpenCV=$(pkg-config --libs opencv)
else
  echo --- OpenCV libs could not be found
fi

libsTesseract=
if pkg-config tesseract; then
  echo --- Tesseract version $(pkg-config --modversion tesseract) found
  libsTesseract=$(pkg-config --libs tesseract)
else
  echo --- Tesseract libs could not be found
fi

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

if [ "" == "libsOpenCV" ]; then
	link_str+="/usr/lib/x86_64-linux-gnu/libopencv_core.so "
	link_str+="/usr/lib/x86_64-linux-gnu/libopencv_highgui.so "
	link_str+="/usr/lib/x86_64-linux-gnu/libopencv_imgproc.so "
else
	link_str+=$libsOpenCV
fi

if [ "" == "libsTesseract" ]; then
	link_str+="/usr/lib/libtesseract.so "
else
	link_str+=$libsTesseract
fi

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
