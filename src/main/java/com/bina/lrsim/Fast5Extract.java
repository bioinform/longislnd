package com.bina.lrsim;

import com.bina.lrsim.ont.Fast5ConverterVisitor;
import com.bina.lrsim.util.ProgramOptions;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * Created by bayolau on 6/10/16.
 */
public class Fast5Extract {
  private final static Logger log = Logger.getLogger(Fast5Extract.class.getName());

  public static class ModuleOptions extends ProgramOptions {
    @Option(name = "--outFormat", required = false, usage = "output format")
    private String outFormat = "fastq";

    @Option(name = "--h5Location", required = false, usage = "location of data in the fast5 location")
    private String h5Location = "/Analyses/Basecall_2D_000/BaseCalled_2D/Fastq";

    @Argument
    private List<String> fileList = new ArrayList<String>();
  }

  public static void main(String[] args) {
    final ModuleOptions po = ProgramOptions.parse(args, ModuleOptions.class);
    if (po == null) {
      System.exit(1);
    }

    if (!Fast5ConverterVisitor.isOutputSupported(po.outFormat)) {
      log.error("supported outputformat: " + Fast5ConverterVisitor.supportedTypesString());
      System.exit(1);
    }

    for (final String dir : po.fileList) {
      log.info("looking in " + dir);
      try (Fast5ConverterVisitor fileVisitor = new Fast5ConverterVisitor(FilenameUtils.getBaseName(dir), po.outFormat, po.h5Location)) {
        Files.walkFileTree(Paths.get(dir), EnumSet.of(java.nio.file.FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, fileVisitor);
      } catch (IOException e) {
        log.error("failed to process " + dir, e);
      }
    }
  }
}
