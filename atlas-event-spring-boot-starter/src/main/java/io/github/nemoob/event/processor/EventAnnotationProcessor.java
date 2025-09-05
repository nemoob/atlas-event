package io.github.nemoob.event.processor;

import io.github.nemoob.event.annotation.EventPublish;
import io.github.nemoob.event.annotation.EventSubscribe;
import io.github.nemoob.event.core.EventBus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 事件注解处理器
 * 扫描并注册带有@EventSubscribe和@EventPublish注解的组件
 */
@Component
public class EventAnnotationProcessor implements BeanPostProcessor {
    
    private final EventBus eventBus;
    
    public EventAnnotationProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 检查是否包含@EventSubscribe注解的方法
        if (hasEventSubscribeAnnotation(bean)) {
            eventBus.register(bean);
        }
        
        // 检查是否包含@EventPublish注解
        if (bean.getClass().isAnnotationPresent(EventPublish.class)) {
            EventPublish eventPublish = bean.getClass().getAnnotation(EventPublish.class);
            if (eventPublish.enable()) {
                // 可以在这里添加发布者的特殊处理逻辑
            }
        }
        
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    private boolean hasEventSubscribeAnnotation(Object bean) {
        Class<?> clazz = bean.getClass();
        while (clazz != null && clazz != Object.class) {
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(EventSubscribe.class)) {
                    return true;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}