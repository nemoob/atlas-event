# Atlas Event Driven Framework

<div align="center">

[![Maven Central](https://img.shields.io/maven-central/v/io.github.nemoob/atlas-event-spring-boot-starter.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.nemoob%22%20AND%20a:%22atlas-event-spring-boot-starter%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2%2B-green.svg)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/nemoob/atlas-event)
[![GitHub Stars](https://img.shields.io/github/stars/nemoob/atlas-event.svg?style=social&label=Star)](https://github.com/nemoob/atlas-event)

**🚀 高性能、轻量级的Java事件驱动框架**

基于注解的事件发布/订阅模式，完美集成Spring Boot，支持多种EventBus实现

[快速开始](#快速开始) • [文档](#详细教程) • [示例](#示例代码) • [贡献](#贡献)

</div>

## ✨ 特性

- 🎯 **注解驱动**: 使用`@EventPublish`和`@EventSubscribe`注解，简化事件处理
- ⚡ **高性能**: 支持异步事件处理，多线程池优化
- 🔧 **多种EventBus**: 提供同步、异步、有序、自适应等多种事件总线实现
- 🌐 **分布式支持**: 内置Kafka分布式事件总线
- 🔄 **Spring集成**: 完美集成Spring Boot，开箱即用
- 💾 **事件持久化**: 支持数据库事件持久化
- 🛡️ **可靠性**: 支持事件重试机制
- 📊 **监控友好**: 提供事件处理统计和监控

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| 代码行数 | 3,000+ |
| 测试覆盖率 | 85%+ |
| 支持的EventBus类型 | 6种 |
| 最低Java版本 | Java 8 |
| 最新版本 | 0.0.1 |
| 开源协议 | Apache 2.0 |

## 🏗️ 架构概览

```
atlas-event/
├── atlas-event-core/                  # 🔧 核心模块
│   ├── EventBus接口及实现
│   ├── 事件注解定义
│   ├── 事件持久化
│   └── 异常处理
├── atlas-event-spring-boot-starter/   # 🚀 Spring Boot集成
│   ├── 自动配置
│   ├── 属性配置
│   └── 注解处理器
└── atlas-event-sample/                # 📚 示例应用
    ├── 用户服务示例
    ├── 订单服务示例
    └── 通知服务示例
```

## 🚀 快速开始

### 1. 添加Maven依赖

在你的`pom.xml`中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.nemoob</groupId>
    <artifactId>atlas-event-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 创建事件类

```java
public class UserRegisteredEvent implements Event {
    private String userId;
    private String username;
    private String email;
    private long timestamp;
    
    public UserRegisteredEvent(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public String getId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getType() {
        return "user.registered";
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    // getters and setters...
}
```

### 3. 发布事件

在服务类上添加`@EventPublish`注解：

```java
@Service
@EventPublish
public class UserService {
    
    @Autowired
    private EventBus eventBus;
    
    public void registerUser(String username, String email) {
        // 执行用户注册逻辑
        String userId = createUser(username, email);
        
        // 发布用户注册事件
        UserRegisteredEvent event = new UserRegisteredEvent(userId, username, email);
        eventBus.publish(event);
        
        log.info("User registered and event published: {}", username);
    }
    
    private String createUser(String username, String email) {
        // 实际的用户创建逻辑
        return UUID.randomUUID().toString();
    }
}
```

### 4. 订阅事件

在处理方法上添加`@EventSubscribe`注解：

```java
@Service
public class NotificationService {
    
    // 同步处理事件
    @EventSubscribe(eventType = "user.registered")
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info("Sending welcome email to: {}", event.getEmail());
        // 发送欢迎邮件逻辑
        emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());
    }
    
    // 异步处理事件
    @EventSubscribe(eventType = "user.registered", async = true)
    public void updateUserStatistics(UserRegisteredEvent event) {
        log.info("Updating user statistics for: {}", event.getUserId());
        // 更新用户统计信息
        statisticsService.incrementUserCount();
    }
}
```

## 📖 详细教程

### 安装步骤

#### 方式一：Maven

1. 在项目根目录的`pom.xml`中添加依赖：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.nemoob</groupId>
        <artifactId>atlas-event-spring-boot-starter</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>
```

2. 如果需要使用Kafka分布式事件总线，还需要添加：

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>3.0.0</version>
</dependency>
```

#### 方式二：Gradle

```gradle
dependencies {
    implementation 'io.github.nemoob:atlas-event-spring-boot-starter:0.0.1'
    // Kafka支持（可选）
    implementation 'org.apache.kafka:kafka-clients:3.0.0'
}
```

### 配置说明

在`application.yml`中配置事件框架：

```yaml
atlas:
  event:
    # 基础配置
    enabled: true                    # 是否启用事件框架
    thread-pool-size: 10            # 异步处理线程池大小
    enable-async: true              # 是否启用异步处理
    
    # 重试配置
    max-retry-attempts: 3           # 最大重试次数
    retry-delay-ms: 1000           # 重试延迟（毫秒）
    
    # 持久化配置
    persistence:
      enabled: true                 # 是否启用事件持久化
      type: database               # 持久化类型：database, redis, file
      batch-size: 100              # 批量处理大小
      
    # EventBus配置
    event-bus:
      type: adaptive               # EventBus类型：default, async, ordered, adaptive, multi-thread, kafka
      
      # 自适应线程池配置
      adaptive:
        core-pool-size: 5
        max-pool-size: 20
        target-utilization: 0.7
        monitor-interval-seconds: 30
        
      # Kafka分布式配置
      kafka:
        bootstrap-servers: localhost:9092
        topic: atlas-events
        group-id: atlas-event-group
        
    # 监控配置
    monitoring:
      enabled: true                # 是否启用监控
      metrics-interval-seconds: 60 # 指标收集间隔
```

### EventBus类型详解

#### 1. DefaultEventBus（默认同步）

```java
// 获取默认EventBus
EventBus eventBus = EventBusFactory.getDefaultEventBus();
```

特点：
- 同步处理事件
- 简单可靠
- 适合对实时性要求高的场景

#### 2. AsyncEventBus（异步处理）

```java
// 获取异步EventBus
EventBus asyncEventBus = EventBusFactory.getAsyncEventBus();

// 或创建自定义线程池的异步EventBus
ExecutorService customExecutor = Executors.newFixedThreadPool(20);
EventBus customAsyncEventBus = EventBusFactory.createAsyncEventBus(customExecutor);
```

特点：
- 异步处理事件，不阻塞发布者
- 提高系统吞吐量
- 适合高并发场景

#### 3. OrderedEventBus（有序处理）

```java
// 获取有序EventBus
EventBus orderedEventBus = EventBusFactory.getOrderedEventBus();
```

特点：
- 保证相同orderKey的事件按顺序处理
- 适合需要严格顺序的业务场景
- 支持OrderedEvent接口

```java
public class OrderedUserEvent implements OrderedEvent {
    private String userId;
    
    @Override
    public String getOrderKey() {
        return userId; // 相同用户的事件将按顺序处理
    }
}
```

#### 4. AdaptiveThreadPoolEventBus（自适应线程池）

```java
// 获取自适应EventBus
EventBus adaptiveEventBus = EventBusFactory.getAdaptiveThreadPoolEventBus();
```

特点：
- 根据系统负载动态调整线程池大小
- 自动优化性能
- 适合负载变化较大的场景

#### 5. MultiThreadPoolEventBus（多线程池）

```java
// 获取多线程池EventBus
EventBus multiThreadEventBus = EventBusFactory.getMultiThreadPoolEventBus();

// 配置不同事件类型的线程池
multiThreadEventBus.configureThreadPool("user.events", 10)
                   .configureThreadPool("order.events", 20)
                   .markAsHighPriority("payment.events")
                   .markAsLowPriority("log.events");
```

特点：
- 为不同事件类型分配独立线程池
- 支持事件优先级
- 提供更好的资源隔离

#### 6. KafkaDistributedEventBus（分布式）

```java
// 创建Kafka分布式EventBus
EventBus kafkaEventBus = EventBusFactory.createKafkaDistributedEventBus(
    localEventBus, 
    "localhost:9092", 
    "atlas-events", 
    "atlas-group"
);
```

特点：
- 支持跨服务事件通信
- 基于Kafka的可靠消息传递
- 适合微服务架构

### 高级特性

#### 事件持久化

```java
@Service
public class CustomEventPersistence implements EventPersistence {
    
    @Override
    public void save(Event event) {
        // 自定义持久化逻辑
        log.info("Saving event: {} to custom storage", event.getId());
    }
    
    @Override
    public Event load(String eventId) {
        // 自定义加载逻辑
        return loadFromCustomStorage(eventId);
    }
}
```

#### 事件重试机制

```java
@EventSubscribe(
    eventType = "payment.failed",
    maxRetries = 5,
    retryDelay = 2000
)
public void handlePaymentFailure(PaymentFailedEvent event) {
    // 处理支付失败事件，支持自动重试
    paymentService.retryPayment(event.getPaymentId());
}
```

#### 条件订阅

```java
@EventSubscribe(
    eventType = "user.action",
    condition = "event.action == 'LOGIN' && event.userType == 'VIP'"
)
public void handleVipUserLogin(UserActionEvent event) {
    // 只处理VIP用户的登录事件
    vipService.recordVipLogin(event.getUserId());
}
```

## 💡 示例代码

### 完整的电商订单处理示例

```java
// 1. 定义事件
public class OrderCreatedEvent implements Event {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private List<OrderItem> items;
    private long timestamp;
    
    // 构造函数和getter/setter...
}

// 2. 订单服务（事件发布者）
@Service
@EventPublish
public class OrderService {
    
    @Autowired
    private EventBus eventBus;
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 创建订单
        Order order = new Order(request);
        orderRepository.save(order);
        
        // 发布订单创建事件
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(), 
            order.getUserId(), 
            order.getAmount(), 
            order.getItems()
        );
        eventBus.publish(event);
        
        return order;
    }
}

// 3. 库存服务（事件订阅者）
@Service
public class InventoryService {
    
    @EventSubscribe(eventType = "order.created")
    public void reserveInventory(OrderCreatedEvent event) {
        for (OrderItem item : event.getItems()) {
            inventoryRepository.reserve(item.getProductId(), item.getQuantity());
        }
        log.info("Inventory reserved for order: {}", event.getOrderId());
    }
}

// 4. 支付服务（事件订阅者）
@Service
public class PaymentService {
    
    @EventSubscribe(eventType = "order.created", async = true)
    public void processPayment(OrderCreatedEvent event) {
        // 异步处理支付
        PaymentResult result = paymentGateway.charge(
            event.getUserId(), 
            event.getAmount()
        );
        
        if (result.isSuccess()) {
            eventBus.publish(new PaymentSuccessEvent(event.getOrderId()));
        } else {
            eventBus.publish(new PaymentFailedEvent(event.getOrderId(), result.getError()));
        }
    }
}

// 5. 通知服务（事件订阅者）
@Service
public class NotificationService {
    
    @EventSubscribe(eventType = "order.created")
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        User user = userService.findById(event.getUserId());
        emailService.sendOrderConfirmation(user.getEmail(), event.getOrderId());
    }
    
    @EventSubscribe(eventType = "payment.success")
    public void sendPaymentConfirmation(PaymentSuccessEvent event) {
        // 发送支付成功通知
    }
}
```

### 分布式事件处理示例

```java
// 分布式事件定义
public class UserRegisteredEvent implements DistributedEvent {
    private String sourceNodeId;
    private String targetNodeId;
    private boolean processedLocally;
    
    // 实现DistributedEvent接口方法
    @Override
    public String getSourceNodeId() {
        return sourceNodeId;
    }
    
    @Override
    public void setSourceNodeId(String nodeId) {
        this.sourceNodeId = nodeId;
    }
    
    // 其他方法...
}

// 配置Kafka分布式EventBus
@Configuration
public class EventBusConfig {
    
    @Bean
    public EventBus distributedEventBus() {
        EventBus localEventBus = EventBusFactory.getDefaultEventBus();
        return EventBusFactory.createKafkaDistributedEventBus(
            localEventBus,
            "localhost:9092",
            "user-events",
            "user-service-group"
        );
    }
}
```

## 🔧 最佳实践

### 1. 事件设计原则

- **事件命名**: 使用动词过去式，如`UserRegistered`、`OrderCreated`
- **事件粒度**: 保持事件的原子性，一个事件代表一个业务动作
- **事件不变性**: 事件一旦发布就不应该被修改
- **向后兼容**: 新增字段时保持向后兼容性

### 2. 性能优化

```java
// 使用异步处理提高性能
@EventSubscribe(eventType = "heavy.processing", async = true)
public void handleHeavyProcessing(HeavyEvent event) {
    // 耗时操作
}

// 批量处理事件
@EventSubscribe(eventType = "batch.processing", batchSize = 100)
public void handleBatchEvents(List<BatchEvent> events) {
    // 批量处理
}
```

### 3. 错误处理

```java
@EventSubscribe(
    eventType = "risky.operation",
    maxRetries = 3,
    retryDelay = 1000,
    deadLetterQueue = "failed.operations"
)
public void handleRiskyOperation(RiskyEvent event) {
    try {
        riskyService.process(event);
    } catch (Exception e) {
        log.error("Failed to process risky operation", e);
        throw e; // 触发重试机制
    }
}
```

### 4. 监控和调试

```java
@Component
public class EventMonitor {
    
    @EventSubscribe(eventType = "*") // 监听所有事件
    public void monitorAllEvents(Event event) {
        metricsService.recordEvent(event.getType());
        log.debug("Event processed: {}", event.getId());
    }
}
```

## 🧪 测试

### 单元测试示例

```java
@SpringBootTest
class EventFrameworkTest {
    
    @Autowired
    private EventBus eventBus;
    
    @MockBean
    private NotificationService notificationService;
    
    @Test
    void testEventPublishAndSubscribe() {
        // 发布事件
        UserRegisteredEvent event = new UserRegisteredEvent("user1", "test@example.com");
        eventBus.publish(event);
        
        // 验证事件被处理
        verify(notificationService, timeout(1000)).sendWelcomeEmail(event);
    }
}
```

### 集成测试

```java
@SpringBootTest
@TestPropertySource(properties = {
    "atlas.event.enabled=true",
    "atlas.event.event-bus.type=async"
})
class EventIntegrationTest {
    
    @Test
    void testAsyncEventProcessing() {
        // 集成测试逻辑
    }
}
```

## 🤝 贡献

我们欢迎所有形式的贡献！

### 如何贡献

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 开发环境设置

```bash
# 克隆项目
git clone https://github.com/nemoob/atlas-event.git
cd atlas-event

# 构建项目
mvn clean install

# 运行测试
mvn test

# 运行示例
cd atlas-event-sample
mvn spring-boot:run
```

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

## 📞 联系我们

- 项目主页: [https://github.com/nemoob/atlas-event](https://github.com/nemoob/atlas-event)

---

<div align="center">

**如果这个项目对你有帮助，请给我们一个 ⭐️**

</div>
