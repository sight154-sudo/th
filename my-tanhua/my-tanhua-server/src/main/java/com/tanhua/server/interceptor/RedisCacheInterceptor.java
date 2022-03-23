package com.tanhua.server.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.common.utils.Cache;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: tang
 * @date: Create in 19:28 2021/8/6
 * @description: 拦截get方法的请求，对数据进行缓存
 */
@Component
public class RedisCacheInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${tanhua.cache.enable}")
    private Boolean enable;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //是否开启缓存
        if(!enable){
            return true;
        }
        //若不是handler方法 则不拦截
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        //若请求方法上没有getmapper注解不拦截
        if(!(((HandlerMethod) handler).hasMethodAnnotation(GetMapping.class))){
            return true;
        }
        //若没有cache注解也不拦截
        if(!(((HandlerMethod) handler).hasMethodAnnotation(Cache.class))){
            return true;
        }
        //拦截到指定方法 查询redis数据库中是否有缓存
        String redisKey = createRedisKey(request);
        String s = redisTemplate.opsForValue().get(redisKey);
        if(StringUtils.isBlank(s)){
            return true;
        }
        //响应数据
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(s);
        return false;
    }


    public static String createRedisKey(HttpServletRequest request) throws JsonProcessingException {
        //key : SERVER_CACHE_DATA_+M5d(url+param+token)
        StringBuilder sb = new StringBuilder();
        String suffix = sb.append(request.getRequestURI()).append("_").
                append(objectMapper.writeValueAsString(request.getParameterMap())).
                append("_").append(request.getHeader("Authorization")).toString();
        String data = DigestUtils.md5Hex(suffix);
        return "SERVER_CACHE_DATA_"+data;
    }
}
