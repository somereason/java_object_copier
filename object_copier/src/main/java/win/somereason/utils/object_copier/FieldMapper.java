package win.somereason.utils.object_copier;

import win.somereason.utils.object_copier.entity.FieldType;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * 映射类中的字段
 * on 2017/4/21.
 */
public class FieldMapper {
    public Field sourceField;
    public Field targetField;

    public FieldType sourceType = FieldType.GENERAL_OBJECT;
    public FieldType targetType = FieldType.GENERAL_OBJECT;

    /**
     * 特殊映射规则,如,有时候要把long转化为LocalDateTime,就可以给这里设置一个函数,转换的时候会自动执行这个函数
     */
    public Function mappingRule;

    public boolean isFieldTypeEqual = false;

    @Override
    public String toString() {
        return String.format("%s(%s) -> %s(%s)",
                sourceField.getType().getName(), sourceType.toString(),
                targetField.getType().getName(), targetType.toString());
    }
}
