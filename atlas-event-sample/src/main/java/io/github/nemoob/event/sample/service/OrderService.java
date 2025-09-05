package io.github.nemoob.event.sample.service;

import io.github.nemoob.event.annotation.EventPublish;
import io.github.nemoob.event.core.EventBus;
import io.github.nemoob.event.sample.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 订单服务类
 * 使用@EventPublish注解标识为事件发布者
 */
@Service
@EventPublish
public class OrderService {
    
    private final EventBus eventBus;
    
    public OrderService(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    /**
     * 创建订单并发布事件
     */
    public void createOrder(String userId, double amount) {
        // 实际的订单创建逻辑
        String orderId = "ORDER-" + System.currentTimeMillis();
        System.out.println("Creating order: " + orderId + " for user: " + userId + " with amount: " + amount);
        
        // 创建并发布事件
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID().toString(), orderId, userId, amount);
        eventBus.publish(event);
    }
}