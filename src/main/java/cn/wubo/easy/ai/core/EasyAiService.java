package cn.wubo.easy.ai.core;

import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.document.IDocumentStorageRecord;
import cn.wubo.easy.ai.document.IDocumentStorageService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class EasyAiService {

    private final IDocumentStorageService fileStorageService;
    private final IDocumentStorageRecord fileStorageRecord;
    private final IDocumentReaderService documentReaderService;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public EasyAiService(IDocumentStorageService fileStorageService, IDocumentStorageRecord fileStorageRecord, IDocumentReaderService documentReaderService, VectorStore vectorStore, ChatClient chatClient) {
        this.fileStorageService = fileStorageService;
        this.fileStorageRecord = fileStorageRecord;
        this.documentReaderService = documentReaderService;
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    /**
     * 上传文件。
     * <p>
     * 该方法接收一个InputStream类型的文件内容和一个字符串类型的文件名，将文件内容存储到系统中，并返回文件的存储信息。
     * 主要包括文件名、文件存储路径、文件状态和创建时间。
     *
     * @param is       文件的内容输入流。
     * @param fileName 文件的名称。
     * @return 返回存储文件的信息数据传输对象（DTO）。
     */
    public DocumentStorageDTO upload(InputStream is, String fileName) {
        // 创建文件存储DTO对象，用于存储文件相关信息
        DocumentStorageDTO documentStorageDTO = new DocumentStorageDTO();
        // 设置文件名
        documentStorageDTO.setFileName(fileName);
        // 调用文件存储服务保存文件，并将返回的文件存储路径设置到DTO中
        documentStorageDTO.setFilePath(fileStorageService.save(is, documentStorageDTO.getFileName()));
        // 设置文件存储状态为"00"，表示存储成功
        documentStorageDTO.setState("00");
        // 设置文件的创建时间为当前时间
        documentStorageDTO.setCreateTime(new Date());
        // 将文件存储DTO保存到文件存储记录中，并返回保存后的DTO
        return fileStorageRecord.save(documentStorageDTO);
    }

    /**
     * 读取文件存储信息并更新状态。
     * <p>
     * 此方法接收一个文件存储数据传输对象（FileStorageDTO）作为输入，读取文件内容，并更新文件存储的状态和更新时间。
     * 方法首先将文件存储的状态设置为"10"，表示正在处理中，然后记录更新时间。
     * 接着，通过文件存储路径获取文件资源，并读取文档内容，将读取到的文档列表设置到文件存储DTO中。
     * 最后，将文件存储的状态更新为"20"，表示处理完成，并再次记录更新时间，然后保存更新后的文件存储信息。
     *
     * @param documentStorageDTO 文件存储DTO，包含文件的存储信息和路径。
     * @return 返回更新后的文件存储DTO，包含文件内容和最新的状态及更新时间。
     */
    public DocumentStorageDTO read(DocumentStorageDTO documentStorageDTO) {
        // 初始化文件存储状态为"10"，表示处理中
        documentStorageDTO.setState("10");
        // 更新文件存储的更新时间为当前时间
        documentStorageDTO.setUpdateTime(new Date());
        // 保存更新后的文件存储信息
        documentStorageDTO = fileStorageRecord.save(documentStorageDTO);
        // 读取文件内容，并更新到文件存储DTO的文档列表中
        documentStorageDTO.setDocumentList(documentReaderService.read(fileStorageService.getResource(documentStorageDTO.getFilePath())));
        // 更新文件存储状态为"20"，表示处理完成
        documentStorageDTO.setState("20");
        // 再次更新文件存储的更新时间为当前时间
        documentStorageDTO.setUpdateTime(new Date());
        // 保存更新后的文件存储信息
        return fileStorageRecord.save(documentStorageDTO);
    }

    /**
     * 保存文件存储信息。
     * <p>
     * 此方法接收一个文件存储数据传输对象（FileStorageDTO）作为输入，该对象包含有关待存储文件的信息。
     * 方法首先设置文件的初始状态为"30"，表示文件存储过程已经开始，然后更新文件的更新时间。
     * 接下来，方法调用fileStorageRecord的save方法来保存文件存储信息，并更新fileStorageDTO对象。
     * 之后，方法将文件内容接受到vectorStore中，这一步可能是将文件内容索引到一个矢量数据库中以供后续查询。
     * 最后，方法更新文件的状态为"40"，表示文件存储过程已经完成，并再次调用fileStorageRecord的save方法来保存更新后的文件存储信息。
     *
     * @param documentStorageDTO 包含待存储文件信息的数据传输对象。
     * @return 返回保存后的文件存储数据传输对象。
     */
    public DocumentStorageDTO save(DocumentStorageDTO documentStorageDTO) {
        // 初始化文件存储状态为"30"，表示正在处理
        documentStorageDTO.setState("30");
        // 更新文件的最后一次更新时间
        documentStorageDTO.setUpdateTime(new Date());
        // 保存文件存储信息
        documentStorageDTO = fileStorageRecord.save(documentStorageDTO);
        // 将文件内容接受到矢量存储中
        vectorStore.accept(documentStorageDTO.getDocumentList());
        // 更新文件存储状态为"40"，表示处理完成
        documentStorageDTO.setState("40");
        // 更新文件的最后一次更新时间
        documentStorageDTO.setUpdateTime(new Date());
        // 保存更新后的文件存储信息
        return fileStorageRecord.save(documentStorageDTO);
    }

    public ChatResponse chat(ChatRecord chatRecord) {
        // @formatter:off
        return chatClient
                .prompt()
                .user(chatRecord.message())
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatRecord.conversationId()))
//                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, chatRecord.message()))
                .call()
                .chatResponse();
        // @formatter:on
    }

    public Flux<String> stream(ChatRecord chatRecord) {
        // @formatter:off
        return chatClient
                .prompt()
                .user(chatRecord.message())
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatRecord.conversationId()))
//                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, chatRecord.message()))
                .stream()
                .content();
        // @formatter:on
    }

    /**
     * 根据文件存储ID删除文件及相关记录。
     * <p>
     * 此方法通过ID查找文件存储记录，然后删除该记录对应的文档列表中的所有文档，
     * 并删除文件存储服务中的实际文件。最后，从文件存储记录中删除该条记录。
     *
     * @param id 文件存储记录的唯一标识符。
     * @return 总是返回Boolean.TRUE，表示删除操作已执行。
     */
    public Boolean delete(String id) {
        // 根据ID查找文件存储记录。
        DocumentStorageDTO documentStorageDTO = fileStorageRecord.findById(id);

        // 删除文件存储DTO中包含的所有文档。
        vectorStore.delete(documentStorageDTO.getDocumentList().stream().map(Document::getId).toList());

        // 从文件存储记录中删除相应的记录。
        fileStorageRecord.delete(documentStorageDTO);

        // 删除文件存储服务中的实际文件。
        fileStorageService.delete(documentStorageDTO.getFilePath());

        // 返回确认删除操作已执行。
        return Boolean.TRUE;
    }

    /**
     * 根据ID查找文档存储信息。
     * <p>
     * 本方法通过调用文件存储记录的查找方法，根据给定的ID检索文档存储的相关信息。
     * 这是对文档存储服务的一个基本操作，用于获取特定文档的存储详情。
     *
     * @param id 文档的唯一标识符。这个标识符用于在存储系统中定位特定的文档记录。
     * @return DocumentStorageDTO 对象，包含所查找文档的存储详情。如果找不到对应ID的文档，则返回null。
     */
    public DocumentStorageDTO findById(String id) {
        return fileStorageRecord.findById(id);
    }

    /**
     * 列出文件存储信息。
     * <p>
     * 本方法通过调用FileStorageRecord类的list方法，来获取并返回文件存储的相关信息。
     * 使用FileStorageDTO作为参数和返回值，可以详细地描述文件存储的细节，包括但不限于文件的名称、位置、大小等。
     *
     * @param documentStorageDTO 文件存储数据传输对象，包含用于查询文件存储信息的条件。
     * @return 返回一个文件存储DTO的列表，列表中的每个元素都代表一个文件的存储信息。
     */
    public List<DocumentStorageDTO> list(DocumentStorageDTO documentStorageDTO) {
        return fileStorageRecord.list(documentStorageDTO);
    }

    /**
     * 根据文件路径获取文件的字节内容。
     * <p>
     * 此方法通过文件存储服务从指定的文件路径中获取文件的字节内容。
     * 它封装了对文件存储服务的调用，使得调用者不需要直接与文件存储细节交互，
     * 提高了代码的可维护性和可扩展性。
     *
     * @param documentStorageDTO 包含文件路径信息的数据传输对象。
     * @return 文件的字节内容。如果文件不存在或路径无效，可能返回null。
     */
    public byte[] getBytes(DocumentStorageDTO documentStorageDTO) {
        // 调用文件存储服务，根据文件路径获取字节内容
        return fileStorageService.getBytes(documentStorageDTO.getFilePath());
    }
}
