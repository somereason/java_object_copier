package com.github.somereason.object_copier_test.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by somereason on 2018/7/25.
 */
@Getter
@Setter
public class NestListModel {
    protected List<SimpleModel> arr2;

}
