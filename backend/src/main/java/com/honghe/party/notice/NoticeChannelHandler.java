package com.honghe.party.notice;

import com.honghe.party.entity.SysNoticeChannel;
import com.honghe.party.notice.dto.ChannelSendResult;
import com.honghe.party.notice.dto.NoticeMessagePayload;

public interface NoticeChannelHandler {
    /**
     * 获取渠道唯一标识
     */
    String getChannelCode();

    /**
     * 获取渠道中文名称
     */
    String getChannelName();

    default void validateConfig(SysNoticeChannel channel) { }

    /**
     * 发送真实党建通知报文
     * @param channel 渠道配置对象（包含真实 configJson 等参数）
     * @param payload 通知业务消息体
     */
    ChannelSendResult send(SysNoticeChannel channel, NoticeMessagePayload payload);

    /**
     * 联通性测试与鉴权探活
     * @param channel 渠道配置对象
     * @param testTarget 测试接收地址
     */
    ChannelSendResult testConnection(SysNoticeChannel channel, String testTarget);
}
