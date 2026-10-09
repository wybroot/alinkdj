package com.honghe.party.notice;

import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.StandardCharsets;

public abstract class AbstractChannelHandler implements NoticeChannelHandler {
    protected final ChannelConfig configs;

    protected AbstractChannelHandler(ChannelConfig configs) { this.configs = configs; }

    @Override
    public final ChannelSendResult send(SysNoticeChannel channel, NoticeMessagePayload payload) {
        try {
            validateConfig(channel);
            if (payload == null || payload.getTitle() == null || payload.getTitle().isBlank()
                    || payload.getContent() == null || payload.getContent().isBlank()) {
                throw new IllegalArgumentException("通知标题和正文不能为空");
            }
            if (payload.getTitle().contains("\r") || payload.getTitle().contains("\n")) {
                throw new IllegalArgumentException("通知标题不能包含换行");
            }
            // Explicit recipients only. Empty targets must never become a broadcast.
            if (payload.getReceiverTarget() == null || payload.getReceiverTarget().isBlank()) {
                throw new IllegalArgumentException("必须填写明确的接收地址");
            }
            return deliver(channel, payload);
        } catch (IllegalArgumentException e) {
            return ChannelSendResult.fail(getChannelCode(), e.getMessage());
        } catch (HttpStatusCodeException e) {
            return ChannelSendResult.fail(getChannelCode(), "服务商返回 HTTP " + e.getStatusCode().value() + "，请核对应用权限或稍后查询服务商记录");
        } catch (Exception e) {
            // Exception messages may contain a token-bearing URL, SMTP credential or request body.
            return ChannelSendResult.unknown(getChannelCode(), "通信异常或响应无法确认；请先查询服务商记录，勿直接重复发送");
        }
    }

    protected abstract ChannelSendResult deliver(SysNoticeChannel channel, NoticeMessagePayload payload) throws Exception;

    protected String text(NoticeMessagePayload payload, int maxBytes) {
        String content = payload.getTitle() + "\n" + payload.getContent();
        if (content.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            throw new IllegalArgumentException("该渠道通知标题与正文合计不能超过 " + maxBytes + " 个 UTF-8 字节");
        }
        return content;
    }

    @Override
    public ChannelSendResult testConnection(SysNoticeChannel channel, String target) {
        return send(channel, NoticeMessagePayload.builder().noticeType("REGULAR").receiverType("USER")
                .receiverTarget(target).receiverName("测试人员").title("通知渠道测试")
                .content("这是一条通知渠道配置测试消息，请核对接收情况。").build());
    }
}
