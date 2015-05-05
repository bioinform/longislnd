package com.bina.hdf5.h5;

/**
 * Created by bayo on 5/3/15.
 */

import java.lang.Class;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.object.HObject;

public class Attributes {
    public void writeTo(HObject obj) throws Exception {
        for (Map.Entry<String, Value> entry : name_value_.entrySet()) {
            final Object raw_buffer = entry.getValue().buffer();
            final EnumType type = EnumType.getEnum(raw_buffer.getClass());
            ncsa.hdf.object.Attribute h5Attribute = null;
            int num_bytes = type.bytes();
            if (raw_buffer instanceof String[] && entry.getValue().dims().length != 1) {
                for (String s : (String[]) entry.getValue().buffer()) {
                    if (num_bytes < s.length()) {
                        num_bytes = s.length();
                    }
                }
                ++num_bytes;
            }
            h5Attribute = new ncsa.hdf.object.Attribute(entry.getKey(),
                    new H5Datatype(type.type(), num_bytes, type.order(), type.sign()),
                    entry.getValue().dims(),
                    entry.getValue().buffer());
            try {
                obj.writeMetadata(h5Attribute);
            } catch (Exception e) {
                log.info(e, e);
                log.info("attribute write failed for " + entry.getKey());
                throw e;
            }
        }
    }

    public <T> void add(ncsa.hdf.object.Attribute in) {
        long[] sizes = in.getDataDims();
        StringBuilder sb = new StringBuilder();
        sb.append(in.getName());
        sb.append(" ");
        sb.append(in.getType().getDatatypeDescription());
        sb.append(" ");
        sb.append(in.getValue().getClass().getName());
        sb.append(" ");
        sb.append("(");
        for (long entry : sizes) {
            sb.append(" " + entry);
        }
        sb.append(") ");
        sb.append(in.getType().getDatatypeClass());
        sb.append(" ");
        sb.append(in.getType().getDatatypeSize());
        sb.append(" ");
        sb.append(in.getType().getDatatypeOrder());
        sb.append(" ");
        sb.append(in.getType().getDatatypeSign());
        sb.append("(");
        sb.append(in.getPropertyKeys().size());
        sb.append(")");

        for (String pkey : in.getPropertyKeys()) {
            Object ooo = in.getProperty(pkey);
            sb.append("p|" + ooo.getClass().getName());
        }
        log.info(sb.toString());
        add(in.getName(), in.getValue(), in.getDataDims());
    }

    public void add(String name, Object buffer, long[] dims) {
        name_value_.put(name, new Value(buffer, dims));
    }

    private final Map<String, Value> name_value_ = new HashMap<String, Value>();
    private final static Logger log = Logger.getLogger(Attributes.class.getName());

    private static enum EnumType {
        U8(byte[].class, Datatype.CLASS_CHAR, 1, Datatype.NATIVE, 0),
        U16(short[].class, Datatype.CLASS_INTEGER, 2, Datatype.NATIVE, 0),
        U32(int[].class, Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, 0),
        F32(float[].class, Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, Datatype.NATIVE),
        S(String[].class, Datatype.CLASS_STRING, 16, Datatype.NATIVE, Datatype.NATIVE);

        public static EnumType getEnum(Class<?> c) {
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

        EnumType(Class<?> a, int b, int c, int d, int e) {
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

        private static final Map<Object, EnumType> class_parameters_ = new HashMap<Object, EnumType>();

        static {
            //WARNING: PacBio uses UNSIGNED integer
            class_parameters_.put(byte[].class, EnumType.U8);
            class_parameters_.put(short[].class, EnumType.U16);
            class_parameters_.put(int[].class, EnumType.U32);
            class_parameters_.put(float[].class, EnumType.F32);
            class_parameters_.put(String[].class, EnumType.S);
        }

    }

    private static class Value {
        public Value(Object buffer, long[] dims) {
            buffer_ = buffer;
            dims_ = dims;
        }

        Object buffer() {
            return buffer_;
        }

        long[] dims() {
            return dims_;
        }

        private Object buffer_ = null;
        private long[] dims_ = null;
    }
}
