#!/bin/sh
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
export KEVOREE_RELEASE=$1
BASE_RELEASE_DIR=`pwd`

echo "Release version"
echo $KEVOREE_RELEASE

#CHANGE EXTRA VERSION
#cd kevoree-extra/org.kevoree.extra.root
#mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false

cd $BASE_RELEASE_DIR

#CHANGE TOP VERSION
cd kevoree-core/org.kevoree.root
mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false

cd $BASE_RELEASE_DIR

#CHANGE TOOLS VERSION
cd kevoree-tools/org.kevoree.tools.root
mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false

cd $BASE_RELEASE_DIR

#CHANGE LIBRARY VERSION
cd kevoree-library
mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false

cd $BASE_RELEASE_DIR

#CHANGE PLATFORM VERSION
cd kevoree-platform/org.kevoree.platform.root
mvn versions:set -DnewVersion=$KEVOREE_RELEASE -DgenerateBackupPoms=false