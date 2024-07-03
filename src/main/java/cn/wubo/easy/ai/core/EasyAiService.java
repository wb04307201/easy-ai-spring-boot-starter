package cn.wubo.easy.ai.core;

import cn.wubo.easy.ai.document.IDocumentContentRecord;
import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.dto.DocumentContentDTO;
import cn.wubo.easy.ai.dto.DocumentStorageDTO;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import cn.wubo.easy.ai.file.IFileStorageRecord;
import cn.wubo.easy.ai.file.IFileStorageService;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EasyAiService {

    private final IFileStorageService fileStorageService;
    private final IFileStorageRecord fileStorageRecord;
    private final IDocumentReaderService documentReaderService;
    private final IDocumentContentRecord documentContentRecord;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    public EasyAiService(IFileStorageService fileStorageService, IFileStorageRecord fileStorageRecord, IDocumentReaderService documentReaderService, IDocumentContentRecord documentContentRecord, VectorStore vectorStore, ChatModel chatModel) {
        this.fileStorageService = fileStorageService;
        this.fileStorageRecord = fileStorageRecord;
        this.documentReaderService = documentReaderService;
        this.documentContentRecord = documentContentRecord;
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public List<DocumentStorageDTO> upload(MultiValueMap<String, Part> multiValueMap) {
        List<DocumentStorageDTO> documentStorageDTOS = new ArrayList<>();
        List<Part> parts = multiValueMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()).toList();
        try {
            for (Part part : parts) {
                DocumentStorageDTO documentStorageDTO = new DocumentStorageDTO();
                documentStorageDTO.setFileName(part.getSubmittedFileName());
                documentStorageDTO.setFilePath(fileStorageService.save(part.getInputStream(), documentStorageDTO.getFileName()));
                documentStorageDTO.setState("00");
                documentStorageDTO.setCreateTime(new Date());
                documentStorageDTO = fileStorageRecord.save(documentStorageDTO);
                documentStorageDTOS.add(documentStorageDTO);
            }
            return documentStorageDTOS;
        } catch (IOException e) {
            throw new EasyAiRuntimeException(e.getMessage(), e);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public DocumentStorageDTO read(DocumentStorageDTO documentStorageDTO) {
        documentStorageDTO.setState("10");
        documentStorageDTO.setUpdateTime(new Date());
        documentStorageDTO = fileStorageRecord.save(documentStorageDTO);
        Resource resource = fileStorageService.getResource(documentStorageDTO.getFilePath());
        List<Document> documentList = documentReaderService.read(resource);
        for (Document document : documentList) {
            DocumentContentDTO documentContentDTO = new DocumentContentDTO();
            documentContentDTO.setStorageId(documentStorageDTO.getId());
            documentContentDTO.setDocument(document);
            documentContentRecord.save(documentContentDTO);
        }
        documentStorageDTO.setState("20");
        documentStorageDTO.setUpdateTime(new Date());
        return fileStorageRecord.save(documentStorageDTO);
    }

    @Transactional(rollbackOn = Exception.class)
    public DocumentStorageDTO save(DocumentStorageDTO documentStorageDTO) {
        documentStorageDTO.setState("30");
        documentStorageDTO.setUpdateTime(new Date());
        documentStorageDTO = fileStorageRecord.save(documentStorageDTO);
        DocumentContentDTO query = new DocumentContentDTO();
        query.setStorageId(documentStorageDTO.getId());
        vectorStore.accept(documentContentRecord.list(query).stream().map(DocumentContentDTO::getDocument).toList());
        documentStorageDTO.setState("40");
        documentStorageDTO.setUpdateTime(new Date());
        return fileStorageRecord.save(documentStorageDTO);
    }

    public ChatResponse chat(Prompt prompt) {
        return chatModel.call(prompt);
    }

    public ChatResponse chatWithDocument(Prompt prompt, String systemPromptTemplate) {
        List<Message> messageList = prompt.getInstructions();
        String lastMessage = messageList.get(messageList.size() - 1).getContent();
        List<Document> documentList = vectorStore.similaritySearch(lastMessage);
        String documents = documentList.stream().map(Document::getContent).collect(Collectors.joining());
        Message systemMessage = new SystemPromptTemplate(systemPromptTemplate).createMessage(Map.of("documents", documents, "message", lastMessage));
        prompt.getInstructions().add(0, systemMessage);
        return chatModel.call(prompt);
    }
}
