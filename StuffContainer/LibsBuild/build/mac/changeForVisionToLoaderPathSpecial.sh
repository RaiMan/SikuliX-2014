#!/bin/sh
# folder of the ready built libs
cd $1
echo -- changing opencv version string from $2 to $3
# since the OpenCV mods internally use the major version to find each other
# which is normally done with symlinks
install_name_tool -change @loader_path/libopencv_core.$2.dylib @loader_path/libopencv_core.$3.dylib libVisionProxy.dylib
install_name_tool -change @loader_path/libopencv_highgui.$2.dylib @loader_path/libopencv_highgui.$3.dylib libVisionProxy.dylib
install_name_tool -change @loader_path/libopencv_imgproc.$2.dylib @loader_path/libopencv_imgproc.$3.dylib libVisionProxy.dylib

otool -L libVisionProxy.dylib
