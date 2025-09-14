package com.zzyl.framework.config;

import com.huaweicloud.sdk.core.auth.AbstractCredentials;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.region.Region;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.zzyl.framework.config.properties.HuaWeiIotConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IotClientConfig {
    @Autowired
    private HuaWeiIotConfigProperties hwp;

    /**
     * 创建华为云IoTDA客户端实例
     *
     * @return IoTDAClient 华为云IoTDA客户端实例
     */
    @Bean
    public IoTDAClient huaWeiIotInstance() {
        //创建认证信息，配置项目ID、AK/SK密钥对以及衍生算法
        ICredential auth = new BasicCredentials()
                .withProjectId(hwp.getProjectId())
                // 标准版/企业版需要使用衍生算法，基础版请删除配置"withDerivedPredicate";
                .withDerivedPredicate(AbstractCredentials.DEFAULT_DERIVED_PREDICATE) // Used in derivative ak/sk authentication scenarios
                .withAk(hwp.getAk())
                .withSk(hwp.getSk());

        //构建IoTDAClient客户端，配置认证信息和区域信息
        return IoTDAClient.newBuilder()
                .withCredential(auth)
                // 标准版/企业版：需自行创建Region对象，基础版：请使用IoTDARegion的region对象，如"withRegion(IoTDARegion.CN_NORTH_4)"
                .withRegion(new Region(hwp.getRegionId(), hwp.getEndpoint()))
                .build();
    }


}
