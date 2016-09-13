#!/usr/bin/env bash

set -ex
HDF_VIEW=HDFView-2.11
TAR_GZ=${HDF_VIEW}-centos6-x64.tar.gz

ROOT=${PWD}
BUILD_DIR=${ROOT}/build/

mkdir ${BUILD_DIR}
cd ${BUILD_DIR}

wget http://www.hdfgroup.org/ftp/HDF5/releases/HDF-JAVA/hdf-java-2.11/bin//${TAR_GZ}
tar xzf ${TAR_GZ}


cd ${HDF_VIEW}
./HDFView-2.11.0-Linux.sh --prefix=${BUILD_DIR}

cd ${ROOT}

mkdir -p target
pushd target
curl -LO http://search.maven.org/remotecontent?filepath=org/apache/commons/commons-math3/3.5/commons-math3-3.5.jar
curl -LO http://search.maven.org/remotecontent?filepath=log4j/log4j/1.2.17/log4j-1.2.17.jar
curl -LO http://search.maven.org/remotecontent?filepath=args4j/args4j/2.33/args4j-2.33.jar
curl -LO http://search.maven.org/remotecontent?filepath=com/github/samtools/htsjdk/1.129/htsjdk-1.129.jar
curl -LO http://search.maven.org/remotecontent?filepath=com/google/guava/guava/16.0.1/guava-16.0.1.jar
curl -LO http://search.maven.org/remotecontent?filepath=org/apache/commons/commons-lang3/3.3.1/commons-lang3-3.3.1.jar
curl -LO http://search.maven.org/remotecontent?filepath=org/lucee/commons-io/2.4.0/commons-io-2.4.0.jar
curl -LO http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar
popd
