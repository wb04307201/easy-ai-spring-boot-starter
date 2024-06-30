package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.document.IDocumentReaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;

import java.util.List;

@Slf4j
public class DocumentReaderServiceImpl implements IDocumentReaderService {

    private final TokenTextSplitter tokenTextSplitter;
    private final ExtractedTextFormatter textFormatter;

    public DocumentReaderServiceImpl(TokenTextSplitter tokenTextSplitter, ExtractedTextFormatter textFormatter) {
        this.tokenTextSplitter = tokenTextSplitter;
        this.textFormatter = textFormatter;
    }

    @Override
    public List<Document> read(Resource fileResource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(fileResource, textFormatter);
        List<Document> documentList = tokenTextSplitter.apply(tikaDocumentReader.get());
        log.debug("拆分出数据条数 {}", documentList.size());
        return documentList;
    }
}
