package cn.wubo.easy.ai.file.impl;

import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.file.IDocumentStorageRecord;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemDocumentStorageRecordImpl implements IDocumentStorageRecord {

    private static List<DocumentStorageDTO> documentStorageDTOS = new ArrayList<>();

    @Override
    public DocumentStorageDTO save(DocumentStorageDTO documentStorageDTO) {
        if (StringUtils.hasLength(documentStorageDTO.getId())) {
            documentStorageDTOS.stream().filter(e -> e.getId().equals(documentStorageDTO.getId())).findAny().ifPresent(e -> e = documentStorageDTO);
        } else {
            documentStorageDTO.setId(UUID.randomUUID().toString());
            documentStorageDTOS.add(documentStorageDTO);
        }
        return documentStorageDTO;
    }

    @Override
    public List<DocumentStorageDTO> list(DocumentStorageDTO documentStorageDTO) {
        // @formatter:off
        return documentStorageDTOS.stream()
                .filter(e -> !StringUtils.hasLength(documentStorageDTO.getId()) || e.getId().equals(documentStorageDTO.getId())).
                filter(e -> !StringUtils.hasLength(documentStorageDTO.getFileName()) || e.getFileName().contains(documentStorageDTO.getFileName()))
                .filter(e -> !StringUtils.hasLength(documentStorageDTO.getFilePath()) || e.getFilePath().contains(documentStorageDTO.getFilePath()))
                .toList();
        // @formatter:on
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
