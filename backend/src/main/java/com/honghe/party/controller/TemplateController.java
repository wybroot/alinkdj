package com.honghe.party.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyDocTemplate;
import com.honghe.party.mapper.PartyDocTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private PartyDocTemplateMapper templateMapper;

    @Value("${party.storage.local-path:./uploads}")
    private String localBasePath;

    /**
     * 获取全流程 25 步文档模板清单（展示默认/自定义状态）
     */
    @GetMapping("/list")
    public Result<List<PartyDocTemplate>> listTemplates() {
        List<PartyDocTemplate> list = templateMapper.selectList(
            new LambdaQueryWrapper<PartyDocTemplate>().orderByAsc(PartyDocTemplate::getStepCode)
        );
        return Result.success("获取模板列表成功", list);
    }

    /**
     * 管理员自定义导入上传模板 (.docx)
     */
    @PostMapping("/{stepCode}/upload")
    public Result<PartyDocTemplate> uploadCustomTemplate(
            @PathVariable Integer stepCode,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "管理员") String operatorName) throws IOException {

        if (file.isEmpty()) {
            return Result.error("上传的模板文件不能为空");
        }

        PartyDocTemplate template = templateMapper.selectOne(
            new LambdaQueryWrapper<PartyDocTemplate>().eq(PartyDocTemplate::getStepCode, stepCode)
        );
        if (template == null) {
            return Result.error(404, "未找到该步骤的模板记录");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        if (!"docx".equalsIgnoreCase(extension) && !"doc".equalsIgnoreCase(extension)) {
            return Result.error("仅支持上传 Word 格式模板 (.docx)");
        }

        // 保存至 uploads/templates/custom/step_X/
        String relativeFolder = "templates/custom/step_" + stepCode;
        Path targetDir = Paths.get(localBasePath, relativeFolder);
        FileUtil.mkdir(targetDir.toFile());

        String newFileName = "custom_step" + stepCode + "_" + IdUtil.simpleUUID() + "." + extension;
        File targetFile = targetDir.resolve(newFileName).toFile();
        file.transferTo(targetFile);

        // 更新模板记录为“自定义状态”
        template.setIsCustomized(true);
        template.setCustomFileName(originalFilename);
        template.setCustomFilePath(relativeFolder + "/" + newFileName);
        template.setUpdateUserName(operatorName);
        template.setUpdatedAt(LocalDateTime.now());
        template.setFileVersion("v" + (System.currentTimeMillis() % 1000));

        templateMapper.updateById(template);

        return Result.success("管理员自定义模板导入成功！已生效为当前使用版本。", template);
    }

    /**
     * 恢复为官方默认内置模板
     */
    @PostMapping("/{stepCode}/restore-default")
    public Result<PartyDocTemplate> restoreDefaultTemplate(
            @PathVariable Integer stepCode,
            @RequestParam(defaultValue = "管理员") String operatorName) {

        PartyDocTemplate template = templateMapper.selectOne(
            new LambdaQueryWrapper<PartyDocTemplate>().eq(PartyDocTemplate::getStepCode, stepCode)
        );
        if (template == null) {
            return Result.error(404, "未找到该步骤模板");
        }

        // 如果存在自定义文件，可清理或保留，将状态改回 false
        template.setIsCustomized(false);
        template.setUpdateUserName(operatorName);
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);

        return Result.success("已恢复为系统官方默认内置模板！", template);
    }

    /**
     * 下载模板 (可选下载默认版本或自定义版本)
     */
    @GetMapping("/{stepCode}/download")
    public ResponseEntity<Resource> downloadTemplate(
            @PathVariable Integer stepCode,
            @RequestParam(defaultValue = "false") Boolean forceDefault) {

        PartyDocTemplate template = templateMapper.selectOne(
            new LambdaQueryWrapper<PartyDocTemplate>().eq(PartyDocTemplate::getStepCode, stepCode)
        );
        if (template == null) {
            return ResponseEntity.notFound().build();
        }

        String targetPath = (Boolean.TRUE.equals(template.getIsCustomized()) && !forceDefault)
                ? template.getCustomFilePath()
                : template.getDefaultFilePath();
        String downloadName = (Boolean.TRUE.equals(template.getIsCustomized()) && !forceDefault)
                ? template.getCustomFileName()
                : template.getDefaultFileName();

        Path path = Paths.get(localBasePath, targetPath);
        File file = path.toFile();
        if (!file.exists()) {
            // 如果物理文件不存在（初次部署），返回示例虚拟资源句柄
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(downloadName != null ? downloadName : "template.docx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(new FileSystemResource(file));
    }
}
