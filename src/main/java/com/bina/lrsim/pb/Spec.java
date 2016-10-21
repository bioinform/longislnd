package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.bax.EnumGroups;
import htsjdk.samtools.BamFileIoUtils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mohiyudm on 3/14/16.
 */
public enum Spec {
    ClrBamSpec(
            "clrbam",
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "IDPV1", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray)),
            BamFileIoUtils.BAM_FILE_EXTENSION,
            false,
            ""
    ),
    BaxSpec(
            "bax",
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray, EnumDat.IDPV1)),
            ".bax.h5",
            false,
            ""
    ),
    BaxSampleSpec(
            "baxsample",
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "IDPV1", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray)),
            ".bax.h5",
            false,
            ""
    ),
    CcsSpec(
            "ccs",
            new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.ZMW, EnumGroups.ZMWMetrics)),
            false,
            EnumGroups.BaseCalls,
            EnumGroups.CZMW,
            EnumGroups.CZMWMetrics,
            EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray, EnumDat.MergeQV, EnumDat.IDPV1)),
            ".ccs.h5",
            false,
            ""
    ),
    FastqSpec(
            "fastq",
            new String[] {"Basecall", "QualityValue", "uint8", "uint8"},
            EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses)),
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.of(EnumDat.BaseCall, EnumDat.QualityValue),
            ".fq",
            false,
            ""
    ),
    UnknownSpec(
            null,
            null,
            null,
            true,
            EnumGroups.BaseCalls,
            EnumGroups.ZMW,
            EnumGroups.ZMWMetrics,
            EnumSet.noneOf(EnumDat.class),
            null,
            false,
            ""
    );

    public final String readType;
    public final String[] dataDescription;
    public final Set<EnumGroups> groupSet;
    public final boolean writeAdapterInsert;
    public final EnumGroups baseCalls;
    public final EnumGroups zmw;
    public final EnumGroups zmwMetrics;
    public final Set<EnumDat> dataSet;
    public final Set<EnumDat> nonBaseDataSet;
    public final String suffix;
    public boolean polymeraseReadFlag;
    public String adapterSequence;

    Spec(final String readType,
         final String[] dataDescription,
         final Set<EnumGroups> groupSet,
         final boolean writeAdapterInsert,
         final EnumGroups baseCalls,
         final EnumGroups zmw,
         final EnumGroups zmwMetrics,
         final Set<EnumDat> dataSet,
         final String suffix,
         final boolean polymeraseReadFlag,
         final String adapterSequence) {
        this.readType = readType;
        this.dataDescription = dataDescription;
        this.groupSet = groupSet;
        this.writeAdapterInsert = writeAdapterInsert;
        this.baseCalls = baseCalls;
        this.zmw = zmw;
        this.zmwMetrics = zmwMetrics;
        this.dataSet = dataSet;
        nonBaseDataSet = EnumSet.copyOf(dataSet);
        this.suffix = suffix;
        this.polymeraseReadFlag = polymeraseReadFlag;
        this.adapterSequence = adapterSequence;

        nonBaseDataSet.remove(EnumDat.BaseCall);
    }

    public static Spec fromReadType(final String readType) {
        for (final Spec spec : values()) {
            if (spec.getReadType().equals(readType)) {
                return spec;
            }
        }
        return UnknownSpec;
    }

    public void setPolymeraseReadFlag(final String outputPolymeraseRead) {
        if (outputPolymeraseRead.equals("True")) {
            this.polymeraseReadFlag = true;
        }
    }

    public void setAdapterSequence(final String adapterSequence) {
        this.adapterSequence = adapterSequence;
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

    public String getReadType() {
        return readType;
    }
}
