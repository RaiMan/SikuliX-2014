${atChar}echo off
setlocal
set base=%~dp0

set %version%=${sikuli.usedversion}

set source=%base%APIFat\target
set apijar=%source%\sikulixapi-complete-%version%-plain.jar

if exist "%apijar%" goto CANRUN
  echo ----- EROR ----- run collectjars.bat in target/classes
goto FINALLY

:CANRUN
echo -
echo ---
echo ----- SikuliX collecting jars in %base%

set dist=%base%SikuliX-Setup
if not exist "%dist%" goto NODIST
rd /S /Q  %dist%
:NODIST
md %dist%
md %dist%\Downloads

echo ----------------- SourceBase %base%

echo --- collecting jars
java -jar %apijar% noSetup
echo --- content of %source%
dir %source%
dir %source%\Downloads

echo --- copy Setup
copy %apijar% %dist%\sikulixsetup-%version%.jar

echo --- copy Jars
copy %source%\Downloads\* %dist%\Downloads

echo ----------- final content of %dist%
dir %dist%
dir %dist%\Downloads
echo ----------- SikuliX collectjars end -----------
echo -
echo --
echo ----
echo ------
echo -------- If it Looks like success -----------
echo You can move the folder to where you want
echo and name it as you like
echo and run setup, to get your wanted packages.
echo BE SURE, not to download anything,
echo you are doing an offline setup
echo ----------------------------------

:FINALLY
endlocal