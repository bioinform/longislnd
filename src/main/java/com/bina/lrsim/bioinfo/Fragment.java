package com.bina.lrsim.bioinfo;

/**
 * Created by bayolau on 9/4/15.
 */
public class Fragment {
  private final byte[] seq;
  private Locus locus = null;

  public Fragment(byte[] seq, Locus locus) {
    this.seq = seq;
    this.locus = locus;
  }

  public Fragment(byte[] seq) {
    this.seq = seq;
  }

  public byte[] getSeq() {
    return seq;
  }

  public Locus getLocus() {
    return locus;
  }

  public void setLocus(Locus locus) {
    this.locus = locus;
  }
}
