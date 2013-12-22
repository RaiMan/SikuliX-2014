#!/bin/sh

# ----------------- system specific settings ----------------
# the swig command for Linux (might have to be adapted)
swigbin=$SWIGEXEC

# ----------------- Sikuli specific settings ----------------
# no need to change normally
# the Java package qualifier
package=org.sikuli.natives

# where SWIG puts the created Java sources
odir=$DEVJAVA/org/sikuli/natives

# where SWIG finds native sources and creates the native wrapper
ivision=$DEVNATIVE/Vision

# ----------------- run the SWIG step ----------------
if [ -e $swigbin ]; then
  $swigbin -java -package $package -outdir $odir -c++ $IncludeParm -I$ivision -o $ivision/visionJAVA_wrap.cxx $ivision/vision.swig
else
  echo ---------- SWIG is not found --- skipping this step
  echo --- SWIGEXEC is $SWIGEXEC --- check your SWIG installation
  echo --- for now we run with the already SWIG-gnerated bundled sources
fi
