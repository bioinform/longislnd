package com.bina.lrsim.pb.h5.bax;

import java.util.Arrays;

/**
 * Created by bayo on 5/6/15.
 */
public enum EnumTypeIdx {
  TypeAdapter("Adapter"), TypeInsert("Insert"), TypeHQRegion("HQRegion");

  public static final String[] descriptionArray;

  static {
    descriptionArray = new String[values().length];
    int i = 0;
    for (EnumTypeIdx enumTypeIdx : values()) {
      descriptionArray[i] = enumTypeIdx.description;
      i++;
    }
  }

  static String[] getDescriptionArray() {
    return descriptionArray;
  }

  EnumTypeIdx(final String description) {
    this.description = description;
  }

  public final String description;
}
