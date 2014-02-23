@echo off
setlocal
set mversion=1.1
set version=%mversion%.0

set base=%~dp0

echo ----------------- SikuliX collecting jars in %base% 

set source=%base%Setup\target

set dist=%base%..\SikuliX-Setup
if not exist "%dist%" goto NODIST
rd /S /Q  %dist%
:NODIST
md %dist% 
md %dist%\Downloads 

set log=%dist%\collectjars-log.txt

echo --- version --- %version%
echo --- major version --- %mversion%
echo --- version --- %version% >%log%
echo --- major version --- %mversion% >>%log%

echo ----------------- SourceBase %base% >>%log%
echo ----------------- running Maven clean install --- takes some time ...
call mvn clean install >>%log%

REM ----------- Setup
echo --- collecting jars
echo --- collecting jars >>%log%
cd %source% >>%log%
java -jar sikulixsetup-%version%-plain.jar noSetup >>%log%
cd %base%
dir %source% >>%log%

REM ----------- Setup
echo --- copy Setup
echo --- copy Setup >>%log%
copy %source%\sikulixsetup*.jar %dist%\sikulixsetup-%mversion%.jar >>%log%
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