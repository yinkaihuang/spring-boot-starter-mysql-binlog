package cn.bucheng.mysql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author buchengyin
 * @create 2019/7/27 8:40
 * @describe
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnName {
    /**
     * sql映射的列名
     * @return
     */
    String sqlColumn();

    /**
     * java 类型对的字段名称
     * @return
     */
    String javaColumn() default "";
}
