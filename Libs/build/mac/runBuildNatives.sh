#!/bin/sh
echo -----  Mac build workflow for native modules
# details see inside the respective scripts

# set some common folders
export JBASICS=../../../Basics/src/main/java
export DEVJAVA=../../../Natives/src/main/java
export DEVNATIVE=../../../Natives/src/main/native
export DEVLIBS=../../src/main/resources/META-INF/libs/mac/libs64

echo ----- create JNI header for MacUtil
. runJNISetupForMacUtil.sh

echo ----- build libMacUtil.dylib
. runBuildMacUtil.sh

echo ----- create JNI header for MacHotkeyManager
. runJNISetupForMacHotkeyManager.sh

echo -----  build libMacHotkeyManager.dylib 
. runBuildMacHotkeyManager.sh

echo -----  use SWIG to create Java interface sources and native wrapper for VisionProxy
. runSwigForVision.sh

echo -----  build libVisionProxy.dylib 
. runBuildVisionProxy.sh

echo -----  switch external refs to point to @loader_path/
# so the dependencies are loaded fom same folder as the Sikuli libs
# not needed currently ./changeForAllToLoaderPath.sh $DEVLIBS libMacUtil.dylib
. changeForAllToLoaderPath.sh $DEVLIBS libMacHotkeyManager.dylib
cd $OLDPWD
. changeForAllToLoaderPath.sh $DEVLIBS libVisionProxy.dylib
cd $OLDPWD

echo -----  change the external refs for OpenCV libs to major version string for @loader_path to work
# this is a hack, that has to be checked for necessary changes 
. changeForVisionToLoaderPathSpecial.sh $DEVLIBS 2.4.6 2.4
cd $OLDPWD