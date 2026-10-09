package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.auth.RequestUser;
import com.honghe.party.common.Result;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.entity.SysNoticeLog;
import com.honghe.party.entity.SysUser;
import com.honghe.party.mapper.SysNoticeChannelMapper;
import com.honghe.party.mapper.SysNoticeLogMapper;
import com.honghe.party.mapper.SysUserMapper;
import com.honghe.party.notice.ChannelConfig;
import com.honghe.party.notice.NoticeChannelFactory;
import com.honghe.party.notice.NoticeChannelHandler;
import com.honghe.party.service.NoticeDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoticeControllerTest {

    @Mock private SysNoticeChannelMapper channelMapper;
    @Mock private SysNoticeLogMapper logMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private NoticeDispatchService noticeDispatchService;
    @Mock private NoticeChannelFactory channelFactory;
    @Spy private ChannelConfig configs = new ChannelConfig();

    @InjectMocks
    private NoticeController noticeController;

    private SysNoticeChannel channel;
    private SysNoticeLog logRecord;
    private RequestUser adminUser;

    @BeforeEach
    void setUp() {
        channel = new SysNoticeChannel();
        channel.setId(1L);
        channel.setChannelCode("IN_APP");
        channel.setChannelName("系统站内信");
        channel.setEnabled(1);
        channel.setConfigJson("{}");

        logRecord = new SysNoticeLog();
        logRecord.setId(100L);
        logRecord.setTitle("测试通知");
        logRecord.setIsRead(0);
        logRecord.setReceiverId(1L);

        SysUser u = new SysUser();
        u.setId(1L);
        u.setRealName("系统管理员");
        adminUser = new RequestUser(u, Set.of("SYS_ADMIN"));
    }

    @Test
    @DisplayName("测试获取所有渠道列表")
    void testGetChannels() {
        when(channelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(channel));

        Result<List<SysNoticeChannel>> result = noticeController.getChannels();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("IN_APP", result.getData().get(0).getChannelCode());
    }

    @Test
    @DisplayName("测试更新渠道配置并执行参数结构校验")
    void testUpdateChannel() {
        when(channelMapper.selectById(1L)).thenReturn(channel);
        NoticeChannelHandler handler = mock(NoticeChannelHandler.class);
        when(channelFactory.getHandler("IN_APP")).thenReturn(handler);

        SysNoticeChannel updateReq = new SysNoticeChannel();
        updateReq.setConfigJson("{}");
        updateReq.setEnabled(1);
        updateReq.setRemark("站内信配置");

        Result<SysNoticeChannel> result = noticeController.updateChannel(1L, updateReq);

        assertEquals(200, result.getCode());
        verify(channelMapper, times(1)).updateById(any(SysNoticeChannel.class));
        verify(handler, times(1)).validateConfig(any());
    }

    @Test
    @DisplayName("测试渠道测试接口：委托调度服务下发并返回真实受理状态")
    void testChannelPing() {
        when(channelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(channel);

        SysNoticeLog sent = new SysNoticeLog();
        sent.setChannelCode("IN_APP");
        sent.setSendStatus(1);
        sent.setProviderMessageId("MSG_TEST_99");

        when(noticeDispatchService.sendNotice(eq("IN_APP"), any(), any(), any(), eq("USER"), eq(1L), any(), any(), any(), any()))
                .thenReturn(sent);

        Map<String, String> payload = new HashMap<>();
        payload.put("target", "admin@honghe.com");

        Result<com.honghe.party.notice.dto.ChannelSendResult> result = noticeController.testChannel("IN_APP", payload, adminUser);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isSuccess());
        assertEquals("MSG_TEST_99", result.getData().getMessageId());
    }

    @Test
    @DisplayName("测试标记单条通知已读：仅限本人通知")
    void testMarkAsRead() {
        when(logMapper.selectById(100L)).thenReturn(logRecord);

        Result<String> result = noticeController.markAsRead(100L, adminUser);

        assertEquals(200, result.getCode());
        assertEquals(1, logRecord.getIsRead());
        verify(logMapper, times(1)).updateById(logRecord);
    }

    @Test
    @DisplayName("测试全部标记已读")
    void testMarkAllAsRead() {
        Result<String> result = noticeController.markAllAsRead(adminUser);

        assertEquals(200, result.getCode());
        verify(logMapper, times(1)).update(any(), any());
    }

    @Test
    @DisplayName("测试手动触发合规预警扫描")
    void testTriggerWarnings() {
        when(noticeDispatchService.triggerComplianceWarningNotices()).thenReturn(3);

        Result<Map<String, Object>> result = noticeController.triggerWarnings();

        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("本次已受理 3 条"));
    }
}
