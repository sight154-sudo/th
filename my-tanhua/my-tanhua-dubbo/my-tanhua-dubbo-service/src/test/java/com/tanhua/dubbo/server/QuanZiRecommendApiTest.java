package com.tanhua.dubbo.server;

import com.tanhua.DubboApplication;
import com.tanhua.dubbo.server.api.QuanZiApi;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 15:04 2021/8/9
 * @description:
 */
@SpringBootTest(classes = DubboApplication.class)
@RunWith(SpringRunner.class)
public class QuanZiRecommendApiTest {

    @Autowired
    private QuanZiApi quanZiApi;

    @Test
    public void testaaa(){
        PageInfo<Publish> pageInfo = quanZiApi.queryRecommendPublishList(1l, 1, 10);
        System.out.println(pageInfo);
    }

}
