package com.appweb.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    private String to;
    private String subject;
    private String body;
    private String attachmentPath;
    private String attachmentName;

    @Override
    public String toString() {
        return "Email{" +
                "to='" + to + "'\n" +
                "subject='" + subject + "'\n" +
                "body='" + body + "'\n" +
                "attachmentPath='" + attachmentPath + "'\n" +
                "attachmentName='" + attachmentName + "'\n" +
                '}';
    }
}
