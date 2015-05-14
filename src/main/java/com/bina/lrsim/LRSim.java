package com.bina.lrsim;

import com.bina.lrsim.h5.H5CloneGroups;
import com.bina.lrsim.h5.H5Summary;
import com.bina.lrsim.h5.bax.BaxH5Writer;
import com.bina.lrsim.h5.cmp.CmpH5Alignment;
import com.bina.lrsim.h5.cmp.CmpH5Reader;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by bayo on 4/30/15.
 */
public class LRSim {

    private final static Logger log = Logger.getLogger(LRSim.class.getName());

    private static String VERSION = "LRSim " + LRSim.class.getPackage().getImplementationVersion();

    public static void run(String[] args) {
        String usage = "java -jar LRSim.jar <mode> [parameters] \n"
                + "       mode    -- simulate/sample\n"
                + "       parameters -- see mode description by not specifying parameters"
                + "\n";
        if (args.length < 1) {
            System.err.println(VERSION);
            System.err.println(usage);
            System.exit(1);
        }
        String[] pass_args = Arrays.copyOfRange(args, 1, args.length);

        switch (args[0]) {
            case "simulate": // run simulator
                H5Simulator.run(pass_args);
                break;
            case "sample": //run sampling
                H5Sampler.run(pass_args);
                break;


            // the following are backdoor tests
            case "cmp":
                log.info("cmp");
            {
                CmpH5Reader ch5 = new CmpH5Reader(args[1]);
                log.info(ch5.toString());
            }
            break;
            case "read":
                log.info("read");
                H5Summary.run(pass_args[0]);
                break;
            case "clonegroups":
                log.info("clonegroups");
                H5CloneGroups.run(pass_args[0], pass_args[1]);
                break;
            case "write":
                log.info("write");
                try {
                    BaxH5Writer writer = new BaxH5Writer();
                    CmpH5Reader ch5 = new CmpH5Reader(pass_args[1]);
                    for (int ii = 0; ii < 1000/*ch5.size()*/ ; ++ii) {
                        CmpH5Alignment aln = ch5.getEventGroup(ii);
                        log.info("alignment " + ii + " of length " + aln.aln_length());
                        if (null == aln) continue;
                        writer.addLast(aln.toSeqRead(), 1000);
                    }
                    String prefix = "m000000_000000_00001_cSIMULATED_s0_p0";
                    writer.write(pass_args[0] + prefix + ".bax.h5", prefix, 0);
                } catch (Exception e) {
                    log.info(e, e);
                }
                break;
            // end of backdoor tests
            default:
                System.err.println(usage);
        }
    }

    public static void main(String[] args) {
        LRSim.run(args);
    }
}
