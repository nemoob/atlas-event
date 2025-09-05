package io.github.nemoob.event.annotation;

import java.lang.annotation.*;

/**
 * 事件订阅注解
 * 用于标识事件消费者方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventSubscribe {
    /**
     * 事件类型
     */
    String eventType() default "";
    
    /**
     * 事件类
     */
    Class<? extends io.github.nemoob.event.core.Event> eventClass() default io.github.nemoob.event.core.Event.class;
    
    /**
     * 是否异步处理
     */
    boolean async() default false;
    
    /**
     * 线程池名称
     */
    String threadPool() default "";
}