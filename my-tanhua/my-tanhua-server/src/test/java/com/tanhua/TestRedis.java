package com.tanhua;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * @author: tang
 * @date: Create in 19:51 2021/8/15
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {


    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Test
    public void testDemo(){
//        System.out.println(redisTemplate);
        Set<Object> person = this.redisTemplate.boundHashOps("person").keys();
        System.out.println(person);
    }

    @Test
    public void testRedisSet(){
        this.redisTemplate.opsForSet().add("hobby","music","game");

    }
}
