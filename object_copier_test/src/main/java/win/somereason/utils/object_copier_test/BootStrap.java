package win.somereason.utils.object_copier_test;


import win.somereason.utils.object_copier.ClassMapper;
import win.somereason.utils.object_copier.MappingManager;
import win.somereason.utils.object_copier_test.entity.ListMapNestDto;
import win.somereason.utils.object_copier_test.entity.ListMapNestModel;
import win.somereason.utils.object_copier_test.entity.SimpleDto;
import win.somereason.utils.object_copier_test.entity.SimpleModel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by somereason on 2018/7/24.
 */
public class BootStrap {
    public static void main(String[] args) {
        PerformanceTest();
    }


    /**
     * 性能测试.对比手工转换的代码,和自动转换的性能.
     * 避免GC的影响,请分开测试.
     */
    public static void PerformanceTest() {
        MappingManager mapper = new MappingManager();

        ClassMapper<ListMapNestDto, ListMapNestModel> mapper1 = mapper.registerMapper(ListMapNestDto.class, ListMapNestModel.class);
        mapper1.setFieldMappingRule("time1", a -> LocalDateTime.ofEpochSecond((long) a, 0, ZoneOffset.UTC));
        ClassMapper<SimpleDto, SimpleModel> mapper2 = mapper.registerMapper(SimpleDto.class, SimpleModel.class);
        mapper2.setFieldMappingRule("int1", a -> (int) a * 5);
        mapper2.setFieldMappingRule("int4", a -> (int) a * 10);
        mapper2.setFieldMappingRule("string2", a -> (String) a + (String) a);
        mapper2.setFieldMappingRule("string3", a -> ((String) a).length());


        long startTick1, endTick1;
        startTick1 = System.currentTimeMillis();
        List<ListMapNestDto> dtos = GetData();
        endTick1 = System.currentTimeMillis();
        System.out.println("生成数据的时间:" + (endTick1 - startTick1));

        /*
        生成数据的时间:125
        自动映射的时间:5243
         */
//        startTick1 = System.currentTimeMillis();
//        for (int i = 0; i < 500; i++) {
//            List<Test1Model> modelAuto = mapper.map(dtos);
//        }
//        endTick1 = System.currentTimeMillis();
//        System.out.println("自动映射的时间:" + (endTick1 - startTick1));

        /*
        生成数据的时间:108
        手工映射的时间:1389
         */
        startTick1 = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            List<ListMapNestModel> modelByHands = Convert(dtos);
        }
        endTick1 = System.currentTimeMillis();
        System.out.println("手工映射的时间:" + (endTick1 - startTick1));

    }

    public static List<ListMapNestModel> Convert(List<ListMapNestDto> listS) {
        List<ListMapNestModel> listD = new ArrayList<>(listS.size());
        for (ListMapNestDto test1Dto : listS) {
            ListMapNestModel m1 = new ListMapNestModel();
            m1.setInt1(test1Dto.getInt1());
            m1.setInt2(test1Dto.getInt2());
            m1.setTime1(LocalDateTime.ofEpochSecond(test1Dto.getTime1(), 0, ZoneOffset.UTC));
            m1.setString1(test1Dto.getString1());

            SimpleModel x2 = new SimpleModel();
            x2.setInt1(test1Dto.getTest2().getInt1() * 5);
            x2.setInt4(test1Dto.getTest2().getInt4() * 10);
            x2.setString3(test1Dto.getTest2().getString3().length());
            x2.setString2(test1Dto.getTest2().getString2() + test1Dto.getTest2().getString2());

            m1.setArr1(test1Dto.getArr1());
//            m1.setM(new ArrayList<>());
//            for (SimpleDto test2Dto : test1Dto.getM()) {
//                SimpleModel temp2Model = new SimpleModel();
//                temp2Model.setInt1(test2Dto.getInt1() * 5);
//                temp2Model.setInt4(test2Dto.getInt4() * 10);
//                temp2Model.setString2(test2Dto.getString2() + test2Dto.getString2());
//                if (test2Dto.getString3() != null)
//                    temp2Model.setString3(test2Dto.getString3().length());
//                m1.getM().add(temp2Model);
//            }

            m1.setMap1(test1Dto.getMap1());
            listD.add(m1);
        }
        return listD;
    }

    public static List<ListMapNestDto> GetData() {
        Random r = new Random();
        int size = 5000;
        List<ListMapNestDto> ret = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ListMapNestDto x = new ListMapNestDto();
            x.setString1("我是test1");
            x.setInt2(r.nextInt());
            x.setInt1(i);
            x.setTime1(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            SimpleDto x2 = new SimpleDto();
            x2.setInt1(r.nextInt());
            x2.setInt4(r.nextInt());
            x2.setString2("从test2而来");
            x2.setString3("我的长度");
            x.setTest2(x2);
            x.setArr1(new ArrayList<>());
            x.getArr1().add(i + 1);
            x.getArr1().add(i + 6);
            x.getArr1().add(i + 11);
//            x.setM(new ArrayList<>());
            SimpleDto x21 = new SimpleDto();
            x21.setInt1(i + 999);
            x21.setInt4(i + 888);
            x21.setString2("从test2而来,as member of m");
//            x.getM().add(x21);
            HashMap<Integer, String> map1 = new HashMap<>();
            map1.put(i, "MapTest");
            map1.put(i + size, "MapTest1");
            x.setMap1(map1);

            ret.add(x);
        }
        return ret;
    }

}
