package com.honghe.party.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.entity.SysNoticeLog;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.SysNoticeChannelMapper;
import com.honghe.party.mapper.SysNoticeLogMapper;
import com.honghe.party.service.impl.NoticeDispatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoticeDispatchServiceTest {

    @Mock
    private SysNoticeLogMapper noticeLogMapper;

    @Mock
    private SysNoticeChannelMapper noticeChannelMapper;

    @Mock
    private PartyMemberMapper partyMemberMapper;

    @InjectMocks
    private NoticeDispatchServiceImpl noticeDispatchService;

    private SysNoticeChannel wechatChannel;

    @BeforeEach
    void setUp() {
        wechatChannel = new SysNoticeChannel();
        wechatChannel.setId(2L);
        wechatChannel.setChannelCode("WECHAT_WORK");
        wechatChannel.setChannelName("企业微信应用消息");
        wechatChannel.setEnabled(1);
    }

    @Test
    @DisplayName("测试发送通知 - 渠道正常启用")
    void testSendNoticeEnabled() {
        when(noticeChannelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(wechatChannel);

        SysNoticeLog log = noticeDispatchService.sendNotice(
                "WECHAT_WORK", "DEADLINE_WARNING", "【谈话临期】测试通知",
                "请尽快完成谈话", "USER", 101L, "张强", "13800000000", 101L, 2
        );

        assertNotNull(log);
        assertEquals(1, log.getSendStatus()); // 成功
        assertEquals("WECHAT_WORK", log.getChannelCode());
        assertEquals("【谈话临期】测试通知", log.getTitle());
        verify(noticeLogMapper, times(1)).insert(log);
    }

    @Test
    @DisplayName("测试发送通知 - 渠道被停用")
    void testSendNoticeDisabledChannel() {
        wechatChannel.setEnabled(0);
        when(noticeChannelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(wechatChannel);

        SysNoticeLog log = noticeDispatchService.sendNotice(
                "WECHAT_WORK", "DEADLINE_WARNING", "【谈话临期】测试通知",
                "请尽快完成谈话", "USER", 101L, "张强", "13800000000", 101L, 2
        );

        assertNotNull(log);
        assertEquals(2, log.getSendStatus()); // 失败
        assertTrue(log.getErrorMsg().contains("已被管理员停用"));
        verify(noticeLogMapper, times(1)).insert(log);
    }

    @Test
    @DisplayName("测试党务合规巡检 - 触发谈话超期与廉洁纪检把关预警")
    void testTriggerComplianceWarningNotices() {
        PartyMember m1 = new PartyMember();
        m1.setId(105L);
        m1.setRealName("陈思佳");
        m1.setCurrentStep(2);
        m1.setApplyDate(LocalDate.now().minusDays(22)); // 满22天，超20天触发

        PartyMember m2 = new PartyMember();
        m2.setId(102L);
        m2.setRealName("林雨涵");
        m2.setCurrentStep(13); // 第13步廉政意见

        when(partyMemberMapper.selectList(null)).thenReturn(Arrays.asList(m1, m2));
        when(noticeChannelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(wechatChannel);

        int count = noticeDispatchService.triggerComplianceWarningNotices();

        assertEquals(2, count);
        verify(noticeLogMapper, times(2)).insert(any(SysNoticeLog.class));
    }

    @Test
    @DisplayName("测试三会一课广播通知")
    void testSendMeetingBroadcast() {
        when(noticeChannelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(wechatChannel);

        List<String> attendees = Arrays.asList("朱文华", "李建忠", "周国平");
        int count = noticeDispatchService.sendMeetingBroadcast(1L, "10月支部大会", "2026-10-15", "党建活动室", attendees);

        assertEquals(3, count);
        verify(noticeLogMapper, times(3)).insert(any(SysNoticeLog.class));
    }

    @Test
    @DisplayName("测试通知统计数据获取")
    void testGetNoticeStatistics() {
        when(noticeLogMapper.selectCount(null)).thenReturn(10L);
        when(noticeLogMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(8L)  // successCount
                .thenReturn(2L)  // failCount
                .thenReturn(3L); // unreadCount

        when(noticeChannelMapper.selectList(null)).thenReturn(Collections.singletonList(wechatChannel));

        Map<String, Object> stats = noticeDispatchService.getNoticeStatistics();

        assertEquals(10L, stats.get("totalCount"));
        assertEquals(8L, stats.get("successCount"));
        assertEquals(2L, stats.get("failCount"));
        assertEquals(3L, stats.get("unreadCount"));
        assertNotNull(stats.get("channels"));
    }
}
