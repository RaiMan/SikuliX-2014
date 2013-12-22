@echo off
SETLOCAL ENABLEEXTENSIONS
set SIKULIX_HOME=%~dp0

echo +++ trying to start Sikuli Setup in %SIKULIX_HOME%
PATH=%SIKULIX_HOME%libs;%PATH%

java -jar "%SIKULIX_HOME%sikuli-setup-1.1.jar" %*

ENDLOCAL
pause
exit