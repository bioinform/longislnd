#!/usr/bin/env bash
set -ex

#ERX708228
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_20_LomanLabz_PC_Ecoli_K12_R7.3.tar | tar x

#ERX708229
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_32_LomanLabz_K12_His_tag.tar | tar x

#ERX708230
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_33_LomanLabz_PC_K12_0.4SPRI_Histag.tar | tar x

#ERX708231
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_39.tar | tar x

declare -a FOLDERS=(flowcell_20 flowcell_32 flowcell_33 flowcell_39_K12_Histag)

#extract fastq
JVM_OPT=-Djava.library.path=../../build/lib
JAR=../../LongISLND.jar
for FOLDER in "${FOLDERS[@]}"
do
  java ${JVM_OPT} -jar ${JAR} fast5extract fastq ${FOLDER}
done

#download reference
REF=GCF_000005845.2_ASM584v2_genomic.fa
curl ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF_000005845.2_ASM584v2/GCF_000005845.2_ASM584v2_genomic.fna.gz | gunzip > ${REF}
SAMTOOLS=../samtools-1.2/samtools
${SAMTOOLS} faidx ${REF}

for FOLDER in "${FOLDERS[@]}"
do
  FASTQ=${FOLDER}.fastq
  ../graphmap/bin/Linux-x64/graphmap align -r ${REF} -d ${FASTQ} | ${SAMTOOLS} view -Shu - | ${SAMTOOLS} sort - ${FASTQ}
  ${SAMTOOLS} index ${FASTQ}.bam
done
