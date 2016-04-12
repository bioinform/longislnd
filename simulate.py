#!/usr/bin/env python

import argparse
import logging
import os
import subprocess
import glob

mydir = os.path.dirname(os.path.realpath(__file__))

class Contig:
    def __init__(self, name, length, sequence=None):
        self.name = name
        self.length = length
        self.sequence = sequence

    def getLength(self):
        return self.length


class ReferenceContigs:
    def __init__(self, filename):
        index_file = "{}.fai".format(filename)

        if not os.path.exists(index_file):
          process = subprocess.Popen(["samtools", "faidx", filename])
          process.communicate()

        self.contigs = []
        with open(index_file) as index_file_fd:
            for line in index_file_fd:
                fields = line.split("\t")
                self.contigs.append(Contig(fields[0], int(fields[1])))

    def get_contigs(self):
        return self.contigs


if __name__ == "__main__":
    FORMAT = '%(levelname)s %(asctime)-15s %(name)-20s %(message)s'
    logging.basicConfig(level=logging.INFO, format=FORMAT)
    logger = logging.getLogger(__name__)

    parser = argparse.ArgumentParser(description="Simulate reads from reference FASTA", formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    parser.add_argument("--fasta", help="FASTA to simulate reads from", required=True)
    parser.add_argument("--lrsim", help="Path to lrsim JAR", default=os.path.join(mydir, "LRSim.jar"))
    parser.add_argument("--hdf5", help="Path to HDF5 library", default=os.path.join(mydir, "build", "lib"))
    parser.add_argument("--model_dir", help="Directory with the model", default=os.path.join(mydir, "run"))
    parser.add_argument("--out", help="Output directory", default="out")
    parser.add_argument("--movie_id", help="Movie id", default="clrbam_p6")
    parser.add_argument("--read_type", help="Read type", default="clrbam")
    parser.add_argument("--sequencing_mode", help="Sequencing mode", default="shotgun")
    parser.add_argument("--coverage", help="Coverage", type=float, default=50)
    parser.add_argument("--sample_per", help="Sample per", type=int, default=100)
    parser.add_argument("--seed", help="Random seed", type=int, default=1351)
    parser.add_argument("--min_frag", help="Minimum length of fragment", type=int, default=800)
    parser.add_argument("--max_frag", help="Maximum length of fragment", type=int, default=1000000000)
    parser.add_argument("--min_pass", help="Minimum passes", type=int, default=10)
    parser.add_argument("--max_pass", help="Maximum passes", type=int, default=15)
    parser.add_argument("--jvm_opt", type=str, help="options to jvm", default="")

    args = parser.parse_args()

    if not os.path.isdir(args.out):
        os.makedirs(args.out)
    else:
        logger.warn("{} already exists".format(args.out))

    # Calculate the number of bases to simulate based on coverage
    base_count = int(sum(map(lambda contig: contig.getLength(), ReferenceContigs(args.fasta).get_contigs())) * args.coverage)

    # Get the model prefix
    model_prefix = ",".join(map(lambda x: os.path.splitext(x)[0], glob.glob(os.path.join(args.model_dir, "*stats"))))

    command_line = "java -Djava.library.path={hdf5} {jvm_opt} -jar {jar} simulate {out} {movie_id} {read_type} {seq_mode} {fasta} {model_prefix} {num_bases} {sample_per} {seed} {min_frag} {max_frag} {min_pass} {max_pass}".format(
        hdf5=args.hdf5,
        jvm_opt=args.jvm_opt,
        jar=args.lrsim,
        out=args.out,
        movie_id=args.movie_id,
        read_type=args.read_type,
        seq_mode=args.sequencing_mode,
        fasta=args.fasta,
        model_prefix=model_prefix,
        num_bases=base_count,
        sample_per=args.sample_per,
        seed=args.seed,
        min_frag=args.min_frag,
        max_frag=args.max_frag,
        min_pass=args.min_pass,
        max_pass=args.max_pass)
    logger.info("Running {}".format(command_line))
    subprocess.check_call(command_line, stdout=open(os.path.join(args.out, "run.out"), "w"), stderr=open(os.path.join(args.out, "run.log"), "w"), shell=True)
    logger.info("Done.")
