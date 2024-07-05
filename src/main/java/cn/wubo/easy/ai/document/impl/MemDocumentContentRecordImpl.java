package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.document.IDocumentContentRecord;
import cn.wubo.easy.ai.dto.DocumentContentDTO;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MemDocumentContentRecordImpl implements IDocumentContentRecord {

    private static List<DocumentContentDTO> documentContentDTOS = new ArrayList<>();

    @Override
    public DocumentContentDTO save(DocumentContentDTO documentContentDTO) {
        if (StringUtils.hasLength(documentContentDTO.getId())) {
            documentContentDTOS.stream().filter(e -> e.getId().equals(documentContentDTO.getId())).findAny().ifPresent(e -> e = documentContentDTO);
        } else {
            documentContentDTO.setId(UUID.randomUUID().toString());
            documentContentDTOS.add(documentContentDTO);
        }
        return documentContentDTO;
    }

    @Override
    public List<DocumentContentDTO> list(DocumentContentDTO documentContentDTO) {
        // @formatter:off
        return documentContentDTOS.stream()
                .filter(e -> !StringUtils.hasLength(documentContentDTO.getId()) || e.getId().equals(documentContentDTO.getId()))
                .filter(e -> !StringUtils.hasLength(documentContentDTO.getStorageId()) || e.getStorageId().equals(documentContentDTO.getStorageId()))
                .toList();
        // @formatter:on
    }

    @Override
    public DocumentContentDTO findById(String id) {
        Optional<DocumentContentDTO> optionalFileInfo = documentContentDTOS.stream().filter(e -> e.getId().equals(id)).findAny();
        if (optionalFileInfo.isPresent()) return optionalFileInfo.get();
        else throw new EasyAiRuntimeException("文件记录未找到!");
    }

    @Override
    public Boolean delete(DocumentContentDTO documentContentDTO) {
        return documentContentDTOS.removeAll(list(documentContentDTO));
    }

    @Override
    public void init() {

    }
}
