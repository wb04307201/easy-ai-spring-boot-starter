package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAIConfiguration;
import cn.wubo.easy.ai.core.EasyAIService;
import cn.wubo.easy.ai.core.Payload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, OllamaAutoConfiguration.class, ChromaVectorStoreAutoConfiguration.class, EasyAIConfiguration.class})
class EasyAIServiceTest {

    @Autowired
    ChatModel chatModel;

    @Autowired
    EasyAIService easyAiService;

    @Autowired
    VectorStore vectorStore;

    @Test
    void testCall() {
        System.out.println(chatModel.call("hello?"));
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("hello?"));
        System.out.println(chatModel.call(new Prompt(messages)));
    }

    @Test
    void testService() {
        ChatResponse chatResponse = easyAiService.chat(new Payload(List.of(new Payload.Message(Payload.Role.USER, "hello?"))));
        System.out.println(chatResponse.getResult().getOutput());
    }

    @Test
    void testVectorStore() {
        Resource fileResource = new FileSystemResource("test.pdf");
        easyAiService.saveSource(fileResource);
    }

    @Test
    void testGetVectorStore() {
        List<Document> listOfSimilarDocuments = vectorStore.similaritySearch("数据要素");
        log.debug("找到向量数据 {} 内容 {}", listOfSimilarDocuments.size(), listOfSimilarDocuments);
    }
}