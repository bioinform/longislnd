#!/usr/bin/env bash
set -ex

if [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  NUM_THREADS=$( cat /proc/cpuinfo | grep '^processor' | wc -l )
else
  NUM_THREADS=$( sysctl -n hw.ncpu )
fi

git clone https://github.com/isovic/graphmap.git
pushd graphmap
  git reset --hard 1d16f07888b60547094e8257688088d6f00be8af
  make modules
  make -j${NUM_THREADS}
popd

curl -L http://sourceforge.net/projects/samtools/files/samtools/1.2/samtools-1.2.tar.bz2 | tar xj

pushd samtools-1.2
  make -j${NUM_THREADS}
popd
