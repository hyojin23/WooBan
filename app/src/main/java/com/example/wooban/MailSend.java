package com.example.wooban;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSend {



    public String MailSend(String temp, String email) {

//      /* 영문 대소문자, 숫자를 섞은 문자열 생성
////        영문자는 int 타입의 숫자를 char 타입으로 캐스팅 하면 아스키코드 문자로 변환됨*/
////
////        // StringBuffer 객체 생성
////        StringBuffer temp = new StringBuffer();
////        // 랜덤수 생성
////        Random random = new Random();
////        for (int i = 0; i < 6; i++) {
////
////            // 0~2까지의 랜덤한 숫자
////            int rIndex = random.nextInt(3);
////            switch (rIndex) {
////                case 0:
////                    // 영소문자 a-z (아스키코드 97~122)
////                    temp.append((char) ((random.nextInt(26)) + 97));
////                    break;
////                case 1:
////                    // 영대문자 A-Z (아스키코드 65~122)
////                    temp.append((char) ((random.nextInt(26)) + 65));
////                    break;
////                case 2:
////                    // 숫자 0-9
////                    temp.append((random.nextInt(10)));
////                    break;
////            }
////        }


        Properties prop = System.getProperties();
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", "587");

        Authenticator auth = new MailAuth();

        Session session = Session.getDefaultInstance(prop, auth);

        MimeMessage msg = new MimeMessage(session);

        try {
            msg.setSentDate(new Date());

            msg.setFrom(new InternetAddress("kimhj9292@gmail.com", "우반"));
            InternetAddress to = new InternetAddress(email);
            msg.setRecipient(Message.RecipientType.TO, to);
            msg.setSubject("[우반] 회원가입 인증번호입니다.", "UTF-8");
            msg.setText("안녕하세요 우리들의 반려동물[우반]입니다. 인증번호는 " + temp + " 입니다.", "UTF-8");


            Transport.send(msg);

        } catch(AddressException ae) {
            System.out.println("AddressException : " + ae.getMessage());
        } catch(MessagingException me) {
            System.out.println("MessagingException : " + me.getMessage());
        } catch(UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException : " + e.getMessage());
        }
        return temp;
    }



}


