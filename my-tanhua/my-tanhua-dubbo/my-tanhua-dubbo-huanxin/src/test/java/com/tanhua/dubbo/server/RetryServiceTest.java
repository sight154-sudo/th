package com.tanhua.dubbo.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 23:31 2021/8/12
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RetryServiceTest {

    @Autowired
    private RetryService retryService;

    @Test
    public void retryTest(){
        retryService.execure(100);

    }
}
