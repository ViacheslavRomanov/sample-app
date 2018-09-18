#!/bin/sh

cd ${0%${0##*/}}.
SIMPLE_APP_DIR=.

if [ ! -d "${SIMPLE_APP_DIR}/.run" ] ; then
    mkdir ${SIMPLE_APP_DIR}/.run
fi

if [ ! -d "${SIMPLE_APP_DIR}/log" ] ; then
    mkdir ${SIMPLE_APP_DIR}/log
fi

if [ "$1" = "start" ]; then
    nohup  java -jar sample-app.jar > ./log/server.out 2>&1 & echo $! > ./.run/server.pid &
else
  SERVER_PID=`cat ./.run/server.pid`
  kill $SERVER_PID
fi
