package com.bina.lrsim.pb.h5;

/**
 * Created by bayo on 5/3/15.
 */

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import org.apache.log4j.Logger;

import java.util.Iterator;

public class H5CloneGroups {
  public static void run(String ofile, String ifile) {
    try {
      log.info(ifile + "->" + ofile);
      H5File oh5 = new H5File(ofile, FileFormat.CREATE);
      H5File ih5 = new H5File(ifile, FileFormat.READ);
      cloneGroups(oh5, ih5, "/");
      oh5.close();
    } catch (Exception e) {
      log.info(e, e);
    }
  }

  public static void cloneGroups(H5File oh5, H5File ih5, String fullPath) throws Exception {
    H5Group igrp = (H5Group) ih5.get(fullPath);
    for (Iterator<HObject> it = igrp.getMemberList().iterator(); it.hasNext();) {
      HObject obj = it.next();
      final int oid = obj.open();
      if (oid >= 0) {
        H5O_info_t info = H5.H5Oget_info(oid);
        final int objType = info.type;
        switch (H5Traits.Trait(obj)) {
          case H5Traits.GROUP:
            log.info(obj.getFullName() + " " + objType + " " + H5Traits.Trait(obj) + " " + obj.hasAttribute());
            H5Group grp = (H5Group) oh5.createGroup(obj.getFullName(), null);
            oh5.open();
            if (obj.hasAttribute()) {
              Attributes attributes = new Attributes();
              for (Object oo : obj.getMetadata()) {
                attributes.add((ncsa.hdf.object.Attribute) oo);
              }
              attributes.writeTo(grp);
            }
            cloneGroups(oh5, ih5, obj.getFullName());
            oh5.close();
            break;
          default:
            // do not do anything if it's not a group
            // might add other cases down the line
        }
      } else {
        log.info(oid);
      }
    }
  }

  private final static Logger log = Logger.getLogger(H5CloneGroups.class.getName());
}
