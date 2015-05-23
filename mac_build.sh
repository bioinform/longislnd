#!/usr/bin/env bash

set -ex

ROOT=${PWD}
BUILD_DIR=${ROOT}/build/
IMAGE_LOC=${BUILD_DIR}/HDFView-2.11.0-Darwin
JAR_LOC=${IMAGE_LOC}/HDFView.app/Contents/Java
LIB_LOC=${IMAGE_LOC}/HDFView.app/Contents/Resources/lib

mkdir ${BUILD_DIR}
cd ${BUILD_DIR}

curl -O "http://www.hdfgroup.org/ftp/HDF5/hdf-java/current/bin/HDFView-2.11.0-Darwin.dmg"
hdiutil attach HDFView-2.11.0-Darwin.dmg -mountroot ${BUILD_DIR}

cp -r ${LIB_LOC} ${BUILD_DIR}/lib

cd ${ROOT}

ORG_NAMES=(
jarnc2obj.jar
slf4j-nop-1.7.5.jar
jarh4obj.jar
jhdfview.jar
fits.jar
jarhdf-2.11.0.jar
jarfitsobj.jar
slf4j-api-1.7.5.jar
netcdf.jar
jarhdf5-2.11.0.jar
jarh5obj.jar
jarhdfobj.jar
)

ARTIFACT=(
jarnc2obj
slf4jnop
jarh4obj
jhdfview
fits
jarhdf
jarfitsobj
slf4japi
netcdf
jarhdf5
jarh5obj
jarhdfobj
)

VERSION=(
0.0.0-SNAPSHOT
1.7.5-SNAPSHOT
0.0.0-SNAPSHOT
0.0.0-SNAPSHOT
0.0.0-SNAPSHOT
2.11.0-SNAPSHOT
0.0.0-SNAPSHOT
1.7.5-SNAPSHOT
0.0.0-SNAPSHOT
2.11.0-SNAPSHOT
0.0.0-SNAPSHOT
0.0.0-SNAPSHOT
)


for (( i =0; i < ${#ORG_NAMES[@]}; ++i ));
do
  mvn install:install-file -DlocalRepositoryPath=repo -DcreateChecksum=true -Dpackaging=jar -Dfile=${JAR_LOC}/${ORG_NAMES[$i]} -DgroupId=com.bina -DartifactId=${ARTIFACT[$i]} -Dversion=${VERSION[$i]}
done
hdiutil detach ${IMAGE_LOC}

mvn package
