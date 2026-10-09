package com.honghe.party.notice;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsGateway {
    public SendSmsResponseBody send(String keyId, String keySecret, SendSmsRequest request) throws Exception {
        Config config = new Config().setAccessKeyId(keyId).setAccessKeySecret(keySecret)
                .setEndpoint("dysmsapi.aliyuncs.com").setProtocol("HTTPS");
        RuntimeOptions runtime = new RuntimeOptions().setConnectTimeout(5000).setReadTimeout(15000).setAutoretry(false);
        return new Client(config).sendSmsWithOptions(request, runtime).getBody();
    }
}
