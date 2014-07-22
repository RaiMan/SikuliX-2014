#!/bin/sh
# this script compiles and links a Sikuli module on Mac
# the module group
mgrp=Hotkey/Mac
# the module name
mname=MacHotkeyManager
sname=$mname.cc
mod=lib$mname.dylib

# specials
lnk="-framework Carbon -framework JavaVM"

# native sources
src=$DEVNATIVE

# folder for intermediate stuff (not synched on github)
externals=stuff/_ext/$mname
rm -f -R $externals
mkdir -p $externals

# needed includes (have to be checked/adapted on your system)
iany=/usr/local/include
ijava=/System/Library/Frameworks/JavaVM.framework/Headers

# modules the linker should know (have to be checked/adapted on your system)

# the compile steps
echo -- $mname
g++ -c -O3 -I$iany -I$ijava -fPIC -MMD -MP -MF $externals/$mname.o.d -o $externals/$mname.o $src/$mgrp/$sname

echo -- finally linking
g++ -o $DEVLIBS/$mod $externals/$mname.o -dynamic -dynamiclib -install_name $mod -Wl,-S -fPIC $lnk
