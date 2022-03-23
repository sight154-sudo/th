package com.tanhua.dubbo.server.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tanhua.dubbo.server.config.HuanXinConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: tang
 * @date: Create in 21:12 2021/8/12
 * @description: 获取登陆环信的token信息
 */

@Service
@Slf4j
public class TokenService {


    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private HuanXinConfig config;


    private static final String HUANXIN_KEY= "HX_TOKEN";


    public String getToken(){
        //是否命中缓存
        String redisKey = HUANXIN_KEY;
        String s = this.redisTemplate.opsForValue().get(redisKey);
        //未命中 则请求环信获取token
        if(StrUtil.isNotBlank(s)){
            return s;
        }
        return this.refreshToken();
    }

    /**
     * 获取管理员权限的token
     * @return
     */
    public String refreshToken() {
        //向环信发送请求获取
        //http://a1.easemob.com/easemob-demo/testapp/token
        String url = config.getUrl()+config.getOrgName()+"/"+config.getAppName()+"/token";
        Map<String,Object> map = new HashMap<>();
        map.put("grant_type","client_credentials");
        map.put("client_id",config.getClientId());
        map.put("client_secret",config.getClientSecret());
        String body = JSONUtil.toJsonStr(map);
        HttpResponse response = HttpRequest.post(url)
                .body(body)
                .timeout(20000)//超时时间 单位毫秒
                .execute();
        if(!response.isOk()){
           //请求失败
            log.error("请求获取环信token失败");
            return null;
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        String token = jsonObject.getStr("access_token");
        if(StrUtil.isEmpty(token)){
            log.error("请求获取环信token失败");
            return null;
        }
        //存放到redis中
        String redisKey = HUANXIN_KEY;
        Long expiresTime = jsonObject.getLong("expires_in") - 3600;
        this.redisTemplate.opsForValue().set(redisKey,token,expiresTime, TimeUnit.SECONDS);
        return token;
    }
}
