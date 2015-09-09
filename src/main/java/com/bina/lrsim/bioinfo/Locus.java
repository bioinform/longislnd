package com.bina.lrsim.bioinfo;

/**
 * Created by bayolau on 9/4/15.
 */
public class Locus {
  private String chrom;
  private int begin0;
  private int end0;
  private boolean rc;

  public Locus(String chrom, int begin0, int end0, boolean rc) {
    this.chrom = chrom;
    this.begin0 = begin0;
    this.end0 = end0;
    this.rc = rc;
  }

  public Locus(Locus other) {
    this.chrom = other.chrom;
    this.begin0 = other.begin0;
    this.end0 = other.end0;
    this.rc = other.rc;
  }

  public String getChrom() {
    return chrom;
  }

  public void setChrom(String chrom) {
    this.chrom = chrom;
  }

  public int getBegin0() {
    return begin0;
  }

  public void setBegin0(int begin0) {
    this.begin0 = begin0;
  }

  public int getEnd0() {
    return end0;
  }

  public void setEnd0(int end0) {
    this.end0 = end0;
  }

  public boolean isRc() {
    return rc;
  }

  public void setRc(boolean rc) {
    this.rc = rc;
  }
}
