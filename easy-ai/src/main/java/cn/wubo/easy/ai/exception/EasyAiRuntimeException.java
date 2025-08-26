package cn.wubo.easy.ai.exception;

public class EasyAiRuntimeException extends RuntimeException{

    public EasyAiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EasyAiRuntimeException(String message) {
        super(message);
    }
}
