package com.bina.lrsim.pb;

/**
 * Created by bayo on 5/2/15.
 */

public enum EnumDat {
  BaseCall(0, "/Basecall", false, false, new Identity()), // for bax
  AlnArray(0, "/AlnArray", false, false, new Identity()), // for cmp
  DeletionQV(1, "/DeletionQV", false, true, new Identity()),
  DeletionTag(2, "/DeletionTag", false, false, new Identity()),
  InsertionQV(3, "/InsertionQV", false, true, new Identity()),
  MergeQV(4, "/MergeQV", false, true, new Identity()),
  QualityValue(5, "/QualityValue", false, true, new Identity()),
  SubstitutionQV(6, "/SubstitutionQV", false, true, new Identity()),
  SubstitutionTag(7, "/SubstitutionTag", false, false, new Identity()),
  IDPV1(8, "/PreBaseFrames", false, false, new IDPCodecV1Compression());
  public static int numBytes = 9;

  public final int value;
  public final String path;
  public final boolean isSigned;
  public final boolean isScore;
  public final ToPrimitiveByteArray mapper;

  EnumDat(int value, String path, boolean isSigned, boolean isScore, ToPrimitiveByteArray mapper) {
    this.value = value;
    this.path = path;
    this.isSigned = isSigned;
    this.isScore = isScore;
    this.mapper = mapper;
  }

  public static interface ToPrimitiveByteArray {
    public byte[] execute(Object obj);
  }

  private static class Identity implements ToPrimitiveByteArray {
    @Override
    public byte[] execute(Object obj) {
      return (byte[]) obj;
    }
  }

  private static class IDPCodecV1Compression implements ToPrimitiveByteArray {
    @Override
    public byte[] execute(Object obj) {
      short[] input = (short[]) obj;
      byte[] ret = new byte[input.length];
      for (int ii = 0; ii < input.length; ++ii) {
        ret[ii] = (byte) IDPCodecV1.framesToCode(input[ii] & 0xFFFF);
      }
      return ret;
    }
  }
}
