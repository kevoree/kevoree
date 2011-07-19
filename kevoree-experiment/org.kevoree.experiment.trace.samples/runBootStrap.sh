#!/bin/bash

echo $*

PWD_OLD=`pwd`
rm -rf bootstrap;mkdir bootstrap;cd bootstrap;~/java/jre1.6.0_25/bin/java -DOAR_NODE_FILE=$OAR_NODE_FILE -jar ../org.kevoree.experiment.modelScript-1.2.0-SNAPSHOT.jar grid $*
cd $PWD_OLD
