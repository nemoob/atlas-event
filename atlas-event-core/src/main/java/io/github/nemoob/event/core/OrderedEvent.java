package io.github.nemoob.event.core;

/**
 * 有序事件接口
 * 实现此接口的事件将按照orderKey进行顺序处理
 */
public interface OrderedEvent extends Event {
    
    /**
     * 获取事件的顺序键
     * 具有相同orderKey的事件将按照发布顺序依次处理
     * 
     * @return 顺序键
     */
    String getOrderKey();
}