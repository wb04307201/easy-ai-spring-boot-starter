package cn.wubo.easy.ai.config;

import cn.wubo.easy.ai.core.EasyAiService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({SplitterProperties.class, ReaderProperties.class})
public class EasyAiConfiguration {

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

    @Bean
    public EasyAiService easyAiService(VectorStore vectorStore, ChatModel chatModel, TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter textFormatter) {
        return new EasyAiService(vectorStore, chatModel, tokenTextSplitter, textFormatter);
    }
}
