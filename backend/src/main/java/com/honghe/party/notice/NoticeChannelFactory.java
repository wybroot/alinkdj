package com.honghe.party.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NoticeChannelFactory {

    private final Map<String, NoticeChannelHandler> handlerMap = new HashMap<>();

    @Autowired
    public NoticeChannelFactory(List<NoticeChannelHandler> handlers) {
        if (handlers != null) {
            for (NoticeChannelHandler h : handlers) {
                handlerMap.put(h.getChannelCode().toUpperCase(), h);
            }
        }
    }

    public NoticeChannelHandler getHandler(String channelCode) {
        if (channelCode == null) return handlerMap.get("IN_APP");
        return handlerMap.get(channelCode.trim().toUpperCase());
    }

    public Map<String, NoticeChannelHandler> getAllHandlers() {
        return handlerMap;
    }
}
