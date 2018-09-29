# Java对象复制器

## 项目介绍

java的对象复制器,如果两个类型有相同的field.通过这个工具,可以把相同名字的field的值复制过去.

复制过程还可以指定转换规则.可以在复制过程中改变复制的值,甚至是类型.

这个工具可以当作对象的深拷贝器使用.

## 软件架构

软件架构说明

## 安装教程

引用jar即可

## 使用说明

### 基本使用

1. 声明MappingManager,注意MappingManager可以重复使用,
2. 注册转换类型,建议在全局声明MappingManager,并在程序初始化的时候注册类型,不要频繁初始化
3. 映射对象

```Java
class From{
    private int a;
}
class To{
    private int a;
}
class Run{
    static void main(){
        //声明映射管理器，最好定义为全局对象
        MappingManager mappingManager = new MappingManager();
        //注册类型,从From转换到To
        mappingManager.registerMapper(From.class, To.class);

        //方式1：通过map函数 得到一个全新的对象
        To to=mappingManager.map(new From(),To.class);
        //方式2：通过mapTo函数，把源对象复制给现有对象
        To toObj=new To();
        toObj=mappingManager.mapTo(new From(),toObj);
    }
}
```

在例子中from和to的字段名都是a,因此,注册之后,程序会记录这个对应关系,把from中的a复制到to中的a.

这里还有一个标记为已过期的map方法，可以不指定目标类型，但是由于调用的时候会查找需要转化为哪种类型，影响性能，不推荐使用。

例子中,a是基本类型(int),实际上字段支持基本类型,对象,list,map

以下写法都支持

```Java
class From{
    private int a;
    private String b;
    private java.time.LocalDateTime c;
    private List<Integer> d;
    private Map<Integer,String> e;
}
```

另外，mapTo函数还有一个可选参数叫isCopyNull，如果isCopyNull==true，那么在复制的时候，如果源对象的属性的值是null，那么复制给目标对象的时候，目标对象的属性a的值将被覆写为null。如果isCopyNull==false，那么在遇到属性值为null的时候，会认为i想保留目标对象当前的值，不会用null覆盖目标对象的对应属性。

### 嵌套其他类型

很常见的需求是类型中嵌套其他类型.如

```Java
class From{
    private int a;
    private Inner b;
}
class Inner{
    private int h;
}

class To{
    private int a;
    private InnerTo b;
}
class InnerTo{
    private int h;
}
```

这种情况下,需要额外注册Inner类,程序才能识别并正确转换.注册Inner有两种选择

方法1:手工注册Inner

```Java
class Run{
    static void main(){
        //声明映射管理器
        MappingManager mappingManager = new MappingManager();
        //注册类型
        mappingManager.registerMapper(From.class, To.class);
        //手工注册
        mappingManager.registerMapper(Inner.class, InnerTo.class);

        //得到复制后的对象
        To to=mappingManager.map(new From(),To.class);
    }
}
```

方法2:给Inner字段添加注解.

```Java
class From{
    private int a;

    @RegisterThisType
    private Inner b;
}
```

如果用这种方式,就不用手工注册,初始化时的mappingManager.registerMapper(Inner.class, InnerTo.class);可以不写.

另外对于list和map,也可以用这个注解.

```Java
class From{
    private int a;

    @RegisterThisType
    private List<Inner> b;
}
class From{
    private int a;

    //指定在字段arr2中,只注册第2个包含的类型,也就是Inner
    @RegisterThisType(typeIndex = {1})
    private Map<Integer,Inner> b;
}
```

注意,
1. 如果一个类中出现了两次Inner类,RegisterThisType注解标记一次就可以了.当然标记两次也没关系.注册的时候会检测是否重复.

### 映射

实际使用中常常要转换字段的值和类型(否则创建那么多类还有什么意义,直接用原来的类多方便),对此也提供了支持.使用方式:在注册类的时候,添加转换规则.

这个例子展示了值类型,对象的值转换,以及拷贝过程改变类型

```Java
public class SimpleDto {
    protected Integer int1;
    protected int int4;
    protected String string2;
    protected String string3;
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
        SimpleModel model = mapper.map(dto);
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

## 改进方向

1. 支持继承机制,能够拷贝继承得到的field的值.
