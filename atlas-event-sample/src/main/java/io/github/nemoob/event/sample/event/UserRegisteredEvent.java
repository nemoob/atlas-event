package io.github.nemoob.event.sample.event;

import io.github.nemoob.event.core.Event;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户注册事件
 */
public class UserRegisteredEvent implements Event {
    private final String id;
    private final String username;
    private final long timestamp;
    private final Map<String, Object> metadata;
    
    public UserRegisteredEvent(String id, String username) {
        this.id = id;
        this.username = username;
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getType() {
        return "user.registered";
    }
    
    @Override
    public Object getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
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
    
    public String getUsername() {
        return username;
    }
}