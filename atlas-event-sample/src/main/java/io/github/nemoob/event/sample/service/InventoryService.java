package io.github.nemoob.event.sample.service;

import io.github.nemoob.event.annotation.EventSubscribe;
import io.github.nemoob.event.sample.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

/**
 * 库存服务类
 * 使用@EventSubscribe注解订阅事件
 */
@Service
public class InventoryService {
    
    /**
     * 订阅订单创建事件
     */
    @EventSubscribe(eventType = "order.created")
    public void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("Updating inventory for order: " + event.getOrderId());
        // 实际的库存更新逻辑
    }
}