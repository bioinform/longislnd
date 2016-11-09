package com.bina.lrsim;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Created by guoy28 on 11/8/16.
 */
public class H5SamplerTest {
  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  /*
  sequencing mode: shotgun, fragment, shotgunfragment
  valid_read_types: bax, ccs, clrbam, fastq
   */
  @Test
  public void sequelBAMSamplingTest() throws IOException {
    File workingDirectory = tmpFolder.newFolder("tmp");
    String fasta = "src/test/resources/samplingTest/sequelBAMSamplingTest/reference_for_1subread.fasta";
    String expectedModelPrefix = "src/test/resources/samplingTest/sequelBAMSamplingTest/expected_model/sequel_arabidopsis_1subread_aln_smallref.bam.bam.clrbam.3.1.100";
    String outputModelDirectory = Paths.get(workingDirectory.getCanonicalPath(), "model").toString();
    String outputPrefix = Paths.get(workingDirectory.getCanonicalPath(), "model", "sequel_arabidopsis_1subread_aln_smallref.bam.bam.clrbam.3.1.100").toString();
    String inputBAM = "src/test/resources/samplingTest/sequelBAMSamplingTest/sequel_arabidopsis_1subread_aln_smallref.bam.bam";
    String inputReference = "src/test/resources/samplingTest/sequelBAMSamplingTest/reference_for_1subread.fasta";

    //create model directory
    assumeTrue(new File(outputModelDirectory).mkdir());

    /* example commands
    sample --outPrefix src/test/resources/samplingTest/sequelBAMSamplingTest/expected_model/sequel_arabidopsis_1subread_aln_smallref.bam.bam.clrbam.3.1.100
            --inFile sequel_arabidopsis_1subread_aln_smallref.bam.bam --readType clrbam --leftFlank 3 --rightFlank 3 --minLength 1 --flankMask 100  --reference reference_for_1subread.fasta
    */
    H5Sampler.main(new String[]{
            "--outPrefix", outputPrefix,
            "--inFile", inputBAM,
            "--readType", "clrbam",
            "--leftFlank", "3",
            "--rightFlank", "3",
            "--minLength", "1",
            "--flankMask", "100",
            "--reference", inputReference,
    });
    //*.stats is too large, skip it
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".events"), new File(outputPrefix + ".events")));
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".hp"), new File(outputPrefix + ".hp")));
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".idx"), new File(outputPrefix + ".idx")));
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".summary"), new File(outputPrefix + ".summary")));
    /*
    region --outPrefix src/test/resources/samplingTest/sequelBAMSamplingTest/expected_model/sequel_arabidopsis_1subread_aln_smallref.bam.bam.clrbam.3.1.100
            --inFile sequel_arabidopsis_1subread_aln_smallref.bam --readType clrbam --minReadScore 0.7
            */
    H5RegionSampler.main(new String[]{
            "--outPrefix", outputPrefix,
            "--inFile", inputBAM,
            "--readType", "clrbam",
            "--minReadScore", "0.7"
    });
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".len"), new File(outputPrefix + ".len")));
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".runinfo"), new File(outputPrefix + ".runinfo")));
    assertTrue(FileUtils.contentEquals(new File(expectedModelPrefix + ".scr"), new File(outputPrefix + ".scr")));
  }
}
