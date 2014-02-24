@echo off
setlocal
set mversion=1.1
set version=%mversion%.0

set base=%~dp0

echo -
echo ---
echo ----- SikuliX collecting jars in %base% 

set source=%base%Setup\target

set dist=%base%..\SikuliX-Setup
if not exist "%dist%" goto NODIST
rd /S /Q  %dist%
:NODIST
md %dist% 
md %dist%\Downloads 

set log=%dist%\collectjars-log.txt

echo --- version %version%
echo --- major   %mversion%
echo --- version --- %version% >%log%
echo --- major version --- %mversion% >>%log%

echo ----------------- SourceBase %base% >>%log%

if "%1" == "2" goto :RUNPACK
if "%1" == "3" goto :RUNSETUP
echo ----------------- running Maven clean install --- takes some time ...
call mvn clean install >>%log%

find "BUILD FAILURE" %log% | findstr /C:"BUILD FAILURE" >nul
if %ERRORLEVEL% GEQ 1 goto RUNPACK
  echo -
  echo --
  echo ----
  echo ------
  echo -------- BUILD ERROR -----------
  echo at least one module had problems
  echo check the logfile
  echo %log%
  echo correct the problems and run again
  echo ----------------------------------
  goto FINALLY

:RUNPACK
REM ----------- Setup
echo --- collecting jars
echo --- collecting jars >>%log%
cd %source% >>%log%
java -jar sikulixsetup-%version%-plain.jar noSetup >>%log%
cd %base%
dir %source% >>%log%

:RUNSETUP
REM ----------- Setup
echo --- copy Setup
echo --- copy Setup >>%log%
copy %source%\sikulixsetup-%version%-plain.jar %dist%\sikulixsetup-%mversion%.jar >>%log%
dir %dist% >>%log%

REM ----------- Jars
echo --- copy Jars 
echo --- copy Jars >>%log%
copy %source%\Downloads\* %dist%\Downloads >>%log%
dir %dist%\Downloads >>%log%

echo ----------- final content of %dist%
dir %dist%
dir %dist%\Downloads
echo ----------- SikuliX collectjars end -----------

:FINALLY
endlocal