#!/bin/sh
export MAVEN_OPTS="-Xms2048m -Xmx2048m -XX:PermSize=512m -XX:MaxPermSize=1024m"
if [ -z "$JAVA_HOME" ]
then
   echo "try Java Path for Mac OS"
   export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
fi
echo $JAVA_HOME
mvn clean install
