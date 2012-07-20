
#!/bin/sh

BASE_RELEASE_DIR=`pwd`
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"

cd $BASE_RELEASE_DIR
cd kevoree-core/org.kevoree.root
mvn -P eclipse eclipse:eclipse

cd $BASE_RELEASE_DIR
cd kevoree-tools/org.kevoree.tools.root
mvn -P eclipse eclipse:eclipse

cd $BASE_RELEASE_DIR
cd kevoree-corelibrary
mvn -P eclipse eclipse:eclipse

cd $BASE_RELEASE_DIR
cd kevoree-platform/org.kevoree.platform.root
mvn -P eclipse eclipse:eclipse



