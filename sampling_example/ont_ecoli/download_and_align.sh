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

#extract fastq
JVM_OPT=-Djava.library.path=../../build/lib
JAR=../../LRSim.jar
java ${JVM_OPT} -jar ${JAR} fast5extract fastq flowcell_20
java ${JVM_OPT} -jar ${JAR} fast5extract fastq flowcell_32
java ${JVM_OPT} -jar ${JAR} fast5extract fastq flowcell_33
java ${JVM_OPT} -jar ${JAR} fast5extract fastq flowcell_39_K12_Histag
