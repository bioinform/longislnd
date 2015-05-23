package com.bina.lrsim.h5;

/**
 * Created by bayo on 5/3/15.
 */

import ncsa.hdf.object.HObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Attributes {
  public void writeTo(HObject obj) {
    for (Map.Entry<String, Value> entry : name_value_.entrySet()) {
      final Object raw_buffer = entry.getValue().buffer();
      final EnumH5Type type = EnumH5Type.getEnum(raw_buffer.getClass());
      ncsa.hdf.object.Attribute h5Attribute = new ncsa.hdf.object.Attribute(entry.getKey(),
                                                                            type.getH5Datatype(raw_buffer,
                                                                                               entry.getValue().dims()),
                                                                            entry.getValue().dims(),
                                                                            entry.getValue().buffer());
      try {
        obj.writeMetadata(h5Attribute);
      } catch (Exception e) {
        log.info(e, e);
        log.info("attribute write failed for " + entry.getKey());
        throw new RuntimeException(e);
      }
    }
  }

  public void add(ncsa.hdf.object.Attribute in) {
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
    /*
     * sb.append("("); sb.append(in.getPropertyKeys().size()); sb.append(")");
     * 
     * for (String pkey : in.getPropertyKeys()) { Object ooo = in.getProperty(pkey); sb.append("p|"
     * + ooo.getClass().getName()); }
     */
    log.info(sb.toString());
    add(in.getName(), in.getValue(), in.getDataDims());
  }

  public void add(String name, Object buffer, long[] dims) {
    name_value_.put(name, new Value(buffer, dims));
  }

  private final Map<String, Value> name_value_ = new HashMap<String, Value>();
  private final static Logger log = Logger.getLogger(Attributes.class.getName());

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
