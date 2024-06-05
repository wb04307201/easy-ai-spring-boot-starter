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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EasyAiService {

    /*private static final String SYSTEM_PROMPT = """
            你需要使用文档内容对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
            不能在回复中体现出你是根据给出的文档内容进行回复的，这点非常重要。
            
            当用户提出的问题无法根据文档内容进行回复或者你也不清楚时，回复不知道即可。
            
            文档内容如下:
            {documents}
            
            """;*/

    /*private static final String SYSTEM_PROMPT = """
            你需要分析文档的内容哪些部分可以帮助你对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
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

    /*private static final String KEYWORD_PROMPT = """
            你需要参考之前的对话内容对用户最后提出的问题，提取进行回复可能需要查询知识库的关键词。
            
            直接将关键词内容回复给我，并且每个关键词之间用“，”分隔开。
            
            当用户提出的问题无法提取关键词或者你也不清楚时，回复不知道即可。
            
            对话内容如下:
            {documents}
            
            """;*/

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

    /**
     * 根据输入的Payload生成聊天响应。
     *
     * @param payload 聊天请求的负载，包含消息内容和其它相关信息。
     * @return 聊天响应对象，包含生成的回复消息。
     */
    public ChatResponse chat(Payload payload) {
        // 将Payload中的消息根据角色转换为相应的消息类型
        List<Message> messages = payload.messages().stream().map(message -> switch (message.role()) {
            case SYSTEM -> new SystemMessage(message.content());
            case ASSISTANT -> new AssistantMessage(message.content());
            case FUNCTION -> new FunctionMessage(message.content());
            default -> new UserMessage(message.content());
        }).collect(Collectors.toList());

        // 使用向量存储进行相似性搜索，获取相关文档列表
        List<Document> listOfSimilarDocuments = vectorStore.similaritySearch(payload.messages().get(payload.messages().size() - 1).content());
        log.debug("找到向量数据 {} 内容 {}", listOfSimilarDocuments.size(), listOfSimilarDocuments);

        // 如果找到了相似的文档，添加系统提示消息
        if (!listOfSimilarDocuments.isEmpty()) {
            // 将搜索到的文档内容拼接为字符串
            String documents = listOfSimilarDocuments.stream().map(Document::getContent).collect(Collectors.joining());
            Map<String, Object> model = new HashMap<>();
            model.put("summary_prompt", documents);
            model.put("message", payload.messages().get(payload.messages().size() - 1).content());
            // 创建系统提示消息并添加到消息列表的前面
            Message systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(model);
            messages.add(0, systemMessage);
        }

        // 调用聊天模型生成聊天响应
        log.debug("组织回答开始");
        ChatResponse rsp = chatModel.call(new Prompt(messages));
        log.debug("组织回答结束 {}", rsp.getResult().getOutput().getContent());

        // 返回聊天响应
        return rsp;
    }
}
