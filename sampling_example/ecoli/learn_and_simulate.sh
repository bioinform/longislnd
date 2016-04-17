#!/usr/bin/env bash

../../sample.py --model_dir model --flank 2
../../simulate.py --model_dir model --fasta polished_assembly.fasta
