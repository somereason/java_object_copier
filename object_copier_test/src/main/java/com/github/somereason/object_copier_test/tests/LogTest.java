package com.github.somereason.object_copier_test.tests;

import com.github.somereason.object_copier_test.entity.SimpleDto;
import com.github.somereason.object_copier_test.entity.SimpleModel;
import org.junit.jupiter.api.Test;
import com.github.somereason.object_copier.ClassMapper;
import com.github.somereason.object_copier.MappingManager;

/**
 * Created by somereason on 2018/7/25.
 */
public class LogTest {
    @Test
    public void ValueAndNull() {
        MappingManager mappingManager = new MappingManager();
        mappingManager.setWriteLogFunction(a -> {
            a = "收到错误信息:" + a;
            System.out.println(a);
        });
        mappingManager.registerMapper(SimpleDto.class, SimpleModel.class);//此处还可以添加

        ClassMapper<SimpleDto, SimpleModel> mapper1 = mappingManager.registerMapper(SimpleDto.class, SimpleModel.class);
        mapper1.setFieldMappingRule("string3", a -> Long.MAX_VALUE);//目标值是int,long显然赋值不过去.

        SimpleDto dto = new SimpleDto();
        dto.setInt1(12345);
        dto.setInt4(-100);
        dto.setString2("xxxxx");
        dto.setString3("66666");//会被处理规则强制转化为Long.MAX_VALUE

        SimpleModel model = mappingManager.map(dto,SimpleModel.class);

        if (!model.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model.getInt4() != dto.getInt4())
            throw new RuntimeException("fail");
        if (!model.getString2().equals(dto.getString2()))
            throw new RuntimeException("fail");
        if (model.getString3() != 0)
            throw new RuntimeException("fail");
    }
}
