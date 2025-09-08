package com.zzyl.common.ai;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.core.auth.Auth;
import com.baidubce.qianfan.model.chat.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaiduAIModelInvoker {
    @Autowired
    private BaiduAIProperties baiduAIProperties;

    public String qianfanInvoker(String prompt) {

        Qianfan qianfan = new Qianfan(Auth.TYPE_IAM, baiduAIProperties.getAccessKey(), baiduAIProperties.getSecretKey());
        ChatResponse response = qianfan.chatCompletion()
                .model(baiduAIProperties.getQianfanModel()) // 模型名称，要选择自己开通付费的模型
                .addMessage("user", prompt) // 聊天内容，可以设置多个，每个消息包含role（角色，user表示用户，assistant表示模型），content（消息内容）
                .temperature(baiduAIProperties.getTemperature()) // 采样参数，取值范围(0,1]
                .maxOutputTokens(baiduAIProperties.getMaxOutputTokens()) // 模型输出最大长度，取值范围[2, 2048]
                .responseFormat(baiduAIProperties.getResponseFormat())  // 模型输出格式，取值范围：text（文本）、json_object（JSON对象）
                .execute();
        String result = response.getResult();
        return result;
    }
}
