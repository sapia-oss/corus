@echo off
setlocal

if "%OS%"=="Windows_NT" goto init
echo This script only works with NT-based versions of Windows.
goto :end

:init
set _APP_HOME=%CORUS_HOME%\bin\win32
set _WRAPPER_CONF=%CORUS_HOME%\config\corus_service_33000.wrapper-win32.properties
if "%CORUS_SIGAR_ARCH%"=="" (
  if /I "%processor_architecture%" == "amd64" (
    set CORUS_SIGAR_ARCH=win-amd64
  ) else (
    set CORUS_SIGAR_ARCH=win-x86
  ) 
)

:exec
rem Install Corus as an NT service with configuration %_WRAPPER_CONF%
"%_APP_HOME%\Wrapper.exe" -p "%_WRAPPER_CONF%" "set.CORUS_SIGAR_ARCH=%CORUS_SIGAR_ARCH%"
if not errorlevel 1 goto end
pause

:end
set _APP_HOME=
set _WRAPPER_CONF=
