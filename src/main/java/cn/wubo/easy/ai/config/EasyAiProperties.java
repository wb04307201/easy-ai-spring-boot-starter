package cn.wubo.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.easy")
public class EasyAiProperties {
    private String defaultSystem;
    private Boolean enableRag = true;
   private String userTextAdvise = """

			上下文信息如下，用"---------------------"包围

			---------------------
			{question_answer_context}
			---------------------

			请基于上下文和提供的历史信息，回复用户。如果答案不在上下文中或者上下文为空，请告知用户无法回答这个问题。
			""";
    private String fileStorageServiceClass = "cn.wubo.easy.ai.document.impl.LocalDocumentStorageServiceImpl";
    private String fileStorageRecordClass = "cn.wubo.easy.ai.document.impl.MemDocumentStorageRecordImpl";
    private Boolean enableWeb = Boolean.TRUE;
    private Boolean enableRest = Boolean.TRUE;
}
