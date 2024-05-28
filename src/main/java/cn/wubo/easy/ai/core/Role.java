package cn.wubo.easy.ai.core;

public enum Role {
    SYSTEM("system"), USER("user"), ASSISTANT("assistant"), FUNCTION("function");

    private String value;

    Role(String value) {
        this.value = value;
    }
}
