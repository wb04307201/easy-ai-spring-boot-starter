package cn.wubo.easy.ai.vector_store.impl;

import cn.wubo.easy.ai.vector_store.IVectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

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
     * 保存文档列表到向量数据库。
     *
     * 此方法用于批量保存一组文档到向量数据库中。在实际操作前，会记录开始保存的日志信息，
     * 保存操作完成后，会再次记录保存完成的日志信息，以方便问题追踪和性能监控。
     *
     * @param documentList 待保存的文档列表，每个文档代表一个向量数据。
     */
    @Override
    public void save(List<Document> documentList) {
        // 记录开始保存文档的日志
        log.debug("保存向量数据库开始");
        // 将文档列表保存到向量数据库
        vectorStore.accept(documentList);
        // 记录保存文档完成的日志
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

    /**
     * 删除指定ID列表的向量。
     *
     * @param idList 向量的ID列表，这些ID代表了需要被删除的向量。
     * @return 返回一个Optional<Boolean>对象，其中包含了删除操作的结果。
     *         如果删除操作成功执行，Optional中的Boolean值为true；
     *         如果删除操作未能执行（例如，由于某些ID不存在），Optional中的Boolean值为false。
     * @override 该方法重写了父类或接口中的delete方法，以适应当前类的特定需求。
     */
    @Override
    public Optional<Boolean> delete(List<String> idList) {
        return vectorStore.delete(idList);
    }

}
