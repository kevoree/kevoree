#!/bin/sh

if [[ $# == 3 ]]; then

echo $1
echo $2
echo $3

dir=`pwd`
echo $dir

$dir/runGregLoggerServer.sh start

sleep 1

$dir/runKevoreeAgents.sh start

sleep 15

$dir/runBootStrap.sh $3

sleep 15

#time=0
delay=$2
startTime=`date +%s`

let "endTime=startTime+$1"

while [[ $endTime -gt `date +%s` ]]; do
	echo $endTime
	echo `date +%s`
        $dir/runModification.sh
#        let "time=time+delay"

        sleep $delay
done

sleep 120

$dir/runKevoreeAgents.sh stop

$dir/runGregLoggerServer.sh stop

fi


