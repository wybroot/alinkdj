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

    public static ChannelSendResult ok(String channelCode, String messageId, String responseRaw) {
        return ChannelSendResult.builder()
                .success(true)
                .channelCode(channelCode)
                .messageId(messageId)
                .responseRaw(responseRaw)
                .build();
    }

    public static ChannelSendResult fail(String channelCode, String errorMsg) {
        return ChannelSendResult.builder()
                .success(false)
                .channelCode(channelCode)
                .errorMsg(errorMsg)
                .build();
    }
}
