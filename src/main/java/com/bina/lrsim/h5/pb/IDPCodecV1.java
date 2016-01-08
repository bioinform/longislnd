package com.bina.lrsim.h5.pb;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by bayolau on 1/7/16.
 */
// basically line-by-line translation of _BamSupport.py
public class IDPCodecV1 {

  public static final int MAX_CODE = 255;
  private static final int _maxFramepoint;
  private static final ArrayList<Integer> _framepoints;
  private static final ArrayList<Integer> _frameToCode;

  static {
    _framepoints = _makeFramepoints();
    final Pair<ArrayList<Integer>, Integer> pair = _makeLookup(_framepoints);
    _frameToCode = pair.getFirst();
    _maxFramepoint = pair.getSecond();
  }

  public static int framesToCode(int nframes) {
    nframes = Math.min(nframes, _maxFramepoint);
    return _frameToCode.get(nframes);
  }

  public static int codeToFrames(int code) {
    return _framepoints.get(code);
  }

  public static int downsampleFrames(int nframes) {
    return codeToFrames(framesToCode(nframes));
  }

  // line-by-line translation of _BamSupport.py
  private static ArrayList<Integer> _makeFramepoints() {
    final int B = 2;
    final int t = 6;
    final int T = (int) (Math.pow(2, t) + 0.5);
    ArrayList<Integer> framepoints = new ArrayList<>();
    int next = 0;
    for (int i = 0; i < 256 / T; ++i) {
      final int grain = (int) (Math.pow(B, i) + 0.5);
      for (int jj = 0; jj < T; ++jj) {
        framepoints.add(next + grain * jj);
      }
      next = framepoints.get(framepoints.size() - 1) + grain;
    }
    return framepoints;
  }

  // line-by-line translation of _BamSupport.py
  private static Pair<ArrayList<Integer>, Integer> _makeLookup(ArrayList<Integer> framepoints) {
    ArrayList<Integer> frameToCode = new ArrayList<Integer>(Collections.nCopies(Collections.max(framepoints) + 1, -1));
    int i = 0, fl = -1, fu = -1; // python code was written like this
    for (i = 0; i + 1 < framepoints.size(); ++i) {
      fl = framepoints.get(i);
      fu = framepoints.get(i + 1);
      if (fu > fl + 1) {
        final int m = (fl + fu) / 2;
        for (int f = fl; f < m; ++f) {
          frameToCode.set(f, i);
        }
        for (int f = m; f < fu; ++f) {
          frameToCode.set(f, i + 1);
        }

      } else {
        frameToCode.set(fl, i);
      }
    }
    frameToCode.set(fu, i);
    return new Pair<ArrayList<Integer>, Integer>(frameToCode, fu);
  }
}
