package cn.wubo.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.easy")
public class EasyAiProperties {
    /*private String systemPromptTemplate = """
            你需要使用文档内容对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
            不能在回复中体现出你是根据给出的文档内容进行回复的，这点非常重要。

            当用户提出的问题无法根据文档内容进行回复或者你也不清楚时，回复不知道即可。

            文档内容如下:
            {documents}
            """;*/
    private String systemPromptTemplate = """
            下面的信息({documents})是否有这个问题({message})有关，
            如果你觉得无关请直接回答({message})这个问题，
            否则请根据({documents})对({message})的问题进行回答。
            """;
    private String fileStorageServiceClass = "cn.wubo.easy.ai.document.impl.LocalDocumentStorageServiceImpl";
    private String fileStorageRecordClass = "cn.wubo.easy.ai.document.impl.MemDocumentStorageRecordImpl";
    private Boolean enableWeb = Boolean.TRUE;
    private Boolean enableRest = Boolean.TRUE;
}
