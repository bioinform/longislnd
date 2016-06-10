package com.bina.lrsim;

import com.bina.lrsim.ont.Fast5ConverterVisitor;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


/**
 * Created by bayolau on 6/10/16.
 */
public class Fast5Extract {
  private final static Logger log = Logger.getLogger(Fast5Extract.class.getName());
  private final static String usage = "parameters: output_type directories; output_type (" + Fast5ConverterVisitor.supportedTypesString() + ")";

  public static void main(String[] args) {
    if (args.length < 2) {
      log.info(usage);
      return;
    }
    final String format = args[0];
    if (!Fast5ConverterVisitor.isOutputSupported(format)) {
      log.info(usage);
      return;
    }
    for (final String dir : Arrays.copyOfRange(args, 1, args.length)) {
      log.info("looking in " + dir);
      try (Fast5ConverterVisitor fileVisitor = new Fast5ConverterVisitor(FilenameUtils.getBaseName(dir), format)) {
        Files.walkFileTree(Paths.get(dir), fileVisitor);
      } catch (IOException e) {
        log.error("failed to process " + dir, e);
      }
    }
  }
}
