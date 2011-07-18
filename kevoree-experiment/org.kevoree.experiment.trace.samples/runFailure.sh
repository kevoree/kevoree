#!/bin/bash

PWD_OLD=`pwd`
mkdir failure;cd failure;~/java/jre1.6.0_25/bin/java -DOAR_NODE_FILE="$OAR_NODE_FILE" -cp ../org.kevoree.experiment.modelScript-1.2.0-SNAPSHOT.jar org.kevoree.experiment.modelScript.FailureApp true $*
cd $PWD_OLD
