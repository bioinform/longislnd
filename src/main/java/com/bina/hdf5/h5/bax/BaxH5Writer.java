package com.bina.hdf5.h5.bax;

/**
 * Created by bayo on 5/4/15.
 */
import java.util.EnumSet;
import org.apache.log4j.Logger;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

public class BaxH5Writer {

    public BaxH5Writer(String filename) {
        filename_ = filename;
        h5_ = new H5File(filename_, FileFormat.CREATE);
    }

    public void writeGroups() throws Exception{
        for(EnumGroups e : EnumSet.allOf(EnumGroups.class)){
            h5_.createGroup(e.path(), null);
        }
    }

    public void close() throws Exception{
        h5_.close();
    }

    private String filename_;
    private H5File h5_ = null;
    private final static Logger log = Logger.getLogger(BaxH5Writer.class.getName());
}
