package win.somereason.utils.object_copier_test.tests;

import org.junit.jupiter.api.Test;
import win.somereason.utils.object_copier.ClassMapper;
import win.somereason.utils.object_copier.FieldMapper;
import win.somereason.utils.object_copier.MappingManager;
import win.somereason.utils.object_copier_test.entity.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 测试类中嵌套另一个类的情况.
 */
public class NestClass {
    @Test
    public void nestClass() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(ListMapNestDto.class, ListMapNestModel.class);

        ListMapNestDto dto = new ListMapNestDto();

        SimpleDto simpleDto = new SimpleDto();
        simpleDto.setInt1(12345);
        simpleDto.setInt4(-100);
        simpleDto.setString2("文本1");
        simpleDto.setString3("66666");//没有设置映射规则,值应该为0
        dto.setTest2(simpleDto);

        ListMapNestModel model = mappingManager.map(dto, ListMapNestModel.class);

        if (!model.getTest2().getInt1().equals(dto.getTest2().getInt1()))
            throw new RuntimeException("fail");
        if (model.getTest2().getInt4() != dto.getTest2().getInt4())
            throw new RuntimeException("fail");
        if (!model.getTest2().getString2().equals(dto.getTest2().getString2()))
            throw new RuntimeException("fail");
        if (model.getTest2().getString3() != 0)
            throw new RuntimeException("fail");

    }

    /**
     * 测试注册类的时候,对于已经隐式注册的类,能返回注册的引用,并能据此添加规则.
     */
    @Test
    public void nestClassWithRule() {
        MappingManager mappingManager = new MappingManager();
        //由于其中包含SimpleDto类型的字段,并且标记了需要注册,程序自动发现并注册了SimpleDto
        mappingManager.registerMapper(ListMapNestDto.class, ListMapNestModel.class);
        //不会重复注册SimpleDto,会返回它的引用
        ClassMapper mapForSimpleClass = mappingManager.registerMapper(SimpleDto.class, SimpleModel.class);
        mapForSimpleClass.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));

        ListMapNestDto dto = new ListMapNestDto();

        SimpleDto simpleDto = new SimpleDto();
        simpleDto.setInt1(12345);
        simpleDto.setInt4(-100);
        simpleDto.setString2("文本1");
        simpleDto.setString3("66666");
        dto.setTest2(simpleDto);

        ListMapNestModel model = mappingManager.map(dto, ListMapNestModel.class);

        if (!model.getTest2().getInt1().equals(dto.getTest2().getInt1()))
            throw new RuntimeException("fail");
        if (model.getTest2().getInt4() != dto.getTest2().getInt4())
            throw new RuntimeException("fail");
        if (!model.getTest2().getString2().equals(dto.getTest2().getString2()))
            throw new RuntimeException("fail");
        if (model.getTest2().getString3() != Integer.parseInt(dto.getTest2().getString3()))
            throw new RuntimeException("fail");

    }

    //测试类中嵌套两个原生类型的情况
    @Test
    public void nestTime() {
        MappingManager mappingManager = new MappingManager();
        ClassMapper map = mappingManager.registerMapper(NestTimeDto.class, NestTimeModel.class);
        map.setFieldMappingRule("time", a -> {
            ZonedDateTime zdt = ((LocalDateTime) a).atZone(ZoneId.systemDefault());
            return Date.from(zdt.toInstant());
        });

        NestTimeDto d = new NestTimeDto();
        d.setTime(LocalDateTime.now());

        NestTimeModel m = mappingManager.map(d, NestTimeModel.class);

        Date targetTime = (Date) ((FieldMapper) map.fieldMappers.get(0)).mappingRule.apply(d.getTime());
        if (!m.getTime().equals(targetTime))
            throw new RuntimeException("fail");
    }

    /**
     * 测试需要注册的类所在字段是list的情况.
     */
    @Test
    public void nestList() {
        MappingManager mappingManager = new MappingManager();
        ClassMapper map = mappingManager.registerMapper(NestListDto.class, NestListModel.class);
        NestListDto d = new NestListDto();
        d.setArr2(new ArrayList<>());
        for (int i = 0; i < 3; i++) {
            SimpleDto s = new SimpleDto();
            s.setInt1(i);
            s.setInt4(i + 1);
            s.setString2("String2:" + i);
            d.getArr2().add(s);
        }

        NestListModel model = mappingManager.map(d, NestListModel.class);
        if (model.getArr2() == null)
            throw new RuntimeException("fail");
        if (model.getArr2().size() != 3)
            throw new RuntimeException("fail");
        if (model.getArr2().get(2).getInt4() != 3)
            throw new RuntimeException("fail");
    }

    @Test
    public void nestMap() {
        MappingManager mappingManager = new MappingManager();
        ClassMapper map = mappingManager.registerMapper(NestMapDto.class, NestMapModel.class);
        NestMapDto d = new NestMapDto();
        d.setM(new HashMap<>());
        for (int i = 0; i < 3; i++) {
            SimpleDto s = new SimpleDto();
            s.setInt1(i);
            s.setInt4(i + 1);
            s.setString2("String2:" + i);
            d.getM().put(i, s);
        }

        NestMapModel model = mappingManager.map(d, NestMapModel.class);
        if (model.getM() == null)
            throw new RuntimeException("fail");
        if (model.getM().size() != 3)
            throw new RuntimeException("fail");
        if (model.getM().get(2).getInt4() != 3)
            throw new RuntimeException("fail");
    }
}
