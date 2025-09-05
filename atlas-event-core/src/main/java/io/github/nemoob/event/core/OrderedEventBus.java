package io.github.nemoob.event.core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 有序事件总线实现
 * 确保具有相同orderKey的事件按照发布顺序依次处理
 */
public class OrderedEventBus implements EventBus {
    
    private final EventBus delegate;
    private final Map<String, BlockingQueue<Event>> orderKeyToQueue = new ConcurrentHashMap<>();
    private final Map<String, Thread> orderKeyToThread = new ConcurrentHashMap<>();
    private final ThreadFactory threadFactory = new OrderedEventThreadFactory();
    private volatile boolean running = true;
    
    /**
     * 创建一个有序事件总线
     * 
     * @param delegate 委托的事件总线，用于实际处理事件
     */
    public OrderedEventBus(EventBus delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void publish(Event event) {
        // 检查事件是否需要顺序保证
        if (event instanceof OrderedEvent) {
            String orderKey = ((OrderedEvent) event).getOrderKey();
            publishOrdered(event, orderKey);
        } else {
            // 非有序事件直接委托给底层事件总线处理
            delegate.publish(event);
        }
    }
    
    /**
     * 发布有序事件
     * 
     * @param event 事件对象
     * @param orderKey 顺序键
     */
    private void publishOrdered(Event event, String orderKey) {
        // 获取或创建该orderKey的队列
        BlockingQueue<Event> queue = orderKeyToQueue.computeIfAbsent(orderKey, k -> {
            BlockingQueue<Event> newQueue = new LinkedBlockingQueue<>();
            // 为每个orderKey创建一个专用线程处理队列
            Thread processor = threadFactory.newThread(() -> processQueue(newQueue, k));
            processor.start();
            orderKeyToThread.put(k, processor);
            return newQueue;
        });
        
        // 将事件添加到队列
        try {
            queue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while queueing ordered event", e);
        }
    }
    
    /**
     * 处理队列中的事件
     * 
     * @param queue 事件队列
     * @param orderKey 顺序键
     */
    private void processQueue(BlockingQueue<Event> queue, String orderKey) {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                Event event = queue.take();
                try {
                    // 按顺序处理事件
                    delegate.publish(event);
                } catch (Exception e) {
                    // 在实际项目中应该使用日志记录异常
                    System.err.println("Error processing ordered event: " + event.getId());
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 清理资源
            orderKeyToQueue.remove(orderKey);
            orderKeyToThread.remove(orderKey);
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
     * 关闭有序事件总线
     * 停止所有处理线程并清理资源
     */
    public void shutdown() {
        running = false;
        for (Thread thread : orderKeyToThread.values()) {
            thread.interrupt();
        }
        orderKeyToQueue.clear();
        orderKeyToThread.clear();
    }
    
    /**
     * 有序事件处理线程工厂
     */
    private static class OrderedEventThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        OrderedEventThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "ordered-event-processor-" + poolNumber.getAndIncrement() + "-";
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