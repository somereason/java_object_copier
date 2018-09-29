package win.somereason.utils.object_copier;

import win.somereason.utils.object_copier.entity.CollectionCopyRule;
import win.somereason.utils.object_copier.entity.CopyConfig;
import win.somereason.utils.object_copier.entity.MappingException;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * 映射管理器,对外暴露,用于管理映射关系
 * <p>
 * 把一个类的属性映射到另一个类中.
 * 也可以用来做深拷贝
 */

public class MappingManager {

    MappingWorker mappingWorker;

    CopyConfig defaultCopyConfig;


    /**
     * 支持通过外部函数写log,从而将转换过程的错误信息输出
     */
    private Consumer<String> writeLogFunction;

    public MappingManager() {
        defaultCopyConfig = new CopyConfig();
        defaultCopyConfig.setCollectionCopyRule(CollectionCopyRule.JOIN);
        defaultCopyConfig.setCopyNull(false);

        mappingWorker = new MappingWorker();
        mappingWorker.writeLogFunction = this::writeLog;
    }

    public CopyConfig getDefaultCopyConfig() {
        return defaultCopyConfig;
    }

    public void setDefaultCopyConfig(CopyConfig defaultCopyConfig) {
        this.defaultCopyConfig = defaultCopyConfig;
    }

    public Consumer<String> getWriteLogFunction() {
        return this.writeLogFunction;
    }

    public void setWriteLogFunction(Consumer<String> writeLogFunction) {
        this.writeLogFunction = writeLogFunction;
    }

    private void writeLog(String message) {
        if (this.writeLogFunction == null) {
            System.out.println(message);
        } else {
            this.writeLogFunction.accept(message);
        }
    }


    public ClassMapper registerMapper(Class typeS, Class typeD) throws MappingException {
        if (typeS.isPrimitive())
            throw new MappingException("不能映射基本类型");
        if (Map.class.isAssignableFrom(typeS) || Collection.class.isAssignableFrom(typeS))
            throw new MappingException("不能映射集合和map类型");

        return mappingWorker.registerMapper(typeS, typeD);
    }

    public <T> void addCompareRule(Class<T> type, BiPredicate<T, T> rule) {
        mappingWorker.addTargetCompareRule(type, rule);
    }

    public <T, K> List<T> map(List<K> entity, Class<T> toType) {
        List<T> list = new ArrayList<>();
        for (K k : entity) {
            list.add(map(k, toType));
        }
        return list;
    }


    public <T, K> T mapTo(K source, T dest, CopyConfig copyConfig) {
        return mappingWorker.mapTo(source, dest, mergeConfig(copyConfig));
    }


    public <T, K> T mapTo(K source, T dest) {
        return mappingWorker.mapTo(source, dest, defaultCopyConfig);
    }

    public <T, K> T map(K entity, Class<T> toType) {
        return map(entity, toType, defaultCopyConfig);
    }

    public <T, K> T map(K entity, Class<T> toType, CopyConfig copyConfig) {
        T d;
        try {
            d = mappingWorker.getNewObject(toType);
        } catch (Exception ex) {
            writeLog(String.format("Get error while create new object %s : %s", toType.getName(), ex.toString()));
            return null;
        }
        d = mappingWorker.mapTo(entity, d, mergeConfig(copyConfig));
        return d;
    }

    private CopyConfig mergeConfig(CopyConfig copyConfig) {
        if (copyConfig == defaultCopyConfig)
            return copyConfig;

        if (copyConfig == null)
            return defaultCopyConfig;

        CopyConfig copyConfigCopy = copyConfig;//不要改变用户的设置,没准他还下次再用
        if (copyConfigCopy.getCopyNull() == null)
            copyConfigCopy.setCopyNull(defaultCopyConfig.getCopyNull());
        if (copyConfigCopy.getCollectionCopyRule() == null)
            copyConfigCopy.setCollectionCopyRule(defaultCopyConfig.getCollectionCopyRule());

        return copyConfigCopy;
    }


}