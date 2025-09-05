package io.github.nemoob.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 事件框架配置属性
 */
@ConfigurationProperties(prefix = "atlas.event")
public class EventProperties {
    
    /**
     * 线程池大小
     */
    private int threadPoolSize = 10;
    
    /**
     * 持久化类型
     */
    private String persistenceType = "database";
    
    /**
     * 是否启用异步处理
     */
    private boolean enableAsync = true;
    
    /**
     * 最大重试次数
     */
    private int maxRetryAttempts = 3;
    
    // getters and setters
    
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
    
    public String getPersistenceType() {
        return persistenceType;
    }
    
    public void setPersistenceType(String persistenceType) {
        this.persistenceType = persistenceType;
    }
    
    public boolean isEnableAsync() {
        return enableAsync;
    }
    
    public void setEnableAsync(boolean enableAsync) {
        this.enableAsync = enableAsync;
    }
    
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }
}