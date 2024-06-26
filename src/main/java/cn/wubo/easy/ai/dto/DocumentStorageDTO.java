package cn.wubo.easy.ai.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentStorageDTO {
    private String id;
    private String originalFilename;
    private String fileName;
    private String filePath;
    private Date createTime;
    private String state;
}
