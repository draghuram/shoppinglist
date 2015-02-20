#!/bin/bash

mvn -q dependency:copy-dependencies

pwd=`pwd`

for f in `ls target/dependency/*.jar`
do
    CLASSPATH=$CLASSPATH:$pwd/$f
done

export CLASSPATH

