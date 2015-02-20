#!/bin/bash

set -e

SCRIPT_PATH=`which $0` 
SCRIPT_DIR=`dirname $SCRIPT_PATH` 
APPENGINE_DIR=$SCRIPT_DIR/../appengine

cp $APPENGINE_DIR/dev.app.yaml $APPENGINE_DIR/app.yaml

