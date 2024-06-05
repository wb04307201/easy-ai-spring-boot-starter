package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAiConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiAutoConfiguration;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, OllamaAutoConfiguration.class, ChromaVectorStoreAutoConfiguration.class, EasyAiConfiguration.class})
//@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, ZhiPuAiAutoConfiguration.class, ChromaVectorStoreAutoConfiguration.class, EasyAiConfiguration.class})
class EasyAiServiceTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    public void testCall() {

        System.out.println(chatModel.call("hello?"));

        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("hello?"));
        System.out.println(chatModel.call(new Prompt(messages)));
    }
}