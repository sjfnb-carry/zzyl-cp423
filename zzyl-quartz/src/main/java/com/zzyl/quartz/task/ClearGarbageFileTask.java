package com.zzyl.quartz.task;

import cn.hutool.core.collection.CollectionUtil;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.oss.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ClearGarbageFileTask {
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;


    public void clearGarbageFile() {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(CacheConstants.GARBAGE_FILE);
            if (CollectionUtil.isEmpty(members)) {
                log.info("没有垃圾文件！");
                return;
            }
            List<String> memberList = members.stream().map(Object::toString).collect(Collectors.toList());

            aliyunOSSOperator.deleteFile(memberList);

            redisTemplate.delete(CacheConstants.GARBAGE_FILE);
            log.info("定时清理垃圾文件成功！");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("定时清理垃圾文件失败！");
        }

    }
}
