package io.github.nemoob.event.sample.service;

import io.github.nemoob.event.annotation.EventSubscribe;
import io.github.nemoob.event.sample.event.UserRegisteredEvent;
import org.springframework.stereotype.Service;

/**
 * 通知服务类
 * 使用@EventSubscribe注解订阅事件
 */
@Service
public class NotificationService {
    
    /**
     * 订阅用户注册事件
     */
    @EventSubscribe(eventType = "user.registered")
    public void onUserRegistered(UserRegisteredEvent event) {
        System.out.println("Sending notification for user: " + event.getUsername());
        // 实际的通知逻辑
    }
    
    /**
     * 异步订阅用户注册事件
     */
    @EventSubscribe(eventType = "user.registered", async = true)
    public void onUserRegisteredAsync(UserRegisteredEvent event) {
        System.out.println("Sending async notification for user: " + event.getUsername());
        // 实际的异步通知逻辑
    }
}