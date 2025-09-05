package io.github.nemoob.event.core;

/**
 * 分布式事件接口
 * 实现此接口的事件可以在分布式系统中传播
 */
public interface DistributedEvent extends Event {
    
    /**
     * 获取事件的目标节点ID
     * 如果为null或空，则表示广播到所有节点
     * 
     * @return 目标节点ID
     */
    String getTargetNodeId();
    
    /**
     * 获取事件的源节点ID
     * 
     * @return 源节点ID
     */
    String getSourceNodeId();
    
    /**
     * 设置事件的源节点ID
     * 
     * @param sourceNodeId 源节点ID
     */
    void setSourceNodeId(String sourceNodeId);
    
    /**
     * 检查事件是否已经在本地处理过
     * 
     * @return 是否已经在本地处理过
     */
    boolean isProcessedLocally();
    
    /**
     * 设置事件是否已经在本地处理过
     * 
     * @param processedLocally 是否已经在本地处理过
     */
    void setProcessedLocally(boolean processedLocally);
}