package cn.wubo.easy.ai.record.impl;

import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.record.IDocumentStorageRecord;

import java.util.ArrayList;
import java.util.List;

public class MemDocumentStorageRecordImpl implements IDocumentStorageRecord {

    private static List<DocumentStorageDTO> documentStorageDTOS = new ArrayList<>();

    @Override
    public DocumentStorageDTO save(DocumentStorageDTO documentStorageDTO) {
        return null;
    }

    @Override
    public List<DocumentStorageDTO> list(DocumentStorageDTO documentStorageDTO) {
        return List.of();
    }

    @Override
    public DocumentStorageDTO findById(String id) {
        return null;
    }

    @Override
    public Boolean delete(DocumentStorageDTO documentStorageDTO) {
        return null;
    }

    @Override
    public void init() {

    }
}
