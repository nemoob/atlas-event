package io.github.nemoob.event.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自适应线程池事件总线
 * 能够根据系统负载动态调整线程池大小，提高事件处理效率
 */
public class AdaptiveThreadPoolEventBus implements EventBus {

    private final EventBus delegate;
    private final ThreadPoolExecutor threadPool;
    private final ScheduledExecutorService monitorService;
    private final int minThreads;
    private final int maxThreads;
    private final double targetUtilization;
    private final long monitorInterval;
    
    /**
     * 创建自适应线程池事件总线
     *
     * @param delegate 委托的事件总线，用于实际处理事件
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param targetUtilization 目标线程利用率（0.0-1.0）
     * @param monitorIntervalSeconds 监控间隔（秒）
     */
    public AdaptiveThreadPoolEventBus(EventBus delegate, int corePoolSize, int maxPoolSize, 
                                     double targetUtilization, int monitorIntervalSeconds) {
        this.delegate = delegate;
        this.minThreads = corePoolSize;
        this.maxThreads = maxPoolSize;
        this.targetUtilization = targetUtilization;
        this.monitorInterval = TimeUnit.SECONDS.toMillis(monitorIntervalSeconds);
        
        // 创建自适应线程池
        this.threadPool = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new AdaptiveThreadFactory()
        );
        
        // 设置拒绝策略
        this.threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 启动监控线程
        this.monitorService = Executors.newSingleThreadScheduledExecutor();
        this.monitorService.scheduleAtFixedRate(
            this::adjustThreadPool,
            monitorInterval,
            monitorInterval,
            TimeUnit.MILLISECONDS
        );
    }
    
    @Override
    public void publish(Event event) {
        threadPool.execute(() -> delegate.publish(event));
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
     * 动态调整线程池大小
     */
    private void adjustThreadPool() {
        int activeThreads = threadPool.getActiveCount();
        int poolSize = threadPool.getPoolSize();
        int queueSize = threadPool.getQueue().size();
        
        // 计算当前线程利用率
        double utilization = poolSize > 0 ? (double) activeThreads / poolSize : 0;
        
        // 根据利用率和队列大小调整线程池
        if (utilization > targetUtilization && queueSize > 0 && poolSize < maxThreads) {
            // 高负载，增加线程数
            int newCoreSize = Math.min(poolSize + 2, maxThreads);
            threadPool.setCorePoolSize(newCoreSize);
            System.out.println("Increasing thread pool size to: " + newCoreSize);
        } else if (utilization < targetUtilization * 0.5 && poolSize > minThreads) {
            // 低负载，减少线程数
            int newCoreSize = Math.max(poolSize - 1, minThreads);
            threadPool.setCorePoolSize(newCoreSize);
            System.out.println("Decreasing thread pool size to: " + newCoreSize);
        }
    }
    
    /**
     * 关闭自适应线程池事件总线
     */
    public void shutdown() {
        monitorService.shutdown();
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            threadPool.shutdownNow();
        }
    }
    
    /**
     * 自适应线程工厂
     */
    private static class AdaptiveThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        AdaptiveThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "adaptive-event-processor-" + poolNumber.getAndIncrement() + "-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}