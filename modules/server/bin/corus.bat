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

if "%CORUS_SIGAR_ARCH%"=="" (
  if /I "%processor_architecture%" == "amd64" (
    set CORUS_SIGAR_ARCH=win-amd64
  ) else (
    set CORUS_SIGAR_ARCH=win-x86
  ) 
)

"%JAVA_HOME%/bin/java" -Dcorus.home="%CORUS_HOME%" -Djava.library.path="%CORUS_HOME%/extra-lib/sigar/%CORUS_SIGAR_ARCH%" -cp "%CORUS_HOME%\lib\server\*" org.sapia.corus.core.CorusServer %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
