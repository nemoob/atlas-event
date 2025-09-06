# 电子商务平台的事件驱动架构实践

## 目录

- [1. 引言](#1-引言)
- [2. 电商平台中的事件驱动场景](#2-电商平台中的事件驱动场景)
  - [2.1 订单生命周期管理](#21-订单生命周期管理)
  - [2.2 库存管理与实时更新](#22-库存管理与实时更新)
  - [2.3 用户行为分析与个性化推荐](#23-用户行为分析与个性化推荐)
  - [2.4 促销活动与价格调整](#24-促销活动与价格调整)
- [3. 框架核心功能实现](#3-框架核心功能实现)
  - [3.1 领域事件模型设计](#31-领域事件模型设计)
  - [3.2 事件优先级与顺序保证](#32-事件优先级与顺序保证)
  - [3.3 事件过滤与条件处理](#33-事件过滤与条件处理)
- [4. 完整代码示例](#4-完整代码示例)
  - [4.1 订单创建与状态变更](#41-订单创建与状态变更)
  - [4.2 库存扣减与补偿机制](#42-库存扣减与补偿机制)
  - [4.3 用户行为事件收集](#43-用户行为事件收集)
- [5. 性能优化策略](#5-性能优化策略)
  - [5.1 高并发场景下的事件处理](#51-高并发场景下的事件处理)
  - [5.2 热点商品的事件缓存](#52-热点商品的事件缓存)
  - [5.3 峰值流量应对策略](#53-峰值流量应对策略)
- [6. 常见问题与解决方案](#6-常见问题与解决方案)
  - [6.1 订单状态不一致问题](#61-订单状态不一致问题)
  - [6.2 库存超卖与少卖](#62-库存超卖与少卖)
  - [6.3 事件风暴与系统降级](#63-事件风暴与系统降级)
- [7. 最佳实践与配置指南](#7-最佳实践与配置指南)
  - [7.1 电商平台事件模型规范](#71-电商平台事件模型规范)
  - [7.2 多渠道事件整合策略](#72-多渠道事件整合策略)
  - [7.3 全链路监控配置](#73-全链路监控配置)
- [8. 总结与展望](#8-总结与展望)

## 1. 引言

电子商务平台是现代商业的核心基础设施，其复杂性和高并发特性对系统架构提出了严峻挑战。传统的单体架构或简单的微服务架构在面对电商业务的快速迭代、促销活动的流量峰值、以及全渠道整合的需求时，往往显得力不从心。

事件驱动架构（Event-Driven Architecture, EDA）为电商平台提供了一种更为灵活、可扩展的解决方案。通过将业务流程分解为一系列事件，系统各组件可以独立响应这些事件，从而实现松耦合、高并发和实时响应的特性。

本文将深入探讨如何在电子商务平台中实践事件驱动架构，从领域事件建模到具体实现，再到性能优化和问题解决，提供一套完整的实施指南。

## 2. 电商平台中的事件驱动场景

### 2.1 订单生命周期管理

订单是电商平台的核心业务实体，其生命周期涉及多个系统和业务流程。采用事件驱动架构可以有效管理订单的各个状态变更：

**订单生命周期中的关键事件：**

1. **订单创建事件**：用户下单后触发，包含订单基本信息和商品明细
2. **支付状态变更事件**：订单支付成功或失败时触发
3. **库存确认事件**：确认商品库存是否充足
4. **订单确认事件**：所有前置条件满足，订单正式生效
5. **物流状态变更事件**：包括出库、配送中、已送达等状态
6. **订单完成事件**：用户确认收货，订单完成
7. **订单取消事件**：用户取消订单或系统自动取消
8. **退换货事件**：用户申请退换货及后续处理

**事件驱动的优势：**

- **业务解耦**：订单服务只负责订单状态管理，不直接调用支付、库存、物流等服务
- **可追溯性**：通过事件序列可以完整重建订单历史，便于问题排查和审计
- **灵活扩展**：新增业务流程（如新的支付方式、配送方式）只需订阅相关事件，无需修改现有逻辑

### 2.2 库存管理与实时更新

库存管理是电商平台的关键挑战，尤其在高并发场景下。事件驱动架构可以提供更灵活的库存管理机制：

**库存相关的关键事件：**

1. **库存扣减事件**：订单确认时触发，减少可用库存
2. **库存释放事件**：订单取消或超时未支付时触发，恢复可用库存
3. **库存预警事件**：库存低于阈值时触发，提醒补货
4. **入库事件**：新商品入库或补货时触发
5. **库存调整事件**：手动调整库存或盘点差异时触发

**事件驱动的优势：**

- **实时性**：库存变更立即通知相关系统，如商品详情页、搜索系统等
- **一致性**：通过事件溯源可以准确追踪库存变更历史，确保账实一致
- **弹性**：高峰期可以采用异步处理非关键库存操作，保证系统稳定性

### 2.3 用户行为分析与个性化推荐

用户在电商平台的每一次交互都是宝贵的数据，通过事件驱动架构可以高效收集和处理这些行为数据：

**用户行为相关的关键事件：**

1. **页面浏览事件**：用户浏览商品、分类、活动页面等
2. **搜索事件**：用户搜索关键词及结果交互
3. **加入购物车事件**：用户将商品加入购物车
4. **收藏事件**：用户收藏商品或店铺
5. **评价事件**：用户对商品或服务进行评价
6. **分享事件**：用户分享商品到社交媒体

**事件驱动的优势：**

- **实时分析**：行为事件可以实时流入分析系统，支持实时个性化推荐
- **解耦采集**：前端只需发布事件，不需要关心数据如何被处理和使用
- **多维应用**：同一事件可以被多个系统消费，如推荐系统、营销系统、用户画像系统等

### 2.4 促销活动与价格调整

电商平台的促销活动通常涉及复杂的规则和时效性要求，事件驱动架构可以提供更灵活的促销管理：

**促销相关的关键事件：**

1. **活动创建事件**：新促销活动创建，包含活动规则、时间范围等
2. **活动开始/结束事件**：活动正式开始或结束时触发
3. **价格调整事件**：商品价格变更，可能是因为促销、清仓、调价等
4. **优惠券发放事件**：系统发放优惠券给用户
5. **优惠券使用/过期事件**：用户使用优惠券或优惠券过期

**事件驱动的优势：**

- **实时生效**：活动规则变更可以实时通知到各相关系统
- **精准控制**：通过事件可以精确控制活动的开始和结束
- **灵活组合**：不同促销规则可以通过事件组合实现复杂的营销策略

## 3. 框架核心功能实现

### 3.1 领域事件模型设计

在电商系统中，设计良好的领域事件模型是实施事件驱动架构的基础。我们采用领域驱动设计（DDD）的思想，将业务领域中的关键事件进行建模：

```java
// 1. 领域事件基类
public abstract class DomainEvent extends AbstractEvent {
    private final String aggregateId; // 聚合根ID，如订单ID、商品ID等
    private final long timestamp;
    private final String userId; // 触发事件的用户ID，可选
    private final Map<String, Object> metadata;
    
    protected DomainEvent(String aggregateId, String userId) {
        this.aggregateId = aggregateId;
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.metadata = new HashMap<>();
    }
    
    // Getters...
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
}

// 2. 订单领域事件
public abstract class OrderDomainEvent extends DomainEvent {
    private final String orderId;
    private final OrderStatus status;
    
    protected OrderDomainEvent(String orderId, OrderStatus status, String userId) {
        super(orderId, userId);
        this.orderId = orderId;
        this.status = status;
    }
    
    // Getters...
}

// 3. 具体订单事件
public class OrderCreatedEvent extends OrderDomainEvent {
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final Address shippingAddress;
    private final String paymentMethod;
    
    public OrderCreatedEvent(String orderId, String userId, List<OrderItem> items, 
                            BigDecimal totalAmount, Address shippingAddress, 
                            String paymentMethod) {
        super(orderId, OrderStatus.CREATED, userId);
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters...
    
    @Override
    public String getType() {
        return "order.created";
    }
}

// 4. 支付事件
public class OrderPaidEvent extends OrderDomainEvent {
    private final String paymentId;
    private final BigDecimal paidAmount;
    private final String paymentMethod;
    private final LocalDateTime paymentTime;
    
    public OrderPaidEvent(String orderId, String userId, String paymentId, 
                         BigDecimal paidAmount, String paymentMethod) {
        super(orderId, OrderStatus.PAID, userId);
        this.paymentId = paymentId;
        this.paidAmount = paidAmount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
    }
    
    // Getters...
    
    @Override
    public String getType() {
        return "order.paid";
    }
}

// 5. 库存事件
public class InventoryDeductedEvent extends DomainEvent {
    private final String productId;
    private final String skuId;
    private final int quantity;
    private final String orderId; // 关联的订单ID
    private final int remainingStock;
    
    public InventoryDeductedEvent(String skuId, int quantity, String orderId, int remainingStock) {
        super(skuId, null); // 库存变更可能不是由特定用户直接触发
        this.productId = skuId.split("_")[0]; // 假设skuId格式为productId_variant
        this.skuId = skuId;
        this.quantity = quantity;
        this.orderId = orderId;
        this.remainingStock = remainingStock;
    }
    
    // Getters...
    
    @Override
    public String getType() {
        return "inventory.deducted";
    }
}

// 6. 用户行为事件
public class UserBehaviorEvent extends DomainEvent {
    private final String eventType; // 具体行为类型，如view, search, add_to_cart等
    private final String targetId; // 目标对象ID，如商品ID
    private final String targetType; // 目标类型，如product, category, shop等
    private final Map<String, Object> properties; // 行为的附加属性
    private final String sessionId;
    private final String deviceInfo;
    
    public UserBehaviorEvent(String userId, String eventType, String targetId, 
                            String targetType, Map<String, Object> properties,
                            String sessionId, String deviceInfo) {
        super(targetId, userId);
        this.eventType = eventType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.properties = properties != null ? new HashMap<>(properties) : new HashMap<>();
        this.sessionId = sessionId;
        this.deviceInfo = deviceInfo;
    }
    
    // Getters...
    
    @Override
    public String getType() {
        return "user.behavior." + eventType;
    }
}
```

### 3.2 事件优先级与顺序保证

在电商系统中，某些事件处理的顺序至关重要，例如库存扣减必须在订单确认之前完成。我们通过事件优先级和顺序保证机制来解决这个问题：

```java
// 1. 事件优先级注解
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EventPriority {
    int value() default 0; // 数值越小优先级越高
}

// 2. 优先级事件监听器注册表
public class PriorityListenerRegistry implements ListenerRegistry {
    private final Map<String, List<PriorityEventListener>> typeToListeners = new ConcurrentHashMap<>();
    
    @Override
    public <E extends Event> void register(EventListener<E> listener, Class<E> eventType) {
        String eventTypeName = resolveEventType(eventType);
        PriorityEventListener priorityListener = new PriorityEventListener(listener, resolveListenerPriority(listener));
        
        typeToListeners.computeIfAbsent(eventTypeName, k -> new CopyOnWriteArrayList<>())
                      .add(priorityListener);
        
        // 每次添加后重新排序
        sortListenersByPriority(eventTypeName);
    }
    
    @Override
    public <E extends Event> List<EventListener<E>> getListeners(String eventType) {
        List<PriorityEventListener> listeners = typeToListeners.getOrDefault(eventType, Collections.emptyList());
        return listeners.stream()
                       .map(pl -> (EventListener<E>) pl.getListener())
                       .collect(Collectors.toList());
    }
    
    private void sortListenersByPriority(String eventType) {
        List<PriorityEventListener> listeners = typeToListeners.get(eventType);
        if (listeners != null) {
            Collections.sort(listeners);
        }
    }
    
    private int resolveListenerPriority(EventListener<?> listener) {
        Class<?> listenerClass = listener.getClass();
        EventPriority annotation = listenerClass.getAnnotation(EventPriority.class);
        return annotation != null ? annotation.value() : 0;
    }
    
    // 其他辅助方法...
    
    // 优先级监听器包装类
    private static class PriorityEventListener implements Comparable<PriorityEventListener> {
        private final EventListener<?> listener;
        private final int priority;
        
        public PriorityEventListener(EventListener<?> listener, int priority) {
            this.listener = listener;
            this.priority = priority;
        }
        
        public EventListener<?> getListener() {
            return listener;
        }
        
        public int getPriority() {
            return priority;
        }
        
        @Override
        public int compareTo(PriorityEventListener other) {
            return Integer.compare(this.priority, other.priority);
        }
    }
}

// 3. 顺序保证的事件总线
public class OrderedEventBus implements EventBus {
    private final EventBus delegate;
    private final Map<String, BlockingQueue<Event>> orderKeyToQueue = new ConcurrentHashMap<>();
    private final Map<String, Thread> orderKeyToThread = new ConcurrentHashMap<>();
    
    public OrderedEventBus(EventBus delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public <E extends Event> void publish(E event) {
        // 检查事件是否需要顺序保证
        if (event instanceof OrderedEvent) {
            String orderKey = ((OrderedEvent) event).getOrderKey();
            publishOrdered(event, orderKey);
        } else {
            delegate.publish(event);
        }
    }
    
    private <E extends Event> void publishOrdered(E event, String orderKey) {
        // 获取或创建该orderKey的队列
        BlockingQueue<Event> queue = orderKeyToQueue.computeIfAbsent(orderKey, k -> {
            BlockingQueue<Event> newQueue = new LinkedBlockingQueue<>();
            // 为每个orderKey创建一个专用线程处理队列
            Thread processor = new Thread(() -> processQueue(newQueue, k));
            processor.setName("ordered-event-processor-" + k);
            processor.start();
            orderKeyToThread.put(k, processor);
            return newQueue;
        });
        
        // 将事件添加到队列
        try {
            queue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Interrupted while queueing ordered event", e);
        }
    }
    
    private void processQueue(BlockingQueue<Event> queue, String orderKey) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Event event = queue.take();
                try {
                    // 按顺序处理事件
                    delegate.publish(event);
                } catch (Exception e) {
                    log.error("Error processing ordered event: " + event.getId(), e);
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
    
    // 其他EventBus接口方法实现...
    
    // 关闭处理器
    public void shutdown() {
        for (Thread thread : orderKeyToThread.values()) {
            thread.interrupt();
        }
    }
    
    // 顺序事件接口
    public interface OrderedEvent extends Event {
        String getOrderKey();
    }
}
```

### 3.3 事件过滤与条件处理

电商系统中，不同类型的事件可能需要根据特定条件进行过滤或特殊处理，例如VIP用户的订单可能需要优先处理，或者某些促销活动的订单需要特殊逻辑：

```java
// 1. 事件过滤器接口
public interface EventFilter<E extends Event> {
    boolean shouldProcess(E event);
}

// 2. 条件事件监听器
public class ConditionalEventListener<E extends Event> implements EventListener<E> {
    private final EventListener<E> delegate;
    private final List<EventFilter<E>> filters;
    
    @SafeVarargs
    public ConditionalEventListener(EventListener<E> delegate, EventFilter<E>... filters) {
        this.delegate = delegate;
        this.filters = Arrays.asList(filters);
    }
    
    @Override
    public void onEvent(E event) {
        // 只有通过所有过滤器的事件才会被处理
        if (filters.stream().allMatch(filter -> filter.shouldProcess(event))) {
            delegate.onEvent(event);
        }
    }
}

// 3. 常用过滤器实现
// VIP用户过滤器
public class VipUserFilter implements EventFilter<DomainEvent> {
    private final UserService userService;
    
    @Override
    public boolean shouldProcess(DomainEvent event) {
        String userId = event.getUserId();
        if (userId == null) {
            return false;
        }
        return userService.isVipUser(userId);
    }
}

// 促销订单过滤器
public class PromotionOrderFilter implements EventFilter<OrderDomainEvent> {
    private final PromotionService promotionService;
    
    @Override
    public boolean shouldProcess(OrderDomainEvent event) {
        return promotionService.isPromotionOrder(event.getOrderId());
    }
}

// 4. 过滤器工厂，便于创建常用过滤器
public class EventFilters {
    public static EventFilter<DomainEvent> forVipUser(UserService userService) {
        return new VipUserFilter(userService);
    }
    
    public static EventFilter<OrderDomainEvent> forPromotionOrder(PromotionService promotionService) {
        return new PromotionOrderFilter(promotionService);
    }
    
    public static <E extends Event> EventFilter<E> and(EventFilter<E>... filters) {
        return event -> Arrays.stream(filters).allMatch(filter -> filter.shouldProcess(event));
    }
    
    public static <E extends Event> EventFilter<E> or(EventFilter<E>... filters) {
        return event -> Arrays.stream(filters).anyMatch(filter -> filter.shouldProcess(event));
    }
    
    public static <E extends Event> EventFilter<E> not(EventFilter<E> filter) {
        return event -> !filter.shouldProcess(event);
    }
}

// 5. 使用示例
@Component
public class VipOrderProcessor {
    private final EventBus eventBus;
    private final UserService userService;
    private final OrderService orderService;
    
    @PostConstruct
    public void init() {
        // 注册一个只处理VIP用户订单的监听器
        EventListener<OrderCreatedEvent> vipOrderListener = new ConditionalEventListener<>(
            this::processVipOrder,
            EventFilters.forVipUser(userService)
        );
        
        eventBus.register(vipOrderListener, OrderCreatedEvent.class);
    }
    
    private void processVipOrder(OrderCreatedEvent event) {
        // VIP订单特殊处理逻辑
        orderService.applyVipPrivileges(event.getOrderId());
    }
}
```

## 4. 完整代码示例

### 4.1 订单创建与状态变更

以下是电商平台中订单创建和状态变更的完整实现示例：

```java
// 1. 订单服务接口
public interface OrderService {
    Order createOrder(OrderRequest request);
    Order getOrder(String orderId);
    void updateOrderStatus(String orderId, OrderStatus newStatus);
    List<Order> getUserOrders(String userId, int page, int size);
    // 其他方法...
}

// 2. 订单服务实现
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final EventBus eventBus;
    private final TransactionalEventPublisher transactionalEventPublisher;
    
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, 
                           EventBus eventBus,
                           TransactionalEventPublisher transactionalEventPublisher) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
        this.transactionalEventPublisher = transactionalEventPublisher;
    }
    
    @Override
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. 创建订单实体
        Order order = new Order();
        order.setId(generateOrderId());
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setItems(convertToOrderItems(request.getItems()));
        order.setTotalAmount(calculateTotalAmount(order.getItems()));
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreatedAt(LocalDateTime.now());
        
        // 2. 保存订单
        Order savedOrder = orderRepository.save(order);
        
        // 3. 创建并发布订单创建事件（在同一事务中）
        OrderCreatedEvent event = new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getUserId(),
            savedOrder.getItems(),
            savedOrder.getTotalAmount(),
            savedOrder.getShippingAddress(),
            savedOrder.getPaymentMethod()
        );
        
        transactionalEventPublisher.publishWithTransaction(event);
        
        return savedOrder;
    }
    
    @Override
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        // 1. 获取订单
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        // 2. 验证状态转换是否有效
        validateStatusTransition(order.getStatus(), newStatus);
        
        // 3. 更新状态
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // 4. 创建并发布状态变更事件
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
            order.getId(),
            order.getUserId(),
            oldStatus,
            newStatus
        );
        
        transactionalEventPublisher.publishWithTransaction(event);
        
        // 5. 根据新状态发布特定事件
        publishSpecificStatusEvent(order, oldStatus, newStatus);
    }
    
    private void publishSpecificStatusEvent(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        switch (newStatus) {
            case PAID:
                // 发布支付成功事件
                PaymentInfo paymentInfo = order.getPaymentInfo();
                if (paymentInfo != null) {
                    OrderPaidEvent paidEvent = new OrderPaidEvent(
                        order.getId(),
                        order.getUserId(),
                        paymentInfo.getPaymentId(),
                        paymentInfo.getAmount(),
                        order.getPaymentMethod()
                    );
                    transactionalEventPublisher.publishWithTransaction(paidEvent);
                }
                break;
            case SHIPPED:
                // 发布发货事件
                ShippingInfo shippingInfo = order.getShippingInfo();
                if (shippingInfo != null) {
                    OrderShippedEvent shippedEvent = new OrderShippedEvent(
                        order.getId(),
                        order.getUserId(),
                        shippingInfo.getTrackingNumber(),
                        shippingInfo.getCarrier(),
                        order.getShippingAddress()
                    );
                    transactionalEventPublisher.publishWithTransaction(shippedEvent);
                }
                break;
            case CANCELLED:
                // 发布取消事件
                OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getCancellationReason()
                );
                transactionalEventPublisher.publishWithTransaction(cancelledEvent);
                break;
            // 其他状态...
        }
    }
    
    // 其他辅助方法...
}

// 3. 订单状态变更监听器
@Component
public class OrderStatusChangeListener {
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;
    private final IdempotentEventProcessor idempotentProcessor;
    
    @EventSubscribe
    @EventPriority(10) // 高优先级
    public void handleOrderPaid(OrderPaidEvent event) {
        idempotentProcessor.processIdempotently(event, e -> {
            // 1. 通知库存服务准备商品
            inventoryService.prepareItems(e.getOrderId());
            
            // 2. 通知用户支付成功
            notificationService.sendPaymentConfirmation(e.getOrderId(), e.getUserId());
            
            return null;
        });
    }
    
    @EventSubscribe
    public void handleOrderShipped(OrderShippedEvent event) {
        idempotentProcessor.processIdempotently(event, e -> {
            // 通知用户订单已发货
            notificationService.sendShippingNotification(
                e.getOrderId(),
                e.getUserId(),
                e.getTrackingNumber(),
                e.getCarrier()
            );
            return null;
        });
    }
    
    @EventSubscribe
    public void handleOrderCancelled(OrderCancelledEvent event) {
        idempotentProcessor.processIdempotently(event, e -> {
            // 1. 释放库存
            inventoryService.releaseItems(e.getOrderId());
            
            // 2. 如果已支付，发起退款
            if (paymentService.isOrderPaid(e.getOrderId())) {
                paymentService.refundOrder(e.getOrderId());
            }
            
            // 3. 通知用户订单已取消
            notificationService.sendOrderCancellationNotification(
                e.getOrderId(),
                e.getUserId(),
                e.getCancellationReason()
            );
            
            return null;
        });
    }
}
```

### 4.2 库存扣减与补偿机制

库存管理是电商系统的关键环节，需要保证在高并发下的一致性和可靠性：

```java
// 1. 库存服务接口
public interface InventoryService {
    boolean deductStock(String skuId, int quantity, String orderId);
    void releaseStock(String skuId, int quantity, String orderId);
    void prepareItems(String orderId);
    void releaseItems(String orderId);
    int getAvailableStock(String skuId);
    // 其他方法...
}

// 2. 库存服务实现
@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventBus eventBus;
    private final TransactionalEventPublisher transactionalEventPublisher;
    private final LockManager lockManager;
    
    @Override
    @Transactional
    public boolean deductStock(String skuId, int quantity, String orderId) {
        // 使用分布式锁防止并发问题
        return lockManager.executeWithLock("inventory:" + skuId, 10, TimeUnit.SECONDS, () -> {
            // 1. 查询当前库存
            Inventory inventory = inventoryRepository.findById(skuId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + skuId));
            
            // 2. 检查库存是否充足
            if (inventory.getAvailableStock() < quantity) {
                // 发布库存不足事件
                StockShortageEvent shortageEvent = new StockShortageEvent(
                    skuId,
                    quantity,
                    inventory.getAvailableStock(),
                    orderId
                );
                eventBus.publish(shortageEvent);
                return false;
            }
            
            // 3. 扣减库存
            inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
            
            // 4. 记录库存变更
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setSkuId(skuId);
            transaction.setQuantity(-quantity); // 负数表示扣减
            transaction.setOrderId(orderId);
            transaction.setType(InventoryTransactionType.DEDUCT);
            transaction.setCreatedAt(LocalDateTime.now());
            inventoryTransactionRepository.save(transaction);
            
            // 5. 发布库存扣减事件
            InventoryDeductedEvent event = new InventoryDeductedEvent(
                skuId,
                quantity,
                orderId,
                inventory.getAvailableStock()
            );
            
            transactionalEventPublisher.publishWithTransaction(event);
            
            // 6. 检查是否需要发布库存预警事件
            if (inventory.getAvailableStock() <= inventory.getWarningThreshold()) {
                StockWarningEvent warningEvent = new StockWarningEvent(
                    skuId,
                    inventory.getAvailableStock(),
                    inventory.getWarningThreshold()
                );
                transactionalEventPublisher.publishWithTransaction(warningEvent);
            }
            
            return true;
        });
    }
    
    @Override
    @Transactional
    public void releaseStock(String skuId, int quantity, String orderId) {
        lockManager.executeWithLock("inventory:" + skuId, 10, TimeUnit.SECONDS, () -> {
            // 1. 查询当前库存
            Inventory inventory = inventoryRepository.findById(skuId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + skuId));
            
            // 2. 增加库存
            inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
            
            // 3. 记录库存变更
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setSkuId(skuId);
            transaction.setQuantity(quantity); // 正数表示增加
            transaction.setOrderId(orderId);
            transaction.setType(InventoryTransactionType.RELEASE);
            transaction.setCreatedAt(LocalDateTime.now());
            inventoryTransactionRepository.save(transaction);
            
            // 4. 发布库存释放事件
            InventoryReleasedEvent event = new InventoryReleasedEvent(
                skuId,
                quantity,
                orderId,
                inventory.getAvailableStock()
            );
            
            transactionalEventPublisher.publishWithTransaction(event);
            
            return null;
        });
    }
    
    @Override
    @Transactional
    public void prepareItems(String orderId) {
        // 获取订单中的所有商品
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        
        // 标记这些商品为准备中
        for (OrderItem item : items) {
            item.setStatus(OrderItemStatus.PREPARING);
            orderItemRepository.save(item);
        }
        
        // 发布订单商品准备事件
        OrderItemsPreparingEvent event = new OrderItemsPreparingEvent(orderId, items);
        transactionalEventPublisher.publishWithTransaction(event);
    }
    
    @Override
    @Transactional
    public void releaseItems(String orderId) {
        // 获取订单中的所有商品
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        
        // 释放每个商品的库存
        for (OrderItem item : items) {
            releaseStock(item.getSkuId(), item.getQuantity(), orderId);
        }
    }
    
    // 其他方法实现...
}

// 3. 库存补偿机制
@Component
public class InventoryCompensationService {
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final EventBus eventBus;
    
    @EventSubscribe
    public void handleStockShortage(StockShortageEvent event) {
        // 1. 记录库存不足事件
        log.warn("Stock shortage detected for SKU: {} (requested: {}, available: {})",
                event.getSkuId(), event.getRequestedQuantity(), event.getAvailableStock());
        
        // 2. 尝试查找替代商品
        List<String> alternativeSkus = findAlternativeSkus(event.getSkuId());
        
        if (!alternativeSkus.isEmpty()) {
            // 3. 发布替代商品建议事件
            AlternativeProductsEvent alternativeEvent = new AlternativeProductsEvent(
                event.getOrderId(),
                event.getSkuId(),
                alternativeSkus
            );
            eventBus.publish(alternativeEvent);
        } else {
            // 4. 如果没有替代品，取消订单中的该商品
            orderService.removeOrderItem(event.getOrderId(), event.getSkuId());
            
            // 5. 通知用户库存不足
            Order order = orderService.getOrder(event.getOrderId());
            notificationService.sendStockShortageNotification(
                order.getUserId(),
                event.getSkuId(),
                event.getRequestedQuantity()
            );
        }
    }
    
    @EventSubscribe
    public void handleOrderCancellation(OrderCancelledEvent event) {
        // 确保释放库存
        inventoryService.releaseItems(event.getOrderId());
    }
    
    @Scheduled(fixedDelay = 3600000) // 每小时执行一次
    public void reconcileInventory() {
        // 库存对账逻辑
        List<InventoryDiscrepancy> discrepancies = findInventoryDiscrepancies();
        
        for (InventoryDiscrepancy discrepancy : discrepancies) {
            // 调整库存
            adjustInventory(discrepancy.getSkuId(), discrepancy.getAdjustmentQuantity());
            
            // 发布库存调整事件
            InventoryAdjustedEvent event = new InventoryAdjustedEvent(
                discrepancy.getSkuId(),
                discrepancy.getAdjustmentQuantity(),
                discrepancy.getReason()
            );
            eventBus.publish(event);
        }
    }
    
    // 辅助方法...
}
```

### 4.3 用户行为事件收集

收集和处理用户行为事件对于个性化推荐和用户体验优化至关重要：

```java
// 1. 用户行为收集服务
@Service
public class UserBehaviorCollector {
    private final EventBus eventBus;
    
    @Autowired
    public UserBehaviorCollector(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    public void collectBehavior(String userId, String eventType, String targetId, 
                              String targetType, Map<String, Object> properties,
                              String sessionId, String deviceInfo) {
        // 创建用户行为事件
        UserBehaviorEvent event = new UserBehaviorEvent(
            userId,
            eventType,
            targetId,
            targetType,
            properties,
            sessionId,
            deviceInfo
        );
        
        // 发布事件
        eventBus.publish(event);
    }
}

// 2. 前端控制器，接收用户行为数据
@RestController
@RequestMapping("/api/behavior")
public class UserBehaviorController {
    private final UserBehaviorCollector behaviorCollector;
    
    @Autowired
    public UserBehaviorController(UserBehaviorCollector behaviorCollector) {
        this.behaviorCollector = behaviorCollector;
    }
    
    @PostMapping("/track")
    public ResponseEntity<Void> trackBehavior(@RequestBody BehaviorTrackingRequest request,
                                           HttpServletRequest httpRequest) {
        // 从请求或会话中获取用户ID
        String userId = getUserIdFromRequest(httpRequest);
        String sessionId = getSessionId(httpRequest);
        String deviceInfo = request.getDeviceInfo() != null ? 
                          request.getDeviceInfo() : 
                          extractDeviceInfo(httpRequest);
        
        // 收集行为数据
        behaviorCollector.collectBehavior(
            userId,
            request.getEventType(),
            request.getTargetId(),
            request.getTargetType(),
            request.getProperties(),
            sessionId,
            deviceInfo
        );
        
        return ResponseEntity.ok().build();
    }
    
    // 辅助方法...
}

// 3. 用户行为处理服务
@Service
public class UserBehaviorProcessor {
    private final RecommendationService recommendationService;
    private final ProductService productService;
    private final UserProfileService userProfileService;
    private final BatchEventProcessor batchProcessor;
    
    @EventSubscribe
    public void handleProductViewEvent(UserBehaviorEvent event) {
        if ("view".equals(event.getEventType()) && "product".equals(event.getTargetType())) {
            // 1. 更新用户兴趣模型
            userProfileService.updateUserInterests(event.getUserId(), event.getTargetId());
            
            // 2. 增加商品热度分
            productService.incrementProductHeatScore(event.getTargetId());
            
            // 3. 如果用户停留时间超过阈值，认为是高质量浏览
            Integer viewTimeSeconds = (Integer) event.getProperties().get("viewTimeSeconds");
            if (viewTimeSeconds != null && viewTimeSeconds > 30) {
                recommendationService.recordHighQualityView(event.getUserId(), event.getTargetId());
            }
        }
    }
    
    @EventSubscribe
    public void handleSearchEvent(UserBehaviorEvent event) {
        if ("search".equals(event.getEventType())) {
            String keyword = (String) event.getProperties().get("keyword");
            if (keyword != null) {
                // 1. 记录搜索关键词
                userProfileService.addUserSearchKeyword(event.getUserId(), keyword);
                
                // 2. 更新热门搜索词统计
                searchAnalyticsService.incrementKeywordCount(keyword);
                
                // 3. 分析搜索结果点击行为
                List<Map<String, Object>> searchResults = 
                    (List<Map<String, Object>>) event.getProperties().get("results");
                Integer clickedIndex = (Integer) event.getProperties().get("clickedIndex");
                
                if (searchResults != null && clickedIndex != null) {
                    searchAnalyticsService.recordSearchResultClick(
                        keyword, searchResults, clickedIndex);
                }
            }
        }
    }
    
    @EventSubscribe
    public void handleCartEvents(UserBehaviorEvent event) {
        if ("add_to_cart".equals(event.getEventType())) {
            // 商品加入购物车事件处理
            String productId = event.getTargetId();
            Integer quantity = (Integer) event.getProperties().getOrDefault("quantity", 1);
            
            // 1. 更新用户购买意向模型
            userProfileService.updatePurchaseIntent(event.getUserId(), productId, quantity);
            
            // 2. 触发相关商品推荐
            List<String> recommendations = recommendationService.getCartRecommendations(
                event.getUserId(), productId);
                
            if (!recommendations.isEmpty()) {
                // 发送推荐事件
                ProductRecommendationsEvent recEvent = new ProductRecommendationsEvent(
                    event.getUserId(),
                    "cart_add",
                    productId,
                    recommendations
                );
                eventBus.publish(recEvent);
            }
        } else if ("remove_from_cart".equals(event.getEventType())) {
            // 商品从购物车移除事件处理
            userProfileService.decreasePurchaseIntent(event.getUserId(), event.getTargetId());
        }
    }
    
    // 批量处理用户行为事件，提高性能
    @EventSubscribe
    public void handleBatchBehaviorEvents(BatchEvent<UserBehaviorEvent> batchEvent) {
        List<UserBehaviorEvent> events = batchEvent.getEvents();
        
        // 按事件类型分组处理
        Map<String, List<UserBehaviorEvent>> eventsByType = events.stream()
            .collect(Collectors.groupingBy(UserBehaviorEvent::getEventType));
            
        // 批量处理各类型事件
        for (Map.Entry<String, List<UserBehaviorEvent>> entry : eventsByType.entrySet()) {
            switch (entry.getKey()) {
                case "view":
                    batchProcessor.processBatchViews(entry.getValue());
                    break;
                case "search":
                    batchProcessor.processBatchSearches(entry.getValue());
                    break;
                // 其他事件类型...
            }
        }
    }
}
```

## 5. 性能优化策略

### 5.1 高并发场景下的事件处理

电商平台经常面临高并发场景，尤其是在促销活动期间。以下是一些优化策略：

```java
// 1. 事件批处理器
public class BatchEventProcessor {
    private final int batchSize;
    private final long maxWaitTimeMs;
    private final Map<String, BlockingQueue<Event>> typeToEventQueue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final EventBus eventBus;
    
    public BatchEventProcessor(EventBus eventBus, int batchSize, long maxWaitTimeMs) {
        this.eventBus = eventBus;
        this.batchSize = batchSize;
        this.maxWaitTimeMs = maxWaitTimeMs;
        this.scheduler = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder().setNameFormat("batch-event-processor-%d").build()
        );
        
        // 启动定时批处理任务
        scheduler.scheduleWithFixedDelay(
            this::processAllQueues, 0, maxWaitTimeMs, TimeUnit.MILLISECONDS);
    }
    
    public <E extends Event> void addToBatch(E event) {
        String eventType = event.getType();
        BlockingQueue<Event> queue = typeToEventQueue.computeIfAbsent(
            eventType, k -> new LinkedBlockingQueue<>());
        queue.offer(event);
        
        // 如果队列达到批处理大小，立即处理
        if (queue.size() >= batchSize) {
            processBatch(eventType, queue);
        }
    }
    
    private void processAllQueues() {
        for (Map.Entry<String, BlockingQueue<Event>> entry : typeToEventQueue.entrySet()) {
            processBatch(entry.getKey(), entry.getValue());
        }
    }
    
    private void processBatch(String eventType, BlockingQueue<Event> queue) {
        List<Event> batch = new ArrayList<>(batchSize);
        queue.drainTo(batch, batchSize);
        
        if (!batch.isEmpty()) {
            // 创建批量事件
            BatchEvent<Event> batchEvent = new BatchEvent<>(batch, eventType);
            eventBus.publish(batchEvent);
        }
    }
    
    // 批量事件定义
    public static class BatchEvent<E extends Event> extends AbstractEvent {
        private final List<E> events;
        private final String batchType;
        
        public BatchEvent(List<E> events, String batchType) {
            this.events = new ArrayList<>(events);
            this.batchType = batchType;
        }
        
        public List<E> getEvents() {
            return Collections.unmodifiableList(events);
        }
        
        @Override
        public String getType() {
            return "batch." + batchType;
        }
    }
}

// 2. 自适应线程池
public class AdaptiveThreadPoolEventBus implements EventBus {
    private final EventBus delegate;
    private final ThreadPoolExecutor executor;
    private final int corePoolSize;
    private final int maxPoolSize;
    private final BlockingQueue<Runnable> workQueue;
    private final AtomicInteger rejectedCount = new AtomicInteger(0);
    private final ScheduledExecutorService monitor;
    
    public AdaptiveThreadPoolEventBus(EventBus delegate, 
                                    int corePoolSize, 
                                    int maxPoolSize, 
                                    int queueCapacity) {
        this.delegate = delegate;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.workQueue = new LinkedBlockingQueue<>(queueCapacity);
        
        // 创建自适应线程池
        this.executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L, TimeUnit.SECONDS,
            workQueue,
            new ThreadFactoryBuilder().setNameFormat("event-processor-%d").build(),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    rejectedCount.incrementAndGet();
                    // 记录拒绝事件
                    log.warn("Event processing task rejected");
                    // 使用调用者线程执行
                    if (!executor.isShutdown()) {
                        r.run();
                    }
                }
            }
        );
        
        // 启动监控线程，动态调整线程池大小
        this.monitor = Executors.newSingleThreadScheduledExecutor();
        this.monitor.scheduleAtFixedRate(
            this::adjustThreadPool, 10, 10, TimeUnit.SECONDS);
    }
    
    @Override
    public <E extends Event> void publish(E event) {
        executor.execute(() -> delegate.publish(event));
    }
    
    private void adjustThreadPool() {
        int currentPoolSize = executor.getPoolSize();
        int activeThreads = executor.getActiveCount();
        int queueSize = workQueue.size();
        int rejected = rejectedCount.getAndSet(0);
        
        // 计算线程池利用率
        double utilization = (double) activeThreads / currentPoolSize;
        
        // 根据利用率、队列大小和拒绝数调整线程池
        if (utilization > 0.75 && queueSize > 0) {
            // 高利用率且队列有积压，增加线程数
            int newPoolSize = Math.min(currentPoolSize + 5, maxPoolSize);
            executor.setCorePoolSize(newPoolSize);
            log.info("Increasing thread pool size to {} due to high utilization", newPoolSize);
        } else if (rejected > 0) {
            // 有任务被拒绝，立即增加到最大线程数
            executor.setCorePoolSize(maxPoolSize);
            log.info("Increasing thread pool size to maximum {} due to rejected tasks", maxPoolSize);
        } else if (utilization < 0.25 && queueSize == 0 && currentPoolSize > corePoolSize) {
            // 低利用率且无队列积压，减少线程数
            int newPoolSize = Math.max(currentPoolSize - 3, corePoolSize);
            executor.setCorePoolSize(newPoolSize);
            log.info("Decreasing thread pool size to {} due to low utilization", newPoolSize);
        }
        
        // 记录当前线程池状态
        log.debug("Thread pool status: size={}, active={}, queue={}, rejected={}", 
                 currentPoolSize, activeThreads, queueSize, rejected);
    }
    
    // 其他EventBus接口方法实现...
}

// 3. 事件优先级队列
public class PriorityEventQueue {
    private final PriorityBlockingQueue<PrioritizedEvent> queue;
    private final Thread consumerThread;
    private final EventBus eventBus;
    private volatile boolean running = true;
    
    public PriorityEventQueue(EventBus eventBus) {
        this.eventBus = eventBus;
        this.queue = new PriorityBlockingQueue<>(1000, 
            Comparator.comparingInt(PrioritizedEvent::getPriority));
        
        // 启动消费线程
        this.consumerThread = new Thread(this::consumeEvents);
        this.consumerThread.setName("priority-event-consumer");
        this.consumerThread.start();
    }
    
    public <E extends Event> void offer(E event, int priority) {
        queue.offer(new PrioritizedEvent(event, priority));
    }
    
    private void consumeEvents() {
        while (running) {
            try {
                PrioritizedEvent prioritizedEvent = queue.take();
                eventBus.publish(prioritizedEvent.getEvent());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing prioritized event", e);
            }
        }
    }
    
    public void shutdown() {
        running = false;
        consumerThread.interrupt();
    }
    
    // 优先级事件包装类
    private static class PrioritizedEvent {
        private final Event event;
        private final int priority; // 数值越小优先级越高
        
        public PrioritizedEvent(Event event, int priority) {
            this.event = event;
            this.priority = priority;
        }
        
        public Event getEvent() {
            return event;
        }
        
        public int getPriority() {
            return priority;
        }
    }
}
```

### 5.2 热点商品的事件缓存

电商平台中的热点商品往往会引发大量相似的事件查询，通过事件缓存可以显著提高系统性能：

```java
// 1. 事件缓存服务
public class EventCacheService {
    private final Cache<String, Event> eventCache;
    private final Cache<String, List<String>> productRecommendationCache;
    private final Cache<String, Integer> productStockCache;
    
    public EventCacheService() {
        // 创建事件缓存，5分钟过期
        this.eventCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .recordStats()
            .build();
        
        // 创建商品推荐缓存，10分钟过期
        this.productRecommendationCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(5000)
            .recordStats()
            .build();
        
        // 创建商品库存缓存，30秒过期
        this.productStockCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(10000)
            .recordStats()
            .build();
    }
    
    public void cacheEvent(Event event) {
        eventCache.put(event.getId(), event);
    }
    
    public Event getEvent(String eventId) {
        return eventCache.getIfPresent(eventId);
    }
    
    public void cacheProductRecommendations(String productId, List<String> recommendedProductIds) {
        productRecommendationCache.put(productId, recommendedProductIds);
    }
    
    public List<String> getProductRecommendations(String productId) {
        return productRecommendationCache.getIfPresent(productId);
    }
    
    public void cacheProductStock(String productId, int stock) {
        productStockCache.put(productId, stock);
    }
    
    public Integer getProductStock(String productId) {
        return productStockCache.getIfPresent(productId);
    }
    
    // 缓存统计信息
    public Map<String, CacheStats> getCacheStats() {
        Map<String, CacheStats> stats = new HashMap<>();
        stats.put("event", eventCache.stats());
        stats.put("recommendation", productRecommendationCache.stats());
        stats.put("stock", productStockCache.stats());
        return stats;
    }
}

// 2. 热点商品检测与缓存预热
public class HotProductDetector {
    private final EventCacheService cacheService;
    private final InventoryService inventoryService;
    private final RecommendationService recommendationService;
    private final LoadingCache<String, AtomicInteger> productAccessCounter;
    private final ScheduledExecutorService scheduler;
    
    public HotProductDetector(EventCacheService cacheService,
                             InventoryService inventoryService,
                             RecommendationService recommendationService) {
        this.cacheService = cacheService;
        this.inventoryService = inventoryService;
        this.recommendationService = recommendationService;
        
        // 创建商品访问计数器，每10分钟重置
        this.productAccessCounter = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, AtomicInteger>() {
                @Override
                public AtomicInteger load(String key) {
                    return new AtomicInteger(0);
                }
            });
        
        // 启动定时任务，每分钟检测热点商品并预热缓存
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduler.scheduleAtFixedRate(
            this::detectAndPreheatHotProducts, 1, 1, TimeUnit.MINUTES);
    }
    
    public void recordProductAccess(String productId) {
        try {
            productAccessCounter.get(productId).incrementAndGet();
        } catch (ExecutionException e) {
            log.error("Error recording product access", e);
        }
    }
    
    private void detectAndPreheatHotProducts() {
        try {
            // 获取访问次数最多的前100个商品
            List<Map.Entry<String, AtomicInteger>> hotProducts = productAccessCounter.asMap().entrySet().stream()
                .sorted(Map.Entry.<String, AtomicInteger>comparingByValue().reversed())
                .limit(100)
                .collect(Collectors.toList());
            
            // 预热这些热点商品的缓存
            for (Map.Entry<String, AtomicInteger> entry : hotProducts) {
                String productId = entry.getKey();
                int accessCount = entry.getValue().get();
                
                if (accessCount > 100) { // 访问次数阈值
                    log.info("Preheating cache for hot product: {} (access count: {})", 
                             productId, accessCount);
                    
                    // 预热库存缓存
                    int stock = inventoryService.getAvailableStock(productId);
                    cacheService.cacheProductStock(productId, stock);
                    
                    // 预热推荐缓存
                    List<String> recommendations = 
                        recommendationService.getProductRecommendations(productId);
                    cacheService.cacheProductRecommendations(productId, recommendations);
                }
            }
        } catch (Exception e) {
            log.error("Error detecting and preheating hot products", e);
        }
    }
}

// 3. 缓存感知的库存服务
@Service
public class CacheAwareInventoryService implements InventoryService {
    private final InventoryService delegate;
    private final EventCacheService cacheService;
    private final HotProductDetector hotProductDetector;
    
    @Override
    public int getAvailableStock(String skuId) {
        // 记录商品访问
        hotProductDetector.recordProductAccess(skuId);
        
        // 尝试从缓存获取
        Integer cachedStock = cacheService.getProductStock(skuId);
        if (cachedStock != null) {
            return cachedStock;
        }
        
        // 缓存未命中，从实际服务获取
        int stock = delegate.getAvailableStock(skuId);
        
        // 更新缓存
        cacheService.cacheProductStock(skuId, stock);
        
        return stock;
    }
    
    // 其他方法实现...
}
```

### 5.3 峰值流量应对策略

电商平台经常面临促销活动带来的流量峰值，需要特殊的应对策略：

```java
// 1. 流量控制服务
public class TrafficControlService {
    private final RateLimiter globalRateLimiter;
    private final Map<String, RateLimiter> apiRateLimiters = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();
    private final AtomicInteger currentConcurrency = new AtomicInteger(0);
    private final int maxConcurrency;
    private final EventBus eventBus;
    
    public TrafficControlService(double globalQps, int maxConcurrency, EventBus eventBus) {
        this.globalRateLimiter = RateLimiter.create(globalQps);
        this.maxConcurrency = maxConcurrency;
        this.eventBus = eventBus;
    }
    
    public boolean allowRequest(String apiPath, String userId) {
        // 1. 检查全局并发度
        if (currentConcurrency.get() >= maxConcurrency) {
            // 发布系统过载事件
            SystemOverloadEvent event = new SystemOverloadEvent(
                currentConcurrency.get(), maxConcurrency);
            eventBus.publish(event);
            return false;
        }
        
        // 2. 检查全局QPS限制
        if (!globalRateLimiter.tryAcquire()) {
            return false;
        }
        
        // 3. 检查API级别限制
        RateLimiter apiLimiter = apiRateLimiters.computeIfAbsent(
            apiPath, k -> createApiRateLimiter(apiPath));
        if (!apiLimiter.tryAcquire()) {
            return false;
        }
        
        // 4. 检查用户级别限制（如果有用户ID）
        if (userId != null) {
            RateLimiter userLimiter = userRateLimiters.computeIfAbsent(
                userId, k -> RateLimiter.create(10.0)); // 默认每个用户10QPS
            if (!userLimiter.tryAcquire()) {
                return false;
            }
        }
        
        // 增加当前并发计数
        currentConcurrency.incrementAndGet();
        return true;
    }
    
    public void releaseRequest() {
        currentConcurrency.decrementAndGet();
    }
    
    private RateLimiter createApiRateLimiter(String apiPath) {
        // 根据API路径设置不同的QPS限制
        if (apiPath.contains("/api/product")) {
            return RateLimiter.create(1000.0); // 商品API允许较高QPS
        } else if (apiPath.contains("/api/order")) {
            return RateLimiter.create(200.0); // 订单API限制较严格
        } else if (apiPath.contains("/api/payment")) {
            return RateLimiter.create(100.0); // 支付API限制更严格
        } else {
            return RateLimiter.create(500.0); // 默认限制
        }
    }
    
    // 动态调整限流参数
    public void adjustRateLimit(String apiPath, double newQps) {
        RateLimiter limiter = apiRateLimiters.get(apiPath);
        if (limiter != null) {
            limiter.setRate(newQps);
            log.info("Adjusted rate limit for {}: {} QPS", apiPath, newQps);
        }
    }
}

// 2. 系统降级服务
public class DegradationService {
    private final AtomicInteger degradationLevel = new AtomicInteger(0);
    private final Map<String, Boolean> featureStatus = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    
    public DegradationService(EventBus eventBus) {
        this.eventBus = eventBus;
        
        // 初始化功能状态
        featureStatus.put("recommendation", true);
        featureStatus.put("realtime-inventory", true);
        featureStatus.put("promotion", true);
        featureStatus.put("user-behavior-tracking", true);
        featureStatus.put("detailed-search", true);
    }
    
    @EventSubscribe
    public void handleSystemOverload(SystemOverloadEvent event) {
        // 根据过载程度决定降级级别
        double overloadRatio = (double) event.getCurrentConcurrency() / event.getMaxConcurrency();
        
        if (overloadRatio > 0.95) { // 接近最大容量
            upgradeDegradationLevel(3); // 最高降级级别
        } else if (overloadRatio > 0.85) {
            upgradeDegradationLevel(2); // 中等降级级别
        } else if (overloadRatio > 0.75) {
            upgradeDegradationLevel(1); // 轻度降级级别
        }
    }
    
    private void upgradeDegradationLevel(int targetLevel) {
        int currentLevel = degradationLevel.get();
        if (targetLevel > currentLevel) {
            degradationLevel.set(targetLevel);
            applyDegradation(targetLevel);
            
            // 发布系统降级事件
            SystemDegradedEvent event = new SystemDegradedEvent(currentLevel, targetLevel);
            eventBus.publish(event);
        }
    }
    
    private void applyDegradation(int level) {
        switch (level) {
            case 1: // 轻度降级
                featureStatus.put("detailed-search", false); // 关闭详细搜索
                break;
            case 2: // 中度降级
                featureStatus.put("detailed-search", false);
                featureStatus.put("user-behavior-tracking", false); // 关闭用户行为跟踪
                featureStatus.put("recommendation", false); // 关闭推荐功能
                break;
            case 3: // 重度降级
                featureStatus.put("detailed-search", false);
                featureStatus.put("user-behavior-tracking", false);
                featureStatus.put("recommendation", false);
                featureStatus.put("realtime-inventory", false); // 使用缓存库存
                featureStatus.put("promotion", false); // 暂停促销计算
                break;
        }
        
        log.warn("System degraded to level {}: {}", level, featureStatus);
    }
    
    public void recoverDegradation() {
        int currentLevel = degradationLevel.getAndSet(0);
        
        // 恢复所有功能
        featureStatus.keySet().forEach(feature -> featureStatus.put(feature, true));
        
        // 发布系统恢复事件
        if (currentLevel > 0) {
            SystemRecoveredEvent event = new SystemRecoveredEvent(currentLevel);
            eventBus.publish(event);
            log.info("System recovered from degradation level {}", currentLevel);
        }
    }
    
    public boolean isFeatureEnabled(String featureName) {
        return featureStatus.getOrDefault(featureName, true);
    }
}

## 6. 常见问题与解决方案

### 6.1 订单状态不一致问题

在电商系统中，订单状态不一致是一个常见问题，尤其在分布式事务场景下：

```java
// 1. 事务性事件处理机制
public class TransactionalEventProcessor {
    private final EventBus eventBus;
    private final TransactionTemplate transactionTemplate;
    private final TransactionalEventRepository eventRepository;
    
    public TransactionalEventProcessor(EventBus eventBus, 
                                      PlatformTransactionManager transactionManager,
                                      TransactionalEventRepository eventRepository) {
        this.eventBus = eventBus;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.eventRepository = eventRepository;
    }
    
    public <T extends Event> void processWithTransaction(Supplier<T> eventSupplier) {
        transactionTemplate.execute(status -> {
            try {
                // 1. 生成事件
                T event = eventSupplier.get();
                
                // 2. 保存事件到事务性存储
                eventRepository.save(event);
                
                // 3. 注册事务提交后回调
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        // 事务提交后发布事件
                        eventBus.publish(event);
                    }
                });
                
                return event;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("Error processing transactional event", e);
                throw e;
            }
        });
    }
    
    // 事件恢复机制 - 定时任务检查未发布的事件并重试
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void recoverUnpublishedEvents() {
        List<Event> unpublishedEvents = eventRepository.findUnpublishedEvents();
        
        for (Event event : unpublishedEvents) {
            try {
                eventBus.publish(event);
                eventRepository.markAsPublished(event.getId());
                log.info("Recovered unpublished event: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to recover event: {}", event.getId(), e);
            }
        }
    }
}

// 2. 订单状态一致性检查服务
public class OrderConsistencyService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final EventBus eventBus;
    
    // 定时检查订单状态一致性
    @Scheduled(cron = "0 0/10 * * * ?") // 每10分钟执行一次
    public void checkOrderConsistency() {
        // 获取需要检查的订单（如最近24小时内的订单）
        LocalDateTime checkTime = LocalDateTime.now().minusHours(24);
        List<Order> orders = orderRepository.findOrdersCreatedAfter(checkTime);
        
        for (Order order : orders) {
            try {
                boolean isConsistent = verifyOrderConsistency(order);
                
                if (!isConsistent) {
                    // 发布订单不一致事件
                    OrderInconsistencyEvent event = new OrderInconsistencyEvent(order.getId());
                    eventBus.publish(event);
                    log.warn("Detected inconsistent order state: {}", order.getId());
                }
            } catch (Exception e) {
                log.error("Error checking order consistency: {}", order.getId(), e);
            }
        }
    }
    
    private boolean verifyOrderConsistency(Order order) {
        // 检查订单支付状态与支付记录是否一致
        if (order.getStatus() == OrderStatus.PAID) {
            Payment payment = paymentRepository.findByOrderId(order.getId());
            if (payment == null || payment.getStatus() != PaymentStatus.SUCCESS) {
                return false;
            }
        }
        
        // 检查订单商品与库存扣减记录是否一致
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPED) {
            for (OrderItem item : order.getItems()) {
                InventoryDeduction deduction = inventoryRepository
                    .findDeductionByOrderIdAndProductId(order.getId(), item.getProductId());
                
                if (deduction == null || deduction.getQuantity() != item.getQuantity()) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // 处理订单不一致事件
    @EventSubscribe
    public void handleOrderInconsistency(OrderInconsistencyEvent event) {
        String orderId = event.getOrderId();
        Order order = orderRepository.findById(orderId);
        
        if (order == null) {
            log.warn("Cannot find order for inconsistency correction: {}", orderId);
            return;
        }
        
        // 根据实际情况执行修复策略
        try {
            OrderRepairStrategy strategy = determineRepairStrategy(order);
            strategy.repair(order);
            log.info("Repaired inconsistent order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to repair inconsistent order: {}", orderId, e);
            // 发送告警通知人工介入
            alertService.sendAlert("Order repair failed: " + orderId);
        }
    }
    
    private OrderRepairStrategy determineRepairStrategy(Order order) {
        // 根据订单状态和问题类型选择修复策略
        // 这里可以实现多种修复策略，如支付状态修复、库存修复等
        // ...
        return new DefaultOrderRepairStrategy(orderRepository, paymentRepository, inventoryRepository);
    }
}
```

### 6.2 库存超卖与少卖

库存管理是电商平台的关键挑战，尤其在高并发场景下：

```java
// 1. 基于乐观锁的库存服务
public class OptimisticInventoryService implements InventoryService {
    private final ProductRepository productRepository;
    private final EventBus eventBus;
    
    @Override
    @Transactional
    public boolean deductStock(String productId, int quantity, String orderId) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                Product product = productRepository.findByIdWithLock(productId);
                
                if (product == null) {
                    log.warn("Product not found: {}", productId);
                    return false;
                }
                
                if (product.getAvailableStock() < quantity) {
                    log.warn("Insufficient stock for product: {}, requested: {}, available: {}",
                            productId, quantity, product.getAvailableStock());
                    
                    // 发布库存不足事件
                    InsufficientStockEvent event = new InsufficientStockEvent(
                        productId, quantity, product.getAvailableStock(), orderId);
                    eventBus.publish(event);
                    
                    return false;
                }
                
                // 扣减库存
                product.setAvailableStock(product.getAvailableStock() - quantity);
                product.setVersion(product.getVersion() + 1); // 乐观锁版本更新
                
                productRepository.save(product);
                
                // 发布库存扣减事件
                StockDeductedEvent event = new StockDeductedEvent(
                    productId, quantity, product.getAvailableStock(), orderId);
                eventBus.publish(event);
                
                return true;
            } catch (OptimisticLockingFailureException e) {
                // 乐观锁冲突，重试
                retryCount++;
                log.warn("Optimistic lock failure when deducting stock for product: {}, retry: {}/{}",
                        productId, retryCount, maxRetries);
                
                if (retryCount >= maxRetries) {
                    log.error("Failed to deduct stock after {} retries", maxRetries);
                    return false;
                }
                
                // 短暂延迟后重试
                try {
                    Thread.sleep(50 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
    
    // 库存补偿机制
    @EventSubscribe
    public void handleOrderCancelled(OrderCancelledEvent event) {
        String orderId = event.getOrderId();
        List<OrderItem> items = event.getItems();
        
        for (OrderItem item : items) {
            try {
                // 恢复库存
                Product product = productRepository.findByIdWithLock(item.getProductId());
                
                if (product != null) {
                    product.setAvailableStock(product.getAvailableStock() + item.getQuantity());
                    product.setVersion(product.getVersion() + 1);
                    
                    productRepository.save(product);
                    
                    // 发布库存恢复事件
                    StockRestoredEvent restoredEvent = new StockRestoredEvent(
                        item.getProductId(), item.getQuantity(), 
                        product.getAvailableStock(), orderId);
                    eventBus.publish(restoredEvent);
                    
                    log.info("Restored stock for product: {}, quantity: {}, order: {}",
                            item.getProductId(), item.getQuantity(), orderId);
                }
            } catch (Exception e) {
                log.error("Failed to restore stock for product: {}, order: {}",
                        item.getProductId(), orderId, e);
            }
        }
    }
}

// 2. 库存预占与释放机制
public class StockPreallocationService {
    private final ProductRepository productRepository;
    private final StockPreallocationRepository preallocationRepository;
    private final EventBus eventBus;
    
    @Transactional
    public boolean preallocateStock(String productId, int quantity, String orderId, int timeoutMinutes) {
        try {
            Product product = productRepository.findByIdWithLock(productId);
            
            if (product == null || product.getAvailableStock() < quantity) {
                return false;
            }
            
            // 创建库存预占记录
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(timeoutMinutes);
            StockPreallocation preallocation = new StockPreallocation();
            preallocation.setProductId(productId);
            preallocation.setQuantity(quantity);
            preallocation.setOrderId(orderId);
            preallocation.setStatus(PreallocationStatus.ACTIVE);
            preallocation.setExpirationTime(expirationTime);
            
            preallocationRepository.save(preallocation);
            
            // 更新产品可用库存
            product.setAvailableStock(product.getAvailableStock() - quantity);
            product.setPreallocatedStock(product.getPreallocatedStock() + quantity);
            productRepository.save(product);
            
            // 发布库存预占事件
            StockPreallocatedEvent event = new StockPreallocatedEvent(
                productId, quantity, orderId, expirationTime);
            eventBus.publish(event);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to preallocate stock for product: {}, order: {}",
                    productId, orderId, e);
            return false;
        }
    }
    
    @Transactional
    public void confirmPreallocation(String orderId) {
        List<StockPreallocation> preallocations = 
            preallocationRepository.findByOrderIdAndStatus(orderId, PreallocationStatus.ACTIVE);
        
        for (StockPreallocation preallocation : preallocations) {
            try {
                // 更新预占状态为已确认
                preallocation.setStatus(PreallocationStatus.CONFIRMED);
                preallocationRepository.save(preallocation);
                
                // 更新产品预占库存
                Product product = productRepository.findByIdWithLock(preallocation.getProductId());
                if (product != null) {
                    product.setPreallocatedStock(product.getPreallocatedStock() - preallocation.getQuantity());
                    product.setReservedStock(product.getReservedStock() + preallocation.getQuantity());
                    productRepository.save(product);
                }
                
                // 发布库存确认事件
                StockAllocationConfirmedEvent event = new StockAllocationConfirmedEvent(
                    preallocation.getProductId(), preallocation.getQuantity(), orderId);
                eventBus.publish(event);
            } catch (Exception e) {
                log.error("Failed to confirm stock preallocation: {}", preallocation.getId(), e);
            }
        }
    }
    
    @Transactional
    public void releasePreallocation(String orderId) {
        List<StockPreallocation> preallocations = 
            preallocationRepository.findByOrderIdAndStatus(orderId, PreallocationStatus.ACTIVE);
        
        for (StockPreallocation preallocation : preallocations) {
            try {
                // 更新预占状态为已释放
                preallocation.setStatus(PreallocationStatus.RELEASED);
                preallocationRepository.save(preallocation);
                
                // 恢复产品可用库存
                Product product = productRepository.findByIdWithLock(preallocation.getProductId());
                if (product != null) {
                    product.setAvailableStock(product.getAvailableStock() + preallocation.getQuantity());
                    product.setPreallocatedStock(product.getPreallocatedStock() - preallocation.getQuantity());
                    productRepository.save(product);
                }
                
                // 发布库存释放事件
                StockAllocationReleasedEvent event = new StockAllocationReleasedEvent(
                    preallocation.getProductId(), preallocation.getQuantity(), orderId);
                eventBus.publish(event);
            } catch (Exception e) {
                log.error("Failed to release stock preallocation: {}", preallocation.getId(), e);
            }
        }
    }
    
    // 定时清理过期的库存预占
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupExpiredPreallocations() {
        LocalDateTime now = LocalDateTime.now();
        List<StockPreallocation> expiredPreallocations = 
            preallocationRepository.findExpiredPreallocations(now, PreallocationStatus.ACTIVE);
        
        for (StockPreallocation preallocation : expiredPreallocations) {
            try {
                releasePreallocation(preallocation.getOrderId());
                log.info("Released expired stock preallocation: {}", preallocation.getId());
            } catch (Exception e) {
                log.error("Failed to release expired preallocation: {}", preallocation.getId(), e);
            }
        }
    }
}
```

### 6.3 事件风暴与系统降级

在高并发场景下，系统可能面临事件风暴，需要有效的降级策略：

```java
// 1. 事件限流器
public class EventThrottler {
    private final Map<String, RateLimiter> eventTypeLimiters = new ConcurrentHashMap<>();
    private final Map<String, Integer> eventTypeThresholds = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    private final DegradationService degradationService;
    
    public EventThrottler(EventBus eventBus, DegradationService degradationService) {
        this.eventBus = eventBus;
        this.degradationService = degradationService;
        
        // 初始化不同事件类型的阈值
        eventTypeThresholds.put("OrderCreatedEvent", 1000); // 每秒最多1000个订单创建事件
        eventTypeThresholds.put("ProductViewedEvent", 5000); // 每秒最多5000个商品浏览事件
        eventTypeThresholds.put("UserBehaviorEvent", 3000); // 每秒最多3000个用户行为事件
        
        // 初始化限流器
        for (Map.Entry<String, Integer> entry : eventTypeThresholds.entrySet()) {
            eventTypeLimiters.put(entry.getKey(), RateLimiter.create(entry.getValue()));
        }
    }
    
    public <E extends Event> boolean shouldThrottle(E event) {
        String eventType = event.getClass().getSimpleName();
        RateLimiter limiter = eventTypeLimiters.getOrDefault(
            eventType, RateLimiter.create(1000)); // 默认限流
        
        boolean allowed = limiter.tryAcquire();
        
        if (!allowed) {
            // 记录被限流的事件
            log.warn("Event throttled: {} (type: {})", event.getId(), eventType);
            
            // 发布事件限流事件
            EventThrottledEvent throttledEvent = new EventThrottledEvent(event.getId(), eventType);
            eventBus.publish(throttledEvent);
            
            // 检查是否需要触发系统降级
            checkForDegradation(eventType);
        }
        
        return !allowed; // 返回是否应该被限流
    }
    
    private void checkForDegradation(String eventType) {
        // 获取当前限流计数
        int threshold = eventTypeThresholds.getOrDefault(eventType, 1000);
        RateLimiter limiter = eventTypeLimiters.get(eventType);
        
        // 如果限流器的速率已经降到阈值的50%以下，触发系统降级
        if (limiter.getRate() < threshold * 0.5) {
            log.warn("Triggering system degradation due to excessive {} events", eventType);
            
            // 根据事件类型决定降级级别
            int degradationLevel = determineDegradationLevel(eventType);
            degradationService.upgradeDegradationLevel(degradationLevel);
        }
    }
    
    private int determineDegradationLevel(String eventType) {
        // 根据事件类型返回适当的降级级别
        switch (eventType) {
            case "OrderCreatedEvent":
                return 2; // 订单事件风暴，中度降级
            case "PaymentProcessedEvent":
                return 3; // 支付事件风暴，高度降级
            default:
                return 1; // 默认轻度降级
        }
    }
    
    // 动态调整事件限流阈值
    public void adjustThreshold(String eventType, int newThreshold) {
        if (newThreshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }
        
        eventTypeThresholds.put(eventType, newThreshold);
        RateLimiter limiter = eventTypeLimiters.get(eventType);
        
        if (limiter != null) {
            limiter.setRate(newThreshold);
            log.info("Adjusted rate limit for event type {}: {} events/second", 
                     eventType, newThreshold);
        } else {
            // 创建新的限流器
            eventTypeLimiters.put(eventType, RateLimiter.create(newThreshold));
            log.info("Created new rate limiter for event type {}: {} events/second", 
                     eventType, newThreshold);
        }
    }
}

// 2. 事件优先级处理器
public class EventPriorityProcessor {
    private final Map<String, Integer> eventPriorities = new ConcurrentHashMap<>();
    private final PriorityQueue<PrioritizedEvent> eventQueue;
    private final EventBus eventBus;
    private final Thread processorThread;
    private volatile boolean running = true;
    
    public EventPriorityProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventQueue = new PriorityQueue<>(
            Comparator.comparingInt(PrioritizedEvent::getPriority));
        
        // 初始化事件优先级（数值越小优先级越高）
        eventPriorities.put("PaymentProcessedEvent", 1); // 最高优先级
        eventPriorities.put("OrderCreatedEvent", 2);
        eventPriorities.put("InventoryUpdatedEvent", 3);
        eventPriorities.put("ProductViewedEvent", 10); // 最低优先级
        
        // 启动处理线程
        this.processorThread = new Thread(this::processEvents);
        this.processorThread.setName("event-priority-processor");
        this.processorThread.start();
    }
    
    public <E extends Event> void submit(E event) {
        String eventType = event.getClass().getSimpleName();
        int priority = eventPriorities.getOrDefault(eventType, 5); // 默认中等优先级
        
        synchronized (eventQueue) {
            eventQueue.offer(new PrioritizedEvent(event, priority));
            eventQueue.notify(); // 唤醒处理线程
        }
    }
    
    private void processEvents() {
        while (running) {
            PrioritizedEvent prioritizedEvent = null;
            
            synchronized (eventQueue) {
                while (eventQueue.isEmpty() && running) {
                    try {
                        eventQueue.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                if (!running) {
                    break;
                }
                
                prioritizedEvent = eventQueue.poll();
            }
            
            if (prioritizedEvent != null) {
                try {
                    // 处理事件
                    eventBus.publish(prioritizedEvent.getEvent());
                } catch (Exception e) {
                    log.error("Error processing prioritized event", e);
                }
            }
        }
    }
    
    public void shutdown() {
        running = false;
        synchronized (eventQueue) {
            eventQueue.notify(); // 唤醒等待的线程
        }
        processorThread.interrupt();
    }
    
    // 优先级事件包装类
    private static class PrioritizedEvent {
        private final Event event;
        private final int priority;
        
        public PrioritizedEvent(Event event, int priority) {
            this.event = event;
            this.priority = priority;
        }
        
        public Event getEvent() {
            return event;
        }
        
        public int getPriority() {
            return priority;
        }
    }
}
```

## 7. 最佳实践与配置指南

### 7.1 事件设计最佳实践

#### 7.1.1 事件命名规范

```java
// 推荐：使用动词过去式 + 业务对象
public class OrderCreatedEvent implements Event {
    // 事件内容
}

public class PaymentProcessedEvent implements Event {
    // 事件内容
}

public class InventoryReservedEvent implements Event {
    // 事件内容
}

// 不推荐：使用现在时或不明确的命名
public class OrderEvent implements Event { // 太泛化
    // 事件内容
}

public class CreateOrder implements Event { // 使用现在时
    // 事件内容
}
```

#### 7.1.2 事件数据设计原则

```java
// 推荐：包含足够的上下文信息
public class OrderCreatedEvent implements Event {
    private final String orderId;
    private final String userId;
    private final BigDecimal totalAmount;
    private final List<OrderItem> items;
    private final String shippingAddress;
    private final PaymentMethod paymentMethod;
    private final long timestamp;
    
    // 构造函数和getter方法
}

// 不推荐：信息不足，需要额外查询
public class OrderCreatedEvent implements Event {
    private final String orderId; // 只有ID，缺少业务上下文
    
    // 构造函数和getter方法
}
```

#### 7.1.3 事件版本管理

```java
// 版本化事件设计
public class OrderCreatedEventV2 implements Event {
    private final String version = "2.0";
    private final String orderId;
    private final String userId;
    private final BigDecimal totalAmount;
    private final List<OrderItem> items;
    private final String shippingAddress;
    private final PaymentMethod paymentMethod;
    private final String promotionCode; // 新增字段
    private final long timestamp;
    
    @Override
    public String getType() {
        return "order.created.v2";
    }
}

// 向后兼容的事件处理
@EventSubscribe(eventType = "order.created")
public void handleOrderCreatedV1(OrderCreatedEvent event) {
    // 处理V1版本事件
}

@EventSubscribe(eventType = "order.created.v2")
public void handleOrderCreatedV2(OrderCreatedEventV2 event) {
    // 处理V2版本事件
}
```

### 7.2 性能优化配置

#### 7.2.1 线程池配置

```yaml
# application.yml
atlas:
  event:
    # 异步EventBus配置
    event-bus:
      type: adaptive
      async:
        core-pool-size: 10          # 核心线程数
        max-pool-size: 50           # 最大线程数
        queue-capacity: 1000        # 队列容量
        keep-alive-seconds: 60      # 线程存活时间
        thread-name-prefix: "atlas-event-"
        
      # 自适应线程池配置
      adaptive:
        core-pool-size: 8
        max-pool-size: 32
        target-utilization: 0.75    # 目标利用率
        monitor-interval-seconds: 30
        scale-up-threshold: 0.8     # 扩容阈值
        scale-down-threshold: 0.3   # 缩容阈值
        
      # 多线程池配置
      multi-thread:
        default-pool-size: 10
        pools:
          order-events: 15          # 订单事件专用线程池
          payment-events: 20        # 支付事件专用线程池
          inventory-events: 10      # 库存事件专用线程池
          notification-events: 5    # 通知事件专用线程池
        priorities:
          high: ["payment.processed", "order.cancelled"]
          low: ["user.activity", "analytics.event"]
```

#### 7.2.2 批处理配置

```yaml
atlas:
  event:
    # 批处理配置
    batch:
      enabled: true
      size: 100                     # 批处理大小
      timeout-ms: 5000             # 批处理超时
      max-wait-ms: 1000            # 最大等待时间
      
    # 持久化批处理
    persistence:
      batch-size: 200
      flush-interval-ms: 3000
      async-write: true
```

#### 7.2.3 内存优化配置

```java
@Configuration
public class EventOptimizationConfig {
    
    @Bean
    public EventObjectPool eventObjectPool() {
        return EventObjectPool.builder()
            .maxPoolSize(1000)          // 对象池最大大小
            .initialSize(100)           // 初始大小
            .maxIdleTime(300000)        // 最大空闲时间(5分钟)
            .build();
    }
    
    @Bean
    public EventCache eventCache() {
        return EventCache.builder()
            .maxSize(10000)             // 缓存最大条目数
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();
    }
}
```

### 7.3 监控与观测配置

#### 7.3.1 指标收集配置

```yaml
atlas:
  event:
    monitoring:
      enabled: true
      metrics:
        enabled: true
        interval-seconds: 60
        export-to-prometheus: true
        export-to-influxdb: false
        
      # 自定义指标
      custom-metrics:
        - name: "order_processing_time"
          type: "histogram"
          event-types: ["order.created", "order.updated"]
        - name: "payment_success_rate"
          type: "counter"
          event-types: ["payment.processed"]
          
      # 健康检查
      health:
        enabled: true
        timeout-ms: 5000
        check-interval-seconds: 30
```

#### 7.3.2 日志配置

```yaml
# logback-spring.xml
logging:
  level:
    io.github.nemoob.event: INFO
    org.springframework.context.event: WARN
    
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"
    
  appenders:
    event-appender:
      type: RollingFileAppender
      file: logs/atlas-event.log
      max-file-size: 100MB
      max-history: 30
```

#### 7.3.3 链路追踪配置

```java
@Component
public class EventTracingInterceptor {
    
    @EventSubscribe(eventType = "*", priority = Integer.MAX_VALUE)
    public void traceEvent(Event event) {
        String traceId = generateTraceId();
        MDC.put("traceId", traceId);
        MDC.put("eventId", event.getId());
        MDC.put("eventType", event.getType());
        
        try {
            // 事件处理逻辑
        } finally {
            MDC.clear();
        }
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
```

### 7.4 安全配置

#### 7.4.1 事件加密配置

```yaml
atlas:
  event:
    security:
      encryption:
        enabled: true
        algorithm: "AES/GCM/NoPadding"
        key-size: 256
        key-source: "environment"    # environment, file, vault
        key-rotation-days: 90
        
      # 敏感事件类型
      sensitive-events:
        - "payment.processed"
        - "user.personal.updated"
        - "order.payment.info"
```

#### 7.4.2 访问控制配置

```java
@Configuration
@EnableEventSecurity
public class EventSecurityConfig {
    
    @Bean
    public EventAccessControl eventAccessControl() {
        return EventAccessControl.builder()
            .requireRole("payment.processed", "PAYMENT_PROCESSOR")
            .requireRole("order.cancelled", "ORDER_MANAGER")
            .requirePermission("user.personal.updated", "USER_DATA_ACCESS")
            .build();
    }
    
    @Bean
    public EventAuditLogger eventAuditLogger() {
        return new EventAuditLogger();
    }
}
```

### 7.5 容错与恢复配置

#### 7.5.1 重试配置

```yaml
atlas:
  event:
    retry:
      enabled: true
      max-attempts: 3
      initial-delay-ms: 1000
      max-delay-ms: 30000
      backoff-multiplier: 2.0
      
      # 按事件类型配置重试策略
      strategies:
        "payment.processed":
          max-attempts: 5
          initial-delay-ms: 2000
        "notification.email":
          max-attempts: 2
          initial-delay-ms: 500
          
      # 重试条件
      retry-on:
        - "java.net.SocketTimeoutException"
        - "org.springframework.dao.DataAccessException"
        
      # 不重试的异常
      no-retry-on:
        - "java.lang.IllegalArgumentException"
        - "javax.validation.ValidationException"
```

#### 7.5.2 死信队列配置

```java
@Configuration
public class DeadLetterQueueConfig {
    
    @Bean
    public DeadLetterQueue deadLetterQueue() {
        return DeadLetterQueue.builder()
            .maxSize(10000)
            .persistenceEnabled(true)
            .retentionDays(7)
            .alertThreshold(100)        // 超过100条时告警
            .build();
    }
    
    @Bean
    public DeadLetterProcessor deadLetterProcessor() {
        return new DeadLetterProcessor();
    }
}
```

### 7.6 环境特定配置

#### 7.6.1 开发环境配置

```yaml
# application-dev.yml
atlas:
  event:
    event-bus:
      type: default                 # 使用同步EventBus便于调试
    monitoring:
      enabled: false               # 关闭监控减少日志
    persistence:
      enabled: false               # 关闭持久化加快启动
    retry:
      max-attempts: 1              # 减少重试加快测试
    security:
      encryption:
        enabled: false             # 开发环境关闭加密
```

#### 7.6.2 测试环境配置

```yaml
# application-test.yml
atlas:
  event:
    event-bus:
      type: async
      async:
        core-pool-size: 2
        max-pool-size: 5
    monitoring:
      enabled: true
      metrics:
        export-to-prometheus: false
    persistence:
      enabled: true
      type: memory                 # 使用内存存储便于测试
```

#### 7.6.3 生产环境配置

```yaml
# application-prod.yml
atlas:
  event:
    event-bus:
      type: adaptive               # 使用自适应EventBus
    monitoring:
      enabled: true
      metrics:
        export-to-prometheus: true
    persistence:
      enabled: true
      type: database
    security:
      encryption:
        enabled: true
    retry:
      enabled: true
    kafka:
      enabled: true                # 启用分布式支持
```

### 7.7 部署配置

#### 7.7.1 Docker配置

```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

# 设置JVM参数
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 事件框架特定配置
ENV ATLAS_EVENT_THREAD_POOL_SIZE=20
ENV ATLAS_EVENT_MONITORING_ENABLED=true
ENV ATLAS_EVENT_PERSISTENCE_TYPE=database

COPY target/ecommerce-app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 7.7.2 Kubernetes配置

```yaml
# k8s-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ecommerce-app
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: app
        image: ecommerce-app:latest
        env:
        - name: ATLAS_EVENT_KAFKA_ENABLED
          value: "true"
        - name: ATLAS_EVENT_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-cluster:9092"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
```

### 7.8 故障排除指南

#### 7.8.1 常见问题诊断

```java
// 事件处理延迟诊断
@Component
public class EventDiagnostics {
    
    @EventSubscribe(eventType = "*")
    public void diagnoseEventProcessing(Event event) {
        long processingTime = System.currentTimeMillis() - event.getTimestamp();
        
        if (processingTime > 5000) { // 超过5秒
            log.warn("Slow event processing detected: {} took {}ms", 
                event.getType(), processingTime);
        }
        
        // 记录处理指标
        Metrics.timer("event.processing.time", "type", event.getType())
               .record(processingTime, TimeUnit.MILLISECONDS);
    }
}
```

#### 7.8.2 性能调优检查清单

1. **线程池配置检查**
   - 核心线程数是否合理（通常为CPU核心数）
   - 最大线程数是否过大（避免上下文切换开销）
   - 队列容量是否适当（避免内存溢出）

2. **事件设计检查**
   - 事件大小是否合理（避免过大的事件对象）
   - 事件频率是否过高（考虑事件聚合）
   - 订阅者数量是否合理（避免扇出过大）

3. **持久化性能检查**
   - 批处理配置是否启用
   - 数据库连接池配置是否合理
   - 索引是否正确创建

### 7.9 小结

本章提供了Atlas Event Framework在电商场景下的最佳实践和配置指南，涵盖了：

- **事件设计**: 命名规范、数据设计、版本管理
- **性能优化**: 线程池、批处理、内存优化
- **监控观测**: 指标收集、日志配置、链路追踪
- **安全配置**: 加密、访问控制、审计
- **容错恢复**: 重试机制、死信队列
- **环境配置**: 开发、测试、生产环境
- **部署配置**: Docker、Kubernetes
- **故障排除**: 诊断工具、性能调优

遵循这些最佳实践，可以确保事件驱动架构在生产环境中稳定、高效地运行。
```