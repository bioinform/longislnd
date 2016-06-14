#!/usr/bin/env bash

../../sample.py --input_suffix fastq.bam --read_type fastq --model_dir model --flank 3 --reference GCF_000005845.2_ASM584v2_genomic.fa
../../simulate.py --movie_id ONT --read_type fastq --model_dir model --fasta GCF_000005845.2_ASM584v2_genomic.fa
