package cn.wubo.easy.ai.document.storage;

import org.springframework.core.io.Resource;

public interface IFileStorageService {

    FileStorageDTO save(byte[] bytes, String fileName);

    Boolean delete(FileStorageDTO filePreviewInfo);

    Resource getBytes(FileStorageDTO dto);

    void init();
}
