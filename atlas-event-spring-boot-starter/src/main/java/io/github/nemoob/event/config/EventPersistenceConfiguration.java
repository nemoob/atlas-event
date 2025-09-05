package io.github.nemoob.event.config;

import io.github.nemoob.event.persistence.EventPersistence;
import io.github.nemoob.event.persistence.DatabaseEventPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事件持久化配置类
 */
@Configuration
public class EventPersistenceConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public EventPersistence eventPersistence() {
        return new DatabaseEventPersistence();
    }
}