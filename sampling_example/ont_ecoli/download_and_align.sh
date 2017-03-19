#!/usr/bin/env bash
set -ex

#ERX708228
wget ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_20_LomanLabz_PC_Ecoli_K12_R7.3.tar
tar xf flowcell_20_LomanLabz_PC_Ecoli_K12_R7.3.tar

#ERX708229
wget ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_32_LomanLabz_K12_His_tag.tar
tar xf flowcell_32_LomanLabz_K12_His_tag.tar

#ERX708230
wget ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_33_LomanLabz_PC_K12_0.4SPRI_Histag.tar
tar xf flowcell_33_LomanLabz_PC_K12_0.4SPRI_Histag.tar

#ERX708231
wget ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_39.tar
tar xf flowcell_39.tar

declare -a FOLDERS=(flowcell_20 flowcell_32 flowcell_33 flowcell_39_K12_Histag)

#extract fastq
for FOLDER in "${FOLDERS[@]}"
do
  java -Djava.library.path=../../build/HDFView-2.11.0-Linux/HDF_Group/HDFView/2.11.0/lib -cp ../../target/*:/home/bayo/longislnd_0.9.4.binary/build/HDFView-2.11.0-Linux/HDF_Group/HDFView/2.11.0/lib/* com.bina.lrsim.LongISLND fast5extract ${FOLDER}
done

#download reference
REF=GCF_000005845.2_ASM584v2_genomic.fa
curl ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/005/845/GCF_000005845.2_ASM584v2/GCF_000005845.2_ASM584v2_genomic.fna.gz | gunzip > ${REF}
SAMTOOLS=../samtools-1.2/samtools
${SAMTOOLS} faidx ${REF}

for FOLDER in "${FOLDERS[@]}"
do
  FASTQ=${FOLDER}.fastq
  ../graphmap/bin/Linux-x64/graphmap align -r ${REF} -d ${FASTQ} | ${SAMTOOLS} view -Shu - | ${SAMTOOLS} sort - ${FASTQ}
  ${SAMTOOLS} index ${FASTQ}.bam
done
