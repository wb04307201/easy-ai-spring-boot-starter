package cn.wubo.easy.ai;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class EasyAiTestConfiguration {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    public ExtractedTextFormatter extractedTextFormatter() {
        return ExtractedTextFormatter.builder().build();
    }
}
