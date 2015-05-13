
set -ex

HDF5_LIB=/Applications/HDFView.app/Contents/Resources/lib

MODEL_PREFIX=model.2

CMP_H5=/Users/bayo/Downloads/bax9_CHM1htert.cmp.h5

BASE_FLANK=3

MIN_LENGTH=1000

FLANK_MASK=100

java -Djava.library.path=${HDF5_LIB} -jar H5Test.jar sample ${MODEL_PREFIX} ${CMP_H5} ${BASE_FLANK} ${BASE_FLANK} ${MIN_LENGTH} ${FLANK_MASK}
