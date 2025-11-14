package com.appweb.application.service;

import com.appweb.application.config.ApplicationConfig;
import com.appweb.application.enums.Environment;
import com.appweb.application.model.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ApplicationConfig applicationConfig;
    private final JavaMailSender emailSender;


    public void sendEmail(Email email) throws MessagingException {
        Environment env = applicationConfig.getEnvironment();
        if (env == Environment.DEVELOPMENT) {
            log.debug("""
                             Email suppressed in {} mode.
                             to: {},
                             subject: {},
                             hasAttachment: {}
                            
                             Body:
                             {}
                            """,
                    env,
                    email.getTo(),
                    email.getSubject(),
                    email.getAttachmentPath() != null,
                    email.getBody()
            );
            return;
        }
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(applicationConfig.getContactEmail());
        helper.setTo(email.getTo());
        helper.setSubject(email.getSubject());
        helper.setText(email.getBody(), true);

        if (email.getAttachmentPath() != null && email.getAttachmentName() != null) {
            FileSystemResource file = new FileSystemResource(new File(email.getAttachmentPath()));
            helper.addAttachment(email.getAttachmentName(), file);
        }
        emailSender.send(message);
    }
}
