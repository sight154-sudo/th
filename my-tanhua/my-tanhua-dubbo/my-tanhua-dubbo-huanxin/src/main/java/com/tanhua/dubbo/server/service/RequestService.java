package com.tanhua.dubbo.server.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.tanhua.dubbo.server.exception.NoAuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;


/**
 * @author: tang
 * @date: Create in 23:33 2021/8/12
 * @description:
 */
@Service
@Slf4j
public class RequestService {

    @Autowired
    private TokenService tokenService;

    /**
     * 通用发送请求方法
     * @param url
     * @param body
     * @param method
     * @return
     */
    @Retryable(value = NoAuthorizationException.class,maxAttempts = 5,backoff = @Backoff(delay = 2000l,multiplier = 2))
    public HttpResponse execute(String url, String body, Method method) {
        String token = this.tokenService.getToken();
        HttpRequest httpRequest;
        switch (method){
            case POST:
                httpRequest = HttpRequest.post(url);
                break;
            case GET:
                httpRequest = HttpRequest.get(url);
                break;
            case PUT:
                httpRequest = HttpRequest.put(url);
                break;
            case DELETE:
                httpRequest = HttpRequest.delete(url);
                break;
            default:
                return null;
        }
        HttpResponse response = httpRequest
                .header("Content-Type", "application/json")//设置请求头信息
                .header("Authorization", "Bearer " + token)//设置请求头信息
                .body(body)
                .timeout(20000)
                .execute();
        if(response.getStatus() == 401){
            //未授权[无token、token错误、token过期]  则需要重试
            this.tokenService.refreshToken();
            throw new NoAuthorizationException(url,body,method);
        }
        return response;
    }

    @Recover
    public HttpResponse recover(NoAuthorizationException e){
        //当所有机会全部重试失败后 则说明token错误
        log.error("获取token失败！url = " + e.getUrl() + ", body = " + e.getBody() + ", method = " + e.getMethod().toString());
        //如果重试5次后，依然不能获取到token，说明网络或账号出现了问题，只能返回null了，后续的请求将无法再执行
        return null;
    }
}
