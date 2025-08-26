package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.core.DocumentStorageDTO;
import cn.wubo.easy.ai.document.IDocumentStorageRecord;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        Optional<DocumentStorageDTO> optionalFileInfo = documentStorageDTOS.stream().filter(e -> e.getId().equals(id)).findAny();
        if (optionalFileInfo.isPresent()) return optionalFileInfo.get();
        else throw new EasyAiRuntimeException("文件记录未找到!");
    }

    @Override
    public Boolean delete(DocumentStorageDTO documentStorageDTO) {
        return documentStorageDTOS.removeAll(list(documentStorageDTO));
    }

    @Override
    public void init() {

    }
}
