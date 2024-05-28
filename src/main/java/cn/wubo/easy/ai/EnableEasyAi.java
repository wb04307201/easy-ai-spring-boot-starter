package cn.wubo.easy.ai;

import cn.wubo.easy.ai.config.EasyAiConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EasyAiConfiguration.class})
public @interface EnableEasyAi {
}
