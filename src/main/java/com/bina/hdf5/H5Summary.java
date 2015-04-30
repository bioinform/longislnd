package com.bina.hdf5;

/**
 * Created by bayo on 4/30/15.
 */

import java.util.List;
import java.util.Iterator;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.ScalarDS;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.apache.log4j.Logger;

public class H5Summary {
    public static void list_data(H5File h5, String full_path)throws Exception{
        H5ScalarDS obj = (H5ScalarDS)h5.get(full_path);
        obj.init();
        log.info(obj.getClass().getName());
        log.info(obj.getDims().length);
        long[] dims = obj.getDims();
        for(long entry:dims){
            log.info(entry);
        }
    }
    public static void list_group(H5File h5, String full_path) throws Exception{
        H5Group grp = (H5Group)h5.get(full_path);
        List<HObject> memberList = grp.getMemberList();
        Iterator<HObject> it = memberList.iterator();
        while (it.hasNext()) {
            HObject obj = it.next();
            int o_id = obj.open();
            if(o_id>=0){
                H5O_info_t info = H5.H5Oget_info(o_id);
                int objType = info.type;
                log.info(obj.getFullName()+" "+objType+" "+obj.getClass().getName()+" "+ H5Traits.Trait(obj));
                switch(H5Traits.Trait(obj)){
                    case H5Traits.GROUP:
                        list_group(h5,obj.getFullName());
                        break;
                    case H5Traits.DATASET:
                        try {
                            list_data(h5, obj.getFullName());
                        }
                        catch(Exception e){
                            log.info(e.getMessage());
                        }
                        break;
                    default:
                }
            }
            else{
                log.info(o_id);
            }
        }
    }
    public static void run(String filename){
        try {
            CmpH5Reader ch5 = new CmpH5Reader(filename);
            log.info(ch5.toString());
/*
            H5File h5 = new H5File(filename);
            log.info("getting group");

            list_group(h5, "/");

            log.info("Closing");
            h5.close();
*/
            log.info("Finished");
        }
        /*
        catch(HDF5Exception e){
            log.info("exception thrown");
        }
        */
        catch(Exception e){
            log.info(e,e);
        }
    }
    private final static Logger log = Logger.getLogger(H5Test.class.getName());
}
