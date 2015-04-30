package com.bina.hdf5;

import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by bayo on 4/30/15.
 */
public class H5Test {

    private final static Logger log = Logger.getLogger(H5Test.class.getName());

    String VERSION = "H5Test " + getClass().getPackage().getImplementationVersion();

    public void run(String[] args) {
        String usage = "java -jar H5test.jar <h5file> \n"
                + "       h5file    -- h5 file to be read "
                + "\n";
        if(args.length == 0){
            System.err.println(VERSION);
            System.err.println(usage);
            System.exit(1);
        }

        String[] pass_args = Arrays.copyOfRange(args, 0, args.length);

        new H5Summary().run(pass_args[0]);

    }

    public static void main(String[] args){ new H5Test().run(args); }
}
