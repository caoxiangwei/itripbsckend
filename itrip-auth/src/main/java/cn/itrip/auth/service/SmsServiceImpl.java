package cn.itrip.auth.service;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class SmsServiceImpl implements SmsService {
    @Override
    public void sendSms(String to, String templateId, String[] datas) throws Exception{
        HashMap<String, Object> result = null;
        CCPRestSmsSDK restAPI = new CCPRestSmsSDK();
        restAPI.init("app.cloopen.com", "8883");
        // 初始化服务器地址和端口，生产环境配置成app.cloopen.com，端口是8883.
        restAPI.setAccount("8a216da8701eb7c1017033517db806f2", "d5bf2a2c4bd04327a92e4d723cb1890c");
        // 初始化主账号名称和主账号令牌，登陆云通讯网站后，可在控制首页中看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN。
        restAPI.setAppId("8a216da8701eb7c1017033517e1606f8");
        // 请使用管理控制台中已创建应用的APPID。
        result = restAPI.sendTemplateSMS(to,templateId ,datas);
        if("000000".equals(result.get("statusCode"))) {
            System.out.println("短信发送成功");
        }else {
            throw new Exception("短信发送失败");
        }
    }
}
