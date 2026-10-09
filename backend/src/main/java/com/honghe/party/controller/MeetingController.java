package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyMeetingAttachment;
import com.honghe.party.entity.PartyMeetingRecord;
import com.honghe.party.mapper.PartyMeetingAttachmentMapper;
import com.honghe.party.mapper.PartyMeetingRecordMapper;
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
import java.util.*;

@RestController
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    private PartyMeetingRecordMapper meetingMapper;

    @Autowired
    private PartyMeetingAttachmentMapper attachmentMapper;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 查询三会一课及主题党日会议台账列表
     */
    @GetMapping("/list")
    public Result<List<PartyMeetingRecord>> listMeetings(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer meetingType,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<PartyMeetingRecord> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(PartyMeetingRecord::getOrgId, orgId);
        }
        if (meetingType != null) {
            wrapper.eq(PartyMeetingRecord::getMeetingType, meetingType);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(PartyMeetingRecord::getMeetingTitle, keyword)
                              .or().like(PartyMeetingRecord::getModeratorName, keyword));
        }
        wrapper.orderByDesc(PartyMeetingRecord::getMeetingDate)
               .orderByDesc(PartyMeetingRecord::getId);
        return Result.success("获取会议台账成功", meetingMapper.selectList(wrapper));
    }

    /**
     * 新增三会一课/主题党日记录
     */
    @PostMapping("/add")
    public Result<PartyMeetingRecord> addMeeting(@RequestBody PartyMeetingRecord record) {
        if (record.getOrgId() == null || record.getMeetingType() == null || record.getMeetingTitle() == null) {
            return Result.error(400, "所属党支部、会议类型和会议主题为必填项！");
        }
        record.setCreatedAt(LocalDateTime.now());
        meetingMapper.insert(record);
        return Result.success("会议纪要与组织生活记录保存成功！", record);
    }

    /**
     * 管理员修改/更新三会一课会议记录及决议内容
     */
    @PutMapping("/{id}")
    public Result<PartyMeetingRecord> updateMeeting(
            @PathVariable Long id,
            @RequestBody PartyMeetingRecord updateForm) {
        PartyMeetingRecord exist = meetingMapper.selectById(id);
        if (exist == null) {
            return Result.error(404, "未找到该会议记录");
        }
        if (updateForm.getMeetingTitle() != null) exist.setMeetingTitle(updateForm.getMeetingTitle());
        if (updateForm.getMeetingType() != null) exist.setMeetingType(updateForm.getMeetingType());
        if (updateForm.getOrgId() != null) exist.setOrgId(updateForm.getOrgId());
        if (updateForm.getMeetingDate() != null) exist.setMeetingDate(updateForm.getMeetingDate());
        if (updateForm.getMeetingPlace() != null) exist.setMeetingPlace(updateForm.getMeetingPlace());
        if (updateForm.getModeratorName() != null) exist.setModeratorName(updateForm.getModeratorName());
        if (updateForm.getSpeakerName() != null) exist.setSpeakerName(updateForm.getSpeakerName());
        if (updateForm.getExpectedCount() != null) exist.setExpectedCount(updateForm.getExpectedCount());
        if (updateForm.getActualCount() != null) exist.setActualCount(updateForm.getActualCount());
        if (updateForm.getMeetingContent() != null) exist.setMeetingContent(updateForm.getMeetingContent());
        if (updateForm.getAttendeeNames() != null) exist.setAttendeeNames(updateForm.getAttendeeNames());
        if (updateForm.getRelatedMemberId() != null) exist.setRelatedMemberId(updateForm.getRelatedMemberId());
        if (updateForm.getRelatedStepCode() != null) exist.setRelatedStepCode(updateForm.getRelatedStepCode());

        meetingMapper.updateById(exist);
        return Result.success("会议记录与决议纪实已成功更新！", exist);
    }

    /**
     * 上传会议附件（支持：NOTICE:会议通知, MINUTES:会议纪要, RESOLUTION:表决决议, SIGNIN:签到表, PHOTO:现场照片）
     */
    @PostMapping("/{meetingId}/upload")
    public Result<PartyMeetingAttachment> uploadAttachment(
            @PathVariable Long meetingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("attachType") String attachType,
            @RequestParam(required = false) String attachTypeName,
            @RequestParam(defaultValue = "组织委员") String operatorName) throws IOException {

        PartyMeetingRecord meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            return Result.error(404, "未找到该会议台账记录");
        }

        String storedPath = fileStorageService.storeMeetingAttachment(file, meetingId, attachType);

        PartyMeetingAttachment attachment = new PartyMeetingAttachment();
        attachment.setMeetingId(meetingId);
        attachment.setAttachType(attachType.toUpperCase());
        attachment.setAttachTypeName(attachTypeName != null ? attachTypeName : mapTypeName(attachType));
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(storedPath);
        attachment.setFileSize(file.getSize());
        attachment.setUploaderName(operatorName);
        attachment.setCreatedAt(LocalDateTime.now());

        attachmentMapper.insert(attachment);

        // 如果是纪要，同步更新主表的docFilePath
        if ("MINUTES".equalsIgnoreCase(attachType)) {
            meeting.setDocFilePath(storedPath);
            meetingMapper.updateById(meeting);
        }

        return Result.success("会议附件上传归档成功！", attachment);
    }

    /**
     * 查询某次会议的所有归档附件清单
     */
    @GetMapping("/{meetingId}/attachments")
    public Result<List<PartyMeetingAttachment>> listAttachments(@PathVariable Long meetingId) {
        List<PartyMeetingAttachment> list = attachmentMapper.selectList(
            new LambdaQueryWrapper<PartyMeetingAttachment>()
                .eq(PartyMeetingAttachment::getMeetingId, meetingId)
                .orderByAsc(PartyMeetingAttachment::getId)
        );
        return Result.success("获取会议附件清单成功", list);
    }

    /**
     * 下载会议附件 (通知/纪要/决议/签到表/照片)
     */
    @GetMapping("/attachment/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        PartyMeetingAttachment attachment = attachmentMapper.selectById(id);
        if (attachment == null || attachment.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileStorageService.loadAsResource(attachment.getFilePath());
        String encodedName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    /**
     * 删除会议附件
     */
    @DeleteMapping("/attachment/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        PartyMeetingAttachment attachment = attachmentMapper.selectById(id);
        if (attachment != null) {
            fileStorageService.deleteFile(attachment.getFilePath());
            attachmentMapper.deleteById(id);
        }
        return Result.success("附件删除成功", null);
    }

    private String mapTypeName(String attachType) {
        if ("NOTICE".equalsIgnoreCase(attachType)) return "会议通知";
        if ("MINUTES".equalsIgnoreCase(attachType)) return "会议纪要";
        if ("RESOLUTION".equalsIgnoreCase(attachType)) return "表决决议书";
        if ("SIGNIN".equalsIgnoreCase(attachType)) return "签到考勤表";
        if ("PHOTO".equalsIgnoreCase(attachType)) return "现场纪实照片";
        return "规范材料";
    }
}
