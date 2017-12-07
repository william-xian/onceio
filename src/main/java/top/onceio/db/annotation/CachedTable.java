package top.onceio.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* 与时间相关的数据，最新一段时间的数据会被保存
* 如日志，评论等
*/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CachedTable {
	/**数据引用自哪个实体类*/
    Class<?> entity();
    /** 时间，数目 */
    String cnd() default "";
}
