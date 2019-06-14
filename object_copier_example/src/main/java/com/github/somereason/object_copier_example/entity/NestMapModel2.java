package com.github.somereason.object_copier_example.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by somereason on 2018/7/25.
 */
@Getter
@Setter
public class NestMapModel2 {
    protected Map<Integer,SimpleModel2> m;
}
