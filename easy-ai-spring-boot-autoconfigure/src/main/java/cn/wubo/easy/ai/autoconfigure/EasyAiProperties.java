package cn.wubo.easy.ai.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.easy")
public class EasyAiProperties {
    private String defaultSystem;
    private Boolean enableRag = true;
    private Rag rag = new Rag();

    @Data
    public class Rag{
        private double similarityThreshold = 0.8;
        private int topK = 4;
        private String template = """
            <query>

            上下文信息如下.

			---------------------
			<question_answer_context>
			---------------------

            根据上下文信息，且不借助任何先验知识，回答该查询。
                
            遵循以下规则：
            1. 如果答案不在上下文中，只需说明你不知道。
            2. 避免使用“根据上下文……”或“所提供的信息……”这类表述。
            """;
    }
}
