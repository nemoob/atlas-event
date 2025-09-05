package io.github.nemoob.event.annotation;

import java.lang.annotation.*;

/**
 * 事件发布注解
 * 用于标识事件发布者类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventPublish {
    /**
     * 是否启用事件发布
     */
    boolean enable() default true;
    
    /**
     * 事件类型数组
     */
    String[] eventTypes() default {};
    
    /**
     * 是否持久化
     */
    boolean persist() default false;
    
    /**
     * 持久化过滤器
     */
    String persistFilter() default "";
}