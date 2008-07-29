@echo off
set _APP_HOME=%CORUS_HOME%\bin\win32
set _WRAPPER_CONF=%CORUS_HOME%\config\corus_service.properties

"%_APP_HOME%\Wrapper.exe" -i "%_WRAPPER_CONF%"
if not errorlevel 1 goto end
pause

:end
set _APP_HOME=
set _WRAPPER_CONF=
