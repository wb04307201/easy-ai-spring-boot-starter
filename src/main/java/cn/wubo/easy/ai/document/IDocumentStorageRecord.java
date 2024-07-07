package cn.wubo.easy.ai.document;

import cn.wubo.easy.ai.core.DocumentStorageDTO;

import java.util.List;

public interface IDocumentStorageRecord {

    DocumentStorageDTO save(DocumentStorageDTO documentStorageDTO);

    List<DocumentStorageDTO> list(DocumentStorageDTO documentStorageDTO);

    DocumentStorageDTO findById(String id);

    Boolean delete(DocumentStorageDTO documentStorageDTO);

    void init();
}
