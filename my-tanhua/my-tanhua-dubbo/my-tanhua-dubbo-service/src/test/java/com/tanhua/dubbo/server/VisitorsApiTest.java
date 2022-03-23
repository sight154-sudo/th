package com.tanhua.dubbo.server;

import com.tanhua.DubboApplication;
import com.tanhua.dubbo.server.api.VisitorsApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 22:35 2021/8/14
 * @description:
 */
@SpringBootTest(classes = DubboApplication.class)
@RunWith(SpringRunner.class)
public class VisitorsApiTest {

    @Autowired
    private VisitorsApi visitorsApi;

    @Test
    public void saveVisitorTest(){
        visitorsApi.saveVisitor(1l,3l,"个人主页");
        visitorsApi.saveVisitor(1l,6l,"个人主页");
        visitorsApi.saveVisitor(1l,3l,"个人主页");
    }

    @Test
    public void queryVisitorsTest(){

        visitorsApi.queryMyVisitor(1l).forEach(v-> System.out.println(v));
    }

}
