# easy-ai-spring-boot-starter
# 易智Spring

[![](https://jitpack.io/v/wb04307201/easy-ai-spring-boot-starter.svg)](https://jitpack.io/#wb04307201/easy-ai-spring-boot-starter)
[![star](https://gitee.com/wb04307201/easy-ai-spring-boot-starter/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/easy-ai-spring-boot-starter)
[![fork](https://gitee.com/wb04307201/easy-ai-spring-boot-starter/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/easy-ai-spring-boot-starter)
[![star](https://img.shields.io/github/stars/wb04307201/easy-ai-spring-boot-starter)](https://github.com/wb04307201/easy-ai-spring-boot-starter)
[![fork](https://img.shields.io/github/forks/wb04307201/easy-ai-spring-boot-starter)](https://github.com/wb04307201/easy-ai-spring-boot-starter)

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
@EnableEasyAi
@SpringBootApplication
public class EasyAiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilePreviewDemoApplication.class, args);
    }

}
```


```shell
docker run -d --name chromadb -p 8000:8000 chromadb/chroma

docker run -d --name cassandra -p 9042:9042 cassandra
```

上传文件-》存储文件/记录（状态：上传文件）-》根据记录获取resource-》开始拆分文件（状态：拆分中）-》异步拆分文件-》存储拆分记录（状态：拆分结束）-》开始向量存储（状态：向量存储中）-》异步向量存储-》向量存储结束（状态：向量存储完成）
