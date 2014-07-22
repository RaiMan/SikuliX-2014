@echo off
setlocal
set J64=1.7.0_51
set J32=1.7.0_45
set JINC=C:\Program Files (x86)\Java\jdk%J32%\include\win32
set JINCW32=C:\Program Files (x86)\Java\jdk%J32%\include

set BASE=C:\Users\RaiMan\Documents\GitHub\SikuliX-2014
set JNATIVES=%BASE%\Natives\src\main\java
set DEVNATIVE=%BASE%\Natives\src\main\native

rem the swig command for Mac
set swigbin=C:\Users\RaiMan\Downloads\swigwin-3.0.0\swigwin-3.0.0\swig.exe
rem set swigbin=echo

rem the Java package qualifier
set package=org.sikuli.natives

rem where SWIG puts the created Java sources
set odir=%JNATIVES%\org\sikuli\natives

rem where SWIG finds native sources and creates the native wrapper
set ivision=%DEVNATIVE%\Vision

rem includes needed
rem this might have to be adapted
set icv=C:\msys\src\opencv\include

rem this should work with a standard 10.6+ and Java 7 setup
rem in other cases has to be adapted
set ijava=-I"%JINC%" -I"%JINCW32%"

%swigbin% -java -package %package% -outdir %odir% -c++ %ijava% -I%icv%\opencv -I%icv% -I%ivision% -o %ivision%\visionJAVA_wrap.cxx %ivision%\vision.swig
endlocal
