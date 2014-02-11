@echo off
setlocal
set base=%~dp0
echo SourceBase %base%
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set setup=Basics
set jython=Jython
set api=API
set ide=IDE

set mversion=1.1
set sversion=%mversion%.0
set version=%sversion%-Beta1

set dist=%base%\..\Build
if not exist %dist% (mkdir %dist%)

REM ----------- Setup
echo --- make Setup
cd %base%
call mvn -f Basics\setup* 
copy Basics\target-setup\sikulixsetup-%version%-plain.jar %dist%\sikulixsetup-%mversion%.jar

if "%1%" == "setup" (goto FINALLY)  
  REM ----------- API
  echo --- make API
  cd %base%
  call mvn -f API\api* 
  copy API\target-api\%version%-api-plain.jar %dist%\%version%-2.jar
  
  REM ----------- SikuliX
  echo --- make SikuliX
  cd %base%
  call mvn -f IDE\ide* 
  copy IDE\target-ide\%version%-ide-plain.jar %dist%\%version%-1.jar
  
  REM ----------- MacApp
  echo --- make MacApp
  cd %base%
  copy MacApp\target\*.jar %dist%
  
  REM ----------- Tesseract
  echo --- make Tesseract
  cd %base%
  copy Tesseract\target\*.jar %dist%  
  
  REM ----------- Remote
  echo --- make Remote
  cd %base%
  copy Remote\target\SikuliX-Remote-%sversion%.jar %dist%\%version%-3.jar  

:FINALLY
endlocal