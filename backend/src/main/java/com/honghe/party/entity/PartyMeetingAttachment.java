package com.honghe.party.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("party_meeting_attachment")
public class PartyMeetingAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;           // 关联会议ID
    private String attachType;        // NOTICE:会议通知, MINUTES:会议纪要, RESOLUTION:会议决议, SIGNIN:签到表, PHOTO:现场照片
    private String attachTypeName;    // 类型中文名
    private String fileName;          // 原始文件名
    private String filePath;          // 本地存储相对路径
    private Long fileSize;            // 文件大小 (字节)
    private String uploaderName;      // 上传经办人
    private LocalDateTime createdAt;
}
