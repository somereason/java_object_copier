package com.github.somereason.object_copier_test.tests;

import com.github.somereason.object_copier_test.entity.*;
import org.junit.jupiter.api.Test;
import com.github.somereason.object_copier.ClassMapper;
import com.github.somereason.object_copier.MappingManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by somereason on 2018/7/25.
 */
public class BasicTest {

    /**
     * 基本映射,涉及基本类型,对象,空
     */
    @Test
    public void ValueAndNull() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(SimpleDto.class, SimpleModel.class);//此处还可以添加

        SimpleDto dto = new SimpleDto();
        dto.setInt1(12345);
        dto.setInt4(-100);
        dto.setString2("xxxxx");
        //注意:如果没有设置规则,而且两边类型不同,不会处理这个字段,映射后这个字段值为null
        dto.setString3("66666");

        SimpleModel model = mappingManager.map(dto, SimpleModel.class);

        if (!model.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model.getInt4() != dto.getInt4())
            throw new RuntimeException("fail");
        if (!model.getString2().equals(dto.getString2()))
            throw new RuntimeException("fail");
        if (model.getString3() != 0)
            throw new RuntimeException("fail");
    }

    /**
     * 深拷贝测试
     */
    @Test
    public void DeepCopy() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(SimpleDto.class, SimpleDto.class);

        SimpleDto dto = new SimpleDto();
        dto.setInt1(12345);
        dto.setInt4(-100);
        dto.setString2("xxxxx");
        dto.setString3("66666");

        SimpleDto model = mappingManager.map(dto, SimpleDto.class);

        if (!model.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model.getInt4() != dto.getInt4())
            throw new RuntimeException("fail");
        if (!model.getString2().equals(dto.getString2()))
            throw new RuntimeException("fail");
        if (model.getString3() != "66666")
            throw new RuntimeException("fail");
        if (model == dto)//检查是否属于同一个地址
            throw new RuntimeException("fail");

    }

    /**
     * 一个源映射成两种对象
     */
    @Test
    public void oneSourceMulitDistinct() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(SimpleDto.class, SimpleModel.class);//此处还可以添加
        ClassMapper map = mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);//此处还可以添加
        map.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));

        SimpleDto dto = new SimpleDto();
        dto.setInt1(12345);
        dto.setInt4(-100);
        dto.setString2("xxxxx");
        //注意:如果没有设置规则,而且两边类型不同,不会处理这个字段,映射后这个字段值为null
        dto.setString3("66666");

        SimpleModel model = mappingManager.map(dto, SimpleModel.class);
        SimpleModel2 model2 = mappingManager.map(dto, SimpleModel2.class);

        if (!model.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model.getInt4() != dto.getInt4())
            throw new RuntimeException("fail");
        if (!model.getString2().equals(dto.getString2()))
            throw new RuntimeException("fail");
        if (model.getString3() != 0)
            throw new RuntimeException("fail");

        if (!model2.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model2.getInt4() != dto.getInt4())
            throw new RuntimeException("fail");
        if (!model2.getString2().equals(dto.getString2()))
            throw new RuntimeException("fail");
        if (model2.getString3() != 66666)
            throw new RuntimeException("fail");
    }

    /**
     * 应用映射规则.
     * 通过映射规则,可以在映射的时候改变映射过去的值,甚至是类型
     */
    @Test
    public void useMappingRule() {

        MappingManager mapper = new MappingManager();

        ClassMapper<SimpleDto, SimpleModel> mapper1 = mapper.registerMapper(SimpleDto.class, SimpleModel.class);
        mapper1.setFieldMappingRule("int1", a -> (int) a * 5);//基本类型的特殊映射
        mapper1.setFieldMappingRule("int4", a -> (int) a * 10);//引用类型的特殊映射
        mapper1.setFieldMappingRule("string2", a -> (String) a + (String) a);
        mapper1.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));//从原始类型到引用类型.


        SimpleDto dto = new SimpleDto();
        dto.setInt1(12345);
        dto.setInt4(-100);
        dto.setString2("文本1");
        dto.setString3("66666");

        SimpleModel model = mapper.map(dto, SimpleModel.class);

        if (!model.getInt1().equals(dto.getInt1() * 5))
            throw new RuntimeException("fail");
        if (model.getInt4() != dto.getInt4() * 10)
            throw new RuntimeException("fail");
        if (!model.getString2().equals(dto.getString2() + dto.getString2()))
            throw new RuntimeException("fail");
        if (model.getString3() != Integer.parseInt(dto.getString3()))
            throw new RuntimeException("fail");
    }

    /**
     * 测试对列表和map的映射
     */
    @Test
    public void listAndMap() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(ListMapNestDto.class, ListMapNestModel.class);//此处还可以添加

        ListMapNestDto dto = new ListMapNestDto();
        dto.setInt1(123);
        dto.setInt2(234);
        dto.setString1("String1");
        dto.setTime1(0);//没有设置规则,而且两边类型不同,不会映射,映射后这个字段值为null
        dto.setTest2(null);//设置为空,不会处理这个字段
        List<Integer> list1 = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            list1.add(i + 1);
        dto.setArr1(list1);
        HashMap<Integer, String> map1 = new HashMap<>();
        for (int i = 0; i < 10; i++)
            map1.put(i, "Number:" + i);
        dto.setMap1(map1);

        ListMapNestModel model = mappingManager.map(dto, ListMapNestModel.class);

        if (!model.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model.getInt2() != dto.getInt2())
            throw new RuntimeException("fail");
        if (!model.getString1().equals(dto.getString1()))
            throw new RuntimeException("fail");
        if (model.getTime1() != null)
            throw new RuntimeException("fail");

        if (!model.getArr1().equals(dto.getArr1()))
            throw new RuntimeException("fail");
        if (!model.getMap1().equals(dto.getMap1()))
            throw new RuntimeException("fail");

    }


}
