#!/bin/sh
#
# This script sets required LOCALCLASSPATH and by default CLASSPATH 
# if no arguments.Otherwise use "set" option to set CLASSPATH
# and use "quiet" option to suppress prinitng of messages
# It must be run by source its content to modify current environment
#    . classpath.sh [build|run] [set] [quiet]
#


LOCALCLASSPATH=.
LOCALCLASSPATH=`echo lib/xpp3/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
LOCALCLASSPATH=`echo lib/xbeans/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
LOCALCLASSPATH=`echo lib/junit/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
LOCALCLASSPATH=`echo lib/dsig_globus/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
LOCALCLASSPATH=`echo lib/opensaml/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
LOCALCLASSPATH=`echo lib/util_concurrent/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
LOCALCLASSPATH=`echo generated/xmlbeans_typelib/*.jar | tr ' ' ':'`:$LOCALCLASSPATH

if [ "$1" = "build" ] ; then 
    LOCALCLASSPATH=`echo lib/ant/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
    LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$LOCALCLASSPATH
    if [ "$2" = "set" ] ; then
        CLASSPATH=$LOCALCLASSPATH
        if [ ! "$3" = "quiet" ] ; then
            echo $LOCALCLASSPATH
        fi
    elif [ ! "$2" = "quiet" ] ; then
        echo $LOCALCLASSPATH
    fi
else 
#    LOCALCLASSPATH=build/samples:build/classes:build/tests:build/dispatcher:$LOCALCLASSPATH
    LOCALCLASSPATH=`echo build/classes/* | tr ' ' ':'`:$LOCALCLASSPATH
    if [ "$1" = "run" ] ; then
        if [ "$2" = "set" ] ; then
            CLASSPATH=$LOCALCLASSPATH
            if [ ! "$3" = "quiet" ] ; then
                echo $LOCALCLASSPATH
            fi
        elif [ ! "$2" = "quiet" ] ; then
            echo $LOCALCLASSPATH
        fi
    else 
        CLASSPATH=$LOCALCLASSPATH
        if [ ! "$1" = "quiet" ] ; then
            echo $CLASSPATH
        fi
    fi
fi
export CLASSPATH

