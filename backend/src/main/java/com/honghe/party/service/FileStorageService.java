package com.honghe.party.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    /**
     * 保存上传的附件材料
     * @param file 附件文件
     * @param memberId 成员ID
     * @param stepCode 步骤编号 (1~25)
     * @return 存储相对路径
     */
    String storeFile(MultipartFile file, Long memberId, Integer stepCode) throws IOException;

    /**
     * 保存三会一课会议附件 (通知、纪要、决议、签到表、现场照片)
     * @param file 附件文件
     * @param meetingId 会议ID
     * @param attachType 附件分类标识
     * @return 存储相对路径
     */
    String storeMeetingAttachment(MultipartFile file, Long meetingId, String attachType) throws IOException;

    /**
     * 读取附件文件资源
     * @param relativePath 相对存储路径
     * @return Resource 资源句柄
     */
    Resource loadAsResource(String relativePath);

    /**
     * 删除文件
     * @param relativePath 相对路径
     */
    boolean deleteFile(String relativePath);
}
