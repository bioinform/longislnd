package com.bina.hdf5.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

// named according to pacbio's online spec
enum EnumIdx {
    AlnID       (0),
    AlnGroupID  (1),
    MovieId     (2),
    RefGroupID  (3),
    tStart      (4),
    tEnd        (5),
    RCRefStrand (6),
    HoleNumber  (7),
    SetNumber   (8),
    StrobeNumber(9),
    MoleculeID  (10),
    rStart      (11),
    rEnd        (12),
    MapQV       (13),
    nM          (14),
    nMM         (15),
    nIns        (16),
    nDel        (17),
    offset_begin(18),
    offset_end  (19),
    nBackRead   (20),
    nBackOverlap(21);

    public int value() {
        return value_;
    }

    EnumIdx(int value) {
        value_ = value;
    }

    private final int value_;
}
