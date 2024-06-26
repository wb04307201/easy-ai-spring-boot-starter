package cn.wubo.easy.ai.document;

import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import org.springframework.core.io.Resource;

public interface IDocumentStorageService {

    DocumentStorageDTO save(byte[] bytes, String fileName);

    Boolean delete(DocumentStorageDTO filePreviewInfo);

    Resource getBytes(DocumentStorageDTO dto);

    void init();
}
