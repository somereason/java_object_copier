package com.github.somereason.object_copier.entity;

/**
 * on 2017/4/21.
 */
public enum FieldType {
    /**
     * 基本类型
     */
    INT(1),
    LONG(2),
    BOOLEAN(3),
    BYTE(4),
    CHAR(5),
    SHORT(6),
    FLOAT(7),
    DOUBLE(8),
    /**
     * 通用对象类型
     */
    GENERAL_OBJECT(101),
    /**
     * 是否是集合,包括List等
     */
    COLLECTION(102),
    /**
     * map
     */
    MAP(103),

    /**
     * 源类型是否在映射列表中,如果不在,说明是基本类型,至少是管不了的类型
     * 如果在,那么对于这个属性,要把他的子属性逐个映射.
     */
    TYPE_REGISTERD(1001);


    private int numVal;

    FieldType(int numVal) {
        this.numVal = numVal;
    }

    public int value() {
        return numVal;
    }
}
