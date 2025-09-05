# Atlas Event Driven Framework

基于Java注解的事件驱动框架，支持发布/订阅模式，集成Spring Boot。

## 模块结构

```
atlas-event/
├── atlas-event-core/                  # 核心模块
├── atlas-event-spring-boot-starter/   # Spring Boot Starter模块
└── atlas-event-sample/                # 示例应用模块
```

## 技术栈

- Java 8+
- Spring 5.2+
- Spring Boot 2.2+
- Maven 3.6+

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.nemoob</groupId>
    <artifactId>atlas-event-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 启用事件发布

在需要发布事件的类上添加[@EventPublish](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/annotation/EventPublish.java#L11-L25)注解：

```java
@Service
@EventPublish
public class UserService {
    
    @Autowired
    private EventBus eventBus;
    
    public void registerUser(String username) {
        // 用户注册逻辑
        // ...
        
        // 发布事件
        UserRegisteredEvent event = new UserRegisteredEvent("user-id", username);
        eventBus.publish(event);
    }
}
```

### 3. 订阅事件

在需要处理事件的方法上添加[@EventSubscribe](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/annotation/EventSubscribe.java#L10-L24)注解：

```java
@Service
public class NotificationService {
    
    @EventSubscribe(eventType = "user.registered")
    public void onUserRegistered(UserRegisteredEvent event) {
        // 处理用户注册事件
        System.out.println("User registered: " + event.getUsername());
    }
    
    // 异步处理事件
    @EventSubscribe(eventType = "user.registered", async = true)
    public void onUserRegisteredAsync(UserRegisteredEvent event) {
        // 异步处理用户注册事件
        System.out.println("Async handling user registered: " + event.getUsername());
    }
}
```

## 配置选项

```yaml
atlas:
  event:
    thread-pool-size: 10        # 线程池大小
    persistence-type: database   # 持久化类型
    enable-async: true          # 是否启用异步处理
    max-retry-attempts: 3       # 最大重试次数
```

## 核心组件

### [@EventPublish](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/annotation/EventPublish.java#L11-L25)

标识事件发布者类。

### [@EventSubscribe](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/annotation/EventSubscribe.java#L10-L24)

标识事件消费者方法。

### [EventBus](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/core/EventBus.java#L6-L22)

事件总线接口，负责事件的发布和订阅管理。

### [Event](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/core/Event.java#L8-L25)

事件接口，定义事件的基本结构。

### [EventPersistence](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-core/src/main/java/io/github/nemoob/event/persistence/EventPersistence.java#L9-L22)

事件持久化接口，支持将事件持久化到存储系统。

## 示例

查看[atlas-event-sample](file:///Users/nemoob/dev/workspace/qoder/atlas/atlas-event/atlas-event-sample/)模块了解完整的使用示例。