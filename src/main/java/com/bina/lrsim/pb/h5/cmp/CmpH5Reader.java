package com.bina.lrsim.pb.h5.cmp;


/**
 * Created by bayo on 5/1/15.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bina.lrsim.pb.Spec;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

import org.apache.log4j.Logger;

import com.bina.lrsim.LRSim;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;

public class CmpH5Reader implements EventGroupFactory {
  private final static Logger log = Logger.getLogger(LRSim.class.getName());
  private H5File h5 = null;
  private AlnIndex alnIndex = null;
  private AlnGroup alnGroup = null;
  private Map<String, AlnData> pathData = null;
  private String lastPath;
  private AlnData lastData;
  private final Spec spec;

  public CmpH5Reader(String filename, Spec spec) {
    this.spec = spec;
    load(filename);
  }

  public int size() {
    return alnIndex.size();
  }

  @Override
  public Iterator<EventGroup> iterator() {
    return new EventGroupIterator();
  }

  private class EventGroupIterator implements Iterator<EventGroup> {
    private int curr;

    EventGroupIterator() {
      curr = 0;
    }

    @Override
    public boolean hasNext() {
      return curr < size();
    }

    @Override
    public EventGroup next() {
      return getEventGroup(curr++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }

  public CmpH5Alignment getEventGroup(int index) {
    String path = alnGroup.path(alnIndex.get(index, EnumIdx.AlnGroupID));
    if (path == null) return null;
    /*
     * AlnData data_ref = pathData.get(path); if (null == data_ref) { data_ref = new AlnData(h5, path); pathData.put(path, data_ref); }
     */
    if (lastPath == null || !path.equals(lastPath)) {
      log.debug("loading alignment group " + path);
      lastData = new AlnData(h5, path);
      lastPath = path;
    }
    return new CmpH5Alignment(alnIndex.get(index), lastData, spec);
  }

  public void load(String filename) {
    h5 = new H5File(filename, FileFormat.READ);
    alnIndex = new AlnIndex(h5);
    alnGroup = new AlnGroup(h5);
    pathData = new HashMap<>();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(alnIndex.toString());
    sb.append(alnGroup.toString());
    sb.append("cmp conversion table:\n");
    sb.append(EnumBP.tableToString());
    sb.append("last data_ref alnarray:\n");
    AlnData tmp = new AlnData(h5, alnGroup.path(2));
    sb.append(alnGroup.path(2) + "\n");

    byte[] bb = tmp.get(EnumDat.AlnArray);
    sb.append(bb.length);
    for (int ii = 0; ii < 10; ++ii) {
      sb.append(" ");
      sb.append(bb[ii] & 0xff);
    }
    sb.append("\n");

    return sb.toString();
  }
}
