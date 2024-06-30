package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.document.IDocumentContentRecord;
import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.document.IDocumentService;
import cn.wubo.easy.ai.document.IVectorStoreService;
import cn.wubo.easy.ai.dto.DocumentContentDTO;
import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.file.IDocumentStorageRecord;
import cn.wubo.easy.ai.file.IFileStorageService;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public class DocumentServiceImpl implements IDocumentService {

    private final IFileStorageService documentStorageService;
    private final IDocumentStorageRecord documentStorageRecord;
    private final IDocumentReaderService documentReaderService;
    private final IDocumentContentRecord documentContentRecord;
    private final IVectorStoreService vectorStoreService;

    public DocumentServiceImpl(IFileStorageService documentStorageService, IDocumentStorageRecord documentStorageRecord, IDocumentReaderService documentReaderService, IDocumentContentRecord documentContentRecord, IVectorStoreService vectorStoreService) {
        this.documentStorageService = documentStorageService;
        this.documentStorageRecord = documentStorageRecord;
        this.documentReaderService = documentReaderService;
        this.documentContentRecord = documentContentRecord;
        this.vectorStoreService = vectorStoreService;
    }

    @Async(value = "easyAiExecutor")
    @Override
    public void read(DocumentStorageDTO documentStorageDTO) {
        documentStorageDTO.setState("10");
        documentStorageDTO = documentStorageRecord.save(documentStorageDTO);
        Resource resource = documentStorageService.getResource(documentStorageDTO.getFilePath());
        List<Document> documentList = documentReaderService.read(resource);
        for (Document document : documentList) {
            DocumentContentDTO documentContentDTO = new DocumentContentDTO();
            documentContentDTO.setStorageId(documentStorageDTO.getId());
            documentContentDTO.setDocument(document);
            documentContentRecord.save(documentContentDTO);
        }
        documentStorageDTO.setState("20");
        documentStorageDTO = documentStorageRecord.save(documentStorageDTO);
        vectorStoreService.save(documentStorageDTO);
    }
}
