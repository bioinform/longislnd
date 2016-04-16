#!/home/bayo/lake/opt/py-pacbio/bin/python

import argparse
import sys
import signal
import itertools

from statsmodels.stats.proportion import proportion_confint

def main():
  parser = argparse.ArgumentParser(description='extract and combine the kmer stats of multiple files')
  parser.add_argument('alpha',type=float,nargs='?', default=0.05, help='alpha of confidence interval')
  args = parser.parse_args()

  for line in sys.stdin:
    fields = line.split()
    values = map(int,fields[-4:])
    total = sum(values) * 1.0

    ci = proportion_confint(values[-1], total, args.alpha, method="wilson")
    print line[:-1], values[-1] / total, ci[0], ci[1]

if __name__=='__main__':
  signal.signal(signal.SIGPIPE, signal.SIG_DFL)
  sys.exit(main())
