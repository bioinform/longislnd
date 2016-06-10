#!/usr/bin/env bash
set -ex

#ERX708228
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_20_LomanLabz_PC_Ecoli_K12_R7.3.tar | tar x

#ERX708229
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_32_LomanLabz_K12_His_tag.tar | tar x

#ERX708230
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_33_LomanLabz_PC_K12_0.4SPRI_Histag.tar | tar x

#ERX708231
curl ftp://ftp.sra.ebi.ac.uk/vol1/ERA411/ERA411499/oxfordnanopore_native/flowcell_39.tar | tarx

#tar xf flowcell_20_LomanLabz_PC_Ecoli_K12_R7.3.tar
#tar xf flowcell_32_LomanLabz_K12_His_tag.tar
#tar xf flowcell_33_LomanLabz_PC_K12_0.4SPRI_Histag.tar
#tar xf flowcell_39.tar
