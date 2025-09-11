package com.zzyl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class HttpUtilsTest {
    @Test
    public void testGet() {
        String result = HttpUtil.get("https://www.baidu.com");
        System.out.println(result);
    }

    @Test
    public void testGetByParam() {
        // 构建查询参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageNum", 1);
        paramMap.put("pageSize", 5);
        // 分页查询护理项目
        String result = HttpUtil.get("http://localhost:8080/nursing/project/list", paramMap);
        System.out.println(result);
    }

    @Test
    public void testCreateRequest() {
        // 构建查询参数
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("pageNum", 1);
        paramMap.put("pageSize", 5);
        // 分页查询护理项目
        HttpResponse response = HttpUtil.createRequest(Method.GET, "http://localhost:8080/nursing/project/list")
                .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6ImUyNTgyNzdhLTMxNzAtNGFhNS05MmNhLWRiNjFiNDYyOGYxNCJ9.p8ieeQe4A6lozsxpzTL4Z3JNAS3UgBOVRZckDFnNvJQs8C2I5X5nZVxAQCaUv_0u9yUN-d0FKDCHwtCkMeIdBA")
                .form(paramMap)
                .execute();
        if(response.isOk()){
            System.out.println(response.body());
        }
    }
}
