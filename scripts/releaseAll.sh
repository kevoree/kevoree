#!/bin/sh

BASE_RELEASE_DIR=`pwd`

cd $BASE_RELEASE_DIR
sh scripts/releaseExtra.sh

cd $BASE_RELEASE_DIR
sh scripts/releaseCore.sh

cd $BASE_RELEASE_DIR
sh scripts/releaseTools.sh

cd $BASE_RELEASE_DIR
sh scripts/releaseLibrary.sh

cd $BASE_RELEASE_DIR
sh scripts/releasePlatform.sh