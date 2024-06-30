package cn.wubo.easy.ai.document;

import cn.wubo.easy.ai.dto.DocumentContentDTO;

import java.util.List;

public interface IDocumentContentRecord {

    DocumentContentDTO save(DocumentContentDTO documentContentDTO);

    List<DocumentContentDTO> list(DocumentContentDTO documentContentDTO);

    DocumentContentDTO findById(String id);

    Boolean delete(DocumentContentDTO documentContentDTO);

    void init();
}
