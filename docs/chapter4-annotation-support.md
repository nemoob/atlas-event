# 第4章：添加注解支持

在前面的章节中，我们已经实现了基础的事件总线，可以通过编程方式注册监听器和发布事件。然而，在实际应用中，我们希望能够更加便捷地定义和注册事件监听器。本章将介绍如何使用注解来简化事件监听器的注册过程。

## 4.1 注解的优势

使用注解来定义事件监听器有以下几个优势：

1. **声明式编程**：通过注解，我们可以以声明式的方式定义事件监听器，使代码更加简洁和易读。
2. **减少样板代码**：不需要手动实现 `EventListener` 接口，只需要在方法上添加注解即可。
3. **类型安全**：可以直接在方法参数中指定事件类型，避免类型转换错误。
4. **自动注册**：可以通过扫描带有注解的方法，自动注册到事件总线，减少手动注册的工作。

## 4.2 定义事件订阅注解

首先，我们需要定义一个注解，用于标记事件监听方法：

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法为事件监听器方法
 */
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
@Target(ElementType.METHOD) // 注解只能应用于方法
public @interface EventSubscribe {
    /**
     * 事件类型，如果为空，则使用方法参数的事件类型
     */
    String eventType() default "";
    
    /**
     * 事件类，如果指定，则只处理该类型的事件
     */
    Class<?> eventClass() default Object.class;
    
    /**
     * 是否异步处理事件
     */
    boolean async() default false;
}
```

这个注解有三个属性：
- `eventType`：指定事件类型，如果为空，则使用方法参数的事件类型。
- `eventClass`：指定事件类，如果指定，则只处理该类型的事件。
- `async`：指定是否异步处理事件。

## 4.3 实现注解处理器

接下来，我们需要实现一个注解处理器，用于扫描带有 `@EventSubscribe` 注解的方法，并将其注册到事件总线：

```java
import java.lang.reflect.Method;
import java.util.Set;

public class EventAnnotationProcessor {
    private final EventBus eventBus;
    
    public EventAnnotationProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    /**
     * 扫描对象中带有 @EventSubscribe 注解的方法，并注册到事件总线
     * 
     * @param listener 包含事件监听方法的对象
     */
    public void processAnnotations(Object listener) {
        // 获取对象的所有方法
        Method[] methods = listener.getClass().getDeclaredMethods();
        
        for (Method method : methods) {
            // 检查方法是否带有 @EventSubscribe 注解
            EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
            if (annotation != null) {
                // 检查方法参数
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) {
                    throw new IllegalArgumentException("Method " + method.getName() + " has @EventSubscribe annotation but does not have exactly one parameter of type Event");
                }
                
                // 获取事件类型
                String eventType = annotation.eventType();
                if (eventType.isEmpty()) {
                    // 如果未指定事件类型，则使用事件类的名称
                    Class<?> eventClass = annotation.eventClass();
                    if (eventClass == Object.class) {
                        // 如果未指定事件类，则使用方法参数的事件类型
                        eventClass = parameterTypes[0];
                    }
                    eventType = eventClass.getName();
                }
                
                // 创建事件监听器
                EventListener eventListener = createEventListener(listener, method, annotation.async());
                
                // 注册到事件总线
                eventBus.register(eventType, eventListener);
            }
        }
    }
    
    /**
     * 创建事件监听器
     * 
     * @param target 目标对象
     * @param method 监听方法
     * @param async 是否异步处理
     * @return 事件监听器
     */
    private EventListener createEventListener(Object target, Method method, boolean async) {
        // 确保方法可访问
        method.setAccessible(true);
        
        // 创建事件监听器
        EventListener listener = event -> {
            try {
                method.invoke(target, event);
            } catch (Exception e) {
                throw new RuntimeException("Error invoking event listener method: " + method.getName(), e);
            }
        };
        
        // 如果需要异步处理，则包装为异步监听器
        if (async) {
            return new AsyncEventListener(listener);
        }
        
        return listener;
    }
    
    /**
     * 异步事件监听器包装类
     */
    private class AsyncEventListener implements EventListener {
        private final EventListener delegate;
        private final ExecutorService executorService;
        
        public AsyncEventListener(EventListener delegate) {
            this.delegate = delegate;
            this.executorService = Executors.newCachedThreadPool();
        }
        
        @Override
        public void onEvent(Event event) {
            executorService.submit(() -> delegate.onEvent(event));
        }
    }
}
```

这个注解处理器的主要功能是：

1. 扫描对象中带有 `@EventSubscribe` 注解的方法。
2. 检查方法参数是否符合要求（只有一个参数，且类型为 `Event` 或其子类）。
3. 获取事件类型，可以从注解的 `eventType` 属性、`eventClass` 属性或方法参数类型中获取。
4. 创建事件监听器，将方法调用包装为 `EventListener` 接口的实现。
5. 如果需要异步处理，则将监听器包装为异步监听器。
6. 将监听器注册到事件总线。

## 4.4 扩展事件总线接口

为了支持注解处理，我们需要扩展 `EventBus` 接口，添加一个方法用于扫描和注册带有注解的监听器：

```java
public interface EventBus {
    // 原有方法...
    
    /**
     * 扫描对象中带有 @EventSubscribe 注解的方法，并注册到事件总线
     * 
     * @param listener 包含事件监听方法的对象
     */
    void scanAndRegister(Object listener);
}
```

然后在 `DefaultEventBus` 中实现这个方法：

```java
public class DefaultEventBus implements EventBus {
    // 原有代码...
    
    private final EventAnnotationProcessor annotationProcessor;
    
    public DefaultEventBus() {
        this(new DefaultEventTypeResolver());
    }
    
    public DefaultEventBus(EventTypeResolver eventTypeResolver) {
        this.eventTypeResolver = eventTypeResolver;
        this.annotationProcessor = new EventAnnotationProcessor(this);
    }
    
    @Override
    public void scanAndRegister(Object listener) {
        annotationProcessor.processAnnotations(listener);
    }
}
```

## 4.5 使用注解的示例

下面是一个使用注解的示例：

```java
// 定义事件类
public class OrderCreatedEvent implements Event {
    // 与前面章节相同...
}

// 使用注解定义事件监听器
public class OrderService {
    @EventSubscribe(eventType = "ORDER_CREATED")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("Order created: " + event.getOrderId() + ", amount: " + event.getAmount());
        // 处理订单创建逻辑...
    }
    
    @EventSubscribe(eventClass = OrderCreatedEvent.class, async = true)
    public void sendOrderConfirmationEmail(OrderCreatedEvent event) {
        System.out.println("Sending confirmation email for order: " + event.getOrderId());
        // 发送确认邮件...
    }
}

// 使用事件总线
public class AnnotationDemo {
    public static void main(String[] args) {
        // 获取默认事件总线
        EventBus eventBus = EventBusFactory.getDefaultEventBus();
        
        // 创建服务对象
        OrderService orderService = new OrderService();
        
        // 扫描并注册带有注解的方法
        eventBus.scanAndRegister(orderService);
        
        // 创建并发布事件
        Event orderEvent = new OrderCreatedEvent("ORD-001", 99.99);
        eventBus.publish(orderEvent);
        
        // 等待异步任务完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

在这个示例中，我们定义了两个带有 `@EventSubscribe` 注解的方法：

1. `handleOrderCreated`：通过 `eventType` 属性指定事件类型，同步处理事件。
2. `sendOrderConfirmationEmail`：通过 `eventClass` 属性指定事件类，异步处理事件。

然后，我们使用 `scanAndRegister` 方法扫描并注册这些方法，最后发布一个事件，这两个方法都会被调用。

## 4.6 注解处理流程

![注解处理流程](/Users/nemoob/dev/workspace/qoder/atlas/atlas-event/docs/images/annotation-processing-flow.svg)

注解处理的流程如下：

1. 调用 `eventBus.scanAndRegister(listener)` 方法。
2. `EventAnnotationProcessor` 扫描对象中带有 `@EventSubscribe` 注解的方法。
3. 对于每个带有注解的方法，创建一个 `EventListener` 实例。
4. 根据注解的属性，确定事件类型和处理方式（同步或异步）。
5. 将创建的监听器注册到事件总线。

## 4.7 反射与性能考虑

使用注解和反射可以简化代码，但也会带来一些性能开销。以下是一些性能考虑和优化建议：

1. **缓存反射结果**：反射操作比较耗时，可以缓存反射的结果，避免重复扫描。
2. **预编译**：可以在应用启动时进行一次性扫描，而不是在运行时动态扫描。
3. **使用字节码生成**：可以使用字节码生成技术（如 ASM、Javassist 等）生成代理类，避免反射调用的开销。
4. **注解处理器**：可以使用 Java 的注解处理器（Annotation Processor）在编译时生成代码，避免运行时反射。

## 4.8 注解处理器的改进

为了提高性能，我们可以对注解处理器进行一些改进：

```java
public class CachingEventAnnotationProcessor extends EventAnnotationProcessor {
    // 缓存已处理的类
    private final Map<Class<?>, List<EventListenerMethod>> cache = new ConcurrentHashMap<>();
    
    public CachingEventAnnotationProcessor(EventBus eventBus) {
        super(eventBus);
    }
    
    @Override
    public void processAnnotations(Object listener) {
        Class<?> listenerClass = listener.getClass();
        
        // 检查缓存
        List<EventListenerMethod> methods = cache.get(listenerClass);
        if (methods == null) {
            // 缓存未命中，扫描并缓存
            methods = scanMethods(listenerClass);
            cache.put(listenerClass, methods);
        }
        
        // 注册监听器
        for (EventListenerMethod method : methods) {
            EventListener eventListener = createEventListener(listener, method.getMethod(), method.isAsync());
            getEventBus().register(method.getEventType(), eventListener);
        }
    }
    
    private List<EventListenerMethod> scanMethods(Class<?> listenerClass) {
        List<EventListenerMethod> result = new ArrayList<>();
        Method[] methods = listenerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
            if (annotation != null) {
                // 检查方法参数
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) {
                    throw new IllegalArgumentException("Method " + method.getName() + " has @EventSubscribe annotation but does not have exactly one parameter of type Event");
                }
                
                // 获取事件类型
                String eventType = annotation.eventType();
                if (eventType.isEmpty()) {
                    // 如果未指定事件类型，则使用事件类的名称
                    Class<?> eventClass = annotation.eventClass();
                    if (eventClass == Object.class) {
                        // 如果未指定事件类，则使用方法参数的事件类型
                        eventClass = parameterTypes[0];
                    }
                    eventType = eventClass.getName();
                }
                
                result.add(new EventListenerMethod(method, eventType, annotation.async()));
            }
        }
        
        return result;
    }
    
    // 内部类，用于存储方法信息
    private static class EventListenerMethod {
        private final Method method;
        private final String eventType;
        private final boolean async;
        
        public EventListenerMethod(Method method, String eventType, boolean async) {
            this.method = method;
            this.eventType = eventType;
            this.async = async;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public boolean isAsync() {
            return async;
        }
    }
}
```

这个改进版的注解处理器使用缓存来存储已处理的类的信息，避免重复扫描，提高性能。

## 4.9 小结

在本章中，我们介绍了如何使用注解来简化事件监听器的注册过程。通过 `@EventSubscribe` 注解，我们可以以声明式的方式定义事件监听器，减少样板代码，提高代码的可读性和可维护性。

我们实现了一个注解处理器，用于扫描带有注解的方法，并将其注册到事件总线。同时，我们也讨论了使用注解和反射带来的性能考虑，并提供了一些优化建议。

在下一章中，我们将介绍如何将事件框架与 Spring Boot 集成，使其更加易用和强大。

## 练习

1. 扩展 `@EventSubscribe` 注解，添加 `priority` 属性，用于指定监听器的优先级。
2. 实现一个 `@EventPublisher` 注解，用于标记事件发布方法，自动将方法的返回值发布为事件。
3. 实现一个编译时注解处理器，在编译时生成代码，避免运行时反射。
4. 修改 `EventAnnotationProcessor`，使其支持继承，即扫描父类中带有注解的方法。