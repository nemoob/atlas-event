package io.github.nemoob.event.core;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Kafka分布式事件总线
 * 通过Kafka实现分布式事件发布与接收
 */
public class KafkaDistributedEventBus implements EventBus {

    private final EventBus localEventBus;
    private final String nodeId;
    private final String kafkaTopic;
    private final KafkaProducer kafkaProducer;
    private final KafkaConsumer kafkaConsumer;
    private final ExecutorService executorService;
    private volatile boolean running = true;
    
    /**
     * 创建Kafka分布式事件总线
     *
     * @param localEventBus 本地事件总线
     * @param bootstrapServers Kafka服务器地址
     * @param topic Kafka主题
     * @param groupId 消费者组ID
     */
    public KafkaDistributedEventBus(EventBus localEventBus, String bootstrapServers, 
                                   String topic, String groupId) {
        this.localEventBus = localEventBus;
        this.nodeId = UUID.randomUUID().toString();
        this.kafkaTopic = topic;
        
        // 配置Kafka生产者
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", bootstrapServers);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.kafkaProducer = new KafkaProducer(producerProps);
        
        // 配置Kafka消费者
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", groupId);
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset", "latest");
        this.kafkaConsumer = new KafkaConsumer(consumerProps);
        
        // 启动消费者线程
        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(this::consumeEvents);
    }
    
    @Override
    public void publish(Event event) {
        // 先在本地处理事件
        localEventBus.publish(event);
        
        // 如果是分布式事件，则发布到Kafka
        if (event instanceof DistributedEvent) {
            DistributedEvent distributedEvent = (DistributedEvent) event;
            
            // 设置源节点ID
            distributedEvent.setSourceNodeId(nodeId);
            
            // 标记为已在本地处理
            distributedEvent.setProcessedLocally(true);
            
            // 发布到指定节点或广播
            String targetNodeId = distributedEvent.getTargetNodeId();
            if (targetNodeId == null || targetNodeId.isEmpty()) {
                // 广播到所有节点
                publishToKafka(distributedEvent);
            } else {
                // 发布到指定节点
                publishToKafka(distributedEvent, targetNodeId);
            }
        }
    }
    
    /**
     * 发布事件到Kafka（广播）
     *
     * @param event 分布式事件
     */
    private void publishToKafka(DistributedEvent event) {
        try {
            String eventJson = serializeEvent(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(kafkaTopic, event.getId(), eventJson);
            kafkaProducer.send(record);
        } catch (Exception e) {
            System.err.println("Error publishing event to Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 发布事件到Kafka（指定目标节点）
     *
     * @param event 分布式事件
     * @param targetNodeId 目标节点ID
     */
    private void publishToKafka(DistributedEvent event, String targetNodeId) {
        try {
            String eventJson = serializeEvent(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(kafkaTopic, targetNodeId, eventJson);
            kafkaProducer.send(record);
        } catch (Exception e) {
            System.err.println("Error publishing event to Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 消费Kafka事件
     */
    private void consumeEvents() {
        try {
            kafkaConsumer.subscribe(Collections.singletonList(kafkaTopic));
            
            while (running) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    // 只处理发给本节点的消息或广播消息
                    String key = record.key();
                    if (key == null || key.equals(nodeId) || key.isEmpty()) {
                        try {
                            DistributedEvent event = deserializeEvent(record.value());
                            
                            // 避免处理自己发出的事件
                            if (!nodeId.equals(event.getSourceNodeId())) {
                                receiveRemoteEvent(event);
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing Kafka message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Error in Kafka consumer: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            kafkaConsumer.close();
        }
    }
    
    /**
     * 接收远程事件
     *
     * @param event 分布式事件
     */
    public void receiveRemoteEvent(DistributedEvent event) {
        // 标记事件来源为远程，避免再次发布到Kafka
        event.setProcessedLocally(false);
        
        // 在本地处理事件
        localEventBus.publish(event);
    }
    
    /**
     * 序列化事件对象为JSON字符串
     *
     * @param event 事件对象
     * @return JSON字符串
     */
    private String serializeEvent(DistributedEvent event) {
        // 在实际项目中应使用Jackson或Gson等库进行序列化
        // 这里简化实现，实际项目中需要完整实现
        return "{ \"id\": \"" + event.getId() + "\", \"type\": \"" + event.getType() + "\", \"sourceNodeId\": \"" + 
               event.getSourceNodeId() + "\", \"processedLocally\": " + event.isProcessedLocally() + " }";
    }
    
    /**
     * 反序列化JSON字符串为事件对象
     *
     * @param json JSON字符串
     * @return 事件对象
     */
    private DistributedEvent deserializeEvent(String json) {
        // 在实际项目中应使用Jackson或Gson等库进行反序列化
        // 这里简化实现，实际项目中需要完整实现
        // 返回一个简单的DistributedEvent实现
        return new SimpleDistributedEvent();
    }
    
    @Override
    public void register(Object listener) {
        localEventBus.register(listener);
    }
    
    @Override
    public void unregister(Object listener) {
        localEventBus.unregister(listener);
    }
    
    @Override
    public void scanAndRegister(Object listener) {
        localEventBus.scanAndRegister(listener);
    }
    
    /**
     * 关闭分布式事件总线
     */
    public void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        kafkaProducer.close();
    }
    
    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    // 注意：这个类仅用于示例，实际项目中需要导入Kafka客户端库并使用真实的Kafka API
    private static class KafkaProducer {
        public KafkaProducer(Properties properties) {
            // 初始化Kafka生产者
        }
        
        public void send(ProducerRecord<String, String> record) {
            // 发送消息到Kafka
        }
        
        public void close() {
            // 关闭生产者
        }
    }
    
    private static class KafkaConsumer {
        public KafkaConsumer(Properties properties) {
            // 初始化Kafka消费者
        }
        
        public void subscribe(List<String> topics) {
            // 订阅主题
        }
        
        public ConsumerRecords<String, String> poll(Duration duration) {
            // 拉取消息
            return new ConsumerRecords<>();
        }
        
        public void close() {
            // 关闭消费者
        }
    }
    
    private static class ProducerRecord<K, V> {
        private final String topic;
        private final K key;
        private final V value;
        
        public ProducerRecord(String topic, K key, V value) {
            this.topic = topic;
            this.key = key;
            this.value = value;
        }
    }
    
    private static class ConsumerRecord<K, V> {
        private final K key;
        private final V value;
        
        public ConsumerRecord(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        public K key() {
            return key;
        }
        
        public V value() {
            return value;
        }
    }
    
    private static class ConsumerRecords<K, V> implements Iterable<ConsumerRecord<K, V>> {
        @Override
        public Iterator<ConsumerRecord<K, V>> iterator() {
            return Collections.emptyIterator();
        }
    }
    
    private static class Duration {
        public static Duration ofMillis(long millis) {
            return new Duration();
        }
    }
    
    /**
     * 简单的分布式事件实现
     */
    private static class SimpleDistributedEvent implements DistributedEvent {
        private String id = UUID.randomUUID().toString();
        private String type = "simple.distributed.event";
        private String sourceNodeId;
        private String targetNodeId;
        private boolean processedLocally;
        private Map<String, Object> data = new HashMap<>();
        private long timestamp = System.currentTimeMillis();
        private Map<String, Object> metadata = new HashMap<>();
        
        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public String getType() {
            return type;
        }
        
        @Override
        public Map<String, Object> getData() {
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
        
        @Override
        public String getTargetNodeId() {
            return targetNodeId;
        }
        
        @Override
        public String getSourceNodeId() {
            return sourceNodeId;
        }
        
        @Override
        public void setSourceNodeId(String sourceNodeId) {
            this.sourceNodeId = sourceNodeId;
        }
        
        @Override
        public boolean isProcessedLocally() {
            return processedLocally;
        }
        
        @Override
        public void setProcessedLocally(boolean processedLocally) {
            this.processedLocally = processedLocally;
        }
    }
}