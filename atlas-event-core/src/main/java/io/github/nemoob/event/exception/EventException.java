package io.github.nemoob.event.exception;

/**
 * 事件异常类
 */
public class EventException extends RuntimeException {
    
    public EventException(String message) {
        super(message);
    }
    
    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}