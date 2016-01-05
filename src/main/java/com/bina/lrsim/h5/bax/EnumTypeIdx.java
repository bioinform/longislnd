package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/6/15.
 */
public enum EnumTypeIdx {
  TypeAdapter(0, "Adapter"), TypeInsert(1, "Insert"), TypeHQRegion(2, "HQRegion");

  static String[] getDescriptionArray() {
    return new String[] {TypeAdapter.description, TypeInsert.description, TypeHQRegion.description};
  }

  EnumTypeIdx(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public final String description;
  public final int value;
}
