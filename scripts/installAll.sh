#!/bin/sh

BASE_RELEASE_DIR=`pwd`

cd $BASE_RELEASE_DIR
sh scripts/installCore.sh

cd $BASE_RELEASE_DIR
sh scripts/installTools.sh

cd $BASE_RELEASE_DIR
sh scripts/installPlatform.sh

cd $BASE_RELEASE_DIR
sh scripts/installLibrary.sh

