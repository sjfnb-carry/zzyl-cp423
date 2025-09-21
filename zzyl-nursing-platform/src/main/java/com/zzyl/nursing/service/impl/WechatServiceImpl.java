package com.zzyl.nursing.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.nursing.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WechatServiceImpl implements WechatService {


    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.appsecret}")
    private String appSecret;

    /** 获取微信openId的请求url地址 */
    private final String openIdUrl
            = "https://api.weixin.qq.com/sns/jscode2session?" +
            "appid=APPID" +
            "&secret=SECRET" +
            "&js_code=JSCODE" +
            "&grant_type=authorization_code";

    /** 获取微信绑定的手机号的请求url地址 */
    private final String phoneUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber" +
            "?access_token=ACCESS_TOKEN";

    /** 获取微信access_token的请求url地址 */
    private final String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
            "&appid=APPID" +
            "&secret=APPSECRET";

    @Override
    public String getOpenid(String code) {
        try {
            //发送HTTP GET请求
            String url = openIdUrl
                    .replace("APPID", appid)
                    .replace("SECRET", appSecret)
                    .replace("JSCODE", code);
            log.info("获取微信openid请求url：{}", url);
            String result = HttpUtil.get(url);
            log.info("获取微信openid结果：{}", result);
            JSONObject jsonObject = JSON.parseObject(result);
            String openid = jsonObject.getString("openid");
            return openid;
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException("获取微信openid失败");
        }
    }

    @Override
    public String getPhone(String detailCode) {
        try {
            String url = phoneUrl.replace("ACCESS_TOKEN", getAccessToken());

            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("code", detailCode);
            String result = HttpUtil.post(url, JSONUtil.toJsonStr(paramMap));
            log.info("获取微信手机号结果：{}", result);
            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject phoneInfo = jsonObject.getJSONObject("phone_info");
            if(phoneInfo!=null){
                return phoneInfo.getString("purePhoneNumber");
            }
            throw new ServiceException("获取微信绑定手机号失败");
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException("获取微信绑定手机号失败");
        }
    }

    /**
     * 获取微信access_token
     * @return access_token
     */
    private CharSequence getAccessToken() {
        String url = accessTokenUrl.replace("APPID", appid).replace("APPSECRET", appSecret);
        String result = HttpUtil.get(url);
        JSONObject jsonObject = JSON.parseObject(result);
        log.info("获取微信access_token结果：{}", result);
        String accessToken = jsonObject.getString("access_token");
        return accessToken;
    }
}