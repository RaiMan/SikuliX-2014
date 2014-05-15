#!/bin/sh
# native module
mod=MacHotkeyManager

# java source folder
cpath=$CLBASICS

# native source folder
odir=$DEVNATIVE/Hotkey/Mac/

# java class file
class=org.sikuli.basics.$mod

# the generated header file
header=org_sikuli_basics_$mod.h

rm -f $odir/$header
javah -jni -classpath $cpath -d $odir $class
