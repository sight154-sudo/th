package com.tanhua.dubbo.server;

import com.tanhua.dubbo.server.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 22:15 2021/8/12
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TokenServiceTest {


    @Autowired
    private TokenService tokenService;

    @Test
    public void getToken(){
        String token = tokenService.getToken();
        System.out.println(token);
    }
}
