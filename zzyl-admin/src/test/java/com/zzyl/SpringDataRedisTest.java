package com.zzyl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class SpringDataRedisTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //测试string
    @Test
    public void testString() {
        redisTemplate.opsForValue().set("name", "zzyl");
        String name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
        redisTemplate.opsForValue().set("age", "18", 100, TimeUnit.SECONDS);
        Boolean age = redisTemplate.opsForValue().setIfAbsent("age", "18");
        System.out.println(age);

        System.out.println(redisTemplate.opsForValue().increment("age"));
    }

    //测试hash
    @Test
    public void testHash() {
        redisTemplate.opsForHash().put("user:1002", "username", "lisi");//hset
        redisTemplate.opsForHash().put("user:1002", "password", "123456");//hset
        redisTemplate.opsForHash().put("user:1002", "age", "23");//hset
        redisTemplate.opsForHash().put("user:1002", "gender", "男");//hset

        Object gender = redisTemplate.opsForHash().get("user:1002", "gender");
        System.out.println(gender);

        Set<Object> keys = redisTemplate.opsForHash().keys("user:1002");
        System.out.println(keys);

        List<Object> values = redisTemplate.opsForHash().values("user:1002");
        System.out.println(values);

        redisTemplate.opsForHash().delete("user:1002", "gender");
    }

    //测试list
    @Test
    public void testList() {
        ListOperations<String, String> opsForList = redisTemplate.opsForList();
        opsForList.leftPush("emplist", "张三");
        opsForList.leftPush("emplist", "李四");
        opsForList.leftPush("emplist", "王五");

        List<String> emplist = opsForList.range("emplist", 0, -1);
        System.out.println(emplist);

        String s = opsForList.rightPop("emplist");
        System.out.println(s);

        Long size = opsForList.size("emplist");
        System.out.println(size);//2

    }

    //测试set
    @Test
    public void testSet() {
        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        opsForSet.add("user:1002:skill", "java");
        opsForSet.add("user:1002:skill", "python");
        opsForSet.add("user:1002:skill", "c");
        opsForSet.add("user:1002:skill", "c++");
        opsForSet.add("user:1002:skill", "javascript");
        opsForSet.add("user:1002:skill", "mysql");
        opsForSet.add("user:1002:skill", "php");

        //smembers
        Set<String> members = opsForSet.members("user:1002:skill");
        System.out.println(members);

        //scard
        Long size = opsForSet.size("user:1002:skill");
        System.out.println(size);

        opsForSet.add("user:1001:skill", "vue");
        opsForSet.add("user:1001:skill", "css");
        opsForSet.add("user:1001:skill", "c");
        opsForSet.add("user:1001:skill", "鸿蒙");
        opsForSet.add("user:1001:skill", "javascript");
        opsForSet.add("user:1001:skill", "mysql");
        opsForSet.add("user:1001:skill", "php");

        Set<String> intersect = opsForSet.intersect("user:1002:skill", "user:1001:skill");
        System.out.println("交集：" + intersect);

        Set<String> union = opsForSet.union("user:1002:skill", "user:1001:skill");
        System.out.println("并集：" + union);

        Set<String> difference = opsForSet.difference("user:1001:skill", "user:1002:skill");
        System.out.println("差集：" + difference);
    }

    //测试zset
    @Test
    public void testZSet() {
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        //zadd
        opsForZSet.add("student", "张三", 89);
        opsForZSet.add("student", "李四", 95);
        opsForZSet.add("student", "王五", 55);
        //zrange
        Set<String> range = opsForZSet.range("student", 0, -1);
        System.out.println(range);

        //zrevrange
        Set<String> reverseRange = opsForZSet.reverseRange("student", 0, -1);
        System.out.println(reverseRange);

        //zincrby
        opsForZSet.incrementScore("student", "张三", 10);

        //zrangewithscore
        Set<ZSetOperations.TypedTuple<String>> student = opsForZSet.rangeWithScores("student", 0, -1);
        for (ZSetOperations.TypedTuple<String> stringTypedTuple : student) {
            System.out.println(stringTypedTuple.getValue()+":"+stringTypedTuple.getScore());

        }
        //zrem
        opsForZSet.remove("student", "张三");
        Set<String> student1 = opsForZSet.range("student", 0, -1);
        System.out.println(student1);

    }
}
