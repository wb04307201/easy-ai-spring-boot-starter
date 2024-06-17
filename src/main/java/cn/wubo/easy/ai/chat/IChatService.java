package cn.wubo.easy.ai.chat;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

public interface IChatService {

    ChatResponse chat(Prompt prompt);
}
