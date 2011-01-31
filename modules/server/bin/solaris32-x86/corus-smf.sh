#!/bin/sh
# Start/stop Sapia Corus server from Solaris SMF
#
. /lib/svc/share/smf_include.sh

if [ ! -d "${CORUS_HOME}/logs" ] ; then
    su - corus -c "mkdir "$CORUS_HOME/logs""
fi

echo `date`" :: $0 $*" >>$CORUS_HOME/logs/corus-smf.log

case "$1" in
'start')
        su - corus -c "$CORUS_HOME/bin/corus_service.sh $*" >>$CORUS_HOME/logs/corus-smf.log
        sleep 5
        ;;
'restart')
        su - corus -c "$CORUS_HOME/bin/corus_service.sh $*" >>$CORUS_HOME/logs/corus-smf.log
        sleep 5
        ;;
'stop')
        su - corus -c "$CORUS_HOME/bin/corus_service.sh $*" >>$CORUS_HOME/logs/corus-smf.log
        ;;
*)
        echo "Usage: $0 { start | retsart | stop }"
        exit 1
        ;;
esac
exit $SMF_EXIT_OK
