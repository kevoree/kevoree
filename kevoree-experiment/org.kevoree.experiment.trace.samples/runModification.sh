#!/bin/bash

APP_PATH="kevoree"

PWD_OLD=`pwd`
cd $APP_PATH;rm -rf modification;mkdir -p modification;cd modification;~/java/jre1.6.0_25/bin/java -DOAR_NODE_FILE="$OAR_NODE_FILE" -cp ../org.kevoree.experiment.modelScript-1.2.0-SNAPSHOT.jar org.kevoree.experiment.modelScript.ModificationApp true
cd $PWD_OLD
