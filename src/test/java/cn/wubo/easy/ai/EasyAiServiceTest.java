package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAiConfiguration;
import cn.wubo.easy.ai.core.EasyAiService;
import cn.wubo.easy.ai.core.Payload;
import cn.wubo.easy.ai.core.Role;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiAutoConfiguration;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, ZhiPuAiAutoConfiguration.class, ChromaVectorStoreAutoConfiguration.class, EasyAiConfiguration.class})
class EasyAiServiceTest {

    @Autowired
    private EasyAiService easyAiService;

    @Test
    public void pdf2VectorStore() {
        Resource fileResource = new FileSystemResource("test.pdf");
        easyAiService.saveSource(fileResource);
    }

    @Test
    public void testCall() {
        Payload payload1 = new Payload(List.of(new Payload.Message(Role.USER, "华为mate60")));
        ChatResponse rsp1 = easyAiService.chat(payload1);

        Payload payload2 = new Payload(List.of(new Payload.Message(Role.USER, "华为mate60"), new Payload.Message(Role.ASSISTANT, rsp1.getResult().getOutput().getContent()), new Payload.Message(Role.USER, "小米14")));
        ChatResponse rsp2 = easyAiService.chat(payload2);

        Payload payload3 = new Payload(List.of(new Payload.Message(Role.USER, "华为mate60"), new Payload.Message(Role.ASSISTANT, rsp1.getResult().getOutput().getContent()), new Payload.Message(Role.USER, "小米14"), new Payload.Message(Role.ASSISTANT, rsp2.getResult().getOutput().getContent()), new Payload.Message(Role.USER, "那他俩谁的性价比更高呢？")));
        ChatResponse rsp3 = easyAiService.chat(payload3);

        System.out.println(rsp3.getResult().getOutput().getContent());
    }
}