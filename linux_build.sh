#!/usr/bin/env bash

set -ex
HDF_VIEW=HDFView-2.11
TAR_GZ=${HDF_VIEW}-centos6-x64.tar.gz

ROOT=${PWD}
BUILD_DIR=${ROOT}/build/
JAR_LOC=${BUILD_DIR}/HDFView-2.11.0-Linux/HDF_Group/HDFView/2.11.0/lib/

mkdir ${BUILD_DIR}
cd ${BUILD_DIR}

wget http://www.hdfgroup.org/ftp/HDF5/releases/HDF-JAVA/hdf-java-2.11/bin/${TAR_GZ}
tar xzf ${TAR_GZ}

cd ${HDF_VIEW}
./HDFView-2.11.0-Linux.sh --prefix=${BUILD_DIR}

cd ${BUILD_DIR}
mkdir lib
cp -r ${JAR_LOC}/*\.so* lib

cd ${ROOT}


#jarnc2obj.jar
#slf4j-nop-1.7.5.jar
#jarh4obj.jar
#jhdfview.jar
#fits.jar
#jarhdf-2.11.0.jar
#jarfitsobj.jar
#slf4j-api-1.7.5.jar
#netcdf.jar
ORG_NAMES=(
jarhdf5-2.11.0.jar
jarh5obj.jar
jarhdfobj.jar
)

#jarnc2obj
#slf4jnop
#jarh4obj
#jhdfview
#fits
#jarhdf
#jarfitsobj
#slf4japi
#netcdf
ARTIFACT=(
jarhdf5
jarh5obj
jarhdfobj
)

#0.0.0-SNAPSHOT
#1.7.5-SNAPSHOT
#0.0.0-SNAPSHOT
#0.0.0-SNAPSHOT
#0.0.0-SNAPSHOT
#2.11.0-SNAPSHOT
#0.0.0-SNAPSHOT
#1.7.5-SNAPSHOT
#0.0.0-SNAPSHOT
VERSION=(
2.11.0-SNAPSHOT
0.0.0-SNAPSHOT
0.0.0-SNAPSHOT
)


for (( i =0; i < ${#ORG_NAMES[@]}; ++i ));
do
  mvn install:install-file -DlocalRepositoryPath=repo -DcreateChecksum=true -Dpackaging=jar -Dfile=${JAR_LOC}/${ORG_NAMES[$i]} -DgroupId=com.bina -DartifactId=${ARTIFACT[$i]} -Dversion=${VERSION[$i]}
done

mvn package
