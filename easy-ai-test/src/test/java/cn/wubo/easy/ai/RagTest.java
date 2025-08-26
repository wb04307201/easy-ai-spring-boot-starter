package cn.wubo.easy.ai;

import cn.wubo.easy.ai.autoconfigure.EasyAiConfiguration;
import cn.wubo.easy.ai.core.ChatRecord;
import cn.wubo.easy.ai.core.DocumntService;
import cn.wubo.easy.ai.core.EasyAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Slf4j
@TestPropertySource(locations = "classpath:application-rag.yml")
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, OllamaChatAutoConfiguration.class, OllamaEmbeddingAutoConfiguration.class, ObjectMapper.class, ChromaVectorStoreAutoConfiguration.class, EasyAiConfiguration.class})
class RagTest {

    @Resource
    EasyAiService easyAiService;

    @Resource
    DocumntService documntService;

    @Test
    void testChat() {
        String conversationId = RandomStringUtils.random(5);
        ChatRecord chatRecord1 = new ChatRecord("你好!", conversationId);
        ChatResponse chatResponse = easyAiService.chat(chatRecord1);
        log.info(chatResponse.getResult().getOutput().toString());

        ChatRecord chatRecord2 = new ChatRecord("能给我讲个故事吗？", conversationId);
        chatResponse = easyAiService.chat(chatRecord2);
        log.info(chatResponse.getResult().getOutput().toString());

        ChatRecord chatRecord3 = new ChatRecord("我上一个问题是什么？", conversationId);
        chatResponse = easyAiService.chat(chatRecord3);
        log.info(chatResponse.getResult().getOutput().toString());
    }
}