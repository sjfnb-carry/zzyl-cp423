package com.zzyl.nursing.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.nursing.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WechatServiceImpl implements WechatService {
    @Value("${wechat.appId}")
    private String appId;
    @Value("${wechat.appSecret}")
    private String appSecret;


    private static final String OpenIdUrl = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code";

    private static final String PhoneUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=ACCESS_TOKEN ";

    private static final String AccessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";


    /**
     * 根据微信授权码获取用户openid
     *
     * @param code 微信授权码，用于换取用户的唯一标识openid
     * @return 用户的openid字符串
     */
    @Override
    public String getOpenid(String code) {
        try {
            // 构造请求参数并发送HTTP GET请求获取openid
            Map<String, Object> params = new HashMap<>();
            params.put("appid", appId);
            params.put("secret", appSecret);
            params.put("js_code", code);
            String result = HttpUtil.get(OpenIdUrl, params);
            log.info("获取微信openid结果：{}", result);
            JSONObject jsonObject = JSON.parseObject(result);
            String openid = jsonObject.getString("openid");
            return openid;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("获取微信openid失败");
        }
    }


    @Override
    public String getPhone(String detailCode) {
        try {
            String url = PhoneUrl.replace("ACCESS_TOKEN", getAccessToken());
            HashMap<String, String> reqBody = new HashMap<>();
            reqBody.put("code", detailCode);
            String result = HttpUtil.post(url, JSON.toJSONString(reqBody));
            log.info("获取微信手机号结果：{}", result);
            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject phoneInfo = jsonObject.getJSONObject("phone_info");
            if (ObjUtil.isNotEmpty(phoneInfo)) {
                return phoneInfo.getString("purePhoneNumber");
            }
            throw new ServiceException("获取微信手机号失败");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("获取微信手机号失败");
        }
    }

    private String getAccessToken() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("appid", appId);
        params.put("secret", appSecret);
        String result = HttpUtil.get(AccessTokenUrl, params);
        JSONObject jsonObject = JSON.parseObject(result);
        log.info("获取微信access_token结果：{}", result);
        return jsonObject.getString("access_token");

    }
}