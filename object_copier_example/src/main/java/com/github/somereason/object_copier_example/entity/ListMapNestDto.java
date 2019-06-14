package com.github.somereason.object_copier_example.entity;

import lombok.Getter;
import lombok.Setter;
import com.github.somereason.object_copier.entity.RegisterThisType;

import java.util.HashMap;
import java.util.List;

/**
 * on 2017/4/20.
 */
@Getter
@Setter
public class ListMapNestDto {
    protected Integer int1;
    protected int int2;
    protected String string1;
    protected long time1;
    //如果是自定义类型,且希望自动拷贝(而不是通过定义特殊规则拷贝),需要添加注解
    @RegisterThisType
    protected SimpleDto test2;
    protected List<Integer> arr1;
    protected HashMap<Integer,String> map1;
}
