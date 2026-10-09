package com.honghe.party.notice.handler;

import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.NoticeChannelHandler;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 平台内置站内信 / 实时未读红点 (In-App Direct Notification)
 */
@Slf4j
@Component
public class InAppChannelHandler implements NoticeChannelHandler {

    @Override
    public String getChannelCode() {
        return "IN_APP";
    }

    @Override
    public String getChannelName() {
        return "系统站内信 / 实时红点";
    }

    @Override
    public ChannelSendResult send(SysNoticeChannel channel, NoticeMessagePayload payload) {
        if (payload.getReceiverId() == null) return ChannelSendResult.fail(getChannelCode(), "站内信必须选择系统接收人");
        String msgId = "MSG_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return ChannelSendResult.ok(getChannelCode(), msgId, "站内通知由调度服务持久化后可读取");
    }

    @Override
    public ChannelSendResult testConnection(SysNoticeChannel channel, String testTarget) {
        return ChannelSendResult.ok(getChannelCode(), "IN_APP_PING_OK", "系统站内信管道就绪");
    }
}
