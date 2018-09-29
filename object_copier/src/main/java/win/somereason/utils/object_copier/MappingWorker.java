package win.somereason.utils.object_copier;

import win.somereason.utils.object_copier.entity.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * 执行映射操作类,执行映射
 */
public class MappingWorker {

    public Consumer<String> writeLogFunction;
    /**
     * 存放映射关系的map
     */
    ConcurrentHashMap<String, ClassMapper> mappers = new ConcurrentHashMap<>();
    /**
     * 缓存构造函数,不需要每次重新查找
     */
    ConcurrentHashMap<Class, Constructor<?>> constructorMap = new ConcurrentHashMap<>();

    /**
     * 目标类比较方法.
     * 在复制list的时候,如果希望合并list,那么需要用到这个属性.
     * 合并的时候需要把相同的对象合起来,不能形成两个一样的对象.
     * 如果不设置,默认调用equal方法,比较两个对象,如果设置,根据规则比较
     * 例如,对于student类,如果学号(studentId)相同,就认为是一个对象,会用新的值覆盖旧的.
     * 此时可以设置 targetCompareRule = (a, b) -> a.studentId == b.studentId;
     */
    ConcurrentHashMap<Class, BiPredicate> targetCompareRule;

    /**
     * 是否需要初始化,
     * 每次添加映射之后,都要重新初始化
     */
    private boolean needInitialization = true;


    //===========初始化部分============//


    public ClassMapper registerMapper(Class typeS, Class typeD) throws MappingException {

        targetCompareRule = new ConcurrentHashMap<>();
        String mappingKey = ClassMapper.getMappingKey(typeS, typeD);
        //如果已经注册过了,直接返回引用.
        if (mappers.containsKey(mappingKey))
            return mappers.get(mappingKey);

        ClassMapper propMapper = new ClassMapper(typeS, typeD);
        mappers.put(mappingKey, propMapper);
        //查找所有字段,看看那个字段的类型需要转换,自动注册.
        registerClassInFields(propMapper);

        needInitialization = true;
        return propMapper;
    }

    public <T> void addTargetCompareRule(Class<T> type, BiPredicate<T, T> rule) {
        this.targetCompareRule.put(type, rule);
    }

    /**
     * 查找所有字段,看看那个字段的类型需要转换,自动注册.
     *
     * @param propMapper
     */
    private void registerClassInFields(ClassMapper propMapper) {
        for (Object f : propMapper.fieldMappers) {
            FieldMapper fmap = (FieldMapper) f;
            //需要注册规则:源和目的类型不相等,并且都是对象
            if (fmap.sourceField.isAnnotationPresent(RegisterThisType.class)) {
                if (!fmap.isFieldTypeEqual) {
                    String mappingKey = ClassMapper.getMappingKey(fmap.sourceField.getType(), fmap.targetField.getType());
                    if (!mappers.containsKey(mappingKey)) {
                        //普通对象
                        ClassMapper tempPropMapper = new ClassMapper(fmap.sourceField.getType(), fmap.targetField.getType());
                        mappers.put(mappingKey, tempPropMapper);
                        //递归查找新注册类的字段中,是否有需要注册的新类
                        registerClassInFields(tempPropMapper);
                    }

                } else if (fmap.sourceType == FieldType.COLLECTION
                        && fmap.targetType == FieldType.COLLECTION) {
                    String mappingKey = ClassMapper.getMappingKey((Class) ((ParameterizedType) fmap.sourceField.getGenericType()).getActualTypeArguments()[0],
                            (Class) ((ParameterizedType) fmap.targetField.getGenericType()).getActualTypeArguments()[0]);
                    if (!mappers.containsKey(mappingKey)) {
                        //如果是list,列表包含的类型是否需要注册,
                        ClassMapper tempPropMapper = new ClassMapper(
                                (Class) ((ParameterizedType) fmap.sourceField.getGenericType()).getActualTypeArguments()[0],
                                (Class) ((ParameterizedType) fmap.targetField.getGenericType()).getActualTypeArguments()[0]);
                        mappers.put(mappingKey, tempPropMapper);
                        //递归查找新注册类的字段中,是否有需要注册的新类
                        registerClassInFields(tempPropMapper);
                    }

                } else if (fmap.sourceType == FieldType.MAP
                        && fmap.targetType == FieldType.MAP) {
                    //如果是map,map包含的类型是否需要注册,注意map包含两个类型,分别是key和value的类型,需要用RegisterThisType的TypeIndex属性指定要注册哪个类型
                    int[] typeIndexs = fmap.sourceField.getAnnotation(RegisterThisType.class).typeIndex();
                    for (int i = 0; i < typeIndexs.length; i++) {
                        String mappingKey = ClassMapper.getMappingKey((Class) ((ParameterizedType) fmap.sourceField.getGenericType()).getActualTypeArguments()[typeIndexs[i]],
                                (Class) ((ParameterizedType) fmap.targetField.getGenericType()).getActualTypeArguments()[typeIndexs[i]]);
                        if (!mappers.containsKey(mappingKey)) {
                            ClassMapper tempPropMapper = new ClassMapper(
                                    (Class) ((ParameterizedType) fmap.sourceField.getGenericType()).getActualTypeArguments()[typeIndexs[i]],
                                    (Class) ((ParameterizedType) fmap.targetField.getGenericType()).getActualTypeArguments()[typeIndexs[i]]);
                            mappers.put(mappingKey, tempPropMapper);
                            //递归查找新注册类的字段中,是否有需要注册的新类
                            registerClassInFields(tempPropMapper);
                        }
                    }
                }

            }
        }

    }

    private void initialization() {
        needInitialization = false;
        //检测每一个映射的源类型,是否在映射列表中,如果在,说明这个属性是一个可以映射的属性.
        for (Map.Entry<String, ClassMapper> entry : mappers.entrySet()) {
            for (int i = 0; i < entry.getValue().fieldMappers.size(); i++) {
                String mappingKey = ClassMapper.getMappingKey(
                        ((FieldMapper) entry.getValue().fieldMappers.get(i)).sourceField.getType(),
                        ((FieldMapper) entry.getValue().fieldMappers.get(i)).targetField.getType()
                );
                if (mappers.containsKey(mappingKey))
                    ((FieldMapper) entry.getValue().fieldMappers.get(i)).sourceType = FieldType.TYPE_REGISTERD;
            }
        }
    }


    //==================对外接口============================//


    /**
     * 把source的属性映射到dest上
     *
     * @param source     源对象
     * @param dest       目标对象
     * @param copyConfig 复制的配置,一定要传来传去,不能设置为全局,这是为并发考虑的
     * @param <T>
     * @param <K>
     */
    public <T, K> T mapTo(K source, T dest, CopyConfig copyConfig) {
        if (needInitialization) {
            initialization();
        }
        if (source == null) {
            dest = null;
            return dest;
        }
        if (dest == null) {
            writeLogFunction.accept(String.format("无法转换%s,目标对象是空的,不知道复制成哪种对象", source.getClass().getName()));
            return null;
        }

        String sourceTypeStr = ClassMapper.getMappingKey(source.getClass(), dest.getClass());

        ClassMapper currentMap = mappers.get(sourceTypeStr);
        //如果没有定义这个类型的转换map,返回原值
        if (currentMap == null) {
            dest = (T) source;
            return dest;
        }

        for (Object fieldMapperTemp : currentMap.fieldMappers) {
            FieldMapper fieldMapper = (FieldMapper) fieldMapperTemp;
            try {

                //只有在双方类型相同,都是基本类型,而且没有使用转换函数的时候,才可以进行快速的直接转换
                //如果使用了转换函数,那么先通过装箱拆箱变为object,然后计算,最后转换成目的类型.
                if (fieldMapper.isFieldTypeEqual && fieldMapper.sourceType.value() < 10 && Objects.isNull(fieldMapper.mappingRule)) {
                    assignValueForBasicType(source, dest, fieldMapper);
                } else {
                    Object fieldValue = getFieldValue(source, fieldMapper, fieldMapper.targetField.get(dest), copyConfig);
                    //函数为空,或者获取的对象为空,不进行计算
                    if (Objects.nonNull(fieldMapper.mappingRule) && Objects.nonNull(fieldValue)) {
                        fieldValue = fieldMapper.mappingRule.apply(fieldValue);
                    }
                    setFieldValue(dest, fieldValue, fieldMapper, copyConfig);
                }


            } catch (Exception ex) {
                writeLogFunction.accept(String.format("Get error while copy properity %s: %s", fieldMapper.sourceField.getName(), ex.toString()));
                continue;
            }

        }
        return dest;
    }


    //===========映射部分============//


    private Object getFieldValue(Object entity, FieldMapper fieldMapper, Object targetObject, CopyConfig copyConfig) throws IllegalAccessException, InstantiationException {
        Object retValue = null;
        switch (fieldMapper.sourceType) {
            case BOOLEAN:
            case INT:
            case BYTE:
            case CHAR:
            case LONG:
            case FLOAT:
            case SHORT:
            case DOUBLE:
            case GENERAL_OBJECT:
                retValue = getBasicObjectValue(entity, fieldMapper);
                break;
            case TYPE_REGISTERD:
                /**
                 * 如果某一个类型在映射表中注册了,则递归进去,把每个字段也逐个映射一下.
                 */
                if (targetObject != null)
                    retValue = targetObject;
                else
                    retValue = getNewObject(fieldMapper.targetField.getType());
                retValue = this.mapTo(fieldMapper.sourceField.get(entity), retValue, copyConfig);
                break;
            case COLLECTION:
                if (targetObject != null)
                    retValue = targetObject;
                retValue = getValueForCollection(entity, fieldMapper, (List) retValue, copyConfig);
                break;
            case MAP:
                if (targetObject != null)
                    retValue = targetObject;
                retValue = getValueForMap(entity, fieldMapper, (Map) retValue, copyConfig);
                break;
            default:
                retValue = getBasicObjectValue(entity, fieldMapper);
                break;
        }
        return retValue;
    }

    private void setFieldValue(Object targetEntity, Object targetValue, FieldMapper fieldMapper, CopyConfig copyConfig) throws IllegalAccessException {

        switch (fieldMapper.targetType) {
            case INT:
                if (targetValue == null) break;
                fieldMapper.targetField.setInt(targetEntity, (int) targetValue);
                break;
            case LONG:
                if (targetValue == null) break;
                fieldMapper.targetField.setLong(targetEntity, (long) targetValue);
                break;
            case BOOLEAN:
                if (targetValue == null) break;
                fieldMapper.targetField.setBoolean(targetEntity, (boolean) targetValue);
                break;
            case BYTE:
                if (targetValue == null) break;
                fieldMapper.targetField.setByte(targetEntity, (byte) targetValue);
                break;
            case CHAR:
                if (targetValue == null) break;
                fieldMapper.targetField.setChar(targetEntity, (char) targetValue);
                break;
            case SHORT:
                if (targetValue == null) break;
                fieldMapper.targetField.setShort(targetEntity, (short) targetValue);
                break;
            case FLOAT:
                if (targetValue == null) break;
                fieldMapper.targetField.setFloat(targetEntity, (float) targetValue);
                break;
            case DOUBLE:
                if (targetValue == null) break;
                fieldMapper.targetField.setDouble(targetEntity, (double) targetValue);
                break;
            case COLLECTION:
            case MAP:
                if (targetValue == null) {
                    if (copyConfig.getCopyNull())
                        fieldMapper.targetField.set(targetEntity, null);
                } else
                    fieldMapper.targetField.set(targetEntity, targetValue);
                break;
            default:
                if (targetValue == null) {
                    if (copyConfig.getCopyNull())
                        fieldMapper.targetField.set(targetEntity, null);
                } else if (targetValue.getClass().equals(fieldMapper.targetField.getType()))
                    fieldMapper.targetField.set(targetEntity, targetValue);
                break;
        }
    }

    private Object getValueForCollection(Object entity, FieldMapper fieldMapper, List targetList, CopyConfig copyConfig) throws
            IllegalAccessException, InstantiationException {
        List sourceItems = (List) fieldMapper.sourceField.get(entity);
        if (sourceItems == null)
            return null;
        Class targetType = (Class) ((ParameterizedType) fieldMapper.targetField.getGenericType()).getActualTypeArguments()[0];

        List newItems = sourceItems.getClass().newInstance();//不可以用 fieldMapper.sourceField.newInstance，因为这极有可能是list接口，没有构造函数

        for (int i = 0; i < sourceItems.size(); i++) {
            //先复制出一个新对象,然后才能去查找是否在target里是否存在,
            //这会导致复制两次,但是很无奈,好像没有别的办法.
            //因为比较方法是针对target类型的.
            //尤其是source和target的key类型都有可能不一样(通过映射关系转换的)
            Object tempTargetItem = mapTo(sourceItems.get(i), getNewObject(targetType), copyConfig);
            int position = searchItemPosition(targetList, tempTargetItem, this.targetCompareRule.get(targetType));
            if (position < 0 || copyConfig.getCollectionCopyRule() == CollectionCopyRule.OVER_WRITE) {
                newItems.add(tempTargetItem);//在目标列表中没有这个.不需要再次复制
            } else {
                tempTargetItem = mapTo(targetList.get(position), tempTargetItem, copyConfig);
                newItems.add(tempTargetItem);//在目标列表有这个,需要基于找到的对象再复制一次,把多余的属性搞过来
            }
        }
        //把target中原有的,但是source中没有的,加进来.实现合并两者的效果.
        if (targetList != null && copyConfig.getCollectionCopyRule() == CollectionCopyRule.FULL_JOIN) {
            for (Object tarObj : targetList) {
                int position = searchItemPosition(newItems, tarObj, this.targetCompareRule.get(targetType));
                if (position < 0)
                    newItems.add(tarObj);
            }
        }
        return newItems;
    }

    private int searchItemPosition(List list, Object obj2Search, BiPredicate compare) {
        int position = -1;
        if (list == null)
            return position;
        for (int i = 0; i < list.size(); i++) {
            if (compare == null) {
                if (obj2Search.equals(list.get(i))) {
                    position = i;
                    break;
                }
            } else {
                if (compare.test(obj2Search, list.get(i))) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    private Object getValueForMap(Object entity, FieldMapper fieldMapper, Map targetMap, CopyConfig copyConfig) throws
            IllegalAccessException, InstantiationException {
        Map sourceItems = (Map) fieldMapper.sourceField.get(entity);
        if (sourceItems == null)
            return null;
        Class keyType = (Class) ((ParameterizedType) fieldMapper.targetField.getGenericType()).getActualTypeArguments()[0];
        Class valueType = (Class) ((ParameterizedType) fieldMapper.targetField.getGenericType()).getActualTypeArguments()[1];

        Map newItems = sourceItems.getClass().newInstance();//不可以用 fieldMapper.sourceField.newInstance，因为这极有可能是list接口，没有构造函数

        //转换并合并二者共有的
        for (Object item : sourceItems.entrySet()) {
            Object sourceKey = ((Map.Entry) item).getKey();
            Object sourceValue = ((Map.Entry) item).getValue();

            //先转换一下key,比较key的时候使用target的类型是最基本的礼仪对不对.
            Object tempNewKey = this.mapTo(sourceKey, getNewObject(keyType), copyConfig);
            if (copyConfig.getCollectionCopyRule() == CollectionCopyRule.OVER_WRITE
                    || targetMap == null || !targetMap.containsKey(tempNewKey)) {
                newItems.put(tempNewKey, this.mapTo(sourceValue, getNewObject(valueType), copyConfig));
            } else {
                newItems.put(tempNewKey, this.mapTo(sourceValue, targetMap.get(tempNewKey), copyConfig));
            }
        }
        //把target中原有的,但是source中没有的,加进来.实现合并两者的效果.
        if (targetMap != null && copyConfig.getCollectionCopyRule() == CollectionCopyRule.FULL_JOIN) {
            for (Object item : targetMap.entrySet()) {
                Object key = ((Map.Entry) item).getKey();
                Object value = ((Map.Entry) item).getValue();
                if (!newItems.containsKey(key))
                    newItems.put(key, value);
            }
        }
        return newItems;
    }

    /**
     * 基本类型转换,不支持函数,不支持不同类型,提高性能
     *
     * @param entity       源对象
     * @param targetEntity 目的对象
     * @param fieldMapper  映射器
     * @throws IllegalAccessException
     */
    private void assignValueForBasicType(Object entity, Object targetEntity, FieldMapper fieldMapper) throws
            IllegalAccessException {
        switch (fieldMapper.sourceType) {
            case INT:
                fieldMapper.targetField.setInt(targetEntity, fieldMapper.sourceField.getInt(entity));
                break;
            case LONG:
                fieldMapper.targetField.setLong(targetEntity, fieldMapper.sourceField.getLong(entity));
                break;
            case BOOLEAN:
                fieldMapper.targetField.setBoolean(targetEntity, fieldMapper.sourceField.getBoolean(entity));
                break;
            case BYTE:
                fieldMapper.targetField.setByte(targetEntity, fieldMapper.sourceField.getByte(entity));
                break;
            case CHAR:
                fieldMapper.targetField.setChar(targetEntity, fieldMapper.sourceField.getChar(entity));
                break;
            case SHORT:
                fieldMapper.targetField.setShort(targetEntity, fieldMapper.sourceField.getShort(entity));
                break;
            case FLOAT:
                fieldMapper.targetField.setFloat(targetEntity, fieldMapper.sourceField.getFloat(entity));
                break;
            case DOUBLE:
                fieldMapper.targetField.setDouble(targetEntity, fieldMapper.sourceField.getDouble(entity));
                break;
            default:
                fieldMapper.targetField.set(targetEntity, fieldMapper.sourceField.get(entity));
                break;
        }
    }

    private Object getBasicObjectValue(Object entity, FieldMapper fieldMapper) throws IllegalAccessException {

        Object retVal;
        switch (fieldMapper.sourceType) {
            case INT:
                retVal = fieldMapper.sourceField.getInt(entity);
                break;
            case LONG:
                retVal = fieldMapper.sourceField.getLong(entity);
                break;
            case BOOLEAN:
                retVal = fieldMapper.sourceField.getBoolean(entity);
                break;
            case BYTE:
                retVal = fieldMapper.sourceField.getByte(entity);
                break;
            case CHAR:
                retVal = fieldMapper.sourceField.getChar(entity);
                break;
            case SHORT:
                retVal = fieldMapper.sourceField.getShort(entity);
                break;
            case FLOAT:
                retVal = fieldMapper.sourceField.getFloat(entity);
                break;
            case DOUBLE:
                retVal = fieldMapper.sourceField.getDouble(entity);
                break;
            default:
                retVal = fieldMapper.sourceField.get(entity);

        }
        return retVal;

    }

    /**
     * 获取新的对象
     *
     * @param c
     * @param <T>
     * @return
     */
    public <T> T getNewObject(Class<T> c) {
        try {
            if (c.isPrimitive()) {
                if (c == int.class)
                    return (T) new Integer(0);
                else if (c == long.class)
                    return (T) new Long(0L);
                else if (c == boolean.class)
                    return (T) new Boolean(false);
                else if (c == byte.class)
                    return (T) new Byte(Byte.MIN_VALUE);
                else if (c == char.class)
                    return (T) new Character('\u0000');
                else if (c == short.class)
                    return (T) new Short(Short.MIN_VALUE);
                else if (c == float.class)
                    return (T) new Float(0.0f);
                else if (c == double.class)
                    return (T) new Double(0.0d);
            }

            Constructor<?> cachedConstructor = constructorMap.get(c);
            if (cachedConstructor != null) {
                Object[] paras = getParas(cachedConstructor);
                return (T) cachedConstructor.newInstance(paras);
            }

            Constructor<?>[] cons = c.getConstructors();
            if (cons.length > 0)//public
            {
                for (Constructor<?> con : cons) {
                    if (con.getParameterCount() < 1)//存在无参数的构造函数,大部分应该是这种情况
                    {
                        constructorMap.putIfAbsent(c, con);
                        return (T) con.newInstance();
                    }
                }
                Constructor<?> tempCon = cons[0];
                constructorMap.putIfAbsent(c, tempCon);
                Object[] paras = getParas(tempCon);
                return (T) tempCon.newInstance(paras);

            } else {//private constructers
                Constructor<?>[] dcons = c.getDeclaredConstructors();
                if (dcons.length == 0)
                    throw new MappingException("No constructior found on type:" + c.getName());
                Constructor<?> tempCon = dcons[0];
                constructorMap.putIfAbsent(c, tempCon);
                tempCon.setAccessible(true);
                Object[] paras = getParas(tempCon);
                return (T) tempCon.newInstance(paras);
            }
        } catch (Exception ex) {
            writeLogFunction.accept(ex.getMessage());
        }
        return null;
    }

    private Object[] getParas(Constructor<?> tempCon) {
        Object[] paras = new Object[tempCon.getParameterCount()];
        for (int i = 0; i < tempCon.getParameterCount(); i++) {
            paras[i] = getNewObject(tempCon.getParameterTypes()[i]);
        }
        return paras;
    }
}
