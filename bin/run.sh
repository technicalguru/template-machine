#!/bin/bash

BASEDIR=$(dirname "$0")
if [ "$BASEDIR" = "." ]
then
        ROOTDIR=".."
else
        ROOTDIR=$(dirname "$BASEDIR")
fi

CLASSPATH=$(ls $ROOTDIR/templating-*.jar):$ROOTDIR/resources/
for file in $ROOTDIR/lib/*.jar
do
        CLASSPATH=$CLASSPATH:$file
done

java -cp $CLASSPATH templating.Templating "$@"

