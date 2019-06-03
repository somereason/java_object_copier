package com.github.somereason.object_copier_test.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by somereason on 2018/7/25.
 */
@Getter
@Setter
public class NestMapModel {
    protected Map<Integer,SimpleModel> m;
}
