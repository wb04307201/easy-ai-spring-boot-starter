package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAiConfiguration;
import cn.wubo.easy.ai.core.EasyAiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, OllamaAutoConfiguration.class, ChromaVectorStoreAutoConfiguration.class, EasyAiConfiguration.class})
class EasyAiServiceTest {

    @Autowired
    ChatModel chatModel;

    @Autowired
    EasyAiService easyAiService;

    @Autowired
    VectorStore vectorStore;

    @Test
    void testEasyAiService() {
        ChatResponse chatResponse = easyAiService.chat(new Prompt(List.of(new UserMessage("hello?"))));
        log.debug(chatResponse.getResult().getOutput().toString());
    }

    @Test
    void testGetVectorStore() {
        List<Document> listOfSimilarDocuments = vectorStore.similaritySearch("数据要素");
        log.debug("找到向量数据 {} 内容 {}", listOfSimilarDocuments.size(), listOfSimilarDocuments);
    }
}