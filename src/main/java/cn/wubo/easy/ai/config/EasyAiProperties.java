package cn.wubo.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.easy")
public class EasyAiProperties {
    private String documentStorageClass = "cn.wubo.easy.ai.file.impl.LocalDocumentStorageServiceImpl";
}
