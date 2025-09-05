package io.github.nemoob.event.core;

import io.github.nemoob.event.annotation.EventSubscribe;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;

/**
 * 默认事件总线实现
 */
public class DefaultEventBus implements EventBus {
    
    // 事件类型与监听器方法的映射
    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    
    @Override
    public void publish(Event event) {
        String eventType = event.getType();
        List<EventListener> eventListeners = listeners.get(eventType);
        
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                if (listener.isAsync()) {
                    // 异步处理
                    new Thread(() -> invokeListener(listener, event)).start();
                } else {
                    // 同步处理
                    invokeListener(listener, event);
                }
            }
        }
    }
    
    @Override
    public void register(Object listener) {
        scanAndRegister(listener);
    }
    
    @Override
    public void unregister(Object listener) {
        // 移除监听器的实现
        // 在实际项目中需要实现
    }
    
    @Override
    public void scanAndRegister(Object listener) {
        Class<?> listenerClass = listener.getClass();
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(listenerClass);
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventSubscribe.class)) {
                EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
                String eventType = annotation.eventType();
                
                // 如果没有指定事件类型，则使用参数类型
                if (eventType.isEmpty() && method.getParameterTypes().length > 0) {
                    eventType = method.getParameterTypes()[0].getName();
                }
                
                if (!eventType.isEmpty()) {
                    EventListener eventListener = new EventListener(listener, method, annotation.async(), annotation.threadPool());
                    listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(eventListener);
                }
            }
        }
    }
    
    private void invokeListener(EventListener listener, Event event) {
        try {
            listener.getMethod().invoke(listener.getTarget(), event);
        } catch (Exception e) {
            // 在实际项目中应该使用日志记录异常
            e.printStackTrace();
        }
    }
    
    /**
     * 事件监听器内部类
     */
    private static class EventListener {
        private final Object target;
        private final Method method;
        private final boolean async;
        private final String threadPool;
        
        public EventListener(Object target, Method method, boolean async, String threadPool) {
            this.target = target;
            this.method = method;
            this.async = async;
            this.threadPool = threadPool;
        }
        
        public Object getTarget() {
            return target;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public boolean isAsync() {
            return async;
        }
        
        public String getThreadPool() {
            return threadPool;
        }
    }
}