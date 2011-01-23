@echo off

@setlocal
set OUTPUT_FILE=
set COMMAND=

if "%1" == "" goto usage
if "%1" == "-h" goto usage
if "%1" == "-o" (
    set OUTPUT_FILE=%2
    shift
    shift
    goto generateCommand
) else (
    goto execNoOutput
)

:generateCommand
set COMMAND=%1
goto execWithOutput


:execWithOutput
cmd /c %COMMAND% >> %OUTPUT_FILE%
goto end



:execNoOutput
echo EXECUTING WITH NO OUTPUT
rem %*
goto end



:usage
    echo Usage: javastart.sh [-h] [-o FILE] COMMAND
    echo Runs a command in a child process
    echo.
    echo  -h    show usage information and exit
    echo  -o    redirect the process output to the FILE
    echo.
    goto end



:end
set OUTPUT_FILE=
set COMMAND=
if "%OS%"=="Windows_NT" @endlocal
