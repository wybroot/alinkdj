package com.honghe.party.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.entity.SysNoticeLog;
import com.honghe.party.entity.SysRole;
import com.honghe.party.entity.SysUser;
import com.honghe.party.entity.SysUserRole;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.SysNoticeChannelMapper;
import com.honghe.party.mapper.SysNoticeLogMapper;
import com.honghe.party.mapper.SysRoleMapper;
import com.honghe.party.mapper.SysUserMapper;
import com.honghe.party.mapper.SysUserRoleMapper;
import com.honghe.party.notice.NoticeChannelFactory;
import com.honghe.party.notice.NoticeChannelHandler;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoticeDispatchServiceTest {

    @Mock private SysNoticeLogMapper noticeLogMapper;
    @Mock private SysNoticeChannelMapper noticeChannelMapper;
    @Mock private PartyMemberMapper partyMemberMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysRoleMapper roleMapper;
    @Mock private NoticeChannelFactory channelFactory;

    @InjectMocks
    private NoticeDispatchServiceImpl noticeDispatchService;

    private SysNoticeChannel inAppChannel;
    private SysUser testUser;

    @BeforeEach
    void setUp() {
        inAppChannel = new SysNoticeChannel();
        inAppChannel.setId(1L);
        inAppChannel.setChannelCode("IN_APP");
        inAppChannel.setChannelName("系统站内信");
        inAppChannel.setEnabled(1);

        testUser = new SysUser();
        testUser.setId(101L);
        testUser.setRealName("张强");
        testUser.setWorkNo("HH-HS-012");
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("测试发送站内信通知：正常写入并持久化")
    void testSendInAppNoticeSuccess() {
        when(userMapper.selectById(101L)).thenReturn(testUser);
        when(noticeChannelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inAppChannel);

        NoticeChannelHandler inAppHandler = mock(NoticeChannelHandler.class);
        when(channelFactory.getHandler("IN_APP")).thenReturn(inAppHandler);
        when(inAppHandler.send(eq(inAppChannel), any(NoticeMessagePayload.class)))
                .thenReturn(ChannelSendResult.ok("IN_APP", "MSG_TEST_01", "delivered"));

        SysNoticeLog log = noticeDispatchService.sendNotice(
                "IN_APP", "MEETING_NOTICE", "支部大会通知", "请准时参会",
                "USER", 101L, "张强", null, null, null
        );

        assertNotNull(log);
        assertEquals(1, log.getSendStatus());
        assertEquals("IN_APP", log.getChannelCode());
        verify(noticeLogMapper, times(1)).insert(log);
        verify(noticeLogMapper, times(1)).updateById(log);
    }

    @Test
    @DisplayName("测试发送通知 - 渠道已停用则拒绝分发")
    void testSendNoticeDisabledChannel() {
        inAppChannel.setEnabled(0);
        when(userMapper.selectById(101L)).thenReturn(testUser);
        when(noticeChannelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inAppChannel);

        SysNoticeLog log = noticeDispatchService.sendNotice(
                "IN_APP", "REGULAR", "通知标题", "通知正文",
                "USER", 101L, "张强", null, null, null
        );

        assertNotNull(log);
        assertEquals(2, log.getSendStatus()); // 失败
        assertTrue(log.getErrorMsg().contains("未启用"));
    }

    @Test
    @DisplayName("合规扫描：自动根据成员组织查出对应支部管理员并定向分发，同日去重")
    void testTriggerComplianceWarningNotices() {
        PartyMember m1 = new PartyMember();
        m1.setId(105L);
        m1.setRealName("陈思佳");
        m1.setOrgId(1L);
        m1.setCurrentStep(2);
        m1.setApplyDate(LocalDate.now().minusDays(22));

        when(partyMemberMapper.selectList(null)).thenReturn(Collections.singletonList(m1));

        SysUser adminUser = new SysUser();
        adminUser.setId(2L);
        adminUser.setRealName("杨海");
        adminUser.setOrgId(1L);
        adminUser.setStatus(1);
        when(userMapper.selectList(any())).thenReturn(Collections.singletonList(adminUser));
        when(userMapper.selectById(2L)).thenReturn(adminUser);

        SysRole role = new SysRole();
        role.setId(2L);
        role.setRoleCode("GENERAL_BRANCH_ADMIN");
        role.setStatus(1);
        when(roleMapper.selectList(any())).thenReturn(Collections.singletonList(role));

        SysUserRole ur = new SysUserRole();
        ur.setUserId(2L);
        ur.setRoleId(2L);
        when(userRoleMapper.selectList(null)).thenReturn(Collections.singletonList(ur));

        SysNoticeChannel wxChannel = new SysNoticeChannel();
        wxChannel.setChannelCode("WECHAT_WORK");
        wxChannel.setEnabled(1);
        when(noticeChannelMapper.selectOne(any())).thenReturn(wxChannel);

        NoticeChannelHandler wxHandler = mock(NoticeChannelHandler.class);
        when(channelFactory.getHandler("WECHAT_WORK")).thenReturn(wxHandler);
        when(wxHandler.send(any(), any())).thenReturn(ChannelSendResult.ok("WECHAT_WORK", "WX_991", "ok"));

        int count = noticeDispatchService.triggerComplianceWarningNotices();
        assertEquals(1, count);
        verify(noticeLogMapper, times(1)).insert(any(SysNoticeLog.class));
    }

    @Test
    @DisplayName("测试通知统计查询")
    void testGetNoticeStatistics() {
        when(noticeLogMapper.selectCount(null)).thenReturn(10L);
        when(noticeLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(7L);

        Map<String, Object> stats = noticeDispatchService.getNoticeStatistics();
        assertEquals(10L, stats.get("totalCount"));
        assertEquals(7L, stats.get("acceptedCount"));
    }
}
