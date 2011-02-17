#!/bin/sh
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
export KEVOREE_RELEASE=1.0.0-RC2
BASE_RELEASE_DIR=`pwd`

#CHANGE TOP VERSION
mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false

#GO TO LIB DIR
cd kevoree-extra/org.kevoree.extra.root
#CHANGE LIB VERSION
mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false
mvn clean install deploy -Dkevoree.version=$KEVOREE_RELEASE
cd $BASE_RELEASE_DIR
echo "current dir "
echo `pwd`
mvn clean install deploy -Dkevoree.version=$KEVOREE_RELEASE

#cd $BASE_RELEASE_DIR
#cd kevoree-library/org.kevoree.library.root
#echo "current dir "
#mvn clean install deploy -Dkevoree.version=1.0.0-RC2