package io.github.nemoob.event;

import io.github.nemoob.event.annotation.EventSubscribe;
import io.github.nemoob.event.core.DefaultEventBus;
import io.github.nemoob.event.core.Event;
import io.github.nemoob.event.core.EventBus;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * 事件框架测试类
 */
public class EventFrameworkTest {
    
    private EventBus eventBus;
    
    @Before
    public void setUp() {
        eventBus = new DefaultEventBus();
    }
    
    @Test
    public void testEventPublishAndSubscribe() {
        TestEventListener listener = new TestEventListener();
        eventBus.register(listener);
        
        TestEvent event = new TestEvent("test-id", "test-type", "test-data");
        eventBus.publish(event);
        
        // 等待事件处理完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue("事件应该被处理", listener.isEventHandled());
    }
    
    /**
     * 测试事件类
     */
    public static class TestEvent implements Event {
        private final String id;
        private final String type;
        private final Object data;
        private final long timestamp;
        private final Map<String, Object> metadata;
        
        public TestEvent(String id, String type, Object data) {
            this.id = id;
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
        
        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public String getType() {
            return type;
        }
        
        @Override
        public Object getData() {
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
    }
    
    /**
     * 测试事件监听器
     */
    public static class TestEventListener {
        private final AtomicBoolean eventHandled = new AtomicBoolean(false);
        
        @EventSubscribe(eventType = "test-type")
        public void handleTestEvent(TestEvent event) {
            System.out.println("Received event: " + event.getId());
            eventHandled.set(true);
        }
        
        public boolean isEventHandled() {
            return eventHandled.get();
        }
    }
}