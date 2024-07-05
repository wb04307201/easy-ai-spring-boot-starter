package cn.wubo.easy.ai.file;

import cn.wubo.easy.ai.dto.FileStorageDTO;

import java.util.List;

public interface IFileStorageRecord {

    FileStorageDTO save(FileStorageDTO fileStorageDTO);

    List<FileStorageDTO> list(FileStorageDTO fileStorageDTO);

    FileStorageDTO findById(String id);

    Boolean delete(FileStorageDTO fileStorageDTO);

    void init();
}
