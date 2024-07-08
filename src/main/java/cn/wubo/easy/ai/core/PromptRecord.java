package cn.wubo.easy.ai.core;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.stream.Collectors;

public record PromptRecord(List<Message> messages) {

    public Prompt getPromptMessages() {
        return new Prompt(messages().stream().filter(e -> MessageType.USER.getValue().equals(e.messageType()) || MessageType.ASSISTANT.getValue().equals(e.messageType())).map(e -> {
            if (MessageType.USER.getValue().equals(e.messageType())) return new UserMessage(e.textContent());
            else return new AssistantMessage(e.textContent());
        }).collect(Collectors.toList()));
    }

    public record Message(String messageType, String textContent) {

    }
}
