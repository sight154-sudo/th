package com.tanhua.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.common.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @author: tang
 * @date: Create in 11:16 2021/8/5
 * @description:
 */
@Service
@Slf4j
public class UserService {


    @Autowired
    private RestTemplate restTemplate;

    @Value("${tanhua.sso.url}")
    private String ssoUrl;

    public User queryUserByToken(String token) {
        //调用sso接口查询用户信息  使用restClient访问sso
        String url = ssoUrl+"/user/"+token;
        //发送请求  返回json类型的数据
        String data = restTemplate.getForObject(url, String.class);
        try {
            if(StringUtils.isBlank(data)){
                return null;
            }
            return new ObjectMapper().readValue(data, User.class);
        } catch (IOException e) {
            log.error("解析用户token信息失败~token="+token);
        }
        return null;
    }
}
