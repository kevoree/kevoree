wget http://www.freepastry.org/FreePastry/FreePastry-2.1.jar 
mvn install:install-file -DgroupId=rice -DartifactId=FreePastry -Dversion=2.1 -Dpackaging=jar -Dfile=$PWD/FreePastry-2.1.jar