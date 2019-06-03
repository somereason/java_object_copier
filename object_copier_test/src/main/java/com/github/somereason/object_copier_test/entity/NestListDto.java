package com.github.somereason.object_copier_test.entity;

import lombok.Getter;
import lombok.Setter;
import com.github.somereason.object_copier.entity.RegisterThisType;

import java.util.List;

/**
 * 测试是否能识别list中嵌套的类,
 */
@Getter
@Setter
public class NestListDto {
    //如果是列表,注册的不是list<>,而是list包含的类型SimpleDto
    @RegisterThisType
    protected List<SimpleDto> arr2;
}
