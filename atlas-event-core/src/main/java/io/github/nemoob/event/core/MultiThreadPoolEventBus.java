package io.github.nemoob.event.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程池事件总线
 * 为不同事件类型使用不同的线程池，提高事件处理的并发性和隔离性
 */
public class MultiThreadPoolEventBus implements EventBus {

    private final EventBus delegate;
    private final Map<String, ExecutorService> eventTypeToThreadPool = new ConcurrentHashMap<>();
    private final ExecutorService defaultThreadPool;
    private final Set<String> highPriorityEventTypes = ConcurrentHashMap.newKeySet();
    private final Set<String> lowPriorityEventTypes = ConcurrentHashMap.newKeySet();
    
    /**
     * 创建多线程池事件总线
     *
     * @param delegate 委托的事件总线，用于实际处理事件
     * @param defaultThreadPoolSize 默认线程池大小
     */
    public MultiThreadPoolEventBus(EventBus delegate, int defaultThreadPoolSize) {
        this.delegate = delegate;
        this.defaultThreadPool = Executors.newFixedThreadPool(
            defaultThreadPoolSize, 
            r -> {
                Thread t = new Thread(r, "default-event-processor");
                t.setDaemon(true);
                return t;
            }
        );
    }
    
    /**
     * 为指定事件类型配置专用线程池
     *
     * @param eventType 事件类型
     * @param threadPoolSize 线程池大小
     * @return 当前实例，用于链式调用
     */
    public MultiThreadPoolEventBus configureThreadPool(String eventType, int threadPoolSize) {
        ExecutorService threadPool = Executors.newFixedThreadPool(
            threadPoolSize,
            r -> {
                Thread t = new Thread(r, eventType + "-processor");
                t.setDaemon(true);
                return t;
            }
        );
        eventTypeToThreadPool.put(eventType, threadPool);
        return this;
    }
    
    /**
     * 将事件类型标记为高优先级
     *
     * @param eventType 事件类型
     * @return 当前实例，用于链式调用
     */
    public MultiThreadPoolEventBus markAsHighPriority(String eventType) {
        highPriorityEventTypes.add(eventType);
        lowPriorityEventTypes.remove(eventType); // 确保不会同时在两个集合中
        return this;
    }
    
    /**
     * 将事件类型标记为低优先级
     *
     * @param eventType 事件类型
     * @return 当前实例，用于链式调用
     */
    public MultiThreadPoolEventBus markAsLowPriority(String eventType) {
        lowPriorityEventTypes.add(eventType);
        highPriorityEventTypes.remove(eventType); // 确保不会同时在两个集合中
        return this;
    }
    
    @Override
    public void publish(Event event) {
        String eventType = event.getType();
        ExecutorService threadPool = eventTypeToThreadPool.getOrDefault(eventType, defaultThreadPool);
        
        // 根据事件优先级调整线程优先级
        if (highPriorityEventTypes.contains(eventType)) {
            threadPool.submit(() -> {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                delegate.publish(event);
            });
        } else if (lowPriorityEventTypes.contains(eventType)) {
            threadPool.submit(() -> {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                delegate.publish(event);
            });
        } else {
            threadPool.submit(() -> delegate.publish(event));
        }
    }
    
    @Override
    public void register(Object listener) {
        delegate.register(listener);
    }
    
    @Override
    public void unregister(Object listener) {
        delegate.unregister(listener);
    }
    
    @Override
    public void scanAndRegister(Object listener) {
        delegate.scanAndRegister(listener);
    }
    
    /**
     * 关闭多线程池事件总线
     */
    public void shutdown() {
        defaultThreadPool.shutdown();
        for (ExecutorService threadPool : eventTypeToThreadPool.values()) {
            threadPool.shutdown();
        }
    }
}