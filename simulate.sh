set -ex

HDF5_LIB=/Applications/HDFView.app/Contents/Resources/lib

MODEL_PREFIX=model

OUTPUT_DIR=/Users/bayo/Downloads

FASTA=/Users/bayo/Downloads/ecoli_mod.fasta

NUM_BASES=235851050

SAMPLE_PER=100

SEED=1351

java -Djava.library.path=${HDF5_LIB} -jar H5Test.jar simulate ${OUTPUT_DIR} ${FASTA} ${MODEL_PREFIX} ${NUM_BASES} ${SAMPLE_PER} ${SEED}
