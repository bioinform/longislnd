#!/usr/bin/env python

import argparse
import logging
import os
import subprocess
import glob
from multiprocessing import Pool

mydir = os.path.dirname(os.path.realpath(__file__))

def Worker(trio):
   subprocess.check_call(trio[0], stdout=open(trio[1],"w"), stderr=open(trio[2], "w"), shell=True)
   return trio
  
if __name__ == "__main__":
    FORMAT = '%(levelname)s %(asctime)-15s %(name)-20s %(message)s'
    logging.basicConfig(level=logging.INFO, format=FORMAT)
    logger = logging.getLogger(__name__)

    parser = argparse.ArgumentParser(description="sample all *.fofn.cmp.h5 alignment into models", formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("--model_dir", help="Directory of the model", default=os.getcwd())
    parser.add_argument("--read_type", help="Read type", default="bax")
    parser.add_argument("--flank", type=int, help="flanks", default=4)
    parser.add_argument("--min_length", type=int, help="minimum read length to consider", default=1000)
    parser.add_argument("--flank_mask", type=int, help="mask out this many bases from alignment", default=100)
    parser.add_argument("--qual", help="minimum qual to sample", default=0.7)
    parser.add_argument("--lrsim", help="Path to lrsim JAR", default=os.path.join(mydir, "LRSim.jar"))
    parser.add_argument("--hdf5", help="Path to HDF5 library", default=os.path.join(mydir, "build", "lib"))
    parser.add_argument("--num_threads", type=int, help="maximum number of concurrent process", default=1)

    args = parser.parse_args()

    if os.path.exists(args.model_dir): assert os.path.isdir(args.model_dir)
    else: os.makedirs(args.model_dir)
    assert os.path.exists(args.model_dir)

    works = []

    for fofn_cmp_h5 in glob.glob("*fofn.cmp.h5"):
      fofn = fofn_cmp_h5[:-7]
      assert os.path.exists(fofn)
      prefix = os.path.join(args.model_dir, ".".join(map(str,(fofn_cmp_h5, args.read_type, args.flank, args.min_length, args.flank_mask))))
      command_line = "java -Djava.library.path={hdf5} -jar {jar} sample {prefix} {fofncmph5} {read_type} {flank} {flank} {min_length} {flank_mask}".format(
          hdf5=args.hdf5,
          jar=args.lrsim,
          prefix=prefix,
          fofncmph5=fofn_cmp_h5,
          read_type=args.read_type,
          flank=args.flank,
          min_length=args.min_length,
          flank_mask=args.flank_mask)
      works.append((command_line, prefix+".log", prefix+".err"))
      command_line = "java -Djava.library.path={hdf5} -jar {jar} region {prefix} {fofn} {read_type} {qual} ".format(
          hdf5=args.hdf5,
          jar=args.lrsim,
          prefix=prefix,
          fofn=fofn,
          read_type=args.read_type,
          qual=args.qual)
      works.append((command_line, prefix+".r.log", prefix+".r.err"))

    pool = Pool(args.num_threads)
    for trio in pool.imap_unordered(Worker, works):
        logger.info("done with " + " ".join(trio))

    logger.info("Done.")
