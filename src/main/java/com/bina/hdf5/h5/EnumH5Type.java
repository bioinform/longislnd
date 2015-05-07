package com.bina.hdf5.h5;

import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.h5.H5Datatype;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bayo on 5/6/15.
 */
public enum EnumH5Type {
    U8 (byte[].class,   Datatype.CLASS_CHAR,    1,               Datatype.NATIVE, 0),
    U16(short[].class,  Datatype.CLASS_INTEGER, 2,               Datatype.NATIVE, 0),
    U32(int[].class,    Datatype.CLASS_INTEGER, 4,               Datatype.NATIVE, 0),
    F32(float[].class,  Datatype.CLASS_FLOAT,   4,               Datatype.NATIVE, Datatype.NATIVE),
    S  (String[].class, Datatype.CLASS_STRING,  Datatype.NATIVE, Datatype.NATIVE, Datatype.NATIVE);

    public static EnumH5Type getEnum(Class<?> c) {
        return class_parameters_.get(c);
    }

    public Class<?> getType() {
        return class_;
    }

    public int type() {
        return h5Type_;
    }

    public int bytes() {
        return h5NumBytes_;
    }

    public int order() {
        return h5Order_;
    }

    public int sign() {
        return h5Sign_;
    }


    public H5Datatype getH5Datatype(Object obj, long[] dims) {
        int numBytes = this.bytes();
        int sign = this.sign();
        if (obj instanceof String[] && null != dims && dims.length > 1) {
            for (String entry : (String[]) obj) {
                if (numBytes < entry.length()) {
                    numBytes = entry.length();
                }
            }
            ++numBytes;
        }
        //ugly hack
        else if(obj instanceof int[]){
            boolean hasNegative = false;
            for (int entry : (int[]) obj) {
                if (entry < 0){
                    hasNegative = true;
                    break;
                }
            }
            if(hasNegative) {
                sign = Datatype.SIGN_2;
            }
        }
        return new H5Datatype(this.type(), numBytes, this.order(), sign);
    }


    EnumH5Type(Class<?> a, int b, int c, int d, int e) {
        class_ = a;
        h5Type_ = b;
        h5NumBytes_ = c;
        h5Order_ = d;
        h5Sign_ = e;
    }

    private Class<?> class_;
    private int h5Type_;
    private int h5NumBytes_;
    private int h5Order_;
    private int h5Sign_;

    private static final Map<Object, EnumH5Type> class_parameters_ = new HashMap<Object, EnumH5Type>();

    static {
        //WARNING: PacBio uses UNSIGNED integer
        class_parameters_.put(byte[].class, EnumH5Type.U8);
        class_parameters_.put(short[].class, EnumH5Type.U16);
        class_parameters_.put(int[].class, EnumH5Type.U32);
        class_parameters_.put(float[].class, EnumH5Type.F32);
        class_parameters_.put(String[].class, EnumH5Type.S);
    }

}
