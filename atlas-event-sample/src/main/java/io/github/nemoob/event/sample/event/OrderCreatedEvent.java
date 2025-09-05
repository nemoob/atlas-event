package io.github.nemoob.event.sample.event;

import io.github.nemoob.event.core.Event;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单创建事件
 */
public class OrderCreatedEvent implements Event {
    private final String id;
    private final String orderId;
    private final String userId;
    private final double amount;
    private final long timestamp;
    private final Map<String, Object> metadata;
    
    public OrderCreatedEvent(String id, String orderId, String userId, double amount) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getType() {
        return "order.created";
    }
    
    @Override
    public Object getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("userId", userId);
        data.put("amount", amount);
        return data;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public double getAmount() {
        return amount;
    }
}