package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.entity.SysNoticeLog;
import com.honghe.party.mapper.SysNoticeChannelMapper;
import com.honghe.party.mapper.SysNoticeLogMapper;
import com.honghe.party.service.NoticeDispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private SysNoticeChannelMapper channelMapper;

    @Autowired
    private SysNoticeLogMapper logMapper;

    @Autowired
    private NoticeDispatchService noticeDispatchService;

    /**
     * 获取所有通知渠道列表及配置状态
     */
    @GetMapping("/channels")
    public Result<List<SysNoticeChannel>> getChannels() {
        List<SysNoticeChannel> list = channelMapper.selectList(
                new LambdaQueryWrapper<SysNoticeChannel>().orderByAsc(SysNoticeChannel::getId)
        );
        return Result.success("获取通知渠道列表成功", list);
    }

    /**
     * 保存/更新渠道配置（启用、停用、更新Key）
     */
    @PutMapping("/channels/{id}")
    public Result<SysNoticeChannel> updateChannel(@PathVariable Long id, @RequestBody SysNoticeChannel channel) {
        channel.setId(id);
        channel.setUpdatedAt(LocalDateTime.now());
        channelMapper.updateById(channel);
        return Result.success("更新通知渠道配置成功", channel);
    }

    /**
     * 测试渠道联通性
     */
    @PostMapping("/channels/{channelCode}/test")
    public Result<String> testChannel(@PathVariable String channelCode, @RequestBody Map<String, String> payload) {
        String testTarget = payload.getOrDefault("target", "admin@honghe.com");
        noticeDispatchService.sendNotice(channelCode, "REGULAR", "【联通性测试】红河智慧党建渠道测试消息",
                "这是一条渠道连通性验证测试消息，来自红河数据产业集团智慧党建系统。",
                "USER", 1L, "测试接收人", testTarget, null, null);
        return Result.success("测试消息已成功推送并留痕", "OK");
    }

    /**
     * 分页/列表获取通知中心消息台账
     */
    @GetMapping("/logs")
    public Result<List<SysNoticeLog>> getNoticeLogs(@RequestParam(required = false) String channelCode,
                                                    @RequestParam(required = false) String noticeType,
                                                    @RequestParam(required = false) Integer isRead) {
        LambdaQueryWrapper<SysNoticeLog> wrapper = new LambdaQueryWrapper<>();
        if (channelCode != null && !channelCode.isEmpty()) {
            wrapper.eq(SysNoticeLog::getChannelCode, channelCode);
        }
        if (noticeType != null && !noticeType.isEmpty()) {
            wrapper.eq(SysNoticeLog::getNoticeType, noticeType);
        }
        if (isRead != null) {
            wrapper.eq(SysNoticeLog::getIsRead, isRead);
        }
        wrapper.orderByDesc(SysNoticeLog::getId);
        return Result.success("获取通知中心记录成功", logMapper.selectList(wrapper));
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/logs/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        SysNoticeLog log = logMapper.selectById(id);
        if (log != null) {
            log.setIsRead(1);
            log.setReadTime(LocalDateTime.now());
            logMapper.updateById(log);
        }
        return Result.success("标记已读成功");
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/logs/read-all")
    public Result<String> markAllAsRead() {
        List<SysNoticeLog> unreadList = logMapper.selectList(
                new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getIsRead, 0)
        );
        for (SysNoticeLog l : unreadList) {
            l.setIsRead(1);
            l.setReadTime(LocalDateTime.now());
            logMapper.updateById(l);
        }
        return Result.success("已全部标记为已读");
    }

    /**
     * 手动触发合规预警自动扫描推送
     */
    @PostMapping("/trigger-warnings")
    public Result<Map<String, Object>> triggerWarnings() {
        int count = noticeDispatchService.triggerComplianceWarningNotices();
        return Result.success("合规扫描完成，触发预警分发共 " + count + " 条", Map.of("dispatchedCount", count));
    }

    /**
     * 获取通知统计指标（各渠道、成功率、未读数等）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success("获取通知统计成功", noticeDispatchService.getNoticeStatistics());
    }

    /**
     * 手动发送通知（支持多渠道、广播）
     */
    @PostMapping("/send")
    public Result<SysNoticeLog> sendManualNotice(@RequestBody SysNoticeLog notice) {
        SysNoticeLog record = noticeDispatchService.sendNotice(
                notice.getChannelCode(),
                notice.getNoticeType(),
                notice.getTitle(),
                notice.getContent(),
                notice.getReceiverType(),
                notice.getReceiverId(),
                notice.getReceiverName(),
                notice.getReceiverTarget(),
                notice.getRelatedMemberId(),
                notice.getRelatedStepCode()
        );
        return Result.success("通知发送成功", record);
    }
}
