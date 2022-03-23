package com.tanhua.dubbo.server;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.tanhua.dubbo.server.config.HuanXinConfig;
import com.tanhua.dubbo.server.service.RequestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 9:44 2021/8/13
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RequestServiceTest {

    @Autowired
    private RequestService requestService;
    @Autowired
    private HuanXinConfig config;

    @Test
    public void testRequestService(){
        String url = config.getUrl()+config.getOrgName()+"/"+config.getAppName()+"/users/1";
        String body = null;
        HttpResponse response = requestService.execute(url, body, Method.GET);
        System.out.println(response.body());
    }

}
