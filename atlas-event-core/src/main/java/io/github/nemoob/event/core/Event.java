package io.github.nemoob.event.core;

import java.util.Map;

/**
 * 事件接口
 */
public interface Event {
    /**
     * 获取事件ID
     */
    String getId();
    
    /**
     * 获取事件类型
     */
    String getType();
    
    /**
     * 获取事件数据
     */
    Object getData();
    
    /**
     * 获取事件时间戳
     */
    long getTimestamp();
    
    /**
     * 获取事件元数据
     */
    Map<String, Object> getMetadata();
}