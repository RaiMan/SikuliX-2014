#!/bin/bash
echo -----  Linux build workflow for native modules on a 32-Bit system
# details see inside the respective scripts

# trying to find the active JDK

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

# if JDK not found with the above eval
# JDK=...insert path to JDK... and uncomment

if [ -e $JDK/include ]; then
  # folder(s) containing the header files
  includeParm="-I$JDK/include -I$JDK/include/linux -I/usr/include -I/usr/local/include"
  echo --- The active JDK seems to be in $JDK
fi

if [ "$includeParm" == "" ]; then
  echo JDK could not be found - check and set path manually \(line 30\)
fi

LIBS=/usr/lib
if [ -e $LIBS/libopencv_core.so ]; then
  libFolderO=$LIBS
  libFolderT=$LIBS
  echo --- OpenCV libs seem to be in $LIBS
fi
LIBS=/usr/lib64
if [ "libFolderO" == "" -a -e $LIBS/libopencv_core.so ]; then
  libFolderO=$LIBS
  libFolderT=$LIBS
  echo --- OpenCV libs seem to be in $LIBS
fi
LIBS=/usr/local/lib
if [ "libFolderO" == "" -a -e $LIBS/libopencv_core.so ]; then
  libFolderO=$LIBS
  libFolderT=$LIBS
  echo --- OpenCV libs seem to be in $LIBS
fi

# if the openCV libs are not found with the above eval
# folder containing the OpenCV libs
# libFolderO=...insert path to folder... and uncomment
# folder containing the Tesseract lib
# libFolderT=...insert path to folder... and uncomment

if [ "libFolderO" == "" ]; then
  echo --- OpenCV libs could not be found - check and set paths manually \(line 63 and 65\)
fi

# --------- Please check/adapt the following settings ------------
# SWIG: See the dev docs to find out wether you need to run the SWIG step
# if you want to run the SWIG step: where is your SWIG executable?
# example: /usr/bin/swig 
# leave the setting as is, to not run the SWIG step
export SWIGEXEC=__NOT_SET__
# any specifics should be resolved in runSwigforVision.sh

# set some common folders in the SikuliX package structure
# no need to change normally
export DEVJAVA=../../src/main/java
export DEVNATIVE=../../src/main/native
export DEVLIBS=../../dist/sikulix-native-libs/linux/libs32

# ------------------------- do what is needed
if [ "$SWIGEXEC" == "__NOT_SET__" ]; then
  echo -----  SWIG step intentionally not run since SWIGEXEC is __NOT_SET__
  echo -----  the already SWIG-generated bundled sources will be used
else
  echo -----  use SWIG to create Java interface sources and native wrapper for VisionProxy
  . runSwigForVision.sh
fi  
echo -----  build libVisionProxy.so 
. runBuildVisionProxy.sh
