package com.bina.lrsim.simulator;

import com.bina.lrsim.SimulatorDriver;
import com.bina.lrsim.bioinfo.Fragment;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.ReferenceSequenceDrawer;
import com.bina.lrsim.interfaces.RandomFragmentGenerator;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static htsjdk.samtools.SAMFileHeader.GroupOrder.reference;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by guoy28 on 10/12/16.
 */
public class SimulatorTest {
    private int seed = 11;
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    /*
    sequencing mode: shotgun, fragment, shotgunfragment
    valid_read_types: bax, ccs, clrbam, fastq
     */
    @Test
    public void firstSimulatorTest() throws IOException {
        File workingDirectory = tmpFolder.newFolder("tmp");
        String fasta = "src/test/resources/firstSimulatorTest/reference.fasta";
        String modelPrefix = "src/test/resources/firstSimulatorTest/ecoli_model/p6_ecoli.fofn.cmp.h5.bax.3.1000.100";
        String expectedBAM = "src/test/resources/firstSimulatorTest/expected.bam";
        Path outputBAM = Paths.get(workingDirectory.getCanonicalPath(), "movie00000_ctest_s1_p0.bam");

        SimulatorDriver.main(new String[]{"--outDir", workingDirectory.toString(),
                "--identifier", "test", "--readType", "clrbam",
                "--sequencingMode", "fragment",
                "--fasta", fasta,
                "--modelPrefixes", modelPrefix,
                "--totalBases", "200", "--samplePer", "100", "--seed", Integer.toString(seed),
                "--minFragmentLength", "50", "--maxFragmentLength", "1000",
                "--minNumPasses", "1", "--maxNumPasses", "10",
                "--eventsFrequency", "0:0:0:1", //make sure error rates are zero for events
                "--forceMovieName", "movie"
        });
        //the reads will be 3bp shorter on 5' end, 3bp shorter on 3' end
        assertTrue(FileUtils.contentEquals(outputBAM.toFile(), new File(expectedBAM)));
    }
}
