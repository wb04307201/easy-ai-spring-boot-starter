package cn.wubo.easy.ai.config;

import cn.wubo.easy.ai.core.EasyAiService;
import cn.wubo.easy.ai.core.Payload;
import cn.wubo.easy.ai.document.IDocumentReaderService;
import cn.wubo.easy.ai.document.IDocumentService;
import cn.wubo.easy.ai.document.impl.DocumentReaderServiceImpl;
import cn.wubo.easy.ai.exception.DocumentStorageRuntimeException;
import cn.wubo.easy.ai.file.IDocumentStorageRecord;
import cn.wubo.easy.ai.file.IFileStorageService;
import cn.wubo.easy.ai.file.impl.LocalFileStorageServiceImpl;
import jakarta.servlet.http.Part;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
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
    public IFileStorageService documentStorageService() {
        return new LocalFileStorageServiceImpl();
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
        return new TokenTextSplitter(properties.getDefaultChunkSize(), properties.getMinChunkSizeChars(), properties.getMinChunkLengthToEmbed(), properties.getMaxNumChunks(), properties.isKeepSeparator());
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

    @Bean
    public EasyAiService easyAiService(List<IFileStorageService> fileStorageServiceList, IDocumentStorageRecord documentStorageRecord, IDocumentService documentService) {
        IFileStorageService fileStorageService = fileStorageServiceList.stream().filter(obj -> obj.getClass().getName().equals(properties.getDocumentStorageClass())).findAny().orElseThrow(() -> new DocumentStorageRuntimeException(String.format("未找到%s对应的bean，无法加载IFileStorageService！", properties.getDocumentStorageClass())));
        fileStorageService.init();

        return new EasyAiService(fileStorageService, documentStorageRecord, documentService);
    }

    @Bean("wb04307201EasyAiRouter")
    public RouterFunction<ServerResponse> easyAiRouter(EasyAiService easyAiService) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        builder.POST("/easy/ai/chatWithPro", request -> {
            Payload payload = request.body(Payload.class);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(easyAiService.chat(payload));
        });
        builder.POST("/easy/ai/chat", request -> {
            Payload payload = request.body(Payload.class);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(easyAiService.chat(payload));
        });
        builder.POST("/easy/ai/upload", request -> {
            MultiValueMap<String, Part> multiValueMap = request.multipartData();
            easyAiService.saveSource(multiValueMap);
            Part part = request.multipartData().getFirst("file");

            part.write("test.pdf");

            part.getInputStream();
            part.getSubmittedFileName();

            Payload payload = request.body(Payload.class);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(easyAiService.chat(payload));
        });
        return builder.build();
    }
}
