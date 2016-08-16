#!/usr/bin/env bash

set -ex
HDF_VIEW=HDFView-2.11
TAR_GZ=${HDF_VIEW}-centos6-x64.tar.gz

ROOT=${PWD}
BUILD_DIR=${ROOT}/build/
JAR_LOC=${BUILD_DIR}/HDFView-2.11.0-Linux/HDF_Group/HDFView/2.11.0/lib/

mkdir ${BUILD_DIR}
cd ${BUILD_DIR}

wget http://www.hdfgroup.org/ftp/HDF5/hdf-java/current/bin/${TAR_GZ}
tar xzf ${TAR_GZ}

cd ${HDF_VIEW}
./HDFView-2.11.0-Linux.sh --prefix=${BUILD_DIR}

cd ${BUILD_DIR}
mkdir lib
cp -r ${JAR_LOC}/*\.so* lib

cd ${ROOT}
