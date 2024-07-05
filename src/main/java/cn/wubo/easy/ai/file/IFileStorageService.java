package cn.wubo.easy.ai.file;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface IFileStorageService {

    String save(InputStream is, String fileName);

    Boolean delete(String path);

    Resource getResource(String path);

    byte[] getBytes(String path);

    void init();
}
