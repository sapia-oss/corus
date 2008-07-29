#!/bin/sh
#
# corus       This shell script takes care of starting and stopping
#             corus on RedHat or other chkconfig-based system.
#
# The following comments are used by the chkconfig command
#
# chkconfig: 2345 25 75
# description: Sapia Corus is a server that manages Java Virtual Machines.
#
#
# INSTALLATION PROCEDURE (must be root)
# -------------------------------------
#
# 1) Rename this file corus in the directory /etc/init.d and make sure it is executable
#
# 2) Because the services are started from an empty environment, we must explicitly define
#    the required variables $CORUS_HOME. Validate the default value and change it if required.
#
# 3) Create a user named 'corus' and make sure it own the $CORUS_HOME directory.
#
# 4) Test the setup by using the command 'service corus {start|status|stop}' at the command line
#
# 5) Register this startup script using the chkconfig command: 'chkconfig --add corus'
#
# 6) Validate the registration with the command 'chkconfig --list' and make sure corus is in the list
#


# Source function library.
. /etc/rc.d/init.d/functions


# Source networking configuration.
. /etc/sysconfig/network


# Check that networking is up.
if [ ${NETWORKING} = "no" ]
then
  echo "Networking is down"
  exit 0
fi


# Look for CORUS HOME environment and define it if not present
if [ -z "${CORUS_HOME}" ] ; then
    CORUS_HOME=/opt/corus/current
    export CORUS_HOME
fi


# Calling the corus script as the corus user (using the default server port)
su - corus -c "${CORUS_HOME}/bin/corus_service.sh $*"

# Use this command instead to specify an alternate server port
#su - corus -c "${CORUS_HOME}/bin/corus_service.sh $1 33001"
