#!/bin/sh
echo -----  Mac build workflow for native modules for $1-Bit
# details see inside the respective scripts

# set some common folders
export JBASICS=../../../Basics/src/main/java
export CLBASICS=../../../Basics/target/classes
export JNATIVES=../../../Natives/src/main/java
export CLNATIVES=../../../Natives/target/classes
export DEVNATIVE=../../../Natives/src/main/native
export DEVLIBS=../../src/main/resources/META-INF/libs/mac/libs$1

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
. changeForAllToLoaderPath.sh $DEVLIBS libVisionProxy.dylib
cd $OLDPWD
