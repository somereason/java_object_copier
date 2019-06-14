package com.github.somereason.object_copier_example.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * on 2017/4/20.
 */
@Setter
@Getter
public class ListMapNestModel2 {
    protected Integer int1;
    protected int int2;
    protected String string1;
    protected LocalDateTime time1;
    protected String string2;
    protected SimpleModel2 test2;
    protected List<Integer> arr1;
    protected HashMap<Integer,String> map1;
}
