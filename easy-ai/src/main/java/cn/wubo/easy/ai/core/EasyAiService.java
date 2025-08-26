package cn.wubo.easy.ai.core;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public class EasyAiService {

    private final ChatClient chatClient;

    public EasyAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatResponse chat(ChatRecord chatRecord) {
        // @formatter:off
        return chatClient
                .prompt()
                .user(chatRecord.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRecord.conversationId()))
                .call()
                .chatResponse();
        // @formatter:on
    }

    public Flux<String> stream(ChatRecord chatRecord) {
        // @formatter:off
        return chatClient
                .prompt()
                .user(chatRecord.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRecord.conversationId()))
                .stream()
                .content();
        // @formatter:on
    }
}
