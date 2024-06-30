package cn.wubo.easy.ai.file;

import cn.wubo.easy.ai.dto.DocumentStorageDTO;

import java.util.List;

public interface IDocumentStorageRecord {

    DocumentStorageDTO save(DocumentStorageDTO documentStorageDTO);

    List<DocumentStorageDTO> list(DocumentStorageDTO documentStorageDTO);

    DocumentStorageDTO findById(String id);

    Boolean delete(DocumentStorageDTO documentStorageDTO);

    void init();
}
