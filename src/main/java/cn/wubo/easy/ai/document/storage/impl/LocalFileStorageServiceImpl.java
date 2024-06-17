package cn.wubo.easy.ai.document.storage.impl;

import cn.wubo.easy.ai.document.storage.FileStorageDTO;
import cn.wubo.easy.ai.document.storage.IFileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileStorageServiceImpl implements IFileStorageService {

    private String basePath = "temp";

    @Override
    public FileStorageDTO save(byte[] bytes, String fileName) {
        FileStorageDTO dto = new FileStorageDTO();
        dto.setFileName(fileName);

        Path filePath = Paths.get(basePath, fileName);
        dto.setFilePath(filePath.toString());
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.exists(filePath)) Files.delete(filePath);
            Files.createFile(filePath);
            Files.write(filePath, bytes);
            return dto;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean delete(FileStorageDTO dto) {
        Path filePath = Paths.get(dto.getFilePath());
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Resource getBytes(FileStorageDTO dto) {
        return new FileSystemResource(dto.getFilePath());
    }

    @Override
    public void init() {

    }
}
