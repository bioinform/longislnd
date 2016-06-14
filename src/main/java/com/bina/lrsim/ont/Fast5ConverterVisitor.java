package com.bina.lrsim.ont;

import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import com.bina.lrsim.util.H5Wrapper;
import ncsa.hdf.object.FileFormat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bayolau on 6/10/16.
 */
public class Fast5ConverterVisitor extends SimpleFileVisitor<Path> implements Closeable {
  private final static Logger log = Logger.getLogger(Fast5ConverterVisitor.class.getName());
  private final static Set<String> VALID_OUTPUT_TYPES = new HashSet<>(Arrays.asList("fastq", "fq"));

  final private Writer writer;
  final private String h5Loc;

  public Fast5ConverterVisitor(String prefix, String type, String h5Loc) throws IOException {
    if (!isOutputSupported(type)) { throw new UnsupportedOperationException("valid formats are " + supportedTypesString()); }
    final File filename = new File(prefix + "." + type);
    if (filename.exists()) {
      log.warn(filename.toString() + " already exists, trying to overwrite.");
      if (filename.isDirectory()) { throw new IOError(new Exception(filename.toString() + " exists as a directory")); }
    }
    this.writer = new BufferedWriter(new FileWriter(filename));
    this.h5Loc = h5Loc;
  }

  public static boolean isOutputSupported(String type) {
    return VALID_OUTPUT_TYPES.contains(type);
  }

  public static String supportedTypesString() {
    return StringUtils.join(VALID_OUTPUT_TYPES, " ");
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
    final String sfile = file.toString();
    if (FilenameUtils.getExtension(sfile).equals("fast5")) {
      try (H5Wrapper h5 = new H5Wrapper(sfile, FileFormat.READ)) {
        String[] data = (String[]) H5ScalarDSIO.Read(h5, this.h5Loc);
        this.writer.write(data[0]);
        this.writer.write('\n');
      } catch (IOException e) {
        log.error("failed to extract from " + sfile + " at " + this.h5Loc, e);
      }
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
