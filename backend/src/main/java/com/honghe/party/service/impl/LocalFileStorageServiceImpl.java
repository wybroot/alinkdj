package com.honghe.party.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.honghe.party.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${party.storage.local-path:./uploads}")
    private String localBasePath;

    @Override
    public String storeFile(MultipartFile file, Long memberId, Integer stepCode) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        String yearMonth = LocalDate.now().getYear() + "/" + String.format("%02d", LocalDate.now().getMonthValue());
        
        // 目录层级：uploads/年/月/成员ID/步骤编码/
        String relativeFolder = String.format("%s/%d/step_%d", yearMonth, memberId, stepCode);
        Path targetDir = Paths.get(localBasePath, relativeFolder);
        FileUtil.mkdir(targetDir.toFile());

        String newFileName = IdUtil.simpleUUID() + (extension.isEmpty() ? "" : "." + extension);
        File targetFile = targetDir.resolve(newFileName).toFile();
        
        file.transferTo(targetFile);

        return relativeFolder + "/" + newFileName;
    }

    @Override
    public String storeMeetingAttachment(MultipartFile file, Long meetingId, String attachType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的会议附件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        String yearMonth = LocalDate.now().getYear() + "/" + String.format("%02d", LocalDate.now().getMonthValue());

        // 目录层级：uploads/meetings/年/月/会议ID/附件类型/
        String relativeFolder = String.format("meetings/%s/%d/%s", yearMonth, meetingId, attachType.toLowerCase());
        Path targetDir = Paths.get(localBasePath, relativeFolder);
        FileUtil.mkdir(targetDir.toFile());

        String newFileName = IdUtil.simpleUUID() + (extension.isEmpty() ? "" : "." + extension);
        File targetFile = targetDir.resolve(newFileName).toFile();

        file.transferTo(targetFile);

        return relativeFolder + "/" + newFileName;
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        Path filePath = Paths.get(localBasePath, relativePath);
        File file = filePath.toFile();
        if (!file.exists() || !file.canRead()) {
            throw new RuntimeException("文件不存在或无法读取: " + relativePath);
        }
        return new FileSystemResource(file);
    }

    @Override
    public boolean deleteFile(String relativePath) {
        Path filePath = Paths.get(localBasePath, relativePath);
        return FileUtil.del(filePath.toFile());
    }
}
