#!/bin/sh
# the folder containing the ready built libs
cd $1
# echo ------------------ switching to @loader_path
# the modules to process
for dylib in $2
do
   echo -- processing $2
   chmod ugo+rw $dylib
   #otool -L $dylib
   #echo -------------------- changes to make
   # filter the external refs that have to be processed
   for ref in `otool -L $dylib | grep "lib.*.dylib[^:]" | awk '{print $1'} | grep -v '^/usr/lib' | grep -v '^/System/' | grep -v '^@loader'`
   do
      # echo $ref
      # change to  @loader_path/
      install_name_tool -change $ref @loader_path/`basename $ref` $dylib
   done
   #echo -------------------- changed version
   otool -L $dylib
done
