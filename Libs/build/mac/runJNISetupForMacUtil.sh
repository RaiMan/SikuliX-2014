#!/bin/sh
# native module
mod=MacUtil

# java source folder
cpath=$DEVJAVA

# native source folder
odir=$DEVNATIVE/OSUtil/Mac/

# java class file
class=org.sikuli.natives.$mod

# the generated header file
header=org_sikuli_natives_$mod.h

rm -f $odir/$header
javah -v -jni -classpath $cpath -d $odir $class
