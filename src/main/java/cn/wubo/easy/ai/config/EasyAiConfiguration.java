package cn.wubo.easy.ai.config;

import cn.wubo.easy.ai.core.ChatRecord;
import cn.wubo.easy.ai.core.DocumentStorageDTO;
import cn.wubo.easy.ai.core.EasyAiService;
import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.document.IDocumentStorageRecord;
import cn.wubo.easy.ai.document.IDocumentStorageService;
import cn.wubo.easy.ai.document.impl.DocumentReaderServiceImpl;
import cn.wubo.easy.ai.document.impl.LocalDocumentStorageServiceImpl;
import cn.wubo.easy.ai.document.impl.MemDocumentStorageRecordImpl;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import cn.wubo.easy.ai.exception.PageRuntimeException;
import cn.wubo.easy.ai.result.Result;
import cn.wubo.easy.ai.utils.FileUtils;
import cn.wubo.easy.ai.utils.IoUtils;
import cn.wubo.easy.ai.utils.PageUtils;
import jakarta.servlet.http.Part;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@EnableConfigurationProperties({SplitterProperties.class, ReaderProperties.class, EasyAiProperties.class})
public class EasyAiConfiguration {

    EasyAiProperties properties;

    public EasyAiConfiguration(EasyAiProperties properties) {
        this.properties = properties;
    }

    /**
     * 配置一个名为"easyAiExecutor"的线程池任务执行器。
     * 这个方法创建并配置了一个ThreadPoolTaskExecutor实例，用于处理EasyAI相关的异步任务。
     * 它通过设置核心线程数、最大线程数、队列容量、线程空闲时间和线程名前缀来定制线程池的行为。
     * 此外，还设置了拒绝策略为CallerRunsPolicy，当队列满时，任务将由调用线程执行。
     *
     * @return ThreadPoolTaskExecutor 一个配置好的线程池任务执行器。
     */
    @Bean(name = "easyAiExecutor")
    public Executor easyAiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数为10，这些线程会一直存在，即使没有任务需要执行
        executor.setCorePoolSize(10);
        // 设置最大线程数为20，当任务队列满时，线程池会增加线程来处理任务，直到达到这个最大值
        executor.setMaxPoolSize(20);
        // 设置任务队列容量为500，当提交的任务数超过核心线程数时，新任务会被放入这个队列等待执行
        executor.setQueueCapacity(500);
        // 设置线程的空闲时间，超过这个时间且线程池中线程数量大于核心线程数时，线程会被终止
        executor.setKeepAliveSeconds(60);
        // 设置线程名前缀，方便识别线程池中的线程
        executor.setThreadNamePrefix("EasyAiExecutor-");
        // 设置拒绝策略为CallerRunsPolicy，当队列满且线程池中的线程数量达到最大值时，由调用者线程执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池，使配置生效
        executor.initialize();
        return executor;
    }

    @Bean
    public IDocumentStorageRecord fileStorageRecord() {
        return new MemDocumentStorageRecordImpl();
    }

    @Bean
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
     * 创建并配置DocumentReaderService bean。
     * 该方法通过依赖注入的方式，使用TokenTextSplitter和ExtractedTextFormatter来实例化DocumentReaderServiceImpl。
     * 这种方式允许Spring容器管理对象的生命周期，并负责对象之间的依赖关系。
     *
     * @param tokenTextSplitter      用于分割文本的令牌化器，它将文本拆分成更小的单元，以便进行进一步处理。
     * @param extractedTextFormatter 对提取的文本进行格式化的处理器，它负责将原始文本格式化为适合进一步处理的格式。
     * @return 返回一个配置好的IDocumentReaderService实例，该实例可以用于读取和处理文档。
     */
    @Bean
    public IDocumentReaderService doumentReaderService(TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter extractedTextFormatter) {
        return new DocumentReaderServiceImpl(tokenTextSplitter, extractedTextFormatter);
    }

    @Bean(value = "easyAiChatClient")
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        if (properties.getDefaultSystem() != null) builder.defaultSystem(properties.getDefaultSystem());
        if (Boolean.TRUE.equals(properties.getEnableRag()))
            builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()), new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults(), properties.getUserTextAdvise()), new SimpleLoggerAdvisor());
        else builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()), new SimpleLoggerAdvisor());
        return builder.build();
    }

    /**
     * 创建并配置EasyAiService实例。
     * 该方法通过依赖注入获得多个服务和模型的实例，从中选择特定的文件存储服务和文件存储记录实例，
     * 并初始化它们。然后，使用这些实例创建EasyAiService对象。
     *
     * @param fileStorageServiceList 文件存储服务列表，用于筛选符合特定类名的文件存储服务实例。
     * @param fileStorageRecordList  文件存储记录列表，用于筛选符合特定类名的文件存储记录实例。
     * @param documentReaderService  文档阅读服务实例，用于处理文档阅读相关任务。
     * @param vectorStore            向量存储实例，用于存储和检索向量数据。
     * @param chatClient             聊天模型实例，用于支持聊天功能。
     * @return 配置完成的EasyAiService实例。
     * @throws EasyAiRuntimeException 如果无法找到配置的文件存储服务或文件存储记录对应的Bean，则抛出此异常。
     */
    @Bean
    public EasyAiService easyAiService(List<IDocumentStorageService> fileStorageServiceList, List<IDocumentStorageRecord> fileStorageRecordList, IDocumentReaderService documentReaderService, VectorStore vectorStore, @Qualifier(value = "easyAiChatClient") ChatClient chatClient) {
        // 从文件存储服务列表中筛选出符合配置类名的服务实例，并初始化它。
        // @formatter:off
        IDocumentStorageService fileStorageService = fileStorageServiceList.stream()
                .filter(obj -> obj.getClass().getName().equals(properties.getFileStorageServiceClass()))
                .findAny()
                .orElseThrow(() -> new EasyAiRuntimeException(String.format("未找到%s对应的bean，无法加载IFileStorageService！", properties.getFileStorageServiceClass())));
        fileStorageService.init();

        // 从文件存储记录列表中筛选出符合配置类名的记录实例，并初始化它。
        IDocumentStorageRecord fileStorageRecord = fileStorageRecordList.stream()
                .filter(obj -> obj.getClass().getName().equals(properties.getFileStorageRecordClass()))
                .findAny()
                .orElseThrow(() -> new EasyAiRuntimeException(String.format("未找到%s对应的bean，无法加载IFileStorageRecord！", properties.getFileStorageRecordClass())));
        fileStorageRecord.init();
        // @formatter:on

        // 使用筛选并初始化的服务和模型实例创建EasyAiService对象。
        return new EasyAiService(fileStorageService, fileStorageRecord, documentReaderService, vectorStore, chatClient);
    }


    private static final String LOST_ID = "请求参数id丢失!";

    @Bean("wb04307201EasyAiRouter")
    public RouterFunction<ServerResponse> easyAiRouter(EasyAiService easyAiService) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        // 如果启用了Web和REST功能，添加文件预览列表的HTML页面渲染。
        if (properties.getEnableWeb() && properties.getEnableRest()) {
            builder.GET("/easy/ai/list", RequestPredicates.accept(MediaType.TEXT_HTML), request -> {
                Map<String, Object> data = new HashMap<>();
                data.put("contextPath", request.requestPath().contextPath().value());
                // 渲染并返回文件预览列表页面。
                return ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(PageUtils.write("list.ftl", data));
            });
            builder.GET("/easy/ai/chat", RequestPredicates.accept(MediaType.TEXT_HTML), request -> {
                Map<String, Object> data = new HashMap<>();
                data.put("contextPath", request.requestPath().contextPath().value());
                // 渲染并返回文件预览列表页面。
                return ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(PageUtils.write("chat.ftl", data));
            });
        }
        if (Boolean.TRUE.equals(properties.getEnableRest())) {
            builder.POST("/easy/ai/list", request -> {
                DocumentStorageDTO documentStorageDTO = request.body(DocumentStorageDTO.class);
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(easyAiService.list(documentStorageDTO)));
            });
            builder.POST("/easy/ai/upload", request -> {
                Part part = request.multipartData().getFirst("file");
                DocumentStorageDTO documentStorageDTO = easyAiService.upload(part.getInputStream(), part.getSubmittedFileName());
                documentStorageDTO = easyAiService.read(documentStorageDTO);
                documentStorageDTO = easyAiService.save(documentStorageDTO);
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(documentStorageDTO));
            });
            builder.GET("/easy/ai/delete", request -> {
                String id = request.param("id").orElseThrow(() -> new IllegalArgumentException(LOST_ID));
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(easyAiService.delete(id)));
            });
            builder.GET("/easy/ai/download", request -> {
                String id = request.param("id").orElseThrow(() -> new IllegalArgumentException(LOST_ID));
                DocumentStorageDTO documentStorageDTO = easyAiService.findById(id);
                byte[] bytes = easyAiService.getBytes(documentStorageDTO);
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
            builder.POST("/easy/ai/chat", request -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Result.success(easyAiService.chat(request.body(ChatRecord.class)))));
            builder.POST("/easy/ai/chat/stream", request -> ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(easyAiService.stream(request.body(ChatRecord.class))));
        }
        return builder.build();
    }
}
