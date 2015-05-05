package com.bina.hdf5;

import com.bina.hdf5.h5.H5Summary;
import com.bina.hdf5.h5.H5CloneGroups;
import com.bina.hdf5.h5.bax.BaxH5Writer;
import org.apache.log4j.Logger;

import java.util.Arrays;
import com.bina.hdf5.h5.cmp.CmpH5Reader;

/**
 * Created by bayo on 4/30/15.
 */
public class H5Test {

    private final static Logger log = Logger.getLogger(H5Test.class.getName());

    String VERSION = "H5Test " + getClass().getPackage().getImplementationVersion();

    public void run(String[] args) {
        String usage = "java -jar H5test.jar <mode> <h5file> \n"
                + "       mode    -- cmp/read/clonegroups \n"
                + "       h5file    -- h5 file to be read "
                + "\n";
        if(args.length < 2){
            System.err.println(VERSION);
            System.err.println(usage);
            System.exit(1);
        }
        String[] pass_args = Arrays.copyOfRange(args, 1, args.length);

        switch( args[0] ) {
            case "cmp":
                CmpH5Reader ch5 = new CmpH5Reader(args[1]);
                log.info(ch5.toString());
                break;
            case "read":
                H5Summary.run(pass_args[0]);
                break;
            case "clonegroups":
                H5CloneGroups.run(pass_args[0], pass_args[1]);
                break;
            case "write":
                try {
                    BaxH5Writer writer = new BaxH5Writer(pass_args[0]);
                    writer.writeGroups();
                    writer.close();
                }
                catch(Exception e){

                }
                break;
            default:
                System.err.println(usage);
        }
    }

    public static void main(String[] args){ new H5Test().run(args); }
}
