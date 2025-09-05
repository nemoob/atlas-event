package io.github.nemoob.event.persistence;

import io.github.nemoob.event.core.Event;
import java.util.List;

/**
 * 事件持久化接口
 */
public interface EventPersistence {
    /**
     * 保存事件
     */
    void save(Event event);
    
    /**
     * 根据事件类型查询事件
     */
    List<Event> findByType(String eventType, int limit);
    
    /**
     * 根据事件ID查询事件
     */
    Event findById(String id);
}