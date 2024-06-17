package cn.wubo.easy.ai.document.vectorStore.impl;

import cn.wubo.easy.ai.document.vectorStore.IVectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

@Slf4j
public class VectorStoreServiceImpl implements IVectorStoreService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final ExtractedTextFormatter textFormatter;

    public VectorStoreServiceImpl(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter textFormatter) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
        this.textFormatter = textFormatter;
    }

    /**
     * 从给定的资源文件中提取文本，并将其拆分为文档列表，然后将这些文档保存到向量数据库中。
     *
     * @param fileResource 要处理的资源文件，不能为null。
     */
    @Override
    public void saveSource(Resource fileResource) {
        // 初始化Tika解析器并从文件提取文本
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(fileResource, textFormatter);

        // 将提取的文本拆分为文档列表
        List<Document> documentList = tokenTextSplitter.apply(tikaDocumentReader.get());

        // 记录拆分后的文档数量
        log.debug("拆分出数据条数 {}", documentList.size());

        // 开始保存文档列表到向量数据库
        log.debug("保存向量数据库开始");
        vectorStore.accept(documentList);

        // 完成保存向量数据库操作
        log.debug("保存向量数据库完成");
    }

    /**
     * 执行相似性搜索。
     * <p>
     * 该方法通过查询向量数据库来寻找与给定查询最相似的文档。相似性搜索是基于查询的向量表示和数据库中文档的向量表示之间的相似度计算的。
     * 使用向量存储中的相似性搜索功能来执行此操作，这通常涉及计算查询向量与数据库中每个文档向量之间的距离或相似度得分，并返回得分最高的文档。
     *
     * @param query 查询字符串，它被转换为向量以便进行相似性搜索。
     * @return 包含相似文档的列表。这个列表中的每个元素都是一个Document对象，代表了与查询相似的文档。
     */
    @Override
    public List<Document> similaritySearch(String query) {
        // 调用向量存储的similaritySearch方法来执行相似性搜索，并获取结果列表。
        List<Document> listOfSimilarDocuments = vectorStore.similaritySearch(query);

        // 使用日志记录搜索结果的数量和内容，以供调试和监控使用。
        log.debug("找到向量数据 {} 内容 {}", listOfSimilarDocuments.size(), listOfSimilarDocuments);

        // 返回包含相似文档的列表。
        return listOfSimilarDocuments;
    }

    @Override
    public Optional<Boolean> delete(List<String> idList) {
        return vectorStore.delete(idList);
    }
}
