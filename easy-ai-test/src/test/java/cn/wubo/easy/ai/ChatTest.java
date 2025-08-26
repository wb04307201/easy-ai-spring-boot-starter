package cn.wubo.easy.ai;

import cn.wubo.easy.ai.autoconfigure.EasyAiConfiguration;
import cn.wubo.easy.ai.core.ChatRecord;
import cn.wubo.easy.ai.core.EasyAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

@Slf4j
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, OllamaChatAutoConfiguration.class, ObjectMapper.class,  EasyAiConfiguration.class})
class ChatTest {

    @Resource
    EasyAiService easyAiService;

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

        ChatRecord chatRecord4 = new ChatRecord("现在是什么日期什么时间？", conversationId);
        chatResponse = easyAiService.chat(chatRecord4);
        log.info(chatResponse.getResult().getOutput().toString());

        ChatRecord chatRecord5 = new ChatRecord("明天是什么日期？", conversationId);
        chatResponse = easyAiService.chat(chatRecord5);
        log.info(chatResponse.getResult().getOutput().toString());
    }

    @Test
    void testStream() {
        ChatRecord chatRecord = new ChatRecord("你好!", RandomStringUtils.random(5));
        Flux<String> strs = easyAiService.stream(chatRecord);
        strs.collectList().block().stream().forEach(log::info);
    }
}