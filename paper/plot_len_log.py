#!/usr/bin/env python
import pylab
import sys
import argparse
import numpy

def ApproxMedianLength( data ):
  num = len(data)
  if num < 4: return max(data)
  return numpy.median( numpy.array( data[1:-1] ) )

def main():
  parser = argparse.ArgumentParser(description='length distribution')
  parser.add_argument('min_pass_to_print',nargs="?",type=int,default=max,help='minimum passes to print')
  parser.add_argument('max_pass_to_print',nargs="?",type=int,default=max,help='maximum passes to print')
  args = parser.parse_args()

  median_length = []
  pass_count = dict()
  max_pass = 0

  for line in sys.stdin:
    if len(line) < 2: continue
    fields = line.split(" [lengths] score: [")
    if len(fields) != 2: continue
    [lengths, score] = fields[-1].split("] ")
    score = 0.001 * float(score)
    lengths = map(int,lengths.split(','))

    n_passes = len(lengths)
    max_pass = max(max_pass, n_passes)
    if n_passes >= args.min_pass_to_print and n_passes <= args.max_pass_to_print: print lengths, score

    if n_passes not in pass_count: pass_count[n_passes] = 0
    pass_count[n_passes] += 1

    median_length.append(ApproxMedianLength(lengths))

  x = range(max_pass+1)
  y = [0]*len(x)
  for key in pass_count:
    y[key] = pass_count[key]
  pylab.subplot(1,2,1)
  pylab.plot(x,y)
  pylab.subplot(1,2,2)
  pylab.hist(median_length,bins=20)
  pylab.show()

if __name__=='__main__':
  sys.exit(main())
