package cn.wubo.easy.ai.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentStorageDTO {
    private String id;
    /**
     * 原文件名
     */
    private String originalFilename;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 转换后文件定位
     */
    private String filePath;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 文件状态
     */
    private String state;
}
