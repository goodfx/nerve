#!/bin/bash
cd `dirname $0`
total=0
NERVE_STOP_FILE=`pwd`/.nerve-stop
while [ 1 == 1 ]
do
    if((total==5)); then
        break;
    fi
    cmd=`cat $NERVE_STOP_FILE`;
    if [ "$cmd" == "2" ]; then
        break;
    fi
    ((total+=1))
    sleep 1
done
KILL_WAIT_COUNT=120
#declare -a modules
#while read module
#do
#	modules[${#modules[@]}]="$module"
#done < "$(pwd)/.modules"
stop(){
    pid=$1;
    kill $pid > /dev/null 2>&1
    COUNT=0
    while [ $COUNT -lt ${KILL_WAIT_COUNT} ]; do
        echo -e ".\c"
        sleep 1
        let COUNT=$COUNT+1
        PID_EXIST=`ps -f -p $pid | grep -w $2`
        if [ -z "$PID_EXIST" ]; then
#            echo -e "\n"
#            echo "stop ${pid} success."
            return 0;
        fi
    done

    echo "stop ${pid} failure,dump and kill -9 it."
    kill -9 $pid > /dev/null 2>&1
}

APP_PID=`ps -ef|grep -w "Modules/Nulstar"|grep -v grep|awk '{print $2}'`
if [ -z "${APP_PID}" ]; then
 echo "Nuls wallet not running"
        exit 0
fi
echo "stoping"
for pid in $APP_PID
do
   stop $pid "Modules/Nulstar"
done
#for module in ${modules[@]}
#do
#    APP_PID=`ps -ef|grep -w "Dapp.name=${module}"|grep -v grep|awk '{print $2}'`
#    if [ -n "${APP_PID}" ];
#    then
#        stop $APP_PID "Dapp.name=${module}"
#    fi
#done
echo ""
echo "shutdown success"
if [ -f "./.DONE" ]; then
    rm ./.DONE
fi
