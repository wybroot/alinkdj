package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.honghe.party.auth.RequestUser;
import com.honghe.party.common.Result;
import com.honghe.party.entity.*;
import com.honghe.party.mapper.*;
import com.honghe.party.notice.*;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.service.NoticeDispatchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired private SysNoticeChannelMapper channelMapper;
    @Autowired private SysNoticeLogMapper logMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private NoticeDispatchService noticeDispatchService;
    @Autowired private NoticeChannelFactory channelFactory;
    @Autowired private ChannelConfig configs;

    private static final Map<String, Set<String>> CONFIG_FIELDS = Map.of(
            "IN_APP", Set.of(),
            "WECHAT_WORK", Set.of("corpId", "agentId", "secretEnv"),
            "DINGTALK", Set.of("corpId", "clientId", "agentId", "clientSecretEnv"),
            "SMS", Set.of("provider", "signName", "accessKeyIdEnv", "accessKeySecretEnv"),
            "EMAIL", Set.of("host", "port", "username", "from", "security", "passwordEnv"));

    private SysNoticeChannel safeChannel(SysNoticeChannel channel) {
        SysNoticeChannel safe = new SysNoticeChannel();
        BeanUtils.copyProperties(channel, safe);
        ObjectNode cfg = (ObjectNode) configs.read(channel);
        cfg.retain(CONFIG_FIELDS.getOrDefault(channel.getChannelCode(), Set.of()));
        safe.setConfigJson(cfg.toString());
        return safe;
    }

    @GetMapping("/channels")
    public Result<List<SysNoticeChannel>> getChannels() {
        return Result.success(channelMapper.selectList(new LambdaQueryWrapper<SysNoticeChannel>().orderByAsc(SysNoticeChannel::getId))
                .stream().map(this::safeChannel).toList());
    }

    @GetMapping("/available-channels")
    public Result<List<SysNoticeChannel>> availableChannels() {
        return Result.success(channelMapper.selectList(new LambdaQueryWrapper<SysNoticeChannel>().eq(SysNoticeChannel::getEnabled, 1))
                .stream().map(channel -> {
                    SysNoticeChannel view = new SysNoticeChannel();
                    view.setId(channel.getId()); view.setChannelCode(channel.getChannelCode()); view.setChannelName(channel.getChannelName()); view.setEnabled(1);
                    return view;
                }).toList());
    }

    @PutMapping("/channels/{id}")
    public Result<SysNoticeChannel> updateChannel(@PathVariable Long id, @RequestBody SysNoticeChannel request) {
        SysNoticeChannel existing = channelMapper.selectById(id);
        if (existing == null) return Result.error(404, "通知渠道不存在");
        try {
            if (request.getConfigJson() != null) {
                JsonNode cfg = configs.parse(request.getConfigJson());
                var names = cfg.fieldNames();
                while (names.hasNext()) {
                    String name = names.next();
                    if (!CONFIG_FIELDS.getOrDefault(existing.getChannelCode(), Set.of()).contains(name)) {
                        return Result.error(400, "配置含不支持字段；密钥须改为服务器环境变量引用");
                    }
                }
                existing.setConfigJson(cfg.toString());
            }
            if (request.getTemplateJson() != null) existing.setTemplateJson(configs.parse(request.getTemplateJson()).toString());
            if (request.getEnabled() != null) {
                if (request.getEnabled() != 0 && request.getEnabled() != 1) return Result.error(400, "启用状态无效");
                existing.setEnabled(request.getEnabled());
            }
            if (request.getRemark() != null) existing.setRemark(request.getRemark());
            if (Integer.valueOf(1).equals(existing.getEnabled())) {
                NoticeChannelHandler handler = channelFactory.getHandler(existing.getChannelCode());
                if (handler == null) return Result.error(400, "不支持该通知渠道");
                handler.validateConfig(existing);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            channelMapper.updateById(existing);
            return Result.success("通知渠道配置已保存", safeChannel(existing));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/channels/{channelCode}/test")
    public Result<ChannelSendResult> testChannel(@PathVariable String channelCode, @RequestBody Map<String, String> payload,
                                                @RequestAttribute(RequestUser.ATTRIBUTE) RequestUser principal) {
        String code = channelCode.toUpperCase(Locale.ROOT);
        SysNoticeChannel channel = channelMapper.selectOne(new LambdaQueryWrapper<SysNoticeChannel>().eq(SysNoticeChannel::getChannelCode, code));
        if (channel == null) return Result.error(404, "通知渠道不存在");
        if (!Integer.valueOf(1).equals(channel.getEnabled())) return Result.error(400, "请先配置并启用渠道");
        String target = payload.get("target");
        if (!"IN_APP".equals(code) && (target == null || target.isBlank())) return Result.error(400, "请填写明确的测试接收地址");
        SysNoticeLog record = noticeDispatchService.sendNotice(code, "REGULAR", "通知渠道测试",
                "这是一条通知渠道配置测试消息，请核对接收情况。", "USER", "IN_APP".equals(code) ? principal.user().getId() : null,
                "测试人员", target, null, null);
        ChannelSendResult result = new ChannelSendResult();
        result.setChannelCode(code); result.setMessageId(record.getProviderMessageId()); result.setSendStatus(record.getSendStatus());
        result.setSuccess(Integer.valueOf(1).equals(record.getSendStatus())); result.setErrorMsg(record.getErrorMsg());
        return new Result<>(result.isSuccess() ? 200 : 502, result.isSuccess() ? "测试消息已受理，请核对收件情况" : record.getErrorMsg(), result);
    }

    @GetMapping("/recipients")
    public Result<List<SysUser>> recipients() {
        return Result.success(userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1).orderByAsc(SysUser::getId)));
    }

    private LambdaQueryWrapper<SysNoticeLog> visibleLogs(RequestUser principal) {
        var query = new LambdaQueryWrapper<SysNoticeLog>();
        if (!principal.managesNotices()) query.eq(SysNoticeLog::getReceiverId, principal.user().getId()).eq(SysNoticeLog::getSendStatus, 1);
        return query;
    }

    @GetMapping("/logs")
    public Result<List<SysNoticeLog>> getNoticeLogs(@RequestParam(required = false) String channelCode,
            @RequestParam(required = false) String noticeType, @RequestParam(required = false) Integer isRead,
            @RequestAttribute(RequestUser.ATTRIBUTE) RequestUser principal) {
        var query = visibleLogs(principal);
        if (channelCode != null && !channelCode.isBlank()) query.eq(SysNoticeLog::getChannelCode, channelCode);
        if (noticeType != null && !noticeType.isBlank()) query.eq(SysNoticeLog::getNoticeType, noticeType);
        if (isRead != null) query.eq(SysNoticeLog::getIsRead, isRead);
        query.orderByDesc(SysNoticeLog::getId).last("LIMIT 500");
        return Result.success(logMapper.selectList(query));
    }

    @PutMapping("/logs/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id, @RequestAttribute(RequestUser.ATTRIBUTE) RequestUser principal) {
        SysNoticeLog record = logMapper.selectById(id);
        if (record == null) return Result.error(404, "通知不存在");
        if (!Objects.equals(record.getReceiverId(), principal.user().getId())) return Result.error(403, "只能标记自己的通知为已读");
        record.setIsRead(1); record.setReadTime(LocalDateTime.now()); logMapper.updateById(record);
        return Result.success("已标记为已读");
    }

    @PostMapping("/logs/read-all")
    public Result<String> markAllAsRead(@RequestAttribute(RequestUser.ATTRIBUTE) RequestUser principal) {
        SysNoticeLog patch = new SysNoticeLog(); patch.setIsRead(1); patch.setReadTime(LocalDateTime.now());
        logMapper.update(patch, new LambdaQueryWrapper<SysNoticeLog>().eq(SysNoticeLog::getReceiverId, principal.user().getId()).eq(SysNoticeLog::getIsRead, 0));
        return Result.success("本人通知已全部标记为已读");
    }

    @PostMapping("/trigger-warnings")
    public Result<Map<String, Object>> triggerWarnings() {
        int count = noticeDispatchService.triggerComplianceWarningNotices();
        return Result.success("扫描完成，本次已受理 " + count + " 条；失败或缺少接收人的记录请查看通知台账", Map.of("dispatchedCount", count));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() { return Result.success(noticeDispatchService.getNoticeStatistics()); }

    @PostMapping("/send")
    public Result<SysNoticeLog> sendManualNotice(@RequestBody SysNoticeLog notice) {
        if (!"USER".equals(notice.getReceiverType())) return Result.error(400, "请选择明确的接收人，不接受全员或角色占位符");
        try {
            SysNoticeLog record = noticeDispatchService.sendNotice(notice.getChannelCode(), notice.getNoticeType(), notice.getTitle(), notice.getContent(),
                    "USER", notice.getReceiverId(), notice.getReceiverName(), notice.getReceiverTarget(), notice.getRelatedMemberId(), notice.getRelatedStepCode());
            boolean accepted = Integer.valueOf(1).equals(record.getSendStatus());
            return new Result<>(accepted ? 200 : 502, accepted ? "通知已受理，外部渠道实际送达以服务商回执为准" : record.getErrorMsg(), record);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
