#!/usr/bin/env bash
set -e

# A) download data,
#    1) download assembly and prepare as reference
#    2) download h5 file and creates fofn of the read

# B) given a fasta file
#    1) creates a sa index

# C) given a list of *.fofn files, each storing a list of bax.h5
#    1) filter for high quality region
#    2) align the rads in bax.h5 into cmp.h5
#    3) for each *.fofn, a *.fofn.cmp.h5 will be generated, and can be processed by the sample.py script


#location of the smrtanalysis 2.3 installation
PB23=${PWD}/../smrtanalysis
#load SMRT-Analysis 2.3's env
source ${PB23}/install/smrtanalysis_2.3.0.140936/etc/setup.sh

#download assembly CHM1 assembly using  MHAP
curl -O  http://gembox.cbcb.umd.edu/mhap/asm/human.quiver.all.fasta.gz
gunzip human.quiver.all.fasta.gz
REFERENCE=human.quiver.all.fasta

#index reference
sawriter ${REFERENCE}
REFERENCE_SA=${REFERENCE}.sa

#download a subset (every 7) of CHM1 P6 data
PREFIX="http://sra-download.ncbi.nlm.nih.gov/srapub_files/"
PATTERN="SRR218BAYO_SRR218BAYO_hdf5.tgz"
FRONT=3739
BACK=4039
STEP=7
for index in `seq ${FRONT} ${STEP} ${BACK}`; do
  NAME=$(echo $PATTERN | sed -e "s/BAYO/$index/g")
  URL=${PREFIX}${NAME}
  wget ${URL}
  tar tzf ${NAME} | grep 'bax.h5' > ${index}.fofn
  tar xzf ${NAME}
  rm -f ${NAME}
done

#example of using an SMRTAnalysis 2.3 installation to align reads of all *.fofn file
TMP=${PWD}/pbalign_tmp
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

  pbalign ${FOFN} ${REFERENCE} ${CMPH5} \
          --seed=1 --minAccuracy=0.75 --minLength=50 --concordant --algorithmOptions="-useQuality" \
          --algorithmOptions=" -allowAdjacentIndels -minMatch 12 -bestn 10 -minPctIdentity 80.0 -sa ${REFERENCE_SA} " \
          --hitPolicy=randombest --tmpDir=${TMP} --nproc=${NUM_THREADS} \
          --regionTable=${FILTER_FOFN} || exit $?

  loadPulses ${FOFN} ${CMPH5} -byread -metrics QualityValue,InsertionQV,DeletionQV,SubstitutionQV,DeletionTag,SubstitutionTag,PulseIndex,WidthInFrames,PreBaseFrames,MergeQV || exit 1
done
