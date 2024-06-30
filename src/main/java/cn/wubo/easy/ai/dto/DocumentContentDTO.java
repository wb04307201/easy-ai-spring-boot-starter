package cn.wubo.easy.ai.dto;

import lombok.Data;
import org.springframework.ai.document.Document;

@Data
public class DocumentContentDTO {
    private String id;
    private String storageId;
    private Document document;
}
