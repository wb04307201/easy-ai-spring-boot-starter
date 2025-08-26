package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.document.IDocumentReaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;

import java.util.List;

@Slf4j
public class DocumentReaderServiceImpl implements IDocumentReaderService {

    private final TokenTextSplitter tokenTextSplitter;
    private final ExtractedTextFormatter textFormatter;
    private final KeywordMetadataEnricher keywordMetadataEnricher;

    public DocumentReaderServiceImpl(TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter textFormatter, KeywordMetadataEnricher keywordMetadataEnricher) {
        this.tokenTextSplitter = tokenTextSplitter;
        this.textFormatter = textFormatter;
        this.keywordMetadataEnricher = keywordMetadataEnricher;
    }

    @Override
    public List<Document> read(Resource fileResource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(fileResource, textFormatter);
        List<Document> documentList = tikaDocumentReader.read();
        List<Document> tokenTextSplitterDocumentList = tokenTextSplitter.apply(documentList);
        List<Document> keywordMetadataEnricherDocumentList =keywordMetadataEnricher.apply(tokenTextSplitterDocumentList);
        log.debug("拆分出数据条数 {}", keywordMetadataEnricherDocumentList.size());
        return keywordMetadataEnricherDocumentList;
    }
}
