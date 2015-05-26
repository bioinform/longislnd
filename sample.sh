
set -ex

ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
HDF5_LIB=${ROOT_DIR}/build/lib

MODEL_PREFIX=model.2

CMP_H5=/Users/bayo/Downloads/bax9_CHM1htert.cmp.h5

READ_TYPE=bax

BASE_FLANK=3

MIN_LENGTH=1000

FLANK_MASK=100

java -Djava.library.path=${HDF5_LIB} -jar ${ROOT_DIR}/LRSim.jar sample ${MODEL_PREFIX} ${CMP_H5} ${READ_TYPE} ${BASE_FLANK} ${BASE_FLANK} ${MIN_LENGTH} ${FLANK_MASK}
