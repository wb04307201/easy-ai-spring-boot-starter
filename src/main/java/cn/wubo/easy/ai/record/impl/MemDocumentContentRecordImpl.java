package cn.wubo.easy.ai.record.impl;

import cn.wubo.easy.ai.dto.DocumentContentDTO;
import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.record.IDocumentContentRecord;
import cn.wubo.easy.ai.record.IDocumentStorageRecord;

import java.util.ArrayList;
import java.util.List;

public class MemDocumentContentRecordImpl implements IDocumentContentRecord {

    private static List<DocumentContentDTO> documentContentDTOS = new ArrayList<>();

    @Override
    public DocumentContentDTO save(DocumentContentDTO documentContentDTO) {
        return null;
    }

    @Override
    public List<DocumentContentDTO> list(DocumentContentDTO documentContentDTO) {
        return List.of();
    }

    @Override
    public DocumentContentDTO findById(String id) {
        return null;
    }

    @Override
    public Boolean delete(DocumentContentDTO documentContentDTO) {
        return null;
    }

    @Override
    public void init() {

    }
}
