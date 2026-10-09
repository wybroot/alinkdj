package com.honghe.party.notice;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import com.honghe.party.notice.handler.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailAuthenticationException;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SpecificChannelsHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private ChannelConfig stubConfigs(Map<String, String> env) {
        return new ChannelConfig() {
            @Override
            protected String environment(String name) {
                return env.get(name);
            }
        };
    }

    @Test
    @DisplayName("企业微信：成功置换令牌并提交发送，解析 msgid 与部分失败状态")
    void testWeChatWorkSuccessAndPartial() {
        ProviderHttpClient http = mock(ProviderHttpClient.class);
        AccessTokenCache tokens = new AccessTokenCache();
        ChannelConfig configs = stubConfigs(Map.of("PARTY_NOTICE_WECOM_SECRET", "test_secret"));
        WeChatWorkChannelHandler handler = new WeChatWorkChannelHandler(configs, http, tokens);

        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"corpId\":\"ww_corp_1\",\"agentId\":\"100008\",\"secretEnv\":\"PARTY_NOTICE_WECOM_SECRET\"}");

        when(http.get(any(URI.class))).thenReturn(mapper.createObjectNode().put("errcode", 0).put("access_token", "wx_tok_123").put("expires_in", 7200));
        when(http.post(any(URI.class), any())).thenReturn(mapper.createObjectNode().put("errcode", 0).put("msgid", "WX_MSG_8888").put("invaliduser", "resigned_user"));

        NoticeMessagePayload payload = NoticeMessagePayload.builder()
                .title("党务通知").content("测试正文").receiverTarget("zhangqiang|resigned_user").build();

        ChannelSendResult res = handler.send(channel, payload);
        assertNotNull(res);
        assertEquals("WX_MSG_8888", res.getMessageId());
        assertEquals(4, res.getSendStatus()); // 部分受理
        assertFalse(res.isSuccess());
        assertTrue(res.getErrorMsg().contains("部分接收人无效"));
    }

    @Test
    @DisplayName("钉钉：企业内部应用成功获取 token 并发起异步工作通知任务")
    void testDingTalkAsyncSend() {
        ProviderHttpClient http = mock(ProviderHttpClient.class);
        AccessTokenCache tokens = new AccessTokenCache();
        ChannelConfig configs = stubConfigs(Map.of("PARTY_NOTICE_DINGTALK_SECRET", "ding_sec_99"));
        DingTalkChannelHandler handler = new DingTalkChannelHandler(configs, http, tokens);

        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"corpId\":\"ding_org_1\",\"clientId\":\"suite_client\",\"agentId\":\"29876543\",\"clientSecretEnv\":\"PARTY_NOTICE_DINGTALK_SECRET\"}");

        when(http.post(eq(URI.create("https://api.dingtalk.com/v1.0/oauth2/ding_org_1/token")), any()))
                .thenReturn(mapper.createObjectNode().put("access_token", "dt_access_tok").put("expires_in", 7200));
        when(http.post(eq(URI.create("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2?access_token=dt_access_tok")), any()))
                .thenReturn(mapper.createObjectNode().put("errcode", 0).put("task_id", 192837465L));

        NoticeMessagePayload payload = NoticeMessagePayload.builder()
                .title("组织生活通知").content("明日召开支委会").receiverTarget("manager4974").build();

        ChannelSendResult res = handler.send(channel, payload);
        assertTrue(res.isSuccess());
        assertEquals(1, res.getSendStatus());
        assertEquals("192837465", res.getMessageId());
        assertTrue(res.getResponseRaw().contains("异步任务"));
    }

    @Test
    @DisplayName("阿里云短信：按通知类型匹配已审核模板并完成变量映射")
    void testAliyunSmsSuccessAndInvalidPhone() throws Exception {
        AliyunSmsGateway gateway = mock(AliyunSmsGateway.class);
        ChannelConfig configs = stubConfigs(Map.of(
                "PARTY_NOTICE_SMS_KEY_ID", "LTAI_TEST_ID",
                "PARTY_NOTICE_SMS_KEY_SECRET", "SECRET_KEY_TEST"
        ));
        SmsChannelHandler handler = new SmsChannelHandler(configs, gateway);

        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"provider\":\"ALIYUN\",\"signName\":\"红河数据产投\",\"accessKeyIdEnv\":\"PARTY_NOTICE_SMS_KEY_ID\",\"accessKeySecretEnv\":\"PARTY_NOTICE_SMS_KEY_SECRET\"}");
        channel.setTemplateJson("{\"DEADLINE_WARNING\":{\"templateCode\":\"SMS_88801\",\"parameters\":{\"name\":\"receiverName\",\"item\":\"title\"}}}");

        SendSmsResponseBody okBody = new SendSmsResponseBody().setCode("OK").setBizId("BIZ_SMS_999911");
        when(gateway.send(eq("LTAI_TEST_ID"), eq("SECRET_KEY_TEST"), any(SendSmsRequest.class))).thenReturn(okBody);

        NoticeMessagePayload payload = NoticeMessagePayload.builder()
                .noticeType("DEADLINE_WARNING").title("谈话到期提醒").content("请按期开展谈话")
                .receiverName("李卫民").receiverTarget("13987301005").build();

        ChannelSendResult res = handler.send(channel, payload);
        assertTrue(res.isSuccess());
        assertEquals(1, res.getSendStatus());
        assertEquals("BIZ_SMS_999911", res.getMessageId());

        // 非法手机号拦截，绝不调用远端 SDK
        NoticeMessagePayload badPhonePayload = NoticeMessagePayload.builder()
                .noticeType("DEADLINE_WARNING").title("谈话到期").content("正文")
                .receiverTarget("123456").build();
        ChannelSendResult badRes = handler.send(channel, badPhonePayload);
        assertFalse(badRes.isSuccess());
        assertEquals(2, badRes.getSendStatus());
        assertTrue(badRes.getErrorMsg().contains("大陆手机号"));
    }

    @Test
    @DisplayName("邮件（SMTP）：捕获认证异常并准确返回错误")
    void testEmailSmtpAuthFailure() throws Exception {
        SmtpGateway gateway = mock(SmtpGateway.class);
        ChannelConfig configs = stubConfigs(Map.of("PARTY_NOTICE_SMTP_PASSWORD", "auth_code_xyz"));
        EmailChannelHandler handler = new EmailChannelHandler(configs, gateway);

        SysNoticeChannel channel = new SysNoticeChannel();
        channel.setConfigJson("{\"host\":\"smtp.exmail.qq.com\",\"port\":465,\"security\":\"SSL\",\"username\":\"party@honghe.com\",\"from\":\"party@honghe.com\",\"passwordEnv\":\"PARTY_NOTICE_SMTP_PASSWORD\"}");

        doThrow(new MailAuthenticationException("Authentication failed"))
                .when(gateway).send(any(), eq("auth_code_xyz"), eq("admin@honghe.com"), any(), any());

        NoticeMessagePayload payload = NoticeMessagePayload.builder()
                .title("月度纪检通报").content("通报内容详情").receiverTarget("admin@honghe.com").build();

        ChannelSendResult res = handler.send(channel, payload);
        assertFalse(res.isSuccess());
        assertEquals(2, res.getSendStatus());
        assertTrue(res.getErrorMsg().contains("SMTP 鉴权失败"));
    }
}
