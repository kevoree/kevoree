#!/bin/sh
export MAVEN_OPTS="-Xms2048m -Xmx2048m -XX:PermSize=512m -XX:MaxPermSize=1024m"
export JAVA_HOME=$(/usr/libexec/java_home)
mvn clean install