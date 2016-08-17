#!/usr/bin/env bash
set -ex

#follow http://www.pacb.com/wp-content/uploads/SMRT-Analysis-Software-Installation-v2-3-0.pdf

# this is linux user name, or use smrtanalysis
export SMRT_USER=${USER}
export SMRT_GROUP=${USER}

#installation directory
export SMRT_ROOT=${PWD}/smrtanalysis/
mkdir ${SMRT_ROOT}

PACKAGE=smrtanalysis_2.3.0.140936.run
PATCH=smrtanalysis-patch_2.3.0.140936.p5.run

curl -O http://files.pacb.com/software/smrtanalysis/2.3.0/${PACKAGE}
curl -O https://s3.amazonaws.com/files.pacb.com/software/smrtanalysis/2.3.0/${PATCH}

bash ${PACKAGE} -p ${PATCH} --rootdir ${SMRT_ROOT}
