# Atlas Event Framework - 开发者指南

<div align="center">

[![Version](https://img.shields.io/badge/version-0.0.1-blue.svg)](https://github.com/nemoob/atlas-event)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2%2B-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**专业的Java事件驱动框架开发指南**

</div>

## 📋 目录

- [1. 概述](#1-概述)
- [2. 环境准备](#2-环境准备)
- [3. 快速安装](#3-快速安装)
- [4. 核心架构](#4-核心架构)
- [5. API 文档](#5-api-文档)
- [6. 配置指南](#6-配置指南)
- [7. 开发示例](#7-开发示例)
- [8. 高级特性](#8-高级特性)
- [9. 性能调优](#9-性能调优)
- [10. 测试指南](#10-测试指南)
- [11. 部署指南](#11-部署指南)
- [12. 故障排除](#12-故障排除)
- [13. 贡献指南](#13-贡献指南)

## 1. 概述

### 1.1 框架介绍

Atlas Event Framework 是一个高性能、轻量级的Java事件驱动框架，专为现代微服务架构设计。框架基于注解驱动，提供了简洁的API和强大的功能，支持同步/异步事件处理、分布式事件传播、事件持久化等企业级特性。

### 1.2 核心特性

- 🎯 **注解驱动**: 使用`@EventPublish`和`@EventSubscribe`注解简化开发
- ⚡ **高性能**: 支持多种EventBus实现，优化并发处理
- 🌐 **分布式**: 内置Kafka支持，实现跨服务事件通信
- 🔄 **Spring集成**: 完美集成Spring Boot生态系统
- 💾 **持久化**: 支持事件存储和重放机制
- 🛡️ **可靠性**: 提供事件重试、死信队列等容错机制
- 📊 **监控**: 内置指标收集和监控支持

### 1.3 适用场景

- 微服务间解耦通信
- 领域事件驱动设计(DDD)
- 异步任务处理
- 系统集成和数据同步
- 审计日志和事件溯源

## 2. 环境准备

### 2.1 系统要求

| 组件 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 8 | 11+ |
| Spring Boot | 2.2.0 | 2.7.x |
| Maven | 3.6.0 | 3.8.x |
| Gradle | 6.0 | 7.x |

### 2.2 开发环境设置

#### IDE配置

**IntelliJ IDEA**
```bash
# 安装必要插件
- Lombok Plugin
- Spring Boot Plugin
- Maven Helper
```

**Eclipse**
```bash
# 安装Spring Tools Suite
# 配置Maven集成
# 启用注解处理
```

#### 项目初始化

```bash
# 克隆项目
git clone https://github.com/nemoob/atlas-event.git
cd atlas-event

# 构建项目
mvn clean install

# 验证安装
mvn test
```

## 3. 快速安装

### 3.1 Maven 依赖

在项目的`pom.xml`中添加以下依赖：

```xml
<dependencies>
    <!-- Atlas Event Spring Boot Starter -->
    <dependency>
        <groupId>io.github.nemoob</groupId>
        <artifactId>atlas-event-spring-boot-starter</artifactId>
        <version>0.0.1</version>
    </dependency>
    
    <!-- 可选：Kafka支持 -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.0.0</version>
        <optional>true</optional>
    </dependency>
    
    <!-- 可选：数据库持久化支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 3.2 Gradle 依赖

在`build.gradle`中添加：

```gradle
dependencies {
    implementation 'io.github.nemoob:atlas-event-spring-boot-starter:0.0.1'
    
    // 可选依赖
    implementation 'org.apache.kafka:kafka-clients:3.0.0'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
}
```

### 3.3 自动配置

框架提供自动配置功能，在Spring Boot应用中会自动启用。如需自定义配置，请参考[配置指南](#6-配置指南)。

## 4. 核心架构

### 4.1 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                    Atlas Event Framework                    │
├─────────────────────────────────────────────────────────────┤
│  Application Layer                                          │
│  ┌─────────────────┐    ┌─────────────────┐               │
│  │  Event Publisher │    │ Event Subscriber │               │
│  │  @EventPublish  │    │ @EventSubscribe │               │
│  └─────────────────┘    └─────────────────┘               │
├─────────────────────────────────────────────────────────────┤
│  Framework Layer                                            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                   EventBus                              │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │   Default   │ │    Async    │ │   Ordered   │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │  Adaptive   │ │ MultiThread │ │    Kafka    │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                       │
│  ┌─────────────────┐    ┌─────────────────┐               │
│  │   Persistence   │    │    Monitoring   │               │
│  │   - Database    │    │    - Metrics    │               │
│  │   - Redis       │    │    - Health     │               │
│  │   - File        │    │    - Tracing    │               │
│  └─────────────────┘    └─────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 核心组件

#### 4.2.1 Event 接口

所有事件必须实现`Event`接口：

```java
public interface Event {
    String getId();           // 事件唯一标识
    String getType();         // 事件类型
    long getTimestamp();      // 事件时间戳
}
```

#### 4.2.2 EventBus 接口

事件总线核心接口：

```java
public interface EventBus {
    void publish(Event event);              // 发布事件
    void register(Object listener);         // 注册监听器
    void unregister(Object listener);       // 注销监听器
    void scanAndRegister(Object listener);  // 扫描并注册
}
```

#### 4.2.3 EventBus 实现类型

| 类型 | 描述 | 适用场景 |
|------|------|----------|
| DefaultEventBus | 同步处理 | 简单场景，要求强一致性 |
| AsyncEventBus | 异步处理 | 高并发场景，提升吞吐量 |
| OrderedEventBus | 有序处理 | 需要保证事件顺序的场景 |
| AdaptiveThreadPoolEventBus | 自适应线程池 | 负载变化大的场景 |
| MultiThreadPoolEventBus | 多线程池 | 不同事件类型需要隔离 |
| KafkaDistributedEventBus | 分布式 | 微服务间通信 |

## 5. API 文档

### 5.1 注解API

#### @EventPublish

标记事件发布者类，启用事件发布功能。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventPublish {
    // 无属性，仅作为标记注解
}
```

**使用示例：**

```java
@Service
@EventPublish
public class UserService {
    @Autowired
    private EventBus eventBus;
    
    public void createUser(User user) {
        // 业务逻辑
        userRepository.save(user);
        
        // 发布事件
        eventBus.publish(new UserCreatedEvent(user.getId(), user.getName()));
    }
}
```

#### @EventSubscribe

标记事件订阅方法，定义事件处理逻辑。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventSubscribe {
    String eventType();                    // 事件类型
    boolean async() default false;        // 是否异步处理
    int maxRetries() default 0;          // 最大重试次数
    long retryDelay() default 1000;      // 重试延迟(ms)
    String condition() default "";        // 条件表达式
    int priority() default 0;            // 优先级
    String deadLetterQueue() default ""; // 死信队列
}
```

**使用示例：**

```java
@Service
public class NotificationService {
    
    // 基本订阅
    @EventSubscribe(eventType = "user.created")
    public void handleUserCreated(UserCreatedEvent event) {
        sendWelcomeEmail(event.getUserId());
    }
    
    // 异步处理
    @EventSubscribe(
        eventType = "order.created", 
        async = true,
        maxRetries = 3,
        retryDelay = 2000
    )
    public void processOrder(OrderCreatedEvent event) {
        // 异步处理订单
    }
    
    // 条件订阅
    @EventSubscribe(
        eventType = "user.action",
        condition = "event.action == 'LOGIN' && event.userType == 'VIP'"
    )
    public void handleVipLogin(UserActionEvent event) {
        // 只处理VIP用户登录
    }
}
```

### 5.2 EventBus Factory API

#### 获取EventBus实例

```java
public class EventBusFactory {
    
    // 获取单例实例
    public static EventBus getDefaultEventBus();
    public static EventBus getAsyncEventBus();
    public static EventBus getOrderedEventBus();
    public static EventBus getAdaptiveThreadPoolEventBus();
    public static EventBus getMultiThreadPoolEventBus();
    
    // 创建新实例
    public static EventBus createSyncEventBus();
    public static EventBus createAsyncEventBus(int threadPoolSize);
    public static EventBus createAsyncEventBus(ExecutorService executor);
    public static EventBus createOrderedEventBus(EventBus delegate);
    public static EventBus createAdaptiveThreadPoolEventBus(
        EventBus delegate, int corePoolSize, int maxPoolSize, 
        double targetUtilization, int monitorIntervalSeconds);
    public static EventBus createMultiThreadPoolEventBus(
        EventBus delegate, int defaultThreadPoolSize);
    public static EventBus createKafkaDistributedEventBus(
        EventBus localEventBus, String bootstrapServers, 
        String topic, String groupId);
    
    // 资源管理
    public static void shutdownAll();
}
```

### 5.3 事件持久化API

#### EventPersistence 接口

```java
public interface EventPersistence {
    void save(Event event);              // 保存事件
    Event load(String eventId);         // 加载事件
    List<Event> loadByType(String type); // 按类型加载
    void delete(String eventId);        // 删除事件
}
```

#### 自定义持久化实现

```java
@Component
public class RedisEventPersistence implements EventPersistence {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void save(Event event) {
        String key = "event:" + event.getId();
        redisTemplate.opsForValue().set(key, event, Duration.ofDays(7));
    }
    
    @Override
    public Event load(String eventId) {
        String key = "event:" + eventId;
        return (Event) redisTemplate.opsForValue().get(key);
    }
    
    // 其他方法实现...
}
```

## 6. 配置指南

### 6.1 基础配置

在`application.yml`中配置框架参数：

```yaml
atlas:
  event:
    # 基础设置
    enabled: true                    # 启用框架
    thread-pool-size: 10            # 默认线程池大小
    enable-async: true              # 启用异步处理
    
    # 重试配置
    retry:
      max-attempts: 3               # 最大重试次数
      delay-ms: 1000               # 重试延迟
      backoff-multiplier: 2.0      # 退避倍数
      max-delay-ms: 30000          # 最大延迟
    
    # 持久化配置
    persistence:
      enabled: true                 # 启用持久化
      type: database               # 类型: database, redis, file
      batch-size: 100              # 批处理大小
      flush-interval-ms: 5000      # 刷新间隔
      
    # EventBus配置
    event-bus:
      type: adaptive               # 默认EventBus类型
      
      # 异步EventBus配置
      async:
        core-pool-size: 5
        max-pool-size: 20
        queue-capacity: 1000
        keep-alive-seconds: 60
        
      # 自适应EventBus配置
      adaptive:
        core-pool-size: 5
        max-pool-size: 50
        target-utilization: 0.7
        monitor-interval-seconds: 30
        scale-up-threshold: 0.8
        scale-down-threshold: 0.3
        
      # 多线程池EventBus配置
      multi-thread:
        default-pool-size: 10
        pools:
          user-events: 5
          order-events: 15
          notification-events: 8
        priorities:
          high: ["payment.events", "security.events"]
          low: ["log.events", "analytics.events"]
          
      # Kafka分布式配置
      kafka:
        enabled: false
        bootstrap-servers: localhost:9092
        topic: atlas-events
        group-id: atlas-event-group
        producer:
          acks: all
          retries: 3
          batch-size: 16384
          linger-ms: 5
        consumer:
          auto-offset-reset: latest
          enable-auto-commit: false
          max-poll-records: 500
          
    # 监控配置
    monitoring:
      enabled: true                # 启用监控
      metrics:
        enabled: true              # 启用指标收集
        interval-seconds: 60       # 收集间隔
        export-to-prometheus: true # 导出到Prometheus
      health:
        enabled: true              # 启用健康检查
        timeout-ms: 5000          # 超时时间
      tracing:
        enabled: true              # 启用链路追踪
        sample-rate: 0.1          # 采样率
```

### 6.2 环境特定配置

#### 开发环境 (application-dev.yml)

```yaml
atlas:
  event:
    event-bus:
      type: default              # 使用同步EventBus便于调试
    monitoring:
      enabled: false             # 关闭监控减少日志
    persistence:
      enabled: false             # 关闭持久化加快启动
```

#### 测试环境 (application-test.yml)

```yaml
atlas:
  event:
    event-bus:
      type: async
      async:
        core-pool-size: 2
        max-pool-size: 5
    retry:
      max-attempts: 1            # 减少重试加快测试
```

#### 生产环境 (application-prod.yml)

```yaml
atlas:
  event:
    event-bus:
      type: adaptive             # 使用自适应EventBus
    persistence:
      enabled: true
      type: database
    monitoring:
      enabled: true
      metrics:
        export-to-prometheus: true
    kafka:
      enabled: true              # 启用分布式支持
```

### 6.3 自定义配置

#### 配置类方式

```java
@Configuration
@EnableConfigurationProperties(AtlasEventProperties.class)
public class EventConfiguration {
    
    @Bean
    @Primary
    public EventBus customEventBus() {
        return EventBusFactory.createAdaptiveThreadPoolEventBus(
            EventBusFactory.createSyncEventBus(),
            10, 50, 0.75, 30
        );
    }
    
    @Bean
    @ConditionalOnProperty("atlas.event.kafka.enabled")
    public EventBus kafkaEventBus(
            @Qualifier("customEventBus") EventBus localEventBus,
            AtlasEventProperties properties) {
        
        var kafkaConfig = properties.getKafka();
        return EventBusFactory.createKafkaDistributedEventBus(
            localEventBus,
            kafkaConfig.getBootstrapServers(),
            kafkaConfig.getTopic(),
            kafkaConfig.getGroupId()
        );
    }
    
    @Bean
    public EventPersistence customEventPersistence() {
        return new RedisEventPersistence();
    }
}
```

## 7. 开发示例

### 7.1 基础示例

#### 定义事件

```java
// 基础事件
public class UserRegisteredEvent implements Event {
    private final String id;
    private final String userId;
    private final String username;
    private final String email;
    private final long timestamp;
    
    public UserRegisteredEvent(String userId, String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public String getId() { return id; }
    
    @Override
    public String getType() { return "user.registered"; }
    
    @Override
    public long getTimestamp() { return timestamp; }
    
    // getters...
}

// 有序事件
public class OrderStatusChangedEvent implements OrderedEvent {
    private final String orderId;
    private final String status;
    // 其他字段...
    
    @Override
    public String getOrderKey() {
        return orderId; // 相同订单的事件将按顺序处理
    }
    
    // 其他方法...
}

// 分布式事件
public class PaymentProcessedEvent implements DistributedEvent {
    private String sourceNodeId;
    private String targetNodeId;
    private boolean processedLocally;
    // 其他字段...
    
    // 实现DistributedEvent接口方法...
}
```

#### 事件发布者

```java
@Service
@EventPublish
@Slf4j
public class UserService {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // 1. 验证用户数据
        validateUserData(request);
        
        // 2. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.ACTIVE);
        
        User savedUser = userRepository.save(user);
        
        // 3. 发布用户注册事件
        UserRegisteredEvent event = new UserRegisteredEvent(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail()
        );
        
        eventBus.publish(event);
        
        log.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }
    
    private void validateUserData(UserRegistrationRequest request) {
        // 验证逻辑
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
    }
}
```

#### 事件订阅者

```java
@Service
@Slf4j
public class NotificationService {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    // 同步处理 - 发送欢迎邮件
    @EventSubscribe(eventType = "user.registered")
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        try {
            emailService.sendWelcomeEmail(
                event.getEmail(), 
                event.getUsername()
            );
            log.info("Welcome email sent to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
            throw e; // 重新抛出异常以触发重试
        }
    }
    
    // 异步处理 - 发送SMS通知
    @EventSubscribe(
        eventType = "user.registered", 
        async = true,
        maxRetries = 3,
        retryDelay = 2000
    )
    public void sendSmsNotification(UserRegisteredEvent event) {
        try {
            smsService.sendRegistrationSms(event.getUserId());
            log.info("SMS notification sent for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send SMS notification", e);
            throw e;
        }
    }
    
    // 条件订阅 - 只处理特定域名的邮箱
    @EventSubscribe(
        eventType = "user.registered",
        condition = "event.email.endsWith('@company.com')"
    )
    public void handleCompanyUserRegistration(UserRegisteredEvent event) {
        // 为公司邮箱用户提供特殊处理
        log.info("Company user registered: {}", event.getUsername());
    }
}
```

### 7.2 电商订单处理示例

这是一个完整的电商订单处理流程示例，展示了事件驱动架构在复杂业务场景中的应用。

#### 事件定义

```java
// 订单创建事件
public class OrderCreatedEvent implements Event {
    private final String id;
    private final String orderId;
    private final String userId;
    private final BigDecimal amount;
    private final List<OrderItem> items;
    private final long timestamp;
    
    // 构造函数和getter方法...
}

// 库存预留事件
public class InventoryReservedEvent implements Event {
    private final String orderId;
    private final Map<String, Integer> reservedItems;
    // 其他字段和方法...
}

// 支付处理事件
public class PaymentProcessedEvent implements Event {
    private final String orderId;
    private final String paymentId;
    private final PaymentStatus status;
    private final BigDecimal amount;
    // 其他字段和方法...
}
```

#### 订单服务

```java
@Service
@EventPublish
@Transactional
@Slf4j
public class OrderService {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public Order createOrder(CreateOrderRequest request) {
        // 1. 创建订单
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(request.getItems());
        order.setAmount(calculateTotalAmount(request.getItems()));
        order.setStatus(OrderStatus.CREATED);
        
        Order savedOrder = orderRepository.save(order);
        
        // 2. 发布订单创建事件
        OrderCreatedEvent event = new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getUserId(),
            savedOrder.getAmount(),
            savedOrder.getItems()
        );
        
        eventBus.publish(event);
        
        log.info("Order created: {}", savedOrder.getId());
        return savedOrder;
    }
    
    @EventSubscribe(eventType = "inventory.reserved")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        // 库存预留成功，更新订单状态
        Order order = orderRepository.findById(event.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));
            
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);
        
        // 触发支付处理
        eventBus.publish(new PaymentRequiredEvent(order.getId(), order.getAmount()));
    }
    
    @EventSubscribe(eventType = "payment.processed")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));
            
        if (event.getStatus() == PaymentStatus.SUCCESS) {
            order.setStatus(OrderStatus.PAID);
            eventBus.publish(new OrderPaidEvent(order.getId()));
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            eventBus.publish(new PaymentFailedEvent(order.getId(), event.getFailureReason()));
        }
        
        orderRepository.save(order);
    }
    
    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

#### 库存服务

```java
@Service
@Slf4j
public class InventoryService {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @EventSubscribe(
        eventType = "order.created",
        maxRetries = 3,
        retryDelay = 1000
    )
    public void reserveInventory(OrderCreatedEvent event) {
        try {
            Map<String, Integer> reservedItems = new HashMap<>();
            
            for (OrderItem item : event.getItems()) {
                boolean reserved = inventoryRepository.reserveItem(
                    item.getProductId(), 
                    item.getQuantity()
                );
                
                if (!reserved) {
                    // 回滚已预留的库存
                    rollbackReservation(reservedItems);
                    throw new InsufficientInventoryException(
                        "Insufficient inventory for product: " + item.getProductId()
                    );
                }
                
                reservedItems.put(item.getProductId(), item.getQuantity());
            }
            
            // 发布库存预留成功事件
            eventBus.publish(new InventoryReservedEvent(
                event.getOrderId(), 
                reservedItems
            ));
            
            log.info("Inventory reserved for order: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}", event.getOrderId(), e);
            
            // 发布库存预留失败事件
            eventBus.publish(new InventoryReservationFailedEvent(
                event.getOrderId(), 
                e.getMessage()
            ));
            
            throw e;
        }
    }
    
    @EventSubscribe(eventType = "payment.failed")
    public void releaseReservation(PaymentFailedEvent event) {
        // 支付失败，释放库存预留
        inventoryRepository.releaseReservation(event.getOrderId());
        log.info("Released inventory reservation for order: {}", event.getOrderId());
    }
    
    private void rollbackReservation(Map<String, Integer> reservedItems) {
        for (Map.Entry<String, Integer> entry : reservedItems.entrySet()) {
            inventoryRepository.releaseItem(entry.getKey(), entry.getValue());
        }
    }
}
```

#### 支付服务

```java
@Service
@Slf4j
public class PaymentService {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private PaymentGateway paymentGateway;
    
    @EventSubscribe(
        eventType = "payment.required",
        async = true,
        maxRetries = 3,
        retryDelay = 2000
    )
    public void processPayment(PaymentRequiredEvent event) {
        try {
            // 调用支付网关
            PaymentResult result = paymentGateway.charge(
                event.getOrderId(),
                event.getAmount()
            );
            
            // 发布支付处理结果事件
            PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                event.getOrderId(),
                result.getPaymentId(),
                result.getStatus(),
                event.getAmount()
            );
            
            if (result.getStatus() == PaymentStatus.FAILED) {
                processedEvent.setFailureReason(result.getFailureReason());
            }
            
            eventBus.publish(processedEvent);
            
            log.info("Payment processed for order: {}, status: {}", 
                event.getOrderId(), result.getStatus());
                
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", event.getOrderId(), e);
            
            // 发布支付失败事件
            eventBus.publish(new PaymentProcessedEvent(
                event.getOrderId(),
                null,
                PaymentStatus.FAILED,
                event.getAmount(),
                e.getMessage()
            ));
            
            throw e;
        }
    }
}
```

### 7.3 分布式事件处理示例

#### 跨服务事件通信

```java
// 用户服务 - 事件发布者
@Service
@EventPublish
public class UserService {
    
    @Autowired
    private EventBus distributedEventBus;
    
    public void updateUserProfile(String userId, UserProfile profile) {
        // 更新用户资料
        userRepository.updateProfile(userId, profile);
        
        // 发布分布式事件，通知其他服务
        UserProfileUpdatedEvent event = new UserProfileUpdatedEvent(
            userId, profile
        );
        
        // 广播到所有服务
        distributedEventBus.publish(event);
        
        // 或者发送到特定服务
        event.setTargetNodeId("order-service");
        distributedEventBus.publish(event);
    }
}

// 订单服务 - 事件订阅者
@Service
public class OrderUserInfoService {
    
    @EventSubscribe(eventType = "user.profile.updated")
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        // 更新订单中的用户信息缓存
        orderUserInfoCache.updateUserInfo(
            event.getUserId(), 
            event.getProfile()
        );
        
        log.info("Updated user info cache for user: {}", event.getUserId());
    }
}
```

#### Kafka配置

```java
@Configuration
@ConditionalOnProperty("atlas.event.kafka.enabled")
public class KafkaEventConfiguration {
    
    @Bean
    public EventBus kafkaDistributedEventBus(
            @Qualifier("localEventBus") EventBus localEventBus,
            AtlasEventProperties properties) {
        
        var kafkaConfig = properties.getKafka();
        
        return EventBusFactory.createKafkaDistributedEventBus(
            localEventBus,
            kafkaConfig.getBootstrapServers(),
            kafkaConfig.getTopic(),
            kafkaConfig.getGroupId()
        );
    }
    
    @Bean
    @Primary
    public EventBus eventBus(
            @Qualifier("kafkaDistributedEventBus") EventBus kafkaEventBus) {
        return kafkaEventBus;
    }
}
```

## 8. 高级特性

### 8.1 事件持久化

#### 数据库持久化

```java
@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    private String id;
    
    @Column(name = "event_type")
    private String type;
    
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String data;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    
    // getters and setters...
}

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {
    List<EventEntity> findByTypeAndStatus(String type, EventStatus status);
    List<EventEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}

@Service
public class DatabaseEventPersistence implements EventPersistence {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void save(Event event) {
        try {
            EventEntity entity = new EventEntity();
            entity.setId(event.getId());
            entity.setType(event.getType());
            entity.setData(objectMapper.writeValueAsString(event));
            entity.setTimestamp(LocalDateTime.now());
            entity.setStatus(EventStatus.SAVED);
            
            eventRepository.save(entity);
        } catch (Exception e) {
            throw new EventPersistenceException("Failed to save event", e);
        }
    }
    
    @Override
    public Event load(String eventId) {
        return eventRepository.findById(eventId)
            .map(this::deserializeEvent)
            .orElse(null);
    }
    
    private Event deserializeEvent(EventEntity entity) {
        try {
            // 根据事件类型反序列化
            Class<?> eventClass = getEventClass(entity.getType());
            return (Event) objectMapper.readValue(entity.getData(), eventClass);
        } catch (Exception e) {
            throw new EventPersistenceException("Failed to deserialize event", e);
        }
    }
}
```

#### 事件重放

```java
@Service
public class EventReplayService {
    
    @Autowired
    private EventPersistence eventPersistence;
    
    @Autowired
    private EventBus eventBus;
    
    public void replayEvents(String eventType, LocalDateTime from, LocalDateTime to) {
        List<Event> events = eventPersistence.loadByTypeAndTimeRange(
            eventType, from, to
        );
        
        events.stream()
            .sorted(Comparator.comparing(Event::getTimestamp))
            .forEach(event -> {
                try {
                    eventBus.publish(event);
                    log.info("Replayed event: {}", event.getId());
                } catch (Exception e) {
                    log.error("Failed to replay event: {}", event.getId(), e);
                }
            });
    }
    
    public void replayEventsForOrder(String orderId) {
        // 重放特定订单的所有事件
        List<Event> orderEvents = eventPersistence.loadByOrderId(orderId);
        orderEvents.forEach(eventBus::publish);
    }
}
```

### 8.2 事件监控和指标

#### 指标收集

```java
@Component
public class EventMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter eventPublishedCounter;
    private final Counter eventProcessedCounter;
    private final Timer eventProcessingTimer;
    
    public EventMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.eventPublishedCounter = Counter.builder("events.published")
            .description("Number of events published")
            .register(meterRegistry);
        this.eventProcessedCounter = Counter.builder("events.processed")
            .description("Number of events processed")
            .register(meterRegistry);
        this.eventProcessingTimer = Timer.builder("events.processing.time")
            .description("Event processing time")
            .register(meterRegistry);
    }
    
    @EventSubscribe(eventType = "*") // 监听所有事件
    public void collectMetrics(Event event) {
        eventProcessedCounter.increment(
            Tags.of("event.type", event.getType())
        );
    }
    
    public void recordEventPublished(String eventType) {
        eventPublishedCounter.increment(
            Tags.of("event.type", eventType)
        );
    }
    
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordProcessingTime(Timer.Sample sample, String eventType) {
        sample.stop(eventProcessingTimer.tag("event.type", eventType));
    }
}
```

#### 健康检查

```java
@Component
public class EventBusHealthIndicator implements HealthIndicator {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private EventPersistence eventPersistence;
    
    @Override
    public Health health() {
        try {
            // 检查EventBus状态
            if (!isEventBusHealthy()) {
                return Health.down()
                    .withDetail("eventBus", "EventBus is not responding")
                    .build();
            }
            
            // 检查持久化状态
            if (!isPersistenceHealthy()) {
                return Health.down()
                    .withDetail("persistence", "Event persistence is not available")
                    .build();
            }
            
            return Health.up()
                .withDetail("eventBus", "OK")
                .withDetail("persistence", "OK")
                .build();
                
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private boolean isEventBusHealthy() {
        try {
            // 发送健康检查事件
            HealthCheckEvent event = new HealthCheckEvent();
            eventBus.publish(event);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isPersistenceHealthy() {
        try {
            // 测试持久化读写
            Event testEvent = new TestEvent();
            eventPersistence.save(testEvent);
            Event loaded = eventPersistence.load(testEvent.getId());
            return loaded != null;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 8.3 事件安全

#### 事件加密

```java
@Component
public class EventEncryptionService {
    
    @Value("${atlas.event.encryption.key}")
    private String encryptionKey;
    
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(), "AES"
            );
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new EventEncryptionException("Failed to encrypt event data", e);
        }
    }
    
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(), "AES"
            );
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(
                Base64.getDecoder().decode(encryptedData)
            );
            return new String(decrypted);
        } catch (Exception e) {
            throw new EventEncryptionException("Failed to decrypt event data", e);
        }
    }
}

// 加密事件实现
public class EncryptedEvent implements Event {
    private final String id;
    private final String type;
    private final String encryptedData;
    private final long timestamp;
    
    // 构造函数和方法...
}
```

#### 事件签名验证

```java
@Component
public class EventSignatureService {
    
    @Value("${atlas.event.signature.secret}")
    private String signatureSecret;
    
    public String generateSignature(Event event) {
        try {
            String payload = event.getId() + event.getType() + event.getTimestamp();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                signatureSecret.getBytes(), "HmacSHA256"
            );
            mac.init(keySpec);
            
            byte[] signature = mac.doFinal(payload.getBytes());
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new EventSignatureException("Failed to generate signature", e);
        }
    }
    
    public boolean verifySignature(Event event, String signature) {
        String expectedSignature = generateSignature(event);
        return MessageDigest.isEqual(
            expectedSignature.getBytes(),
            signature.getBytes()
        );
    }
}
```

## 9. 性能调优

### 9.1 线程池优化

#### 自定义线程池配置

```java
@Configuration
public class ThreadPoolConfiguration {
    
    @Bean("eventProcessingExecutor")
    public ThreadPoolTaskExecutor eventProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数 = CPU核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        
        // 最大线程数 = CPU核心数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        
        // 队列容量
        executor.setQueueCapacity(1000);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀
        executor.setThreadNamePrefix("event-processing-");
        
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
    
    @Bean
    public EventBus optimizedAsyncEventBus(
            @Qualifier("eventProcessingExecutor") Executor executor) {
        return EventBusFactory.createAsyncEventBus((ExecutorService) executor);
    }
}
```

#### 动态线程池调整

```java
@Component
@Slf4j
public class DynamicThreadPoolManager {
    
    @Autowired
    @Qualifier("eventProcessingExecutor")
    private ThreadPoolTaskExecutor executor;
    
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void adjustThreadPool() {
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        
        int activeCount = threadPool.getActiveCount();
        int corePoolSize = threadPool.getCorePoolSize();
        int maximumPoolSize = threadPool.getMaximumPoolSize();
        int queueSize = threadPool.getQueue().size();
        
        double utilization = (double) activeCount / corePoolSize;
        
        log.debug("Thread pool stats - Active: {}, Core: {}, Max: {}, Queue: {}, Utilization: {:.2f}",
            activeCount, corePoolSize, maximumPoolSize, queueSize, utilization);
        
        // 高负载时增加线程
        if (utilization > 0.8 && queueSize > 100 && corePoolSize < maximumPoolSize) {
            int newCoreSize = Math.min(corePoolSize + 2, maximumPoolSize);
            threadPool.setCorePoolSize(newCoreSize);
            log.info("Increased core pool size to: {}", newCoreSize);
        }
        
        // 低负载时减少线程
        else if (utilization < 0.3 && queueSize == 0 && corePoolSize > 2) {
            int newCoreSize = Math.max(corePoolSize - 1, 2);
            threadPool.setCorePoolSize(newCoreSize);
            log.info("Decreased core pool size to: {}", newCoreSize);
        }
    }
}
```

### 9.2 批处理优化

#### 批量事件处理

```java
@Component
public class BatchEventProcessor {
    
    private final List<Event> eventBuffer = new ArrayList<>();
    private final Object bufferLock = new Object();
    
    @Value("${atlas.event.batch.size:100}")
    private int batchSize;
    
    @Value("${atlas.event.batch.timeout:5000}")
    private long batchTimeout;
    
    @Autowired
    private EventPersistence eventPersistence;
    
    @EventSubscribe(eventType = "*")
    public void bufferEvent(Event event) {
        synchronized (bufferLock) {
            eventBuffer.add(event);
            
            if (eventBuffer.size() >= batchSize) {
                processBatch();
            }
        }
    }
    
    @Scheduled(fixedDelay = 5000) // 每5秒检查一次
    public void flushBuffer() {
        synchronized (bufferLock) {
            if (!eventBuffer.isEmpty()) {
                processBatch();
            }
        }
    }
    
    private void processBatch() {
        if (eventBuffer.isEmpty()) {
            return;
        }
        
        List<Event> batch = new ArrayList<>(eventBuffer);
        eventBuffer.clear();
        
        try {
            // 批量持久化
            eventPersistence.saveBatch(batch);
            log.debug("Processed batch of {} events", batch.size());
        } catch (Exception e) {
            log.error("Failed to process event batch", e);
            // 重新加入缓冲区或发送到死信队列
            handleBatchFailure(batch);
        }
    }
    
    private void handleBatchFailure(List<Event> failedBatch) {
        // 实现失败处理逻辑
    }
}
```

### 9.3 内存优化

#### 事件对象池

```java
@Component
public class EventObjectPool {
    
    private final Map<Class<?>, Queue<Event>> pools = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T extends Event> T borrowEvent(Class<T> eventClass) {
        Queue<Event> pool = pools.computeIfAbsent(eventClass, k -> new ConcurrentLinkedQueue<>());
        
        T event = (T) pool.poll();
        if (event == null) {
            try {
                event = eventClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create event instance", e);
            }
        }
        
        return event;
    }
    
    public void returnEvent(Event event) {
        if (event instanceof Poolable) {
            ((Poolable) event).reset();
            Queue<Event> pool = pools.get(event.getClass());
            if (pool != null && pool.size() < 100) { // 限制池大小
                pool.offer(event);
            }
        }
    }
}

// 可池化事件接口
public interface Poolable {
    void reset(); // 重置事件状态
}
```

## 10. 测试指南

### 10.1 单元测试

#### 事件发布测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldPublishEventWhenUserRegistered() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest(
            "testuser", "test@example.com"
        );
        
        User savedUser = new User("user-1", "testuser", "test@example.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        userService.registerUser(request);
        
        // Then
        ArgumentCaptor<UserRegisteredEvent> eventCaptor = 
            ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        UserRegisteredEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getUserId()).isEqualTo("user-1");
        assertThat(capturedEvent.getUsername()).isEqualTo("testuser");
        assertThat(capturedEvent.getEmail()).isEqualTo("test@example.com");
        assertThat(capturedEvent.getType()).isEqualTo("user.registered");
    }
}
```

#### 事件订阅测试

```java
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    @Test
    void shouldSendWelcomeEmailWhenUserRegistered() {
        // Given
        UserRegisteredEvent event = new UserRegisteredEvent(
            "user-1", "testuser", "test@example.com"
        );
        
        // When
        notificationService.sendWelcomeEmail(event);
        
        // Then
        verify(emailService).sendWelcomeEmail("test@example.com", "testuser");
    }
    
    @Test
    void shouldRetryOnEmailFailure() {
        // Given
        UserRegisteredEvent event = new UserRegisteredEvent(
            "user-1", "testuser", "test@example.com"
        );
        
        when(emailService.sendWelcomeEmail(anyString(), anyString()))
            .thenThrow(new EmailServiceException("Service unavailable"));
        
        // When & Then
        assertThatThrownBy(() -> notificationService.sendWelcomeEmail(event))
            .isInstanceOf(EmailServiceException.class)
            .hasMessage("Service unavailable");
    }
}
```

### 10.2 集成测试

#### Spring Boot 集成测试

```java
@SpringBootTest
@TestPropertySource(properties = {
    "atlas.event.enabled=true",
    "atlas.event.event-bus.type=async",
    "atlas.event.persistence.enabled=false"
})
class EventIntegrationTest {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private EmailService emailService;
    
    @Test
    void shouldProcessEventEndToEnd() throws InterruptedException {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest(
            "testuser", "test@example.com"
        );
        
        // When
        userService.registerUser(request);
        
        // Then - 等待异步处理完成
        Thread.sleep(1000);
        verify(emailService, timeout(2000)).sendWelcomeEmail(
            "test@example.com", "testuser"
        );
    }
}
```

#### 测试配置

```java
@TestConfiguration
public class EventTestConfiguration {
    
    @Bean
    @Primary
    public EventBus testEventBus() {
        // 使用同步EventBus便于测试
        return EventBusFactory.createSyncEventBus();
    }
    
    @Bean
    public TestEventCollector testEventCollector() {
        return new TestEventCollector();
    }
}

@Component
public class TestEventCollector {
    private final List<Event> collectedEvents = new ArrayList<>();
    
    @EventSubscribe(eventType = "*")
    public void collectEvent(Event event) {
        collectedEvents.add(event);
    }
    
    public List<Event> getCollectedEvents() {
        return new ArrayList<>(collectedEvents);
    }
    
    public void clear() {
        collectedEvents.clear();
    }
}
```

### 10.3 性能测试

#### 负载测试

```java
@SpringBootTest
class EventPerformanceTest {
    
    @Autowired
    private EventBus eventBus;
    
    @Test
    void shouldHandleHighVolumeEvents() throws InterruptedException {
        int eventCount = 10000;
        CountDownLatch latch = new CountDownLatch(eventCount);
        
        // 注册计数器
        eventBus.register(new Object() {
            @EventSubscribe(eventType = "performance.test")
            public void handleEvent(PerformanceTestEvent event) {
                latch.countDown();
            }
        });
        
        // 发布大量事件
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < eventCount; i++) {
            eventBus.publish(new PerformanceTestEvent("test-" + i));
        }
        
        // 等待所有事件处理完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        assertThat(completed).isTrue();
        
        long duration = endTime - startTime;
        double eventsPerSecond = (double) eventCount / (duration / 1000.0);
        
        log.info("Processed {} events in {}ms ({:.2f} events/sec)", 
            eventCount, duration, eventsPerSecond);
        
        // 性能断言
        assertThat(eventsPerSecond).isGreaterThan(1000); // 至少1000事件/秒
    }
}
```

## 11. 部署指南

### 11.1 Docker 部署

#### Dockerfile

```dockerfile
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 复制应用JAR
COPY target/atlas-event-sample-0.0.1.jar app.jar

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Docker Compose

```yaml
version: '3.8'

services:
  atlas-event-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - ATLAS_EVENT_KAFKA_ENABLED=true
      - ATLAS_EVENT_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - kafka
    networks:
      - atlas-network

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: atlas_events
      POSTGRES_USER: atlas
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - atlas-network

  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
    networks:
      - atlas-network

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - atlas-network

volumes:
  postgres_data:

networks:
  atlas-network:
    driver: bridge
```

### 11.2 Kubernetes 部署

#### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: atlas-event-app
  labels:
    app: atlas-event
spec:
  replicas: 3
  selector:
    matchLabels:
      app: atlas-event
  template:
    metadata:
      labels:
        app: atlas-event
    spec:
      containers:
      - name: atlas-event
        image: atlas-event:0.0.1
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: ATLAS_EVENT_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

#### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: atlas-event-service
spec:
  selector:
    app: atlas-event
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 11.3 监控配置

#### Prometheus 配置

```yaml
scrape_configs:
  - job_name: 'atlas-event'
    static_configs:
      - targets: ['atlas-event-service:80']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

#### Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Atlas Event Framework",
    "panels": [
      {
        "title": "Events Published",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(events_published_total[5m])",
            "legendFormat": "{{event_type}}"
          }
        ]
      },
      {
        "title": "Event Processing Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(events_processing_time_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      }
    ]
  }
}
```

## 12. 故障排除

### 12.1 常见问题

#### 问题1：事件未被处理

**症状**：发布事件后，订阅者方法未被调用

**可能原因**：
1. 订阅者类未被Spring管理
2. 事件类型不匹配
3. EventBus未正确注入

**解决方案**：
```java
// 确保订阅者类有@Component注解
@Component // 必须有此注解
public class MyEventSubscriber {
    
    @EventSubscribe(eventType = "my.event") // 确保类型匹配
    public void handleEvent(MyEvent event) {
        // 处理逻辑
    }
}

// 确保事件发布者正确注入EventBus
@Service
@EventPublish
public class MyEventPublisher {
    
    @Autowired // 确保正确注入
    private EventBus eventBus;
}
```

#### 问题2：异步事件处理缓慢

**症状**：异步事件处理延迟很高

**解决方案**：
```yaml
# 调整线程池配置
atlas:
  event:
    event-bus:
      async:
        core-pool-size: 10    # 增加核心线程数
        max-pool-size: 50     # 增加最大线程数
        queue-capacity: 200   # 调整队列容量
```

#### 问题3：内存泄漏

**症状**：应用运行一段时间后内存持续增长

**可能原因**：
1. 事件对象未被正确回收
2. 事件监听器未正确注销

**解决方案**：
```java
@PreDestroy
public void cleanup() {
    // 确保在应用关闭时注销监听器
    eventBus.unregister(this);
}

// 使用弱引用避免内存泄漏
public class WeakEventSubscriber {
    private final WeakReference<EventHandler> handlerRef;
    
    public WeakEventSubscriber(EventHandler handler) {
        this.handlerRef = new WeakReference<>(handler);
    }
}
```

### 12.2 调试技巧

#### 启用调试日志

```yaml
logging:
  level:
    io.github.nemoob.event: DEBUG
    org.springframework.context.event: DEBUG
```

#### 事件追踪

```java
@Component
public class EventTracer {
    
    @EventSubscribe(eventType = "*")
    public void traceEvent(Event event) {
        log.debug("Event traced: {} - {} at {}", 
            event.getType(), event.getId(), 
            Instant.ofEpochMilli(event.getTimestamp()));
    }
}
```

## 13. 贡献指南

### 13.1 开发环境设置

```bash
# 1. Fork 项目
git clone https://github.com/your-username/atlas-event.git
cd atlas-event

# 2. 创建开发分支
git checkout -b feature/your-feature-name

# 3. 安装依赖
mvn clean install

# 4. 运行测试
mvn test

# 5. 启动示例应用
cd atlas-event-sample
mvn spring-boot:run
```

### 13.2 代码规范

#### Java 代码风格

```java
// 使用标准的Java命名约定
public class EventProcessor {  // 类名：PascalCase
    
    private final EventBus eventBus;  // 字段：camelCase
    
    public void processEvent(Event event) {  // 方法：camelCase
        // 方法体
    }
    
    private static final String CONSTANT_VALUE = "value";  // 常量：UPPER_SNAKE_CASE
}
```

#### 提交信息格式

```
type(scope): description

[optional body]

[optional footer]
```

示例：
```
feat(eventbus): add adaptive thread pool implementation

Implemented AdaptiveThreadPoolEventBus that automatically
adjusts thread pool size based on system load.

Closes #123
```

### 13.3 测试要求

- 新功能必须包含单元测试
- 测试覆盖率不低于80%
- 集成测试覆盖主要使用场景
- 性能测试验证关键指标

### 13.4 文档要求

- 公共API必须包含Javadoc
- 新功能需要更新用户文档
- 重大变更需要更新迁移指南

---

## 📞 获取帮助

- **GitHub Issues**: [报告问题](https://github.com/nemoob/atlas-event/issues)
- **GitHub Discussions**: [讨论交流](https://github.com/nemoob/atlas-event/discussions)
- **文档**: [在线文档](https://github.com/nemoob/atlas-event/wiki)

---

<div align="center">

**感谢使用 Atlas Event Framework！**

如果这个项目对你有帮助，请给我们一个 ⭐️

</div>