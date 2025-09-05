package io.github.nemoob.event.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 事件总线工厂类
 * 用于创建和获取各种类型的事件总线实例
 */
public class EventBusFactory {
    
    private static EventBus defaultEventBus;
    private static EventBus asyncEventBus;
    private static EventBus orderedEventBus;
    private static EventBus adaptiveThreadPoolEventBus;
    private static EventBus multiThreadPoolEventBus;
    
    /**
     * 获取默认的同步事件总线实例（单例）
     */
    public static synchronized EventBus getDefaultEventBus() {
        if (defaultEventBus == null) {
            defaultEventBus = new DefaultEventBus();
        }
        return defaultEventBus;
    }
    
    /**
     * 获取异步事件总线实例（单例）
     */
    public static synchronized EventBus getAsyncEventBus() {
        if (asyncEventBus == null) {
            // 使用处理器核心数作为线程池大小
            int threadPoolSize = Runtime.getRuntime().availableProcessors();
            asyncEventBus = new AsyncEventBus(threadPoolSize);
        }
        return asyncEventBus;
    }
    
    /**
     * 获取有序事件总线实例（单例）
     */
    public static synchronized EventBus getOrderedEventBus() {
        if (orderedEventBus == null) {
            orderedEventBus = new OrderedEventBus(getDefaultEventBus());
        }
        return orderedEventBus;
    }
    
    /**
     * 获取自适应线程池事件总线实例（单例）
     */
    public static synchronized EventBus getAdaptiveThreadPoolEventBus() {
        if (adaptiveThreadPoolEventBus == null) {
            int corePoolSize = Runtime.getRuntime().availableProcessors();
            int maxPoolSize = corePoolSize * 2;
            adaptiveThreadPoolEventBus = new AdaptiveThreadPoolEventBus(
                getDefaultEventBus(), corePoolSize, maxPoolSize, 0.7, 30);
        }
        return adaptiveThreadPoolEventBus;
    }
    
    /**
     * 获取多线程池事件总线实例（单例）
     */
    public static synchronized EventBus getMultiThreadPoolEventBus() {
        if (multiThreadPoolEventBus == null) {
            int defaultThreadPoolSize = Runtime.getRuntime().availableProcessors();
            multiThreadPoolEventBus = new MultiThreadPoolEventBus(getDefaultEventBus(), defaultThreadPoolSize);
        }
        return multiThreadPoolEventBus;
    }
    
    /**
     * 创建一个新的同步事件总线
     */
    public static EventBus createSyncEventBus() {
        return new DefaultEventBus();
    }
    
    /**
     * 创建一个新的异步事件总线，使用指定大小的线程池
     */
    public static EventBus createAsyncEventBus(int threadPoolSize) {
        return new AsyncEventBus(threadPoolSize);
    }
    
    /**
     * 创建一个新的异步事件总线，使用指定的线程池
     */
    public static EventBus createAsyncEventBus(ExecutorService executorService) {
        return new AsyncEventBus(executorService);
    }
    
    /**
     * 创建一个新的有序事件总线
     */
    public static EventBus createOrderedEventBus(EventBus delegate) {
        return new OrderedEventBus(delegate);
    }
    
    /**
     * 创建一个新的自适应线程池事件总线
     */
    public static EventBus createAdaptiveThreadPoolEventBus(
            EventBus delegate, int corePoolSize, int maxPoolSize, 
            double targetUtilization, int monitorIntervalSeconds) {
        return new AdaptiveThreadPoolEventBus(delegate, corePoolSize, maxPoolSize, 
                                           targetUtilization, monitorIntervalSeconds);
    }
    
    /**
     * 创建一个新的多线程池事件总线
     */
    public static EventBus createMultiThreadPoolEventBus(EventBus delegate, int defaultThreadPoolSize) {
        return new MultiThreadPoolEventBus(delegate, defaultThreadPoolSize);
    }
    
    /**
     * 创建一个新的Kafka分布式事件总线
     */
    public static EventBus createKafkaDistributedEventBus(
            EventBus localEventBus, String bootstrapServers, String topic, String groupId) {
        return new KafkaDistributedEventBus(localEventBus, bootstrapServers, topic, groupId);
    }
    
    /**
     * 关闭所有单例事件总线实例
     */
    public static synchronized void shutdownAll() {
        if (asyncEventBus instanceof AsyncEventBus) {
            ((AsyncEventBus) asyncEventBus).shutdown();
        }
        
        if (orderedEventBus instanceof OrderedEventBus) {
            ((OrderedEventBus) orderedEventBus).shutdown();
        }
        
        if (adaptiveThreadPoolEventBus instanceof AdaptiveThreadPoolEventBus) {
            ((AdaptiveThreadPoolEventBus) adaptiveThreadPoolEventBus).shutdown();
        }
        
        if (multiThreadPoolEventBus instanceof MultiThreadPoolEventBus) {
            ((MultiThreadPoolEventBus) multiThreadPoolEventBus).shutdown();
        }
        
        defaultEventBus = null;
        asyncEventBus = null;
        orderedEventBus = null;
        adaptiveThreadPoolEventBus = null;
        multiThreadPoolEventBus = null;
    }
}