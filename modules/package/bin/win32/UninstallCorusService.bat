@echo off
setlocal

if "%OS%"=="Windows_NT" goto init
echo This script only works with NT-based versions of Windows.
goto :end

:init
set _APP_HOME=%CORUS_HOME%\bin\win32
set _WRAPPER_CONF=%CORUS_HOME%\config\corus_service_33000.wrapper-win32.properties

:exec
rem Uninstall Corus as an NT service for configuration %_WRAPPER_CONF%
"%_APP_HOME%\Wrapper.exe" -r %_WRAPPER_CONF%
if not errorlevel 1 goto end
pause

:end
set _APP_HOME=
set _WRAPPER_CONF=

