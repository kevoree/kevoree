#!/bin/bash

APP_PATH="kevoree"

start() {
	echo "Cleaning old logs" 
	rm -rf *.log* 2> /dev/null
	rm -rf nodes 2> /dev/null
	rm -rf *.stderr *.stdout 2> /dev/null

	# for each node
	for i in $(uniq /var/lib/oar/${OAR_JOB_ID});
	do 
		oarsh $i "cd $APP_PATH; mkdir -p nodes;cd nodes;rm -rf kevoree-agent$i;mkdir -p  kevoree-agent$i; cd  kevoree-agent$i ; screen -A -m -d -S kevoree-agent$i ~/java/jre1.6.0_25/bin/java -Dkevoree.location="$HOME/$APP_PATH/org.kevoree.platform.osgi.standalone-1.2.0-SNAPSHOT.jar" -jar ~/$APP_PATH/org.kevoree.platform.agent-1.2.0-SNAPSHOT.jar" & 
	done
}

stop() {
	# for each node
        for i in $(uniq /var/lib/oar/${OAR_JOB_ID});
        do 
		oarsh $i "screen -p 0 -S kevoree-agent$i  -X eval 'stuff \"q\"\015'" &
	done
}

#Start-Stop here
case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    start
    ;;
  *)
  echo "Usage: /etc/init.d/<SERVICE>  {start|stop|restart}"
  exit 1
  ;;
esac

exit 0
