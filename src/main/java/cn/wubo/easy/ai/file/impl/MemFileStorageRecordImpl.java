package cn.wubo.easy.ai.file.impl;

import cn.wubo.easy.ai.dto.FileStorageDTO;
import cn.wubo.easy.ai.exception.EasyAiRuntimeException;
import cn.wubo.easy.ai.file.IFileStorageRecord;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MemFileStorageRecordImpl implements IFileStorageRecord {

    private static List<FileStorageDTO> fileStorageDTOS = new ArrayList<>();

    @Override
    public FileStorageDTO save(FileStorageDTO fileStorageDTO) {
        if (StringUtils.hasLength(fileStorageDTO.getId())) {
            fileStorageDTOS.stream().filter(e -> e.getId().equals(fileStorageDTO.getId())).findAny().ifPresent(e -> e = fileStorageDTO);
        } else {
            fileStorageDTO.setId(UUID.randomUUID().toString());
            fileStorageDTOS.add(fileStorageDTO);
        }
        return fileStorageDTO;
    }

    @Override
    public List<FileStorageDTO> list(FileStorageDTO fileStorageDTO) {
        // @formatter:off
        return fileStorageDTOS.stream()
                .filter(e -> !StringUtils.hasLength(fileStorageDTO.getId()) || e.getId().equals(fileStorageDTO.getId())).
                filter(e -> !StringUtils.hasLength(fileStorageDTO.getFileName()) || e.getFileName().contains(fileStorageDTO.getFileName()))
                .filter(e -> !StringUtils.hasLength(fileStorageDTO.getFilePath()) || e.getFilePath().contains(fileStorageDTO.getFilePath()))
                .toList();
        // @formatter:on
    }

    @Override
    public FileStorageDTO findById(String id) {
        Optional<FileStorageDTO> optionalFileInfo = fileStorageDTOS.stream().filter(e -> e.getId().equals(id)).findAny();
        if (optionalFileInfo.isPresent()) return optionalFileInfo.get();
        else throw new EasyAiRuntimeException("文件记录未找到!");
    }

    @Override
    public Boolean delete(FileStorageDTO fileStorageDTO) {
        return fileStorageDTOS.removeAll(list(fileStorageDTO));
    }

    @Override
    public void init() {

    }
}
