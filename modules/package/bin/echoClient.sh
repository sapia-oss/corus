#! /bin/sh

# Look for JAVA HOME environment variables
if [ -z "${JAVA_HOME}${JAVA_HOME_OVERRIDE}" ] ; then
    echo "ERROR: The variable JAVA_HOME is not defined; cannot start."
    echo "       Please set your JAVA_HOME environment variable or use the -javahome parameter."
    exit 1
fi

# Look for CORUS HOME environment
if [ -z "${CORUS_HOME}" ] ; then
    echo "ERROR: The variable CORUS_HOME is not defined; cannot start."
    echo "       Please set your CORUS_HOME environment variable."
    exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*) cygwin=true ;;
    Darwin*) darwin=true ;;
esac


# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "${JAVA_HOME}" ] &&
        JAVA_HOME=`cygpath --unix "${JAVA_HOME}"`
    [ -n "${JAVA_HOME_OVERRIDE}" ] &&
        JAVA_HOME_OVERRIDE=`cygpath --unix "${JAVA_HOME_OVERRIDE}"`
    [ -n "${CLASSPATH}" ] &&
        CLASSPATH=`cygpath --path --unix "${CLASSPATH}"`
fi

if [ -n "${JAVA_HOME_OVERRIDE}" ] ; then
    if [ -x "${JAVA_HOME_OVERRIDE}/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=${JAVA_HOME_OVERRIDE}/jre/sh/java
    else
        JAVACMD=${JAVA_HOME_OVERRIDE}/bin/java
    fi
else
    if [ -x "${JAVA_HOME}/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=${JAVA_HOME}/jre/sh/java
    else
        JAVACMD=${JAVA_HOME}/bin/java
    fi
fi

if [ ! -x "${JAVACMD}" ] ; then
    echo "Error: JAVA_HOME is not defined correctly."
    echo "  We cannot execute ${JAVACMD}"
    exit 1
fi

MAINCLASS=org.sapia.corus.examples.EchoClient
CORUS_CLASSPATH=""
for JAR in ${CORUS_HOME}/server/lib/*.jar
do
    # if the directory is empty, then it will return the input string
    # this is stupid, so case for it
    if [ "${JAR}" != "${CORUS_HOME}/server/lib/*.jar" ] ; then
        if [ -z "${CORUS_CLASSPATH}" ] ; then
            CORUS_CLASSPATH=${JAR}
        else
            CORUS_CLASSPATH="${JAR}":${CORUS_CLASSPATH}
        fi
    fi
done

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JAVA_HOME=`cygpath --path --windows "${JAVA_HOME}"`
    JAVA_HOME_OVERRIDE=`cygpath --path --windows "${JAVA_HOME_OVERRIDE}"`
    JNDI_CLASSPATH=`cygpath --path --windows "${CORUS_CLASSPATH}"`
fi

${JAVACMD} -Dcorus.home=${CORUS_HOME} -cp ${CORUS_CLASSPATH} ${MAINCLASS} "$@"

