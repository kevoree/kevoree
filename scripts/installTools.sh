#!/bin/sh
export MAVEN_OPTS="-Xms1024m -Xmx2024m -XX:PermSize=512m -XX:MaxPermSize=512m"
BASE_RELEASE_DIR=`pwd`

#CHANGE TOP VERSION
cd kevoree-tools
mvn clean install