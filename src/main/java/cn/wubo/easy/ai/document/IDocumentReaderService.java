package cn.wubo.easy.ai.document;

import cn.wubo.easy.ai.document.dto.DocumentStorageDTO;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface IDocumentReaderService {

    List<Document> saveSource(Resource fileResource);
}
