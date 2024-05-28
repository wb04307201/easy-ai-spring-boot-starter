package cn.wubo.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ReaderProperties 类用于配置与AI阅读器相关的属性。
 * 这些配置项可以在应用的配置文件中以 "spring.ai.reader" 前缀定义。
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.reader")
public class ReaderProperties {

    /**
     * 是否开启左对齐模式。默认值：false。
     */
    private boolean leftAlignment = false;

    /**
     * 在删除之前要跳过的顶部页面数量。默认值：0。
     */
    private int numberOfTopPagesToSkipBeforeDelete = 0;

    /**
     * 要删除的顶部文本行数。默认值：0。
     */
    private int numberOfTopTextLinesToDelete = 0;

    /**
     * 要删除的底部文本行数。默认值：0。
     */
    private int numberOfBottomTextLinesToDelete = 0;
}


