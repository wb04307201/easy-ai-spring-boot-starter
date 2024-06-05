package cn.wubo.easy.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableEasyAi
public class EasyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyAiApplication.class, args);
    }

}
