# 第6章：高级特性与性能优化

在前面的章节中，我们已经构建了一个功能完整的事件框架，包括核心接口设计、基础事件总线实现、注解支持以及与Spring Boot的集成。本章将介绍一些高级特性和性能优化技术，使我们的事件框架更加强大、灵活和高效。

## 6.1 事件过滤器（Event Filter）

事件过滤器允许我们在事件分发前对事件进行筛选，决定是否将事件传递给特定的监听器。这对于实现条件性事件处理非常有用。

### 6.1.1 设计事件过滤器接口

```java
public interface EventFilter {
    /**
     * 判断事件是否应该被传递给监听器
     * @param event 要过滤的事件
     * @param listener 目标监听器
     * @return true表示事件应该被传递，false表示应该被过滤掉
     */
    boolean shouldDeliver(Event event, EventListener listener);
}
```

### 6.1.2 实现常用过滤器

#### 属性过滤器

```java
public class PropertyEventFilter implements EventFilter {
    private final String propertyName;
    private final Object propertyValue;
    
    public PropertyEventFilter(String propertyName, Object propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }
    
    @Override
    public boolean shouldDeliver(Event event, EventListener listener) {
        if (event instanceof GenericEvent) {
            GenericEvent genericEvent = (GenericEvent) event;
            Object value = genericEvent.getProperty(propertyName);
            return Objects.equals(value, propertyValue);
        }
        return true; // 非GenericEvent类型的事件默认通过
    }
}
```

#### 类型过滤器

```java
public class TypeEventFilter implements EventFilter {
    private final Class<?> targetType;
    
    public TypeEventFilter(Class<?> targetType) {
        this.targetType = targetType;
    }
    
    @Override
    public boolean shouldDeliver(Event event, EventListener listener) {
        return targetType.isInstance(event);
    }
}
```

### 6.1.3 在事件总线中集成过滤器

```java
public interface EventBus {
    // 现有方法...
    
    /**
     * 为特定监听器添加事件过滤器
     * @param listener 监听器
     * @param filter 过滤器
     */
    void addFilterForListener(EventListener listener, EventFilter filter);
    
    /**
     * 移除监听器的过滤器
     * @param listener 监听器
     * @param filter 过滤器
     */
    void removeFilterForListener(EventListener listener, EventFilter filter);
}
```

在`DefaultEventBus`中实现这些方法：

```java
public class DefaultEventBus implements EventBus {
    // 现有字段...
    private final Map<EventListener, List<EventFilter>> listenerFilters = new ConcurrentHashMap<>();
    
    // 现有方法...
    
    @Override
    public void addFilterForListener(EventListener listener, EventFilter filter) {
        listenerFilters.computeIfAbsent(listener, k -> new CopyOnWriteArrayList<>()).add(filter);
    }
    
    @Override
    public void removeFilterForListener(EventListener listener, EventFilter filter) {
        List<EventFilter> filters = listenerFilters.get(listener);
        if (filters != null) {
            filters.remove(filter);
        }
    }
    
    @Override
    protected void dispatchEventToListener(Event event, EventListener listener) {
        // 应用过滤器
        List<EventFilter> filters = listenerFilters.get(listener);
        if (filters != null) {
            for (EventFilter filter : filters) {
                if (!filter.shouldDeliver(event, listener)) {
                    return; // 如果任何过滤器返回false，则不传递事件
                }
            }
        }
        
        // 通过所有过滤器，执行监听器
        super.dispatchEventToListener(event, listener);
    }
}
```

### 6.1.4 使用示例

```java
// 创建过滤器
EventFilter importantFilter = new PropertyEventFilter("importance", "high");

// 注册监听器并添加过滤器
eventBus.register(new OrderEventListener());
eventBus.addFilterForListener(orderEventListener, importantFilter);

// 发布事件
OrderCreatedEvent event = new OrderCreatedEvent(orderId);
event.setProperty("importance", "low"); // 这个事件不会传递给带有importantFilter的监听器
eventBus.post(event);

OrderCreatedEvent importantEvent = new OrderCreatedEvent(vipOrderId);
importantEvent.setProperty("importance", "high"); // 这个事件会传递给带有importantFilter的监听器
eventBus.post(importantEvent);
```

## 6.2 事件拦截器（Event Interceptor）

事件拦截器允许我们在事件处理的不同阶段进行干预，例如在事件发布前、监听器执行前后以及事件处理完成后。这对于实现横切关注点（如日志记录、性能监控、事务管理等）非常有用。

### 6.2.1 设计拦截器接口

```java
public interface EventInterceptor {
    /**
     * 事件发布前调用
     * @param event 要发布的事件
     * @return true表示继续处理，false表示中断处理
     */
    default boolean beforePublish(Event event) {
        return true;
    }
    
    /**
     * 监听器执行前调用
     * @param event 事件
     * @param listener 即将执行的监听器
     * @return true表示继续执行监听器，false表示跳过此监听器
     */
    default boolean beforeInvoke(Event event, EventListener listener) {
        return true;
    }
    
    /**
     * 监听器执行后调用
     * @param event 事件
     * @param listener 已执行的监听器
     * @param exception 执行过程中抛出的异常，如果没有异常则为null
     */
    default void afterInvoke(Event event, EventListener listener, Exception exception) {
    }
    
    /**
     * 事件处理完成后调用（所有监听器都已执行）
     * @param event 已处理的事件
     */
    default void afterComplete(Event event) {
    }
}
```

### 6.2.2 实现常用拦截器

#### 日志拦截器

```java
public class LoggingEventInterceptor implements EventInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingEventInterceptor.class);
    
    @Override
    public boolean beforePublish(Event event) {
        logger.info("Publishing event: {}", event);
        return true;
    }
    
    @Override
    public boolean beforeInvoke(Event event, EventListener listener) {
        logger.debug("Invoking listener {} for event {}", listener.getClass().getSimpleName(), event);
        return true;
    }
    
    @Override
    public void afterInvoke(Event event, EventListener listener, Exception exception) {
        if (exception != null) {
            logger.error("Error while handling event {} in listener {}: {}", 
                    event, listener.getClass().getSimpleName(), exception.getMessage(), exception);
        } else {
            logger.debug("Successfully handled event {} in listener {}", 
                    event, listener.getClass().getSimpleName());
        }
    }
    
    @Override
    public void afterComplete(Event event) {
        logger.info("Completed processing of event: {}", event);
    }
}
```

#### 性能监控拦截器

```java
public class PerformanceEventInterceptor implements EventInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceEventInterceptor.class);
    private final ThreadLocal<Map<EventListener, Long>> startTimes = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Long> eventStartTime = ThreadLocal.withInitial(() -> 0L);
    
    @Override
    public boolean beforePublish(Event event) {
        eventStartTime.set(System.currentTimeMillis());
        return true;
    }
    
    @Override
    public boolean beforeInvoke(Event event, EventListener listener) {
        startTimes.get().put(listener, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterInvoke(Event event, EventListener listener, Exception exception) {
        long startTime = startTimes.get().remove(listener);
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > 100) { // 设置阈值，只记录执行时间较长的监听器
            logger.warn("Slow event handling detected: Listener {} took {}ms to process event {}", 
                    listener.getClass().getSimpleName(), duration, event);
        }
    }
    
    @Override
    public void afterComplete(Event event) {
        long totalDuration = System.currentTimeMillis() - eventStartTime.get();
        logger.info("Total event processing time for {}: {}ms", event, totalDuration);
        
        // 清理ThreadLocal
        startTimes.remove();
        eventStartTime.remove();
    }
}
```

### 6.2.3 在事件总线中集成拦截器

```java
public interface EventBus {
    // 现有方法...
    
    /**
     * 添加事件拦截器
     * @param interceptor 拦截器
     */
    void addInterceptor(EventInterceptor interceptor);
    
    /**
     * 移除事件拦截器
     * @param interceptor 拦截器
     */
    void removeInterceptor(EventInterceptor interceptor);
}
```

在`DefaultEventBus`中实现这些方法：

```java
public class DefaultEventBus implements EventBus {
    // 现有字段...
    private final List<EventInterceptor> interceptors = new CopyOnWriteArrayList<>();
    
    // 现有方法...
    
    @Override
    public void addInterceptor(EventInterceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    @Override
    public void removeInterceptor(EventInterceptor interceptor) {
        interceptors.remove(interceptor);
    }
    
    @Override
    public void post(Event event) {
        // 调用拦截器的beforePublish方法
        for (EventInterceptor interceptor : interceptors) {
            if (!interceptor.beforePublish(event)) {
                return; // 如果任何拦截器返回false，则中断处理
            }
        }
        
        // 执行事件分发
        try {
            doPost(event);
        } finally {
            // 调用拦截器的afterComplete方法
            for (EventInterceptor interceptor : interceptors) {
                try {
                    interceptor.afterComplete(event);
                } catch (Exception e) {
                    logger.error("Error in interceptor afterComplete: {}", e.getMessage(), e);
                }
            }
        }
    }
    
    @Override
    protected void dispatchEventToListener(Event event, EventListener listener) {
        // 调用拦截器的beforeInvoke方法
        for (EventInterceptor interceptor : interceptors) {
            if (!interceptor.beforeInvoke(event, listener)) {
                return; // 如果任何拦截器返回false，则跳过此监听器
            }
        }
        
        // 执行监听器
        Exception exception = null;
        try {
            listener.onEvent(event);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 调用拦截器的afterInvoke方法
            for (EventInterceptor interceptor : interceptors) {
                try {
                    interceptor.afterInvoke(event, listener, exception);
                } catch (Exception e) {
                    logger.error("Error in interceptor afterInvoke: {}", e.getMessage(), e);
                }
            }
        }
    }
}
```

### 6.2.4 使用示例

```java
// 创建并添加拦截器
EventInterceptor loggingInterceptor = new LoggingEventInterceptor();
EventInterceptor performanceInterceptor = new PerformanceEventInterceptor();

eventBus.addInterceptor(loggingInterceptor);
eventBus.addInterceptor(performanceInterceptor);

// 发布事件
OrderCreatedEvent event = new OrderCreatedEvent(orderId);
eventBus.post(event); // 拦截器会在事件处理的各个阶段被调用
```

## 6.3 事件传播控制（Event Propagation）

在某些情况下，我们可能希望在事件处理过程中控制事件的传播，例如停止事件继续传递给其他监听器。这类似于DOM事件中的事件冒泡和捕获机制。

### 6.3.1 设计可控制传播的事件

```java
public interface PropagatingEvent extends Event {
    /**
     * 检查事件是否继续传播
     * @return true表示继续传播，false表示停止传播
     */
    boolean isPropagating();
    
    /**
     * 停止事件传播
     */
    void stopPropagation();
}
```

实现基础抽象类：

```java
public abstract class AbstractPropagatingEvent extends AbstractEvent implements PropagatingEvent {
    private boolean propagating = true;
    
    public AbstractPropagatingEvent(Object source) {
        super(source);
    }
    
    @Override
    public boolean isPropagating() {
        return propagating;
    }
    
    @Override
    public void stopPropagation() {
        this.propagating = false;
    }
}
```

### 6.3.2 在事件总线中实现传播控制

```java
public class DefaultEventBus implements EventBus {
    // 现有方法...
    
    @Override
    protected void doPost(Event event) {
        Class<?> eventType = event.getClass();
        Set<Class<?>> eventTypes = eventTypeResolver.resolveEventTypes(eventType);
        
        for (Class<?> type : eventTypes) {
            List<EventListener> listeners = listenerRegistry.getListeners(type);
            if (listeners != null) {
                for (EventListener listener : listeners) {
                    dispatchEventToListener(event, listener);
                    
                    // 检查事件传播状态
                    if (event instanceof PropagatingEvent && !((PropagatingEvent) event).isPropagating()) {
                        return; // 如果事件停止传播，则中断处理
                    }
                }
            }
        }
    }
}
```

### 6.3.3 使用示例

```java
// 定义支持传播控制的事件
public class OrderProcessingEvent extends AbstractPropagatingEvent {
    private final String orderId;
    private final OrderStatus status;
    
    public OrderProcessingEvent(Object source, String orderId, OrderStatus status) {
        super(source);
        this.orderId = orderId;
        this.status = status;
    }
    
    // getter方法...
}

// 在监听器中控制传播
public class HighPriorityOrderListener implements EventListener {
    @Override
    public void onEvent(Event event) {
        if (event instanceof OrderProcessingEvent) {
            OrderProcessingEvent orderEvent = (OrderProcessingEvent) event;
            if (isVipOrder(orderEvent.getOrderId())) {
                // 处理VIP订单
                processVipOrder(orderEvent);
                
                // 停止事件传播，不让其他监听器处理此VIP订单
                orderEvent.stopPropagation();
            }
        }
    }
    
    // 其他方法...
}
```

## 6.4 事件优先级（Event Priority）

在某些场景中，我们可能希望控制监听器的执行顺序，确保某些监听器先于其他监听器执行。

### 6.4.1 定义优先级注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Priority {
    /**
     * 优先级值，数值越小优先级越高
     */
    int value() default 0;
}
```

### 6.4.2 在注解处理器中处理优先级

```java
public class EventAnnotationProcessor {
    // 现有方法...
    
    private int getMethodPriority(Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        
        // 检查类级别的优先级
        priority = method.getDeclaringClass().getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        
        return 0; // 默认优先级
    }
    
    public void process(Object bean, EventBus eventBus) {
        Class<?> clazz = bean.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        List<PrioritizedMethod> prioritizedMethods = new ArrayList<>();
        
        for (Method method : methods) {
            EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
            if (annotation != null) {
                int priority = getMethodPriority(method);
                prioritizedMethods.add(new PrioritizedMethod(method, priority));
            }
        }
        
        // 按优先级排序
        prioritizedMethods.sort(Comparator.comparingInt(PrioritizedMethod::getPriority));
        
        // 注册监听器
        for (PrioritizedMethod pm : prioritizedMethods) {
            Method method = pm.getMethod();
            EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
            registerListener(bean, method, annotation, eventBus);
        }
    }
    
    private static class PrioritizedMethod {
        private final Method method;
        private final int priority;
        
        public PrioritizedMethod(Method method, int priority) {
            this.method = method;
            this.priority = priority;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public int getPriority() {
            return priority;
        }
    }
}
```

### 6.4.3 在监听器注册表中维护优先级

```java
public class ListenerRegistry {
    private final Map<Class<?>, List<PrioritizedEventListener>> listenerMap = new ConcurrentHashMap<>();
    
    public void register(Class<?> eventType, EventListener listener, int priority) {
        listenerMap.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new PrioritizedEventListener(listener, priority));
        
        // 按优先级排序
        sortListeners(eventType);
    }
    
    private void sortListeners(Class<?> eventType) {
        List<PrioritizedEventListener> listeners = listenerMap.get(eventType);
        if (listeners instanceof ArrayList) {
            listeners.sort(Comparator.comparingInt(PrioritizedEventListener::getPriority));
        }
    }
    
    public List<EventListener> getListeners(Class<?> eventType) {
        List<PrioritizedEventListener> prioritizedListeners = listenerMap.get(eventType);
        if (prioritizedListeners == null) {
            return Collections.emptyList();
        }
        
        return prioritizedListeners.stream()
                .map(PrioritizedEventListener::getListener)
                .collect(Collectors.toList());
    }
    
    private static class PrioritizedEventListener {
        private final EventListener listener;
        private final int priority;
        
        public PrioritizedEventListener(EventListener listener, int priority) {
            this.listener = listener;
            this.priority = priority;
        }
        
        public EventListener getListener() {
            return listener;
        }
        
        public int getPriority() {
            return priority;
        }
    }
}
```

### 6.4.4 使用示例

```java
// 在类级别设置优先级
@Priority(1)
public class HighPriorityListener {
    @EventSubscribe
    public void handleOrder(OrderCreatedEvent event) {
        // 此监听器将先于其他监听器执行
        System.out.println("High priority listener executed first");
    }
}

// 在方法级别设置优先级
public class OrderListener {
    @EventSubscribe
    @Priority(2)
    public void handleOrder(OrderCreatedEvent event) {
        // 此方法将在优先级为1的监听器之后执行
        System.out.println("Medium priority listener executed second");
    }
    
    @EventSubscribe
    @Priority(3)
    public void logOrder(OrderCreatedEvent event) {
        // 此方法将在优先级为2的监听器之后执行
        System.out.println("Low priority listener executed last");
    }
}
```

## 6.5 性能优化

### 6.5.1 缓存机制

为了提高事件框架的性能，我们可以在多个层面实现缓存：

#### 事件类型解析缓存

```java
public class CachingEventTypeResolver implements EventTypeResolver {
    private final Map<Class<?>, Set<Class<?>>> eventTypeCache = new ConcurrentHashMap<>();
    
    @Override
    public Set<Class<?>> resolveEventTypes(Class<?> eventType) {
        return eventTypeCache.computeIfAbsent(eventType, this::doResolveEventTypes);
    }
    
    private Set<Class<?>> doResolveEventTypes(Class<?> eventType) {
        Set<Class<?>> types = new LinkedHashSet<>();
        collectEventTypes(eventType, types);
        return types;
    }
    
    private void collectEventTypes(Class<?> type, Set<Class<?>> types) {
        // 现有的类型解析逻辑...
    }
}
```

#### 注解方法缓存

```java
public class CachingEventAnnotationProcessor extends EventAnnotationProcessor {
    private final Map<Class<?>, List<AnnotatedMethod>> annotatedMethodsCache = new ConcurrentHashMap<>();
    
    @Override
    public void process(Object bean, EventBus eventBus) {
        Class<?> clazz = bean.getClass();
        List<AnnotatedMethod> methods = annotatedMethodsCache.computeIfAbsent(clazz, this::findAnnotatedMethods);
        
        for (AnnotatedMethod method : methods) {
            registerListener(bean, method.getMethod(), method.getAnnotation(), eventBus);
        }
    }
    
    private List<AnnotatedMethod> findAnnotatedMethods(Class<?> clazz) {
        List<AnnotatedMethod> result = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
            if (annotation != null) {
                result.add(new AnnotatedMethod(method, annotation));
            }
        }
        
        return result;
    }
    
    private static class AnnotatedMethod {
        private final Method method;
        private final EventSubscribe annotation;
        
        public AnnotatedMethod(Method method, EventSubscribe annotation) {
            this.method = method;
            this.annotation = annotation;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public EventSubscribe getAnnotation() {
            return annotation;
        }
    }
}
```

### 6.5.2 批量事件处理

在某些场景中，我们可能需要一次性处理多个事件，批量事件处理可以减少系统开销：

```java
public interface EventBus {
    // 现有方法...
    
    /**
     * 批量发布事件
     * @param events 要发布的事件集合
     */
    void postAll(Collection<? extends Event> events);
}
```

实现：

```java
public class DefaultEventBus implements EventBus {
    // 现有方法...
    
    @Override
    public void postAll(Collection<? extends Event> events) {
        for (Event event : events) {
            post(event);
        }
    }
}

public class AsyncEventBus extends DefaultEventBus {
    // 现有方法...
    
    @Override
    public void postAll(Collection<? extends Event> events) {
        // 批量提交到线程池，减少线程调度开销
        executorService.submit(() -> {
            for (Event event : events) {
                try {
                    doPost(event);
                } catch (Exception e) {
                    logger.error("Error processing event in batch: {}", e.getMessage(), e);
                }
            }
        });
    }
}
```

### 6.5.3 线程池优化

对于异步事件总线，线程池的配置对性能有重要影响：

```java
public class OptimizedAsyncEventBus extends AsyncEventBus {
    public OptimizedAsyncEventBus(int corePoolSize, int maxPoolSize, long keepAliveTime, 
                                 TimeUnit unit, int queueCapacity) {
        super(new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                unit,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactoryBuilder()
                        .setNameFormat("event-bus-worker-%d")
                        .setDaemon(true)
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 避免任务被拒绝
        ));
    }
    
    // 根据不同事件类型使用不同的线程池
    public static class MultiThreadPoolEventBus implements EventBus {
        private final Map<Class<?>, EventBus> eventBusMap = new ConcurrentHashMap<>();
        private final EventBus defaultEventBus;
        
        public MultiThreadPoolEventBus(EventBus defaultEventBus) {
            this.defaultEventBus = defaultEventBus;
        }
        
        public void registerEventType(Class<?> eventType, EventBus eventBus) {
            eventBusMap.put(eventType, eventBus);
        }
        
        @Override
        public void post(Event event) {
            EventBus eventBus = eventBusMap.getOrDefault(event.getClass(), defaultEventBus);
            eventBus.post(event);
        }
        
        // 实现其他EventBus接口方法...
    }
}
```

### 6.5.4 监控与指标收集

为了持续优化事件框架的性能，我们可以收集关键指标：

```java
public class MetricsEventInterceptor implements EventInterceptor {
    private final Counter totalEventsCounter = new Counter();
    private final Map<Class<?>, Counter> eventTypeCounters = new ConcurrentHashMap<>();
    private final Map<Class<?>, Timer> eventProcessingTimers = new ConcurrentHashMap<>();
    private final ThreadLocal<Long> startTimes = ThreadLocal.withInitial(() -> 0L);
    
    @Override
    public boolean beforePublish(Event event) {
        totalEventsCounter.increment();
        eventTypeCounters.computeIfAbsent(event.getClass(), k -> new Counter()).increment();
        startTimes.set(System.nanoTime());
        return true;
    }
    
    @Override
    public void afterComplete(Event event) {
        long duration = System.nanoTime() - startTimes.get();
        eventProcessingTimers.computeIfAbsent(event.getClass(), k -> new Timer()).record(duration);
        startTimes.remove();
    }
    
    // 指标收集类
    private static class Counter {
        private final AtomicLong count = new AtomicLong();
        
        public void increment() {
            count.incrementAndGet();
        }
        
        public long getCount() {
            return count.get();
        }
    }
    
    private static class Timer {
        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong count = new AtomicLong();
        
        public void record(long durationNanos) {
            totalTime.addAndGet(durationNanos);
            count.incrementAndGet();
        }
        
        public double getAverageTimeMillis() {
            long cnt = count.get();
            return cnt > 0 ? (totalTime.get() / 1_000_000.0) / cnt : 0;
        }
    }
    
    // 获取指标的方法
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalEvents", totalEventsCounter.getCount());
        
        Map<String, Long> eventCounts = new HashMap<>();
        for (Map.Entry<Class<?>, Counter> entry : eventTypeCounters.entrySet()) {
            eventCounts.put(entry.getKey().getSimpleName(), entry.getValue().getCount());
        }
        metrics.put("eventCounts", eventCounts);
        
        Map<String, Double> avgProcessingTimes = new HashMap<>();
        for (Map.Entry<Class<?>, Timer> entry : eventProcessingTimers.entrySet()) {
            avgProcessingTimes.put(entry.getKey().getSimpleName(), entry.getValue().getAverageTimeMillis());
        }
        metrics.put("avgProcessingTimeMs", avgProcessingTimes);
        
        return metrics;
    }
}
```

## 6.6 事件框架的高级应用场景

### 6.6.1 分布式事件处理

在微服务架构中，我们可能需要跨服务发布和处理事件。以下是一个简单的分布式事件总线实现思路：

```java
public class DistributedEventBus implements EventBus {
    private final EventBus localEventBus;
    private final MessageBroker messageBroker; // 消息中间件客户端，如Kafka、RabbitMQ等
    
    public DistributedEventBus(EventBus localEventBus, MessageBroker messageBroker) {
        this.localEventBus = localEventBus;
        this.messageBroker = messageBroker;
        
        // 订阅来自消息中间件的事件
        messageBroker.subscribe(this::handleRemoteEvent);
    }
    
    @Override
    public void post(Event event) {
        // 本地处理
        localEventBus.post(event);
        
        // 分布式处理
        if (event instanceof DistributableEvent) {
            messageBroker.publish(serializeEvent(event));
        }
    }
    
    private void handleRemoteEvent(String serializedEvent) {
        Event event = deserializeEvent(serializedEvent);
        if (event != null) {
            // 只在本地处理，避免循环发布
            localEventBus.post(event);
        }
    }
    
    private String serializeEvent(Event event) {
        // 使用JSON或其他序列化方式
        return JsonUtils.serialize(event);
    }
    
    private Event deserializeEvent(String serializedEvent) {
        // 反序列化事件
        return JsonUtils.deserialize(serializedEvent, Event.class);
    }
    
    // 实现其他EventBus接口方法...
}

// 标记可分布式传播的事件
public interface DistributableEvent extends Event {
    // 标记接口
}
```

### 6.6.2 事件溯源（Event Sourcing）

事件溯源是一种存储系统状态变化的设计模式，它将所有变化记录为事件序列：

```java
public class EventSourcingBus implements EventBus {
    private final EventBus delegateEventBus;
    private final EventStore eventStore;
    
    public EventSourcingBus(EventBus delegateEventBus, EventStore eventStore) {
        this.delegateEventBus = delegateEventBus;
        this.eventStore = eventStore;
    }
    
    @Override
    public void post(Event event) {
        // 存储事件
        if (event instanceof DomainEvent) {
            eventStore.store((DomainEvent) event);
        }
        
        // 处理事件
        delegateEventBus.post(event);
    }
    
    // 实现其他EventBus接口方法...
}

// 领域事件接口
public interface DomainEvent extends Event {
    String getAggregateId();
    long getVersion();
    LocalDateTime getTimestamp();
}

// 事件存储接口
public interface EventStore {
    void store(DomainEvent event);
    List<DomainEvent> getEvents(String aggregateId);
    List<DomainEvent> getEventsSince(LocalDateTime timestamp);
}
```

### 6.6.3 事件重放（Event Replay）

基于事件溯源，我们可以实现事件重放功能，用于系统恢复、测试或数据迁移：

```java
public class EventReplayService {
    private final EventStore eventStore;
    private final EventBus eventBus;
    
    public EventReplayService(EventStore eventStore, EventBus eventBus) {
        this.eventStore = eventStore;
        this.eventBus = eventBus;
    }
    
    public void replayEvents(String aggregateId) {
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        replayEvents(events);
    }
    
    public void replayEventsSince(LocalDateTime timestamp) {
        List<DomainEvent> events = eventStore.getEventsSince(timestamp);
        replayEvents(events);
    }
    
    private void replayEvents(List<DomainEvent> events) {
        // 按时间戳排序
        events.sort(Comparator.comparing(DomainEvent::getTimestamp));
        
        // 重放事件
        for (DomainEvent event : events) {
            eventBus.post(new ReplayedEvent(event));
        }
    }
    
    // 重放事件包装器
    public static class ReplayedEvent implements Event {
        private final DomainEvent originalEvent;
        
        public ReplayedEvent(DomainEvent originalEvent) {
            this.originalEvent = originalEvent;
        }
        
        public DomainEvent getOriginalEvent() {
            return originalEvent;
        }
        
        @Override
        public Object getSource() {
            return originalEvent.getSource();
        }
    }
}
```

## 6.7 练习题

1. 实现一个自定义的事件过滤器，根据事件的时间戳过滤事件，只处理特定时间范围内的事件。

2. 设计并实现一个事务拦截器，确保在同一事务中发布的多个事件要么全部成功处理，要么全部回滚。

3. 扩展事件框架，支持事件的持久化和重放功能，并编写测试用例验证其正确性。

4. 实现一个基于优先级的事件分发策略，允许在运行时动态调整监听器的优先级。

5. 设计一个分布式事件总线，支持跨JVM、跨服务的事件发布和订阅，并考虑网络分区、消息丢失等异常情况的处理。

## 6.8 小结

在本章中，我们介绍了事件框架的多种高级特性和性能优化技术：

1. **事件过滤器**：允许根据条件筛选事件，控制事件是否传递给特定监听器。

2. **事件拦截器**：在事件处理的不同阶段进行干预，实现横切关注点如日志记录、性能监控等。

3. **事件传播控制**：允许在事件处理过程中控制事件的传播，类似于DOM事件的冒泡机制。

4. **事件优先级**：通过优先级控制监听器的执行顺序，确保重要的监听器先执行。

5. **性能优化**：包括缓存机制、批量事件处理、线程池优化和监控指标收集等技术。

6. **高级应用场景**：分布式事件处理、事件溯源和事件重放等企业级应用场景。

通过这些高级特性和优化技术，我们的事件框架变得更加强大、灵活和高效，能够满足各种复杂业务场景的需求。在实际应用中，可以根据具体需求选择性地实现这些特性，打造一个适合自己项目的事件框架。

在下一章中，我们将总结整个事件框架的设计与实现，并探讨其在实际项目中的应用和最佳实践。