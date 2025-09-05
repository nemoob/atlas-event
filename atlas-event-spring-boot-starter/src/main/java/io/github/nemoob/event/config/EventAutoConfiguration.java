package io.github.nemoob.event.config;

import io.github.nemoob.event.core.EventBus;
import io.github.nemoob.event.core.DefaultEventBus;
import io.github.nemoob.event.processor.EventAnnotationProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 事件自动配置类
 */
@Configuration
@ConditionalOnClass(EventBus.class)
@EnableConfigurationProperties(EventProperties.class)
@Import({
    EventAnnotationProcessor.class
})
public class EventAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }
}