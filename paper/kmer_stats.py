#!/usr/bin/env python

import glob
import signal
import argparse

def starts_with(pattern, line):
  return line.find(pattern) == 0

def repair_line(line):
  return line.replace("A100.00","A 1000.00").replace("T100.00","T 1000.00").replace("C100.00","C 1000.00").replace("G100.00","G 1000.00")

class PatternIterator:
  def __init__(self, f, p):
    self.i = iter(open(f,"r"))
    self.p = p

  def __iter__(self):
    return self

  def next(self):
    while True:
      ret = self.i.next()
      if starts_with(self.p, ret):
        return ret

def process(files, pattern, k):
  data = {}
  for lines in zip( *[ PatternIterator(f, pattern) for f in files ]):
    lines = map(repair_line, lines)
    assert len(set(map(starts_with, [pattern]*len(files), lines))) == 1, map(starts_with, [pattern]*len(files), lines)

    fields = map(lambda instance: instance.split(), lines)
    assert len(set(map(lambda instance: instance[1], fields))) == 1, map(lambda instance: instance[1], fields)
    num_ins = sum(map(lambda instance: int(instance[-4]), fields))
    num_del = sum(map(lambda instance: int(instance[-3]), fields))
    num_sub = sum(map(lambda instance: int(instance[-2]), fields))
    num_mat = sum(map(lambda instance: int(instance[-1]), fields))

    kmer = fields[0][1]
    begin = 0
    end = len(kmer)
    assert end >= k
    if k == None or len(kmer) == k:
      num_tot = 1.0 * (num_ins + num_del + num_sub + num_mat)
      print pattern, kmer, num_ins/num_tot, num_del/num_tot, num_sub/num_tot, num_mat/num_tot, num_ins, num_del, num_sub, num_mat
    else:
      while True:
        if end - begin == k:
          break;
        else:
          begin += 1
        if end - begin == k:
          break;
        else:
          end -= 1
      kmer = kmer[begin:end]
      if kmer not in data:
        data[kmer] = [0, 0, 0, 0]
      data[kmer][0] += num_ins
      data[kmer][1] += num_del
      data[kmer][2] += num_sub
      data[kmer][3] += num_mat
  for kmer in data:
    num_tot = sum(data[kmer]) * 1.0
    print pattern, kmer, data[kmer][0]/num_tot, data[kmer][1]/num_tot, data[kmer][2]/num_tot, data[kmer][3]/num_tot, data[kmer][0], data[kmer][1], data[kmer][2], data[kmer][3]

if __name__ == "__main__":
  signal.signal(signal.SIGPIPE, signal.SIG_DFL)

  parser = argparse.ArgumentParser(description='compute kmer stats')
  parser.add_argument('-k', type=int, default = None, help='kmer size')
  parser.add_argument('--pattern', default="KMER_STATS:", type=str,help='pattern to look for')
  parser.add_argument('--directory', default="", help="glob *summary this directory)")
  args = parser.parse_args()

  files = glob.glob(args.directory+"*summary")
  process(files, args.pattern, args.k)
