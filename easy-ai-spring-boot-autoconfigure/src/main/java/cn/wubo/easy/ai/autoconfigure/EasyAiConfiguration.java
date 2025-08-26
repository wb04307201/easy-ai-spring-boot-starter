package cn.wubo.easy.ai.autoconfigure;

import cn.wubo.easy.ai.core.ChatRecord;
import cn.wubo.easy.ai.core.DocumentStorageDTO;
import cn.wubo.easy.ai.core.DocumntService;
import cn.wubo.easy.ai.core.EasyAiService;
import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.document.IDocumentStorageRecord;
import cn.wubo.easy.ai.document.IDocumentStorageService;
import cn.wubo.easy.ai.document.impl.DocumentReaderServiceImpl;
import cn.wubo.easy.ai.document.impl.LocalDocumentStorageServiceImpl;
import cn.wubo.easy.ai.document.impl.MemDocumentStorageRecordImpl;
import cn.wubo.easy.ai.exception.PageRuntimeException;
import cn.wubo.easy.ai.result.Result;
import cn.wubo.easy.ai.tool.DateTimeTools;
import cn.wubo.easy.ai.utils.FileUtils;
import cn.wubo.easy.ai.utils.IoUtils;
import cn.wubo.easy.ai.utils.PageUtils;
import jakarta.servlet.http.Part;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AutoConfiguration
@EnableConfigurationProperties({SplitterProperties.class, ReaderProperties.class, EasyAiProperties.class})
public class EasyAiConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public IDocumentStorageRecord fileStorageRecord() {
        return new MemDocumentStorageRecordImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public IDocumentStorageService fileStorageService() {
        return new LocalDocumentStorageServiceImpl();
    }

    /**
     * 创建并返回一个TokenTextSplitter实例。
     * 这个方法使用EasyAiProperties配置来初始化TokenTextSplitter实例，
     * 为文本分割提供特定的规则，例如每块的默认大小、最小大小、最大数量等。
     *
     * @param properties EasyAiProperties的实例，包含分割文本时使用的配置，
     *                   如默认块大小、最小块大小字符数、最大块数等。
     * @return TokenTextSplitter实例，配置完毕以用于文本分割任务。
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter(SplitterProperties properties) {
        // 使用配置属性初始化TokenTextSplitter实例
        // @formatter:off
        return new TokenTextSplitter(
                properties.getDefaultChunkSize(),
                properties.getMinChunkSizeChars(),
                properties.getMinChunkLengthToEmbed(),
                properties.getMaxNumChunks(),
                properties.isKeepSeparator()
        );
        // @formatter:on
    }

    /**
     * 创建并配置 ExtractedTextFormatter 的 Bean 实例。
     * 此方法根据 ReaderProperties 中的配置，构建一个 ExtractedTextFormatter 实例，
     * 用于后续文本提取和格式化相关操作。
     *
     * @param properties ReaderProperties 实例，包含文本格式化的配置信息，如左对齐设置、
     *                   删除的底部和顶部文本行数、跳过的顶部页面数等。
     * @return ExtractedTextFormatter 配置好的实例，用于文本格式化。
     */
    @Bean
    public ExtractedTextFormatter extractedTextFormatter(ReaderProperties properties) {
        // 使用 Builder 模式根据配置属性构建 ExtractedTextFormatter 实例
        return ExtractedTextFormatter.builder().withLeftAlignment(properties.isLeftAlignment()) // 设置左对齐选项
                .withNumberOfBottomTextLinesToDelete(properties.getNumberOfBottomTextLinesToDelete()) // 设置要删除的底部文本行数
                .withNumberOfTopTextLinesToDelete(properties.getNumberOfTopTextLinesToDelete()) // 设置要删除的顶部文本行数
                .withNumberOfTopPagesToSkipBeforeDelete(properties.getNumberOfTopPagesToSkipBeforeDelete()) // 设置在删除前要跳过的顶部页面数
                .build(); // 构建并返回配置好的 ExtractedTextFormatter 实例
    }

    /**
     * 创建关键词元数据增强器Bean
     *
     * @param chatModel 聊天模型实例，用于处理关键词提取和分析
     * @return 配置好的KeywordMetadataEnricher实例，用于增强元数据中的关键词信息
     */
    @Bean
    public KeywordMetadataEnricher keywordMetadataEnricher(ChatModel chatModel) {
        // 构建关键词元数据增强器，设置关键词数量为5个
        return KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();
    }


    /**
     * 创建并配置DocumentReaderService bean。
     * 该方法通过依赖注入的方式，使用TokenTextSplitter和ExtractedTextFormatter来实例化DocumentReaderServiceImpl。
     * 这种方式允许Spring容器管理对象的生命周期，并负责对象之间的依赖关系。
     *
     * @param tokenTextSplitter      用于分割文本的令牌化器，它将文本拆分成更小的单元，以便进行进一步处理。
     * @param extractedTextFormatter 对提取的文本进行格式化的处理器，它负责将原始文本格式化为适合进一步处理的格式。
     * @return 返回一个配置好的IDocumentReaderService实例，该实例可以用于读取和处理文档。
     */
    @Bean
    @ConditionalOnMissingBean
    public IDocumentReaderService doumentReaderService(TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter extractedTextFormatter, KeywordMetadataEnricher keywordMetadataEnricher) {
        return new DocumentReaderServiceImpl(tokenTextSplitter, extractedTextFormatter, keywordMetadataEnricher);
    }

    @Bean(value = "easyAiChatClient")
    @ConditionalOnExpression("${spring.ai.easy.enableRag:false}")
    public ChatClient ragChatClient(ChatModel chatModel, VectorStore vectorStore,EasyAiProperties properties) {
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultTools(new DateTimeTools());
        if (properties.getDefaultSystem() != null) builder.defaultSystem(properties.getDefaultSystem());

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(20).build();

        PromptTemplate customPromptTemplate = PromptTemplate.builder().renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()).template(properties.getRag().getTemplate()).build();

        builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), // chat-memory advisor
                QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().similarityThreshold(properties.getRag().getSimilarityThreshold()).topK(properties.getRag().getTopK()).build()).promptTemplate(customPromptTemplate).build(),    // RAG advisor
                SimpleLoggerAdvisor.builder().build() // logger advisor
        );

        return builder.build();
    }

    @Bean(value = "easyAiChatClient")
    @ConditionalOnMissingBean
    public ChatClient chatClient(ChatModel chatModel,EasyAiProperties properties) {
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultTools(new DateTimeTools());
        if (properties.getDefaultSystem() != null) builder.defaultSystem(properties.getDefaultSystem());

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(20).build();

        builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), // chat-memory advisor
                new SimpleLoggerAdvisor() // logger advisor
        );

        return builder.build();
    }

    /**
     * 创建并配置EasyAiService Bean实例
     *
     * @param chatClient             AI聊天客户端，用于与AI模型进行交互
     * @return 配置完成的EasyAiService实例
     */
    @Bean
    public EasyAiService easyAiService(@Qualifier(value = "easyAiChatClient") ChatClient chatClient) {
        return new EasyAiService(chatClient);
    }

    @Bean
    @ConditionalOnExpression("${spring.ai.easy.enableRag:true}")
    public DocumntService documntService(IDocumentStorageService documentStorageService, IDocumentStorageRecord documentStorageRecord, IDocumentReaderService documentReaderService, VectorStore vectorStore) {
        // 初始化文档存储服务
        documentStorageService.init();
        // 初始化文档存储记录服务
        documentStorageRecord.init();
        return new DocumntService(documentStorageService, documentStorageRecord, documentReaderService, vectorStore);
    }


    private static final String LOST_ID = "请求参数id丢失!";

    @Bean("wb04307201EasyAiRouter")
    @ConditionalOnExpression("${spring.ai.easy.enableRag:true}")
    public RouterFunction<ServerResponse> easyAiRagRouter(EasyAiService easyAiService, DocumntService documntService) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        // 如果启用了Web和REST功能，添加文件预览列表的HTML页面渲染。
            builder.GET("/easy/ai/list", RequestPredicates.accept(MediaType.TEXT_HTML), request -> {
                Map<String, Object> data = new HashMap<>();
                data.put("contextPath", request.requestPath().contextPath().value());
                // 渲染并返回文件预览列表页面。
                return ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(PageUtils.write("list.ftl", data));
            });
            builder.POST("/easy/ai/list", request -> {
                DocumentStorageDTO documentStorageDTO = request.body(DocumentStorageDTO.class);
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(documntService.list(documentStorageDTO)));
            });
            builder.POST("/easy/ai/upload", request -> {
                Part part = request.multipartData().getFirst("file");
                DocumentStorageDTO documentStorageDTO = documntService.upload(part.getInputStream(), part.getSubmittedFileName());
                documentStorageDTO = documntService.read(documentStorageDTO);
                documentStorageDTO = documntService.save(documentStorageDTO);
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(documentStorageDTO));
            });
            builder.GET("/easy/ai/delete", request -> {
                String id = request.param("id").orElseThrow(() -> new IllegalArgumentException(LOST_ID));
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(documntService.delete(id)));
            });
            builder.GET("/easy/ai/download", request -> {
                String id = request.param("id").orElseThrow(() -> new IllegalArgumentException(LOST_ID));
                DocumentStorageDTO documentStorageDTO = documntService.findById(id);
                byte[] bytes = documntService.getBytes(documentStorageDTO);
                // @formatter:off
                // 处理文件下载请求，返回文件内容。
                return ServerResponse.ok().contentType(MediaType.parseMediaType(FileUtils.getMimeType(documentStorageDTO.getFileName())))
                        .contentLength(bytes.length)
                        .header("Content-Disposition", "attachment;filename=" + new String(Objects.requireNonNull(documentStorageDTO.getFileName()).getBytes(), StandardCharsets.ISO_8859_1))
                        .build((res, req) -> {
                            try (OutputStream os = req.getOutputStream()) {
                                IoUtils.writeToStream(bytes, os);
                            } catch (IOException e) {
                                throw new PageRuntimeException(e.getMessage(), e);
                            }
                            return null;
                        });
                // @formatter:on
            });
        builder.GET("/easy/ai/chat", RequestPredicates.accept(MediaType.TEXT_HTML), request -> {
            Map<String, Object> data = new HashMap<>();
            data.put("contextPath", request.requestPath().contextPath().value());
            // 渲染并返回文件预览列表页面。
            return ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(PageUtils.write("chat.ftl", data));
        });
        // @formatter:off
        builder.POST("/easy/ai/chat", request ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(Result.success(easyAiService.chat(request.body(ChatRecord.class))))
        );
        builder.POST("/easy/ai/chat/stream", request -> {
            ChatRecord chatRecord = request.body(ChatRecord.class);
            Flux<String> stream = easyAiService.stream(chatRecord);
            return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .body(stream.doOnError(Throwable::printStackTrace));
        });
        // @formatter:on

        return builder.build();
    }

    @Bean("wb04307201EasyAiRouter")
    @ConditionalOnMissingBean
    public RouterFunction<ServerResponse> easyAiRouter(EasyAiService easyAiService) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        builder.GET("/easy/ai/chat", RequestPredicates.accept(MediaType.TEXT_HTML), request -> {
            Map<String, Object> data = new HashMap<>();
            data.put("contextPath", request.requestPath().contextPath().value());
            // 渲染并返回文件预览列表页面。
            return ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(PageUtils.write("chat.ftl", data));
        });
        // @formatter:off
        builder.POST("/easy/ai/chat", request ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(Result.success(easyAiService.chat(request.body(ChatRecord.class))))
        );
        builder.POST("/easy/ai/chat/stream", request -> {
            ChatRecord chatRecord = request.body(ChatRecord.class);
            Flux<String> stream = easyAiService.stream(chatRecord);
            return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .body(stream.doOnError(Throwable::printStackTrace));
        });
        // @formatter:on

        return builder.build();
    }
}
