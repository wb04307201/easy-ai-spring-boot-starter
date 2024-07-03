package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.document.IDocumentContentRecord;
import cn.wubo.easy.ai.document.IVectorStoreService;
import cn.wubo.easy.ai.dto.DocumentContentDTO;
import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.file.IDocumentStorageRecord;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;

public class VectorStoreServiceImpl implements IVectorStoreService {

    private final IDocumentStorageRecord documentStorageRecord;
    private final IDocumentContentRecord documentContentRecord;
    private final VectorStore vectorStore;

    public VectorStoreServiceImpl(IDocumentStorageRecord documentStorageRecord, IDocumentContentRecord documentContentRecord, VectorStore vectorStore) {
        this.documentStorageRecord = documentStorageRecord;
        this.documentContentRecord = documentContentRecord;
        this.vectorStore = vectorStore;
    }

    @Async(value = "easyAiExecutor")
    @Override
    public void save(DocumentStorageDTO documentStorageDTO) {
        documentStorageDTO.setState("30");
        documentStorageDTO.setUpdateTime(new Date());
        documentStorageDTO = documentStorageRecord.save(documentStorageDTO);
        DocumentContentDTO query = new DocumentContentDTO();
        query.setStorageId(documentStorageDTO.getId());
        vectorStore.accept(documentContentRecord.list(query).stream().map(DocumentContentDTO::getDocument).toList());
        documentStorageDTO.setState("40");
        documentStorageDTO.setUpdateTime(new Date());
        documentStorageRecord.save(documentStorageDTO);
    }
}
