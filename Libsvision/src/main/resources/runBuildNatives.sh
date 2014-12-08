#!/bin/bash

if [ -z $1 ]; then
  ARCH=`getconf LONG_BIT`
else
  ARCH=$1
fi

if ! [[ "$ARCH" == "32" || "$ARCH" == "64" ]]; then
  printf  "Usage: $0 [ 32 | 64 ]\nSpecify 32 or 64 bit\n"
  exit
fi

echo -----  Linux build workflow for native modules on $ARCH-bit systems
# details see inside the respective scripts

# trying to find the active JDK

if [ -z "$JDK" ]; then

# this worked on Ubuntu 12
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

# if JDK not found with the above eval
# JDK=...insert path to JDK... and uncomment

if [ -e $JDK/include/jni.h ]; then
  # folder(s) containing the header files
  includeParm="-I$JDK/include -I$JDK/include/linux -I/usr/include -I/usr/local/include"
  echo --- The active JDK seems to be in $JDK
else
  echo --- JDK could not be found - please set the \"JDK\" environment variable
fi

if pkg-config opencv; then
  echo --- OpenCV version $(pkg-config --modversion opencv) found
else
  echo --- OpenCV libs could not be found
fi

if pkg-config tesseract; then
  echo --- Tesseract version $(pkg-config --modversion tesseract) found
else
  echo --- Tesseract libs could not be found
fi

# --------- Please check/adapt the following settings ------------
# SWIG: See the dev docs to find out wether you need to run the SWIG step
# if you want to run the SWIG step: where is your SWIG executable?
# example: /usr/bin/swig 
# leave the setting as is, to not run the SWIG step

export SWIGEXEC="__NOT_SET__"
if [ "$2" == "swig" ]; then
  export SWIGEXEC=/usr/bin/swig
fi

# any specifics should be resolved in runSwigforVision.sh

# set some common folders in the SikuliX package structure
# no need to change normally
export DEVJAVA=../../../../API/src/main/java
export DEVNATIVE=../../../../Libsvision/src/main/java/native
export DEVLIBS=../../../../Libslux/src/main/resources/META-INF/libs/linux/libs$ARCH/

# ------------------------- do what is needed
if [ "$SWIGEXEC" == "__NOT_SET__" ]; then
  echo -----  SWIG step intentionally not run since SWIGEXEC is __NOT_SET__
  echo -----  the already SWIG-generated bundled sources will be used
else
  echo -----  use SWIG to create Java interface sources and native wrapper for VisionProxy
  . runSwigForVision.sh
fi

if [ -n "$includeParm" ]; then
  echo -----  build libVisionProxy.so
  . runBuildVisionProxy.sh
fi
