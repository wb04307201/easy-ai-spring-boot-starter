package cn.wubo.easy.ai.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentStorageDTO {
    private String id;
    private String fileName;
    private String filePath;
    private Date createTime;
    /* 00 上传 10 文档拆分中 20 文档拆分完 30 向量存储中 40向量存储完 */
    private String state;
    private Date updateTime;
}
