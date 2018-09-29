package win.somereason.utils.object_copier;

import win.somereason.utils.object_copier.entity.MappingException;
import win.somereason.utils.object_copier.entity.FieldType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 映射两个类
 * on 2017/4/20.
 */
public class ClassMapper<S, T> {

    public List<FieldMapper> fieldMappers;
    protected Class<S> sourceClass;
    protected Class<T> targetClass;


    public ClassMapper(Class<S> sourceClass, Class<T> targetClass) throws MappingException {

        setMapping(sourceClass, targetClass);
    }

    public static String getMappingKey(Class from, Class to) {
        return String.format("%s|%s", from.getTypeName(), to.getTypeName());
    }


    private void setMapping(Class<S> sourceClass, Class<T> targetClass) throws MappingException {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;

        List<Field> typeSFields = Arrays.asList(sourceClass.getDeclaredFields());
        List<Field> typeDFields = Arrays.asList(targetClass.getDeclaredFields());
        List<FieldMapper> mapperList = new ArrayList<>();
        for (Field typeSField : typeSFields) {
            //不处理static字段
            if (java.lang.reflect.Modifier.isStatic(typeSField.getModifiers())) {
                continue;
            }
            Optional<Field> typeDField = typeDFields.stream().filter(a -> a.getName().equals(typeSField.getName())).findAny();
            //在目的类中不存在,则不copy
            if (!typeDField.isPresent())
                continue;
            typeSField.setAccessible(true);
            typeDField.get().setAccessible(true);
            FieldMapper mapper = new FieldMapper();
            mapper.sourceField = typeSField;
            mapper.targetField = typeDField.get();

            mapper.isFieldTypeEqual = mapper.sourceField.getType().getName().equals(mapper.targetField.getType().getName());

            //目的类的类型
            if (mapper.targetField.getType().isPrimitive()) {
                if (mapper.targetField.getType() == int.class)
                    mapper.targetType = FieldType.INT;
                if (mapper.targetField.getType() == long.class)
                    mapper.targetType = FieldType.LONG;
                if (mapper.targetField.getType() == boolean.class)
                    mapper.targetType = FieldType.BOOLEAN;
                if (mapper.targetField.getType() == byte.class)
                    mapper.targetType = FieldType.BYTE;
                if (mapper.targetField.getType() == char.class)
                    mapper.targetType = FieldType.CHAR;
                if (mapper.targetField.getType() == short.class)
                    mapper.targetType = FieldType.SHORT;
                if (mapper.targetField.getType() == float.class)
                    mapper.targetType = FieldType.FLOAT;
                if (mapper.targetField.getType() == double.class)
                    mapper.targetType = FieldType.DOUBLE;
            } else if (Map.class.isAssignableFrom(mapper.targetField.getType()))
                mapper.targetType = FieldType.MAP;
            else if (Collection.class.isAssignableFrom(mapper.targetField.getType()))
                mapper.targetType = FieldType.COLLECTION;
            //源头类的类型
            if (typeSField.getType().isPrimitive()) {
                if (mapper.sourceField.getType() == int.class)
                    mapper.sourceType = FieldType.INT;
                if (mapper.sourceField.getType() == long.class)
                    mapper.sourceType = FieldType.LONG;
                if (mapper.sourceField.getType() == boolean.class)
                    mapper.sourceType = FieldType.BOOLEAN;
                if (mapper.sourceField.getType() == byte.class)
                    mapper.sourceType = FieldType.BYTE;
                if (mapper.sourceField.getType() == char.class)
                    mapper.sourceType = FieldType.CHAR;
                if (mapper.sourceField.getType() == short.class)
                    mapper.sourceType = FieldType.SHORT;
                if (mapper.sourceField.getType() == float.class)
                    mapper.sourceType = FieldType.FLOAT;
                if (mapper.sourceField.getType() == double.class)
                    mapper.sourceType = FieldType.DOUBLE;
            } else if (Map.class.isAssignableFrom(typeSField.getType())) {
                if (Map.class.isAssignableFrom(typeDField.get().getType()))
                    mapper.sourceType = FieldType.MAP;
                else
                    throw new MappingException("当源类型是map的时候,目标类型必须也是map");
            } else if (Collection.class.isAssignableFrom(typeSField.getType())) {
                if (Collection.class.isAssignableFrom(typeDField.get().getType()))
                    mapper.sourceType = FieldType.COLLECTION;
                else
                    throw new MappingException("当源类型是list的时候,目标类型必须也是list");
            }

            mapperList.add(mapper);
        }

        fieldMappers = mapperList;
    }

    /**
     * 注册特殊映射规则,可以把数据变换后再赋给新的field,还可以一个类型转化为另一个类型
     *
     * @param sourceClassFieldName 需要映射的field的名字
     * @param specialMapRule       映射的方式
     * @throws MappingException
     */
    public void setFieldMappingRule(String sourceClassFieldName, Function specialMapRule) throws MappingException {
        Optional<FieldMapper> targetField = fieldMappers.stream().filter(b -> b.sourceField.getName().equals(sourceClassFieldName)).findAny();
        if (targetField.isPresent()) {
            targetField.get().mappingRule = specialMapRule;
        } else
            throw new MappingException("没有找到要设置的属性:" + sourceClassFieldName);
    }

    public String getSourceClassName() {
        return this.sourceClass.getTypeName();
    }

    public String getKeyName() {
        return getMappingKey(this.sourceClass, this.targetClass);
    }

    public String toString() {
        return String.format("(%s -> %s)", this.sourceClass.getName(), this.targetClass.getName());
    }

}