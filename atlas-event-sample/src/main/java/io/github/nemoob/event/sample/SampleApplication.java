package io.github.nemoob.event.sample;

import io.github.nemoob.event.sample.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 示例应用主类
 */
@SpringBootApplication
public class SampleApplication {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SampleApplication.class, args);
        
        // 获取UserService并测试事件发布
        UserService userService = context.getBean(UserService.class);
        userService.registerUser("JohnDoe");
    }
}