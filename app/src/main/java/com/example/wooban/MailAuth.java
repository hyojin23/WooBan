package com.example.wooban;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MailAuth extends Authenticator{

    PasswordAuthentication pa;

    public MailAuth() {
        String mail_id = "kimhj9292";
        String mail_pw = "khj10305806!";

        pa = new PasswordAuthentication(mail_id, mail_pw);
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return pa;
    }
}


