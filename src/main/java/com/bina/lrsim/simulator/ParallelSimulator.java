package com.bina.lrsim.simulator;

import com.bina.lrsim.interfaces.RandomFragmentGenerator;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.ThreadLocalResources;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by bayolau on 3/24/16.
 */
public class ParallelSimulator {
  private final static Logger log = Logger.getLogger(ParallelSimulator.class.getName());

  public static long process(RandomFragmentGenerator randomFragmentGenerator, String outDir, String moviePrefix, String movieSuffix, SamplesDrawer samples, int targetChunk, long targetNumBases, Spec spec, RandomGenerator gen) {
    Simulator simulator = new Simulator(randomFragmentGenerator);
    final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    int batchNumber = 0;
    for (long scheduledBases = 0; scheduledBases < targetNumBases; scheduledBases += targetChunk, ++batchNumber) {
      final String movieName = moviePrefix + String.format("%05d", batchNumber) + movieSuffix;
      threadPool.execute(new Worker(simulator, outDir, movieName, samples, (int) Math.min((long)targetChunk, targetNumBases), spec, gen.nextInt()));
    }
    threadPool.shutdown();
    try {
      while (!threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
        log.error("been waiting for a loooooooong time. still waiting.");
      }
    } catch (InterruptedException e) {
      log.error("thread pool had problem terminating");
      e.printStackTrace();
    }
    return batchNumber;
  }

  static private class Worker implements Runnable {
    final Simulator sim;
    final String outDir;
    final String movieName;
    final SamplesDrawer samples;
    final int targetNumBases;
    final Spec spec;
    final int seed;

    private Worker(Simulator sim, String outDir, String movieName, SamplesDrawer samples, int targetNumBases, Spec spec, int seed) {
      this.sim = sim;
      this.outDir = outDir;
      this.movieName = movieName;
      this.samples = samples;
      this.targetNumBases = targetNumBases;
      this.spec = spec;
      this.seed = seed;
    }

    @Override
    public void run() {
      try {
        ThreadLocalResources.random().setSeed(seed);
        sim.simulate(outDir, movieName, 0, samples, targetNumBases, spec, new MersenneTwister(seed));
      } catch (IOException e) {
        log.error("Failed to generate " + movieName + " with seed " + seed);
        e.printStackTrace();
      }
    }
  }
}
