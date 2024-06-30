package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAIConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EasyAIConfiguration.class})
public @interface EnableEasyAI {
}
