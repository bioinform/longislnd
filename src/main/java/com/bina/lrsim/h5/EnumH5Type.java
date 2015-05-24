package com.bina.lrsim.h5;

import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.h5.H5Datatype;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bayo on 5/6/15.
 */
public enum EnumH5Type {
  I8(byte[].class, Datatype.CLASS_CHAR, 1, Datatype.NATIVE),
  I16(short[].class, Datatype.CLASS_INTEGER, 2, Datatype.NATIVE),
  I32(int[].class, Datatype.CLASS_INTEGER, 4, Datatype.NATIVE),
  F32( float[].class, Datatype.CLASS_FLOAT, 4, Datatype.NATIVE),
  S( String[].class, Datatype.CLASS_STRING, Datatype.NATIVE, Datatype.NATIVE);

  /**
   * return a type based on obj, dims, and sign
   * @param obj a primitive array
   * @param dims dimension of the data, null means that it's scalar
   * @param isSigned for integral primitives, if it's signed or not
   * @return a H5Datatype
   */
  public static H5Datatype getH5Datatype(Object obj, long[] dims, boolean isSigned) {
    final EnumH5Type type = class_parameters_.get(obj.getClass());

    int numBytes = type.h5NumBytes_;
    int sign = Datatype.SIGN_NONE;

    if (obj instanceof String[] ) {
      sign = Datatype.NATIVE;
      //the API cannot handle multidimensional variable string
      if( null != dims && dims.length > 1) {
        for (String entry : (String[]) obj) {
          if (numBytes < entry.length()) {
            numBytes = entry.length();
          }
        }
      }
      ++numBytes;
    }
    else if (obj instanceof float[] ) {
      sign = Datatype.NATIVE;
    }
    else {
      sign = isSigned ? Datatype.SIGN_2 : Datatype.SIGN_NONE;
      /*
      boolean hasNegative = false;
      for (int entry : (int[]) obj) {
        if (entry < 0) {
          hasNegative = true;
          break;
        }
      }
      if (hasNegative) {
        sign = Datatype.SIGN_2;
      }
      */
    }
    return new H5Datatype(type.h5Type_, numBytes, type.h5Order_, sign);
  }


  EnumH5Type(Class<?> a, int b, int c, int d) {
    class_ = a;
    h5Type_ = b;
    h5NumBytes_ = c;
    h5Order_ = d;
  }

  private Class<?> class_;
  private int h5Type_;
  private int h5NumBytes_;
  private int h5Order_;

  private static final Map<Object, EnumH5Type> class_parameters_ = new HashMap<Object, EnumH5Type>();

  static {
    for(EnumH5Type e: EnumSet.allOf(EnumH5Type.class)) {
      class_parameters_.put(e.class_,e);
    }
  }

}
