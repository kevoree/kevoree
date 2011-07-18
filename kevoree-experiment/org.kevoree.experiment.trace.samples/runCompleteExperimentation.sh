#!/bin/sh

if [[ $# == 2 ]]; then

dir=`pwd`
echo $dir

$dir/runGregLoggerServer.sh start

sleep 1

$dir/runKevoreeAgents.sh start

sleep 5

$dir/runBootStrap.sh

sleep 5

time=0
delay=$2

while [[ $1 > $time ]]; do
	$dir/runModification.sh
	let "time=time+delay"
	sleep $delay
done

$dir/runKevoreeAgents.sh stop

$dir/runGregLoggerServer.sh stop

fi


