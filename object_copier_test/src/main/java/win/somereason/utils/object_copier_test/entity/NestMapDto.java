package win.somereason.utils.object_copier_test.entity;

import lombok.Getter;
import lombok.Setter;
import win.somereason.utils.object_copier.entity.RegisterThisType;

import java.util.Map;

/**
 * 测试是否能识别list中嵌套的类,
 */
@Getter
@Setter
public class NestMapDto {
    //指定在字段arr2中,只注册第2个包含的类型,也就是SimpleDto
    @RegisterThisType(typeIndex = {1})
    protected Map<Integer, SimpleDto> m;
}
