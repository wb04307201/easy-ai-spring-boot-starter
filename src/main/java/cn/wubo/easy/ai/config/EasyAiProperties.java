package cn.wubo.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.easy")
public class EasyAiProperties {
    private String defaultSystem = "请尽量使用中文回答问题";
    private String fileStorageServiceClass = "cn.wubo.easy.ai.document.impl.LocalDocumentStorageServiceImpl";
    private String fileStorageRecordClass = "cn.wubo.easy.ai.document.impl.MemDocumentStorageRecordImpl";
    private Boolean enableWeb = Boolean.TRUE;
    private Boolean enableRest = Boolean.TRUE;
}
