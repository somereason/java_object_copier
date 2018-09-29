package win.somereason.utils.object_copier.entity;

import java.lang.annotation.*;

/**
 * Created by somereason on 2018/7/25.
 */
@Target( ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterThisType {
    int[] typeIndex() default {};
}
