package com.tanhua.dubbo.server;

import com.tanhua.common.pojo.HuanXinUser;
import com.tanhua.dubbo.server.api.HuanXinApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 10:57 2021/8/13
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HuanXinApiTest {

    @Autowired
    private HuanXinApi huanXinApi;


    @Test
    public void testRegister(){
        for (int i = 1; i < 100; i++) {
            huanXinApi.register(Long.valueOf(i));
        }
    }


    @Test
    public void testQueryUser(){
        HuanXinUser huanXinUser = huanXinApi.queryHuanXinUser(1l);
        System.out.println(huanXinUser);
    }
}
