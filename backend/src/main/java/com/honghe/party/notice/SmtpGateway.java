package com.honghe.party.notice;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpGateway {
    public String send(JsonNode cfg, String password, String target, String title, String content) throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(ChannelConfig.required(cfg, "host"));
        sender.setPort((int) ChannelConfig.positiveLong(cfg, "port"));
        sender.setUsername(ChannelConfig.required(cfg, "username"));
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");
        var props = sender.getJavaMailProperties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.connectiontimeout", "5000");
        props.setProperty("mail.smtp.timeout", "15000");
        props.setProperty("mail.smtp.writetimeout", "15000");
        props.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        boolean ssl = "SSL".equals(cfg.path("security").asText());
        props.setProperty("mail.smtp.ssl.enable", String.valueOf(ssl));
        props.setProperty("mail.smtp.starttls.enable", String.valueOf(!ssl));
        props.setProperty("mail.smtp.starttls.required", String.valueOf(!ssl));
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(ChannelConfig.required(cfg, "from"));
        helper.setTo(target);
        helper.setSubject(title);
        helper.setText(content, false);
        sender.send(message);
        return message.getMessageID();
    }
}
