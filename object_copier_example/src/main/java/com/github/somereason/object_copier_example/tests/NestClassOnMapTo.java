package com.github.somereason.object_copier_example.tests;

import com.github.somereason.object_copier_example.entity.*;
import org.junit.jupiter.api.Test;
import com.github.somereason.object_copier.ClassMapper;
import com.github.somereason.object_copier.MappingManager;
import com.github.somereason.object_copier.entity.CollectionCopyRule;
import com.github.somereason.object_copier.entity.CopyConfig;
import com.github.somereason.object_copier_example.entity.mapping.IntTointD;
import com.github.somereason.object_copier_example.entity.mapping.IntTointS;
import com.github.somereason.object_copier_example.entity.mapping.StringTointD;
import com.github.somereason.object_copier_example.entity.mapping.StringTointS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 对mapTo函数的测试,这是最重要的部分.因为mapTo涉及到对目标对象原有值的合并.
 */
public class NestClassOnMapTo {
    @Test
    public void simpleModel() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);//此处还可以添加

        SimpleDto dto = new SimpleDto();
        dto.setInt1(12345);
        dto.setInt4(-100);
        dto.setString2(null);//test copyNull
        //注意:如果没有设置规则,而且两边类型不同,不会处理这个字段,映射后这个字段值为null
        dto.setString3("66666");

        SimpleModel2 model2 = new SimpleModel2();
        model2.setInt5(62456);
        model2.setString2("HHHHHHHHHHHHHHH");

        CopyConfig config = new CopyConfig();
        config.setCopyNull(true);
        model2 = mappingManager.mapTo(dto, model2, config);


        SimpleModel2 model3 = new SimpleModel2();
        config.setCopyNull(false);
        model3.setString2("HHHHHHHHHHHHHHH");
        model3 = mappingManager.mapTo(dto, model3, config);

        if (!model2.getInt1().equals(dto.getInt1()))
            throw new RuntimeException("fail");
        if (model2.getInt4() != dto.getInt4())
            throw new RuntimeException("fail");
        //dto的值为null，如果要复制null，目标值为null
        if (model2.getString2() != null)
            throw new RuntimeException("fail");
        //如果不复制null，目标值保留
        if (!model3.getString2().equals("HHHHHHHHHHHHHHH"))
            throw new RuntimeException("fail");

        if (model2.getString3() != 0)
            throw new RuntimeException("fail");
        if (model2.getInt5() != 62456) //映射之后，int5的值不能变
            throw new RuntimeException("fail");
    }

    @Test
    public void nestClass() {
        MappingManager mappingManager = new MappingManager();

        mappingManager.registerMapper(ListMapNestDto.class, ListMapNestModel2.class);

        ListMapNestDto dto = new ListMapNestDto();

        SimpleDto simpleDto = new SimpleDto();
        simpleDto.setInt1(12345);
        simpleDto.setInt4(-100);
        simpleDto.setString2("文本1");
        simpleDto.setString3("66666");//没有设置映射规则,值应该为0
        dto.setTest2(simpleDto);

        ListMapNestModel2 model = new ListMapNestModel2();
        model.setTest2(new SimpleModel2());
        model.getTest2().setInt5(77777);
        model.setString2("Hello world");
        model = mappingManager.mapTo(dto, model);

        if (!model.getTest2().getInt1().equals(dto.getTest2().getInt1()))
            throw new RuntimeException("fail");
        if (model.getTest2().getInt4() != dto.getTest2().getInt4())
            throw new RuntimeException("fail");
        if (!model.getTest2().getString2().equals(dto.getTest2().getString2()))
            throw new RuntimeException("fail");
        if (model.getTest2().getString3() != 0)
            throw new RuntimeException("fail");
        if (model.getTest2().getInt5() != 77777) //映射之后，int5的值不能变
            throw new RuntimeException("fail");
        if (!model.getString2().equals("Hello world"))
            throw new RuntimeException("fail");
    }

    @Test
    public void nestClassList() {
        MappingManager mappingManager = new MappingManager();

        ClassMapper rule = mappingManager.registerMapper(ListMapNestDto.class, ListMapNestModel2.class);
        ClassMapper rule2 = mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);
        rule2.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));
        ListMapNestDto dto = new ListMapNestDto();

        SimpleDto simpleDto = new SimpleDto();
        simpleDto.setInt1(12345);
        simpleDto.setInt4(-100);
        simpleDto.setString2("文本1");
        simpleDto.setString3("66666");
        dto.setTest2(simpleDto);
        dto.setArr1(new ArrayList<>(10));
        for (int i = 0; i < 10; i++) {
            dto.getArr1().add(i);
        }

        ListMapNestModel2 model = new ListMapNestModel2();
        model.setTest2(new SimpleModel2());
        model.getTest2().setInt5(77777);
        model.setString2("Hello world");

        model.setArr1(new ArrayList<>(15));
        for (int i = 100; i < 100 + 15; i++) {
            model.getArr1().add(i);
        }
        //默认设置,list join,结果应该是0~9
        model = mappingManager.mapTo(dto, model);

        //基本元素
        if (!model.getTest2().getInt1().equals(dto.getTest2().getInt1()))
            throw new RuntimeException("fail");
        if (model.getTest2().getInt4() != dto.getTest2().getInt4())
            throw new RuntimeException("fail");
        if (!model.getTest2().getString2().equals(dto.getTest2().getString2()))
            throw new RuntimeException("fail");
        if (model.getTest2().getString3() != 66666)
            throw new RuntimeException("fail");
        if (model.getTest2().getInt5() != 77777) //映射之后，int5的值不能变
            throw new RuntimeException("fail");
        if (!model.getString2().equals("Hello world"))
            throw new RuntimeException("fail");
        //list
        if (!model.getArr1().get(1).equals(1))
            throw new RuntimeException("fail");
        if (!model.getArr1().get(9).equals(9))
            throw new RuntimeException("fail");
        if (model.getArr1().size() != 10)
            throw new RuntimeException("fail");

        ListMapNestModel2 model2 = new ListMapNestModel2();
        model2.setTest2(new SimpleModel2());
        model2.getTest2().setInt5(77777);
        model2.setString2("Hello world");

        model2.setArr1(new ArrayList<>(15));
        //值是100~114
        for (int i = 100; i < 100 + 15; i++) {
            model2.getArr1().add(i);
        }
        model2.getArr1().add(1);
        model2.getArr1().add(2);
        model2.getArr1().add(3);

        CopyConfig config = new CopyConfig();
        config.setCollectionCopyRule(CollectionCopyRule.FULL_JOIN);
        //list会合并,结果应该是0~9,100~114,
        model2 = mappingManager.mapTo(dto, model2, config);

        //list
        if (!model2.getArr1().get(1).equals(1))
            throw new RuntimeException("fail");
        if (!model2.getArr1().get(24).equals(114))
            throw new RuntimeException("fail");
        if (!model2.getArr1().get(20).equals(110))
            throw new RuntimeException("fail");
        if (model2.getArr1().size() != 25)
            throw new RuntimeException("fail");
    }

    @Test
    public void intMapTointMap() {
        MappingManager mappingManager = new MappingManager();

        ClassMapper rule = mappingManager.registerMapper(IntTointS.class, IntTointD.class);
        IntTointS s = new IntTointS();
        s.setM1(new HashMap<>());
        for (int i = 0; i < 5; i++) {
            s.getM1().put(i, i + 100);
        }

        IntTointD d = new IntTointD();
        d.setM1(new HashMap<>());
        for (int i = 3; i < 8; i++) {
            d.getM1().put(i, i + 1000);
        }
        CopyConfig config = new CopyConfig();
        config.setCollectionCopyRule(CollectionCopyRule.OVER_WRITE);
        d = mappingManager.mapTo(s, d, config);

        IntTointD d2 = new IntTointD();
        d2.setM1(new HashMap<>());
        for (int i = 3; i < 8; i++) {
            d2.getM1().put(i, i + 1000);
        }
        CopyConfig config2 = new CopyConfig();
        config2.setCollectionCopyRule(CollectionCopyRule.FULL_JOIN);
        d2 = mappingManager.mapTo(s, d2, config2);


        if (!d.getM1().get(1).equals(101))
            throw new RuntimeException("fail");
        if (!d.getM1().get(4).equals(104))
            throw new RuntimeException("fail");
        if (d.getM1().size() != 5)
            throw new RuntimeException("fail");

        if (!d2.getM1().get(1).equals(101))
            throw new RuntimeException("fail");
        if (!d2.getM1().get(4).equals(104))
            throw new RuntimeException("fail");
        if (!d2.getM1().get(7).equals(1007))
            throw new RuntimeException("fail");
        if (d2.getM1().size() != 8)
            throw new RuntimeException("fail");
    }

    @Test
    public void stringMapTointMap() {
        MappingManager mappingManager = new MappingManager();

        ClassMapper rule = mappingManager.registerMapper(StringTointS.class, StringTointD.class);
        StringTointS s = new StringTointS();
        s.setM1(new HashMap<>());
        for (int i = 0; i < 5; i++) {
            s.getM1().put(String.valueOf(i), i + 100);
        }
        StringTointD d = new StringTointD();
        d.setM1(new HashMap<>());
        for (int i = 3; i < 8; i++) {
            d.getM1().put(i, i + 1000);
        }

        d = mappingManager.mapTo(s, d);
        if (!d.getM1().get("1").equals(101))
            throw new RuntimeException("fail");
        if (!d.getM1().get("4").equals(104))
            throw new RuntimeException("fail");
    }
    @Test
    public void stringMapTointMap_FullJoin() {
        MappingManager mappingManager = new MappingManager();
        mappingManager.getDefaultCopyConfig().setCollectionCopyRule(CollectionCopyRule.FULL_JOIN);
        ClassMapper rule = mappingManager.registerMapper(StringTointS.class, StringTointD.class);
        StringTointS s = new StringTointS();
        s.setM1(new HashMap<>());
        for (int i = 0; i < 5; i++) {
            s.getM1().put(String.valueOf(i), i + 100);
        }
        StringTointD d = new StringTointD();
        d.setM1(new HashMap<>());
        for (int i = 3; i < 8; i++) {
            d.getM1().put(i, i + 1000);
        }

        d = mappingManager.mapTo(s, d);
        if (!d.getM1().get("1").equals(101))
            throw new RuntimeException("fail");
        if (!d.getM1().get("4").equals(104))
            throw new RuntimeException("fail");
        if (!d.getM1().get(4).equals(1004))
            throw new RuntimeException("fail");
    }

    /**
     * 测试拷贝列表的时候,能否根据指定的id,合并两个列表
     */
    @Test
    public void nestListJoin() {
        MappingManager mappingManager = new MappingManager();
        //设置处理list的方式为合并
        mappingManager.getDefaultCopyConfig().setCollectionCopyRule(CollectionCopyRule.JOIN);
        mappingManager.registerMapper(NestListDto.class, NestListModel2.class);
        ClassMapper cm = mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);
        cm.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));

        NestListDto source = new NestListDto();
        List<SimpleDto> arr2 = getSimpleDtos();
        source.setArr2(arr2);

        //第一次,不设置SimpleModel的比较规则,合并的时候调用默认的Object.equal
        //由于比较结果都是false,所以相当于覆盖
        NestListModel2 target1 = new NestListModel2();
        List<SimpleModel2> arr2m = getSimpleModel2s();
        target1.setArr2(arr2m);

        target1 = mappingManager.mapTo(source, target1);

        if (target1.getArr2().size() != 5)
            throw new RuntimeException("fail");
        if (target1.getArr2().get(4).getInt5() != 0)
            throw new RuntimeException("fail");

        //第二次,指定合并的时候,用int1作为主键,如果int1相同,认为两个对象相同.
        mappingManager.addCompareRule(SimpleModel2.class, (a, b) -> a.getInt1() == b.getInt1());

        NestListModel2 target2 = new NestListModel2();
        List<SimpleModel2> arr2m2 = getSimpleModel2s();
        target2.setArr2(arr2m2);

        target2 = mappingManager.mapTo(source, target2);

        if (target2.getArr2().size() != 5)
            throw new RuntimeException("fail");
        String ids = intArrayToString(target2.getArr2().stream().mapToInt(a -> a.getInt1()).toArray());
        if (!ids.equals("0,1,2,3,4,"))
            throw new RuntimeException("fail");
        if (target2.getArr2().stream().filter(a -> a.getInt1().equals(2)).findFirst().get().getInt5() != 0)
            throw new RuntimeException("fail");
        if (target2.getArr2().stream().filter(a -> a.getInt1().equals(4)).findFirst().get().getInt5() != 8963)
            throw new RuntimeException("fail");
    }


    /**
     * 测试拷贝列表的时候,能否根据指定的id,合并两个列表
     */
    @Test
    public void nestListFullJoin() {
        MappingManager mappingManager = new MappingManager();
        //设置处理list的方式为合并
        mappingManager.getDefaultCopyConfig().setCollectionCopyRule(CollectionCopyRule.FULL_JOIN);
        mappingManager.registerMapper(NestListDto.class, NestListModel2.class);
        ClassMapper cm = mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);
        cm.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));

        NestListDto source = new NestListDto();
        List<SimpleDto> arr2 = getSimpleDtos();
        source.setArr2(arr2);

        //第一次,不设置SimpleModel的比较规则,合并的时候调用默认的Object.equal
        //由于比较结果都是false,所以相当于覆盖
        NestListModel2 target1 = new NestListModel2();
        List<SimpleModel2> arr2m = getSimpleModel2s();
        target1.setArr2(arr2m);

        target1 = mappingManager.mapTo(source, target1);
        //结果,两个列表合并了,因为没有比较规则,id:0~4,3~7
        if (target1.getArr2().size() != 10)
            throw new RuntimeException("fail");
        String ids1 = intArrayToString(target1.getArr2().stream().mapToInt(a -> a.getInt1()).toArray());
        if (!ids1.equals("0,1,2,3,4,3,4,5,6,7,"))
            throw new RuntimeException("fail");
        if (target1.getArr2().get(4).getInt5() != 0)
            throw new RuntimeException("fail");
        if (target1.getArr2().get(9).getInt5() != 8963)
            throw new RuntimeException("fail");

        //第二次,指定合并的时候,用int1作为主键,如果int1相同,认为两个对象相同.
        mappingManager.addCompareRule(SimpleModel2.class, (a, b) -> a.getInt1() == b.getInt1());

        NestListModel2 target2 = new NestListModel2();
        List<SimpleModel2> arr2m2 = getSimpleModel2s();
        target2.setArr2(arr2m2);

        target2 = mappingManager.mapTo(source, target2);
        //结果,由于有了比较规则,根据id合并,id从0~7,而且3~8保留了特殊属性Int5的值8963
        if (target2.getArr2().size() != 8)
            throw new RuntimeException("fail");
        String ids = intArrayToString(target2.getArr2().stream().mapToInt(a -> a.getInt1()).toArray());
        if (!ids.equals("0,1,2,3,4,5,6,7,"))
            throw new RuntimeException("fail");
        if (target2.getArr2().stream().filter(a -> a.getInt1().equals(2)).findFirst().get().getInt5() != 0)
            throw new RuntimeException("fail");
        if (target2.getArr2().stream().filter(a -> a.getInt1().equals(4)).findFirst().get().getInt5() != 8963)
            throw new RuntimeException("fail");
    }

    @Test
    public void nestMapFullJoin() {
        MappingManager mappingManager = new MappingManager();
        //设置处理list的方式为合并
        mappingManager.getDefaultCopyConfig().setCollectionCopyRule(CollectionCopyRule.FULL_JOIN);
        mappingManager.registerMapper(NestMapDto.class, NestMapModel2.class);
        ClassMapper cm = mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);
        cm.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));

        NestMapDto source = new NestMapDto();
        source.setM(new HashMap<>());
        List<SimpleDto> simpleDtos = getSimpleDtos();
        for (SimpleDto simpleDto : simpleDtos) {
            source.getM().put(simpleDto.getInt1(), simpleDto);
        }

        NestMapModel2 target = new NestMapModel2();
        target.setM(new HashMap<>());
        List<SimpleModel2> simpleModel2s = getSimpleModel2s();
        for (SimpleModel2 simpleModel2 : simpleModel2s) {
            target.getM().put(simpleModel2.getInt1(), simpleModel2);
        }

        target = mappingManager.mapTo(source, target);

        if (target.getM().size() != 8)
            throw new RuntimeException("fail");
        if(target.getM().get(0).getInt5()!=0)
            throw new RuntimeException("fail");
        if(target.getM().get(3).getInt5()!=8963)
            throw new RuntimeException("fail");
    }
    @Test
    public void nestMapJoin() {
        MappingManager mappingManager = new MappingManager();
        //设置处理list的方式为合并
        mappingManager.getDefaultCopyConfig().setCollectionCopyRule(CollectionCopyRule.JOIN);
        mappingManager.registerMapper(NestMapDto.class, NestMapModel2.class);
        ClassMapper cm = mappingManager.registerMapper(SimpleDto.class, SimpleModel2.class);
        cm.setFieldMappingRule("string3", a -> Integer.parseInt((String) a));

        NestMapDto source = new NestMapDto();
        source.setM(new HashMap<>());
        List<SimpleDto> simpleDtos = getSimpleDtos();
        for (SimpleDto simpleDto : simpleDtos) {
            source.getM().put(simpleDto.getInt1(), simpleDto);
        }

        NestMapModel2 target = new NestMapModel2();
        target.setM(new HashMap<>());
        List<SimpleModel2> simpleModel2s = getSimpleModel2s();
        for (SimpleModel2 simpleModel2 : simpleModel2s) {
            target.getM().put(simpleModel2.getInt1(), simpleModel2);
        }

        target = mappingManager.mapTo(source, target);

        if (target.getM().size() != 5)
            throw new RuntimeException("fail");
        if(target.getM().get(0).getInt5()!=0)
            throw new RuntimeException("fail");
        if(target.getM().get(3).getInt5()!=8963)
            throw new RuntimeException("fail");
    }

    //--------------------------------------------------------------------
    private String intArrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i : arr) {
            sb.append(i);
            sb.append(",");
        }
        return sb.toString();
    }

    private List<SimpleDto> getSimpleDtos() {
        List<SimpleDto> arr2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SimpleDto s = new SimpleDto();
            s.setInt1(i);
            s.setInt4(i * 100);
            s.setString2("I am string2:" + i);
            s.setString3(String.valueOf(i + 888));
            arr2.add(s);
        }
        return arr2;
    }

    private List<SimpleModel2> getSimpleModel2s() {
        List<SimpleModel2> arr2m = new ArrayList<>();
        for (int i = 3; i < 8; i++) {
            SimpleModel2 s = new SimpleModel2();
            s.setInt1(i);
            s.setInt4(i * 100);
            s.setString2("I am string2:" + i);
            s.setString3(i * 1000 + 888);
            s.setInt5(8963);
            arr2m.add(s);
        }
        return arr2m;
    }
}
