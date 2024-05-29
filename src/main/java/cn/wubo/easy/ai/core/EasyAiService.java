package cn.wubo.easy.ai.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EasyAiService {

    private static final String SYSTEM_PROMPT = """
            你需要使用文档内容对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
            不能在回复中体现出你是根据给出的文档内容进行回复的，这点非常重要。
            
            当用户提出的问题无法根据文档内容进行回复或者你也不清楚时，回复不知道即可。
            
            文档内容如下:
            {documents}
            
            """;

    /*private static final String SYSTEM_PROMPT = """
            你需要分析文档的内容哪些部分可以帮助你对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
            不能在回复中体现出你是根据给出的文档内容进行回复的，这点非常重要。
            
            当用户提出的问题无法根据文档内容进行回复或者你也不清楚时，回复不知道即可。
            
            文档内容如下:
            {documents}
            
            """;*/

    private static final String KEYWORD_PROMPT = """
            你需要参考之前的对话内容对用户最后提出的问题，提取进行回复可能需要查询知识库的关键词。
            
            直接将关键词内容回复给我，并且每个关键词之间用“，”分隔开。
            
            当用户提出的问题无法提取关键词或者你也不清楚时，回复不知道即可。
            
            对话内容如下:
            {documents}
            
            """;

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final TokenTextSplitter tokenTextSplitter;
    private final ExtractedTextFormatter textFormatter;

    public EasyAiService(VectorStore vectorStore, ChatModel chatModel, TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter textFormatter) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.tokenTextSplitter = tokenTextSplitter;
        this.textFormatter = textFormatter;
    }

    /**
     * 从给定的资源文件中提取文本，并将其拆分为文档列表，然后将这些文档保存到向量数据库中。
     *
     * @param fileResource 要处理的资源文件，不能为null。
     */
    public void saveSource(Resource fileResource) {
        // 初始化Tika解析器并从文件提取文本
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(fileResource, textFormatter);

        // 将提取的文本拆分为文档列表
        List<Document> documentList = tokenTextSplitter.apply(tikaDocumentReader.get());

        // 记录拆分后的文档数量
        log.debug("拆分出数据条数 {}", documentList.size());

        // 开始保存文档列表到向量数据库
        log.debug("保存向量数据库开始");
        vectorStore.accept(documentList);

        // 完成保存向量数据库操作
        log.debug("保存向量数据库完成");
    }

    public ChatResponse call(Prompt prompt) {
        return chatModel.call(prompt);
    }

    /**
     * 与用户进行聊天交互的函数。
     *
     * @param payload 包含聊天消息的载荷对象，最后一条消息将用于相似性搜索。
     * @return ChatResponse 包含聊天响应的对象。
     */
    public ChatResponse chat(Payload payload) {
        // 提取搜索向量数据库用的关键词
        log.debug("提取关键词开始");
        ChatResponse kwRsp = call(new PromptTemplate(KEYWORD_PROMPT).create(Map.of("documents", payload.messages().toString())));
        log.debug("提取关键词结束 {}", kwRsp.getResult().getOutput().getContent());

        // 将输入的消息分类并转换为相应的消息类型
        List<Message> messages = payload.messages().stream().map(message -> switch (message.role()) {
            case SYSTEM -> new SystemMessage(message.content());
            case ASSISTANT -> new AssistantMessage(message.content());
            case FUNCTION -> new FunctionMessage(message.content());
            default -> new UserMessage(message.content());
        }).collect(Collectors.toList());

        // 执行向量相似性搜索，找到与最后一条消息内容相似的文档列表
        List<Document> listOfSimilarDocuments = vectorStore.similaritySearch(kwRsp.getResult().getOutput().getContent());
        log.debug("找到向量数据 {} 内容 {}", listOfSimilarDocuments.size(), listOfSimilarDocuments);
        if (!listOfSimilarDocuments.isEmpty()) {
            // 将搜索到的文档内容拼接为字符串，用于系统提示消息
            String documents = listOfSimilarDocuments.stream().map(Document::getContent).collect(Collectors.joining());

            // 创建一条系统消息，包含搜索到的文档内容
            Message systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documents));

            // 将系统消息添加到消息列表的开头
            messages.add(0, systemMessage);
        }

        // 使用聊天模型处理消息列表，并生成最终的回复
        log.debug("组织回答开始");
        ChatResponse rsp = call(new Prompt(messages));
        log.debug("组织回答结束 {}", rsp.getResult().getOutput().getContent());

        return rsp;
    }


}
