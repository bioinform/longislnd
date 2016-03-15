package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.bax.EnumGroups;
import htsjdk.samtools.BamFileIoUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by mohiyudm on 3/14/16.
 */
public enum Spec {
    ClrBamSpec(
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "IDPV1", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray)),
            EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray)),
            BamFileIoUtils.BAM_FILE_EXTENSION
    ),
    BaxSpec(
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray, EnumDat.IDPV1)),
            EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray, EnumDat.IDPV1)),
            ".bax.h5"
    ),
    BaxSampleSpec(
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "IDPV1", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray)),
            EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray)),
            ".bax.h5"
    ),
    CcsSpec(
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.ZMW, EnumGroups.ZMWMetrics)),
            false,
            EnumGroups.BaseCalls,
            EnumGroups.CZMW,
            EnumGroups.CZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray, EnumDat.MergeQV, EnumDat.IDPV1)),
            EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray, EnumDat.MergeQV, EnumDat.IDPV1)),
            ".ccs.h5"
    ),
    FastqSpec(
            new String[] {"Basecall", "QualityValue", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.of(EnumDat.BaseCall, EnumDat.QualityValue),
            EnumSet.of(EnumDat.QualityValue),
            ".fq"
    );

    public final String[] dataDescription;
    public final Set<EnumGroups> groupSet;
    public final boolean writeAdapterInsert;
    public final EnumGroups baseCalls;
    public final EnumGroups zmw;
    public final EnumGroups zmwMetrics;
    public final Set<EnumDat> dataSet;
    public final Set<EnumDat> nonBaseDataSet;
    public final String suffix;

    Spec(final String[] dataDescription,
         final Set<EnumGroups> groupSet,
         final boolean writeAdapterInsert,
         final EnumGroups baseCalls,
         final EnumGroups zmw,
         final EnumGroups zmwMetrics,
         final Set<EnumDat> dataSet,
         final Set<EnumDat> nonBaseDataSet,
         final String suffix) {
        this.dataDescription = dataDescription;
        this.groupSet = groupSet;
        this.writeAdapterInsert = writeAdapterInsert;
        this.baseCalls = baseCalls;
        this.zmw = zmw;
        this.zmwMetrics = zmwMetrics;
        this.dataSet = dataSet;
        this.nonBaseDataSet = nonBaseDataSet;
        this.suffix = suffix;
    }

    public Set<EnumDat> getDataSet() {
        return dataSet;
    }

    public Set<EnumDat> getNonBaseDataSet() {
        return nonBaseDataSet;
    }

    public String[] getDataDescription() {
        return dataDescription;
    }

    public Set<EnumGroups> getGroupSet() {
        return groupSet;
    }

    public boolean writeAdapterInsert() {
        return writeAdapterInsert;
    }

    public EnumGroups getBaseCallsEnum() {
        return baseCalls;
    }

    public EnumGroups getZMWEnum() {
        return zmw;
    }

    public EnumGroups getZMWMetricsEnum() {
        return zmwMetrics;
    }

    public String getSuffix() {
        return suffix;
    }
}
