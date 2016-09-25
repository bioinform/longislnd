#!/usr/bin/env bash

set -ex

../../sample.py --model_dir model --flank 3
../../simulate.py --model_dir model --fasta polished_assembly.fasta
