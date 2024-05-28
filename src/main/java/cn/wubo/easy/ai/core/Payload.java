package cn.wubo.easy.ai.core;

import java.util.List;

public record Payload(List<Message> messages) {

    public record Message(Role role, String content) {

    }
}
