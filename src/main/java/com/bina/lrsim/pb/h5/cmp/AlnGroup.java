package com.bina.lrsim.pb.h5.cmp;

/**
 * Created by bayo on 5/1/15.
 */

import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

class AlnGroup {

  private final static Logger log = Logger.getLogger(AlnIndex.class.getName());
  private String[] id2Path = null; // the id seems contiguous mostly and there's a small number of
                                    // them

  public AlnGroup(H5File h5) {
    load(h5);
  }

  public String path(int id) {
    if (id < 0 || id >= id2Path.length) return null;
    return id2Path[id];
  }

  public int max_key() {
    return id2Path.length - 1;
  }

  public boolean load(H5File h5) {
    try {
      final int[] d = (int[]) H5ScalarDSIO.Read(h5, "/AlnGroup/ID");
      final String[] s = (String[]) H5ScalarDSIO.Read(h5, "/AlnGroup/Path");

      if (d.length != s.length) throw new RuntimeException("inconsistent AlnGroup");

      // pacbio is using unsigned int, so can't be < 0
      int maxId = -1;
      for (int entry : d) {
        if (entry > maxId) maxId = entry;
      }

      id2Path = new String[maxId + 1];
      for (int ii = 0; ii < d.length; ++ii) {
        id2Path[d[ii]] = s[ii];
      }
    } catch (Exception e) {
      log.info(e, e);
      log.info(e.toString());
      return true;
    }
    return false;

  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int id = 0; id < id2Path.length; ++id) {
      sb.append("AlnGroup " + id + " ");
      sb.append(id2Path[id]);
      sb.append("\n");
    }
    return sb.toString();
  }
}
