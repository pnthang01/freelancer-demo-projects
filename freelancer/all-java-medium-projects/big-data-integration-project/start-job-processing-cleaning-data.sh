#!/bin/bash

# where supervisor's jar to use (absolute path)
JOB_JAR="./big-data-integration-1.0.jar Class flc.social.process.FacebookDataRetriever-flc.social.process.YoutubeDataRetriever-flc.social.process.InstagramDataRetriever Name job-processing-cleaning-data Param {}"
echo "jar path=$JOB_JAR"

# where java to run
JAVA="java"

# JVM performance options
if [ -z "$JVM_PERFORMANCE_OPTS" ]; then
  JVM_PERFORMANCE_OPTS="-server -Xmx512m -Xms512m -XX:MaxMetaspaceSize=128m -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=300 -XX:ParallelGCThreads=4 -XX:ConcGCThreads=2 -XX:InitiatingHeapOccupancyPercent=85 -verbosegc -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime"
fi

# GC options
GC_FILE_SUFFIX='-gc.log'
GC_LOG_FILE_NAME=''
if [ "$1" = "daemon" ] && [ -z "$GC_LOG_OPTS"] ; then
  shift
  GC_LOG_FILE_NAME=$1$GC_FILE_SUFFIX
  shift
  GC_LOG_OPTS="-Xloggc:$LOG_DIR/$GC_LOG_FILE_NAME -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps"
fi

echo $JAVA $HEAP_OPTS $JVM_PERFORMANCE_OPTS $GC_LOG_OPTS -jar $JOB_JAR
nohup $JAVA $HEAP_OPTS $JVM_PERFORMANCE_OPTS $GC_LOG_OPTS -jar $JOB_JAR > ./test.log 2>&1 &
rm -f nohup.out
