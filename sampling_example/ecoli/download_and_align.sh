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
source ${PB23}/install/smrtanalysis_2.3.0.140936/etc/setup.sh

#download pacbio's assembly according to https://github.com/PacificBiosciences/DevNet/wiki/E.-coli-Bacterial-Assembly
curl -O https://s3.amazonaws.com/files.pacb.com/datasets/secondary-analysis/e-coli-k12-P6C4/polished_assembly.fastq.gz
gunzip polished_assembly.fastq.gz

REFERENCE=polished_assembly.fasta
head -n1 polished_assembly.fastq | sed -e 's/^@/>/' > ${REFERENCE}
head -n2 polished_assembly.fastq | tail -n1 | fold -w 80 >> ${REFERENCE}
sawriter ${REFERENCE}
REFERENCE_SA=${REFERENCE}.sa

#download pacbio's data according to https://github.com/PacificBiosciences/DevNet/wiki/E.-coli-Bacterial-Assembly
FILE=p6c4_ecoli_RSII_DDR2_with_15kb_cut_E01_1.tar.gz
FOFN=p6_ecoli.fofn

curl -O https://s3.amazonaws.com/files.pacb.com/datasets/secondary-analysis/e-coli-k12-P6C4/${FILE}
tar xzf ${FILE}
find ${PWD}/E01_1 -name '*bax.h5' > ${FOFN}

#example of using an SMRTAnalysis 2.3 installation to align reads
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
