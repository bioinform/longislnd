package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/21/15.
 */
enum EnumAttributes {
    DESCRIPTION ( "Description" ),
    INDEX_FIELD ( "IndexField" ),
    NUM_EVENT ( "NumEvent" ),
    UNITS_OR_ENCODING ( "UnitsOrEncoding" ),
    PHRED_QV  ( "Phred QV" );

    public final String fieldName;

    EnumAttributes(String s) {
        fieldName = s;
    }
}
