package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyMaterialFile;
import com.honghe.party.mapper.PartyMaterialFileMapper;
import com.honghe.party.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private PartyMaterialFileMapper materialFileMapper;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 查询某成员在特定步骤下的材料列表
     */
    @GetMapping("/list")
    public Result<List<PartyMaterialFile>> listMaterials(
            @RequestParam Long memberId,
            @RequestParam(required = false) Integer stepCode) {
        LambdaQueryWrapper<PartyMaterialFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PartyMaterialFile::getMemberId, memberId);
        if (stepCode != null) {
            wrapper.eq(PartyMaterialFile::getStepCode, stepCode);
        }
        wrapper.orderByAsc(PartyMaterialFile::getId);
        return Result.success(materialFileMapper.selectList(wrapper));
    }

    /**
     * 上传附件材料（本地文件系统存储，无需依赖 MinIO）
     */
    @PostMapping("/upload")
    public Result<PartyMaterialFile> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("memberId") Long memberId,
            @RequestParam("stepCode") Integer stepCode,
            @RequestParam("materialCode") String materialCode,
            @RequestParam("materialName") String materialName) throws IOException {

        if (file.isEmpty()) {
            return Result.error("上传的材料文件不能为空");
        }

        // 文件大小防护：单文件不超过 30MB
        if (file.getSize() > 30L * 1024 * 1024) {
            return Result.error("单份材料文件大小不能超过 30MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = cn.hutool.core.io.FileUtil.extName(originalFilename);
        List<String> allowedExts = List.of("pdf", "doc", "docx", "jpg", "jpeg", "png");
        if (extension == null || !allowedExts.contains(extension.toLowerCase())) {
            return Result.error("仅支持上传常用文档或图片格式 (PDF, Word, JPG, PNG)");
        }

        String storedPath = fileStorageService.storeFile(file, memberId, stepCode);

        PartyMaterialFile record = new PartyMaterialFile();
        record.setMemberId(memberId);
        record.setStepCode(stepCode);
        record.setMaterialCode(materialCode);
        record.setMaterialName(materialName);
        record.setFilePath(storedPath);
        record.setFileSize(file.getSize());
        record.setIsRequired(true);
        record.setReviewStatus(1); // 1: 待审核
        record.setCreatedAt(LocalDateTime.now());

        materialFileMapper.insert(record);

        return Result.success("材料上传成功", record);
    }

    /**
     * 下载附件文件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        PartyMaterialFile fileRecord = materialFileMapper.selectById(id);
        if (fileRecord == null || fileRecord.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileStorageService.loadAsResource(fileRecord.getFilePath());
        String encodedFilename = URLEncoder.encode(fileRecord.getMaterialName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }
}
