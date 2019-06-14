# Java对象复制器

## 项目介绍

java的对象复制器,用于在不同类型的对象之间复制字段值.常用于POJO类的复制.也可以用于对象的深拷贝.

举例来说,这个工具可以将DTO类的值拷贝给Model类.避免写一堆转换代码.

基本规则:如果两个类型的两个字段名相同,就可以把值拷贝过去.

支持的类型:Object,基本类型,List,Map,同时也支持嵌套,如Object内部可以嵌套另外一个对象,或者List.

复制过程还可以指定转换规则.可以在复制过程中改变复制的值,甚至是类型.如,复制过程中,字段值可以加1,或者将LocalDateTime类型的字段转换到Long类型的字段上.

性能方面,在第一次调用的时候会计算并缓存映射关系以及目标对象的构造函数,因此拷贝过程的性能有所保证.

## 使用说明

### 引用

'''XML
	<dependency>
			<groupId>com.github.somereason</groupId>
			<artifactId>object_copier</artifactId>
			<version>1.3.1</version>
	</dependency>
'''

### 基本使用

1. 声明MappingManager,注意MappingManager可以重复使用,
2. 注册转换类型,建议在全局声明MappingManager,并在程序初始化的时候注册类型,不要频繁初始化
3. 映射对象

```Java

class From{
    private int studentId;
}
class To{
    private int studentId;
}
class Run{
    static void main(){
        //声明映射管理器，最好定义为全局对象,避免频繁初始化
        MappingManager mappingManager = new MappingManager();
        //注册类型,告诉映射管理器,要把From类拷贝为To类
        //由于二者都有名字为studentId属性,因而From.studentId会拷贝到To.studentId上.
        mappingManager.registerMapper(From.class, To.class);

        //映射方式1：通过map函数 得到一个全新的对象
        To to=mappingManager.map(new From(),To.class);
        //映射方式2：通过mapTo函数，把源对象复制给现有对象
        To toObj=new To();
        toObj=mappingManager.mapTo(new From(),toObj);
    }
}
```

在例子中from和to类都包含名字为studentId的字段,因此,注册之后,程序会记录这个对应关系,把from中的studentId复制到to中的studentId.

例子中,studentId是基本类型(int),object_copier支持如下几种类型:基本类型,对象,list,map

如:

```Java
class From{
    private int a;
    private String b;
    private SomeClass1 c;
    private List<Integer> d;
    private Map<Integer,String> e;
    private List<SomeClass2> f;
    private Map<Integer,SomeClass3> g;
}
```

### 嵌套其他类型

类中定义了其他类,这是一种常见的需求.例如学生类中带着家乡信息,课程信息,这种场景object_copier也能应付.

如想把studentDto映射为StudentModel,定义如下:

```Java
class StudentDto{
    private int id;
    private String name;
    private HometownDto hometown;
    private List<CourseDto> courses;
}
class HometownDto{
    private int cityId;
    private int cityName;
}
class CourseDto{
    private int courseId;
    private String courseName;
}



class StudentModel{
    private int id;
    private String name;
    private HometownModel hometown;
    private List<CourseModel> courses;
}
class HometownModel{
    private int cityId;
    private int cityName;
}
class CourseModel{
    private int courseId;
    private String courseName;
}
```

这种情况下,需要额外注册Hometown,Course,告诉object_copier这两对类也需要转换.程序才能正常工作,如果不注册的话,会忽略StudentModel.hometown这个字段,导致它的值会为空,同时输出一条错误日志.

注册Inner有两种选择

方法1:手工注册Inner

```Java
class Run{
    static void main(){
        //声明映射管理器
        MappingManager mappingManager = new MappingManager();
        //注册类型
        mappingManager.registerMapper(StudentDto.class, StudentModel.class);
        //手工注册
        mappingManager.registerMapper(HometownDto.class, HometownModel.class);
        mappingManager.registerMapper(CourseDto.class, CourseModel.class);

        //得到复制后的对象
        StudentModel to = mappingManager.map(new StudentDto(),StudentModel.class);
    }
}
```

方法2:给StudentDto中,包含特殊对象的字段添加注解.

```Java
class StudentDto{
    private int id;
    private String name;
    @RegisterThisType
    private HometownDto hometown;
    @RegisterThisType
    private List<CourseDto> courses;
}
```

如果用这种方式,就不用手工注册,注册类型的时候只要写一行就可以了.余下的HometownDto,CourseDto会被自动识别.

```Java
mappingManager.registerMapper(StudentDto.class, StudentModel.class);
```

另外对于list和map,也可以用这个注解.使用方法如下,此时会自动发现list和map内部的CourseDto,并建立映射关系.

```Java
class StudentDto{
    private int id;

    @RegisterThisType
    private List<CourseDto> courses;
}
class StudentDto{
    private int id;

    //指定在字段courses中,只注册第2个包含的类型,也就是courses
    @RegisterThisType(typeIndex = {1})
    private Map<Integer,CourseDto> courses;
}
```

注意:

* 如果一个类中出现了两次CourseDto类,RegisterThisType注解标记一次就可以了.当然标记两次也没关系.注册的时候会检测是否重复.

### 映射

实际使用中常常要转换字段的值和类型(否则创建那么多类还有什么意义,直接用原来的类多方便),对此也提供了支持.

使用方式:在注册类的时候,添加转换规则.

这个例子展示了值类型,对象的值转换,以及拷贝过程改变类型

```Java
public class SimpleDto {
    protected Integer int1;
    protected int int4;
    protected String string2;
    protected String string3; //注意两个类的string3,类型不同,一个是string一个是int
}
public class SimpleModel {
    protected Integer int1;
    protected int int4;
    protected String string2;
    protected int string3;
}
class run{
   public static void main() {
        MappingManager mapper = new MappingManager();
        //注册类的时候,拿到ClassMap的引用
        ClassMap<SimpleDto, SimpleModel> mapper1 = mapper.registerMapper(SimpleDto.class, SimpleModel.class);
        //在这个类的映射中,添加规则
        mapper1.setSpecialMappRule("int1", a -> (int) a * 5);//基本类型的特殊映射
        mapper1.setSpecialMappRule("int4", a -> (int) a * 10);//引用类型的特殊映射
        mapper1.setSpecialMappRule("string2", a -> (String) a + (String) a);
        //类型转换,从string到int
        mapper1.setSpecialMappRule("string3", a -> Integer.parseInt((String) a));


        //准备数据
        SimpleDto dto = new SimpleDto();
        dto.setInt1(1000);
        dto.setInt4(-100);
        dto.setString2("文本1");
        dto.setString3("66666");

        //映射
        SimpleModel model = mapper.map(dto,SimpleModel.class);
        //检查结果
        if (!model.getInt1().equals(5000))
            throw new RuntimeException("fail");
        if (model.getInt4() != -1000)
            throw new RuntimeException("fail");
        if (!model.getString2().equals("文本1文本1"))
            throw new RuntimeException("fail");
        if (model.getString3() != 66666)
            throw new RuntimeException("fail");
    }
}
```

### 拷贝规则

在类拷贝的时候,支持使用规则改变拷贝的行为,这些规则可以大大扩展object_copier的应用范围.目前规则有两条,包括copyNull(是否拷贝空值),collectionCopyRule(处理集合拷贝的规则)

初始化MappingManager之后,会应用默认的初始化规则,这个规则是全局的,而且你可以随时修改.

```Java
MappingManager mappingManager = new MappingManager();
//设置处理list的方式为合并
mappingManager.getDefaultCopyConfig().setCollectionCopyRule(CollectionCopyRule.FULL_JOIN);
```

也可以不修改全局规则,而是在每次map的时候,指定本次映射的规则.

指定的时候,只设置你想特殊指定的规则就可以,其他规则会使用全局规则的.

```Java
CopyConfig c = new CopyConfig();
c.setCopyNull(false);//这里只指定了一个规则,其他规则不设置,会自动读取全局规则.
SimpleModel model = mapper.map(dto,SimpleModel.class,c);
```

### 拷贝规则copyNull

* 含义:拷贝的时候,如果源对象的某个字段为空,是跳过这个字段(false),还是把空复制过去(true).
* 可选值:true,false
* 默认值:false
* 用途:如果选择true,拷贝的时候会把null赋值给目标字段.这会忠实的反应源对象的值.但是在某些场合,如果源对象为的某个字段空,我们不希望目标的对应字段被覆盖.这可以用于对对象的部分更新.

### 拷贝规则collectionCopyRule

* 含义:拷贝的时候对集合进行处理的方式
* 可选值:
  * OVER_WRITE(源对象覆盖目标对象,删除target里的所有元素,并用source的覆盖)
  * JOIN(半合并,以source中的元素为主,但是如果target有相同的元素,那么会对相同的元素进行合并.)
  * FULL_JOIN(完全合并,源集合和目标集合里的元素都会保留,重复的会合并.)
* 默认值:JOIN

### 定义比较方式

刚才可能会引起疑问,如果认为集合中两个对象是相同的?答案是我也不知道,在合并集合的时候,默认采用Object.equal()比较对象,但很多情况下,这种方式是不合适的.比如.通常当两个student对象的id相同,我们就认为两个对象是一样.但此时如果还调用equal来比较两个对象,返回结果是false(除非你override了equal方法).鉴于有写equal习惯的人不多,而且有些时候写不了equal函数(比如自动生成的类).这里提供了addCompareRule函数,来注册类的比较方法.

```Java
mappingManager.addCompareRule(StudentModel.class, (a, b) -> a.getId() == b.getId());
```

上面的例子给studentModel类设定了比较规则：当ID相同的时候就认为两个类相同,如果你愿意,可以给所有的类都注册上比较函数,但这样做意义不大,通常,给list所包含的类注册上就可以了.只有它们才会面临要比较的情况.

### 输出日志

为了不因为一些字段的错误中断拷贝过程,object_copier会捕获一些不重要的错误,然后输出日志.

使用者可以注册一个回调函数来输出这些日志.回调函数是一个Consumer<String>,注册的方式为:

```Java
mappingManager.setWriteLogFunction(a -> {
        a = "自动拷贝过程出现错误:" + a;
        logger.info(a);
    });
```
## 改进方向

1. 支持继承机制,能够拷贝继承得到的field的值.
1. 支持枚举
