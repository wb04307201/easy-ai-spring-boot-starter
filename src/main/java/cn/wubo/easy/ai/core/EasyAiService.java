package cn.wubo.easy.ai.core;

import cn.wubo.easy.ai.document.IDocumentContentRecord;
import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.dto.DocumentContentDTO;
import cn.wubo.easy.ai.dto.FileStorageDTO;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import cn.wubo.easy.ai.file.IFileStorageRecord;
import cn.wubo.easy.ai.file.IFileStorageService;
import jakarta.servlet.http.Part;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.util.MultiValueMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /**
     * 处理文件上传的请求。
     *
     * @param multiValueMap 包含上传文件的MultiValueMap。
     * @return 返回一个包含文件存储信息的DTO列表。
     * @throws EasyAiRuntimeException 如果文件读取发生IOException，则抛出此异常。
     */
    public List<FileStorageDTO> upload(MultiValueMap<String, Part> multiValueMap) {
        // 初始化用于存储文件信息的DTO列表
        List<FileStorageDTO> fileStorageDTOS = new ArrayList<>();
        // 将MultiValueMap转换为Part列表，方便后续处理
        List<Part> parts = multiValueMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()).toList();

        try {
            // 遍历每个上传的文件
            for (Part part : parts) {
                // 创建一个新的FileStorageDTO实例用于存储文件信息
                FileStorageDTO fileStorageDTO = new FileStorageDTO();
                // 设置文件名
                fileStorageDTO.setFileName(part.getSubmittedFileName());
                // 保存文件并设置文件路径
                fileStorageDTO.setFilePath(fileStorageService.save(part.getInputStream(), fileStorageDTO.getFileName()));
                // 设置文件状态为"00"，表示上传成功
                fileStorageDTO.setState("00");
                // 设置文件的创建时间
                fileStorageDTO.setCreateTime(new Date());
                // 保存文件信息到数据库，并更新fileStorageDTO实例
                fileStorageDTO = fileStorageRecord.save(fileStorageDTO);
                // 将文件信息添加到结果列表
                fileStorageDTOS.add(fileStorageDTO);
            }
            // 返回处理后的文件信息列表
            return fileStorageDTOS;
        } catch (IOException e) {
            // 如果发生IOException，则抛出自定义异常
            throw new EasyAiRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 读取文件存储信息，并处理文件内容的存储。
     *
     * @param fileStorageDTO 文件存储数据传输对象，包含文件的存储信息。
     * @return 更新后的文件存储数据传输对象，包含最新的存储状态和时间。
     */
    public FileStorageDTO read(FileStorageDTO fileStorageDTO) {
        // 初始化文件存储状态为处理中
        fileStorageDTO.setState("10");
        // 更新文件存储的最后处理时间
        fileStorageDTO.setUpdateTime(new Date());
        // 保存或更新文件存储信息
        fileStorageDTO = fileStorageRecord.save(fileStorageDTO);

        // 根据文件存储信息获取实际的文件资源
        Resource resource = fileStorageService.getResource(fileStorageDTO.getFilePath());
        // 读取文件内容，转换为文档列表
        List<Document> documentList = documentReaderService.read(resource);

        // 遍历文档列表，为每个文档创建并保存内容记录
        for (Document document : documentList) {
            DocumentContentDTO documentContentDTO = new DocumentContentDTO();
            // 关联文件存储ID
            documentContentDTO.setStorageId(fileStorageDTO.getId());
            // 设置文档内容
            documentContentDTO.setDocument(document);
            // 保存文档内容记录
            documentContentRecord.save(documentContentDTO);
        }

        // 更新文件存储状态为处理完成
        fileStorageDTO.setState("20");
        // 更新文件存储的最后处理时间
        fileStorageDTO.setUpdateTime(new Date());
        // 保存或更新文件存储信息，记录处理完成的状态
        return fileStorageRecord.save(fileStorageDTO);
    }

    /**
     * 保存文件存储信息。
     *
     * 此方法接收一个文件存储数据传输对象（FileStorageDTO），首先设置其状态为"30"，表示正在处理中，
     * 并更新其更新时间。然后，通过文件存储记录服务保存这个对象，得到更新后的文件存储DTO。
     * 接下来，根据保存后的文件存储DTO的ID，查询相关的文档内容，并将这些内容接受到向量存储中。
     * 最后，将文件存储DTO的状态更新为"40"，表示处理完成，并再次更新其更新时间，然后保存这个更新后的对象并返回。
     *
     * @param fileStorageDTO 文件存储DTO，包含文件存储相关信息。
     * @return 返回保存后的文件存储DTO，包含最新的状态和更新时间。
     */
    public FileStorageDTO save(FileStorageDTO fileStorageDTO) {
        // 初始化文件存储状态为处理中
        fileStorageDTO.setState("30");
        // 更新文件存储的更新时间
        fileStorageDTO.setUpdateTime(new Date());
        // 保存文件存储信息
        fileStorageDTO = fileStorageRecord.save(fileStorageDTO);

        // 初始化查询文档内容的DTO
        DocumentContentDTO query = new DocumentContentDTO();
        // 设置查询的存储ID
        query.setStorageId(fileStorageDTO.getId());
        // 根据存储ID查询文档内容，并将内容接受到向量存储中
        vectorStore.accept(documentContentRecord.list(query).stream().map(DocumentContentDTO::getDocument).toList());

        // 更新文件存储状态为处理完成
        fileStorageDTO.setState("40");
        // 更新文件存储的更新时间
        fileStorageDTO.setUpdateTime(new Date());
        // 保存更新后的文件存储信息并返回
        return fileStorageRecord.save(fileStorageDTO);
    }

    /**
     * 根据给定的提示进行聊天交互。
     *
     * 本方法通过调用chatModel的call方法，传入一个提示（Prompt），来发起一次聊天交互。
     * 主要用于在人机对话系统中，根据用户的输入生成相应的回复。
     *
     * @param prompt 用户的输入或者对话系统的提示信息，用于引导聊天模型生成响应。
     * @return ChatResponse 聊天模型生成的响应结果，包含回复的内容以及其他可能的相关信息。
     */
    public ChatResponse chat(Prompt prompt) {
        return chatModel.call(prompt);
    }

    /**
     * 根据给定的提示和文档进行聊天响应生成。
     * 该方法首先从提示中获取一系列消息，然后针对最后一条消息寻找相关的文档。
     * 使用找到的文档内容和系统提示模板生成一条系统消息，并将其插入到提示序列的开头。
     * 最后，使用更新后的提示序列调用聊天模型以生成聊天响应。
     *
     * @param prompt 用户的提示，包含一系列消息。
     * @param systemPromptTemplate 系统提示的模板，用于生成系统消息。
     * @return 生成的聊天响应。
     */
    public ChatResponse chatWithDocument(Prompt prompt, String systemPromptTemplate) {
        // 从用户的提示中获取消息列表。
        List<Message> messageList = prompt.getInstructions();
        // 获取消息列表中的最后一条消息的内容。
        String lastMessage = messageList.get(messageList.size() - 1).getContent();
        // 根据最后一条消息的内容，在向量存储中寻找相似的文档。
        List<Document> documentList = vectorStore.similaritySearch(lastMessage);
        // 将找到的文档内容合并为一个字符串。
        String documents = documentList.stream().map(Document::getContent).collect(Collectors.joining());
        // 使用系统提示模板和文档内容生成一条系统消息。
        Message systemMessage = new SystemPromptTemplate(systemPromptTemplate).createMessage(Map.of("documents", documents, "message", lastMessage));
        // 将系统消息插入到提示序列的开头。
        prompt.getInstructions().add(0, systemMessage);
        // 使用更新后的提示序列调用聊天模型以生成聊天响应。
        return chatModel.call(prompt);
    }

    /**
     * 根据文件ID删除文件及其相关文档内容。
     *
     * @param id 文件存储记录的唯一标识。
     * @return 返回一个布尔值，表示删除操作是否成功。
     */
    public Boolean delete(String id) {
        // 根据ID查找文件存储记录
        FileStorageDTO fileStorageDTO = fileStorageRecord.findById(id);
        // 初始化文档内容查询对象
        DocumentContentDTO query = new DocumentContentDTO();
        // 设置查询对象的存储ID，用于后续查询关联的文档内容
        query.setStorageId(fileStorageDTO.getId());
        // 根据存储ID查询所有关联的文档内容记录
        List<DocumentContentDTO> documentContentDTOS = documentContentRecord.list(query);
        // 删除所有关联的文档内容在向量存储中的记录
        vectorStore.delete(documentContentDTOS.stream().map(e -> e.getDocument().getId()).toList());
        // 删除文件存储记录对应的文件
        fileStorageService.delete(fileStorageDTO.getFilePath());
        // 删除所有关联的文档内容记录
        documentContentRecord.delete(query);
        // 删除文件存储记录
        fileStorageRecord.delete(fileStorageDTO);
        // 返回删除操作成功标志
        return Boolean.TRUE;
    }

    /**
     * 列出文件存储信息。
     *
     * 本方法通过调用FileStorageRecord类的list方法，来获取并返回文件存储的相关信息。
     * 使用FileStorageDTO作为参数和返回值，可以详细地描述文件存储的细节，包括但不限于文件的名称、位置、大小等。
     *
     * @param fileStorageDTO 文件存储数据传输对象，包含用于查询文件存储信息的条件。
     * @return 返回一个文件存储DTO的列表，列表中的每个元素都代表一个文件的存储信息。
     */
    public List<FileStorageDTO> list(FileStorageDTO fileStorageDTO) {
        return fileStorageRecord.list(fileStorageDTO);
    }

    /**
     * 根据文件路径获取文件的字节内容。
     *
     * 此方法通过文件存储服务从指定的文件路径中获取文件的字节内容。
     * 它封装了对文件存储服务的调用，使得调用者不需要直接与文件存储细节交互，
     * 提高了代码的可维护性和可扩展性。
     *
     * @param fileStorageDTO 包含文件路径信息的数据传输对象。
     * @return 文件的字节内容。如果文件不存在或路径无效，可能返回null。
     */
    public byte[] getBytes(FileStorageDTO fileStorageDTO) {
        // 调用文件存储服务，根据文件路径获取字节内容
        return fileStorageService.getBytes(fileStorageDTO.getFilePath());
    }
}
