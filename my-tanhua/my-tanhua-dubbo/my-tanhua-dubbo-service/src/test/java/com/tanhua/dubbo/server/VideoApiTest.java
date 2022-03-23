package com.tanhua.dubbo.server;

import com.tanhua.DubboApplication;
import com.tanhua.dubbo.server.api.VideoApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 11:38 2021/8/11
 * @description:
 */
@SpringBootTest(classes = DubboApplication.class)
@RunWith(SpringRunner.class)
public class VideoApiTest {

    @Autowired
    private VideoApi videoApi;

    @Test
    public void testVideoList(){
        videoApi.queryVideoList(1l,1,12).getRecords().forEach(v-> System.out.println(v));
        System.out.println("==============");
        videoApi.queryVideoList(1l,3,12).getRecords().forEach(v-> System.out.println(v));
        System.out.println("==============");
        videoApi.queryVideoList(1l,4,12).getRecords().forEach(v-> System.out.println(v));
    }
}
