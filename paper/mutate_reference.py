#!/usr/bin/env python

from Bio import SeqIO
import random

import os
import signal
import argparse

if __name__ == "__main__":
  signal.signal(signal.SIGPIPE, signal.SIG_DFL)

  parser = argparse.ArgumentParser(description='given a genome R, introduce mutation to build genome O, output O->R vcf')
  parser.add_argument('--fasta', type=str, required=True, help='fasta file')
  parser.add_argument('--ins_rate', type=float, default=0.0001, help='rate of ins')
  parser.add_argument('--del_rate', type=float, default=0.0001, help='rate of del')
  parser.add_argument('--sub_rate', type=float, default=0.0001, help='rate of sub')
  parser.add_argument('--flank', type=int, default=10000, help='do not introduce variantion over flank region')
  parser.add_argument('--org_fasta', type=str, default="org.fasta", help='output fasta file')
  parser.add_argument('--vcf', type=str, default="from_org.vcf", help='output vcf file')
  parser.add_argument('--seed', type=int, default=1378, help='seed for random number')
  parser.add_argument('--wiggle', type=int, default=10, help='wiggle room')
  args = parser.parse_args()

  assert os.path.isfile(args.fasta)

  total = [0, 0, 0]

  ACGT = ('A', 'C', 'G', 'T')
  other = { 'A':('C','G','T'), 'C':('A','G','T'), 'G':('A','C','T'), 'T':('A','C','G') }
  random.seed(args.seed)

  with open(args.fasta, "r") as fasta, open(args.org_fasta,"w") as org_fasta, open(args.vcf,"w") as vcf:
    vcf_fields = ["CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFP", "FORMAT", "simulated"]
    vcf.write("#")
    vcf.write("\t".join(vcf_fields))
    vcf.write("\n")
    for seq_record in SeqIO.parse(fasta, "fasta"):
      num_base = len(seq_record)
      if args.flank * 2 >= num_base: continue
      samples = range(args.flank, num_base - args.flank)


      chrom = seq_record.id
      seq = str(seq_record.seq).upper()
      
      loc = sorted([[r, 0, None] for r in random.sample(samples, int(num_base * args.ins_rate + 0.5))] + \
                   [[r, 1, None] for r in random.sample(samples, int(num_base * args.del_rate + 0.5))] + \
                   [[r, 2, None] for r in random.sample(samples, int(num_base * args.sub_rate + 0.5))] \
                  )
      for l in loc:
        if l[1] == 0:
          l[2] = random.choice(ACGT)
          while l[0] > 0 and seq[l[0]] == l[2]: l[0] -= 1
        elif l[1] == 1:
          while l[0] > 0 and seq[l[0]-1] == seq[l[0]]: l[0] -= 1
        elif l[1] == 2:
          l[2] = random.choice(other[seq[l[0]]])
        else:
          raise RuntimeError("type must be 0/1/2")

      org_fasta.write(">%s\n"%(chrom))
      next_index = 0
      org_index = 0
      for l in loc:
        l_index = l[0]
        if l_index + args.wiggle <= next_index: continue

        org_fasta.write(seq[next_index:l_index])
        org_index += l_index - next_index

        l_type = l[1]

        if l_type == 0:
          alt = seq[l_index]
          assert l[2] != alt
          ref = alt + l[2]
          pos = org_index

          org_fasta.write(ref)
          org_index += len(ref)

        elif l_type == 1:
          alt = seq[l_index - 1 : l_index + 1]
          ref = seq[l_index - 1]
          pos = org_index - 1

        elif l_type == 2:
          alt = seq[l_index]
          assert alt != l[2]
          ref = l[2]
          pos = org_index

          org_fasta.write(ref)
          org_index += len(ref)
        else:
          raise RuntimeError("type must be 0/1/2")
        assert alt != ref
        vcf_fields = [chrom, str(pos + 1), ".", ref, alt, ".", ".", ".", "GT", "1/1"]
        vcf.write("\t".join(vcf_fields))
        vcf.write("\n")

        total[l_type] += 1
        next_index = l_index + 1
      org_fasta.write(seq[next_index:num_base])
      org_fasta.write('\n')
  print total
