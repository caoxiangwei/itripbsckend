package cn.itrip.auth.service;

public interface MailService {
    void sendMail(String mailAddress,String code) throws Exception;
}
