# easy-ai-spring-boot-starter
# 易智Spring

[![](https://jitpack.io/v/wb04307201/file-preview-spring-boot-starter.svg)](https://jitpack.io/#wb04307201/file-preview-spring-boot-starter)
[![star](https://gitee.com/wb04307201/file-preview-spring-boot-starter/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/file-preview-spring-boot-starter)
[![fork](https://gitee.com/wb04307201/file-preview-spring-boot-starter/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/file-preview-spring-boot-starter)
[![star](https://img.shields.io/github/stars/wb04307201/file-preview-spring-boot-starter)](https://github.com/wb04307201/file-preview-spring-boot-starter)
[![fork](https://img.shields.io/github/forks/wb04307201/file-preview-spring-boot-starter)](https://github.com/wb04307201/file-preview-spring-boot-starter)

> 快速集成AI大模型到Spring项目的组件

## 代码示例

## 第一步 增加 JitPack 仓库
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## 第二步 引入jar
```xml
<dependency>
    <groupId>com.github.wb04307201</groupId>
    <artifactId>easy-ai-spring-boot-starter</artifactId>
    <version>0.5.0</version>
</dependency>
```

## 第三步 在启动类上加上`@EnableFilePreview`注解
```java
@EnableFilePreview
@SpringBootApplication
public class FilePreviewDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilePreviewDemoApplication.class, args);
    }

}
```