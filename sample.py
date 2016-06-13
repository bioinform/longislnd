#!/usr/bin/env python

import argparse
import logging
import os
import subprocess
import glob
from multiprocessing import Pool

mydir = os.path.dirname(os.path.realpath(__file__))

def Worker(trio):
   try:
     subprocess.check_call(trio[0], stdout=open(trio[1],"w"), stderr=open(trio[2], "w"), shell=True)
   except subprocess.CalledProcessError as e:
     return (e.returncode, trio)
   return (0, trio)
  
if __name__ == "__main__":
    FORMAT = '%(levelname)s %(asctime)-15s %(name)-20s %(message)s'
    logging.basicConfig(level=logging.INFO, format=FORMAT)
    logger = logging.getLogger(__name__)

    parser = argparse.ArgumentParser(description="sample all alignment files into models", formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("--input_suffix", help="suffix of alignment file", default="fofn.cmp.h5")
    parser.add_argument("--reference", help="reference", default="")
    parser.add_argument("--model_dir", help="Directory of the model", default=os.getcwd())
    parser.add_argument("--read_type", help="Read type", default="bax")
    parser.add_argument("--flank", type=int, help="flanks", default=4)
    parser.add_argument("--min_length", type=int, help="minimum read length to consider", default=1000)
    parser.add_argument("--flank_mask", type=int, help="mask out this many bases from alignment", default=100)
    parser.add_argument("--qual", help="minimum qual to sample", default=0.7)
    parser.add_argument("--lrsim", help="Path to lrsim JAR", default=os.path.join(mydir, "LRSim.jar"))
    parser.add_argument("--hdf5", help="Path to HDF5 library", default=os.path.join(mydir, "build", "lib"))
    parser.add_argument("--num_threads", type=int, help="maximum number of concurrent process", default=1)
    parser.add_argument("--jvm_opt", type=str, help="options to jvm", default="")

    args = parser.parse_args()

    if len(args.reference) > 0:
      args.reference = " --reference %s "%(args.reference)

    if os.path.exists(args.model_dir): assert os.path.isdir(args.model_dir)
    else: os.makedirs(args.model_dir)
    assert os.path.exists(args.model_dir)

    assert args.input_suffix == "fofn.cmp.h5" or args.input_suffix == "fastq.bam", "this convenient script expects alignment would name files such that the read to alignment is either *fofn -> *fofn.cmp.h5, or *fastq -> *fastq.bam"
    alignment_files = glob.glob("*" + args.input_suffix)
    assert len(alignment_files) > 0, "failed to find any alignment files with suffix " + args.input_suffix

    works = []
    for alignment_file in alignment_files:
      n_strip = len(args.input_suffix) - len(args.input_suffix.split(".")[0])
      read_file = alignment_file[:-n_strip]
      assert os.path.exists(read_file), "%s must exists"%(read_file)
      prefix = os.path.join(args.model_dir, ".".join(map(str,(alignment_file, args.read_type, args.flank, args.min_length, args.flank_mask))))
      command_line = "java -Djava.library.path={hdf5} {jvm_opt} -jar {jar} sample --outPrefix {prefix} --inFile {alignment_file} --readType {read_type} --leftFlank {flank} --rightFlank {flank} --minLength {min_length} --flankMask {flank_mask} {reference}".format(
          hdf5=args.hdf5,
          jvm_opt=args.jvm_opt,
          jar=args.lrsim,
          prefix=prefix,
          alignment_file=alignment_file,
          read_type=args.read_type,
          flank=args.flank,
          min_length=args.min_length,
          flank_mask=args.flank_mask,
          reference=args.reference)
      works.append((command_line, prefix+".log", prefix+".err"))
      command_line = "java -Djava.library.path={hdf5} {jvm_opt} -jar {jar} region {prefix} {read_file} {read_type} {qual} ".format(
          hdf5=args.hdf5,
          jvm_opt=args.jvm_opt,
          jar=args.lrsim,
          prefix=prefix,
          read_file=read_file,
          read_type=args.read_type,
          qual=args.qual)
#      works.append((command_line, prefix+".r.log", prefix+".r.err"))

    pool = Pool(args.num_threads)
    error = False
    for (returncode, trio) in pool.imap_unordered(Worker, works):
        spec = " > ".join(trio)
        if returncode == 0:
            logger.info("done with " + spec)
        else:
            logger.error(str(returncode) + " returned by " + spec)
            error = True

    if error:
      logger.error("Error detected")
    else:
      logger.info("Done.")
