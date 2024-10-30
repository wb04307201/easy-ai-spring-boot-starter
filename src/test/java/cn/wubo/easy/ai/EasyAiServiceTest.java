package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAiConfiguration;
import cn.wubo.easy.ai.core.ChatRecord;
import cn.wubo.easy.ai.core.EasyAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, OllamaAutoConfiguration.class, ObjectMapper.class, ChromaVectorStoreAutoConfiguration.class, EasyAiConfiguration.class})
class EasyAiServiceTest {

    @Resource
    EasyAiService easyAiService;

    @Resource
    ChatModel chatModel;

    @Test
    void testChatModel() {
        log.debug(chatModel.call("Hello world!"));
    }

    @Test
    void testEasyAiService() {
        ChatRecord chatRecord = new ChatRecord("你好", RandomStringUtils.random(5));
        ChatResponse chatResponse = easyAiService.chat(chatRecord);
        log.debug(chatResponse.getResult().getOutput().toString());
    }
}