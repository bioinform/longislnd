package com.bina.lrsim.pb.h5.bax;

/**
 * Created by bayo on 5/6/15.
 */
public enum EnumTypeIdx {
  TypeAdapter("Adapter"), TypeInsert("Insert"), TypeHQRegion("HQRegion");

  static String[] getDescriptionArray() {
    return new String[] {TypeAdapter.description, TypeInsert.description, TypeHQRegion.description};
  }

  EnumTypeIdx(final String description) {
    this.description = description;
  }

  public final String description;
}
