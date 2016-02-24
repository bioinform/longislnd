set -e

ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
JAR=${ROOT_DIR}/LRSim.jar
HDF5_LIB=${ROOT_DIR}/build/lib

OUTPUT_DIR=$1
[ -e ${OUTPUT_DIR} ] && (echo "${OUTPUT_DIR} exists already" && exit 1)
mkdir -p ${OUTPUT_DIR}

MOVIE_ID=clrbam_p6

READ_TYPE=clrbam

SEQ_MODE=shotgun

FASTA=/net/hippo/volumes/wadi/shared/prj/pacbio/chr1/human_g1k_v37_decoy.1.fasta

for stats in ${PWD}/*stats
do
  prefix=${stats:0:-6}
  MODEL_LIST=("${MODEL_LIST[@]}" "$prefix")
done
function join { local IFS="$1"; shift; echo "$*"; }
MODEL_PREFIX=$(join , ${MODEL_LIST[@]})
echo ${MODEL_PREFIX}

NUM_BASES=2077088916

SAMPLE_PER=100

SEED=1351

MIN_FRAG=800
MAX_FRAG=1000000000

MIN_PASS=10
MAX_PASS=15

java -Djava.library.path=${HDF5_LIB} -jar ${JAR} simulate ${OUTPUT_DIR} ${MOVIE_ID} ${READ_TYPE} ${SEQ_MODE} ${FASTA} ${MODEL_PREFIX} ${NUM_BASES} ${SAMPLE_PER} ${SEED} ${MIN_FRAG} ${MAX_FRAG} ${MIN_PASS} ${MAX_PASS}
