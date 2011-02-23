#!/bin/sh
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
BASE_RELEASE_DIR=`pwd`

#CHANGE TOP VERSION
cd kevoree-platform/org.kevoree.platform.root
mvn clean install deploy