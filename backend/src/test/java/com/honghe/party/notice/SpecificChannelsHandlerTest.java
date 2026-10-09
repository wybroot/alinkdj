package com.honghe.party.notice;

import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import com.honghe.party.notice.handler.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpecificChannelsHandlerTest {

    @Test
    @DisplayName("测试企业微信ChannelHandler：协议验证与缺失校验")
    void testWeChatWork() {
        WeChatWorkChannelHandler handler = new WeChatWorkChannelHandler();
        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"corpId\":\"ww123\",\"agentId\":\"100008\",\"secret\":\"sec_abc\"}");

        NoticeMessagePayload payload = NoticeMessagePayload.builder()
                .title("【企微】党务通知")
                .content("测试内容")
                .receiverTarget("zhangqiang")
                .build();

        ChannelSendResult res = handler.send(channel, payload);
        assertTrue(res.isSuccess());
        assertEquals("WECHAT_WORK", res.getChannelCode());
        assertNotNull(res.getMessageId());

        // 测试配置缺失校验
        SysNoticeChannel invalidChannel = new SysNoticeChannel();
        invalidChannel.setConfigJson("{}");
        ChannelSendResult failRes = handler.send(invalidChannel, payload);
        assertFalse(failRes.isSuccess());
        assertTrue(failRes.getErrorMsg().contains("缺失"));
    }

    @Test
    @DisplayName("测试钉钉ChannelHandler：Markdown消息与鉴权校验")
    void testDingTalk() {
        DingTalkChannelHandler handler = new DingTalkChannelHandler();
        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"appKey\":\"ding_key\",\"appSecret\":\"ding_sec\",\"agentId\":\"29876543\"}");

        NoticeMessagePayload payload = NoticeMessagePayload.builder()
                .title("【钉钉】三会一课通知")
                .content("支部党员大会即将召开")
                .receiverTarget("user_001")
                .build();

        ChannelSendResult res = handler.send(channel, payload);
        assertTrue(res.isSuccess());
        assertEquals("DINGTALK", res.getChannelCode());
        assertNotNull(res.getMessageId());
    }

    @Test
    @DisplayName("测试106短信ChannelHandler：手机号合规性校验与模板参数替换")
    void testSms() {
        SmsChannelHandler handler = new SmsChannelHandler();
        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"signName\":\"红河数据集团党总支\",\"tplDeadline\":\"SMS_001928\"}");

        // 正确手机号
        NoticeMessagePayload validPayload = NoticeMessagePayload.builder()
                .title("【短信】转正催办")
                .content("预备期满1年前请提交申请")
                .receiverTarget("13987301005")
                .receiverName("杨海")
                .build();

        ChannelSendResult res = handler.send(channel, validPayload);
        assertTrue(res.isSuccess());
        assertEquals("SMS", res.getChannelCode());

        // 非法手机号
        NoticeMessagePayload invalidPayload = NoticeMessagePayload.builder()
                .title("【短信】转正催办")
                .content("预备期满1年前请提交申请")
                .receiverTarget("invalid_phone_number")
                .build();

        ChannelSendResult failRes = handler.send(channel, invalidPayload);
        assertFalse(failRes.isSuccess());
        assertTrue(failRes.getErrorMsg().contains("不符合规范"));
    }

    @Test
    @DisplayName("测试邮件ChannelHandler：邮箱格式校验与SMTP协议封装")
    void testEmail() {
        EmailChannelHandler handler = new EmailChannelHandler();
        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"host\":\"mail.honghe-data.com\",\"port\":465,\"user\":\"party@honghe.com\"}");

        NoticeMessagePayload validPayload = NoticeMessagePayload.builder()
                .title("【邮件】月度党建纪实")
                .content("请查收附件")
                .receiverTarget("admin@honghe-data.com")
                .build();

        ChannelSendResult res = handler.send(channel, validPayload);
        assertTrue(res.isSuccess());
        assertEquals("EMAIL", res.getChannelCode());

        // 非法邮箱
        NoticeMessagePayload invalidPayload = NoticeMessagePayload.builder()
                .title("【邮件】月度党建纪实")
                .content("请查收附件")
                .receiverTarget("not_an_email")
                .build();

        ChannelSendResult failRes = handler.send(channel, invalidPayload);
        assertFalse(failRes.isSuccess());
        assertTrue(failRes.getErrorMsg().contains("格式不正确"));
    }
}
