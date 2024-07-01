package cn.wubo.easy.ai.core;

import cn.wubo.easy.ai.document.IDocumentService;
import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import cn.wubo.easy.ai.file.IDocumentStorageRecord;
import cn.wubo.easy.ai.file.IFileStorageService;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class EasyAiService {

    /*private static final String SYSTEM_PROMPT = """
            你需要使用文档内容对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
            不能在回复中体现出你是根据给出的文档内容进行回复的，这点非常重要。

            当用户提出的问题无法根据文档内容进行回复或者你也不清楚时，回复不知道即可。

            文档内容如下:
            {documents}

            """;*/

    private static final String SYSTEM_PROMPT = """
            下面的信息({summary_prompt})是否有这个问题({message})有关，
            如果你觉得无关请告诉我无法根据提供的上下文回答'{message}'这个问题，
            简要回答即可，
            否则请根据{summary_prompt}对{message}的问题进行回答
            """;

    private final IFileStorageService documentStorageService;
    private final IDocumentStorageRecord documentStorageRecord;
    private final IDocumentService documentService;

    public EasyAiService(IFileStorageService documentStorageService, IDocumentStorageRecord documentStorageRecord, IDocumentService documentService) {
        this.documentStorageService = documentStorageService;
        this.documentStorageRecord = documentStorageRecord;
        this.documentService = documentService;
    }

    public List<DocumentStorageDTO> upload(MultiValueMap<String, Part> multiValueMap) {
        List<DocumentStorageDTO> documentStorageDTOS = new ArrayList<>();
        List<Part> parts = multiValueMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()).toList();
        try {
            for (Part part : parts) {
                DocumentStorageDTO documentStorageDTO = new DocumentStorageDTO();
                documentStorageDTO.setFileName(part.getSubmittedFileName());
                documentStorageDTO.setFilePath(documentStorageService.save(part.getInputStream(), documentStorageDTO.getFileName()));
                documentStorageDTO.setState("00");
                documentStorageDTO.setCreateTime(new Date());
                documentStorageDTO = documentStorageRecord.save(documentStorageDTO);
                documentService.read(documentStorageDTO);
                documentStorageDTOS.add(documentStorageDTO);
            }
            return documentStorageDTOS;
        } catch (IOException e) {
            throw new EasyAiRuntimeException(e.getMessage(), e);
        }
    }
}
