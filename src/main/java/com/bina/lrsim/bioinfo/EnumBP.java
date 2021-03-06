package com.bina.lrsim.bioinfo;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.Arrays;
import java.util.EnumSet;

public enum EnumBP {
  // value, cmp.h5's 4-bit code, ascii, character
  A(0, 1, 65, 'A'),
  C(1, 2, 67, 'C'),
  G(2, 4, 71, 'G'),
  T(3, 8, 84, 'T'),
  N(4, 15, 78, 'N'), // includes any wild card
  Gap(5, 0, 32, ' '),
  Invalid(6, -1, -1, '-');

  public static EnumBP cmp2Ref(byte cmp) {
    return cmp2ref[cmp & 0xff];
  }

  public static EnumBP cmp2Seq(byte cmp) {
    return cmp2seq[cmp & 0xff];
  }

  EnumBP(int value, int cmp, int ascii, char character) {
    this.value = (byte) value;
    this.cmp = (byte) cmp;
    this.ascii = (byte) ascii;
    this.character = character;
  }

  public final byte value;
  public final byte cmp;
  public final byte ascii;
  public final char character;


  public static byte value2ascii(byte v) {
    return value2ascii_[v];
  }

  private static final byte[] value2ascii_ = new byte[Invalid.value + 1];

  public static byte ascii2value(byte a) {
    return ascii2value_[a];
  }

  private static final byte[] ascii2value_ = new byte[256];

  public static byte ascii_rc(byte a) {
    return ascii_rc_[a];
  }

  private static final byte[] ascii_rc_ = new byte[256];

  public static final EnumBP[] cmp2ref = new EnumBP[256];
  public static final EnumBP[] cmp2seq = new EnumBP[256];

  static {
    Arrays.fill(ascii2value_, Invalid.value);
    for (EnumBP e : EnumSet.of(A, G, C, T, N)) {
      ascii2value_[e.ascii] = e.value;
    }
    ascii2value_['a'] = A.value;
    ascii2value_['c'] = C.value;
    ascii2value_['g'] = G.value;
    ascii2value_['t'] = T.value;
    ascii2value_['n'] = N.value;

    ascii2value_['U'] = T.value;
    ascii2value_['u'] = T.value;

    ascii2value_['R'] = N.value;
    ascii2value_['r'] = N.value;
    ascii2value_['Y'] = N.value;
    ascii2value_['y'] = N.value;
    ascii2value_['K'] = N.value;
    ascii2value_['k'] = N.value;
    ascii2value_['M'] = N.value;
    ascii2value_['m'] = N.value;
    ascii2value_['S'] = N.value;
    ascii2value_['s'] = N.value;
    ascii2value_['W'] = N.value;
    ascii2value_['w'] = N.value;
    ascii2value_['B'] = N.value;
    ascii2value_['b'] = N.value;
    ascii2value_['D'] = N.value;
    ascii2value_['d'] = N.value;
    ascii2value_['H'] = N.value;
    ascii2value_['h'] = N.value;
    ascii2value_['V'] = N.value;
    ascii2value_['v'] = N.value;

    Arrays.fill(ascii_rc_, Invalid.value);
    ascii_rc_[A.ascii] = T.ascii;
    ascii_rc_['a'] = T.ascii;
    ascii_rc_[T.ascii] = A.ascii;
    ascii_rc_['t'] = A.ascii;
    ascii_rc_[C.ascii] = G.ascii;
    ascii_rc_['c'] = G.ascii;
    ascii_rc_[G.ascii] = C.ascii;
    ascii_rc_['g'] = C.ascii;
    ascii_rc_[N.ascii] = N.ascii;
    ascii_rc_['n'] = N.ascii;
    ascii_rc_[' '] = Gap.ascii;

    for (EnumBP e : EnumSet.of(EnumBP.A, EnumBP.G, EnumBP.C, EnumBP.T)) {
      ascii2value_[e.ascii] = e.value;
    }

    Arrays.fill(cmp2ref, Invalid);
    Arrays.fill(cmp2seq, Invalid);
    for (EnumBP seq : EnumSet.allOf(EnumBP.class)) {
      value2ascii_[seq.value] = seq.ascii;
      if (seq.equals(Invalid)) continue;
      final int seq_value = ((int) seq.cmp) << 4;
      for (EnumBP ref : EnumSet.allOf(EnumBP.class)) {
        if (ref.equals(Invalid)) continue;
        final int key = seq_value | (int) ref.cmp;
        cmp2ref[key] = ref;
        cmp2seq[key] = seq;
      }
    }
  }

  public static String tableToString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\nR:");
    for (EnumBP entry : EnumBP.cmp2ref) {
      sb.append(entry.character);
    }
    sb.append("\nQ:");
    for (EnumBP entry : EnumBP.cmp2seq) {
      sb.append(entry.character);
    }
    sb.append("\n");
    return sb.toString();
  }
}
