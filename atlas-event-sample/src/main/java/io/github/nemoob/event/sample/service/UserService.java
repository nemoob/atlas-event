package io.github.nemoob.event.sample.service;

import io.github.nemoob.event.annotation.EventPublish;
import io.github.nemoob.event.core.EventBus;
import io.github.nemoob.event.sample.event.UserRegisteredEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户服务类
 * 使用@EventPublish注解标识为事件发布者
 */
@Service
@EventPublish
public class UserService {
    
    private final EventBus eventBus;
    
    public UserService(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    /**
     * 注册用户并发布事件
     */
    public void registerUser(String username) {
        // 实际的用户注册逻辑
        System.out.println("Registering user: " + username);
        
        // 创建并发布事件
        UserRegisteredEvent event = new UserRegisteredEvent(UUID.randomUUID().toString(), username);
        eventBus.publish(event);
    }
}