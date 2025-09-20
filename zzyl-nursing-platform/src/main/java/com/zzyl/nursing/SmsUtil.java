package com.zzyl.nursing;


import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class SmsUtil {
//【注意：自定义-短信模版内容，需要联系服务商开通】
//            【测试模板ID：JM1000372
//    测试模板内容：您的验证码是@`0`@。如非本人操作，请忽略本短信。此短信为测试专用，无实际用途。】

    public static void sendSms(String mobile, String code) {
        String host = "https://jumsendsms.market.alicloudapi.com";
        String path = "/sms/send-upgrade";
        String method = "POST";
        String appcode = "efafe7016b8d4b3a9cff4bfc8dbaba08";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", mobile);
        querys.put("templateId", "JM1000372");
        querys.put("value", code);
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
