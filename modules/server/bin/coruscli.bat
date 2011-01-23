echo off
cls

if not "%CORUS_HOME%"=="" goto okCorusHome

CORUS_HOME environment variable not set.
Set this variable to point to your CORUS
installation directory.

goto end

:okCorusHome

if not "%JAVA_HOME%"=="" goto okJavaHome

JAVA_HOME environment variable not set.
Set this variable to point to your Java
installation directory.

goto end

:okJavaHome

SET LOCALCLASSPATH=

if "%LOCALCLASSPATH_DEFINED%"=="true" goto okLcp

for %%i in (%CORUS_HOME%\lib\server\*.jar) do call %CORUS_HOME%\bin\lcp.bat %%i
for %%i in (%CORUS_HOME%\extra-lib\*.jar) do call %CORUS_HOME%\bin\lcp.bat %%i

set LOCALCLASSPATH_DEFINED=true

:okLcp

rem echo %LOCALCLASSPATH%

set CLASSPATH=%CLASSPATH%;%LOCALCLASSPATH%

"%JAVA_HOME%/bin/java" -Dcorus.home="%CORUS_HOME%" org.sapia.corus.client.cli.CorusCli %1 %2 %3 %4

:end