package io.github.nemoob.event.core;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步事件总线实现
 * 使用线程池异步处理事件，避免阻塞发布者线程
 */
public class AsyncEventBus extends DefaultEventBus {
    
    private final ExecutorService executorService;
    
    /**
     * 创建一个异步事件总线，使用固定大小的线程池
     * 
     * @param threadPoolSize 线程池大小
     */
    public AsyncEventBus(int threadPoolSize) {
        this(Executors.newFixedThreadPool(threadPoolSize, new EventThreadFactory()));
    }
    
    /**
     * 创建一个异步事件总线，使用指定的线程池
     * 
     * @param executorService 线程池
     */
    public AsyncEventBus(ExecutorService executorService) {
        super();
        this.executorService = executorService;
    }
    
    @Override
    public void publish(Event event) {
        // 将事件发布任务提交到线程池中执行
        executorService.submit(() -> super.publish(event));
    }
    
    /**
     * 关闭线程池
     * 应在应用程序关闭时调用此方法，以释放资源
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * 立即关闭线程池
     * 尝试停止所有正在执行的任务，并不再处理等待队列中的任务
     * 
     * @return 未执行的任务列表
     */
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }
    
    /**
     * 事件处理线程工厂
     */
    private static class EventThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        EventThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "event-bus-pool-" + poolNumber.getAndIncrement() + "-thread-";
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