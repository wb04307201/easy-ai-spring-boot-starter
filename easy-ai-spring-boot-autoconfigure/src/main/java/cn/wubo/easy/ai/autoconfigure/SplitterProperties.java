package cn.wubo.easy.ai.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyAiProperties 类用于存储与文本分割相关的配置属性。
 * 这些属性通过@ConfigurationProperties 注解绑定到 "spring.ai.transformer.splitter" 前缀的配置键。
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.transformer.splitter")
public class SplitterProperties {

    /**
     * 默认每个文本块的目标令牌数。
     * 设置为 800，默认值可以在配置文件中更改。
     */
    private int defaultChunkSize = 800;

    /**
     * 每个文本块的最小字符数。
     * 设置为 350，默认值可以在配置文件中更改。
     */
    private int minChunkSizeChars = 350;

    /**
     * 当生成的文本块长度小于这个值时将被丢弃。
     * 设置为 5，默认值可以在配置文件中更改。
     */
    private int minChunkLengthToEmbed = 5;

    /**
     * 从文本中生成的最大块数。
     * 设置为 10000，默认值可以在配置文件中更改。
     */
    private int maxNumChunks = 10000;

    /**
     * 是否保留分割符。
     * 如果为 true，分割文本时会保留分隔符，默认为 true。
     */
    private boolean keepSeparator = true;
}

