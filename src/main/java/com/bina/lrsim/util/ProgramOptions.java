package com.bina.lrsim.util;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Created by bayolau on 6/13/16.
 */
abstract public class ProgramOptions {

  private final static Logger log = Logger.getLogger(ProgramOptions.class.getName());

  @Option(name = "--help", aliases = "-h", usage = "print help message")
  public boolean help;

  public static <Derived extends ProgramOptions> Derived parse(String[] args, Class<Derived> c) {
    Derived ret = null;
    try {
      ret = c.newInstance();
    } catch (InstantiationException e) {
      log.error(e.getMessage());
      return null;
    } catch (IllegalAccessException e) {
      log.error(e.getMessage());
      return null;
    }
    CmdLineParser parser = new CmdLineParser(ret);
    boolean help = false;
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      log.error(e.getMessage());
      ret.help = true;
    }
    if (ret.help) {
      System.err.println("parameters for " + c.getName() + " module");
      parser.printUsage(System.err);
      return null;
    } else {
      return ret;
    }
  }

}
