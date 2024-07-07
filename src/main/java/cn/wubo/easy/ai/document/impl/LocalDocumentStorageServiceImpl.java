package cn.wubo.easy.ai.document.impl;

import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import cn.wubo.easy.ai.document.IDocumentStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalDocumentStorageServiceImpl implements IDocumentStorageService {

    private String basePath = "temp";

    @Override
    public String save(InputStream is, String fileName) {
        Path filePath = Paths.get(basePath, fileName);
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.exists(filePath)) Files.delete(filePath);
            Files.createFile(filePath);
            Files.copy(is, filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new EasyAiRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean delete(String path) {
        Path filePath = Paths.get(path);
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new EasyAiRuntimeException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Resource getResource(String path) {
        return new FileSystemResource(path);
    }

    @Override
    public byte[] getBytes(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new EasyAiRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void init() {

    }
}
