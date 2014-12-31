#!/bin/bash
# this script compiles and links a Sikuli module on Linux 32 or 64Bit

# abort on the first error
# set -e

if [ "$1" = "32" ]; then
  ARCH=32
fi

if [ "$1" = "64" ]; then
  ARCH=64
fi

if  [ -z "$ARCH" ]; then
  echo  "Bit-Arch missing - Specify 32 or 64"
  exit
fi

DEVJAVA=../../../../API/src/main/java
DEVNATIVE=../../../../Libsvision/src/main/java/native
DEVLIBS=../../../../Libslux/src/main/resources/META-INF/libs/linux/libs$ARCH/

echo -----  Linux build workflow for native modules on $ARCH-bit systems

# trying to find the active JDK
if [ -z "$JDK" ]; then
  for e in `whereis -b javac`; do
    if [ "$e" = "javac:" ]; then
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
  exit
fi

if pkg-config opencv; then
  echo --- OpenCV version $(pkg-config --modversion opencv) found
  libsOpenCV=$(pkg-config --libs opencv)
else
  echo --- pkg-config - OpenCV libs could not be found
  libsOpenCV=
fi

if pkg-config tesseract; then
  echo --- Tesseract version $(pkg-config --modversion tesseract) found
  libsTesseract=$(pkg-config --libs tesseract)
else
  echo --- pkg-config - Tesseract libs could not be found
  libsTesseract=
fi

# native Sikuli sources
src=$DEVNATIVE

# folder(s) containing the header files
includeParm="-I$JDK/include -I$JDK/include/linux -I/usr/include -I/usr/local/include"

# folder for intermediate stuff (not synched on github)
externals=stuff/_ext/VisionProxy
rm -f -R $externals
mkdir -p $externals

list="cvgui.cpp finder.cpp pyramid-template-matcher.cpp sikuli-debug.cpp tessocr.cpp vision.cpp visionJAVA_wrap.cxx"

link_str="-shared -s -fPIC -dynamic -o libVisionProxy.so "

echo ----- now compiling
for fn in ${list}; do
  echo "--  $fn"
  g++ -c -O3 -fPIC -MMD -MP $includeParm -MF $externals/$fn.o.d -o $externals/$fn.o $src/Vision/$fn
  link_str=$link_str" $externals/$fn.o "
done

echo ----- finally linking
rm -f libVisionProxy.so

if [ "" = "$libsOpenCV" ]; then
	link_str=$link_str"/usr/lib/x86_64-linux-gnu/libopencv_core.so "
	link_str=$link_str"/usr/lib/x86_64-linux-gnu/libopencv_highgui.so "
	link_str=$link_str"/usr/lib/x86_64-linux-gnu/libopencv_imgproc.so "
else
	link_str=$link_str$libsOpenCV
fi

if [ "" = "$libsTesseract" ]; then
	link_str=$link_str"/usr/lib/libtesseract.so "
else
	link_str=$link_str$libsTesseract
fi

#echo ---------------------------------
#echo $link_str
#echo ---------------------------------

g++ $link_str

if [ -e libVisionProxy.so ]; then
  echo ----- checking created libVisionProxy.so
  undefined=`ldd -r libVisionProxy.so | grep -c "undefined symbol:"`
  if [ "$undefined" = "0" ]; then
    echo -- should be useable
    if [ -e $DEVLIBS ]; then
      cp libVisionProxy.so $DEVLIBS
      echo -- copied to $DEVLIBS
    fi
  else
    echo -- not useable - has unresolved symbols
    ldd -r libVisionProxy.so | grep "undefined symbol:"
  fi
else
  echo -- error building libVisionProxy.so
fi
