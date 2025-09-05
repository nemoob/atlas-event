package io.github.nemoob.event.core;

/**
 * 事件总线接口
 */
public interface EventBus {
    /**
     * 发布事件
     */
    void publish(Event event);
    
    /**
     * 注册事件监听器
     */
    void register(Object listener);
    
    /**
     * 注销事件监听器
     */
    void unregister(Object listener);
    
    /**
     * 扫描并注册监听器
     */
    void scanAndRegister(Object listener);
}