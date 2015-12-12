#!/bin/sh

if [ "'ARG'$1" = "'ARG'" -o "'ARG'$1" = "'ARG'-h" ]
then
    echo "Usage: javastart.sh [-h] [-o FILE] COMMAND"
    echo "Runs a command in a child process"
    echo ""
    echo " -h    show usage information and exit"
    echo " -o    redirect the process output to the FILE"
    echo ""
    exit 0
fi

if [ "'ARG'$1" = "'ARG'-o" ] 
then
    OUTPUT_FILE=$2
    shift 2

    # Run the command and send the output to the file
    echo "$*" >> ${OUTPUT_FILE}
    echo "" >> ${OUTPUT_FILE}
    ${1+"$@"} 1>> ${OUTPUT_FILE} 2>> ${OUTPUT_FILE} &


else
    # Run the command as is
    ${1+"$@"} &
fi

echo $!
