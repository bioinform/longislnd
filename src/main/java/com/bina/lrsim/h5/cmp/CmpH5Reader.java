package com.bina.lrsim.h5.cmp;


/**
 * Created by bayo on 5/1/15.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

import org.apache.log4j.Logger;

import com.bina.lrsim.LRSim;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;

public class CmpH5Reader implements EventGroupFactory {
  private final static Logger log = Logger.getLogger(LRSim.class.getName());
  private H5File h5_ = null;
  private AlnIndex AlnIndex_ = null;
  private AlnGroup AlnGroup_ = null;
  private Map<String, AlnData> path_data_ = null;
  private String last_path_;
  private AlnData last_data_;
  private final PBSpec spec;

  public CmpH5Reader(String filename, PBSpec spec) {
    this.spec = spec;
    load(filename);
  }

  public int size() {
    return AlnIndex_.size();
  }

  @Override
  public Iterator<EventGroup> getIterator() {
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
    String path = AlnGroup_.path(AlnIndex_.get(index, EnumIdx.AlnGroupID));
    if (path == null) return null;
    /*
     * AlnData data_ref = path_data_.get(path); if (null == data_ref) { data_ref = new AlnData(h5_, path); path_data_.put(path, data_ref); }
     */
    if (last_path_ == null || !path.equals(last_path_)) {
      log.debug("loading alignment group " + path);
      last_data_ = new AlnData(h5_, path);
      last_path_ = path;
    }
    return new CmpH5Alignment(AlnIndex_.get(index), last_data_, spec);
  }

  public void load(String filename) {
    h5_ = new H5File(filename, FileFormat.READ);
    AlnIndex_ = new AlnIndex(h5_);
    AlnGroup_ = new AlnGroup(h5_);
    path_data_ = new HashMap<String, AlnData>();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(AlnIndex_.toString());
    sb.append(AlnGroup_.toString());
    sb.append("cmp conversion table:\n");
    sb.append(EnumBP.tableToString());
    sb.append("last data_ref alnarray:\n");
    AlnData tmp = new AlnData(h5_, AlnGroup_.path(2));
    sb.append(AlnGroup_.path(2) + "\n");

    byte[] bb = tmp.get(EnumDat.AlnArray);
    sb.append(bb.length);
    for (int ii = 0; ii < 10; ++ii) {
      sb.append(" ");
      sb.append(bb[ii] & 0xff);
    }
    sb.append("\n");

    CmpH5Alignment aa = getEventGroup(2);
    sb.append(aa.toString());
    int[] aln = aa.aln();
    sb.append(aln.length + " " + aa.aln_begin() + " " + aa.aln_end() + "\n");
    for (int ii = 0; ii < 10; ++ii) {
      sb.append(" ");
      sb.append(aln[ii]);
    }
    sb.append("\n");

    return sb.toString();
  }
}
