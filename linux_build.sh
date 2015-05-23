#!/usr/bin/env bash

set -ex
HDF_VIEW=HDFView-2.11
TAR_GZ=${HDF_VIEW}-centos6-x64.tar.gz

ROOT=${PWD}
build_dir=${ROOT}/build/
JAR_LOC=${build_dir}/HDFView-2.11.0-Linux/HDF_Group/HDFView/2.11.0/lib/

mkdir ${build_dir}
cd ${build_dir}

wget http://www.hdfgroup.org/ftp/HDF5/hdf-java/current/bin/${TAR_GZ}

tar xzf ${TAR_GZ}

cd ${HDF_VIEW}
./HDFView-2.11.0-Linux.sh --prefix=${build_dir}

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

mvn package
