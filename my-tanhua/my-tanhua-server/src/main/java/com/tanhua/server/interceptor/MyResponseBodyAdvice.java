package com.tanhua.server.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.common.utils.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: tang
 * @date: Create in 20:17 2021/8/6
 * @description:
 */
@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${tanhua.cache.enable}")
    private Boolean enable;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {

        return enable && methodParameter.hasMethodAnnotation(GetMapping.class) && methodParameter.hasMethodAnnotation(Cache.class);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest request, ServerHttpResponse serverHttpResponse) {
        if(Objects.isNull(o)){
            return null;
        }
        //将数据添加到缓存中
        String data = null;
        try {
            String redisKey = RedisCacheInterceptor.createRedisKey(((ServletServerHttpRequest)request).getServletRequest());
            Cache cache = methodParameter.getMethodAnnotation(Cache.class);
            long time = Long.valueOf(cache.time());
            if(!(o instanceof String)){
                //将数据写入到redis数据库中
                data = objectMapper.writeValueAsString(o);
            }
            redisTemplate.opsForValue().set(redisKey,data,time, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return o;
    }
}
