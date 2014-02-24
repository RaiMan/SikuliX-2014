Sikuli Libs 2014 (version 1.1.x)
===

The prebuilt native libraries for Windows, Mac and Linux (partially).<br />
(contained in `sikulixsetup.jar`)

It contains a Maven workflow to execute build scripts for the native libraries on the system running on:
 - Windows (not yet ready): WinUtil.dll, VisionProxy.dll
 - Mac: libMacHotkeyManager.dylib, libMacUtil.dylib, libVisionProxy.dylib
 - Linux: libVisionProxy.so

To run it from the project root folder:<br />
`mvn -f Libs/x*`

The build scripts are in the respective system folder in folder `build` <br />
The built libraries will directly go to the respective resources folder. <br />
In case of errors you have to check the prerequisites: [look here ...](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-prepare-for-and-build-the-native-libraries)

For Linux there will still be a supplemental package available on the download page, that allows to build libVisionProxy.so

