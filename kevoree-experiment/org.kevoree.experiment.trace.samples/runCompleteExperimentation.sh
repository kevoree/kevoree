#!/bin/sh

if [[ $# == 3 ]]; then

echo $1
echo $2
echo $3

dir=`pwd`
echo $dir

#$dir/runGregLoggerServer.sh start

sleep 1

$dir/runKevoreeAgents.sh start

sleep 15

$dir/runBootStrap.sh $3

sleep 15

time=0
delay=$2

while [[ $1 > $time ]]; do
	$dir/runModification.sh
	let "time=time+delay"
	sleep $delay
done

sleep 120

$dir/runKevoreeAgents.sh stop

#$dir/runGregLoggerServer.sh stop

fi


