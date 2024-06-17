package cn.wubo.easy.ai.core;

import java.util.List;

public record Payload(List<Message> messages) {

    public record Message(Role role, String content) {

    }

    public enum Role {
        SYSTEM("system"), USER("user"), ASSISTANT("assistant"), FUNCTION("function");

        private String value;

        Role(String value) {
            this.value = value;
        }
    }
}
