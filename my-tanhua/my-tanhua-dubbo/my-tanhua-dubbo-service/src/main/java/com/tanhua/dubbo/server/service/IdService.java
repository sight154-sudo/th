package com.tanhua.dubbo.server.service;

import com.tanhua.dubbo.server.enums.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 21:59 2021/8/7
 * @description:
 */
@Service
public class IdService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public Long getPublishId(IdType idType){
        String key = "TANHUA_ID_"+idType.toString();
        return this.redisTemplate.opsForValue().increment(key);
    }
}
