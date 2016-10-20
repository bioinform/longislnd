package com.bina.lrsim.bioinfo;

/**
 * Created by bayolau on 9/4/15.
 */
public class Locus {
  private String chrom;
  private int begin0;
  private int end0;
  private boolean isReverseComplement;

  public Locus(String chrom, int begin0, int end0, boolean isReverseComplement) {
    this.chrom = chrom;
    this.begin0 = begin0;
    this.end0 = end0;
    this.isReverseComplement = isReverseComplement;
  }

  public Locus(Locus other) {
    this.chrom = other.chrom;
    this.begin0 = other.begin0;
    this.end0 = other.end0;
    this.isReverseComplement = other.isReverseComplement;
  }

  public String getChrom() {
    return chrom;
  }

  public void setChrom(String chrom) {
    this.chrom = chrom;
  }

  /**
   * return 0-based start (first coordinate will be 0 for a chromosome).
   * @return
   */
  public int getBegin0() {
    return begin0;
  }

  public void setBegin0(int begin0) {
    this.begin0 = begin0;
  }

  /**
   * return 1-based end (last end will be equal to length of a chromosome).
   * @return
   */
  public int getEnd0() {
    return end0;
  }

  public void setEnd0(int end0) {
    this.end0 = end0;
  }

  public boolean isReverseComplement() {
    return isReverseComplement;
  }

  public void setReverseComplement(boolean reverseComplement) {
    this.isReverseComplement = reverseComplement;
  }
}
