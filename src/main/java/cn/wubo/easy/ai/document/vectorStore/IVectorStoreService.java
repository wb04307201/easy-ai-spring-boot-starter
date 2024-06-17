package cn.wubo.easy.ai.document.vectorStore;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public interface IVectorStoreService {

    void saveSource(Resource fileResource);

    List<Document> similaritySearch(String query);

    Optional<Boolean> delete(List<String> idList);
}
