package com.bina.lrsim.pb.h5;

/**
 * Created by bayo on 5/3/15.
 */

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.HObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Attributes {
  public void writeTo(HObject obj) {
    for (Map.Entry<String, Value> entry : nameValue.entrySet()) {
      final Object raw_buffer = entry.getValue().buffer;
      ncsa.hdf.object.Attribute h5Attribute = new ncsa.hdf.object.Attribute(entry.getKey(), EnumH5Type.getH5Datatype(raw_buffer, entry.getValue().dims, entry.getValue().isSigned), entry.getValue().dims, entry.getValue().buffer);
      try {
        obj.writeMetadata(h5Attribute);
      } catch (Exception e) {
        log.info(e, e);
        log.info("attribute write failed for " + entry.getKey());
        throw new RuntimeException(e);
      }
    }
  }

  public void add(List<ncsa.hdf.object.Attribute> in) {
    for (ncsa.hdf.object.Attribute oo : in) {
      this.add(oo);
    }
  }

  public void add(ncsa.hdf.object.Attribute in) {
    /*
     * long[] sizes = in.getDataDims(); StringBuilder sb = new StringBuilder(); sb.append(in.getName()); sb.append(" ");
     * sb.append(in.getType().getDatatypeDescription()); sb.append(" "); sb.append(in.getValue().getClass().getName()); sb.append(" "); sb.append("("); for
     * (long entry : sizes) { sb.append(" " + entry); } sb.append(") "); sb.append(in.getType().getDatatypeClass()); sb.append(" ");
     * sb.append(in.getType().getDatatypeSize()); sb.append(" "); sb.append(in.getType().getDatatypeOrder()); sb.append(" ");
     * sb.append(in.getType().getDatatypeSign());
     */
    /*
     * sb.append("("); sb.append(in.getPropertyKeys().size()); sb.append(")");
     * 
     * for (String pkey : in.getPropertyKeys()) { Object ooo = in.getProperty(pkey); sb.append("p|" + ooo.getClass().getName()); }
     */
    /*
     * log.info(sb.toString());
     */
    add(in.getName(), in.getValue(), in.getDataDims(), in.getType().getDatatypeSign() == Datatype.SIGN_2);
  }

  public void add(String name, Object buffer, long[] dims, boolean isSigned) {
    nameValue.put(name, new Value(buffer, dims, isSigned));
  }

  public void add(final String name, final String[] array) {
    add(name, array, new long[] {array.length}, false);
  }

  public Value get(String key) {
    return nameValue.get(key);
  }

  public static Object extract(HObject from, String key) {
    if (!from.hasAttribute()) return null;
    try {
      Attributes instance = new Attributes();
      instance.add((List<Attribute>) from.getMetadata());
      return instance.get(key).buffer;
    } catch (Exception e) {
      return null;
    }
  }

  private final Map<String, Value> nameValue = new HashMap<>();
  private final static Logger log = Logger.getLogger(Attributes.class.getName());

  private static class Value {
    public Value(Object buffer, long[] dims, boolean isSigned) {
      this.buffer = buffer;
      this.dims = dims;
      this.isSigned = isSigned;
    }

    private final Object buffer;
    private final long[] dims;
    private final boolean isSigned;
  }
}
