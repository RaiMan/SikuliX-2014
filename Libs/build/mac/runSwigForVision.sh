#!/bin/sh
# the swig command for Mac
swigbin=/usr/local/bin/swig

# the Java package qualifier
package=org.sikuli.natives

# where SWIG puts the created Java sources
odir=$JNATIVES/org/sikuli/natives

# where SWIG finds native sources and creates the native wrapper
ivision=$DEVNATIVE/Vision

# includes needed
# this might have to be adapted
icv=/usr/local/include/opencv

# this should work with a standard 10.6+ and Java 7 setup
# in other cases has to be adapted≤
ijava=/System/Library/Frameworks/JavaVM.framework/Headers

if [ -e $swigbin ]; then
  $swigbin -java -package $package -outdir $odir -c++ -I$ijava -I$icv/opencv -I$icv -I$ivision -I/usr/local/include -o $ivision/visionJAVA_wrap.cxx $ivision/vision.swig
else
  echo ---------- swig is not found --- skipping this step --- using the bundled stuff
fi