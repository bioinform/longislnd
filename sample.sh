
set -e

ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
JAR=${ROOT_DIR}/LRSim.jar
HDF5_LIB=${ROOT_DIR}/build/lib

READ_TYPE=bax
BASE_FLANK=4
MIN_LENGTH=1000
FLANK_MASK=100

for FOFNCMPH5 in ${PWD}/*\.fofn\.cmp\.h5
do
  FOFN=${FOFNCMPH5:0:-7}
  echo "sampling from ${FOFNCMPH5} and ${FOFN}"
  [ -f ${FOFN} ] || (echo "${FOFN}, list of reads H5, not found" && exit 1)
  MODEL_PREFIX=${FOFNCMPH5}.${READ_TYPE}.${BASE_FLANK}.${MIN_LENGTH}.${FLANK_MASK}
  java -Djava.library.path=${HDF5_LIB} -jar ${JAR} sample ${MODEL_PREFIX} ${FOFNCMPH5} ${READ_TYPE} ${BASE_FLANK} ${BASE_FLANK} ${MIN_LENGTH} ${FLANK_MASK} 2>&1 | tee ${MODEL_PREFIX}.log
  java -Djava.library.path=${HDF5_LIB} -jar ${JAR} region ${MODEL_PREFIX} ${FOFN} ${READ_TYPE} 0.1 2>&1 | tee ${MODEL_PREFIX}.r.log
done
