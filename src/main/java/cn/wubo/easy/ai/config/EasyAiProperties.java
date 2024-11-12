package cn.wubo.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.easy")
public class EasyAiProperties {
    private String defaultSystem;
    private Boolean enableRag = true;
    private String userTextAdvise = "\n上下文信息如下，用---------------------包围\n\n---------------------\n{question_answer_context}\n---------------------\n\n基于上下文和提供的历史信息（而非先验知识），回复用户评论。如果答案不在上下文中，请告知用户无法回答这个问题。\n";
    private String fileStorageServiceClass = "cn.wubo.easy.ai.document.impl.LocalDocumentStorageServiceImpl";
    private String fileStorageRecordClass = "cn.wubo.easy.ai.document.impl.MemDocumentStorageRecordImpl";
    private Boolean enableWeb = Boolean.TRUE;
    private Boolean enableRest = Boolean.TRUE;
}
