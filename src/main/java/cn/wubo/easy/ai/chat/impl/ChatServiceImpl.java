package cn.wubo.easy.ai.chat.impl;


import cn.wubo.easy.ai.chat.IChatService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

public class ChatServiceImpl implements IChatService {

    private final ChatModel chatModel;

    public ChatServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatResponse chat(Prompt prompt) {
        return chatModel.call(prompt);
    }
}
