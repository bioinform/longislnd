set -ex

ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
HDF5_LIB=${ROOT_DIR}/build/lib

OUTPUT_DIR=/Users/bayo/Downloads

MOVIE_ID=FromLRSim

READ_TYPE=bax

FASTA=/Users/bayo/Downloads/ecoli_mod.fasta

MODEL_PREFIX=model

NUM_BASES=235851050

SAMPLE_PER=100

SEED=1351

java -Djava.library.path=${HDF5_LIB} -jar ${ROOT_DIR}/LRSim.jar simulate ${OUTPUT_DIR} ${MOVIE_ID} ${READ_TYPE} ${FASTA} ${MODEL_PREFIX} ${NUM_BASES} ${SAMPLE_PER} ${SEED}
