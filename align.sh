set -ex
#example of using an SMRTAnalysis 2.3 instalation to align reads

# given a list of *.fofn files, each storing a list of bax.h5
# 1) filter for high quality region
# 2) align the rads in bax.h5 into cmp.h5
# 3) for each *.fofn, a *.fofn.cmp.h5 will be generated, and can be processed by the sample.py script

#location of the smrtanalysis 2.3 installation

PB23=/home/bayo/opt/smartanalysis
source ${PB23}/install/smrtanalysis_2.3.0.140936/etc/setup.sh

#location of the imported reference
PB_REFERENCE=/net/kodiak/volumes/delta/shared/home/bayo/work/smrt_userdata/references/CHM1_mhap
PB_REFERENCE_SA=/net/kodiak/volumes/delta/shared/home/bayo/work/smrt_userdata/references/CHM1_mhap/sequence/CHM1_mhap.fasta.sa

#temporary directory
TMP=/mnt/scratch/users/bayo/pbalign_tmp
NUM_THREADS=$( cat /proc/cpuinfo | grep '^processor' | wc -l )

FILTER_DIR=${PWD}/filtered_regions
FILTER_SUMMARY_DIR=${PWD}/filtered_regions_summary
FILTER_FOFN_DIR=${PWD}/filtered_regions_fofn

mkdir -p $FILTER_DIR
mkdir -p $FILTER_SUMMARY_DIR
mkdir -p $FILTER_FOFN_DIR

for FOFN in ${PWD}/*\.fofn
do
  BASENAME=$(basename $FOFN)
  FILTER_SUMMARY=${FILTER_SUMMARY_DIR}/${BASENAME}.csv
  FILTER_FOFN=${FILTER_SUMMARY_DIR}/${BASENAME}.filtered_regions.fofn
  filter_plsh5.py --debug --filter='MinReadScore=0.7500,MinSRL=50,MinRL=50' --trim='True' --outputDir=${FILTER_DIR} --outputSummary=${FILTER_SUMMARY} --outputFofn=${FILTER_FOFN} ${FOFN}

  CMPH5=${FOFN}.cmp.h5

  pbalign ${FOFN} ${PB_REFERENCE} ${CMPH5} \
          --seed=1 --minAccuracy=0.75 --minLength=50 --concordant --algorithmOptions="-useQuality" \
          --algorithmOptions=" -allowAdjacentIndels -minMatch 12 -bestn 10 -minPctIdentity 80.0 -sa ${PB_REFERENCE_SA} " \
          --hitPolicy=randombest --tmpDir=${TMP} --nproc=${NUM_THREADS} \
          --regionTable=${FILTER_FOFN} || exit $?

  loadPulses ${FOFN} ${CMPH5} -byread -metrics QualityValue,InsertionQV,DeletionQV,SubstitutionQV,DeletionTag,SubstitutionTag,PulseIndex,WidthInFrames,PreBaseFrames,MergeQV || exit 1
done
