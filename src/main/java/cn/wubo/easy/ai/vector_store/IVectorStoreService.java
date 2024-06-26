package cn.wubo.easy.ai.vector_store;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Optional;

public interface IVectorStoreService {

    void save(List<Document> documentList);

    List<Document> similaritySearch(String query);

    Optional<Boolean> delete(List<String> idList);
}
