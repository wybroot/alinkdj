package com.honghe.party.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSendResult {
    private boolean success;
    private String messageId;
    private String channelCode;
    private String responseRaw;
    private String errorMsg;
    private int sendStatus; // 1 服务商受理/站内存储，2 失败，3 结果未知，4 部分受理

    public static ChannelSendResult ok(String channelCode, String messageId, String responseRaw) {
        return ChannelSendResult.builder()
                .success(true)
                .sendStatus(1)
                .channelCode(channelCode)
                .messageId(messageId)
                .responseRaw(responseRaw)
                .build();
    }

    public static ChannelSendResult fail(String channelCode, String errorMsg) {
        return ChannelSendResult.builder()
                .success(false)
                .sendStatus(2)
                .channelCode(channelCode)
                .errorMsg(errorMsg)
                .build();
    }

    public static ChannelSendResult unknown(String channelCode, String message) {
        ChannelSendResult result = fail(channelCode, message);
        result.setSendStatus(3);
        return result;
    }
}
