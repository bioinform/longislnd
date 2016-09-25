#!/usr/bin/env bash

set -ex

NUM_THREADS=1
../../sample.py --model_dir model --flank 4 --num_threads ${NUM_THREADS}
