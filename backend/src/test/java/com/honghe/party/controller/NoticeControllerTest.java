package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.entity.SysNoticeLog;
import com.honghe.party.mapper.SysNoticeChannelMapper;
import com.honghe.party.mapper.SysNoticeLogMapper;
import com.honghe.party.service.NoticeDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoticeControllerTest {

    @Mock
    private SysNoticeChannelMapper channelMapper;

    @Mock
    private SysNoticeLogMapper logMapper;

    @Mock
    private NoticeDispatchService noticeDispatchService;

    @Mock
    private com.honghe.party.notice.NoticeChannelFactory channelFactory;

    @InjectMocks
    private NoticeController noticeController;

    private SysNoticeChannel channel;
    private SysNoticeLog logRecord;

    @BeforeEach
    void setUp() {
        channel = new SysNoticeChannel();
        channel.setId(1L);
        channel.setChannelCode("IN_APP");
        channel.setChannelName("系统站内信");
        channel.setEnabled(1);

        logRecord = new SysNoticeLog();
        logRecord.setId(100L);
        logRecord.setTitle("测试通知");
        logRecord.setIsRead(0);
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
    @DisplayName("测试更新渠道配置")
    void testUpdateChannel() {
        Result<SysNoticeChannel> result = noticeController.updateChannel(1L, channel);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getUpdatedAt());
        verify(channelMapper, times(1)).updateById(channel);
    }

    @Test
    @DisplayName("测试渠道连通性测试接口")
    void testChannelPing() {
        when(channelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(channel);
        when(channelFactory.getHandler("IN_APP")).thenReturn(new com.honghe.party.notice.handler.InAppChannelHandler());

        Map<String, String> payload = new HashMap<>();
        payload.put("target", "admin@honghe.com");

        Result<com.honghe.party.notice.dto.ChannelSendResult> result = noticeController.testChannel("IN_APP", payload);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isSuccess());
        assertEquals("IN_APP", result.getData().getChannelCode());
    }

    @Test
    @DisplayName("测试标记单条通知已读")
    void testMarkAsRead() {
        when(logMapper.selectById(100L)).thenReturn(logRecord);

        Result<String> result = noticeController.markAsRead(100L);

        assertEquals(200, result.getCode());
        assertEquals(1, logRecord.getIsRead());
        assertNotNull(logRecord.getReadTime());
        verify(logMapper, times(1)).updateById(logRecord);
    }

    @Test
    @DisplayName("测试全部标记已读")
    void testMarkAllAsRead() {
        when(logMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(logRecord));

        Result<String> result = noticeController.markAllAsRead();

        assertEquals(200, result.getCode());
        assertEquals(1, logRecord.getIsRead());
        verify(logMapper, times(1)).updateById(logRecord);
    }

    @Test
    @DisplayName("测试手动触发合规预警扫描")
    void testTriggerWarnings() {
        when(noticeDispatchService.triggerComplianceWarningNotices()).thenReturn(3);

        Result<Map<String, Object>> result = noticeController.triggerWarnings();

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().get("dispatchedCount"));
    }
}
