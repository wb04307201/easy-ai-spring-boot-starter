# Easy AI 简单易用的AI功能集成

> 一个用于简化Spring Boot应用程序中AI功能集成的starter包，支持聊天功能和文档处理（RAG - 检索增强生成）。

[![](https://jitpack.io/v/com.gitee.wb04307201/easy-ai.svg)](https://jitpack.io/#com.gitee.wb04307201/easy-ai)
[![star](https://gitee.com/wb04307201/easy-ai/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/easy-ai)
[![fork](https://gitee.com/wb04307201/easy-ai/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/easy-ai)
[![star](https://img.shields.io/github/stars/wb04307201/easy-ai)](https://github.com/wb04307201/easy-ai)
[![fork](https://img.shields.io/github/forks/wb04307201/easy-ai)](https://github.com/wb04307201/easy-ai)  
![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-17+-green.svg) ![SpringBoot](https://img.shields.io/badge/Srping%20Boot-3+-green.svg)

## 功能特性

- 🤖 AI聊天功能：支持普通聊天和流式聊天
- 📄 文档处理：支持文档上传、存储、读取和检索
- 🧠 RAG支持：基于向量存储的检索增强生成
- ⚙️ 自动配置：通过Spring Boot自动配置简化集成
- 🎛️ 可配置：丰富的配置选项，满足不同场景需求

## 快速开始
### 引入依赖
增加 JitPack 仓库
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
引入jar
```xml
<dependency>
    <groupId>com.github.wb04307201.easy-ai</groupId>
    <artifactId>easy-ai-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 安装向量数据库
通过docker安装chromadb数据库
```shell
docker run -it --rm --name chroma -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0
```

### 安装大语言模型
默认通过[ollama](https://ollama.com/)使用大模型，下载并安装
```shell
ollama pull qwen3
ollama pull nomic-embed-text
```

### 添加相关配置
```yaml
spring:
  application:
    name: spring_ai_demo
  ai:
    ollama:
      chat:
        options:
          model: qwen3
      embedding:
        options:
          model: nomic-embed-text
      base-url: http://localhost:11434
    vectorstore:
      chroma:
        client:
          host: http://localhost
          port: 8000
        collection-name: SpringAiCollection
        initialize-schema: true
    easy:
      enableRag: true
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### 使用检索增强生成(RAG)辅助对话
当未上传知识库时  
![img_4.png](img_4.png)  
显然开始胡说八道了

现在让我们上传一些知识库，访问文档上传界面[http://localhost:8080//easy/ai/list](http://localhost:8080//easy/ai/list)  
![img.png](img.png)  
状态列显示“向量存储完”即文档已转入知识库  

访问聊天界面[http://localhost:8080//easy/ai/chat](http://localhost:8080//easy/ai/chat)  
![img_5.png](img_5.png)