package cn.wubo.easy.ai.file.impl;

import cn.wubo.easy.ai.file.IFileStorageService;
import cn.wubo.easy.ai.exception.DocumentStorageRuntimeException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileStorageServiceImpl implements IFileStorageService {

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
            throw new DocumentStorageRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean delete(String path) {
        Path filePath = Paths.get(path);
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new DocumentStorageRuntimeException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Resource getResource(String path) {
        return new FileSystemResource(path);
    }

    @Override
    public void init() {

    }
}