#!/bin/sh
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
BASE_RELEASE_DIR=`pwd`
cd kevoree-extra/org.kevoree.extra.root
mvn clean install deploy -Dkevoree.version=1.0.0-RC1
cd $BASE_RELEASE_DIR
echo "current dir "
echo `pwd`
mvn clean install deploy -Dkevoree.version=1.0.0-RC1