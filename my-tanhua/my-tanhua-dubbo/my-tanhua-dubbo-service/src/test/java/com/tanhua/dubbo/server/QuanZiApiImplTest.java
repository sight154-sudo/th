package com.tanhua.dubbo.server;

import com.tanhua.DubboApplication;
import com.tanhua.dubbo.server.api.QuanZiApi;
import com.tanhua.dubbo.server.service.TimeLineService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;

/**
 * @author: tang
 * @date: Create in 16:57 2021/8/7
 * @description:
 */
@SpringBootTest(classes = DubboApplication.class)
@RunWith(SpringRunner.class)
public class QuanZiApiImplTest {


    @Autowired
    private QuanZiApi quanZiApi;

    @Autowired
    private TimeLineService timeLineService;

    /*@Test
    public void testQuziApiQueryPublishList(){
        quanZiApi.queryPublishList(18l, 1, 2).getRecords().forEach(o-> System.out.println(o));
        System.out.println("-------------");
        quanZiApi.queryPublishList(28l, 1, 2).getRecords().forEach(o-> System.out.println(o));
        System.out.println("-------------");
        quanZiApi.queryPublishList(29l, 1, 2).getRecords().forEach(o-> System.out.println(o));
        System.out.println("-------------");
    }*/


    @Test
    public void testSavePublish(){
        ObjectId objectId = ObjectId.get();
        System.out.println("生成的id为：" + objectId.toHexString());
        CompletableFuture<String> future = this.timeLineService.saveTimeLine(1L, objectId);
        future.whenComplete((s, throwable) -> {
            System.out.println("执行完成：" + s);
        });

        System.out.println("异步方法执行完成");


        try {
            future.get(); //阻塞当前的主线程，等待异步执行的结束
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Test
    public void testCommentFunction(){
        Boolean dislike = quanZiApi.dislikeComment(2l, "5fae54057e52992e78a3ff25");
        System.out.println("取消点赞: "+dislike);
        Boolean likeComment = quanZiApi.likeComment(2l, "5fae54057e52992e78a3ff25");
        System.out.println("点赞动态："+likeComment);
        Long count = quanZiApi.queryLikeCount("5fae54057e52992e78a3ff25");
        System.out.println("动态的点赞数为: "+count);
        Boolean isLike = quanZiApi.queryUserIsLike(2l, "5fae54057e52992e78a3ff25");
        System.out.println("该动态用户是否点赞: "+isLike);

    }
}
